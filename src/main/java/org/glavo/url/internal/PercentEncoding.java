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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/// Percent encoding and decoding helpers for URL components.
@NotNullByDefault
final class PercentEncoding {
    /// Upper-case hexadecimal digits used by percent encoding.
    private static final String UPPER_HEX_DIGITS = "0123456789ABCDEF";

    /// Lazily initialized cache for the 256 possible percent-encoded byte strings.
    ///
    /// Cache slots are allowed to be initialized more than once by racing threads because each computed value is
    /// immutable and determined only by the slot index.
    private static final @Nullable String[] PERCENT_ENCODED_BYTE_STRINGS = new String[256];

    /// Creates no instances.
    private PercentEncoding() {
    }

    /// Decodes percent triplets from a byte sequence.
    static byte[] percentDecodeBytes(byte[] input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        for (int i = 0; i < input.length; i++) {
            int value = input[i] & 0xff;
            if (value == '%' && i + 2 < input.length
                    && Infra.isAsciiHex(input[i + 1] & 0xff)
                    && Infra.isAsciiHex(input[i + 2] & 0xff)) {
                output.write(hexValue(input[i + 1] & 0xff) * 16 + hexValue(input[i + 2] & 0xff));
                i += 2;
            } else {
                output.write(value);
            }
        }
        return output.toByteArray();
    }

    /// Decodes percent triplets from the UTF-8 bytes of a string.
    static byte[] percentDecodeString(String input) {
        return percentDecodeBytes(Utf8.encode(input));
    }

