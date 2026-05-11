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

/// Shared string and ASCII helper methods used by URL algorithms.
@NotNullByDefault
public final class StringUtils {
    /// Creates no instances.
    private StringUtils() {
    }

    /// Returns whether the code point is an ASCII digit.
    public static boolean isAsciiDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    /// Returns whether the code point is an ASCII alphabetic code point.
    public static boolean isAsciiAlpha(int codePoint) {
        return (codePoint >= 'A' && codePoint <= 'Z') || (codePoint >= 'a' && codePoint <= 'z');
    }

    /// Returns whether the code point is an ASCII alphanumeric code point.
    public static boolean isAsciiAlphanumeric(int codePoint) {
        return isAsciiAlpha(codePoint) || isAsciiDigit(codePoint);
    }

    /// Returns whether the code point is an ASCII hexadecimal digit.
    public static boolean isAsciiHex(int codePoint) {
        return isAsciiDigit(codePoint)
                || (codePoint >= 'A' && codePoint <= 'F')
                || (codePoint >= 'a' && codePoint <= 'f');
    }

    /// Returns whether the string contains only ASCII code points.
    public static boolean isAsciiOnly(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 0x7f) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether the string contains at least one non-ASCII code point.
    public static boolean containsNonAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 0x7f) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether the string contains at least one ASCII uppercase letter.
    public static boolean containsAsciiUppercase(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    /// Returns whether the string slice contains only ASCII decimal digits and is not empty.
    public static boolean isAsciiDecimal(String value, int start, int end) {
        if (start >= end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            if (!isAsciiDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether the string slice contains only ASCII hexadecimal digits and is not empty.
    public static boolean isAsciiHex(String value, int start, int end) {
        if (start >= end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            if (!isAsciiHex(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /// Trims leading and trailing C0 controls and spaces.
    public static String trimControlChars(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) <= 0x20) {
            start++;
        }
        while (end > start && value.charAt(end - 1) <= 0x20) {
            end--;
        }
        return value.substring(start, end);
    }

    /// Removes ASCII tabs and newlines.
    public static String removeAsciiTabsAndNewlines(String value) {
        int firstSkipped = -1;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r') {
                firstSkipped = i;
                break;
            }
        }
        if (firstSkipped < 0) {
            return value;
        }

        StringBuilder output = new StringBuilder(value.length() - 1);
        output.append(value, 0, firstSkipped);
        for (int i = firstSkipped + 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\t' && c != '\n' && c != '\r') {
                output.append(c);
            }
        }
        return output.toString();
    }
}
