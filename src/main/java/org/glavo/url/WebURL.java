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
package org.glavo.url;

import org.glavo.url.internal.WebURLImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A WHATWG URL.
@NotNullByDefault
public sealed interface WebURL permits WebURLImpl {
    /// Creates a URL from an absolute input string.
    static WebURL of(String input) {
        return WebURLImpl.of(input);
    }

    /// Creates a URL from an input string and a base URL string.
    static WebURL of(String input, String base) {
        return WebURLImpl.of(input, base);
    }

    /// Creates a URL from an input string and a base URL.
    static WebURL of(String input, WebURL base) {
        return WebURLImpl.of(input, base);
    }

    /// Parses a URL and returns `null` on failure.
    static @Nullable WebURL parse(String input) {
        return WebURLImpl.parse(input);
    }

    /// Parses a URL against a base URL string and returns `null` on failure.
    static @Nullable WebURL parse(String input, String base) {
        return WebURLImpl.parse(input, base);
    }

    /// Parses a URL against a base URL and returns `null` on failure.
    static @Nullable WebURL parse(String input, WebURL base) {
        return WebURLImpl.parse(input, base);
    }

    /// Returns whether an input can be parsed as a URL.
    static boolean canParse(String input) {
        return WebURLImpl.canParse(input);
    }

    /// Returns whether an input can be parsed against a base URL string.
    static boolean canParse(String input, String base) {
        return WebURLImpl.canParse(input, base);
    }

    /// Returns whether an input can be parsed against a base URL.
    static boolean canParse(String input, WebURL base) {
        return WebURLImpl.canParse(input, base);
    }

    /// Returns the serialized URL.
    String getHref();

    /// Returns the serialized origin.
    String getOrigin();

    /// Returns the protocol, including the trailing colon.
    String getProtocol();

    /// Returns a URL with the protocol updated when the URL Standard permits the change.
    WebURL withProtocol(String value);

    /// Returns the username.
    String getUsername();

    /// Returns a URL with the username updated when the URL can have credentials.
    WebURL withUsername(String value);

    /// Returns the password.
    String getPassword();

    /// Returns a URL with the password updated when the URL can have credentials.
    WebURL withPassword(String value);

    /// Returns the host, including the port when present.
    String getHost();

    /// Returns a URL with the host updated when the URL has a non-opaque path.
    WebURL withHost(String value);

    /// Returns the hostname.
    String getHostname();

    /// Returns a URL with the hostname updated when the URL has a non-opaque path.
    WebURL withHostname(String value);

    /// Returns the port as a string.
    String getPort();

    /// Returns a URL with the port updated when the URL can have a port.
    WebURL withPort(String value);

    /// Returns the serialized pathname.
    String getPathname();

    /// Returns a URL with the pathname updated when the URL has a non-opaque path.
    WebURL withPathname(String value);

    /// Returns the search string, including the leading question mark when non-empty.
    String getSearch();

    /// Returns a URL with the search string updated.
    WebURL withSearch(String value);

    /// Returns immutable search parameters parsed from the current query.
    WebURLSearchParams getSearchParams();

    /// Returns a URL with the query replaced by serialized search parameters.
    WebURL withSearchParams(WebURLSearchParams value);

    /// Returns the hash string, including the leading number sign when non-empty.
    String getHash();

    /// Returns a URL with the hash string updated.
    WebURL withHash(String value);

    /// Returns the JSON representation of this URL.
    String toJSON();

    /// Returns the serialized URL.
    @Override
    String toString();
}
