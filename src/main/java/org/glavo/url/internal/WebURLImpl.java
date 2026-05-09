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
    /// Creates an immutable URL from a completed URL record.
    WebURLImpl(UrlRecord record) {
        record.ensureHref();
        this.record = record;
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
        if (record.hostStart < 0) {
            return "";
        }
        return record.portStart < 0
                ? href.substring(record.hostStart, record.hostEnd)
                : href.substring(record.hostStart, record.portEnd);
    }

    /// Returns the serialized URL without its fragment.
    String hrefWithoutFragment() {
        String href = href();
        return record.fragmentStart < 0 ? href : href.substring(0, record.fragmentStart - 1);
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
        String href = record.href;
        assert href != null;
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
                @Nullable WebURLImpl pathUrl = UrlParser.parseUrl(getRawPathOrEmpty());
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
            value = record.usernameStart < 0 ? "" : href.substring(record.usernameStart, record.usernameEnd);
            username = value;
        }
        return value;
    }

    /// Returns the raw username, or `null` when absent.
    @Override
    public @Nullable String getUsername() {
        return record.usernameStart < 0 ? null : getUsernameOrEmpty();
    }

    /// Returns the raw password, or the empty string when absent.
    @Override
    public String getPasswordOrEmpty() {
        @Nullable String value = password;
        if (value == null) {
            String href = href();
            value = record.passwordStart < 0 ? "" : href.substring(record.passwordStart, record.passwordEnd);
            password = value;
        }
        return value;
    }

    /// Returns the raw password, or `null` when absent.
    @Override
    public @Nullable String getPassword() {
        return record.passwordStart < 0 ? null : getPasswordOrEmpty();
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
            value = PercentEncoding.percentDecodeUtf8(getRawPathOrEmpty());
            path = value;
        }
        return value;
    }

    /// Returns the raw path, or the empty string when absent.
    @Override
    public String getRawPathOrEmpty() {
        @Nullable String value = rawPath;
        if (value == null) {
            value = href().substring(record.pathStart, record.pathEnd);
            rawPath = value;
        }
        return value;
    }

    /// Returns the raw path.
    @Override
    public String getRawPath() {
        return getRawPathOrEmpty();
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
        output.append(href, 0, record.schemeEnd + 1);

        if (hasOpaquePath()) {
            appendRfc2396Encoded(output, href, record.pathStart, record.pathEnd, WebURLImpl::isRfc2396Uric);
        } else {
            if (hasHost()) {
                output.append("//");
                if (record.usernameStart >= 0) {
                    appendRfc2396Encoded(output, href, record.usernameStart, record.usernameEnd,
                            WebURLImpl::isRfc2396UserInfo);
                    if (record.passwordStart >= 0) {
                        output.append(':');
                        appendRfc2396Encoded(output, href, record.passwordStart, record.passwordEnd,
                                WebURLImpl::isRfc2396UserInfo);
                    }
                    output.append('@');
                }
                appendRfc2396Encoded(output, href, record.hostStart, record.hostEnd, WebURLImpl::isRfc2396Host);
                if (record.portStart >= 0) {
                    output.append(':').append(href, record.portStart, record.portEnd);
                }
            } else if (record.pathPrefix) {
                output.append("/.");
            }
            appendRfc2396Encoded(output, href, record.pathStart, record.pathEnd, WebURLImpl::isRfc2396Path);
        }

        if (record.queryStart >= 0) {
            output.append('?');
            appendRfc2396Encoded(output, href, record.queryStart, record.queryEnd, WebURLImpl::isRfc2396Uric);
        }
        if (record.fragmentStart >= 0) {
            output.append('#');
            appendRfc2396Encoded(output, href, record.fragmentStart, href.length(), WebURLImpl::isRfc2396Uric);
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
