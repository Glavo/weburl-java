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

import org.glavo.url.internal.WebURLPatternBuilderImpl;
import org.glavo.url.internal.WebURLPatternComponentResultImpl;
import org.glavo.url.internal.WebURLPatternImpl;
import org.glavo.url.internal.WebURLPatternOptionsImpl;
import org.glavo.url.internal.WebURLPatternResultImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
/// this interface does not include a trailing colon.
///
/// Custom regular-expression groups are not supported yet because URLPattern uses ECMAScript
/// `v` / `vi` regular-expression semantics, which are not equivalent to Java regular expressions.
/// Pattern strings containing custom regular-expression groups are rejected during compilation.
///
/// @since 0.3.0
@NotNullByDefault
public sealed interface WebURLPattern permits WebURLPatternImpl {
    /// Compiles a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern cannot be compiled
    @Contract("_ -> new")
    static WebURLPattern compile(String input) {
        return compile(input, null, Options.DEFAULT);
    }

    /// Compiles a shorthand URLPattern string with a base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern or base URL cannot be compiled
    @Contract("_, _ -> new")
    static WebURLPattern compile(String input, String baseURL) {
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
    static WebURLPattern compile(String input, @Nullable String baseURL, Options options) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(options, "options");
        return WebURLPatternImpl.compile(input, baseURL, options.ignoreCase());
    }

    /// Compiles a component URLPattern builder.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the components cannot be compiled
    @Contract("_ -> new")
    static WebURLPattern compile(Builder builder) {
        return compile(builder, Options.DEFAULT);
    }

    /// Compiles a component URLPattern builder with options.
    ///
    /// @param builder the component pattern builder
    /// @param options compilation options
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the components cannot be compiled
    @Contract("_, _ -> new")
    static WebURLPattern compile(Builder builder, Options options) {
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(options, "options");
        return WebURLPatternImpl.compile(builder, options.ignoreCase());
    }

