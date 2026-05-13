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
package org.glavo.url.pattern;

import org.glavo.url.internal.WebURLPatternParserImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A reusable parser for compiling immutable [WebURLPattern] matchers.
///
/// `WebURLPatternParser` encapsulates URLPattern compilation policy. The static convenience methods
/// on [WebURLPattern] use [#getDefault()]. Callers that need case-insensitive matching can derive a
/// parser with [#withIgnoreCase()] or [#withIgnoreCase(boolean)] instead of passing per-call option objects.
///
/// # Thread Safety and Reuse
///
/// The built-in parser instances are immutable, thread-safe, and reusable.
///
/// # Usage
///
/// ```java
/// WebURLPatternParser parser = WebURLPatternParser.getDefault().withIgnoreCase();
/// WebURLPattern pattern = parser.compile("https://example.com/users/:id");
/// pattern.test("https://example.com/Users/42"); // true
/// ```
///
/// @see WebURLPattern
/// @see WebURLPatternSyntaxException
/// @since 0.3.0
@NotNullByDefault
public sealed interface WebURLPatternParser permits WebURLPatternParserImpl {
    /// Returns the default URLPattern parser.
    ///
    /// This parser compiles patterns for case-sensitive matching.
    ///
    /// @return the default parser
    @Contract(pure = true)
    static WebURLPatternParser getDefault() {
        return WebURLPatternParserImpl.DEFAULT;
    }

    /// Returns a parser with case-insensitive matching enabled.
    ///
    /// If this parser already uses case-insensitive matching, this method may return this parser.
    ///
    /// @return a parser that compiles case-insensitive patterns
    @Contract(pure = true)
    WebURLPatternParser withIgnoreCase();

    /// Returns a parser with the specified case sensitivity policy.
    ///
    /// If this parser already uses the requested policy, this method may return this parser.
    ///
    /// @param ignoreCase whether compiled patterns should use case-insensitive matching
    /// @return a parser that uses the specified case sensitivity policy
    @Contract(pure = true)
    WebURLPatternParser withIgnoreCase(boolean ignoreCase);

    /// Returns whether patterns compiled by this parser use case-insensitive matching.
    ///
    /// @return `true` when this parser compiles case-insensitive patterns
    @Contract(pure = true)
    boolean isIgnoreCase();

    /// Compiles a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern cannot be compiled
    @Contract("_ -> new")
    WebURLPattern compile(String input);

    /// Compiles a shorthand URLPattern string with an optional base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings, or `null` for no base URL
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern or base URL cannot be compiled
    @Contract("_, _ -> new")
    WebURLPattern compile(String input, @Nullable String baseURL);

    /// Compiles a component URLPattern builder.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the components cannot be compiled
    @Contract("_ -> new")
    WebURLPattern compile(WebURLPattern.Builder builder);

    /// Tries to compile a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern, or `null` when compilation fails
    @Nullable WebURLPattern tryCompile(String input);

    /// Tries to compile a shorthand URLPattern string with an optional base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings, or `null` for no base URL
    /// @return the compiled URL pattern, or `null` when compilation fails
    @Nullable WebURLPattern tryCompile(String input, @Nullable String baseURL);

    /// Tries to compile a component URLPattern builder.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern, or `null` when compilation fails
    @Nullable WebURLPattern tryCompile(WebURLPattern.Builder builder);
}
