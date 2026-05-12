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

import org.glavo.url.internal.pattern.WebURLPatternEngine;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// An immutable, precompiled matcher for WHATWG URL Pattern syntax.
///
/// `WebURLPattern` follows the core URLPattern model from the
/// [WHATWG URL Pattern Standard](https://urlpattern.spec.whatwg.org/). It can be created from
/// a shorthand pattern string or from component patterns via [Builder], and it can match URL
/// strings, [WebURL] values, or component inputs.
///
/// The API intentionally follows `WebURL` naming. Java-style getters such as [#getScheme()]
/// and [#getPath()] return normalized pattern strings without URL delimiters. URLPattern
/// attribute getters such as [#getWebProtocol()] and [#getWebPathname()] return the corresponding
/// URLPattern standard attribute values; unlike [WebURL#getWebProtocol()], `getWebProtocol()` on
/// this class does not include a trailing colon.
///
/// Java regular expressions are used as the matching backend. The implementation uses
/// [java.util.regex.Pattern#CASE_INSENSITIVE] and [java.util.regex.Pattern#UNICODE_CASE] for
/// `ignoreCase`; this is not a complete implementation of ECMAScript `v` or `vi` regular
/// expression flags.
///
/// @since 0.3.0
@NotNullByDefault
public final class WebURLPattern {
    /// Compiled internal matcher.
    private final WebURLPatternEngine engine;

    /// Creates a public pattern wrapper.
    private WebURLPattern(WebURLPatternEngine engine) {
        this.engine = engine;
    }

    /// Compiles a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern cannot be compiled
    @Contract("_ -> new")
    public static WebURLPattern compile(String input) {
        return compile(input, null, Options.DEFAULT);
    }

    /// Compiles a shorthand URLPattern string with a base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern or base URL cannot be compiled
    @Contract("_, _ -> new")
    public static WebURLPattern compile(String input, String baseURL) {
        return compile(input, baseURL, Options.DEFAULT);
    }

