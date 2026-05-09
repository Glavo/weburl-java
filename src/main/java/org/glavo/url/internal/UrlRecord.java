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
    String scheme = "";
    /// Percent-encoded username.
    String username = "";
    /// Percent-encoded password.
    String password = "";
    /// URL host, or `null` when absent.
    @Nullable UrlHost host;
    /// URL port, or `-1` when absent or defaulted.
    int port = -1;
    /// Non-opaque path segments.
    List<String> path = new ArrayList<>();
    /// Opaque path, or `null` when the URL has a path segment list.
    @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    @Nullable String query;
    /// Percent-encoded fragment, or `null` when absent.
    @Nullable String fragment;

    /// Serialized URL, or `null` until adopted or generated.
    private @Nullable String href;
    /// Index of the colon after the scheme.
    private int schemeEnd;
    /// Start index of the username, or `-1` when credentials are absent.
    private int usernameStart = -1;
    /// End index of the username, or `-1` when credentials are absent.
    private int usernameEnd = -1;
    /// Start index of the password, or `-1` when absent.
    private int passwordStart = -1;
    /// End index of the password, or `-1` when absent.
    private int passwordEnd = -1;
    /// Start index of the host, or `-1` when absent.
    private int hostStart = -1;
    /// End index of the host, or `-1` when absent.
    private int hostEnd = -1;
    /// Start index of the port, or `-1` when absent.
    private int portStart = -1;
    /// End index of the port, or `-1` when absent.
    private int portEnd = -1;
    /// Start index of the logical path.
    private int pathStart;
    /// End index of the logical path.
    private int pathEnd;
    /// Start index of the query, or `-1` when absent.
    private int queryStart = -1;
    /// End index of the query, or `-1` when absent.
    private int queryEnd = -1;
    /// Start index of the fragment, or `-1` when absent.
    private int fragmentStart = -1;
    /// Whether href contains the extra `/.` prefix before the logical path.
    private boolean pathPrefix;

    /// Creates an empty mutable URL record.
    UrlRecord() {
    }

    /// Creates a mutable URL record from parsed components.
    UrlRecord(
            String scheme,
            String username,
            String password,
            @Nullable UrlHost host,
            int port,
            List<String> path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = new ArrayList<>(path);
        this.opaquePath = opaquePath;
        this.query = query;
        this.fragment = fragment;
    }

    /// Creates a component-only mutable copy of another record.
    UrlRecord(UrlRecord source) {
        this(source.scheme, source.username, source.password, source.host, source.port,
                source.path, source.opaquePath, source.query, source.fragment);
    }

    /// Returns a component-only mutable copy of this record.
    UrlRecord copy() {
        return new UrlRecord(this);
    }

    /// Returns whether this URL has a host.
    boolean hasHost() {
        return host != null;
    }

    /// Returns whether this URL has a host that serializes to an empty string.
    boolean hasEmptyHost() {
        return host != null && host.isEmpty();
    }

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return opaquePath != null;
    }

    /// Returns whether the scheme equals the supplied lower-case ASCII value.
    boolean schemeEquals(String value) {
        return scheme.equals(value);
    }

    /// Returns a mutable copy of the non-opaque path segments.
    List<String> pathSegments() {
        return new ArrayList<>(path);
    }

    /// Returns the first path segment, or `null` when absent.
    @Nullable String firstPathSegment() {
        return opaquePath != null || path.isEmpty() ? null : path.get(0);
    }

    /// Returns the serialized URL.
    String href() {
        ensureHref();
        return href == null ? "" : href;
    }

    /// Returns the serialized URL without its fragment.
    String hrefWithoutFragment() {
        String value = href();
        return fragmentStart < 0 ? value : value.substring(0, fragmentStart - 1);
    }

    /// Returns the scheme component without the trailing colon.
    String scheme() {
        return scheme;
    }

    /// Returns the protocol component with the trailing colon.
    String protocol() {
        String value = href();
        return value.substring(0, schemeEnd + 1);
    }

    /// Returns the username component.
    String username() {
        String value = href();
        return usernameStart < 0 ? "" : value.substring(usernameStart, usernameEnd);
    }

    /// Returns the password component.
    String password() {
        String value = href();
        return passwordStart < 0 ? "" : value.substring(passwordStart, passwordEnd);
    }

    /// Returns the host component, including the port when present.
    String host() {
        String value = href();
        if (hostStart < 0) {
            return "";
        }
        return portStart < 0 ? value.substring(hostStart, hostEnd) : value.substring(hostStart, portEnd);
    }

    /// Returns the hostname component.
    String hostname() {
        String value = href();
        return hostStart < 0 ? "" : value.substring(hostStart, hostEnd);
    }

    /// Returns the port as a string.
    String portString() {
        String value = href();
        return portStart < 0 ? "" : value.substring(portStart, portEnd);
    }

    /// Returns the serialized pathname.
    String pathname() {
        String value = href();
        return value.substring(pathStart, pathEnd);
    }

    /// Returns the search string, including the leading question mark when non-empty.
    String search() {
        String value = href();
        return queryStart < 0 || queryStart == queryEnd ? "" : value.substring(queryStart - 1, queryEnd);
    }

    /// Returns the hash string, including the leading number sign when non-empty.
    String hash() {
        String value = href();
        return fragmentStart < 0 || fragmentStart == value.length() ? "" : value.substring(fragmentStart - 1);
    }

    /// Returns the query value, or `null` when absent.
    @Nullable String queryValue() {
        return query;
    }

    /// Returns the fragment value, or `null` when absent.
    @Nullable String fragmentValue() {
        return fragment;
    }

    /// Returns the opaque path value, or `null` for a non-opaque path.
    @Nullable String opaquePathValue() {
        return opaquePath;
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

    /// Returns the scheme delimiter index.
    int schemeEnd() {
        ensureHref();
        return schemeEnd;
    }

    /// Returns the username start index, or `-1`.
    int usernameStart() {
        ensureHref();
        return usernameStart;
    }

    /// Returns the username end index, or `-1`.
    int usernameEnd() {
        ensureHref();
        return usernameEnd;
    }

    /// Returns the password start index, or `-1`.
    int passwordStart() {
        ensureHref();
        return passwordStart;
    }

    /// Returns the password end index, or `-1`.
    int passwordEnd() {
        ensureHref();
        return passwordEnd;
    }

    /// Returns the host start index, or `-1`.
    int hostStart() {
        ensureHref();
        return hostStart;
    }

    /// Returns the host end index, or `-1`.
    int hostEnd() {
        ensureHref();
        return hostEnd;
    }

    /// Returns the port start index, or `-1`.
    int portStart() {
        ensureHref();
        return portStart;
    }

    /// Returns the port end index, or `-1`.
    int portEnd() {
        ensureHref();
        return portEnd;
    }

    /// Returns the path start index.
    int pathStart() {
        ensureHref();
        return pathStart;
    }

    /// Returns the path end index.
    int pathEnd() {
        ensureHref();
        return pathEnd;
    }

    /// Returns the query start index, or `-1`.
    int queryStart() {
        ensureHref();
        return queryStart;
    }

    /// Returns the query end index, or `-1`.
    int queryEnd() {
        ensureHref();
        return queryEnd;
    }

    /// Returns the fragment start index, or `-1`.
    int fragmentStart() {
        ensureHref();
        return fragmentStart;
    }

    /// Returns whether the serialized URL includes a path prefix before the logical path.
    boolean pathPrefix() {
        ensureHref();
        return pathPrefix;
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
