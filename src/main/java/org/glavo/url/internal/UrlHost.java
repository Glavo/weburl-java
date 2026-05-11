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

import org.glavo.url.internal.idna.UTS46;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal representation of a WHATWG URL host.
@NotNullByDefault
public sealed interface UrlHost permits UrlHost.Domain, UrlHost.Opaque, UrlHost.IPv4, UrlHost.IPv6 {
    /// The empty domain host.
    Domain EMPTY_DOMAIN = new Domain("");

    /// The `localhost` domain host.
    Domain LOCALHOST_DOMAIN = new Domain("localhost");

    /// Creates a domain host.
    static UrlHost domain(String value) {
        Objects.requireNonNull(value, "value");
        if (value.isEmpty()) {
            return EMPTY_DOMAIN;
        }
        if (value.equals("localhost")) {
            return LOCALHOST_DOMAIN;
        }
        return new Domain(value);
    }

    /// Creates an opaque host.
    static UrlHost opaque(String value) {
        return new Opaque(value);
    }

    /// Creates an IPv4 host.
    static UrlHost ipv4(int value) {
        return new IPv4(value);
    }

    /// Creates an IPv6 host from eight 16-bit address pieces.
    static IPv6 ipv6(int[] value) {
        if (value.length != 8) {
            throw new IllegalArgumentException("IPv6 address must contain eight pieces");
        }

        long highBits = 0;
        long lowBits = 0;
        for (int i = 0; i < 4; i++) {
            int piece = checkedIpv6Piece(value[i]);
            highBits = highBits << 16 | piece;
        }
        for (int i = 4; i < 8; i++) {
            int piece = checkedIpv6Piece(value[i]);
            lowBits = lowBits << 16 | piece;
        }
        return ipv6(highBits, lowBits);
    }

    /// Creates an IPv6 host from the high and low 64-bit halves of the address.
    static IPv6 ipv6(long highBits, long lowBits) {
        return new IPv6(highBits, lowBits);
    }

    /// Returns whether this host is an empty domain host.
    boolean isEmptyDomain();

    /// Serializes the host into the supplied builder.
    void serialize(StringBuilder output);

    /// Returns a Unicode display form for a domain host, or `null` when the serialized host should be used.
    @Nullable String displayString();

    /// Matches the serialized host at the given index and returns the end index, or `-1`.
    int matchSerialized(String input, int start);

    /// A domain host.
    record Domain(String value) implements UrlHost {
        /// Creates a domain host.
        public Domain {
            Objects.requireNonNull(value, "value");
        }

        /// Returns whether this host is an empty domain host.
        @Override
        public boolean isEmptyDomain() {
            return value.isEmpty();
        }

        /// Serializes the host into the supplied builder.
        @Override
        public void serialize(StringBuilder output) {
            output.append(value);
        }

        /// Returns a Unicode display form for a domain host, or `null` when the serialized host should be used.
        @Override
        public @Nullable String displayString() {
            if (value.isEmpty() || !containsPunycodeLabel(value)) {
                return null;
            }

            UTS46.Result unicode = UTS46.toUnicode(value, false, true, true, false, false, false);
            if (unicode.error() || unicode.value().equals(value) || StringUtils.isAsciiOnly(unicode.value())) {
                return null;
            }

            UTS46.Result ascii = UTS46.toAsciiForUrl(unicode.value(), false);
            return !ascii.error() && ascii.value().equals(value) ? unicode.value() : null;
        }

        /// Matches the serialized host at the given index and returns the end index, or `-1`.
        @Override
        public int matchSerialized(String input, int start) {
            return matchText(input, start, value);
        }
    }

    /// An opaque host.
    record Opaque(String value) implements UrlHost {
        /// Creates an opaque host.
        public Opaque {
            Objects.requireNonNull(value, "value");
        }

        /// Returns whether this host is an empty domain host.
        @Override
        public boolean isEmptyDomain() {
            return false;
        }

        /// Serializes the host into the supplied builder.
        @Override
        public void serialize(StringBuilder output) {
            output.append(value);
        }

        /// Returns no display override for opaque hosts.
        @Override
        public @Nullable String displayString() {
            return null;
        }

        /// Matches the serialized host at the given index and returns the end index, or `-1`.
        @Override
        public int matchSerialized(String input, int start) {
            return matchText(input, start, value);
        }
    }

    /// An IPv4 host.
    record IPv4(int address) implements UrlHost {
        /// Creates an IPv4 host.
        public IPv4 {
        }

        /// Returns whether this host is an empty domain host.
        @Override
        public boolean isEmptyDomain() {
            return false;
        }

