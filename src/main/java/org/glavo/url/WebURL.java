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

    /// Replaces the URL with a parsed absolute URL.
    void setHref(String value);

    /// Returns the serialized origin.
    String getOrigin();

    /// Returns the protocol, including the trailing colon.
    String getProtocol();

    /// Sets the protocol.
    void setProtocol(String value);

    /// Returns the username.
    String getUsername();

    /// Sets the username.
    void setUsername(String value);

    /// Returns the password.
    String getPassword();

    /// Sets the password.
    void setPassword(String value);

    /// Returns the host, including the port when present.
    String getHost();

    /// Sets the host.
    void setHost(String value);

    /// Returns the hostname.
    String getHostname();

    /// Sets the hostname.
    void setHostname(String value);

    /// Returns the port as a string.
    String getPort();

    /// Sets the port.
    void setPort(String value);

    /// Returns the serialized pathname.
    String getPathname();

    /// Sets the pathname.
    void setPathname(String value);

    /// Returns the search string, including the leading question mark when non-empty.
    String getSearch();

    /// Sets the search string.
    void setSearch(String value);

    /// Returns the live search parameters.
    WebURLSearchParams getSearchParams();

    /// Returns the hash string, including the leading number sign when non-empty.
    String getHash();

    /// Sets the hash string.
    void setHash(String value);

    /// Returns the JSON representation of this URL.
    String toJSON();

    /// Returns the serialized URL.
    @Override
    String toString();
}