    /// Decodes valid percent triplets in a URL component as UTF-8.
    static String percentDecodeUtf8(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (isValidPercentTriplet(input, i, input.length())) {
                return Utf8.decode(percentDecodeString(input));
            }
        }
        return input;
    }

    /// Percent-encodes one Unicode code point with the given byte predicate.
    static String utf8PercentEncodeCodePoint(int codePoint, BytePredicate percentEncodePredicate) {
        if (codePoint >= 0 && codePoint <= 0x7f) {
            return percentEncodePredicate.test(codePoint)
                    ? percentEncodedByteString(codePoint)
                    : Character.toString((char) codePoint);
        }
        return utf8PercentEncodeString(new String(Character.toChars(codePoint)), percentEncodePredicate);
    }

    /// Percent-encodes a string with the given byte predicate.
    static String utf8PercentEncodeString(String input, BytePredicate percentEncodePredicate) {
        for (int index = 0; index < input.length(); ) {
            int codePoint = input.codePointAt(index);
            if (codePoint > 0x7f || percentEncodePredicate.test(codePoint)) {
                return utf8PercentEncodeString(input, percentEncodePredicate, index);
            }
            index += Character.charCount(codePoint);
        }
        return input;
    }

    /// Percent-encodes a string starting at the first known changed code point.
    private static String utf8PercentEncodeString(
            String input,
            BytePredicate percentEncodePredicate,
            int start
    ) {
        StringBuilder output = new StringBuilder(input.length());
        output.append(input, 0, start);
        for (int index = start; index < input.length(); ) {
            int codePoint = input.codePointAt(index);
            if (codePoint <= 0x7f) {
                if (percentEncodePredicate.test(codePoint)) {
                    output.append(percentEncodedByteString(codePoint));
                } else {
                    output.append((char) codePoint);
                }
            } else {
                byte[] bytes = Utf8.encode(new String(Character.toChars(codePoint)));
                for (byte b : bytes) {
                    int value = b & 0xff;
                    if (percentEncodePredicate.test(value)) {
                        output.append(percentEncodedByteString(value));
                    } else {
                        output.append((char) value);
                    }
                }
            }
            index += Character.charCount(codePoint);
        }
        return output.toString();
    }

    /// Returns whether the byte belongs to the C0 control percent encode set.
    static boolean isC0ControlPercentEncode(int value) {
        return value <= 0x1f || value > 0x7e;
    }

    /// Returns whether the byte belongs to the fragment percent encode set.
    static boolean isFragmentPercentEncode(int value) {
        return isC0ControlPercentEncode(value) || value == ' ' || value == '"' || value == '<'
                || value == '>' || value == '`';
    }

    /// Returns whether the byte belongs to the query percent encode set.
    static boolean isQueryPercentEncode(int value) {
        return isC0ControlPercentEncode(value) || value == ' ' || value == '"' || value == '#'
                || value == '<' || value == '>';
    }

    /// Returns whether the byte belongs to the special-query percent encode set.
    static boolean isSpecialQueryPercentEncode(int value) {
        return isQueryPercentEncode(value) || value == '\'';
    }

    /// Returns whether the byte belongs to the path percent encode set.
    static boolean isPathPercentEncode(int value) {
        return isQueryPercentEncode(value) || value == '?' || value == '`' || value == '{'
                || value == '}' || value == '^';
    }

    /// Returns whether the byte belongs to the userinfo percent encode set.
    static boolean isUserinfoPercentEncode(int value) {
        return isPathPercentEncode(value) || value == '/' || value == ':' || value == ';'
                || value == '=' || value == '@' || value == '[' || value == '\\' || value == ']'
                || value == '|';
    }

    /// Percent-encodes a query string using the special or non-special query encode set.
    static String percentEncodeQuery(String input, boolean special) {
        return utf8PercentEncodeString(input,
                special ? PercentEncoding::isSpecialQueryPercentEncode : PercentEncoding::isQueryPercentEncode);
    }

    /// Returns whether the character starts an invalid percent triplet at the given UTF-16 index.
    static boolean startsInvalidPercentTriplet(String input, int pointer) {
        return input.charAt(pointer) == '%'
                && !isValidPercentTriplet(input, pointer, input.length());
    }

    /// Returns whether the string contains a valid percent triplet at the given UTF-16 index.
    static boolean isValidPercentTriplet(String input, int pointer, int end) {
        return pointer + 2 < end
                && input.charAt(pointer) == '%'
                && Infra.isAsciiHex(input.charAt(pointer + 1))
                && Infra.isAsciiHex(input.charAt(pointer + 2));
    }

    /// Decodes the byte value represented by a valid percent triplet.
    static int percentEncodedByte(String input, int pointer) {
        return hexValue(input.charAt(pointer + 1)) * 16 + hexValue(input.charAt(pointer + 2));
    }

    /// Appends the UTF-8 bytes of one code point as percent escapes.
    static void appendUtf8PercentEncodedCodePoint(StringBuilder output, int codePoint) {
        if (codePoint <= 0x7f) {
            output.append(percentEncodedByteString(codePoint));
            return;
        }

        byte[] bytes = Utf8.encode(new String(Character.toChars(codePoint)));
        for (byte b : bytes) {
            output.append(percentEncodedByteString(b & 0xff));
        }
    }

    /// Converts an ASCII hexadecimal digit to its numeric value.
    private static int hexValue(int value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return value - 'a' + 10;
    }

    /// Returns one percent-encoded byte as a string.
    private static String percentEncodedByteString(int value) {
        String string = PERCENT_ENCODED_BYTE_STRINGS[value];
        if (string == null) {
            string = new String(new byte[]{
                    '%',
                    (byte) UPPER_HEX_DIGITS.charAt((value >>> 4) & 0xf),
                    (byte) UPPER_HEX_DIGITS.charAt(value & 0xf)
            }, StandardCharsets.ISO_8859_1);
            PERCENT_ENCODED_BYTE_STRINGS[value] = string;
        }
        return string;
    }

    /// Predicate over unsigned byte values.
    @FunctionalInterface
    @NotNullByDefault
    interface BytePredicate {
        /// Returns whether the byte should be percent-encoded.
        boolean test(int value);
    }
}
