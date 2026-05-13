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

import org.glavo.url.internal.IndexRange;
import org.glavo.url.internal.IndexRanges;
import org.glavo.url.pattern.WebURLPatternParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/// A compiled URLPattern component matcher.
@NotNullByDefault
final class PatternComponent {
    /// Empty group range array shared by components without captures.
    private static final @IndexRange long @Unmodifiable [] EMPTY_GROUP_RANGES = new long[0];

    /// Serialized component pattern string.
    private final String pattern;
    /// Java regular expression for complex components, or `null` for fast-path components.
    private final @Nullable Pattern regexp;
    /// Capture group names in regular-expression order.
    private final List<String> groupNames;
    /// Public group keys mapped to indexes in `groupRanges`.
    private final @Unmodifiable Map<String, Integer> groupIndexes;
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
        this.groupIndexes = groupIndexes(groupNames);
        this.hasRegExpGroups = hasRegExpGroups;
        this.type = type;
        this.exactMatchValue = exactMatchValue;
    }

    /// Compiles a component pattern.
    static PatternComponent compile(String input, Function<String, String> encodingCallback, PatternOptions options) {
        List<PatternPart> parts = processRegExpParts(PatternParser.parsePatternString(input, options, encodingCallback),
                options.regExpPolicy());
        return compileParts(parts, options);
    }

    /// Compiles an IPv6 hostname component pattern.
    static PatternComponent compileIpv6Hostname(String input, PatternOptions options) {
        List<PatternPart> parts = processRegExpParts(PatternParser.parsePatternString(input, options,
                URLPatternCanonicalizer::canonicalizeIpv6HostnamePatternText), options.regExpPolicy());
        if (parts.size() == 1) {
            PatternPart part = parts.get(0);
            if (part.type() == PatternPart.Type.FIXED_TEXT && part.modifier() == PatternPart.Modifier.NONE) {
                String canonical = URLPatternCanonicalizer.canonicalizeIpv6Hostname(part.value());
                return new PatternComponent(PatternParser.escapePatternString(canonical), null, List.of(), false,
                        Type.EXACT_MATCH, canonical);
            }
        }
        return compileParts(parts, options);
    }

    /// Compiles already parsed component parts.
    private static PatternComponent compileParts(List<PatternPart> parts, PatternOptions options) {
        boolean hasRegExpGroups = false;
        for (PatternPart part : parts) {
            if (part.isRegExp()) {
                hasRegExpGroups = true;
                break;
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
            return new PatternComponent(patternString, null, names, hasRegExpGroups, type, exactMatchValue);
        }

        PatternParser.GeneratedRegExp generated = PatternParser.generateRegularExpressionAndNameList(parts, options);
        int flags = options.ignoreCase() ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
        try {
            return new PatternComponent(patternString, Pattern.compile(generated.regexp(), flags),
                    generated.names(), hasRegExpGroups, type, exactMatchValue);
        } catch (PatternSyntaxException exception) {
            throw new WebURLPatternSyntaxException("Invalid Java regular expression generated for URLPattern",
                    exception);
        }
    }

    /// Processes user-written regular-expression elements.
    private static List<PatternPart> processRegExpParts(
            List<PatternPart> parts,
            WebURLPatternParser.RegExpPolicy regExpPolicy
    ) {
        RegExpElementProcessor processor = RegExpElementProcessor.forPolicy(regExpPolicy);
        @Nullable ArrayList<PatternPart> processed = null;
        for (int i = 0; i < parts.size(); i++) {
            PatternPart part = parts.get(i);
            if (!part.isRegExp()) {
                if (processed != null) {
                    processed.add(part);
                }
                continue;
            }

            String value = processor.process(part.value());
            PatternPart processedPart = value.equals(part.value()) ? part : part.withValue(value);
            if (processed == null) {
                processed = new ArrayList<>(parts.size());
                processed.addAll(parts.subList(0, i));
            }
            processed.add(processedPart);
        }
        return processed == null ? parts : processed;
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
                @IndexRange("input") long range = IndexRanges.of(0, input.length());
                @IndexRange("input") long @Unmodifiable [] groupRanges =
                        groupNames.isEmpty() ? EMPTY_GROUP_RANGES : new long[]{range};
                yield new WebURLPatternEngine.ComponentMatch(input, range, groupRanges, groupIndexes);
            }
            case EXACT_MATCH -> input.equals(exactMatchValue)
                    ? new WebURLPatternEngine.ComponentMatch(input, IndexRanges.of(0, input.length()),
                    EMPTY_GROUP_RANGES, groupIndexes)
                    : null;
            case EMPTY -> input.isEmpty()
                    ? new WebURLPatternEngine.ComponentMatch(input, IndexRanges.of(0, 0),
                    EMPTY_GROUP_RANGES, groupIndexes)
                    : null;
            case REGEXP -> regexpMatch(input);
        };
    }

    /// Returns whether this component matches a special URL scheme.
    boolean matchesSpecialScheme() {
        return test("http") || test("https") || test("ws") || test("wss") || test("ftp") || test("file");
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

        @IndexRange("input") long @Unmodifiable [] groupRanges = new long[groupNames.size()];
        int count = Math.min(matcher.groupCount(), groupRanges.length);
        for (int i = 0; i < groupRanges.length; i++) {
            groupRanges[i] = IndexRanges.ABSENT;
        }
        for (int i = 0; i < count; i++) {
            int start = matcher.start(i + 1);
            if (start >= 0) {
                groupRanges[i] = IndexRanges.of(start, matcher.end(i + 1));
            }
        }
        return new WebURLPatternEngine.ComponentMatch(input, IndexRanges.of(0, input.length()),
                groupRanges, groupIndexes);
    }

    /// Creates a group key-to-index map.
    private static @Unmodifiable Map<String, Integer> groupIndexes(List<String> groupNames) {
        if (groupNames.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<>();
        for (int i = 0; i < groupNames.size(); i++) {
            indexes.put(groupNames.get(i), i);
        }
        return Collections.unmodifiableMap(indexes);
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
