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

/// WHATWG URLPattern-style matching for normalized web URLs.
///
/// This package contains the URLPattern API, separate from the core URL value and parser types in
/// [org.glavo.url]. A [WebURLPattern] is an immutable, precompiled matcher that can be constructed
/// from shorthand URLPattern strings or from component patterns. [WebURLPatternParser] provides
/// reusable compilation policy, including case-insensitive matching. Builder setters and component
/// pattern getters use names such as `setSchemePattern()` and `getSchemePattern()` to distinguish
/// pattern strings from parsed URL component values.
///
/// # Quick Start
///
/// ```java
/// WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
///         .setSchemePattern("https")
///         .setHostPattern("example.com")
///         .setPathPattern("/users/:id"));
/// pattern.test("https://example.com/users/42"); // true
///
/// WebURLPattern ignoreCasePattern = WebURLPatternParser.getDefault().withIgnoreCase()
///         .compile("https://example.com/users/:id");
/// ignoreCasePattern.test("https://example.com/Users/42"); // true
/// ```
///
/// @see org.glavo.url.pattern.WebURLPattern
/// @see org.glavo.url.pattern.WebURLPatternResult
/// @see org.glavo.url.pattern.WebURLPatternComponentResult
/// @see org.glavo.url.pattern.WebURLPatternParser
/// @see org.glavo.url.pattern.WebURLPatternSyntaxException
@NotNullByDefault
package org.glavo.url.pattern;

import org.jetbrains.annotations.NotNullByDefault;
