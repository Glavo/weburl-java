/*
 * Copyright 2026 Glavo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glavo.url.internal;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Internal representation of a WHATWG URL host.
@NotNullByDefault
public final class UrlHost {
    /// The kind of host value.
    private final Kind kind;
    /// Domain or opaque host text.
    private final @Nullable String text;
    /// IPv4 address as an unsigned 32-bit value stored in a long.
    private final long ipv4Address;
    /// IPv6 address pieces.
    private final int @Nullable [] ipv6Address;

    /// Creates a host value.
    private UrlHost(Kind kind, @Nullable String text, long ipv4Address, int @Nullable [] ipv6Address) {
        this.kind = kind;
        this.text = text;
        this.ipv4Address = ipv4Address;
        this.ipv6Address = ipv6Address;
    }

    /// Creates a domain host.
    public static UrlHost domain(String value) {
        return new UrlHost(Kind.DOMAIN, value, 0, null);
    }

    /// Creates an opaque host.
    public static UrlHost opaque(String value) {
        return new UrlHost(Kind.OPAQUE, value, 0, null);
    }

    /// Creates an IPv4 host.
    public static UrlHost ipv4(long value) {
        return new UrlHost(Kind.IPV4, null, value, null);
    }

    /// Creates an IPv6 host.
    public static UrlHost ipv6(int[] value) {
        return new UrlHost(Kind.IPV6, null, 0, value.clone());
    }

    /// Returns whether this host is an empty domain host.
    public boolean isEmptyDomain() {
        return kind == Kind.DOMAIN && text != null && text.isEmpty();
    }

    /// Returns whether this host serializes to an empty string.
    public boolean isEmpty() {
        return text != null && text.isEmpty();
    }

    /// Serializes the host.
    public String serialize() {
        switch (kind) {
            case DOMAIN:
            case OPAQUE:
                return text == null ? "" : text;
            case IPV4:
                return serializeIpv4(ipv4Address);
            case IPV6:
                return "[" + serializeIpv6(ipv6Address == null ? new int[8] : ipv6Address) + "]";
            default:
                throw new AssertionError(kind);
        }
    }

    /// Matches the serialized host at the given index and returns the end index, or `-1`.
    int matchSerialized(String input, int start) {
        switch (kind) {
            case DOMAIN:
            case OPAQUE:
                String value = text == null ? "" : text;
                int end = start + value.length();
                return end <= input.length() && input.regionMatches(start, value, 0, value.length()) ? end : -1;
            case IPV4:
                return matchSerializedIpv4(input, start, ipv4Address);
            case IPV6:
                return matchSerializedIpv6(input, start, ipv6Address == null ? new int[8] : ipv6Address);
            default:
                throw new AssertionError(kind);
        }
    }

    /// Serializes an IPv4 address.
    private static String serializeIpv4(long address) {
        return ((address >>> 24) & 0xff) + "."
                + ((address >>> 16) & 0xff) + "."
                + ((address >>> 8) & 0xff) + "."
                + (address & 0xff);
    }

    /// Matches a serialized IPv4 address and returns the end index, or `-1`.
    private static int matchSerializedIpv4(String input, int start, long address) {
        int index = start;
        for (int shift = 24; shift >= 0; shift -= 8) {
            if (shift != 24) {
                if (!hasChar(input, index, '.')) {
                    return -1;
                }
                index++;
            }
            index = matchDecimalByte(input, index, (int) ((address >>> shift) & 0xff));
            if (index < 0) {
                return -1;
            }
        }
        return index;
    }

    /// Serializes an IPv6 address.
    private static String serializeIpv6(int[] address) {
        StringBuilder output = new StringBuilder();
        int compress = findCompressedPieceIndex(address);
        boolean ignoreZero = false;

        for (int pieceIndex = 0; pieceIndex <= 7; pieceIndex++) {
            if (ignoreZero && address[pieceIndex] == 0) {
                continue;
            } else if (ignoreZero) {
                ignoreZero = false;
            }

            if (compress == pieceIndex) {
                output.append(pieceIndex == 0 ? "::" : ":");
                ignoreZero = true;
                continue;
            }

            output.append(Integer.toHexString(address[pieceIndex]));
            if (pieceIndex != 7) {
                output.append(':');
            }
        }

        return output.toString();
    }

    /// Matches a serialized IPv6 address and returns the end index, or `-1`.
    private static int matchSerializedIpv6(String input, int start, int[] address) {
        if (!hasChar(input, start, '[')) {
            return -1;
        }

        int index = start + 1;
        int compress = findCompressedPieceIndex(address);
        boolean ignoreZero = false;

        for (int pieceIndex = 0; pieceIndex <= 7; pieceIndex++) {
            if (ignoreZero && address[pieceIndex] == 0) {
                continue;
            } else if (ignoreZero) {
                ignoreZero = false;
            }

            if (compress == pieceIndex) {
                if (pieceIndex == 0) {
                    if (!hasChar(input, index, ':') || !hasChar(input, index + 1, ':')) {
                        return -1;
                    }
                    index += 2;
                } else {
                    if (!hasChar(input, index, ':')) {
                        return -1;
                    }
                    index++;
                }
                ignoreZero = true;
                continue;
            }

            index = matchHexPiece(input, index, address[pieceIndex]);
            if (index < 0) {
                return -1;
            }
            if (pieceIndex != 7) {
                if (!hasChar(input, index, ':')) {
                    return -1;
                }
                index++;
            }
        }

        if (!hasChar(input, index, ']')) {
            return -1;
        }
        return index + 1;
    }

    /// Finds the IPv6 zero run to compress during serialization.
    private static int findCompressedPieceIndex(int[] address) {
        int longestIndex = -1;
        int longestSize = 1;
        int foundIndex = -1;
        int foundSize = 0;

        for (int pieceIndex = 0; pieceIndex < address.length; pieceIndex++) {
            if (address[pieceIndex] != 0) {
                if (foundSize > longestSize) {
                    longestIndex = foundIndex;
                    longestSize = foundSize;
                }
                foundIndex = -1;
                foundSize = 0;
            } else {
                if (foundIndex == -1) {
                    foundIndex = pieceIndex;
                }
                foundSize++;
            }
        }

        if (foundSize > longestSize) {
            return foundIndex;
        }
        return longestIndex;
    }

    /// Matches one decimal byte and returns the end index, or `-1`.
    private static int matchDecimalByte(String input, int start, int value) {
        int length = value >= 100 ? 3 : value >= 10 ? 2 : 1;
        if (start + length > input.length()) {
            return -1;
        }
        int divisor = length == 3 ? 100 : length == 2 ? 10 : 1;
        int index = start;
        for (int current = value; divisor > 0; divisor /= 10) {
            int digit = current / divisor;
            if (input.charAt(index) != (char) ('0' + digit)) {
                return -1;
            }
            index++;
            current %= divisor;
        }
        return index;
    }

    /// Matches one lower-case hexadecimal IPv6 piece and returns the end index, or `-1`.
    private static int matchHexPiece(String input, int start, int value) {
        int length = value == 0 ? 1 : (32 - Integer.numberOfLeadingZeros(value) + 3) / 4;
        if (start + length > input.length()) {
            return -1;
        }
        int index = start;
        for (int shift = (length - 1) * 4; shift >= 0; shift -= 4) {
            char digit = Character.forDigit((value >>> shift) & 0xf, 16);
            if (input.charAt(index) != digit) {
                return -1;
            }
            index++;
        }
        return index;
    }

    /// Returns whether a character exists at an index and equals the expected value.
    private static boolean hasChar(String input, int index, char expected) {
        return index >= 0 && index < input.length() && input.charAt(index) == expected;
    }

    /// Internal host kinds.
    @NotNullByDefault
    private enum Kind {
        /// A domain host.
        DOMAIN,
        /// An IPv4 host.
        IPV4,
        /// An IPv6 host.
        IPV6,
        /// An opaque host.
        OPAQUE
    }
}
