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

import org.glavo.url.internal.WebURLPatternResultImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;

/// Result for a successful URLPattern match.
///
/// Each method returns the match result for one URLPattern component. Component results use
/// Java-style [java.util.regex.MatchResult] group indexing and additionally expose URLPattern
/// groups object semantics through [WebURLPatternComponentResult#getWebGroups()].
///
/// @since 0.3.0
@NotNullByDefault
public sealed interface WebURLPatternResult permits WebURLPatternResultImpl {
    /// Returns the protocol component result.
    ///
    /// @return the protocol component result
    @Contract(pure = true)
    WebURLPatternComponentResult protocol();

    /// Returns the username component result.
    ///
    /// @return the username component result
    @Contract(pure = true)
    WebURLPatternComponentResult username();

    /// Returns the password component result.
    ///
    /// @return the password component result
    @Contract(pure = true)
    WebURLPatternComponentResult password();

    /// Returns the hostname component result.
    ///
    /// @return the hostname component result
    @Contract(pure = true)
    WebURLPatternComponentResult hostname();

    /// Returns the port component result.
    ///
    /// @return the port component result
    @Contract(pure = true)
    WebURLPatternComponentResult port();

    /// Returns the pathname component result.
    ///
    /// @return the pathname component result
    @Contract(pure = true)
    WebURLPatternComponentResult pathname();

    /// Returns the search component result.
    ///
    /// @return the search component result
    @Contract(pure = true)
    WebURLPatternComponentResult search();

    /// Returns the hash component result.
    ///
    /// @return the hash component result
    @Contract(pure = true)
    WebURLPatternComponentResult hash();
}
