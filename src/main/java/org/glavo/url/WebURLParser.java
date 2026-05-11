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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/// A reusable parser for creating [WebURL] values from URL strings.
///
/// `WebURLParser` encapsulates a parsing policy that controls which URL Standard validation errors cause
/// parsing to fail. It exists so callers can choose between lenient and strict validation without changing
/// the core URL processing logic.
///
/// # Parsing Policies
///
/// Two built-in parsers are available:
///
/// - **[#getDefault()]** — Accepts recoverable validation errors and returns the normalized URL.
///   This is the parser used by all static convenience methods on [WebURL] such as
///   [WebURL#parse(String)]. It mirrors browser behavior: browsers do not reject a URL because
///   it contains a recoverable warning.
/// - **[#getStrict()]** — Treats every recoverable validation error as a parse failure, throwing
///   [WebURLParseException]. Non-recoverable errors (such as a missing host or an invalid domain
///   code point) are always rejected regardless of the parser.
///
/// # Validation Error Model
///
/// The URL Standard defines validation errors that are either:
///
/// - **Non-recoverable** ([WebURLParseException.ErrorType#isRecoverable()] returns `false`) — the
///   standard requires the URL parser to fail. These are rejected by every parser.
/// - **Recoverable** ([WebURLParseException.ErrorType#isRecoverable()] returns `true`) — the
///   standard continues processing and records the error as informational. The default parser
///   accepts these; the strict parser rejects them.
///
/// Recoverable errors include warnings like `invalid-URL-unit`, `invalid-reverse-solidus`,
/// `IPv4-non-decimal-part`, and `invalid-credentials`. They represent input that is unusual or
/// deprecated but still leads to a well-defined normalized URL.
///
/// # Thread Safety and Reuse
///
/// The [WebURLParser] instances returned by the factory methods are immutable, thread-safe, and reusable.
///
/// # Usage
///
/// ```java
/// WebURLParser parser = WebURLParser.getDefault();
/// WebURL url = parser.parse("https://example.com/path");
///
/// // Switch to strict validation for security-sensitive contexts
/// WebURLParser strict = WebURLParser.getStrict();
/// try {
///     WebURL safe = strict.parse(untrustedInput);
/// } catch (WebURLParseException ex) {
///     // handle the specific validation error
/// }
/// ```
///
/// @see WebURL
/// @see WebURLParseException
/// @see WebURLParseException.ErrorType
/// @since 0.2.0
@NotNullByDefault
public sealed interface WebURLParser permits WebURLParserImpl {
    /// Returns the default parser.
    ///
    /// This parser ignores non-fatal validation errors and continues parsing according to the URL Standard.
    @Contract(pure = true)
    static WebURLParser getDefault() {
        return WebURLParserImpl.DEFAULT;
    }

    /// Returns the strict parser.
    ///
    /// This parser treats non-fatal validation errors as parse failures and throws `WebURLParseException`.
    @Contract(pure = true)
    static WebURLParser getStrict() {
        return WebURLParserImpl.STRICT;
    }

    /// Returns the recoverable validation errors that this parser treats as parse failures.
    ///
    /// The returned set contains configurable validation errors only. Error types for which
    /// `WebURLParseException.ErrorType.isRecoverable()` returns `false` are parse failures and are rejected by every
    /// parser regardless of whether they appear in this set.
    ///
    /// @return an immutable set of recoverable validation errors rejected by this parser
    @Contract(pure = true)
    @Unmodifiable
    Set<WebURLParseException.ErrorType> getRejectedValidationErrors();

    /// Parses an absolute input string and returns the parsed URL.
    ///
    /// The input must be an absolute URL. Relative inputs fail; use a base-aware overload when relative URL
    /// references should be accepted.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input is not accepted by this parser
    @Contract(pure = true)
    WebURL parse(String input);

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The base string must be a valid absolute URL. The input may be either absolute or relative to that base.
    ///
    /// @param input the URL input string
    /// @param base  the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input is not accepted by this parser
    @Contract(pure = true)
    WebURL parse(String input, String base);

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The input may be either absolute or relative to the supplied base URL.
    ///
    /// @param input the URL input string
    /// @param base  the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when the input cannot be resolved against the base URL
    @Contract(pure = true)
    WebURL parse(String input, WebURL base);

    /// Parses an absolute input string and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parse(String)], except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if the input is not accepted by this parser
    @Contract(pure = true)
    @Nullable WebURL tryParse(String input);

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parse(String, String)], except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base  the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    @Contract(pure = true)
    @Nullable WebURL tryParse(String input, String base);

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parse(String, WebURL)], except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base  the base URL
    /// @return the parsed URL, or `null` if the input cannot be parsed against the base
    @Contract(pure = true)
    @Nullable WebURL tryParse(String input, WebURL base);

    /// Parses a user-entered browser-style URL input and returns the parsed URL.
    ///
    /// This method has the same user-input contract as `WebURL.parseBrowserInput(String)`, using this parser's
    /// validation policy after browser-style input preprocessing.
    ///
    /// @param input the browser-style URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input is not accepted by this parser
    @Contract(pure = true)
    WebURL parseBrowserInput(String input);

    /// Parses a user-entered browser-style URL input and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as [#parseBrowserInput(String)], except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the browser-style URL input string
    /// @return the parsed URL, or `null` if the input is not accepted by this parser
    @Contract(pure = true)
    @Nullable WebURL tryParseBrowserInput(String input);
}
