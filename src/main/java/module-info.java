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

/// A modern, zero-dependency Java URL library that implements the
/// [WHATWG URL Standard](https://url.spec.whatwg.org/).
///
/// # Overview
///
/// This module provides URL parsing, normalization, and serialization through the {@code org.glavo.url}
/// package. It follows the WHATWG URL Standard implemented by modern browsers, making it suitable for
/// applications that need URL handling aligned with the web platform.
///
/// # Exported Packages
///
/// | Package           | Contents                                                   |
/// |-------------------|------------------------------------------------------------|
/// | {@code org.glavo.url} | Core API: {@code WebURL}, {@code WebURLParser}, {@code WebURLParseException} |
///
/// Internal implementation classes in {@code org.glavo.url.internal} are not exported.
///
/// # Module Dependencies
///
/// This module requires only {@code java.base} at runtime.
///
/// # Design Principles
///
/// - **WHATWG URL processing:** The module is tested against web-platform-tests URL and IDNA data covering
///   thousands of real-world URL inputs.
/// - **IDNA UTS #46:** Internationalized domain names are processed with Unicode 17.0.0 IDNA mapping tables,
///   including Punycode encoding and decoding.
/// - **Zero runtime dependencies:** Beyond {@code java.base}, no other module or library is required.
/// - **Immutable value types:** {@code WebURL} is deeply immutable and thread-safe. Parsers are also
///   immutable and thread-safe.
/// - **Safe equality:** No network I/O is ever performed by {@code equals()} or {@code hashCode()}.
/// - **Interoperable:** Seamless conversion to and from {@code java.net.URI}, {@code java.net.URL}, and
///   {@code java.nio.file.Path}.
///
/// # Usage
///
/// Add this module to your module path and declare a dependency:
///
/// ```java
/// module my.app {
///     requires org.glavo.url;
/// }
/// ```
///
/// Then use the public API:
///
/// ```java
/// import org.glavo.url.WebURL;
///
/// WebURL url = WebURL.parse("https://example.com/path");
/// String host = url.getHost();   // "example.com"
/// String href = url.href();      // "https://example.com/path"
/// ```
///
/// @see <a href="https://url.spec.whatwg.org/">WHATWG URL Living Standard</a>
/// @see <a href="https://github.com/Glavo/weburl-java">Project repository</a>
module org.glavo.url {
    requires static org.jetbrains.annotations;

    exports org.glavo.url;
}
