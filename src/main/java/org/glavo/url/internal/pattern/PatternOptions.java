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
package org.glavo.url.internal.pattern;

import org.glavo.url.pattern.WebURLPatternParser;
import org.jetbrains.annotations.NotNullByDefault;

/// Component compilation options from the URLPattern Standard.
@NotNullByDefault
record PatternOptions(
        String delimiter,
        String prefix,
        boolean ignoreCase,
        WebURLPatternParser.RegExpPolicy regExpPolicy
) {
    /// Default component options.
    static final PatternOptions DEFAULT =
            new PatternOptions("", "", false, WebURLPatternParser.RegExpPolicy.SUPPORTED);

    /// Hostname component options.
    static final PatternOptions HOSTNAME =
            new PatternOptions(".", "", false, WebURLPatternParser.RegExpPolicy.SUPPORTED);

    /// Pathname component options.
    static final PatternOptions PATHNAME =
            new PatternOptions("/", "/", false, WebURLPatternParser.RegExpPolicy.SUPPORTED);

    /// Returns a copy with a different ignore-case flag.
    PatternOptions withIgnoreCase(boolean value) {
        return new PatternOptions(delimiter, prefix, value, regExpPolicy);
    }

    /// Returns a copy with a different regular-expression element policy.
    PatternOptions withRegExpPolicy(WebURLPatternParser.RegExpPolicy value) {
        return new PatternOptions(delimiter, prefix, ignoreCase, value);
    }
}
