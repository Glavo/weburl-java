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

import java.util.ArrayList;
import java.util.List;

/// Mutable internal representation of a WHATWG URL record.
///
/// Parser runs mutate this object directly. Once a `WebURLImpl` receives a record, the record is treated as
/// frozen by convention and is no longer mutated. Future copy-on-write support can make that convention
/// explicit without changing the public URL object shape.
@NotNullByDefault
final class UrlRecord {
    /// URL scheme without the trailing colon.
    public String scheme = "";
    /// Percent-encoded username.
    public String username = "";
    /// Percent-encoded password.
    public String password = "";
    /// URL host, or `null` when absent.
    public @Nullable UrlHost host;
    /// URL port, or `-1` when absent or defaulted.
    public int port = -1;
    /// Non-opaque path segments.
    public List<String> path = new ArrayList<>();
    /// Opaque path, or `null` when the URL has a path segment list.
    public @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    public @Nullable String query;
    /// Percent-encoded fragment, or `null` when absent.
    public @Nullable String fragment;

    /// Serialized URL, or `null` until adopted or generated.
    public @Nullable String href;
    /// Index of the colon after the scheme.
    public int schemeEnd;
    /// Start index of the username, or `-1` when credentials are absent.
    public int usernameStart = -1;
    /// End index of the username, or `-1` when credentials are absent.
    public int usernameEnd = -1;
    /// Start index of the password, or `-1` when absent.
    public int passwordStart = -1;
    /// End index of the password, or `-1` when absent.
    public int passwordEnd = -1;
    /// Start index of the host, or `-1` when absent.
    public int hostStart = -1;
    /// End index of the host, or `-1` when absent.
    public int hostEnd = -1;
    /// Start index of the port, or `-1` when absent.
    public int portStart = -1;
    /// End index of the port, or `-1` when absent.
    public int portEnd = -1;
    /// Start index of the logical path.
    public int pathStart;
    /// End index of the logical path.
    public int pathEnd;
    /// Start index of the query, or `-1` when absent.
    public int queryStart = -1;
    /// End index of the query, or `-1` when absent.
    public int queryEnd = -1;
    /// Start index of the fragment, or `-1` when absent.
    public int fragmentStart = -1;
    /// Whether href contains the extra `/.` prefix before the logical path.
    public boolean pathPrefix;

    /// Creates an empty mutable URL record.
    UrlRecord() {
    }

