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

    /// Serializes an IPv4 address.
    private static String serializeIpv4(long address) {
        return ((address >>> 24) & 0xff) + "."
                + ((address >>> 16) & 0xff) + "."
                + ((address >>> 8) & 0xff) + "."
                + (address & 0xff);
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
