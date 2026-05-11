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

import org.glavo.url.internal.WebURLParserImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/// A reusable parser for creating `WebURL` values from URL strings.
///
/// `WebURLParser` represents the parsing policy used by the URL parser. The default parser follows normal URL
/// Standard behavior: non-fatal validation errors are accepted, and the parser continues with the normalized URL.
/// The strict parser rejects all recoverable validation errors. Validation errors for which
/// `WebURLParseException.ErrorType.isRejectionRequired()` returns `true` are always rejected by every parser.
///
/// Implementations are immutable and thread-safe. Static convenience methods on `WebURL` use [#getDefault()].
///
/// @since 0.2.0
@NotNullByDefault
public sealed interface WebURLParser permits WebURLParserImpl {
    /// Returns the default parser.
    ///
    /// This parser ignores non-fatal validation errors and continues parsing according to the URL Standard.
    static WebURLParser getDefault() {
        return WebURLParserImpl.DEFAULT;
    }

    /// Returns the strict parser.
    ///
    /// This parser treats non-fatal validation errors as parse failures and throws `WebURLParseException`.
    static WebURLParser getStrict() {
        return WebURLParserImpl.STRICT;
    }

    /// Returns the recoverable validation errors that this parser treats as parse failures.
    ///
    /// The returned set contains configurable validation errors only. Error types for which
    /// `WebURLParseException.ErrorType.isRejectionRequired()` returns `true` are parse failures and are rejected by
    /// every parser regardless of whether they appear in this set.
    ///
    /// @return an immutable set of recoverable validation errors rejected by this parser
    @Unmodifiable Set<WebURLParseException.ErrorType> getRejectedValidationErrors();

    /// Parses an absolute input string and returns the parsed URL.
    ///
    /// The input must be an absolute URL. Relative inputs fail; use a base-aware overload when relative URL
    /// references should be accepted.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input is not accepted by this parser
    WebURL parse(String input);

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The base string must be a valid absolute URL. The input may be either absolute or relative to that base.
    ///
    /// @param input the URL input string
    /// @param base  the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input is not accepted by this parser
    WebURL parse(String input, String base);

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The input may be either absolute or relative to the supplied base URL.
    ///
    /// @param input the URL input string
    /// @param base  the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when the input cannot be resolved against the base URL
    WebURL parse(String input, WebURL base);

    /// Parses an absolute input string and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parse(String)], except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if the input is not accepted by this parser
    @Nullable WebURL tryParse(String input);

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parse(String, String)], except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base  the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    @Nullable WebURL tryParse(String input, String base);

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parse(String, WebURL)], except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base  the base URL
    /// @return the parsed URL, or `null` if the input cannot be parsed against the base
    @Nullable WebURL tryParse(String input, WebURL base);

    /// Parses a user-entered browser-style URL input and returns the parsed URL.
    ///
    /// This method has the same user-input contract as `WebURL.parseBrowserInput(String)`, using this parser's
    /// validation policy after browser-style input preprocessing.
    ///
    /// @param input the browser-style URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input is not accepted by this parser
    WebURL parseBrowserInput(String input);

    /// Parses a user-entered browser-style URL input and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parseBrowserInput(String)], except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the browser-style URL input string
    /// @return the parsed URL, or `null` if the input is not accepted by this parser
    @Nullable WebURL tryParseBrowserInput(String input);
}
