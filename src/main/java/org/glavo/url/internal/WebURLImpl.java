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

import java.util.ArrayList;

/// Internal implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL, SearchParamsOwner {
    /// The mutable internal URL record.
    private UrlRecord url;
    /// The live query parameter object.
    private final WebURLSearchParams searchParams;

    /// Creates a URL from an absolute input string.
    public static WebURL of(String input) {
        return new WebURLImpl(input, null);
    }

    /// Creates a URL from an input string and a base URL string.
    public static WebURL of(String input, String base) {
        return new WebURLImpl(input, parseBase(base));
    }

    /// Creates a URL from an input string and a base URL.
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
        UrlRecord parsed = UrlParser.basicParse(input, base, null, null);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid URL: " + input);
        }

        this.url = parsed;
        this.searchParams = WebURLSearchParams.createLive(parsed.query == null ? "" : parsed.query, this);
    }

    /// Creates a URL from a parsed record.
    private WebURLImpl(UrlRecord url) {
        this.url = url;
        this.searchParams = WebURLSearchParams.createLive(url.query == null ? "" : url.query, this);
    }

    /// Returns the serialized URL.
    @Override
    public String getHref() {
        return UrlParser.serializeUrl(url);
    }

    /// Replaces the URL with a parsed absolute URL.
    @Override
    public void setHref(String value) {
        UrlRecord parsed = UrlParser.basicParse(value, null, null, null);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid URL: " + value);
        }

        this.url = parsed;
        this.searchParams.replaceAllInternal(UrlEncoded.parseUrlencodedString(parsed.query == null ? "" : parsed.query));
    }

    /// Returns the serialized origin.
    @Override
    public String getOrigin() {
        return UrlParser.serializeOrigin(url);
    }

    /// Returns the protocol, including the trailing colon.
    @Override
    public String getProtocol() {
        return url.scheme + ":";
    }

    /// Sets the protocol.
    @Override
    public void setProtocol(String value) {
        UrlParser.basicParse(value + ":", null, url, UrlParser.State.SCHEME_START);
    }

    /// Returns the username.
    @Override
    public String getUsername() {
        return url.username;
    }

    /// Sets the username.
    @Override
    public void setUsername(String value) {
        if (!UrlParser.cannotHaveAUsernamePasswordPort(url)) {
            UrlParser.setTheUsername(url, value);
        }
    }

    /// Returns the password.
    @Override
    public String getPassword() {
        return url.password;
    }

    /// Sets the password.
    @Override
    public void setPassword(String value) {
        if (!UrlParser.cannotHaveAUsernamePasswordPort(url)) {
            UrlParser.setThePassword(url, value);
        }
    }

    /// Returns the host, including the port when present.
    @Override
    public String getHost() {
        if (url.host == null) {
            return "";
        }
        String host = UrlParser.serializeHost(url.host);
        return url.port == null ? host : host + ":" + url.port;
    }

    /// Sets the host.
    @Override
    public void setHost(String value) {
        if (!url.hasOpaquePath()) {
            UrlParser.basicParse(value, null, url, UrlParser.State.HOST);
        }
    }

    /// Returns the hostname.
    @Override
    public String getHostname() {
        return url.host == null ? "" : UrlParser.serializeHost(url.host);
    }

    /// Sets the hostname.
    @Override
    public void setHostname(String value) {
        if (!url.hasOpaquePath()) {
            UrlParser.basicParse(value, null, url, UrlParser.State.HOSTNAME);
        }
    }

    /// Returns the port as a string.
    @Override
    public String getPort() {
        return url.port == null ? "" : Integer.toString(url.port);
    }

    /// Sets the port.
    @Override
    public void setPort(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(url)) {
            return;
        }
        if (value.isEmpty()) {
            url.port = null;
        } else {
            UrlParser.basicParse(value, null, url, UrlParser.State.PORT);
        }
    }

    /// Returns the serialized pathname.
    @Override
    public String getPathname() {
        return UrlParser.serializePath(url);
    }

    /// Sets the pathname.
    @Override
    public void setPathname(String value) {
        if (url.hasOpaquePath()) {
            return;
        }
        url.path = new ArrayList<>();
        url.opaquePath = null;
        UrlParser.basicParse(value, null, url, UrlParser.State.PATH_START);
    }

    /// Returns the search string, including the leading question mark when non-empty.
    @Override
    public String getSearch() {
        return url.query == null || url.query.isEmpty() ? "" : "?" + url.query;
    }

    /// Sets the search string.
    @Override
    public void setSearch(String value) {
        if (value.isEmpty()) {
            url.query = null;
            searchParams.replaceAllInternal(new ArrayList<>());
            return;
        }

        String input = value.charAt(0) == '?' ? value.substring(1) : value;
        url.query = "";
        UrlParser.basicParse(input, null, url, UrlParser.State.QUERY);
        searchParams.replaceAllInternal(UrlEncoded.parseUrlencodedString(input));
    }

    /// Returns the live search parameters.
    @Override
    public WebURLSearchParams getSearchParams() {
        return searchParams;
    }

    /// Returns the hash string, including the leading number sign when non-empty.
    @Override
    public String getHash() {
        return url.fragment == null || url.fragment.isEmpty() ? "" : "#" + url.fragment;
    }

    /// Sets the hash string.
    @Override
    public void setHash(String value) {
        if (value.isEmpty()) {
            url.fragment = null;
            return;
        }

        String input = value.charAt(0) == '#' ? value.substring(1) : value;
        url.fragment = "";
        UrlParser.basicParse(input, null, url, UrlParser.State.FRAGMENT);
    }

    /// Returns the JSON representation of this URL.
    @Override
    public String toJSON() {
        return getHref();
    }

    /// Returns the serialized URL.
    @Override
    public String toString() {
        return getHref();
    }

    /// Updates the query from a live `WebURLSearchParams` object.
    @Override
    public void setQueryFromSearchParams(@Nullable String query) {
        url.query = query;
    }

    /// Returns the internal URL record for a `WebURL`.
    private static UrlRecord record(WebURL url) {
        return ((WebURLImpl) url).url;
    }

    /// Parses a base URL string.
    private static UrlRecord parseBase(String base) {
        UrlRecord parsedBase = UrlParser.basicParse(base, null, null, null);
        if (parsedBase == null) {
            throw new IllegalArgumentException("Invalid base URL: " + base);
        }
        return parsedBase;
    }
}
