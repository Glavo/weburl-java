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
import org.jetbrains.annotations.Unmodifiable;

import java.io.ByteArrayOutputStream;

/// Percent encoding and decoding helpers for URL components.
@NotNullByDefault
final class PercentEncoding {
    /// Hexadecimal digits used by percent encoding.
    private static final char @Unmodifiable [] HEX = "0123456789ABCDEF".toCharArray();

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
        return percentDecodeBytes(Encoding.utf8Encode(input));
    }

    /// Percent-encodes one Unicode code point with the given byte predicate.
    static String utf8PercentEncodeCodePoint(int codePoint, BytePredicate percentEncodePredicate) {
        return utf8PercentEncodeString(new String(Character.toChars(codePoint)), percentEncodePredicate, false);
    }

    /// Percent-encodes a string with the given byte predicate.
    static String utf8PercentEncodeString(String input, BytePredicate percentEncodePredicate) {
        return utf8PercentEncodeString(input, percentEncodePredicate, false);
    }

    /// Percent-encodes a string with the given byte predicate and optional space-to-plus conversion.
    static String utf8PercentEncodeString(String input, BytePredicate percentEncodePredicate, boolean spaceAsPlus) {
        StringBuilder output = new StringBuilder(input.length());
        input.codePoints().forEach(codePoint -> {
            if (spaceAsPlus && codePoint == ' ') {
                output.append('+');
                return;
            }

            byte[] bytes = Encoding.utf8Encode(new String(Character.toChars(codePoint)));
            for (byte b : bytes) {
                int value = b & 0xff;
                if (percentEncodePredicate.test(value)) {
                    appendPercentEncoded(output, value);
                } else {
                    output.append((char) value);
                }
            }
        });
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

    /// Returns whether the byte belongs to the component percent encode set.
    static boolean isComponentPercentEncode(int value) {
        return isUserinfoPercentEncode(value) || value == '$' || value == '%' || value == '&'
                || value == '+' || value == ',';
    }

    /// Returns whether the byte belongs to the form-urlencoded percent encode set.
    static boolean isUrlEncodedPercentEncode(int value) {
        return isComponentPercentEncode(value) || value == '!' || value == '\'' || value == '('
                || value == ')' || value == '~';
    }

    /// Percent-encodes a query string using the special or non-special query encode set.
    static String percentEncodeQuery(String input, boolean special) {
        return utf8PercentEncodeString(input,
                special ? PercentEncoding::isSpecialQueryPercentEncode : PercentEncoding::isQueryPercentEncode);
    }

    /// Returns whether the code point starts an invalid percent triplet at the given position.
    static boolean startsInvalidPercentTriplet(int[] input, int pointer) {
        return input[pointer] == '%'
                && (pointer + 2 >= input.length
                || !Infra.isAsciiHex(input[pointer + 1])
                || !Infra.isAsciiHex(input[pointer + 2]));
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

    /// Appends one percent-encoded byte.
    private static void appendPercentEncoded(StringBuilder output, int value) {
        output.append('%');
        output.append(HEX[(value >>> 4) & 0xf]);
        output.append(HEX[value & 0xf]);
    }

    /// Predicate over unsigned byte values.
    @FunctionalInterface
    @NotNullByDefault
    interface BytePredicate {
        /// Returns whether the byte should be percent-encoded.
        boolean test(int value);
    }
}
