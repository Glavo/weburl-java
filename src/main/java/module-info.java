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
/// # Overview
///
/// The `org.glavo.url` module provides a modern, zero-dependency URL library that implements the
/// [WHATWG URL Living Standard](https://url.spec.whatwg.org/) — the same specification followed
/// by Chrome, Firefox, and Safari. It is designed as a drop-in improvement over
/// `java.net.URI` and `java.net.URL` for applications that need URL handling aligned with the
/// web platform.
///
/// # Module Contents
///
/// The module exports [org.glavo.url] for URL values and parsing, and
/// [org.glavo.url.pattern] for URLPattern matching.
///
/// It contains these primary public types:
///
/// | Type | Role |
/// |------|------|
/// | [org.glavo.url.WebURL] | Immutable, normalized, absolute URL value |
/// | [org.glavo.url.WebURLParser] | Reusable parser with configurable validation policy |
/// | [org.glavo.url.WebURLParseException] | Unchecked exception carrying structured parse-error details |
/// | [org.glavo.url.pattern.WebURLPattern] | Immutable WHATWG URLPattern matcher interface |
/// | [org.glavo.url.pattern.WebURLPatternResult] | Result for a successful URLPattern match |
/// | [org.glavo.url.pattern.WebURLPatternComponentResult] | Result for one matched URLPattern component |
/// | [org.glavo.url.pattern.WebURLPatternParser] | Reusable URLPattern compiler with configurable compilation policy |
/// | [org.glavo.url.pattern.WebURLPatternSyntaxException] | Unchecked exception for URLPattern compilation failures |
///
/// # Dependencies
///
/// This module requires **only `java.base`** at runtime.
/// [JetBrains Annotations](https://github.com/JetBrains/java-annotations) (`org.jetbrains.annotations`)
/// are used for compile-time nullability checking and are declared as a static (`compileOnly`)
/// dependency; they are not required on the module path at runtime.
///
/// # Thread Safety
///
/// [org.glavo.url.WebURL] instances are deeply immutable and safe for concurrent use without
/// additional synchronization. The built-in parsers exposed by
/// [org.glavo.url.WebURLParser#getDefault()] and
/// [org.glavo.url.WebURLParser#getStrict()] are likewise immutable and thread-safe. URLPattern
/// matchers and parsers are also immutable and reusable.
///
/// @see <a href="https://url.spec.whatwg.org/">WHATWG URL Living Standard</a>
/// @see org.glavo.url
module org.glavo.url {
    requires static org.jetbrains.annotations;

    exports org.glavo.url;
    exports org.glavo.url.pattern;
}
