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
public final class PercentEncoding {
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

    /// Decodes percent triplets from the UTF-8 bytes of a string.
    public static byte[] percentDecodeString(String input) {
        int firstTriplet = firstValidPercentTriplet(input);
        return firstTriplet < 0 ? Utf8.encode(input) : percentDecodeString(input, firstTriplet);
    }

    /// Decodes valid percent triplets in a URL component as UTF-8.
    public static String percentDecodeUtf8(String input) {
        int firstTriplet = firstValidPercentTriplet(input);
        return firstTriplet < 0 ? input : Utf8.decode(percentDecodeString(input, firstTriplet));
    }

    /// Decodes percent triplets from a string with a known first valid triplet.
    private static byte[] percentDecodeString(String input, int firstTriplet) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length());
        appendUtf8(output, input, 0, firstTriplet);

        int index = firstTriplet;
        int end = input.length();
        while (index < end) {
            int percent = input.indexOf('%', index);
            if (percent < 0) {
                appendUtf8(output, input, index, end);
                break;
            }

            appendUtf8(output, input, index, percent);
            if (isValidPercentTriplet(input, percent, end)) {
                output.write(percentEncodedByte(input, percent));
                index = percent + 3;
            } else {
                output.write('%');
                index = percent + 1;
            }
        }

        return output.toByteArray();
    }

    /// Appends a string slice encoded as UTF-8.
    private static void appendUtf8(ByteArrayOutputStream output, String input, int start, int end) {
        if (start == end) {
            return;
        }

        byte[] bytes = Utf8.encode(input.substring(start, end));
        output.write(bytes, 0, bytes.length);
    }

    /// Percent-encodes one Unicode code point with the given byte predicate.
    public static String utf8PercentEncodeCodePoint(int codePoint, BytePredicate percentEncodePredicate) {
        if (codePoint >= 0 && codePoint <= 0x7f) {
            return percentEncodePredicate.test(codePoint)
                    ? percentEncodedByteString(codePoint)
                    : Character.toString((char) codePoint);
        }
        return utf8PercentEncodeString(new String(Character.toChars(codePoint)), percentEncodePredicate);
    }

    /// Percent-encodes a string with the given byte predicate.
    public static String utf8PercentEncodeString(String input, BytePredicate percentEncodePredicate) {
        for (int index = 0; index < input.length(); ) {
            int codePoint = input.codePointAt(index);
            if (codePoint > 0x7f || percentEncodePredicate.test(codePoint)) {
                return utf8PercentEncodeString(input, percentEncodePredicate, index);
            }
            index += Character.charCount(codePoint);
        }
        return input;
    }

    /// Percent-encodes a decoded component string with the given byte predicate.
    public static String utf8PercentEncodeDecodedString(String input, BytePredicate percentEncodePredicate) {
        for (int index = 0; index < input.length(); ) {
            int codePoint = input.codePointAt(index);
            if (codePoint == '%' || codePoint > 0x7f || percentEncodePredicate.test(codePoint)) {
                return utf8PercentEncodeDecodedString(input, percentEncodePredicate, index);
            }
            index += Character.charCount(codePoint);
        }
        return input;
    }

    /// Percent-encodes a decoded component string starting at the first known changed code point.
    private static String utf8PercentEncodeDecodedString(
            String input,
            BytePredicate percentEncodePredicate,
            int start
    ) {
        StringBuilder output = new StringBuilder(input.length());
        output.append(input, 0, start);
        for (int index = start; index < input.length(); ) {
            int codePoint = input.codePointAt(index);
            if (codePoint <= 0x7f) {
                if (codePoint == '%' || percentEncodePredicate.test(codePoint)) {
                    output.append(percentEncodedByteString(codePoint));
                } else {
                    output.append((char) codePoint);
                }
            } else {
                appendUtf8PercentEncodedCodePoint(output, scalarValue(codePoint));
            }
            index += Character.charCount(codePoint);
        }
        return output.toString();
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
                byte[] bytes = Utf8.encode(new String(Character.toChars(scalarValue(codePoint))));
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
    public static boolean isC0ControlPercentEncode(int value) {
        return value <= 0x1f || value > 0x7e;
    }

    /// Returns whether the byte belongs to the fragment percent encode set.
    public static boolean isFragmentPercentEncode(int value) {
        return isC0ControlPercentEncode(value) || value == ' ' || value == '"' || value == '<'
                || value == '>' || value == '`';
    }

    /// Returns whether the byte belongs to the query percent encode set.
    public static boolean isQueryPercentEncode(int value) {
        return isC0ControlPercentEncode(value) || value == ' ' || value == '"' || value == '#'
                || value == '<' || value == '>';
    }

    /// Returns whether the byte belongs to the special-query percent encode set.
    public static boolean isSpecialQueryPercentEncode(int value) {
        return isQueryPercentEncode(value) || value == '\'';
    }

    /// Returns whether the byte belongs to the path percent encode set.
    public static boolean isPathPercentEncode(int value) {
        return isQueryPercentEncode(value) || value == '?' || value == '`' || value == '{'
                || value == '}' || value == '^';
    }

    /// Returns whether the byte belongs to the userinfo percent encode set.
    public static boolean isUserinfoPercentEncode(int value) {
        return isPathPercentEncode(value) || value == '/' || value == ':' || value == ';'
                || value == '=' || value == '@' || value == '[' || value == '\\' || value == ']'
                || value == '|';
    }

    /// Returns whether the character starts an invalid percent triplet at the given UTF-16 index.
    public static boolean startsInvalidPercentTriplet(String input, int pointer) {
        return input.charAt(pointer) == '%'
                && !isValidPercentTriplet(input, pointer, input.length());
    }

    /// Returns whether the string contains a valid percent triplet at the given UTF-16 index.
    public static boolean isValidPercentTriplet(String input, int pointer, int end) {
        return pointer + 2 < end
                && input.charAt(pointer) == '%'
                && StringUtils.isAsciiHex(input.charAt(pointer + 1))
                && StringUtils.isAsciiHex(input.charAt(pointer + 2));
    }

    /// Returns the first valid percent triplet index in a string, or `-1` when none exists.
    private static int firstValidPercentTriplet(String input) {
        int end = input.length();
        int pointer = input.indexOf('%');
        while (pointer >= 0) {
            if (isValidPercentTriplet(input, pointer, end)) {
                return pointer;
            }
            pointer = input.indexOf('%', pointer + 1);
        }
        return -1;
    }

    /// Decodes the byte value represented by a valid percent triplet.
    public static int percentEncodedByte(String input, int pointer) {
        return hexValue(input.charAt(pointer + 1)) * 16 + hexValue(input.charAt(pointer + 2));
    }

    /// Appends the UTF-8 bytes of one code point as percent escapes.
    public static void appendUtf8PercentEncodedCodePoint(StringBuilder output, int codePoint) {
        codePoint = scalarValue(codePoint);
        if (codePoint <= 0x7f) {
            output.append(percentEncodedByteString(codePoint));
            return;
        }

        byte[] bytes = Utf8.encode(new String(Character.toChars(codePoint)));
        for (byte b : bytes) {
            output.append(percentEncodedByteString(b & 0xff));
        }
    }

    /// Converts surrogate code points to the replacement character.
    private static int scalarValue(int codePoint) {
        return codePoint >= 0xd800 && codePoint <= 0xdfff ? 0xfffd : codePoint;
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
    public interface BytePredicate {
        /// Returns whether the byte should be percent-encoded.
        boolean test(int value);
    }
}
