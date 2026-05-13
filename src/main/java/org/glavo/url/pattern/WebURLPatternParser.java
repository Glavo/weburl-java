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
/// User-written regular-expression elements are controlled by [RegExpPolicy].
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
    /// This parser compiles patterns for case-sensitive matching and uses [RegExpPolicy#SUPPORTED]
    /// for user-written regular-expression elements.
    ///
    /// @return the default parser
    @Contract(pure = true)
    static WebURLPatternParser getDefault() {
        return WebURLPatternParserImpl.DEFAULT;
    }

    /// URLPattern regular-expression element handling policy.
    ///
    /// This policy controls only user-written `( ... )` regular-expression elements in URLPattern
    /// component patterns. It does not disable the internal matching expressions used to implement
    /// wildcards and named segment groups.
    enum RegExpPolicy {
        /// Accepts the currently implemented standard-compatible JavaScript regular-expression subset.
        ///
        /// This policy currently accepts:
        ///
        /// - literal characters allowed by URLPattern regular-expression tokenization;
        /// - the `.` wildcard;
        /// - top-level `|` alternatives;
        /// - character classes such as `[abc]`, `[^abc]`, and `[a-z]`;
        /// - finite ASCII class-set expressions using nested positive classes, ranges, `\d`, `\w`, union,
        ///   intersection `&&`, and subtraction `--`;
        /// - character-class escapes `\d`, `\D`, `\w`, and `\W`;
        /// - control escapes `\n`, `\r`, `\t`, and `\f`;
        /// - syntax escapes for regular-expression syntax characters, with `\-` also accepted inside character classes;
        /// - quantifiers `*`, `+`, `?`, `{m}`, `{m,}`, and `{m,n}`, including lazy forms such as `*?`;
        /// - non-capturing groups `(?:...)`;
        /// - named capture groups `(?<name>...)`, compiled as non-capturing groups because URLPattern does
        ///   not expose inner regular-expression groups.
        ///
        /// Numbered capture groups, lookahead, and lookbehind are rejected. Anchors, backreferences, Unicode
        /// and property escapes, word-boundary escapes, possessive quantifiers, complemented class-set
        /// operands such as `\D` and `\W`, and non-ASCII class-set operands are also rejected.
        ///
        /// Unsupported syntax is rejected during compilation. This is the default policy.
        SUPPORTED,

        /// Rejects every user-written regular-expression element during compilation.
        REJECT,

        /// Compiles user-written regular-expression elements with `java.util.regex.Pattern`.
        ///
        /// This policy is not WHATWG-compatible and must be requested explicitly.
        JAVA
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

    /// Returns a parser with the specified regular-expression element policy.
    ///
    /// If this parser already uses the requested policy, this method may return this parser.
    ///
    /// @param policy regular-expression element policy
    /// @return a parser that uses the specified regular-expression element policy
    @Contract(pure = true)
    WebURLPatternParser withRegExpPolicy(RegExpPolicy policy);

    /// Returns whether patterns compiled by this parser use case-insensitive matching.
    ///
    /// @return `true` when this parser compiles case-insensitive patterns
    @Contract(pure = true)
    boolean isIgnoreCase();

    /// Returns how this parser handles user-written regular-expression elements.
    ///
    /// @return the regular-expression element policy
    @Contract(pure = true)
    RegExpPolicy getRegExpPolicy();

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
