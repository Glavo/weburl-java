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
import org.jetbrains.annotations.Unmodifiable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/// Internal immutable implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
    /// URL scheme without the trailing colon.
    private final String scheme;
    /// URL host, or `null` when absent.
    private final @Nullable UrlHost urlHost;
    /// URL port, or `-1` when absent or defaulted.
    private final int port;
    /// Immutable non-opaque path segments.
    private final @Unmodifiable List<String> pathSegments;
    /// Opaque path, or `null` when the URL has a path segment list.
    private final @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    private final @Nullable String rawQueryValue;
    /// Percent-encoded fragment, or `null` when absent.
    private final @Nullable String rawFragmentValue;
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
    /// Cached decoded username string, or `null` until requested or when absent.
    private @Nullable String username;
    /// Cached raw username string, or `null` until requested.
    private @Nullable String rawUsername;
    /// Cached decoded password string, or `null` until requested or when absent.
    private @Nullable String password;
    /// Cached raw password string, or `null` until requested.
    private @Nullable String rawPassword;
    /// Cached decoded user-info string, or `null` until requested or when absent.
    private @Nullable String userInfo;
    /// Cached raw user-info string, or `null` until requested or when absent.
    private @Nullable String rawUserInfo;
    /// Cached decoded authority string, or `null` until requested or when absent.
    private @Nullable String authority;
    /// Cached raw authority string, or `null` until requested or when absent.
    private @Nullable String rawAuthority;
    /// Cached host string, or `null` until requested or when absent.
    private @Nullable String host;
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
    /// Creates an immutable URL from a completed URL record and serialized URL indexes.
    WebURLImpl(
            UrlRecord record,
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
        this.scheme = record.scheme;
        this.urlHost = record.host;
        this.port = record.port;
        this.pathSegments = List.copyOf(record.path);
        this.opaquePath = record.opaquePath;
        this.rawQueryValue = record.query;
        this.rawFragmentValue = record.fragment;
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

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return opaquePath != null;
    }

    /// Returns whether this URL has a host.
    boolean hasHost() {
        return urlHost != null;
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
        return scheme.equals(value);
    }

    /// Returns the port value, or `-1` when absent.
    int portValue() {
        return port;
    }

    /// Returns the host value for parser state.
    @Nullable UrlHost hostValue() {
        return urlHost;
    }

    /// Returns a mutable copy of the non-opaque path segments.
    List<String> pathSegments() {
        return new ArrayList<>(pathSegments);
    }

    /// Returns the first path segment, or `null` when absent.
    @Nullable String firstPathSegment() {
        return opaquePath != null || pathSegments.isEmpty() ? null : pathSegments.get(0);
    }

    /// Returns the opaque path value, or `null` for a non-opaque path.
    @Nullable String opaquePathValue() {
        return opaquePath;
    }

    /// Returns the query value, or `null` when absent.
    @Nullable String queryValue() {
        return rawQueryValue;
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
        switch (scheme) {
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
                    value = scheme + "://" + hostAndPort();
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
        return scheme;
    }

    /// Returns the decoded username, or `null` when absent.
    @Override
    public @Nullable String getUsername() {
        @Nullable String rawValue = getRawUsername();
        if (rawValue == null) {
            return null;
        }

        @Nullable String value = username;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(rawValue);
            username = value;
        }
        return value;
    }

    /// Returns the raw username, or the empty string when absent.
    @Override
    public String getRawUsernameOrEmpty() {
        @Nullable String value = rawUsername;
        if (value == null) {
            String href = href();
            value = usernameStart < 0 ? "" : href.substring(usernameStart, usernameEnd);
            rawUsername = value;
        }
        return value;
    }

    /// Returns the raw username, or `null` when absent.
    @Override
    public @Nullable String getRawUsername() {
        return usernameStart < 0 ? null : getRawUsernameOrEmpty();
    }

    /// Returns the decoded password, or `null` when absent.
    @Override
    public @Nullable String getPassword() {
        @Nullable String rawValue = getRawPassword();
        if (rawValue == null) {
            return null;
        }

        @Nullable String value = password;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(rawValue);
            password = value;
        }
        return value;
    }

    /// Returns the raw password, or the empty string when absent.
    @Override
    public String getRawPasswordOrEmpty() {
        @Nullable String value = rawPassword;
        if (value == null) {
            String href = href();
            value = passwordStart < 0 ? "" : href.substring(passwordStart, passwordEnd);
            rawPassword = value;
        }
        return value;
    }

    /// Returns the raw password, or `null` when absent.
    @Override
    public @Nullable String getRawPassword() {
        return passwordStart < 0 ? null : getRawPasswordOrEmpty();
    }

    /// Returns the decoded user-info, or `null` when absent.
    @Override
    public @Nullable String getUserInfo() {
        @Nullable String rawValue = getRawUserInfo();
        if (rawValue == null) {
            return null;
        }

        @Nullable String value = userInfo;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(rawValue);
            userInfo = value;
        }
        return value;
    }

    /// Returns the raw user-info, or `null` when absent.
    @Override
    public @Nullable String getRawUserInfo() {
        if (usernameStart < 0) {
            return null;
        }

        @Nullable String value = rawUserInfo;
        if (value == null) {
            value = href().substring(usernameStart, hostStart - 1);
            rawUserInfo = value;
        }
        return value;
    }

    /// Returns the decoded authority, or `null` when absent.
    @Override
    public @Nullable String getAuthority() {
        @Nullable String rawValue = getRawAuthority();
        if (rawValue == null) {
            return null;
        }

        @Nullable String value = authority;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(rawValue);
            authority = value;
        }
        return value;
    }

    /// Returns the raw authority, or `null` when absent.
    @Override
    public @Nullable String getRawAuthority() {
        if (hostStart < 0) {
            return null;
        }

        @Nullable String value = rawAuthority;
        if (value == null) {
            value = href().substring(schemeEnd + 3, portStart < 0 ? hostEnd : portEnd);
            rawAuthority = value;
        }
        return value;
    }

    /// Returns the host, or `null` when absent.
    @Override
    public @Nullable String getHost() {
        if (hostStart < 0) {
            return null;
        }

        @Nullable String value = host;
        if (value == null) {
            value = href().substring(hostStart, hostEnd);
            host = value;
        }
        return value;
    }

    /// Returns the port value, or `-1` when absent.
    @Override
    public int getPort() {
        return port;
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
        if (rawQueryValue == null) {
            return null;
        }

        @Nullable String value = query;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(rawQueryValue);
            query = value;
        }
        return value;
    }

    /// Returns the raw query, or `null` when absent.
    @Override
    public @Nullable String getRawQuery() {
        return rawQueryValue;
    }

    /// Returns the raw query, or the empty string when absent.
    @Override
    public String getRawQueryOrEmpty() {
        @Nullable String value = rawQueryValue;
        return value == null ? "" : value;
    }

    /// Returns the decoded fragment, or `null` when absent.
    @Override
    public @Nullable String getFragment() {
        if (rawFragmentValue == null) {
            return null;
        }

        @Nullable String value = fragment;
        if (value == null) {
            value = PercentEncoding.percentDecodeUtf8(rawFragmentValue);
            fragment = value;
        }
        return value;
    }

    /// Returns the raw fragment, or `null` when absent.
    @Override
    public @Nullable String getRawFragment() {
        return rawFragmentValue;
    }

    /// Returns the raw fragment, or the empty string when absent.
    @Override
    public String getRawFragmentOrEmpty() {
        @Nullable String value = rawFragmentValue;
        return value == null ? "" : value;
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

}
