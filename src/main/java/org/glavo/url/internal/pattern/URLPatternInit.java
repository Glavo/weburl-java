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
package org.glavo.url.internal.pattern;

import org.glavo.url.WebURL;
import org.glavo.url.WebURLPatternSyntaxException;
import org.glavo.url.internal.UrlParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Internal URLPatternInit value.
@NotNullByDefault
public final class URLPatternInit {
    /// Protocol component, or `null` when absent.
    public @Nullable String protocol;
    /// Username component, or `null` when absent.
    public @Nullable String username;
    /// Password component, or `null` when absent.
    public @Nullable String password;
    /// Hostname component, or `null` when absent.
    public @Nullable String hostname;
    /// Port component, or `null` when absent.
    public @Nullable String port;
    /// Pathname component, or `null` when absent.
    public @Nullable String pathname;
    /// Search component, or `null` when absent.
    public @Nullable String search;
    /// Hash component, or `null` when absent.
    public @Nullable String hash;
    /// Base URL string, or `null` when absent.
    public @Nullable String baseURL;

    /// Creates an empty init.
    URLPatternInit() {
    }

    /// Creates an init from components.
    public URLPatternInit(
            @Nullable String protocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String hostname,
            @Nullable String port,
            @Nullable String pathname,
            @Nullable String search,
            @Nullable String hash,
            @Nullable String baseURL
    ) {
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
        this.pathname = pathname;
        this.search = search;
        this.hash = hash;
        this.baseURL = baseURL;
    }

    /// Processes an init according to URLPattern rules.
    static URLPatternInit process(
            URLPatternInit init,
            ProcessType type,
            @Nullable String protocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String hostname,
            @Nullable String port,
            @Nullable String pathname,
            @Nullable String search,
            @Nullable String hash
    ) {
        URLPatternInit result = new URLPatternInit(protocol, username, password, hostname, port, pathname, search,
                hash, null);

        @Nullable WebURL baseURL = null;
        if (init.baseURL != null) {
            try {
                baseURL = WebURL.parse(init.baseURL);
            } catch (RuntimeException exception) {
                throw new WebURLPatternSyntaxException("Invalid URLPattern base URL", exception);
            }

            if (init.protocol == null) {
                result.protocol = URLPatternCanonicalizer.processBaseUrlString(baseURL.getScheme(), type);
            }
            if (type != ProcessType.PATTERN
                    && init.protocol == null && init.hostname == null && init.port == null && init.username == null) {
                result.username = URLPatternCanonicalizer.processBaseUrlString(baseURL.getWebUsername(), type);
            }
            if (type != ProcessType.PATTERN
                    && init.protocol == null && init.hostname == null && init.port == null
                    && init.username == null && init.password == null) {
                result.password = URLPatternCanonicalizer.processBaseUrlString(baseURL.getWebPassword(), type);
            }
            if (init.protocol == null && init.hostname == null) {
                result.hostname = URLPatternCanonicalizer.processBaseUrlString(baseURL.getWebHostname(), type);
            }
            if (init.protocol == null && init.hostname == null && init.port == null) {
                result.port = URLPatternCanonicalizer.processBaseUrlString(baseURL.getWebPort(), type);
            }
            if (init.protocol == null && init.hostname == null && init.port == null && init.pathname == null) {
                result.pathname = URLPatternCanonicalizer.processBaseUrlString(baseURL.getWebPathname(), type);
            }
            if (init.protocol == null && init.hostname == null && init.port == null && init.pathname == null
                    && init.search == null) {
                @Nullable String query = baseURL.getRawQuery();
                result.search = URLPatternCanonicalizer.processBaseUrlString(query == null ? "" : query, type);
            }
            if (init.protocol == null && init.hostname == null && init.port == null && init.pathname == null
                    && init.search == null && init.hash == null) {
                @Nullable String fragment = baseURL.getRawFragment();
                result.hash = URLPatternCanonicalizer.processBaseUrlString(fragment == null ? "" : fragment, type);
            }
        }

        if (init.protocol != null) {
            result.protocol = processProtocol(init.protocol, type);
        }
        if (init.username != null) {
            result.username = processUsername(init.username, type);
        }
        if (init.password != null) {
            result.password = processPassword(init.password, type);
        }
        if (init.hostname != null) {
            result.hostname = processHostname(init.hostname, type);
        }
        if (init.port != null) {
            result.port = processPort(init.port, result.protocol == null ? "fake" : result.protocol, type);
        }
        if (init.pathname != null) {
            result.pathname = init.pathname;
            if (baseURL != null
                    && !hasOpaquePath(baseURL)
                    && !URLPatternCanonicalizer.isAbsolutePathname(result.pathname, type)) {
                String basePathname = URLPatternCanonicalizer.processBaseUrlString(baseURL.getWebPathname(), type);
                int slash = basePathname.lastIndexOf('/');
                if (slash >= 0) {
                    result.pathname = basePathname.substring(0, slash + 1) + result.pathname;
                }
            }
            result.pathname = processPathname(result.pathname, result.protocol == null ? "" : result.protocol, type);
        }
        if (init.search != null) {
            result.search = processSearch(init.search, type);
        }
        if (init.hash != null) {
            result.hash = processHash(init.hash, type);
        }
        return result;
    }

