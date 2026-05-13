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

import org.glavo.url.pattern.WebURLPattern;
import org.glavo.url.pattern.WebURLPatternParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal immutable implementation of `WebURLPatternParser`.
@NotNullByDefault
public final class WebURLPatternParserImpl implements WebURLPatternParser {
    /// The default case-sensitive parser.
    public static final WebURLPatternParserImpl DEFAULT =
            new WebURLPatternParserImpl(false, RegExpPolicy.SUPPORTED);

    /// The case-insensitive parser with default regular-expression policy.
    private static final WebURLPatternParserImpl IGNORE_CASE =
            new WebURLPatternParserImpl(true, RegExpPolicy.SUPPORTED);

    /// Whether compiled patterns use case-insensitive matching.
    private final boolean ignoreCase;
    /// How user-written regular-expression elements are handled.
    private final RegExpPolicy regExpPolicy;

    /// Creates a URLPattern parser implementation.
    ///
    /// @param ignoreCase whether compiled patterns should use case-insensitive matching
    /// @param regExpPolicy regular-expression element policy
    private WebURLPatternParserImpl(boolean ignoreCase, RegExpPolicy regExpPolicy) {
        this.ignoreCase = ignoreCase;
        this.regExpPolicy = Objects.requireNonNull(regExpPolicy, "regExpPolicy");
    }

    /// Returns whether this parser compiles case-insensitive patterns.
    @Override
    @Contract(pure = true)
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /// Returns a parser with case-insensitive matching enabled.
    @Override
    @Contract(pure = true)
    public WebURLPatternParser withIgnoreCase() {
        return withIgnoreCase(true);
    }

    /// Returns a parser with the specified case sensitivity policy.
    @Override
    @Contract(pure = true)
    public WebURLPatternParser withIgnoreCase(boolean ignoreCase) {
        return withOptions(ignoreCase, regExpPolicy);
    }

    /// Returns a parser with the specified regular-expression element policy.
    @Override
    @Contract(pure = true)
    public WebURLPatternParser withRegExpPolicy(RegExpPolicy policy) {
        return withOptions(ignoreCase, Objects.requireNonNull(policy, "policy"));
    }

    /// Returns how this parser handles user-written regular-expression elements.
    @Override
    @Contract(pure = true)
    public RegExpPolicy getRegExpPolicy() {
        return regExpPolicy;
    }

    /// Compiles a shorthand URLPattern string.
    @Override
    @Contract("_ -> new")
    public WebURLPattern compile(String input) {
        return compile(input, null);
    }

    /// Compiles a shorthand URLPattern string with an optional base URL.
    @Override
    @Contract("_, _ -> new")
    public WebURLPattern compile(String input, @Nullable String baseURL) {
        Objects.requireNonNull(input, "input");
        return WebURLPatternImpl.compile(input, baseURL, ignoreCase, regExpPolicy);
    }

    /// Compiles a component URLPattern builder.
    @Override
    @Contract("_ -> new")
    public WebURLPattern compile(WebURLPattern.Builder builder) {
        Objects.requireNonNull(builder, "builder");
        return WebURLPatternImpl.compile(builder, ignoreCase, regExpPolicy);
    }

    /// Tries to compile a shorthand URLPattern string.
    @Override
    public @Nullable WebURLPattern tryCompile(String input) {
        return tryCompile(input, null);
    }

    /// Tries to compile a shorthand URLPattern string with an optional base URL.
    @Override
    public @Nullable WebURLPattern tryCompile(String input, @Nullable String baseURL) {
        try {
            return compile(input, baseURL);
        } catch (WebURLPatternSyntaxException ignored) {
            return null;
        }
    }

    /// Tries to compile a component URLPattern builder.
    @Override
    public @Nullable WebURLPattern tryCompile(WebURLPattern.Builder builder) {
        try {
            return compile(builder);
        } catch (WebURLPatternSyntaxException ignored) {
            return null;
        }
    }

    /// Compares this parser with another object.
    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable Object obj) {
        return this == obj || obj instanceof WebURLPatternParser other
                && ignoreCase == other.isIgnoreCase()
                && regExpPolicy == other.getRegExpPolicy();
    }

    /// Returns the hash code of this parser.
    @Override
    @Contract(pure = true)
    public int hashCode() {
        return 31 * Boolean.hashCode(ignoreCase) + regExpPolicy.hashCode();
    }

    /// Returns a string representation of this parser.
    @Override
    @Contract(pure = true)
    public String toString() {
        if (!ignoreCase && regExpPolicy == RegExpPolicy.SUPPORTED) {
            return "WebURLPatternParser.DEFAULT";
        }
        return "WebURLPatternParser[ignoreCase=" + ignoreCase + ", regExpPolicy=" + regExpPolicy + "]";
    }

    /// Returns a parser with the given options.
    private WebURLPatternParser withOptions(boolean ignoreCase, RegExpPolicy regExpPolicy) {
        if (this.ignoreCase == ignoreCase && this.regExpPolicy == regExpPolicy) {
            return this;
        }
        if (regExpPolicy == RegExpPolicy.SUPPORTED) {
            return ignoreCase ? IGNORE_CASE : DEFAULT;
        }
        return new WebURLPatternParserImpl(ignoreCase, regExpPolicy);
    }
}
