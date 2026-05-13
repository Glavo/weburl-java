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
import org.glavo.url.internal.UrlParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Internal immutable URLPattern matcher.
@NotNullByDefault
public final class WebURLPatternEngine {
    /// Protocol component.
    private final PatternComponent protocol;
    /// Username component.
    private final PatternComponent username;
    /// Password component.
    private final PatternComponent password;
    /// Hostname component.
    private final PatternComponent hostname;
    /// Port component.
    private final PatternComponent port;
    /// Pathname component.
    private final PatternComponent pathname;
    /// Search component.
    private final PatternComponent search;
    /// Hash component.
    private final PatternComponent hash;
    /// Whether matching is case-insensitive.
    private final boolean ignoreCase;

    /// Creates a compiled engine.
    private WebURLPatternEngine(
            PatternComponent protocol,
            PatternComponent username,
            PatternComponent password,
            PatternComponent hostname,
            PatternComponent port,
            PatternComponent pathname,
            PatternComponent search,
            PatternComponent hash,
            boolean ignoreCase
    ) {
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
        this.pathname = pathname;
        this.search = search;
        this.hash = hash;
        this.ignoreCase = ignoreCase;
    }

    /// Compiles a shorthand constructor string.
    public static WebURLPatternEngine compile(String input, @Nullable String baseURL, boolean ignoreCase) {
        URLPatternInit init = ConstructorStringParser.parse(input);
        if (baseURL == null && init.protocol == null) {
            throw new WebURLPatternSyntaxException("URLPattern string input requires a protocol or base URL");
        }
        init.baseURL = baseURL;
        return compile(init, ignoreCase);
    }

    /// Compiles a component URLPattern init.
    public static WebURLPatternEngine compile(URLPatternInit input, boolean ignoreCase) {
        URLPatternInit processed = URLPatternInit.process(input, URLPatternInit.ProcessType.PATTERN,
                null, null, null, null, null, null, null, null);
        fillDefaults(processed);
        normalizeDefaultPort(processed);

        PatternOptions defaultOptions = PatternOptions.DEFAULT.withIgnoreCase(ignoreCase);
        PatternComponent protocol = PatternComponent.compile(require(processed.protocol),
                URLPatternCanonicalizer::canonicalizeProtocol, defaultOptions);
        PatternComponent username = PatternComponent.compile(require(processed.username),
                URLPatternCanonicalizer::canonicalizeUsername, defaultOptions);
        PatternComponent password = PatternComponent.compile(require(processed.password),
                URLPatternCanonicalizer::canonicalizePassword, defaultOptions);
        String hostnameValue = require(processed.hostname);
        PatternComponent hostname = isIpv6Address(hostnameValue)
                ? PatternComponent.exact(URLPatternCanonicalizer.canonicalizeIpv6Hostname(hostnameValue))
                : PatternComponent.compile(hostnameValue, URLPatternCanonicalizer::canonicalizeHostname,
                PatternOptions.HOSTNAME.withIgnoreCase(ignoreCase));
        PatternComponent port = PatternComponent.compile(require(processed.port),
                URLPatternCanonicalizer::canonicalizePort, defaultOptions);
        PatternComponent pathname = protocol.matchesSpecialScheme()
                ? PatternComponent.compile(require(processed.pathname),
                URLPatternCanonicalizer::canonicalizePathname, PatternOptions.PATHNAME.withIgnoreCase(ignoreCase))
                : PatternComponent.compile(require(processed.pathname),
                URLPatternCanonicalizer::canonicalizeOpaquePathname, defaultOptions);
        PatternComponent search = PatternComponent.compile(require(processed.search),
                URLPatternCanonicalizer::canonicalizeSearch, defaultOptions);
        PatternComponent hash = PatternComponent.compile(require(processed.hash),
                URLPatternCanonicalizer::canonicalizeHash, defaultOptions);
        return new WebURLPatternEngine(protocol, username, password, hostname, port, pathname, search, hash,
                ignoreCase);
    }

    /// Matches a parsed URL.
    public @Nullable MatchResult match(WebURL input) {
        return match(toMatchInput(input));
    }

