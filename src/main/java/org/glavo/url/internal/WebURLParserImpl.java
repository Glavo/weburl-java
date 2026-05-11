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
import org.glavo.url.WebURLParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Implementation object behind the public `WebURLParser` API.
@NotNullByDefault
public final class WebURLParserImpl implements WebURLParser {
    /// The default parser implementation.
    private static final WebURLParser DEFAULT = new WebURLParserImpl(false);

    /// The strict parser implementation.
    private static final WebURLParser STRICT = new WebURLParserImpl(true);

    /// Whether non-fatal validation errors are parse failures.
    private final boolean strictValidation;

    /// Creates a parser implementation.
    private WebURLParserImpl(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }

    /// Returns the default parser implementation.
    public static WebURLParser defaultParser() {
        return DEFAULT;
    }

    /// Returns the strict parser implementation.
    public static WebURLParser strictParser() {
        return STRICT;
    }

    /// Returns whether non-fatal validation errors are parse failures.
    @Override
    public boolean isStrictValidationEnabled() {
        return strictValidation;
    }

    /// Parses an absolute input string and returns the parsed URL.
    @Override
    public WebURL parse(String input) {
        return WebURLParsing.parse(input, strictValidation);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    @Override
    public WebURL parse(String input, String base) {
        return WebURLParsing.parse(input, base, strictValidation);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    @Override
    public WebURL parse(String input, WebURL base) {
        return WebURLParsing.parse(input, base, strictValidation);
    }

    /// Parses an absolute input string and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParse(String input) {
        return WebURLParsing.tryParse(input, strictValidation);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParse(String input, String base) {
        return WebURLParsing.tryParse(input, base, strictValidation);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParse(String input, WebURL base) {
        return WebURLParsing.tryParse(input, base, strictValidation);
    }

    /// Parses a browser-style URL input and returns the parsed URL.
    @Override
    public WebURL parseBrowserInput(String input) {
        return WebURLParsing.parseBrowserInput(input, strictValidation);
    }

    /// Parses a browser-style URL input and returns `null` on failure.
    @Override
    public @Nullable WebURL tryParseBrowserInput(String input) {
        return WebURLParsing.tryParseBrowserInput(input, strictValidation);
    }
}
