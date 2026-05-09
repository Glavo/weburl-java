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
import org.glavo.url.WebURLSearchParams;
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
    /// Cached protocol string, or `null` until requested.
    private @Nullable String protocol;
    /// Cached username string, or `null` until requested.
    private @Nullable String username;
    /// Cached password string, or `null` until requested.
    private @Nullable String password;
    /// Cached host string, or `null` until requested.
    private @Nullable String host;
    /// Cached hostname string, or `null` until requested.
    private @Nullable String hostname;
    /// Cached port string, or `null` until requested.
    private @Nullable String port;
    /// Cached pathname string, or `null` until requested.
    private @Nullable String pathname;
    /// Cached search string, or `null` until requested.
    private @Nullable String search;
    /// Cached hash string, or `null` until requested.
    private @Nullable String hash;
    /// Cached RFC 2396 URI string, or `null` until requested.
    private @Nullable String rfc2396String;
    /// Cached immutable query parameter object, or `null` until requested.
    private @Nullable WebURLSearchParams searchParams;

    /// Creates an immutable URL from a completed URL record.
    WebURLImpl(UrlRecord record) {
        record.ensureHref();
        this.record = record;
    }

    /// Creates an immutable URL from parsed components.
    WebURLImpl(
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
        this(new UrlRecord(scheme, username, password, host, port, path, opaquePath, query, fragment));
    }

    /// Returns a component-only mutable copy of the owned URL record.
    UrlRecord mutableRecord() {
        return record.copy();
    }

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return record.opaquePath != null;
    }

    /// Returns whether this URL has a host.
    boolean hasHost() {
        return record.host != null;
    }

    /// Returns whether this URL has a host that serializes to an empty string.
    boolean hasEmptyHost() {
        return record.host != null && record.host.isEmpty();
    }

    /// Returns the serialized URL without its fragment.
    String hrefWithoutFragment() {
        String href = hrefValue();
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

    /// Returns the fragment value, or `null` when absent.
    @Nullable String fragmentValue() {
        return record.fragment;
    }

    /// Returns the serialized URL.
    @Override
    public String href() {
        return hrefValue();
    }

    /// Returns the serialized origin.
    @Override
    public String origin() {
        String cached = origin;
        if (cached != null) {
            return cached;
        }

        String value;
        switch (record.scheme) {
            case "blob":
                WebURLImpl pathUrl = UrlParser.parseUrl(pathname());
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
                    value = record.scheme + "://" + host();
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
    public String scheme() {
        return record.scheme;
    }

    /// Returns a URL with the scheme updated when the URL Standard permits the change.
    @Override
    public WebURL withScheme(String value) {
        return withSchemeOverride(value);
    }

    /// Returns the protocol, including the trailing colon.
    @Override
    public String protocol() {
        String value = protocol;
        if (value == null) {
            value = hrefValue().substring(0, record.schemeEnd + 1);
            protocol = value;
        }
        return value;
    }

    /// Returns a URL with the protocol updated when the URL Standard permits the change.
    @Override
    public WebURL withProtocol(String value) {
        return withSchemeOverride(value);
    }

    /// Returns the username.
    @Override
    public String username() {
        String value = username;
        if (value == null) {
            String href = hrefValue();
            value = record.usernameStart < 0 ? "" : href.substring(record.usernameStart, record.usernameEnd);
            username = value;
        }
        return value;
    }

    /// Returns a URL with the username updated when the URL can have credentials.
    @Override
    public WebURL withUsername(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        UrlRecord copy = mutableRecord();
        copy.username = UrlParser.percentEncodeUserInfo(value);
        return new WebURLImpl(copy);
    }

    /// Returns the password.
    @Override
    public String password() {
        String value = password;
        if (value == null) {
            String href = hrefValue();
            value = record.passwordStart < 0 ? "" : href.substring(record.passwordStart, record.passwordEnd);
            password = value;
        }
        return value;
    }

    /// Returns a URL with the password updated when the URL can have credentials.
    @Override
    public WebURL withPassword(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        UrlRecord copy = mutableRecord();
        copy.password = UrlParser.percentEncodeUserInfo(value);
        return new WebURLImpl(copy);
    }

    /// Returns the host, including the port when present.
    @Override
    public String host() {
        String value = host;
        if (value == null) {
            String href = hrefValue();
            if (record.hostStart < 0) {
                value = "";
            } else {
                value = record.portStart < 0
                        ? href.substring(record.hostStart, record.hostEnd)
                        : href.substring(record.hostStart, record.portEnd);
            }
            host = value;
        }
        return value;
    }

    /// Returns a URL with the host updated when the URL has a non-opaque path.
    @Override
    public WebURL withHost(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        return withStateOverride(value, UrlParser.State.HOST);
    }

    /// Returns the hostname.
    @Override
    public String hostname() {
        String value = hostname;
        if (value == null) {
            String href = hrefValue();
            value = record.hostStart < 0 ? "" : href.substring(record.hostStart, record.hostEnd);
            hostname = value;
        }
        return value;
    }

    /// Returns a URL with the hostname updated when the URL has a non-opaque path.
    @Override
    public WebURL withHostname(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        return withStateOverride(value, UrlParser.State.HOSTNAME);
    }

    /// Returns the port as a string.
    @Override
    public String port() {
        String value = port;
        if (value == null) {
            String href = hrefValue();
            value = record.portStart < 0 ? "" : href.substring(record.portStart, record.portEnd);
            port = value;
        }
        return value;
    }

    /// Returns a URL with the port updated when the URL can have a port.
    @Override
    public WebURL withPort(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        if (value.isEmpty()) {
            UrlRecord copy = mutableRecord();
            copy.port = -1;
            return new WebURLImpl(copy);
        }
        return withStateOverride(value, UrlParser.State.PORT);
    }

    /// Returns the serialized pathname.
    @Override
    public String pathname() {
        String value = pathname;
        if (value == null) {
            value = hrefValue().substring(record.pathStart, record.pathEnd);
            pathname = value;
        }
        return value;
    }

    /// Returns a URL with the pathname updated when the URL has a non-opaque path.
    @Override
    public WebURL withPathname(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        UrlRecord copy = mutableRecord();
        copy.path = new ArrayList<>();
        copy.opaquePath = null;
        return parseIntoCopyOrThis(value, copy, UrlParser.State.PATH_START);
    }

    /// Returns the search string, including the leading question mark when non-empty.
    @Override
    public String search() {
        String value = search;
        if (value == null) {
            String href = hrefValue();
            value = record.queryStart < 0 || record.queryStart == record.queryEnd
                    ? ""
                    : href.substring(record.queryStart - 1, record.queryEnd);
            search = value;
        }
        return value;
    }

    /// Returns a URL with the search string updated.
    @Override
    public WebURL withSearch(String value) {
        UrlRecord copy = mutableRecord();
        if (value.isEmpty()) {
            copy.query = null;
            return new WebURLImpl(copy);
        }

        String input = value.charAt(0) == '?' ? value.substring(1) : value;
        copy.query = "";
        return parseIntoCopyOrThis(input, copy, UrlParser.State.QUERY);
    }

    /// Returns immutable search parameters parsed from the current query.
    @Override
    public WebURLSearchParams searchParams() {
        WebURLSearchParams params = searchParams;
        if (params == null) {
            String href = hrefValue();
            int queryStart = record.queryStart;
            int queryEnd = record.queryEnd;
            params = WebURLSearchParamsImpl.fromQueryInternal(
                    queryStart < 0 ? "" : href.substring(queryStart, queryEnd));
            searchParams = params;
        }
        return params;
    }

    /// Returns a URL with the query replaced by serialized search parameters.
    @Override
    public WebURL withSearchParams(WebURLSearchParams value) {
        UrlRecord copy = mutableRecord();
        String serializedQuery = value.toString();
        copy.query = serializedQuery.isEmpty() ? null : serializedQuery;
        return new WebURLImpl(copy);
    }

    /// Returns the hash string, including the leading number sign when non-empty.
    @Override
    public String hash() {
        String value = hash;
        if (value == null) {
            String href = hrefValue();
            value = record.fragmentStart < 0 || record.fragmentStart == href.length()
                    ? ""
                    : href.substring(record.fragmentStart - 1);
            hash = value;
        }
        return value;
    }

    /// Returns a URL with the hash string updated.
    @Override
    public WebURL withHash(String value) {
        UrlRecord copy = mutableRecord();
        if (value.isEmpty()) {
            copy.fragment = null;
            return new WebURLImpl(copy);
        }

        String input = value.charAt(0) == '#' ? value.substring(1) : value;
        copy.fragment = "";
        return parseIntoCopyOrThis(input, copy, UrlParser.State.FRAGMENT);
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

    /// Returns the serialized URL.
    @Override
    public String toString() {
        return href();
    }

    /// Returns the serialized URL from the frozen record.
    private String hrefValue() {
        String href = record.href;
        if (href == null) {
            throw new AssertionError("URL record is not serialized");
        }
        return href;
    }

    /// Returns the serialized URL converted to Java's RFC 2396 URI syntax.
    @Override
    public String toRFC2396String() {
        String cached = rfc2396String;
        if (cached != null) {
            return cached;
        }

        String href = hrefValue();
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

    /// Runs a state override on a copy of this URL.
    private WebURL withStateOverride(String input, UrlParser.State state) {
        return parseIntoCopyOrThis(input, mutableRecord(), state);
    }

    /// Runs the scheme-state override with an input that has the colon delimiter required by the parser.
    private WebURL withSchemeOverride(String value) {
        return withStateOverride(value.endsWith(":") ? value : value + ":", UrlParser.State.SCHEME_START);
    }

    /// Parses into a copied URL record and returns a new URL, or this URL on parser failure.
    private WebURL parseIntoCopyOrThis(String input, UrlRecord copy, UrlParser.State state) {
        WebURLImpl parsed = UrlParser.basicParse(input, null, copy, state);
        return parsed == null ? this : parsed;
    }
}