    /// Attempts to adopt an input string that is already the exact URL serialization.
    boolean tryAdoptHref(String input) {
        int index = 0;
        if (!matches(input, index, scheme)) {
            return false;
        }
        int schemeEndValue = scheme.length();
        index = schemeEndValue;
        if (!hasChar(input, index, ':')) {
            return false;
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

        if (host != null) {
            if (!hasChar(input, index, '/') || !hasChar(input, index + 1, '/')) {
                return false;
            }
            index += 2;
            if (!username.isEmpty() || !password.isEmpty()) {
                usernameStartValue = index;
                if (!matches(input, index, username)) {
                    return false;
                }
                index += username.length();
                usernameEndValue = index;
                if (!password.isEmpty()) {
                    if (!hasChar(input, index, ':')) {
                        return false;
                    }
                    index++;
                    passwordStartValue = index;
                    if (!matches(input, index, password)) {
                        return false;
                    }
                    index += password.length();
                    passwordEndValue = index;
                }
                if (!hasChar(input, index, '@')) {
                    return false;
                }
                index++;
            }

            hostStartValue = index;
            index = host.matchSerialized(input, index);
            if (index < 0) {
                return false;
            }
            hostEndValue = index;
            if (port != -1) {
                if (!hasChar(input, index, ':')) {
                    return false;
                }
                index++;
                portStartValue = index;
                index = matchDecimal(input, index, port);
                if (index < 0) {
                    return false;
                }
                portEndValue = index;
            }
        }

        boolean pathPrefixValue = false;
        int pathStartValue = index;
        if (opaquePath != null) {
            if (!matches(input, index, opaquePath)) {
                return false;
            }
            index += opaquePath.length();
        } else {
            if (host == null && path.size() > 1 && path.get(0).isEmpty()) {
                if (!hasChar(input, index, '/') || !hasChar(input, index + 1, '.')) {
                    return false;
                }
                index += 2;
                pathStartValue = index;
                pathPrefixValue = true;
            }
            for (String segment : path) {
                if (!hasChar(input, index, '/')) {
                    return false;
                }
                index++;
                if (!matches(input, index, segment)) {
                    return false;
                }
                index += segment.length();
            }
        }
        int pathEndValue = index;

        int queryStartValue = -1;
        int queryEndValue = -1;
        if (query != null) {
            if (!hasChar(input, index, '?')) {
                return false;
            }
            index++;
            queryStartValue = index;
            if (!matches(input, index, query)) {
                return false;
            }
            index += query.length();
            queryEndValue = index;
        }

        int fragmentStartValue = -1;
        if (fragment != null) {
            if (!hasChar(input, index, '#')) {
                return false;
            }
            index++;
            fragmentStartValue = index;
            if (!matches(input, index, fragment)) {
                return false;
            }
            index += fragment.length();
        }
        if (index != input.length()) {
            return false;
        }

        setHref(input, schemeEndValue, usernameStartValue, usernameEndValue, passwordStartValue, passwordEndValue,
                hostStartValue, hostEndValue, portStartValue, portEndValue, pathStartValue, pathEndValue,
                queryStartValue, queryEndValue, fragmentStartValue, pathPrefixValue);
        return true;
    }

    /// Ensures that the serialized URL and component indexes are available.
    void ensureHref() {
        if (href != null) {
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append(scheme);
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

        if (host != null) {
            output.append("//");
            if (!username.isEmpty() || !password.isEmpty()) {
                usernameStartValue = output.length();
                output.append(username);
                usernameEndValue = output.length();
                if (!password.isEmpty()) {
                    output.append(':');
                    passwordStartValue = output.length();
                    output.append(password);
                    passwordEndValue = output.length();
                }
                output.append('@');
            }

            hostStartValue = output.length();
            output.append(UrlParser.serializeHost(host));
            hostEndValue = output.length();
            if (port != -1) {
                output.append(':');
                portStartValue = output.length();
                output.append(port);
                portEndValue = output.length();
            }
        }

        boolean pathPrefixValue = false;
        int pathStartValue = output.length();
        if (opaquePath != null) {
            output.append(opaquePath);
        } else {
            if (host == null && path.size() > 1 && path.get(0).isEmpty()) {
                output.append("/.");
                pathPrefixValue = true;
                pathStartValue = output.length();
            }
            for (String segment : path) {
                output.append('/').append(segment);
            }
        }
        int pathEndValue = output.length();

        int queryStartValue = -1;
        int queryEndValue = -1;
        if (query != null) {
            output.append('?');
            queryStartValue = output.length();
            output.append(query);
            queryEndValue = output.length();
        }

        int fragmentStartValue = -1;
        if (fragment != null) {
            output.append('#');
            fragmentStartValue = output.length();
            output.append(fragment);
        }

        setHref(output.toString(), schemeEndValue, usernameStartValue, usernameEndValue,
                passwordStartValue, passwordEndValue, hostStartValue, hostEndValue, portStartValue, portEndValue,
                pathStartValue, pathEndValue, queryStartValue, queryEndValue, fragmentStartValue, pathPrefixValue);
    }

    /// Stores a serialized URL and its component indexes.
    private void setHref(
            String href,
            int schemeEnd,
            int usernameStart,
            int usernameEnd,
            int passwordStart,
            int passwordEnd,
            int hostStart,
            int hostEnd,
            int portStart,
            int portEnd,
            int pathStart,
            int pathEnd,
            int queryStart,
            int queryEnd,
            int fragmentStart,
            boolean pathPrefix
    ) {
        this.href = href;
        this.schemeEnd = schemeEnd;
        this.usernameStart = usernameStart;
        this.usernameEnd = usernameEnd;
        this.passwordStart = passwordStart;
        this.passwordEnd = passwordEnd;
        this.hostStart = hostStart;
        this.hostEnd = hostEnd;
        this.portStart = portStart;
        this.portEnd = portEnd;
        this.pathStart = pathStart;
        this.pathEnd = pathEnd;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.fragmentStart = fragmentStart;
        this.pathPrefix = pathPrefix;
    }

    /// Returns whether a character exists at an index and equals the expected value.
    private static boolean hasChar(String input, int index, char expected) {
        return index >= 0 && index < input.length() && input.charAt(index) == expected;
    }

    /// Returns whether input contains value at the given index.
    private static boolean matches(String input, int index, String value) {
        return index >= 0
                && index + value.length() <= input.length()
                && input.regionMatches(index, value, 0, value.length());
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
