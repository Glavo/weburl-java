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
import org.jetbrains.annotations.Unmodifiable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/// Internal immutable implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
    /// Shared empty immutable path.
    private static final String @Unmodifiable [] EMPTY_PATH = new String[0];

    /// URL scheme without the trailing colon.
    final String scheme;
    /// Percent-encoded username.
    final String username;
    /// Percent-encoded password.
    final String password;
    /// URL host, or `null` when absent.
    final @Nullable UrlHost host;
    /// URL port, or `-1` when absent or defaulted.
    final int port;
    /// Non-opaque immutable path segments.
    final String @Unmodifiable [] path;
    /// Opaque path, or `null` when the URL has a path segment list.
    final @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    final @Nullable String query;
    /// Percent-encoded fragment, or `null` when absent.
    final @Nullable String fragment;
    /// Cached immutable query parameter object, or `null` until requested.
    private volatile @Nullable WebURLSearchParams searchParams;

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
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path.isEmpty() ? EMPTY_PATH : path.toArray(String[]::new);
        this.opaquePath = opaquePath;
        this.query = query;
        this.fragment = fragment;
    }

    /// Creates an immutable URL from parsed components and an existing immutable path array.
    private WebURLImpl(
            String scheme,
            String username,
            String password,
            @Nullable UrlHost host,
            int port,
            String @Unmodifiable [] path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path.length == 0 ? EMPTY_PATH : path.clone();
        this.opaquePath = opaquePath;
        this.query = query;
        this.fragment = fragment;
    }

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return opaquePath != null;
    }

    /// Returns the serialized URL.
    @Override
    public String href() {
        return UrlParser.serializeUrl(this);
    }

    /// Returns the serialized origin.
    @Override
    public String origin() {
        return UrlParser.serializeOrigin(this);
    }

    /// Returns the scheme.
    @Override
    public String scheme() {
        return scheme;
    }

    /// Returns a URL with the scheme updated when the URL Standard permits the change.
    @Override
    public WebURL withScheme(String value) {
        return withSchemeOverride(value);
    }

    /// Returns the protocol, including the trailing colon.
    @Override
    public String protocol() {
        return scheme + ":";
    }

    /// Returns a URL with the protocol updated when the URL Standard permits the change.
    @Override
    public WebURL withProtocol(String value) {
        return withSchemeOverride(value);
    }

    /// Returns the username.
    @Override
    public String username() {
        return username;
    }

    /// Returns a URL with the username updated when the URL can have credentials.
    @Override
    public WebURL withUsername(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        return copy(UrlParser.percentEncodeUserInfo(value), password, host, port, path, opaquePath, query, fragment);
    }

    /// Returns the password.
    @Override
    public String password() {
        return password;
    }

    /// Returns a URL with the password updated when the URL can have credentials.
    @Override
    public WebURL withPassword(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        return copy(username, UrlParser.percentEncodeUserInfo(value), host, port, path, opaquePath, query, fragment);
    }

    /// Returns the host, including the port when present.
    @Override
    public String host() {
        if (host == null) {
            return "";
        }
        String serializedHost = UrlParser.serializeHost(host);
        return port == -1 ? serializedHost : serializedHost + ":" + port;
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
        return host == null ? "" : UrlParser.serializeHost(host);
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
        return port == -1 ? "" : Integer.toString(port);
    }

    /// Returns a URL with the port updated when the URL can have a port.
    @Override
    public WebURL withPort(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        if (value.isEmpty()) {
            return copy(username, password, host, -1, path, opaquePath, query, fragment);
        }
        return withStateOverride(value, UrlParser.State.PORT);
    }

    /// Returns the serialized pathname.
    @Override
    public String pathname() {
        return UrlParser.serializePath(this);
    }

    /// Returns a URL with the pathname updated when the URL has a non-opaque path.
    @Override
    public WebURL withPathname(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        WebURLImpl copy = copy(username, password, host, port, List.of(), null, query, fragment);
        return parseIntoCopyOrThis(value, copy, UrlParser.State.PATH_START);
    }

    /// Returns the search string, including the leading question mark when non-empty.
    @Override
    public String search() {
        return query == null || query.isEmpty() ? "" : "?" + query;
    }

    /// Returns a URL with the search string updated.
    @Override
    public WebURL withSearch(String value) {
        if (value.isEmpty()) {
            return copy(username, password, host, port, path, opaquePath, null, fragment);
        }

        String input = value.charAt(0) == '?' ? value.substring(1) : value;
        WebURLImpl copy = copy(username, password, host, port, path, opaquePath, "", fragment);
        return parseIntoCopyOrThis(input, copy, UrlParser.State.QUERY);
    }

    /// Returns immutable search parameters parsed from the current query.
    @Override
    public WebURLSearchParams searchParams() {
        WebURLSearchParams params = searchParams;
        if (params == null) {
            params = WebURLSearchParamsImpl.fromQueryInternal(query == null ? "" : query);
            searchParams = params;
        }
        return params;
    }

    /// Returns a URL with the query replaced by serialized search parameters.
    @Override
    public WebURL withSearchParams(WebURLSearchParams value) {
        String serializedQuery = value.toString();
        return copy(username, password, host, port, path, opaquePath,
                serializedQuery.isEmpty() ? null : serializedQuery, fragment);
    }

    /// Returns the hash string, including the leading number sign when non-empty.
    @Override
    public String hash() {
        return fragment == null || fragment.isEmpty() ? "" : "#" + fragment;
    }

    /// Returns a URL with the hash string updated.
    @Override
    public WebURL withHash(String value) {
        if (value.isEmpty()) {
            return copy(username, password, host, port, path, opaquePath, query, null);
        }

        String input = value.charAt(0) == '#' ? value.substring(1) : value;
        WebURLImpl copy = copy(username, password, host, port, path, opaquePath, query, "");
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

    /// Creates a URL copy with replacement components.
    private WebURLImpl copy(
            String username,
            String password,
            @Nullable UrlHost host,
            int port,
            String @Unmodifiable [] path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        return new WebURLImpl(scheme, username, password, host, port, path, opaquePath, query, fragment);
    }

    /// Creates a URL copy with replacement components and a path list.
    private WebURLImpl copy(
            String username,
            String password,
            @Nullable UrlHost host,
            int port,
            List<String> path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        return new WebURLImpl(scheme, username, password, host, port, path, opaquePath, query, fragment);
    }

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) url;
    }

    /// Returns the serialized URL converted to Java's RFC 2396 URI syntax.
    @Override
    public String toRFC2396String() {
        StringBuilder output = new StringBuilder();
        output.append(scheme).append(':');

        if (hasOpaquePath()) {
            appendRfc2396Encoded(output, opaquePath == null ? "" : opaquePath, WebURLImpl::isRfc2396Uric);
        } else {
            if (host != null) {
                output.append("//");
                if (!username.isEmpty() || !password.isEmpty()) {
                    appendRfc2396Encoded(output, username, WebURLImpl::isRfc2396UserInfo);
                    if (!password.isEmpty()) {
                        output.append(':');
                        appendRfc2396Encoded(output, password, WebURLImpl::isRfc2396UserInfo);
                    }
                    output.append('@');
                }
                appendRfc2396Encoded(output, UrlParser.serializeHost(host), WebURLImpl::isRfc2396Host);
                if (port != -1) {
                    output.append(':').append(port);
                }
            } else if (path.length > 1 && path[0].isEmpty()) {
                output.append("/.");
            }
            appendRfc2396Encoded(output, UrlParser.serializePath(this), WebURLImpl::isRfc2396Path);
        }

        if (query != null) {
            output.append('?');
            appendRfc2396Encoded(output, query, WebURLImpl::isRfc2396Uric);
        }
        if (fragment != null) {
            output.append('#');
            appendRfc2396Encoded(output, fragment, WebURLImpl::isRfc2396Uric);
        }
        return output.toString();
    }

    /// Appends a component encoded for Java's RFC 2396 URI parser.
    private static void appendRfc2396Encoded(
            StringBuilder output,
            String value,
            Rfc2396CharPredicate allowed
    ) {
        for (int index = 0; index < value.length(); ) {
            int c = value.codePointAt(index);
            if (c == '%' && index + 2 < value.length()
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
        return parseIntoCopyOrThis(input, this, state);
    }

    /// Runs the scheme-state override with an input that has the colon delimiter required by the parser.
    private WebURL withSchemeOverride(String value) {
        return withStateOverride(value.endsWith(":") ? value : value + ":", UrlParser.State.SCHEME_START);
    }

    /// Parses into a copied URL and returns a new URL, or this URL on parser failure.
    private WebURL parseIntoCopyOrThis(String input, WebURLImpl copy, UrlParser.State state) {
        WebURLImpl parsed = UrlParser.basicParse(input, null, copy, state);
        return parsed == null ? this : parsed;
    }
}