    /// Tries to compile a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(String input) {
        return tryCompile(input, null, Options.DEFAULT);
    }

    /// Tries to compile a shorthand URLPattern string with a base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(String input, String baseURL) {
        return tryCompile(input, baseURL, Options.DEFAULT);
    }

    /// Tries to compile a shorthand URLPattern string with a base URL and options.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings
    /// @param options compilation options
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(String input, @Nullable String baseURL, Options options) {
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
    static @Nullable WebURLPattern tryCompile(Builder builder) {
        return tryCompile(builder, Options.DEFAULT);
    }

    /// Tries to compile a component URLPattern builder with options.
    ///
    /// @param builder the component pattern builder
    /// @param options compilation options
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(Builder builder, Options options) {
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
    static Builder newBuilder() {
        return new WebURLPatternBuilderImpl();
    }

    /// Tests a URL string.
    ///
    /// @param input the URL input string
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    boolean test(String input);

    /// Tests a URL string with a base URL.
    ///
    /// @param input the URL input string
    /// @param baseURL the base URL used for relative input
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    boolean test(String input, String baseURL);

    /// Tests a parsed URL.
    ///
    /// @param input the URL input value
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    boolean test(WebURL input);

    /// Tests component input.
    ///
    /// @param input the component input builder
    /// @return `true` if the input matches this pattern
    @Contract(pure = true)
    boolean test(Builder input);

    /// Executes this pattern against a URL string.
    ///
    /// @param input the URL input string
    /// @return the match result, or `null` when the input does not match or is not a valid URL
    @Contract(pure = true)
    @Nullable Result exec(String input);

    /// Executes this pattern against a URL string with a base URL.
    ///
    /// @param input the URL input string
    /// @param baseURL the base URL used for relative input
    /// @return the match result, or `null` when the input does not match or cannot be parsed
    @Contract(pure = true)
    @Nullable Result exec(String input, String baseURL);

    /// Executes this pattern against a parsed URL.
    ///
    /// @param input the URL input value
    /// @return the match result, or `null` when the input does not match
    @Contract(pure = true)
    @Nullable Result exec(WebURL input);

    /// Executes this pattern against component input.
    ///
    /// @param input the component input builder
    /// @return the match result, or `null` when the input does not match or cannot be canonicalized
    @Contract(pure = true)
    @Nullable Result exec(Builder input);

    /// Returns the protocol pattern string without a trailing colon.
    @Contract(pure = true)
    String getScheme();

    /// Returns the username pattern string.
    @Contract(pure = true)
    String getUsername();

    /// Returns the password pattern string.
    @Contract(pure = true)
    String getPassword();

    /// Returns the hostname pattern string.
    @Contract(pure = true)
    String getHost();

    /// Returns the port pattern string.
    @Contract(pure = true)
    String getPort();

    /// Returns the pathname pattern string.
    @Contract(pure = true)
    String getPath();

    /// Returns the search pattern string without a leading question mark.
    @Contract(pure = true)
    String getQuery();

    /// Returns the hash pattern string without a leading number sign.
    @Contract(pure = true)
    String getFragment();

    /// Returns the URLPattern `protocol` attribute pattern string.
    ///
    /// Unlike [WebURL#getWebProtocol()], this URLPattern attribute does not include a trailing colon.
    @Contract(pure = true)
    String getWebProtocol();

    /// Returns the URLPattern `username` attribute pattern string.
    @Contract(pure = true)
    String getWebUsername();

    /// Returns the URLPattern `password` attribute pattern string.
    @Contract(pure = true)
    String getWebPassword();

    /// Returns the URLPattern `hostname` attribute pattern string.
    @Contract(pure = true)
    String getWebHostname();

    /// Returns the URLPattern `port` attribute pattern string.
    @Contract(pure = true)
    String getWebPort();

    /// Returns the URLPattern `pathname` attribute pattern string.
    @Contract(pure = true)
    String getWebPathname();

    /// Returns the URLPattern `search` attribute pattern string without a leading question mark.
    @Contract(pure = true)
    String getWebSearch();

    /// Returns the URLPattern `hash` attribute pattern string without a leading number sign.
    @Contract(pure = true)
    String getWebHash();

    /// Returns whether this pattern was compiled with case-insensitive matching.
    @Contract(pure = true)
    boolean isIgnoreCase();

    /// Returns whether any component contains custom regular-expression groups.
    ///
    /// This currently returns `false` for every successfully compiled pattern because custom
    /// regular-expression groups are rejected during compilation.
    @Contract(pure = true)
    boolean hasRegExpGroups();

    /// Mutable builder for URLPattern component patterns or component match input.
    ///
    /// The builder stores URLPattern standard component names. Setter names mirror `WebURL`:
    /// scheme maps to URLPattern `protocol`, host maps to URLPattern `hostname`, path maps to
    /// URLPattern `pathname`, query maps to URLPattern `search`, and fragment maps to URLPattern
    /// `hash`.
    @NotNullByDefault
    sealed interface Builder permits WebURLPatternBuilderImpl {
        /// Sets the protocol or scheme component.
        ///
        /// @param scheme the protocol or scheme component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setScheme(@Nullable String scheme);

        /// Sets the username component.
        ///
        /// @param username the username component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setUsername(@Nullable String username);

        /// Sets the password component.
        ///
        /// @param password the password component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setPassword(@Nullable String password);

        /// Sets the hostname or host component.
        ///
        /// @param host the hostname component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setHost(@Nullable String host);

        /// Sets the port component.
        ///
        /// @param port the port component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setPort(@Nullable String port);

        /// Sets the pathname or path component.
        ///
        /// @param path the pathname component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setPath(@Nullable String path);

        /// Sets the search or query component.
        ///
        /// @param query the search component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setQuery(@Nullable String query);

        /// Sets the hash or fragment component.
        ///
        /// @param fragment the hash component, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setFragment(@Nullable String fragment);

        /// Sets the base URL.
        ///
        /// @param baseURL the base URL string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setBaseURL(@Nullable String baseURL);
    }

    /// URLPattern compilation options.
    @NotNullByDefault
    sealed interface Options permits WebURLPatternOptionsImpl {
        /// Default URLPattern options.
        Options DEFAULT = new WebURLPatternOptionsImpl(false);

        /// Creates URLPattern compilation options.
        ///
        /// @param ignoreCase whether component matching should be case-insensitive
        /// @return new URLPattern compilation options
        @Contract("_ -> new")
        static Options of(boolean ignoreCase) {
            return new WebURLPatternOptionsImpl(ignoreCase);
        }

        /// Returns whether component matching should be case-insensitive.
        ///
        /// @return `true` for case-insensitive matching
        @Contract(pure = true)
        boolean ignoreCase();
    }

    /// Result for one matched URLPattern component.
    @NotNullByDefault
    sealed interface ComponentResult permits WebURLPatternComponentResultImpl {
        /// Returns the matched component input.
        ///
        /// @return the matched component input
        @Contract(pure = true)
        String input();

        /// Returns named and numeric capture groups.
        ///
        /// Unmatched optional groups are represented by `null` values.
        ///
        /// @return immutable capture groups
        @Contract(pure = true)
        @Unmodifiable Map<String, @Nullable String> groups();
    }

    /// Result for a successful URLPattern match.
    @NotNullByDefault
    sealed interface Result permits WebURLPatternResultImpl {
        /// Returns the protocol component result.
        ///
        /// @return the protocol component result
        @Contract(pure = true)
        ComponentResult protocol();

        /// Returns the username component result.
        ///
        /// @return the username component result
        @Contract(pure = true)
        ComponentResult username();

        /// Returns the password component result.
        ///
        /// @return the password component result
        @Contract(pure = true)
        ComponentResult password();

        /// Returns the hostname component result.
        ///
        /// @return the hostname component result
        @Contract(pure = true)
        ComponentResult hostname();

        /// Returns the port component result.
        ///
        /// @return the port component result
        @Contract(pure = true)
        ComponentResult port();

        /// Returns the pathname component result.
        ///
        /// @return the pathname component result
        @Contract(pure = true)
        ComponentResult pathname();

        /// Returns the search component result.
        ///
        /// @return the search component result
        @Contract(pure = true)
        ComponentResult search();

        /// Returns the hash component result.
        ///
        /// @return the hash component result
        @Contract(pure = true)
        ComponentResult hash();
    }
}
