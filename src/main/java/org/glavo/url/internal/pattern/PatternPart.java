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

import org.jetbrains.annotations.NotNullByDefault;

/// One parsed URLPattern component part.
@NotNullByDefault
record PatternPart(
        Type type,
        String value,
        Modifier modifier,
        String name,
        String prefix,
        String suffix
) {
    /// Creates a fixed-text part.
    static PatternPart fixed(String value, Modifier modifier) {
        return new PatternPart(Type.FIXED_TEXT, value, modifier, "", "", "");
    }

    /// Creates a matching part.
    static PatternPart matching(
            Type type,
            String value,
            Modifier modifier,
            String name,
            String prefix,
            String suffix
    ) {
        return new PatternPart(type, value, modifier, name, prefix, suffix);
    }

    /// Returns whether this part is a custom regular-expression part.
    boolean isRegExp() {
        return type == Type.REGEXP;
    }

    /// URLPattern part kinds.
    enum Type {
        /// A literal text part.
        FIXED_TEXT,
        /// A custom regular-expression part.
        REGEXP,
        /// A named segment wildcard part.
        SEGMENT_WILDCARD,
        /// A full wildcard part.
        FULL_WILDCARD
    }

    /// URLPattern part modifiers.
    enum Modifier {
        /// No modifier.
        NONE,
        /// The `?` modifier.
        OPTIONAL,
        /// The `*` modifier.
        ZERO_OR_MORE,
        /// The `+` modifier.
        ONE_OR_MORE
    }
}
