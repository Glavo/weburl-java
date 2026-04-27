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

/// Internal implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
    /// The internal URL record.
    private final UrlRecord url;
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
        return new WebURLImpl(input, record(base));
    }

    /// Parses a URL and returns `null` on failure.
    public static @Nullable WebURL parse(String input) {
        UrlRecord parsed = UrlParser.basicParse(input, null, null, null);
        return parsed == null ? null : new WebURLImpl(parsed);
    }

    /// Parses a URL against a base URL string and returns `null` on failure.
    public static @Nullable WebURL parse(String input, String base) {
        UrlRecord parsedBase = UrlParser.basicParse(base, null, null, null);
        if (parsedBase == null) {
            return null;
        }
        UrlRecord parsed = UrlParser.basicParse(input, parsedBase, null, null);
        return parsed == null ? null : new WebURLImpl(parsed);
    }

    /// Parses a URL against a base URL and returns `null` on failure.
    public static @Nullable WebURL parse(String input, WebURL base) {
        UrlRecord parsed = UrlParser.basicParse(input, record(base), null, null);
        return parsed == null ? null : new WebURLImpl(parsed);
    }

    /// Returns whether an input can be parsed as a URL.
    public static boolean canParse(String input) {
        return UrlParser.basicParse(input, null, null, null) != null;
    }

    /// Returns whether an input can be parsed against a base URL string.
    public static boolean canParse(String input, String base) {
        UrlRecord parsedBase = UrlParser.basicParse(base, null, null, null);
        return parsedBase != null && UrlParser.basicParse(input, parsedBase, null, null) != null;
    }

    /// Returns whether an input can be parsed against a base URL.
    public static boolean canParse(String input, WebURL base) {
        return UrlParser.basicParse(input, record(base), null, null) != null;
    }

    /// Creates a URL from an input string and an optional base record.
    private WebURLImpl(String input, @Nullable UrlRecord base) {
        UrlParser.ParseResult result = UrlParser.basicParseResult(input, base, null, null);
        UrlRecord parsed = result.url();
        if (parsed == null) {
            throw parseExceptionOrIllegalArgument(result.error(), "Invalid URL: " + input);
        }

        this.url = parsed;
        this.searchParams = WebURLSearchParamsImpl.fromQueryInternal(parsed.query == null ? "" : parsed.query);
    }

    /// Creates a URL from a parsed record.
    private WebURLImpl(UrlRecord url) {
        this.url = url;
        this.searchParams = WebURLSearchParamsImpl.fromQueryInternal(url.query == null ? "" : url.query);
    }

    /// Returns the serialized URL.
    @Override
    public String href() {
        return UrlParser.serializeUrl(url);
    }

    /// Returns the serialized origin.
    @Override
    public String origin() {
        return UrlParser.serializeOrigin(url);
    }

    /// Returns the protocol, including the trailing colon.
    @Override
    public String protocol() {
        return url.scheme + ":";
    }

    /// Returns a URL with the protocol updated when the URL Standard permits the change.
    @Override
    public WebURL withProtocol(String value) {
        return withStateOverride(value + ":", UrlParser.State.SCHEME_START);
    }

    /// Returns the username.
    @Override
    public String username() {
        return url.username;
    }

    /// Returns a URL with the username updated when the URL can have credentials.
    @Override
    public WebURL withUsername(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(url)) {
            return this;
        }
        UrlRecord copy = url.clone();
        UrlParser.setTheUsername(copy, value);
        return new WebURLImpl(copy);
    }

    /// Returns the password.
    @Override
    public String password() {
        return url.password;
    }

    /// Returns a URL with the password updated when the URL can have credentials.
    @Override
    public WebURL withPassword(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(url)) {
            return this;
        }
        UrlRecord copy = url.clone();
        UrlParser.setThePassword(copy, value);
        return new WebURLImpl(copy);
    }

    /// Returns the host, including the port when present.
    @Override
    public String host() {
        if (url.host == null) {
            return "";
        }
        String host = UrlParser.serializeHost(url.host);
        return url.port == -1 ? host : host + ":" + url.port;
    }

    /// Returns a URL with the host updated when the URL has a non-opaque path.
    @Override
    public WebURL withHost(String value) {
        if (url.hasOpaquePath()) {
            return this;
        }
        return withStateOverride(value, UrlParser.State.HOST);
    }

    /// Returns the hostname.
    @Override
    public String hostname() {
        return url.host == null ? "" : UrlParser.serializeHost(url.host);
    }

    /// Returns a URL with the hostname updated when the URL has a non-opaque path.
    @Override
    public WebURL withHostname(String value) {
        if (url.hasOpaquePath()) {
            return this;
        }
        return withStateOverride(value, UrlParser.State.HOSTNAME);
    }

    /// Returns the port as a string.
    @Override
    public String port() {
        return url.port == -1 ? "" : Integer.toString(url.port);
    }

    /// Returns a URL with the port updated when the URL can have a port.
    @Override
    public WebURL withPort(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(url)) {
            return this;
        }
        UrlRecord copy = url.clone();
        if (value.isEmpty()) {
            copy.port = -1;
            return new WebURLImpl(copy);
        } else {
            return withStateOverride(value, UrlParser.State.PORT);
        }
    }

    /// Returns the serialized pathname.
    @Override
    public String pathname() {
        return UrlParser.serializePath(url);
    }

    /// Returns a URL with the pathname updated when the URL has a non-opaque path.
    @Override
    public WebURL withPathname(String value) {
        if (url.hasOpaquePath()) {
            return this;
        }
        UrlRecord copy = url.clone();
        copy.path = new java.util.ArrayList<>();
        copy.opaquePath = null;
        return parseIntoCopyOrThis(value, copy, UrlParser.State.PATH_START);
    }

    /// Returns the search string, including the leading question mark when non-empty.
    @Override
    public String search() {
        return url.query == null || url.query.isEmpty() ? "" : "?" + url.query;
    }

    /// Returns a URL with the search string updated.
    @Override
    public WebURL withSearch(String value) {
        UrlRecord copy = url.clone();
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
        return searchParams;
    }

    /// Returns a URL with the query replaced by serialized search parameters.
    @Override
    public WebURL withSearchParams(WebURLSearchParams value) {
        UrlRecord copy = url.clone();
        String query = value.toString();
        copy.query = query.isEmpty() ? null : query;
        return new WebURLImpl(copy);
    }

    /// Returns the hash string, including the leading number sign when non-empty.
    @Override
    public String hash() {
        return url.fragment == null || url.fragment.isEmpty() ? "" : "#" + url.fragment;
    }

    /// Returns a URL with the hash string updated.
    @Override
    public WebURL withHash(String value) {
        UrlRecord copy = url.clone();
        if (value.isEmpty()) {
            copy.fragment = null;
            return new WebURLImpl(copy);
        }

        String input = value.charAt(0) == '#' ? value.substring(1) : value;
        copy.fragment = "";
        return parseIntoCopyOrThis(input, copy, UrlParser.State.FRAGMENT);
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

    /// Returns the internal URL record for a `WebURL`.
    private static UrlRecord record(WebURL url) {
        return ((WebURLImpl) url).url;
    }

    /// Runs a state override on a copy of this URL.
    private WebURL withStateOverride(String input, UrlParser.State state) {
        UrlRecord copy = url.clone();
        return parseIntoCopyOrThis(input, copy, state);
    }

    /// Parses into a copied record and returns a new URL, or this URL on parser failure.
    private WebURL parseIntoCopyOrThis(String input, UrlRecord copy, UrlParser.State state) {
        UrlRecord parsed = UrlParser.basicParse(input, null, copy, state);
        return parsed == null ? this : new WebURLImpl(copy);
    }

    /// Parses a base URL string.
    private static UrlRecord parseBase(String base) {
        UrlParser.ParseResult result = UrlParser.basicParseResult(base, null, null, null);
        UrlRecord parsedBase = result.url();
        if (parsedBase == null) {
            throw parseExceptionOrIllegalArgument(result.error(), "Invalid base URL: " + base);
        }
        return parsedBase;
    }

    /// Returns the parser exception, or a generic argument exception when none is available.
    private static IllegalArgumentException parseExceptionOrIllegalArgument(
            @Nullable WebURLParseException exception,
            String message
    ) {
        return exception == null ? new IllegalArgumentException(message) : exception;
    }
}
