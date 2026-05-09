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

import org.glavo.url.WebURL;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/// Internal immutable implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
    /// Frozen URL record owned by this immutable URL.
    private final UrlRecord record;
    /// Serialized WHATWG URL.
    private final String href;
    /// Index of the colon after the scheme.
    private final int schemeEnd;
    /// Start index of the username, or `-1` when credentials are absent.
    private final int usernameStart;
    /// End index of the username, or `-1` when credentials are absent.
    private final int usernameEnd;
    /// Start index of the password, or `-1` when absent.
    private final int passwordStart;
    /// End index of the password, or `-1` when absent.
    private final int passwordEnd;
    /// Start index of the host, or `-1` when absent.
    private final int hostStart;
    /// End index of the host, or `-1` when absent.
    private final int hostEnd;
    /// Start index of the port, or `-1` when absent.
    private final int portStart;
    /// End index of the port, or `-1` when absent.
    private final int portEnd;
    /// Start index of the logical path.
    private final int pathStart;
    /// End index of the logical path.
    private final int pathEnd;
    /// Start index of the query, or `-1` when absent.
    private final int queryStart;
    /// End index of the query, or `-1` when absent.
    private final int queryEnd;
    /// Start index of the fragment, or `-1` when absent.
    private final int fragmentStart;
    /// Whether href contains the extra `/.` prefix before the logical path.
    private final boolean pathPrefix;
    /// Cached origin string, or `null` until requested.
    private @Nullable String origin;
    /// Cached username string, or `null` until requested.
    private @Nullable String username;
    /// Cached password string, or `null` until requested.
    private @Nullable String password;
    /// Cached decoded path string, or `null` until requested.
    private @Nullable String path;
    /// Cached raw path string, or `null` until requested.
    private @Nullable String rawPath;
    /// Cached decoded query string, or `null` until requested or when absent.
    private @Nullable String query;
    /// Cached decoded fragment string, or `null` until requested or when absent.
    private @Nullable String fragment;
    /// Cached RFC 2396 URI string, or `null` until requested.
    private @Nullable String rfc2396String;
    /// Creates an immutable URL from a completed URL record and the parser input.
    WebURLImpl(UrlRecord record, String input) {
        Serialization serialization = tryAdoptHref(record, input);
        if (serialization == null) {
            serialization = serialize(record);
        }
        this.record = record;
        this.href = serialization.href();
        this.schemeEnd = serialization.schemeEnd();
        this.usernameStart = serialization.usernameStart();
        this.usernameEnd = serialization.usernameEnd();
        this.passwordStart = serialization.passwordStart();
        this.passwordEnd = serialization.passwordEnd();
        this.hostStart = serialization.hostStart();
        this.hostEnd = serialization.hostEnd();
        this.portStart = serialization.portStart();
        this.portEnd = serialization.portEnd();
        this.pathStart = serialization.pathStart();
        this.pathEnd = serialization.pathEnd();
        this.queryStart = serialization.queryStart();
        this.queryEnd = serialization.queryEnd();
        this.fragmentStart = serialization.fragmentStart();
        this.pathPrefix = serialization.pathPrefix();
    }

    /// Attempts to adopt an input string that is already the exact URL serialization.
    private static @Nullable Serialization tryAdoptHref(UrlRecord record, String input) {
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

        return new Serialization(input, schemeEndValue, usernameStartValue, usernameEndValue,
                passwordStartValue, passwordEndValue, hostStartValue, hostEndValue, portStartValue, portEndValue,
                pathStartValue, pathEndValue, queryStartValue, queryEndValue, fragmentStartValue, pathPrefixValue);
    }

    /// Serializes a URL record and stores component indexes into the serialized string.
    private static Serialization serialize(UrlRecord record) {
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

        return new Serialization(output.toString(), schemeEndValue, usernameStartValue, usernameEndValue,
                passwordStartValue, passwordEndValue, hostStartValue, hostEndValue, portStartValue, portEndValue,
                pathStartValue, pathEndValue, queryStartValue, queryEndValue, fragmentStartValue, pathPrefixValue);
    }

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return record.opaquePath != null;
    }

    /// Returns whether this URL has a host.
    boolean hasHost() {
        return record.host != null;
    }

    /// Returns the serialized host plus port when a port is present.
    private String hostAndPort() {
        String href = href();
        if (hostStart < 0) {
            return "";
        }
        return portStart < 0
                ? href.substring(hostStart, hostEnd)
                : href.substring(hostStart, portEnd);
    }

    /// Returns the serialized URL without its fragment.
    String hrefWithoutFragment() {
        String href = href();
        return fragmentStart < 0 ? href : href.substring(0, fragmentStart - 1);
    }

    /// Returns whether the scheme equals the supplied lower-case ASCII value.
    boolean schemeEquals(String value) {
        return record.scheme.equals(value);
    }

    /// Returns the port value, or `-1` when absent.
    int portValue() {
        return record.port;
    }

    /// Returns the host value for parser state.
    @Nullable UrlHost hostValue() {
        return record.host;
    }

    /// Returns a mutable copy of the non-opaque path segments.
    List<String> pathSegments() {
        return new ArrayList<>(record.path);
    }

    /// Returns the first path segment, or `null` when absent.
    @Nullable String firstPathSegment() {
        return record.opaquePath != null || record.path.isEmpty() ? null : record.path.get(0);
    }

    /// Returns the opaque path value, or `null` for a non-opaque path.
    @Nullable String opaquePathValue() {
        return record.opaquePath;
    }

    /// Returns the query value, or `null` when absent.
    @Nullable String queryValue() {
        return record.query;
    }

    /// Returns the serialized URL.
    @Override
    public String href() {
        return href;
    }

    /// Returns the serialized origin.
    @Override
    public String origin() {
        @Nullable String cached = origin;
        if (cached != null) {
            return cached;
        }

        String value;
        switch (record.scheme) {
            case "blob":
                @Nullable WebURLImpl pathUrl = UrlParser.parseUrl(getRawPath());
                if (pathUrl == null || (!pathUrl.schemeEquals("http") && !pathUrl.schemeEquals("https"))) {
                    value = "null";
                } else {
                    value = pathUrl.origin();
                }
                break;
            case "ftp":
            case "http":
            case "https":
            case "ws":
            case "wss":
                if (!hasHost()) {
                    value = "null";
                } else {
                    value = record.scheme + "://" + hostAndPort();
                }
                break;
            case "file":
            default:
                value = "null";
                break;
        }
        origin = value;
        return value;
    }

    /// Returns the scheme.
    @Override
    public String getScheme() {
        return record.scheme;
    }

    /// Returns the raw username, or the empty string when absent.
    @Override
    public String getUsernameOrEmpty() {
        @Nullable String value = username;
        if (value == null) {
            String href = href();
            value = usernameStart < 0 ? "" : href.substring(usernameStart, usernameEnd);
            username = value;
        }
        return value;
    }

    /// Returns the raw username, or `null` when absent.
    @Override
    public @Nullable String getUsername() {
        return usernameStart < 0 ? null : getUsernameOrEmpty();
    }

    /// Returns the raw password, or the empty string when absent.
    @Override
    public String getPasswordOrEmpty() {
        @Nullable String value = password;
        if (value == null) {
            String href = href();
            value = passwordStart < 0 ? "" : href.substring(passwordStart, passwordEnd);
            password = value;
        }
        return value;
    }

    /// Returns the raw password, or `null` when absent.
    @Override
    public @Nullable String getPassword() {
        return passwordStart < 0 ? null : getPasswordOrEmpty();
    }

    /// Returns the port value, or `-1` when absent.
    @Override
    public int getPort() {
        return record.port;
    }

    /// Returns the decoded path.
    @Override
    public String getPath() {
        @Nullable String value = path;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(getRawPath());
            path = value;
        }
        return value;
    }

    /// Returns the raw path.
    @Override
    public String getRawPath() {
        @Nullable String value = rawPath;
        if (value == null) {
            value = href().substring(pathStart, pathEnd);
            rawPath = value;
        }
        return value;
    }

    /// Returns the decoded query, or `null` when absent.
    @Override
    public @Nullable String getQuery() {
        if (record.query == null) {
            return null;
        }

        @Nullable String value = query;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(record.query);
            query = value;
        }
        return value;
    }

    /// Returns the raw query, or `null` when absent.
    @Override
    public @Nullable String getRawQuery() {
        return record.query;
    }

    /// Returns the raw query, or the empty string when absent.
    @Override
    public String getRawQueryOrEmpty() {
        @Nullable String query = record.query;
        return query == null ? "" : query;
    }

    /// Returns the decoded fragment, or `null` when absent.
    @Override
    public @Nullable String getFragment() {
        if (record.fragment == null) {
            return null;
        }

        @Nullable String value = fragment;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(record.fragment);
            fragment = value;
        }
        return value;
    }

    /// Returns the raw fragment, or `null` when absent.
    @Override
    public @Nullable String getRawFragment() {
        return record.fragment;
    }

    /// Returns the raw fragment, or the empty string when absent.
    @Override
    public String getRawFragmentOrEmpty() {
        @Nullable String fragment = record.fragment;
        return fragment == null ? "" : fragment;
    }

    /// Returns the serialized URL as a Java `URI`.
    @Override
    public URI toURI() {
        try {
            return new URI(toRFC2396String());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("This URL cannot be represented as an RFC 2396 URI", exception);
        }
    }

    /// Returns the serialized URL as a Java `URL`.
    @Override
    public URL toURL() throws MalformedURLException {
        return toURI().toURL();
    }

    /// Compares this URL with another URL by serialized URL string.
    @Override
    public int compareTo(WebURL other) {
        return href().compareTo(other.href());
    }

    /// Compares this URL with another object for serialized URL equality.
    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURL other && href().equals(other.href());
    }

    /// Returns the serialized URL hash code.
    @Override
    public int hashCode() {
        return href().hashCode();
    }

    /// Returns the serialized URL.
    @Override
    public String toString() {
        return href();
    }

    /// Returns the serialized URL converted to Java's RFC 2396 URI syntax.
    @Override
    public String toRFC2396String() {
        @Nullable String cached = rfc2396String;
        if (cached != null) {
            return cached;
        }

        String href = href();
        StringBuilder output = new StringBuilder();
        output.append(href, 0, schemeEnd + 1);

        if (hasOpaquePath()) {
            appendRfc2396Encoded(output, href, pathStart, pathEnd, WebURLImpl::isRfc2396Uric);
        } else {
            if (hasHost()) {
                output.append("//");
                if (usernameStart >= 0) {
                    appendRfc2396Encoded(output, href, usernameStart, usernameEnd,
                            WebURLImpl::isRfc2396UserInfo);
                    if (passwordStart >= 0) {
                        output.append(':');
                        appendRfc2396Encoded(output, href, passwordStart, passwordEnd,
                                WebURLImpl::isRfc2396UserInfo);
                    }
                    output.append('@');
                }
                appendRfc2396Encoded(output, href, hostStart, hostEnd, WebURLImpl::isRfc2396Host);
                if (portStart >= 0) {
                    output.append(':').append(href, portStart, portEnd);
                }
            } else if (pathPrefix) {
                output.append("/.");
            }
            appendRfc2396Encoded(output, href, pathStart, pathEnd, WebURLImpl::isRfc2396Path);
        }

        if (queryStart >= 0) {
            output.append('?');
            appendRfc2396Encoded(output, href, queryStart, queryEnd, WebURLImpl::isRfc2396Uric);
        }
        if (fragmentStart >= 0) {
            output.append('#');
            appendRfc2396Encoded(output, href, fragmentStart, href.length(), WebURLImpl::isRfc2396Uric);
        }
        String value = output.toString();
        rfc2396String = value;
        return value;
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

    /// Appends a component encoded for Java's RFC 2396 URI parser.
    private static void appendRfc2396Encoded(
            StringBuilder output,
            String value,
            int start,
            int end,
            Rfc2396CharPredicate allowed
    ) {
        for (int index = start; index < end; ) {
            int c = value.codePointAt(index);
            if (c == '%' && index + 2 < end
                    && Infra.isAsciiHex(value.charAt(index + 1))
                    && Infra.isAsciiHex(value.charAt(index + 2))) {
                output.append('%').append(value.charAt(index + 1)).append(value.charAt(index + 2));
                index += 3;
                continue;
            }

            if (c <= 0x7f && allowed.test(c)) {
                output.append((char) c);
            } else {
                for (byte b : Encoding.utf8Encode(new String(Character.toChars(c)))) {
                    appendRfc2396Escape(output, b & 0xff);
                }
            }
            index += Character.charCount(c);
        }
    }

    /// Appends one RFC 2396 percent escape.
    private static void appendRfc2396Escape(StringBuilder output, int value) {
        output.append('%');
        output.append(Character.toUpperCase(Character.forDigit((value >>> 4) & 0xf, 16)));
        output.append(Character.toUpperCase(Character.forDigit(value & 0xf, 16)));
    }

    /// Returns whether a character is an RFC 2396 path character.
    private static boolean isRfc2396Path(int c) {
        return isRfc2396Unreserved(c) || c == '/' || c == ';' || c == ':' || c == '@'
                || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is an RFC 2396 URI character.
    private static boolean isRfc2396Uric(int c) {
        return isRfc2396Unreserved(c) || c == ';' || c == '/' || c == '?' || c == ':'
                || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is allowed in an RFC 2396 user-info component.
    private static boolean isRfc2396UserInfo(int c) {
        return isRfc2396Unreserved(c) || c == ';' || c == ':' || c == '&' || c == '='
                || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is allowed in an RFC 2396 host component.
    private static boolean isRfc2396Host(int c) {
        return isRfc2396Unreserved(c) || c == '[' || c == ']' || c == ':' || c == ';'
                || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is unreserved under RFC 2396.
    private static boolean isRfc2396Unreserved(int c) {
        return Infra.isAsciiAlpha(c) || Infra.isAsciiDigit(c) || c == '-' || c == '_'
                || c == '.' || c == '!' || c == '~' || c == '*' || c == '\'' || c == '(' || c == ')';
    }

    /// Predicate over RFC 2396 ASCII characters.
    @FunctionalInterface
    @NotNullByDefault
    private interface Rfc2396CharPredicate {
        /// Returns whether the character may appear without escaping.
        boolean test(int c);
    }

    /// Serialized URL text plus indexes into that text.
    ///
    /// @param href serialized WHATWG URL
    /// @param schemeEnd index of the colon after the scheme
    /// @param usernameStart start index of the username, or `-1` when credentials are absent
    /// @param usernameEnd end index of the username, or `-1` when credentials are absent
    /// @param passwordStart start index of the password, or `-1` when absent
    /// @param passwordEnd end index of the password, or `-1` when absent
    /// @param hostStart start index of the host, or `-1` when absent
    /// @param hostEnd end index of the host, or `-1` when absent
    /// @param portStart start index of the port, or `-1` when absent
    /// @param portEnd end index of the port, or `-1` when absent
    /// @param pathStart start index of the logical path
    /// @param pathEnd end index of the logical path
    /// @param queryStart start index of the query, or `-1` when absent
    /// @param queryEnd end index of the query, or `-1` when absent
    /// @param fragmentStart start index of the fragment, or `-1` when absent
    /// @param pathPrefix whether href contains the extra `/.` prefix before the logical path
    @NotNullByDefault
    private record Serialization(
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
    }

}
