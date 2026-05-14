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
import org.glavo.url.internal.pattern.WebURLPatternEngine;
import org.glavo.url.pattern.WebURLPattern;
import org.glavo.url.pattern.WebURLPatternParser;
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
    public static WebURLPattern compile(
            String input,
            @Nullable String baseURL,
            boolean ignoreCase,
            WebURLPatternParser.RegExpPolicy regExpPolicy
    ) {
        return new WebURLPatternImpl(WebURLPatternEngine.compile(input, baseURL, ignoreCase, regExpPolicy));
    }

    /// Creates a pattern from a component builder.
    public static WebURLPattern fromBuilder(
            WebURLPattern.Builder builder,
            boolean ignoreCase,
            WebURLPatternParser.RegExpPolicy regExpPolicy
    ) {
        WebURLPatternBuilderImpl implementation = implementation(Objects.requireNonNull(builder, "builder"));
        return new WebURLPatternImpl(WebURLPatternEngine.compile(implementation.toPatternInit(), ignoreCase,
                regExpPolicy));
    }

    /// Tests a URL string.
    @Override
    @Contract(pure = true)
    public boolean test(String input) {
        return match(input) != null;
    }

    /// Tests a URL string with a base URL.
    @Override
    @Contract(pure = true)
    public boolean test(String input, String baseURL) {
        return match(input, baseURL) != null;
    }

    /// Tests a parsed URL.
    @Override
    @Contract(pure = true)
    public boolean test(WebURL input) {
        return match(input) != null;
    }

    /// Tests component input.
    @Override
    @Contract(pure = true)
    public boolean test(WebURLPattern.Builder input) {
        return match(input) != null;
    }

    /// Matches this pattern against a URL string.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result match(String input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input, null));
    }

    /// Matches this pattern against a URL string with a base URL.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result match(String input, String baseURL) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(baseURL, "baseURL");
        return toResult(engine.match(input, baseURL));
    }

    /// Matches this pattern against a parsed URL.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result match(WebURL input) {
        Objects.requireNonNull(input, "input");
        return toResult(engine.match(input));
    }

    /// Matches this pattern against component input.
    @Override
    @Contract(pure = true)
    public @Nullable WebURLPattern.Result match(WebURLPattern.Builder input) {
        WebURLPatternBuilderImpl implementation = implementation(Objects.requireNonNull(input, "input"));
        return toResult(engine.match(implementation.toPatternInit()));
    }

    /// Returns the scheme component pattern string without a trailing colon.
    @Override
    @Contract(pure = true)
    public String getSchemePattern() {
        return engine.protocol();
    }

    /// Returns the username pattern string.
    @Override
    @Contract(pure = true)
    public String getUsernamePattern() {
        return engine.username();
    }

    /// Returns the password pattern string.
    @Override
    @Contract(pure = true)
    public String getPasswordPattern() {
        return engine.password();
    }

    /// Returns the host component pattern string.
    @Override
    @Contract(pure = true)
    public String getHostPattern() {
        return engine.hostname();
    }

    /// Returns the port pattern string.
    @Override
    @Contract(pure = true)
    public String getPortPattern() {
        return engine.port();
    }

    /// Returns the path component pattern string.
    @Override
    @Contract(pure = true)
    public String getPathPattern() {
        return engine.pathname();
    }

    /// Returns the query component pattern string without a leading question mark.
    @Override
    @Contract(pure = true)
    public String getQueryPattern() {
        return engine.search();
    }

    /// Returns the fragment component pattern string without a leading number sign.
    @Override
    @Contract(pure = true)
    public String getFragmentPattern() {
        return engine.hash();
    }

    /// Returns whether this pattern was compiled with case-insensitive matching.
    @Override
    @Contract(pure = true)
    public boolean isIgnoreCase() {
        return engine.ignoreCase();
    }

    /// Returns whether this pattern preserves URLPattern standard-compatible regular-expression semantics.
    @Override
    @Contract(pure = true)
    public boolean isStandardCompatible() {
        return engine.standardCompatible();
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
        return new WebURLPatternResultImpl(
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
        return new WebURLPatternComponentResultImpl(result.input(), result.range(), result.groupRanges(),
                result.groupIndexes());
    }

    /// Casts a public builder to its internal implementation.
    private static WebURLPatternBuilderImpl implementation(WebURLPattern.Builder builder) {
        if (builder instanceof WebURLPatternBuilderImpl implementation) {
            return implementation;
        }
        throw new IllegalArgumentException("Unsupported WebURLPattern.Builder implementation");
    }
}