    /// Processes a protocol component.
    private static String processProtocol(String value, ProcessType type) {
        String stripped = value.endsWith(":") ? value.substring(0, value.length() - 1) : value;
        return type == ProcessType.PATTERN ? stripped : URLPatternCanonicalizer.canonicalizeProtocol(stripped);
    }

    /// Processes a username component.
    private static String processUsername(String value, ProcessType type) {
        return type == ProcessType.PATTERN ? value : URLPatternCanonicalizer.canonicalizeUsername(value);
    }

    /// Processes a password component.
    private static String processPassword(String value, ProcessType type) {
        return type == ProcessType.PATTERN ? value : URLPatternCanonicalizer.canonicalizePassword(value);
    }

    /// Processes a hostname component.
    private static String processHostname(String value, ProcessType type) {
        return type == ProcessType.PATTERN ? value : URLPatternCanonicalizer.canonicalizeHostname(value);
    }

    /// Processes a port component.
    private static String processPort(String value, String protocol, ProcessType type) {
        return type == ProcessType.PATTERN ? value : URLPatternCanonicalizer.canonicalizePortWithProtocol(value,
                protocol);
    }

    /// Processes a pathname component.
    private static String processPathname(String value, String protocol, ProcessType type) {
        if (type == ProcessType.PATTERN) {
            return value;
        }
        if (protocol.isEmpty() || UrlParser.isSpecialScheme(protocol)) {
            return URLPatternCanonicalizer.canonicalizePathname(value);
        }
        return URLPatternCanonicalizer.canonicalizeOpaquePathname(value);
    }

    /// Processes a search component.
    private static String processSearch(String value, ProcessType type) {
        String stripped = value.startsWith("?") ? value.substring(1) : value;
        return type == ProcessType.PATTERN ? stripped : URLPatternCanonicalizer.canonicalizeSearch(stripped);
    }

    /// Processes a hash component.
    private static String processHash(String value, ProcessType type) {
        String stripped = value.startsWith("#") ? value.substring(1) : value;
        return type == ProcessType.PATTERN ? stripped : URLPatternCanonicalizer.canonicalizeHash(stripped);
    }

    /// Returns whether a parsed URL has an opaque path using public URL shape.
    private static boolean hasOpaquePath(WebURL url) {
        return url.getHost() == null && !UrlParser.isSpecialScheme(url.getScheme()) && !url.getWebPathname().startsWith("/");
    }

    /// URLPatternInit processing mode.
    enum ProcessType {
        /// Canonicalize components as URL input.
        URL,
        /// Preserve components as pattern input.
        PATTERN
    }
}
