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
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;

/// Processes user-written URLPattern regular-expression elements.
@NotNullByDefault
interface RegExpElementProcessor {
    /// Processor that accepts the supported standard-compatible subset.
    RegExpElementProcessor SUPPORTED = RegExpElementProcessor::processSupported;

    /// Processor that rejects every user-written regular-expression element.
    RegExpElementProcessor REJECT = regexp -> {
        throw new WebURLPatternSyntaxException("Custom URLPattern regular expressions are rejected by this parser");
    };

    /// Processor that accepts Java regular-expression syntax without additional capture groups.
    RegExpElementProcessor JAVA = regexp -> {
        validateNoCapturingGroups(regexp);
        return regexp;
    };

    /// Returns the processor for the given policy.
    ///
    /// @param policy the regular-expression element policy
    /// @return the processor
    static RegExpElementProcessor forPolicy(WebURLPatternParser.RegExpPolicy policy) {
        return switch (policy) {
            case SUPPORTED -> SUPPORTED;
            case REJECT -> REJECT;
            case JAVA -> JAVA;
        };
    }

    /// Processes one regular-expression element and returns Java-compatible source.
    ///
    /// @param regexp regular-expression element source
    /// @return Java-compatible regular-expression source
    String process(String regexp);

    /// Validates the supported standard-compatible regular-expression subset.
    private static String processSupported(String regexp) {
        SupportedRegExpParser parser = new SupportedRegExpParser(regexp);
        parser.parse();
        return regexp;
    }

    /// Rejects Java regular expressions that would shift URLPattern capture group indexes.
    private static void validateNoCapturingGroups(String regexp) {
        boolean inClass = false;
        boolean escaped = false;

        for (int i = 0; i < regexp.length(); i++) {
            char c = regexp.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (inClass) {
                if (c == ']') {
                    inClass = false;
                }
                continue;
            }
            if (c == '[') {
                inClass = true;
                continue;
            }
            if (c != '(') {
                continue;
            }
            if (i + 1 >= regexp.length() || regexp.charAt(i + 1) != '?') {
                throw new WebURLPatternSyntaxException("URLPattern regular expressions cannot contain capture groups");
            }
            if (i + 2 < regexp.length() && regexp.charAt(i + 2) == '<') {
                if (i + 3 >= regexp.length() || regexp.charAt(i + 3) != '=' && regexp.charAt(i + 3) != '!') {
                    throw new WebURLPatternSyntaxException(
                            "URLPattern regular expressions cannot contain named capture groups");
                }
            }
        }
    }

    /// Parser for the supported standard-compatible regular-expression subset.
    @NotNullByDefault
    final class SupportedRegExpParser {
        /// Regular-expression element source.
        private final String input;
        /// Current input index.
        private int index;
        /// Whether the previous atom can receive a quantifier.
        private boolean canQuantify;

        /// Creates a parser.
        ///
        /// @param input regular-expression element source
        private SupportedRegExpParser(String input) {
            this.input = input;
        }

        /// Parses the whole regular-expression element.
        private void parse() {
            while (index < input.length()) {
                char c = input.charAt(index);
                switch (c) {
                    case '[' -> {
                        parseCharacterClass();
                        canQuantify = true;
                    }
                    case '\\' -> {
                        parseEscape(false);
                        canQuantify = true;
                    }
                    case '.' -> {
                        index++;
                        canQuantify = true;
                    }
                    case '|' -> {
                        index++;
                        canQuantify = false;
                    }
                    case '*', '+', '?' -> parseSimpleQuantifier();
                    case '{' -> parseBraceQuantifier();
                    case '(', ')', '^', '$', ']', '}' -> throw unsupported();
                    default -> {
                        index++;
                        canQuantify = true;
                    }
                }
            }
        }