    /// Matches a string input with an optional base URL.
    public @Nullable MatchResult match(String input, @Nullable String baseURL) {
        try {
            WebURL url = baseURL == null ? WebURL.parse(input) : WebURL.parse(input, baseURL);
            return match(url);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /// Matches a component URLPattern init input.
    public @Nullable MatchResult match(URLPatternInit input) {
        URLPatternInit processed;
        try {
            processed = URLPatternInit.process(input, URLPatternInit.ProcessType.URL,
                    "", "", "", "", "", "", "", "");
        } catch (RuntimeException ignored) {
            return null;
        }
        return match(new MatchInput(
                require(processed.protocol),
                require(processed.username),
                require(processed.password),
                require(processed.hostname),
                require(processed.port),
                require(processed.pathname),
                stripPrefix(require(processed.search), '?'),
                stripPrefix(require(processed.hash), '#')
        ));
    }

    /// Returns the protocol pattern string.
    public String protocol() {
        return protocol.pattern();
    }

    /// Returns the username pattern string.
    public String username() {
        return username.pattern();
    }

    /// Returns the password pattern string.
    public String password() {
        return password.pattern();
    }

    /// Returns the hostname pattern string.
    public String hostname() {
        return hostname.pattern();
    }

    /// Returns the port pattern string.
    public String port() {
        return port.pattern();
    }

    /// Returns the pathname pattern string.
    public String pathname() {
        return pathname.pattern();
    }

    /// Returns the search pattern string.
    public String search() {
        return search.pattern();
    }

    /// Returns the hash pattern string.
    public String hash() {
        return hash.pattern();
    }

    /// Returns whether matching is case-insensitive.
    public boolean ignoreCase() {
        return ignoreCase;
    }

    /// Returns whether any component contains custom regular-expression groups.
    public boolean hasRegExpGroups() {
        return protocol.hasRegExpGroups()
                || username.hasRegExpGroups()
                || password.hasRegExpGroups()
                || hostname.hasRegExpGroups()
                || port.hasRegExpGroups()
                || pathname.hasRegExpGroups()
                || search.hasRegExpGroups()
                || hash.hasRegExpGroups();
    }

    /// Matches already prepared input components.
    private @Nullable MatchResult match(MatchInput input) {
        @Nullable ComponentMatch protocolMatch = protocol.match(input.protocol());
        if (protocolMatch == null) {
            return null;
        }
        @Nullable ComponentMatch usernameMatch = username.match(input.username());
        if (usernameMatch == null) {
            return null;
        }
        @Nullable ComponentMatch passwordMatch = password.match(input.password());
        if (passwordMatch == null) {
            return null;
        }
        @Nullable ComponentMatch hostnameMatch = hostname.match(input.hostname());
        if (hostnameMatch == null) {
            return null;
        }
        @Nullable ComponentMatch portMatch = port.match(input.port());
        if (portMatch == null) {
            return null;
        }
        @Nullable ComponentMatch pathnameMatch = pathname.match(input.pathname());
        if (pathnameMatch == null) {
            return null;
        }
        @Nullable ComponentMatch searchMatch = search.match(input.search());
        if (searchMatch == null) {
            return null;
        }
        @Nullable ComponentMatch hashMatch = hash.match(input.hash());
        if (hashMatch == null) {
            return null;
        }
        return new MatchResult(protocolMatch, usernameMatch, passwordMatch, hostnameMatch, portMatch, pathnameMatch,
                searchMatch, hashMatch);
    }

    /// Converts a URL to URLPattern matching components.
    private static MatchInput toMatchInput(WebURL input) {
        @Nullable String query = input.getRawQuery();
        @Nullable String fragment = input.getRawFragment();
        return new MatchInput(
                input.getScheme(),
                input.getWebUsername(),
                input.getWebPassword(),
                input.getWebHostname(),
                input.getWebPort(),
                input.getWebPathname(),
                query == null ? "" : query,
                fragment == null ? "" : fragment
        );
    }

    /// Fills absent pattern components with `*`.
    private static void fillDefaults(URLPatternInit init) {
        if (init.protocol == null) {
            init.protocol = "*";
        }
        if (init.username == null) {
            init.username = "*";
        }
        if (init.password == null) {
            init.password = "*";
        }
        if (init.hostname == null) {
            init.hostname = "*";
        }
        if (init.port == null) {
            init.port = "*";
        }
        if (init.pathname == null) {
            init.pathname = "*";
        }
        if (init.search == null) {
            init.search = "*";
        }
        if (init.hash == null) {
            init.hash = "*";
        }
    }

    /// Removes a pattern port when it is a literal default port for a literal special protocol.
    private static void normalizeDefaultPort(URLPatternInit init) {
        String protocol = require(init.protocol);
        String port = require(init.port);
        if (UrlParser.isSpecialScheme(protocol)) {
            int defaultPort = UrlParser.defaultPort(protocol);
            try {
                String canonicalPort = URLPatternCanonicalizer.canonicalizePort(port);
                init.port = Integer.toString(defaultPort).equals(canonicalPort) ? "" : canonicalPort;
            } catch (WebURLPatternSyntaxException ignored) {
                // Non-literal port patterns are compiled later by the component parser.
            }
        }
    }

    /// Returns whether a hostname pattern starts as an IPv6 address pattern.
    private static boolean isIpv6Address(String input) {
        return input.length() >= 2
                && (input.charAt(0) == '[' || input.startsWith("{[") || input.startsWith("\\["));
    }

    /// Returns a non-null string.
    private static String require(@Nullable String value) {
        if (value == null) {
            throw new WebURLPatternSyntaxException("Missing URLPattern component");
        }
        return value;
    }

    /// Strips a leading prefix character if present.
    private static String stripPrefix(String input, char prefix) {
        return !input.isEmpty() && input.charAt(0) == prefix ? input.substring(1) : input;
    }

    /// Prepared matching components.
    private record MatchInput(
            String protocol,
            String username,
            String password,
            String hostname,
            String port,
            String pathname,
            String search,
            String hash
    ) {
    }

    /// Internal match result.
    public record MatchResult(
            ComponentMatch protocol,
            ComponentMatch username,
            ComponentMatch password,
            ComponentMatch hostname,
            ComponentMatch port,
            ComponentMatch pathname,
            ComponentMatch search,
            ComponentMatch hash
    ) {
    }

    /// Internal component match result.
    public record ComponentMatch(
            String input,
            java.util.LinkedHashMap<String, @Nullable String> groups
    ) {
    }
}
