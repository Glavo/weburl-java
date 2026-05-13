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

import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/// A compiled URLPattern component matcher.
@NotNullByDefault
final class PatternComponent {
    /// Serialized component pattern string.
    private final String pattern;
    /// Java regular expression for complex components, or `null` for fast-path components.
    private final @Nullable Pattern regexp;
    /// Capture group names in regular-expression order.
    private final List<String> groupNames;
    /// Whether the component contains a custom regular-expression group.
    private final boolean hasRegExpGroups;
    /// Fast-path component type.
    private final Type type;
    /// Exact string for exact-match components.
    private final String exactMatchValue;
    /// Creates a compiled component.
    private PatternComponent(
            String pattern,
            @Nullable Pattern regexp,
            List<String> groupNames,
            boolean hasRegExpGroups,
            Type type,
            String exactMatchValue
    ) {
        this.pattern = pattern;
        this.regexp = regexp;
        this.groupNames = List.copyOf(groupNames);
        this.hasRegExpGroups = hasRegExpGroups;
        this.type = type;
        this.exactMatchValue = exactMatchValue;
    }

    /// Compiles a component pattern.
    static PatternComponent compile(String input, Function<String, String> encodingCallback, PatternOptions options) {
        List<PatternPart> parts = PatternParser.parsePatternString(input, options, encodingCallback);
        for (PatternPart part : parts) {
            if (part.isRegExp()) {
                throw new WebURLPatternSyntaxException(
                        "Custom URLPattern regular expressions are not supported");
            }
        }

        Type type = Type.REGEXP;
        String exactMatchValue = "";
        if (parts.isEmpty()) {
            type = Type.EMPTY;
        } else if (parts.size() == 1) {
            PatternPart part = parts.get(0);
            if (part.type() == PatternPart.Type.FIXED_TEXT
                    && part.modifier() == PatternPart.Modifier.NONE
                    && !options.ignoreCase()) {
                type = Type.EXACT_MATCH;
                exactMatchValue = part.value();
            } else if (part.type() == PatternPart.Type.FULL_WILDCARD
                    && part.modifier() == PatternPart.Modifier.NONE
                    && part.prefix().isEmpty()
                    && part.suffix().isEmpty()) {
                type = Type.FULL_WILDCARD;
            }
        }

        String patternString = PatternParser.generatePatternString(parts, options);
        if (type != Type.REGEXP) {
            ArrayList<String> names = new ArrayList<>();
            if (type == Type.FULL_WILDCARD && !parts.isEmpty()) {
                names.add(parts.get(0).name());
            }
            return new PatternComponent(patternString, null, names, false, type, exactMatchValue);
        }

        PatternParser.GeneratedRegExp generated = PatternParser.generateRegularExpressionAndNameList(parts, options);
        int flags = options.ignoreCase() ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
        try {
            return new PatternComponent(patternString, Pattern.compile(generated.regexp(), flags),
                    generated.names(), false, type, exactMatchValue);
        } catch (PatternSyntaxException exception) {
            throw new WebURLPatternSyntaxException("Invalid Java regular expression generated for URLPattern",
                    exception);
        }
    }

    /// Creates a literal exact-match component from an already canonicalized value.
    static PatternComponent exact(String value) {
        return new PatternComponent(value, null, List.of(), false, Type.EXACT_MATCH, value);
    }

    /// Returns the serialized pattern string.
    String pattern() {
        return pattern;
    }

    /// Returns whether this component contains custom regular-expression groups.
    boolean hasRegExpGroups() {
        return hasRegExpGroups;
    }

    /// Tests an input string.
    boolean test(String input) {
        return switch (type) {
            case FULL_WILDCARD -> true;
            case EXACT_MATCH -> input.equals(exactMatchValue);
            case EMPTY -> input.isEmpty();
            case REGEXP -> {
                Pattern value = regexp;
                if (value == null) {
                    yield false;
                }
                yield value.matcher(input).matches();
            }
        };
    }

    /// Matches an input string and returns capture groups.
    @Nullable WebURLPatternEngine.ComponentMatch match(String input) {
        return switch (type) {
            case FULL_WILDCARD -> {
                LinkedHashMap<String, @Nullable String> groups = new LinkedHashMap<>();
                if (!groupNames.isEmpty()) {
                    groups.put(groupNames.get(0), input);
                }
                yield new WebURLPatternEngine.ComponentMatch(input, groups);
            }
            case EXACT_MATCH -> input.equals(exactMatchValue)
                    ? new WebURLPatternEngine.ComponentMatch(input, new LinkedHashMap<>())
                    : null;
            case EMPTY -> input.isEmpty() ? new WebURLPatternEngine.ComponentMatch(input, new LinkedHashMap<>()) : null;
            case REGEXP -> regexpMatch(input);
        };
    }

    /// Returns whether this component matches a special URL scheme.
    boolean matchesSpecialScheme() {
        return test("http") || test("https") || test("ws") || test("wss") || test("ftp");
    }

    /// Runs regular-expression matching for a complex component.
    private @Nullable WebURLPatternEngine.ComponentMatch regexpMatch(String input) {
        Pattern value = regexp;
        if (value == null) {
            return null;
        }
        Matcher matcher = value.matcher(input);
        if (!matcher.matches()) {
            return null;
        }
        LinkedHashMap<String, @Nullable String> groups = new LinkedHashMap<>();
        int count = Math.min(matcher.groupCount(), groupNames.size());
        for (int i = 0; i < count; i++) {
            groups.put(groupNames.get(i), matcher.group(i + 1));
        }
        return new WebURLPatternEngine.ComponentMatch(input, groups);
    }

    /// Fast-path component kinds.
    private enum Type {
        /// Matches only the empty string.
        EMPTY,
        /// Matches a fixed literal exactly.
        EXACT_MATCH,
        /// Matches any string.
        FULL_WILDCARD,
        /// Requires Java regular-expression matching.
        REGEXP
    }
}