        /// Parses a character class.
        private void parseCharacterClass() {
            index++;
            if (index < input.length() && input.charAt(index) == '^') {
                index++;
            }

            boolean hasContent = false;
            boolean escaped = false;
            while (index < input.length()) {
                char c = input.charAt(index);
                if (escaped) {
                    escaped = false;
                    hasContent = true;
                    index++;
                    continue;
                }
                if (c == '\\') {
                    parseEscape(true);
                    hasContent = true;
                    continue;
                }
                if (c == ']') {
                    if (!hasContent) {
                        throw unsupported();
                    }
                    index++;
                    return;
                }
                if (c == '[' || c == '&') {
                    throw unsupported();
                }
                hasContent = true;
                index++;
            }
            throw unsupported();
        }

        /// Parses an escape sequence.
        private void parseEscape(boolean inClass) {
            index++;
            if (index >= input.length()) {
                throw unsupported();
            }

            char escaped = input.charAt(index);
            if (isAllowedCharacterClassEscape(escaped)) {
                index++;
                return;
            }
            if (isAllowedControlEscape(escaped)) {
                index++;
                return;
            }
            if (isAllowedSyntaxEscape(escaped, inClass)) {
                index++;
                return;
            }
            throw unsupported();
        }

        /// Parses `*`, `+`, or `?`.
        private void parseSimpleQuantifier() {
            if (!canQuantify) {
                throw unsupported();
            }
            index++;
            consumeLazyMarker();
            canQuantify = false;
        }

        /// Parses a `{m}`, `{m,}`, or `{m,n}` quantifier.
        private void parseBraceQuantifier() {
            if (!canQuantify) {
                throw unsupported();
            }

            int start = index;
            index++;
            int minimum = parseDecimalInteger();
            int maximum = minimum;
            if (index < input.length() && input.charAt(index) == ',') {
                index++;
                maximum = index < input.length() && Character.isDigit(input.charAt(index))
                        ? parseDecimalInteger()
                        : Integer.MAX_VALUE;
            }
            if (index >= input.length() || input.charAt(index) != '}') {
                throw unsupported();
            }
            if (maximum < minimum) {
                throw unsupported();
            }
            index++;
            consumeLazyMarker();
            canQuantify = false;

            if (index == start + 1) {
                throw unsupported();
            }
        }

        /// Parses a decimal integer.
        private int parseDecimalInteger() {
            if (index >= input.length() || !Character.isDigit(input.charAt(index))) {
                throw unsupported();
            }
            int value = 0;
            do {
                int digit = input.charAt(index) - '0';
                if (value > (Integer.MAX_VALUE - digit) / 10) {
                    throw unsupported();
                }
                value = value * 10 + digit;
                index++;
            } while (index < input.length() && Character.isDigit(input.charAt(index)));
            return value;
        }

        /// Consumes a lazy quantifier suffix and rejects possessive quantifiers.
        private void consumeLazyMarker() {
            if (index >= input.length()) {
                return;
            }
            char c = input.charAt(index);
            if (c == '?') {
                index++;
            } else if (c == '+') {
                throw unsupported();
            }
        }

        /// Creates the standard unsupported-syntax exception.
        private WebURLPatternSyntaxException unsupported() {
            return new WebURLPatternSyntaxException("Unsupported URLPattern regular-expression syntax");
        }

        /// Returns whether the escape is a supported character-class escape.
        private static boolean isAllowedCharacterClassEscape(char c) {
            return c == 'd' || c == 'D' || c == 'w' || c == 'W';
        }

        /// Returns whether the escape is a supported control escape.
        private static boolean isAllowedControlEscape(char c) {
            return c == 'n' || c == 'r' || c == 't' || c == 'f';
        }

        /// Returns whether the escape is a supported syntax escape.
        private static boolean isAllowedSyntaxEscape(char c, boolean inClass) {
            return switch (c) {
                case '\\', '/', '.', '+', '*', '?', '^', '$', '{', '}', '(', ')', '|' -> true;
                case '[', ']' -> true;
                case '-' -> inClass;
                default -> false;
            };
        }
    }
}
