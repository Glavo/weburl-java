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
import org.glavo.url.WebURLParseException;
import org.glavo.url.WebURLParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// Implementation object behind the public `WebURLParser` API.
@NotNullByDefault
public final class WebURLParserImpl implements WebURLParser {
    /// The validation errors accepted by the default parser.
    private static final @Unmodifiable Set<WebURLParseException.ErrorType> DEFAULT_REJECTED_VALIDATION_ERRORS = Set.of();

    /// The validation errors rejected by the strict parser.
    private static final @Unmodifiable Set<WebURLParseException.ErrorType> STRICT_REJECTED_VALIDATION_ERRORS =
            Stream.of(WebURLParseException.ErrorType.values())
                    .filter(WebURLParseException.ErrorType::isRecoverable)
                    .collect(Collectors.toUnmodifiableSet());

    /// The default parser implementation.
    public static final WebURLParser DEFAULT = new WebURLParserImpl(DEFAULT_REJECTED_VALIDATION_ERRORS);

    /// The strict parser implementation.
    public static final WebURLParser STRICT = new WebURLParserImpl(STRICT_REJECTED_VALIDATION_ERRORS);

    /// Non-fatal validation errors that should be treated as parse failures.
    private final @Unmodifiable Set<WebURLParseException.ErrorType> rejectedValidationErrors;

    /// Creates a parser implementation.
    private WebURLParserImpl(@Unmodifiable Set<WebURLParseException.ErrorType> rejectedValidationErrors) {
        this.rejectedValidationErrors = rejectedValidationErrors;
    }

    /// Returns the recoverable validation errors rejected by this parser.
    @Override
    public @Unmodifiable Set<WebURLParseException.ErrorType> getRejectedValidationErrors() {
        return rejectedValidationErrors;
    }

    /// Parses an absolute input string and returns the parsed URL.
    @Override
    public WebURL parse(String input) {
        return WebURLParsing.parse(input, rejectedValidationErrors);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    @Override
    public WebURL parse(String input, String base) {
        return WebURLParsing.parse(input, base, rejectedValidationErrors);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    @Override
    public WebURL parse(String input, WebURL base) {
        return WebURLParsing.parse(input, base, rejectedValidationErrors);
    }

    /// Parses an absolute input string and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParse(String input) {
        return WebURLParsing.tryParse(input, rejectedValidationErrors);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParse(String input, String base) {
        return WebURLParsing.tryParse(input, base, rejectedValidationErrors);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParse(String input, WebURL base) {
        return WebURLParsing.tryParse(input, base, rejectedValidationErrors);
    }

    /// Parses a browser-style URL input and returns the parsed URL.
    @Override
    public WebURL parseBrowserInput(String input) {
        return WebURLParsing.parseBrowserInput(input, rejectedValidationErrors);
    }

    /// Parses a browser-style URL input and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParseBrowserInput(String input) {
        return WebURLParsing.tryParseBrowserInput(input, rejectedValidationErrors);
    }
}
