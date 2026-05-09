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
import org.glavo.url.WebURLParseException;
import org.glavo.url.WebURLSearchParams;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/// Internal immutable implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
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
    final @Unmodifiable List<String> path;
    /// Opaque path, or `null` when the URL has a path segment list.
    final @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    final @Nullable String query;
    /// Percent-encoded fragment, or `null` when absent.
    final @Nullable String fragment;
    /// The immutable query parameter object.
    private final WebURLSearchParams searchParams;

    /// Creates a URL from an absolute input string.
    ///
    /// Throws `WebURLParseException` when the input cannot be parsed.
    public static WebURL of(String input) {
        return new WebURLImpl(input, null);
    }

    /// Creates a URL from an input string and a base URL string.
    ///
    /// Throws `WebURLParseException` when the input or base URL cannot be parsed.
    public static WebURL of(String input, String base) {
        return new WebURLImpl(input, parseBase(base));
    }

    /// Creates a URL from an input string and a base URL.
    ///
    /// Throws `WebURLParseException` when the input cannot be parsed against the base URL.
    public static WebURL of(String input, WebURL base) {
        return new WebURLImpl(input, implementation(base));
    }

    /// Parses a URL and returns `null` on failure.
    public static @Nullable WebURL parse(String input) {
        return UrlParser.basicParse(input, null, null, null);
    }

    /// Parses a URL against a base URL string and returns `null` on failure.
    public static @Nullable WebURL parse(String input, String base) {
        WebURLImpl parsedBase = UrlParser.basicParse(base, null, null, null);
        return parsedBase == null ? null : UrlParser.basicParse(input, parsedBase, null, null);
    }

    /// Parses a URL against a base URL and returns `null` on failure.
    public static @Nullable WebURL parse(String input, WebURL base) {
        return UrlParser.basicParse(input, implementation(base), null, null);
    }

    /// Returns whether an input can be parsed as a URL.
    public static boolean canParse(String input) {
        return UrlParser.basicParse(input, null, null, null) != null;
    }

    /// Returns whether an input can be parsed against a base URL string.
    public static boolean canParse(String input, String base) {
        WebURLImpl parsedBase = UrlParser.basicParse(base, null, null, null);
        return parsedBase != null && UrlParser.basicParse(input, parsedBase, null, null) != null;
    }

    /// Returns whether an input can be parsed against a base URL.
    public static boolean canParse(String input, WebURL base) {
        return UrlParser.basicParse(input, implementation(base), null, null) != null;
    }

    /// Creates a URL from an input string and an optional base URL.
    private WebURLImpl(String input, @Nullable WebURLImpl base) {
        WebURLImpl parsed = parseRequired(input, base, "Invalid URL: " + input);

        this.scheme = parsed.scheme;
        this.username = parsed.username;
        this.password = parsed.password;
        this.host = parsed.host;
        this.port = parsed.port;
        this.path = parsed.path;
        this.opaquePath = parsed.opaquePath;
        this.query = parsed.query;
        this.fragment = parsed.fragment;
        this.searchParams = parsed.searchParams;
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
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = List.copyOf(path);
        this.opaquePath = opaquePath;
        this.query = query;
        this.fragment = fragment;
        this.searchParams = WebURLSearchParamsImpl.fromQueryInternal(query == null ? "" : query);
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

    /// Returns the protocol, including the trailing colon.
    @Override
    public String protocol() {
        return scheme + ":";
    }

    /// Returns a URL with the protocol updated when the URL Standard permits the change.
    @Override
    public WebURL withProtocol(String value) {
        return withStateOverride(value + ":", UrlParser.State.SCHEME_START);
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
        return searchParams;
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
            if (hasOpaquePath()) {
                return new URI(scheme, uriSchemeSpecificPart(), uriFragment());
            }

            URI serverUri = serverUri();
            if (serverUri != null) {
                return serverUri;
            }
            return new URI(scheme, uriAuthority(), uriPath(), uriQuery(), uriFragment());
        } catch (URISyntaxException exception) {
            throw new AssertionError("Failed to construct Java URI from parsed URL", exception);
        }
    }

    /// Returns the serialized URL as a Java `URL`.
    @Override
    public URL toURL() throws MalformedURLException {
        return toURI().toURL();
    }

    /// Returns the JSON representation of this URL.
    @Override
    public String toJSON() {
        return href();
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
            List<String> path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        return new WebURLImpl(scheme, username, password, host, port, path, opaquePath, query, fragment);
    }

    /// Attempts to construct a Java URI with a server authority.
    private @Nullable URI serverUri() {
        if (host == null) {
            return null;
        }

        String javaServerHost = host.javaServerHost();
        if (javaServerHost == null) {
            return null;
        }

        try {
            return new URI(
                    scheme,
                    uriUserInfo(),
                    javaServerHost,
                    port,
                    uriPath(),
                    uriQuery(),
                    uriFragment());
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    /// Returns the Java URI authority component.
    private @Nullable String uriAuthority() {
        if (host == null) {
            return null;
        }

        StringBuilder authority = new StringBuilder();
        if (!username.isEmpty() || !password.isEmpty()) {
            authority.append(uriComponent(username));
            if (!password.isEmpty()) {
                authority.append(':').append(uriComponent(password));
            }
            authority.append('@');
        }
        authority.append(uriComponent(UrlParser.serializeHost(host)));
        if (port != -1) {
            authority.append(':').append(port);
        }
        return authority.toString();
    }

    /// Returns the Java URI user-info component.
    private @Nullable String uriUserInfo() {
        if (username.isEmpty() && password.isEmpty()) {
            return null;
        }

        StringBuilder userInfo = new StringBuilder(uriComponent(username));
        if (!password.isEmpty()) {
            userInfo.append(':').append(uriComponent(password));
        }
        return userInfo.toString();
    }

    /// Returns the Java URI path component.
    private String uriPath() {
        String serializedPath = UrlParser.serializePath(this);
        if (host == null && path.size() > 1 && path.get(0).isEmpty()) {
            serializedPath = "/." + serializedPath;
        }
        return uriComponent(serializedPath);
    }

    /// Returns the Java URI query component.
    private @Nullable String uriQuery() {
        return query == null ? null : uriComponent(query);
    }

    /// Returns the Java URI fragment component.
    private @Nullable String uriFragment() {
        return fragment == null ? null : uriComponent(fragment);
    }

    /// Returns the Java URI scheme-specific part for an opaque URL.
    private String uriSchemeSpecificPart() {
        StringBuilder schemeSpecificPart =
                new StringBuilder(uriComponent(opaquePath == null ? "" : opaquePath));
        if (query != null) {
            schemeSpecificPart.append('?').append(uriComponent(query));
        }
        return schemeSpecificPart.toString();
    }

    /// Returns a Java URI constructor component from a WHATWG percent-encoded component.
    private static String uriComponent(String component) {
        return new String(PercentEncoding.percentDecodeString(component), StandardCharsets.UTF_8);
    }

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) url;
    }

    /// Runs a state override on a copy of this URL.
    private WebURL withStateOverride(String input, UrlParser.State state) {
        return parseIntoCopyOrThis(input, this, state);
    }

    /// Parses into a copied URL and returns a new URL, or this URL on parser failure.
    private WebURL parseIntoCopyOrThis(String input, WebURLImpl copy, UrlParser.State state) {
        WebURLImpl parsed = UrlParser.basicParse(input, null, copy, state);
        return parsed == null ? this : parsed;
    }

    /// Parses a base URL string.
    private static WebURLImpl parseBase(String base) {
        return parseRequired(base, null, "Invalid base URL: " + base);
    }

    /// Parses an input string and throws when parsing fails.
    private static WebURLImpl parseRequired(String input, @Nullable WebURLImpl base, String message) {
        try {
            return UrlParser.basicParseRequired(input, base, null, null);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(message, exception);
        }
    }
}
