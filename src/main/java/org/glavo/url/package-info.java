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

/// Browser-grade WHATWG URL parsing, normalization, and serialization for Java.
///
/// This package provides a modern URL library that implements the
/// [WHATWG URL Standard](https://url.spec.whatwg.org/) — the same standard used by modern browsers.
/// It is a lightweight, zero-dependency alternative to {@code java.net.URI} and {@code java.net.URL}
/// for applications that need URL handling aligned with the web platform.
///
/// # Main Types
///
/// | Type                    | Role                                                  |
/// |-------------------------|-------------------------------------------------------|
/// | [org.glavo.url.WebURL]          | Immutable, absolute URL value; resolved and normalized |
/// | [org.glavo.url.WebURLParser]    | Reusable parser with configurable validation policy    |
/// | [org.glavo.url.WebURLParseException] | Unchecked exception with structured error details |
///
/// # Quick Start
///
/// ```java
/// // Absolute URL
/// WebURL url = WebURL.parse("https://example.com/path?q=1#frag");
///
/// // Resolve a relative reference against a base
/// WebURL resolved = WebURL.parse("api/v2", "https://example.com/");
///
/// // Null-returning variant for untrusted input
/// WebURL maybe = WebURL.tryParse(userInput);
///
/// // Browser-address-bar-style input
/// WebURL addr = WebURL.parseBrowserInput("example.com:8080/path");
/// // addr.href() == "http://example.com:8080/path"
///
/// ```
///
/// # Comparison with java.net
///
/// {@link org.glavo.url.WebURL} differs from {@code java.net.URI} and {@code java.net.URL} in several fundamental ways:
///
/// - **WHATWG Standard, not RFC 2396.** WebURL follows the URL Standard implemented by browsers.
///   It handles spaces, backslashes, tabs, newlines, non-ASCII hosts, and many other real-world URL
///   patterns that the Java standard library rejects or mishandles.
/// - **IDNA UTS #46.** Domain hosts are processed with Unicode IDNA Compatibility Processing.
///   Internationalized domain names are automatically normalized to their Punycode form.
///   Java's prior IDN support is based on the obsolete IDNA 2003 specification.
/// - **Browser-like input handling.** [org.glavo.url.WebURL#parseBrowserInput(String)] applies address-bar
///   heuristics to user-typed text, auto-detecting the scheme and resolving bare domain names.
/// - **Safe equality.** {@code WebURL.equals()} and {@code hashCode()} are purely structural — they never
///   perform DNS resolution or any other network I/O. {@code WebURL} objects are safe as {@code Map} keys.
/// - **All schemes supported.** No scheme-specific handlers are required. Any scheme the URL Standard
///   accepts — including non-standard schemes — can be parsed and examined.
/// - **No runtime dependencies.** Only the {@code java.base} module is required.
///
/// # Thread Safety
///
/// {@link org.glavo.url.WebURL} instances are deeply immutable and safe for concurrent use. The default and strict
/// parsers from {@link org.glavo.url.WebURLParser} are also immutable and thread-safe.
///
/// @see <a href="https://url.spec.whatwg.org/">WHATWG URL Living Standard</a>
/// @see org.glavo.url.WebURL
/// @see org.glavo.url.WebURLParser
/// @see org.glavo.url.WebURLParseException
/// @see org.glavo.url.pattern.WebURLPattern
@NotNullByDefault
package org.glavo.url;

import org.jetbrains.annotations.NotNullByDefault;
