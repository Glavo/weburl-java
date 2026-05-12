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
import org.glavo.url.WebURLPattern;
import org.glavo.url.internal.pattern.WebURLPatternEngine;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal immutable implementation of `WebURLPattern`.
@NotNullByDefault
public final class WebURLPatternImpl implements WebURLPattern {
    /// Compiled internal matcher.
    private final WebURLPatternEngine engine;

    /// Creates a pattern wrapper for an internal engine.
    private WebURLPatternImpl(WebURLPatternEngine engine) {
        this.engine = engine;
    }

    /// Compiles a shorthand constructor string.
    public static WebURLPattern compile(String input, @Nullable String baseURL, boolean ignoreCase) {
        return new WebURLPatternImpl(WebURLPatternEngine.compile(input, baseURL, ignoreCase));
    }

    /// Compiles a component builder.
    public static WebURLPattern compile(WebURLPattern.Builder builder, boolean ignoreCase) {
        WebURLPatternBuilderImpl implementation = implementation(Objects.requireNonNull(builder, "builder"));
        return new WebURLPatternImpl(WebURLPatternEngine.compile(implementation.toEngineInit(), ignoreCase));
    }

    /// Tests a URL string.
    @Override
    @Contract(pure = true)
    public boolean test(String input) {
        return exec(input) != null;
    }

    /// Tests a URL string with a base URL.
    @Override
    @Contract(pure = true)
    public boolean test(String input, String baseURL) {
        return exec(input, baseURL) != null;
    }

    /// Tests a parsed URL.
    @Override
    @Contract(pure = true)
    public boolean test(WebURL input) {
        return exec(input) != null;
    }

    /// Tests component input.
    @Override
    @Contract(pure = true)
    public boolean test(WebURLPattern.Builder input) {
        return exec(input) != null;
    }

    /// Executes this pattern against a URL string.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result exec(String input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input, null));
    }

    /// Executes this pattern against a URL string with a base URL.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result exec(String input, String baseURL) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(baseURL, "baseURL");
        return toResult(engine.match(input, baseURL));
    }

    /// Executes this pattern against a parsed URL.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result exec(WebURL input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input));
    }

    /// Executes this pattern against component input.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result exec(WebURLPattern.Builder input) {
        WebURLPatternBuilderImpl implementation = implementation(Objects.requireNonNull(input, "input"));
        return toResult(engine.match(implementation.toEngineInit()));
    }

    /// Returns the protocol pattern string without a trailing colon.
    @Override
    @Contract(pure = true)
    public String getScheme() {
        return engine.protocol();
    }

    /// Returns the username pattern string.
    @Override
    @Contract(pure = true)
    public String getUsername() {
        return engine.username();
    }

    /// Returns the password pattern string.
    @Override
    @Contract(pure = true)
    public String getPassword() {
        return engine.password();
    }

    /// Returns the hostname pattern string.
    @Override
    @Contract(pure = true)
    public String getHost() {
        return engine.hostname();
    }

    /// Returns the port pattern string.
    @Override
    @Contract(pure = true)
    public String getPort() {
        return engine.port();
    }

    /// Returns the pathname pattern string.
    @Override
    @Contract(pure = true)
    public String getPath() {
        return engine.pathname();
    }

    /// Returns the search pattern string without a leading question mark.
    @Override
    @Contract(pure = true)
    public String getQuery() {
        return engine.search();
    }

    /// Returns the hash pattern string without a leading number sign.
    @Override
    @Contract(pure = true)
    public String getFragment() {
        return engine.hash();
    }

    /// Returns the URLPattern `protocol` attribute pattern string.
    @Override
    @Contract(pure = true)
    public String getWebProtocol() {
        return engine.protocol();
    }

    /// Returns the URLPattern `username` attribute pattern string.
    @Override
    @Contract(pure = true)
    public String getWebUsername() {
        return engine.username();
    }

    /// Returns the URLPattern `password` attribute pattern string.
    @Override
    @Contract(pure = true)
    public String getWebPassword() {
        return engine.password();
    }

    /// Returns the URLPattern `hostname` attribute pattern string.
    @Override
    @Contract(pure = true)
    public String getWebHostname() {
        return engine.hostname();
    }

    /// Returns the URLPattern `port` attribute pattern string.
    @Override
    @Contract(pure = true)
    public String getWebPort() {
        return engine.port();
    }

    /// Returns the URLPattern `pathname` attribute pattern string.
    @Override
    @Contract(pure = true)
    public String getWebPathname() {
        return engine.pathname();
    }

    /// Returns the URLPattern `search` attribute pattern string without a leading question mark.
    @Override
    @Contract(pure = true)
    public String getWebSearch() {
        return engine.search();
    }

    /// Returns the URLPattern `hash` attribute pattern string without a leading number sign.
    @Override
    @Contract(pure = true)
    public String getWebHash() {
        return engine.hash();
    }

    /// Returns whether this pattern was compiled with case-insensitive matching.
    @Override
    @Contract(pure = true)
    public boolean isIgnoreCase() {
        return engine.ignoreCase();
    }

    /// Returns whether any component contains custom regular-expression groups.
    @Override
    @Contract(pure = true)
    public boolean hasRegExpGroups() {
        return engine.hasRegExpGroups();
    }

    /// Converts an internal result into the public result type.
    private static @Nullable WebURLPattern.Result toResult(@Nullable WebURLPatternEngine.MatchResult result) {
        if (result == null) {
            return null;
        }
        return new WebURLPattern.Result(
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
    private static WebURLPattern.ComponentResult toComponentResult(WebURLPatternEngine.ComponentMatch result) {
        return new WebURLPattern.ComponentResult(result.input(), result.groups());
    }

    /// Casts a public builder to its internal implementation.
    private static WebURLPatternBuilderImpl implementation(WebURLPattern.Builder builder) {
        if (builder instanceof WebURLPatternBuilderImpl implementation) {
            return implementation;
        }
        throw new IllegalArgumentException("Unsupported WebURLPattern.Builder implementation");
    }
}