        /// Serializes the host into the supplied builder.
        @Override
        public void serialize(StringBuilder output) {
            output.append((address >>> 24) & 0xff)
                    .append('.')
                    .append((address >>> 16) & 0xff)
                    .append('.')
                    .append((address >>> 8) & 0xff)
                    .append('.')
                    .append(address & 0xff);
        }

        /// Returns no display override for IPv4 hosts.
        @Override
        public @Nullable String displayString() {
            return null;
        }

        /// Matches the serialized host at the given index and returns the end index, or `-1`.
        @Override
        public int matchSerialized(String input, int start) {
            int index = start;
            for (int shift = 24; shift >= 0; shift -= 8) {
                if (shift != 24) {
                    if (!hasChar(input, index, '.')) {
                        return -1;
                    }
                    index++;
                }
                index = matchDecimalByte(input, index, (address >>> shift) & 0xff);
                if (index < 0) {
                    return -1;
                }
            }
            return index;
        }
    }

    /// An IPv6 host.
    record IPv6(long highBits, long lowBits) implements UrlHost {
        /// Creates an IPv6 host.
        public IPv6 {
        }

        /// Returns whether this host is an empty domain host.
        @Override
        public boolean isEmptyDomain() {
            return false;
        }

        /// Serializes the host into the supplied builder.
        @Override
        public void serialize(StringBuilder output) {
            output.append('[');

            int compress = findCompressedPieceIndex(highBits, lowBits);
            boolean ignoreZero = false;

            for (int pieceIndex = 0; pieceIndex <= 7; pieceIndex++) {
                int piece = ipv6Piece(highBits, lowBits, pieceIndex);
                if (ignoreZero && piece == 0) {
                    continue;
                } else if (ignoreZero) {
                    ignoreZero = false;
                }

                if (compress == pieceIndex) {
                    output.append(pieceIndex == 0 ? "::" : ":");
                    ignoreZero = true;
                    continue;
                }

                output.append(Integer.toHexString(piece));
                if (pieceIndex != 7) {
                    output.append(':');
                }
            }

            output.append(']');
        }

        /// Returns no display override for IPv6 hosts.
        @Override
        public @Nullable String displayString() {
            return null;
        }

        /// Matches the serialized host at the given index and returns the end index, or `-1`.
        @Override
        public int matchSerialized(String input, int start) {
            if (!hasChar(input, start, '[')) {
                return -1;
            }

            int index = start + 1;
            int compress = findCompressedPieceIndex(highBits, lowBits);
            boolean ignoreZero = false;

            for (int pieceIndex = 0; pieceIndex <= 7; pieceIndex++) {
                int piece = ipv6Piece(highBits, lowBits, pieceIndex);
                if (ignoreZero && piece == 0) {
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

                index = matchHexPiece(input, index, piece);
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
    }

    /// Matches a literal host string and returns the end index, or `-1`.
    private static int matchText(String input, int start, String value) {
        int end = start + value.length();
        return end <= input.length() && input.regionMatches(start, value, 0, value.length()) ? end : -1;
    }

    /// Returns one 16-bit IPv6 address piece.
    private static int ipv6Piece(long highBits, long lowBits, int index) {
        if (index < 4) {
            return (int) ((highBits >>> (48 - index * 16)) & 0xffffL);
        }
        return (int) ((lowBits >>> (48 - (index - 4) * 16)) & 0xffffL);
    }

    /// Checks an IPv6 piece and returns it unchanged.
    private static int checkedIpv6Piece(int value) {
        if ((value & ~0xffff) != 0) {
            throw new IllegalArgumentException("IPv6 address piece is out of range");
        }
        return value;
    }

    /// Finds the IPv6 zero run to compress during serialization.
    private static int findCompressedPieceIndex(long highBits, long lowBits) {
        int longestIndex = -1;
        int longestSize = 1;
        int foundIndex = -1;
        int foundSize = 0;

        for (int pieceIndex = 0; pieceIndex < 8; pieceIndex++) {
            if (ipv6Piece(highBits, lowBits, pieceIndex) != 0) {
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

    /// Returns whether the host contains an ASCII Compatible Encoding label.
    private static boolean containsPunycodeLabel(String value) {
        int labelStart = 0;
        while (labelStart < value.length()) {
            if (startsWithPunycodePrefix(value, labelStart)) {
                return true;
            }

            int dot = value.indexOf('.', labelStart);
            if (dot < 0) {
                return false;
            }
            labelStart = dot + 1;
        }
        return false;
    }

    /// Returns whether a label starts with the Punycode prefix.
    private static boolean startsWithPunycodePrefix(String value, int offset) {
        return offset + 4 <= value.length()
                && value.charAt(offset) == 'x'
                && value.charAt(offset + 1) == 'n'
                && value.charAt(offset + 2) == '-'
                && value.charAt(offset + 3) == '-';
    }

}
