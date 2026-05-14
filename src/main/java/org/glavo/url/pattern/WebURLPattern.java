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

import org.glavo.url.WebURL;
import org.glavo.url.internal.WebURLPatternBuilderImpl;
import org.glavo.url.internal.WebURLPatternComponentResultImpl;
import org.glavo.url.internal.WebURLPatternImpl;
import org.glavo.url.internal.WebURLPatternResultImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.regex.MatchResult;

/// An immutable, precompiled matcher for WHATWG URL Pattern syntax.
///
/// `WebURLPattern` follows the core URLPattern model from the
/// [WHATWG URL Pattern Standard](https://urlpattern.spec.whatwg.org/). It can be created from
/// a shorthand pattern string or from component patterns via [Builder#build()], and it can match
/// URL strings, [WebURL] values, or component inputs.
///
/// Builder setters and component pattern getters such as [Builder#setSchemePattern(String)] and
/// [#getSchemePattern()] use the `Pattern` suffix to avoid confusing component pattern strings with
/// parsed URL component values.
///
/// User-written regular-expression elements are handled by [WebURLPatternParser.RegExpPolicy].
/// The default parser accepts the currently supported standard-compatible subset and rejects
/// unsupported syntax during compilation.
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
        return WebURLPatternParser.getDefault().compile(input);
    }

    /// Compiles a shorthand URLPattern string with a base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings, or `null` for no base URL
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the pattern or base URL cannot be compiled
    @Contract("_, _ -> new")
    static WebURLPattern compile(String input, @Nullable String baseURL) {
        return WebURLPatternParser.getDefault().compile(input, baseURL);
    }

    /// Compiles a component URLPattern builder.
    ///
    /// This convenience method uses [WebURLPatternParser#getDefault()]. For component construction,
    /// prefer [#newBuilder()] followed by [Builder#build()], or [WebURLPatternParser#newBuilder()]
    /// when a custom parser policy is needed.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern
    /// @throws WebURLPatternSyntaxException when the components cannot be compiled
    @Contract("_ -> new")
    static WebURLPattern compile(Builder builder) {
        return WebURLPatternParser.getDefault().compile(builder);
    }

    /// Tries to compile a shorthand URLPattern string.
    ///
    /// @param input the shorthand pattern string
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(String input) {
        return WebURLPatternParser.getDefault().tryCompile(input);
    }

    /// Tries to compile a shorthand URLPattern string with a base URL.
    ///
    /// @param input the shorthand pattern string
    /// @param baseURL the base URL used for relative pattern strings, or `null` for no base URL
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(String input, @Nullable String baseURL) {
        return WebURLPatternParser.getDefault().tryCompile(input, baseURL);
    }

    /// Tries to compile a component URLPattern builder.
    ///
    /// This convenience method uses [WebURLPatternParser#getDefault()]. For component construction,
    /// prefer [#newBuilder()] followed by [Builder#build()], or [WebURLPatternParser#newBuilder()]
    /// when a custom parser policy is needed.
    ///
    /// @param builder the component pattern builder
    /// @return the compiled URL pattern, or `null` when compilation fails
    static @Nullable WebURLPattern tryCompile(Builder builder) {
        return WebURLPatternParser.getDefault().tryCompile(builder);
    }

    /// Creates a new mutable URLPattern component builder.
    ///
    /// @return a new builder
    @Contract("-> new")
    static Builder newBuilder() {
        return WebURLPatternParser.getDefault().newBuilder();
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

    /// Matches this pattern against a URL string and returns the match result.
    ///
    /// @param input the URL input string
    /// @return the match result, or `null` when the input does not match or is not a valid URL
    @Contract(pure = true)
    @Nullable Result match(String input);

    /// Matches this pattern against a URL string with a base URL and returns the match result.
    ///
    /// @param input the URL input string
    /// @param baseURL the base URL used for relative input
    /// @return the match result, or `null` when the input does not match or cannot be parsed
    @Contract(pure = true)
    @Nullable Result match(String input, String baseURL);

    /// Matches this pattern against a parsed URL and returns the match result.
    ///
    /// @param input the URL input value
    /// @return the match result, or `null` when the input does not match
    @Contract(pure = true)
    @Nullable Result match(WebURL input);

    /// Matches this pattern against component input and returns the match result.
    ///
    /// @param input the component input builder
    /// @return the match result, or `null` when the input does not match or cannot be canonicalized
    @Contract(pure = true)
    @Nullable Result match(Builder input);

    /// Returns the scheme component pattern string without a trailing colon.
    @Contract(pure = true)
    String getSchemePattern();

    /// Returns the username pattern string.
    @Contract(pure = true)
    String getUsernamePattern();

    /// Returns the password pattern string.
    @Contract(pure = true)
    String getPasswordPattern();

    /// Returns the host component pattern string.
    @Contract(pure = true)
    String getHostPattern();

    /// Returns the port pattern string.
    @Contract(pure = true)
    String getPortPattern();

    /// Returns the path component pattern string.
    @Contract(pure = true)
    String getPathPattern();

    /// Returns the query component pattern string without a leading question mark.
    @Contract(pure = true)
    String getQueryPattern();

    /// Returns the fragment component pattern string without a leading number sign.
    @Contract(pure = true)
    String getFragmentPattern();

    /// Returns whether this pattern was compiled with case-insensitive matching.
    @Contract(pure = true)
    boolean isIgnoreCase();

    /// Returns whether this pattern preserves URLPattern standard-compatible regular-expression semantics.
    ///
    /// This method returns `false` when the pattern contains a user-written regular-expression element and was
    /// compiled with [WebURLPatternParser.RegExpPolicy#JAVA], because Java regular-expression syntax and
    /// semantics are not equivalent to ECMAScript regular expressions used by the URLPattern standard.
    ///
    /// @return `true` when this pattern is standard-compatible
    @Contract(pure = true)
    boolean isStandardCompatible();

    /// Returns whether any component contains a user-written regular-expression element.
    ///
    /// @return `true` when any component contains a user-written regular-expression element
    @Contract(pure = true)
    boolean hasRegExpGroups();

    /// Mutable builder for URLPattern component pattern strings or component match input strings.
    ///
    /// The builder uses `*Pattern` names because the primary construction use case stores URLPattern
    /// component pattern strings. Scheme maps to URLPattern `protocol`, host maps to URLPattern
    /// `hostname`, path maps to URLPattern `pathname`, query maps to URLPattern `search`, and fragment
    /// maps to URLPattern `hash`.
    @NotNullByDefault
    sealed interface Builder permits WebURLPatternBuilderImpl {
        /// Sets the scheme component pattern string.
        ///
        /// @param schemePattern the scheme component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setSchemePattern(@Nullable String schemePattern);

        /// Sets the username component pattern string.
        ///
        /// @param usernamePattern the username component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setUsernamePattern(@Nullable String usernamePattern);

        /// Sets the password component pattern string.
        ///
        /// @param passwordPattern the password component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setPasswordPattern(@Nullable String passwordPattern);

        /// Sets the host component pattern string.
        ///
        /// @param hostPattern the host component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setHostPattern(@Nullable String hostPattern);

        /// Sets the port component pattern string.
        ///
        /// @param portPattern the port component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setPortPattern(@Nullable String portPattern);

        /// Sets the path component pattern string.
        ///
        /// @param pathPattern the path component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setPathPattern(@Nullable String pathPattern);

        /// Sets the query component pattern string.
        ///
        /// @param queryPattern the query component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setQueryPattern(@Nullable String queryPattern);

        /// Sets the fragment component pattern string.
        ///
        /// @param fragmentPattern the fragment component pattern string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setFragmentPattern(@Nullable String fragmentPattern);

        /// Sets the base URL.
        ///
        /// @param baseURL the base URL string, or `null` when absent
        /// @return this builder
        @Contract("_ -> this")
        Builder setBaseURL(@Nullable String baseURL);

        /// Builds an immutable URLPattern using the parser that created this builder.
        ///
        /// Builders returned by [WebURLPattern#newBuilder()] use [WebURLPatternParser#getDefault()].
        /// Builders returned by [WebURLPatternParser#newBuilder()] use that parser's matching and
        /// regular-expression policy.
        ///
        /// @return the compiled URL pattern
        /// @throws WebURLPatternSyntaxException when the components cannot be compiled
        @Contract("-> new")
        WebURLPattern build();
    }

    /// Result for one matched URLPattern component.
    ///
    /// This interface extends [MatchResult] with Java-style capture group semantics and also exposes
    /// URLPattern groups object semantics through [#getWebGroups()] and [#getWebGroup(String)].
    @NotNullByDefault
    sealed interface ComponentResult extends MatchResult permits WebURLPatternComponentResultImpl {
        /// Returns the start index of the whole component match.
        ///
        /// The index is relative to the component input represented by this result.
        ///
        /// @return the start index of the whole component match
        @Override
        @Contract(pure = true)
        int start();

        /// Returns the start index of a Java-style capture group.
        ///
        /// Group `0` is the whole component match. Groups `1` through [#groupCount()] are URLPattern
        /// capture groups in matching order. If the requested group exists but did not match, this
        /// method returns `-1`.
        ///
        /// @param group the Java-style capture group index
        /// @return the start index of the group, or `-1` when the group did not match
        /// @throws IndexOutOfBoundsException when `group` is negative or greater than [#groupCount()]
        @Override
        @Contract(pure = true)
        int start(int group);

        /// Returns the end index of the whole component match.
        ///
        /// The index is relative to the component input represented by this result.
        ///
        /// @return the end index of the whole component match
        @Override
        @Contract(pure = true)
        int end();

        /// Returns the end index of a Java-style capture group.
        ///
        /// Group `0` is the whole component match. Groups `1` through [#groupCount()] are URLPattern
        /// capture groups in matching order. If the requested group exists but did not match, this
        /// method returns `-1`.
        ///
        /// @param group the Java-style capture group index
        /// @return the end index of the group, or `-1` when the group did not match
        /// @throws IndexOutOfBoundsException when `group` is negative or greater than [#groupCount()]
        @Override
        @Contract(pure = true)
        int end(int group);

        /// Returns the whole component match.
        ///
        /// This is equivalent to `group(0)`.
        ///
        /// @return the whole component match
        @Override
        @Contract(pure = true)
        String group();

        /// Returns a Java-style capture group.
        ///
        /// Group `0` is the whole component match. Groups `1` through [#groupCount()] are URLPattern
        /// capture groups in matching order. If the requested group exists but did not match, this
        /// method returns `null`.
        ///
        /// @param group the Java-style capture group index
        /// @return the group value, or `null` when the group did not match
        /// @throws IndexOutOfBoundsException when `group` is negative or greater than [#groupCount()]
        @Override
        @Contract(pure = true)
        @Nullable String group(int group);

        /// Returns the number of Java-style capture groups.
        ///
        /// The returned value excludes group `0`, which is always the whole component match.
        ///
        /// @return the number of capture groups
        @Override
        @Contract(pure = true)
        int groupCount();

        /// Returns URLPattern named and numeric capture groups.
        ///
        /// Unmatched optional groups are represented by `null` values.
        ///
        /// This map follows URLPattern groups object semantics. Numeric keys such as `"0"` refer to
        /// anonymous URLPattern groups, while [#group(int)] follows `java.util.regex.MatchResult`
        /// semantics where group `0` is the whole component match.
        ///
        /// @return immutable URLPattern capture groups
        @Contract(pure = true)
        @Unmodifiable Map<String, @Nullable String> getWebGroups();

        /// Returns a named URLPattern capture group.
        ///
        /// This method returns `null` when the group is absent or when the group is present but did not match.
        ///
        /// @param name the group name
        /// @return the group value, or `null` when absent or unmatched
        @Contract(pure = true)
        @Nullable String getWebGroup(String name);

        /// Returns a numeric URLPattern capture group.
        ///
        /// URLPattern numeric groups are exposed with decimal string keys such as `"0"` and `"1"`.
        /// Unlike [#group(int)], index `0` means URLPattern's first anonymous capture group, not the
        /// entire component match.
        ///
        /// @param index the numeric URLPattern capture group index
        /// @return the group value, or `null` when absent or unmatched
        /// @throws IndexOutOfBoundsException when `index` is negative
        @Contract(pure = true)
        @Nullable String getWebGroup(int index);
    }

    /// Result for a successful URLPattern match.
    ///
    /// Each method returns the match result for one URLPattern component. Component results use
    /// Java-style [MatchResult] group indexing and additionally expose URLPattern groups object
    /// semantics through [ComponentResult#getWebGroups()].
    @NotNullByDefault
    sealed interface Result permits WebURLPatternResultImpl {
        /// Returns the scheme component result.
        ///
        /// @return the scheme component result
        @Contract(pure = true)
        ComponentResult getScheme();

        /// Returns the username component result.
        ///
        /// @return the username component result
        @Contract(pure = true)
        ComponentResult getUsername();

        /// Returns the password component result.
        ///
        /// @return the password component result
        @Contract(pure = true)
        ComponentResult getPassword();

        /// Returns the host component result.
        ///
        /// @return the host component result
        @Contract(pure = true)
        ComponentResult getHost();

        /// Returns the port component result.
        ///
        /// @return the port component result
        @Contract(pure = true)
        ComponentResult getPort();

        /// Returns the path component result.
        ///
        /// @return the path component result
        @Contract(pure = true)
        ComponentResult getPath();

        /// Returns the query component result.
        ///
        /// @return the query component result
        @Contract(pure = true)
        ComponentResult getQuery();

        /// Returns the fragment component result.
        ///
        /// @return the fragment component result
        @Contract(pure = true)
        ComponentResult getFragment();
    }
}