    /// Compiles a shorthand URLPattern string with a base URL and options.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @param options compilation options
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern or base URL cannot be compiled
    @Contract("_, _, _ -> new")
    public static WebURLPattern compile(String input, @Nullable String baseURL, Options options) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(options, "options");
        return new WebURLPattern(WebURLPatternEngine.compile(input, baseURL, options.ignoreCase()));
    }

    /// Compiles a component URLPattern builder.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the components cannot be compiled
    @Contract("_ -> new")
    public static WebURLPattern compile(Builder builder) {
        return compile(builder, Options.DEFAULT);
    }

    /// Compiles a component URLPattern builder with options.
    ///
    /// @param builder the component pattern builder
    /// @param options compilation options
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the components cannot be compiled
    @Contract("_, _ -> new")
    public static WebURLPattern compile(Builder builder, Options options) {
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(options, "options");
        return new WebURLPattern(WebURLPatternEngine.compile(builder.toEngineInit(), options.ignoreCase()));
    }

    /// Tries to compile a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern, or `null` when compilation fails
    public static @Nullable WebURLPattern tryCompile(String input) {
        return tryCompile(input, null, Options.DEFAULT);
    }

    /// Tries to compile a shorthand URLPattern string with a base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @return the compiled URL pattern, or `null` when compilation fails
    public static @Nullable WebURLPattern tryCompile(String input, String baseURL) {
        return tryCompile(input, baseURL, Options.DEFAULT);
    }

    /// Tries to compile a shorthand URLPattern string with a base URL and options.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @param options compilation options
    /// @return the compiled URL pattern, or `null` when compilation fails
    public static @Nullable WebURLPattern tryCompile(String input, @Nullable String baseURL, Options options) {
        try {
            return compile(input, baseURL, options);
        } catch (WebURLPatternSyntaxException ignored) {
            return null;
        }
    }

    /// Tries to compile a component URLPattern builder.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern, or `null` when compilation fails
    public static @Nullable WebURLPattern tryCompile(Builder builder) {
        return tryCompile(builder, Options.DEFAULT);
    }

    /// Tries to compile a component URLPattern builder with options.
    ///
    /// @param builder the component pattern builder
    /// @param options compilation options
    /// @return the compiled URL pattern, or `null` when compilation fails
    public static @Nullable WebURLPattern tryCompile(Builder builder, Options options) {
        try {
            return compile(builder, options);
        } catch (WebURLPatternSyntaxException ignored) {
            return null;
        }
    }

    /// Creates a new mutable URLPattern component builder.
    ///
    /// @return a new builder
    @Contract("-> new")
    public static Builder newBuilder() {
        return new Builder();
    }

    /// Tests a URL string.
    ///
    /// @param input the URL input string
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    public boolean test(String input) {
        return exec(input) != null;
    }

    /// Tests a URL string with a base URL.
    ///
    /// @param input the URL input string
    /// @param baseURL the base URL used for relative input
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    public boolean test(String input, String baseURL) {
        return exec(input, baseURL) != null;
    }

    /// Tests a parsed URL.
    ///
    /// @param input the URL input value
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    public boolean test(WebURL input) {
        return exec(input) != null;
    }

    /// Tests component input.
    ///
    /// @param input the component input builder
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    public boolean test(Builder input) {
        return exec(input) != null;
    }

    /// Executes this pattern against a URL string.
    ///
    /// @param input the URL input string
    /// @return the match result, or `null` when the input does not match or is not a valid URL
    @Contract(pure = true)
    public @Nullable Result exec(String input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input, null));
    }

    /// Executes this pattern against a URL string with a base URL.
    ///
    /// @param input the URL input string
    /// @param baseURL the base URL used for relative input
    /// @return the match result, or `null` when the input does not match or cannot be parsed
    @Contract(pure = true)
    public @Nullable Result exec(String input, String baseURL) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(baseURL, "baseURL");
        return toResult(engine.match(input, baseURL));
    }

    /// Executes this pattern against a parsed URL.
    ///
    /// @param input the URL input value
    /// @return the match result, or `null` when the input does not match
    @Contract(pure = true)
    public @Nullable Result exec(WebURL input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input));
    }

    /// Executes this pattern against component input.
    ///
    /// @param input the component input builder
    /// @return the match result, or `null` when the input does not match or cannot be canonicalized
    @Contract(pure = true)
    public @Nullable Result exec(Builder input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input.toEngineInit()));
    }

    /// Returns the protocol pattern string without a trailing colon.
    @Contract(pure = true)
    public String getScheme() {
        return engine.protocol();
    }

    /// Returns the username pattern string.
    @Contract(pure = true)
    public String getUsername() {
        return engine.username();
    }

    /// Returns the password pattern string.
    @Contract(pure = true)
    public String getPassword() {
        return engine.password();
    }

    /// Returns the hostname pattern string.
    @Contract(pure = true)
    public String getHost() {
        return engine.hostname();
    }

    /// Returns the port pattern string.
    @Contract(pure = true)
    public String getPort() {
        return engine.port();
    }

    /// Returns the pathname pattern string.
    @Contract(pure = true)
    public String getPath() {
        return engine.pathname();
    }

    /// Returns the search pattern string without a leading question mark.
    @Contract(pure = true)
    public String getQuery() {
        return engine.search();
    }

    /// Returns the hash pattern string without a leading number sign.
    @Contract(pure = true)
    public String getFragment() {
        return engine.hash();
    }

    /// Returns the URLPattern `protocol` attribute pattern string.
    ///
    /// Unlike [WebURL#getWebProtocol()], this URLPattern attribute does not include a trailing colon.
    @Contract(pure = true)
    public String getWebProtocol() {
        return engine.protocol();
    }

    /// Returns the URLPattern `username` attribute pattern string.
    @Contract(pure = true)
    public String getWebUsername() {
        return engine.username();
    }

    /// Returns the URLPattern `password` attribute pattern string.
    @Contract(pure = true)
    public String getWebPassword() {
        return engine.password();
    }

    /// Returns the URLPattern `hostname` attribute pattern string.
    @Contract(pure = true)
    public String getWebHostname() {
        return engine.hostname();
    }

    /// Returns the URLPattern `port` attribute pattern string.
    @Contract(pure = true)
    public String getWebPort() {
        return engine.port();
    }

    /// Returns the URLPattern `pathname` attribute pattern string.
    @Contract(pure = true)
    public String getWebPathname() {
        return engine.pathname();
    }

    /// Returns the URLPattern `search` attribute pattern string without a leading question mark.
    @Contract(pure = true)
    public String getWebSearch() {
        return engine.search();
    }

    /// Returns the URLPattern `hash` attribute pattern string without a leading number sign.
    @Contract(pure = true)
    public String getWebHash() {
        return engine.hash();
    }

    /// Returns whether this pattern was compiled with case-insensitive matching.
    @Contract(pure = true)
    public boolean isIgnoreCase() {
        return engine.ignoreCase();
    }

    /// Returns whether any component contains custom regular-expression groups.
    @Contract(pure = true)
    public boolean hasRegExpGroups() {
        return engine.hasRegExpGroups();
    }

    /// Converts an internal result into the public result type.
    private static @Nullable Result toResult(@Nullable WebURLPatternEngine.MatchResult result) {
        if (result == null) {
            return null;
        }
        return new Result(
                toComponentResult(result.protocol()),
                toComponentResult(result.username()),
                toComponentResult(result.password()),
                toComponentResult(result.hostname()),
                toComponentResult(result.port()),
                toComponentResult(result.pathname()),
                toComponentResult(result.search()),
                toComponentResult(result.hash())
        );
    }

    /// Converts an internal component result.
    private static ComponentResult toComponentResult(WebURLPatternEngine.ComponentMatch result) {
        return new ComponentResult(result.input(), result.groups());
    }

    /// Mutable builder for URLPattern component patterns or component match input.
    ///
    /// The builder stores URLPattern standard component names. Setter names mirror `WebURL`:
    /// scheme maps to URLPattern `protocol`, host maps to URLPattern `hostname`, path maps to
    /// URLPattern `pathname`, query maps to URLPattern `search`, and fragment maps to URLPattern
    /// `hash`.
    @NotNullByDefault
    public static final class Builder {
        /// Protocol or scheme component.
        private @Nullable String scheme;
        /// Username component.
        private @Nullable String username;
        /// Password component.
        private @Nullable String password;
        /// Hostname component.
        private @Nullable String host;
        /// Port component.
        private @Nullable String port;
        /// Pathname or path component.
        private @Nullable String path;
        /// Search or query component.
        private @Nullable String query;
        /// Hash or fragment component.
        private @Nullable String fragment;
        /// Base URL string.
        private @Nullable String baseURL;

        /// Creates an empty builder.
        private Builder() {
        }

        /// Sets the protocol or scheme component.
        @Contract("_ -> this")
        public Builder setScheme(@Nullable String scheme) {
            this.scheme = scheme;
            return this;
        }

        /// Sets the username component.
        @Contract("_ -> this")
        public Builder setUsername(@Nullable String username) {
            this.username = username;
            return this;
        }

        /// Sets the password component.
        @Contract("_ -> this")
        public Builder setPassword(@Nullable String password) {
            this.password = password;
            return this;
        }

        /// Sets the hostname or host component.
        @Contract("_ -> this")
        public Builder setHost(@Nullable String host) {
            this.host = host;
            return this;
        }

        /// Sets the port component.
        @Contract("_ -> this")
        public Builder setPort(@Nullable String port) {
            this.port = port;
            return this;
        }

        /// Sets the pathname or path component.
        @Contract("_ -> this")
        public Builder setPath(@Nullable String path) {
            this.path = path;
            return this;
        }

        /// Sets the search or query component.
        @Contract("_ -> this")
        public Builder setQuery(@Nullable String query) {
            this.query = query;
            return this;
        }

        /// Sets the hash or fragment component.
        @Contract("_ -> this")
        public Builder setFragment(@Nullable String fragment) {
            this.fragment = fragment;
            return this;
        }

        /// Sets the base URL.
        @Contract("_ -> this")
        public Builder setBaseURL(@Nullable String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        /// Converts this builder to the internal init type.
        private WebURLPatternEngine.Init toEngineInit() {
            return new WebURLPatternEngine.Init(scheme, username, password, host, port, path, query, fragment, baseURL);
        }
    }

    /// URLPattern compilation options.
    ///
    /// @param ignoreCase whether component matching should be case-insensitive
    public record Options(boolean ignoreCase) {
        /// Default URLPattern options.
        public static final Options DEFAULT = new Options(false);
    }

    /// Result for one matched URLPattern component.
    ///
    /// @param input the matched component input
    /// @param groups named and numeric capture groups, with `null` for unmatched optional groups
    public record ComponentResult(String input, @Unmodifiable Map<String, @Nullable String> groups) {
        /// Creates a component result.
        public ComponentResult {
            Objects.requireNonNull(input, "input");
            Objects.requireNonNull(groups, "groups");
            groups = Collections.unmodifiableMap(new LinkedHashMap<>(groups));
        }
    }

    /// Result for a successful URLPattern match.
    ///
    /// @param protocol protocol component result
    /// @param username username component result
    /// @param password password component result
    /// @param hostname hostname component result
    /// @param port port component result
    /// @param pathname pathname component result
    /// @param search search component result
    /// @param hash hash component result
    public record Result(
            ComponentResult protocol,
            ComponentResult username,
            ComponentResult password,
            ComponentResult hostname,
            ComponentResult port,
            ComponentResult pathname,
            ComponentResult search,
            ComponentResult hash
    ) {
    }
}
