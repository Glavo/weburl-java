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

/// Creates immutable URL instances from completed mutable URL records.
@NotNullByDefault
final class UrlSerializer {
    /// Prevents instantiation.
    private UrlSerializer() {
    }

    /// Creates an immutable URL from the completed parser record.
    static WebURLImpl toUrl(UrlRecord record, String input) {
        @Nullable WebURLImpl url = tryAdoptHref(record, input);
        return url == null ? serialize(record) : url;
    }

    /// Attempts to adopt an input string that is already the exact URL serialization.
    private static @Nullable WebURLImpl tryAdoptHref(UrlRecord record, String input) {
        int index = 0;
        if (mismatches(input, index, record.scheme)) {
            return null;
        }
        int schemeEndValue = record.scheme.length();
        index = schemeEndValue;
        if (missingChar(input, index, ':')) {
            return null;
        }
        index++;

        int usernameStartValue = -1;
        int usernameEndValue = -1;
        int passwordStartValue = -1;
        int passwordEndValue = -1;
        int hostStartValue = -1;
        int hostEndValue = -1;
        int portStartValue = -1;
        int portEndValue = -1;

        if (record.host != null) {
            if (missingChar(input, index, '/') || missingChar(input, index + 1, '/')) {
                return null;
            }
            index += 2;
            if (!record.username.isEmpty() || !record.password.isEmpty()) {
                usernameStartValue = index;
                if (mismatches(input, index, record.username)) {
                    return null;
                }
                index += record.username.length();
                usernameEndValue = index;
                if (!record.password.isEmpty()) {
                    if (missingChar(input, index, ':')) {
                        return null;
                    }
                    index++;
                    passwordStartValue = index;
                    if (mismatches(input, index, record.password)) {
                        return null;
                    }
                    index += record.password.length();
                    passwordEndValue = index;
                }
                if (missingChar(input, index, '@')) {
                    return null;
                }
                index++;
            }

            hostStartValue = index;
            index = record.host.matchSerialized(input, index);
            if (index < 0) {
                return null;
            }
            hostEndValue = index;
            if (record.port != -1) {
                if (missingChar(input, index, ':')) {
                    return null;
                }
                index++;
                portStartValue = index;
                index = matchDecimal(input, index, record.port);
                if (index < 0) {
                    return null;
                }
                portEndValue = index;
            }
        }

        boolean pathPrefixValue = false;
        int pathStartValue = index;
        if (record.opaquePath != null) {
            if (mismatches(input, index, record.opaquePath)) {
                return null;
            }
            index += record.opaquePath.length();
        } else {
            if (record.host == null && record.path.size() > 1 && record.path.get(0).isEmpty()) {
                if (missingChar(input, index, '/') || missingChar(input, index + 1, '.')) {
                    return null;
                }
                index += 2;
                pathStartValue = index;
                pathPrefixValue = true;
            }
            for (String segment : record.path) {
                if (missingChar(input, index, '/')) {
                    return null;
                }
                index++;
                if (mismatches(input, index, segment)) {
                    return null;
                }
                index += segment.length();
            }
        }
        int pathEndValue = index;

        int queryStartValue = -1;
        int queryEndValue = -1;
        if (record.query != null) {
            if (missingChar(input, index, '?')) {
                return null;
            }
            index++;
            queryStartValue = index;
            if (mismatches(input, index, record.query)) {
                return null;
            }
            index += record.query.length();
            queryEndValue = index;
        }

        int fragmentStartValue = -1;
        if (record.fragment != null) {
            if (missingChar(input, index, '#')) {
                return null;
            }
            index++;
            fragmentStartValue = index;
            if (mismatches(input, index, record.fragment)) {
                return null;
            }
            index += record.fragment.length();
        }
        if (index != input.length()) {
            return null;
        }

        return new WebURLImpl(record, input, schemeEndValue, usernameStartValue, usernameEndValue,
                passwordStartValue, passwordEndValue, hostStartValue, hostEndValue, portStartValue, portEndValue,
                pathStartValue, pathEndValue, queryStartValue, queryEndValue, fragmentStartValue, pathPrefixValue);
    }

    /// Serializes a URL record and stores component indexes into the serialized string.
    private static WebURLImpl serialize(UrlRecord record) {
        StringBuilder output = new StringBuilder();
        output.append(record.scheme);
        int schemeEndValue = output.length();
        output.append(':');

        int usernameStartValue = -1;
        int usernameEndValue = -1;
        int passwordStartValue = -1;
        int passwordEndValue = -1;
        int hostStartValue = -1;
        int hostEndValue = -1;
        int portStartValue = -1;
        int portEndValue = -1;

        if (record.host != null) {
            output.append("//");
            if (!record.username.isEmpty() || !record.password.isEmpty()) {
                usernameStartValue = output.length();
                output.append(record.username);
                usernameEndValue = output.length();
                if (!record.password.isEmpty()) {
                    output.append(':');
                    passwordStartValue = output.length();
                    output.append(record.password);
                    passwordEndValue = output.length();
                }
                output.append('@');
            }

            hostStartValue = output.length();
            output.append(UrlParser.serializeHost(record.host));
            hostEndValue = output.length();
            if (record.port != -1) {
                output.append(':');
                portStartValue = output.length();
                output.append(record.port);
                portEndValue = output.length();
            }
        }

        boolean pathPrefixValue = false;
        int pathStartValue = output.length();
        if (record.opaquePath != null) {
            output.append(record.opaquePath);
        } else {
            if (record.host == null && record.path.size() > 1 && record.path.get(0).isEmpty()) {
                output.append("/.");
                pathPrefixValue = true;
                pathStartValue = output.length();
            }
            for (String segment : record.path) {
                output.append('/').append(segment);
            }
        }
        int pathEndValue = output.length();

        int queryStartValue = -1;
        int queryEndValue = -1;
        if (record.query != null) {
            output.append('?');
            queryStartValue = output.length();
            output.append(record.query);
            queryEndValue = output.length();
        }

        int fragmentStartValue = -1;
        if (record.fragment != null) {
            output.append('#');
            fragmentStartValue = output.length();
            output.append(record.fragment);
        }

        return new WebURLImpl(record, output.toString(), schemeEndValue, usernameStartValue, usernameEndValue,
                passwordStartValue, passwordEndValue, hostStartValue, hostEndValue, portStartValue, portEndValue,
                pathStartValue, pathEndValue, queryStartValue, queryEndValue, fragmentStartValue, pathPrefixValue);
    }

    /// Returns whether a character is absent or differs from the expected value.
    private static boolean missingChar(String input, int index, char expected) {
        return index < 0 || index >= input.length() || input.charAt(index) != expected;
    }

    /// Returns whether input does not contain value at the given index.
    private static boolean mismatches(String input, int index, String value) {
        return index < 0
                || index + value.length() > input.length()
                || !input.regionMatches(index, value, 0, value.length());
    }

    /// Matches a non-negative decimal integer and returns the end index, or `-1`.
    private static int matchDecimal(String input, int index, int value) {
        int length = decimalLength(value);
        if (index + length > input.length()) {
            return -1;
        }
        int divisor = pow10(length - 1);
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

    /// Returns the decimal digit length of a non-negative integer.
    private static int decimalLength(int value) {
        int length = 1;
        for (int current = value; current >= 10; current /= 10) {
            length++;
        }
        return length;
    }

    /// Returns ten raised to the given non-negative exponent.
    private static int pow10(int exponent) {
        int value = 1;
        for (int i = 0; i < exponent; i++) {
            value *= 10;
        }
        return value;
    }
}
