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

/// Processes the supported JavaScript regular-expression subset used by URLPattern.
@NotNullByDefault
final class JavaScriptRegExpProcessor {
    /// Creates no instances.
    private JavaScriptRegExpProcessor() {
    }

    /// Processes one JavaScript regular-expression element and returns Java-compatible source.
    ///
    /// @param regexp JavaScript regular-expression element source
    /// @return Java-compatible regular-expression source
    static String process(String regexp) {
        Parser parser = new Parser(regexp);
        return parser.parse();
    }

    /// Parser for the supported standard-compatible regular-expression subset.
    @NotNullByDefault
    private static final class Parser {
        /// Regular-expression element source.
        private final String input;
        /// Java-compatible regular-expression output.
        private final StringBuilder output = new StringBuilder();
        /// Current input index.
        private int index;
        /// Whether the previous atom can receive a quantifier.
        private boolean canQuantify;

        /// Creates a parser.
        ///
        /// @param input regular-expression element source
        private Parser(String input) {
            this.input = input;
        }

        /// Parses the whole regular-expression element.
        private String parse() {
            parseUntilGroupEnd(false);
            return output.toString();
        }

        /// Parses until the end of input or the end of a group.
        private void parseUntilGroupEnd(boolean inGroup) {
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
                        output.append(c);
                        index++;
                        canQuantify = true;
                    }
                    case '|' -> {
                        output.append(c);
                        index++;
                        canQuantify = false;
                    }
                    case '*', '+', '?' -> parseSimpleQuantifier();
                    case '{' -> parseBraceQuantifier();
                    case '(' -> parseGroup();
                    case ')' -> {
                        if (!inGroup) {
                            throw unsupported();
                        }
                        output.append(c);
                        index++;
                        canQuantify = true;
                        return;
                    }
                    case '^', '$', ']', '}' -> throw unsupported();
                    default -> {
                        output.append(c);
                        index++;
                        canQuantify = true;
                    }
                }
            }
            if (inGroup) {
                throw unsupported();
            }
        }

        /// Parses a character class.
        private void parseCharacterClass() {
            int start = index;
            if (containsClassSetSyntax(index)) {
                ClassSetParser parser = new ClassSetParser(input, index);
                output.append(parser.parse());
                index = parser.index();
                return;
            }

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
                    output.append(input, start, index);
                    return;
                }
                if (c == '[' || c == '&' || c == '-' && index + 1 < input.length() && input.charAt(index + 1) == '-') {
                    throw unsupported();
                }
                hasContent = true;
                index++;
            }
            throw unsupported();
        }

        /// Returns whether the character class uses supported `v`-mode set syntax.
        private boolean containsClassSetSyntax(int start) {
            for (int i = start + 1; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '\\') {
                    i++;
                    continue;
                }
                if (c == ']') {
                    return false;
                }
                if (c == '[' || c == '&' && i + 1 < input.length() && input.charAt(i + 1) == '&'
                        || c == '-' && i + 1 < input.length() && input.charAt(i + 1) == '-') {
                    return true;
                }
            }
            throw unsupported();
        }

        /// Parses an escape sequence.
        private void parseEscape(boolean inClass) {
            int start = index;
            index++;
            if (index >= input.length()) {
                throw unsupported();
            }

            char escaped = input.charAt(index);
            if (isAllowedCharacterClassEscape(escaped)) {
                index++;
                if (!inClass) {
                    output.append(input, start, index);
                }
                return;
            }
            if (isAllowedControlEscape(escaped)) {
                index++;
                if (!inClass) {
                    output.append(input, start, index);
                }
                return;
            }
            if (isAllowedSyntaxEscape(escaped, inClass)) {
                index++;
                if (!inClass) {
                    output.append(input, start, index);
                }
                return;
            }
            throw unsupported();
        }

        /// Parses a non-capturing or named-capturing group as a non-capturing Java group.
        private void parseGroup() {
            if (input.startsWith("(?:", index)) {
                output.append("(?:");
                index += 3;
                canQuantify = false;
                parseUntilGroupEnd(true);
                return;
            }
            if (input.startsWith("(?<", index)) {
                int nameStart = index + 3;
                int nameEnd = input.indexOf('>', nameStart);
                if (nameEnd <= nameStart) {
                    throw unsupported();
                }
                for (int i = nameStart; i < nameEnd; i++) {
                    if (!isGroupNameChar(input.charAt(i))) {
                        throw unsupported();
                    }
                }
                output.append("(?:");
                index = nameEnd + 1;
                canQuantify = false;
                parseUntilGroupEnd(true);
                return;
            }
            throw unsupported();
        }

        /// Parses `*`, `+`, or `?`.
        private void parseSimpleQuantifier() {
            if (!canQuantify) {
                throw unsupported();
            }
            output.append(input.charAt(index));
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
            output.append(input, start, index);
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
                output.append(c);
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

        /// Returns whether a named group name character is supported.
        private static boolean isGroupNameChar(char c) {
            return c == '_' || c == '$' || c >= '0' && c <= '9'
                    || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
        }
    }

    /// Parser for the supported finite ASCII subset of JavaScript `v`-mode class-set expressions.
    @NotNullByDefault
    private static final class ClassSetParser {
        /// Number of ASCII code points represented by a finite set.
        private static final int ASCII_LIMIT = 0x80;

        /// Regular-expression element source.
        private final String input;
        /// Current input index.
        private int index;

        /// Creates a parser.
        ///
        /// @param input regular-expression element source
        /// @param index index of the opening `[` character
        private ClassSetParser(String input, int index) {
            this.input = input;
            this.index = index;
        }

        /// Parses one class-set expression and returns Java-compatible source.
        private String parse() {
            boolean[] set = parseClass();
            return serialize(set);
        }

        /// Returns the index after the parsed class.
        private int index() {
            return index;
        }

        /// Parses a bracketed class-set expression.
        private boolean[] parseClass() {
            consume('[');
            if (peek('^')) {
                throw unsupported();
            }

            boolean[] result = parseUnion();
            consume(']');
            return result;
        }

        /// Parses class-set union and binary operations.
        private boolean[] parseUnion() {
            boolean[] result = emptySet();
            @Nullable Operator operatorKind = null;
            @Nullable Operator pendingOperator = null;
            boolean hasTerm = false;
            boolean expectingTerm = true;

            while (!atEnd() && !peek(']')) {
                @Nullable Operator nextOperator = tryConsumeOperator();
                if (nextOperator != null) {
                    if (expectingTerm || operatorKind != null && operatorKind != nextOperator) {
                        throw unsupported();
                    }
                    operatorKind = nextOperator;
                    pendingOperator = nextOperator;
                    expectingTerm = true;
                    continue;
                }
                if (operatorKind != null && pendingOperator == null) {
                    throw unsupported();
                }

                boolean[] term = parseTerm();
                if (pendingOperator == null) {
                    unionInto(result, term);
                } else if (pendingOperator == Operator.INTERSECTION) {
                    intersectInto(result, term);
                } else {
                    subtractFrom(result, term);
                }
                pendingOperator = null;
                hasTerm = true;
                expectingTerm = false;
            }

            if (!hasTerm || expectingTerm) {
                throw unsupported();
            }
            return result;
        }

        /// Parses one class-set term.
        private boolean[] parseTerm() {
            if (peek('[')) {
                return parseClass();
            }
            if (peek('\\')) {
                return parseEscapedTerm();
            }

            char start = consumeLiteral();
            if (peek('-') && !startsWith("--") && index + 1 < input.length() && input.charAt(index + 1) != ']') {
                index++;
                char end = peek('\\') ? consumeEscapedLiteral() : consumeLiteral();
                if (end < start) {
                    throw unsupported();
                }
                return rangeSet(start, end);
            }
            return singleSet(start);
        }

        /// Parses an escaped class-set term.
        private boolean[] parseEscapedTerm() {
            consume('\\');
            if (atEnd()) {
                throw unsupported();
            }
            char escaped = input.charAt(index++);
            return switch (escaped) {
                case 'd' -> rangeSet('0', '9');
                case 'w' -> wordSet();
                case 'n' -> singleSet('\n');
                case 'r' -> singleSet('\r');
                case 't' -> singleSet('\t');
                case 'f' -> singleSet('\f');
                case '\\', '/', '.', '+', '*', '?', '^', '$', '{', '}', '(', ')', '|', '[', ']', '-' ->
                        singleSet(escaped);
                default -> throw unsupported();
            };
        }

        /// Consumes an escaped literal used as a range endpoint.
        private char consumeEscapedLiteral() {
            boolean[] set = parseEscapedTerm();
            int value = onlyCodePoint(set);
            if (value < 0) {
                throw unsupported();
            }
            return (char) value;
        }

        /// Consumes one unescaped literal character.
        private char consumeLiteral() {
            if (atEnd()) {
                throw unsupported();
            }
            char c = input.charAt(index);
            if (c == '[' || c == ']' || c == '\\' || c == '&' || c == '-') {
                throw unsupported();
            }
            index++;
            return c;
        }

        /// Tries to consume a class-set operator.
        private @Nullable Operator tryConsumeOperator() {
            if (startsWith("&&")) {
                index += 2;
                return Operator.INTERSECTION;
            }
            if (startsWith("--")) {
                index += 2;
                return Operator.SUBTRACTION;
            }
            return null;
        }

        /// Consumes the expected character.
        private void consume(char expected) {
            if (!peek(expected)) {
                throw unsupported();
            }
            index++;
        }

        /// Returns whether the current character matches the expected one.
        private boolean peek(char expected) {
            return !atEnd() && input.charAt(index) == expected;
        }

        /// Returns whether the current input starts with the supplied string.
        private boolean startsWith(String expected) {
            return input.startsWith(expected, index);
        }

        /// Returns whether the parser reached the end of the regexp element.
        private boolean atEnd() {
            return index >= input.length();
        }

        /// Creates an empty ASCII set.
        private static boolean[] emptySet() {
            return new boolean[ASCII_LIMIT];
        }

        /// Creates a single-code-point ASCII set.
        private static boolean[] singleSet(char value) {
            if (value >= ASCII_LIMIT) {
                throw unsupported();
            }
            boolean[] set = emptySet();
            set[value] = true;
            return set;
        }

        /// Creates an inclusive ASCII range set.
        private static boolean[] rangeSet(char start, char end) {
            if (start >= ASCII_LIMIT || end >= ASCII_LIMIT) {
                throw unsupported();
            }
            boolean[] set = emptySet();
            for (int i = start; i <= end; i++) {
                set[i] = true;
            }
            return set;
        }

        /// Creates the ASCII JavaScript `\w` set.
        private static boolean[] wordSet() {
            boolean[] set = rangeSet('0', '9');
            unionInto(set, rangeSet('A', 'Z'));
            unionInto(set, rangeSet('a', 'z'));
            set['_'] = true;
            return set;
        }

        /// Unions a set into another set.
        private static void unionInto(boolean[] result, boolean[] term) {
            for (int i = 0; i < result.length; i++) {
                result[i] |= term[i];
            }
        }

        /// Intersects a set with another set.
        private static void intersectInto(boolean[] result, boolean[] term) {
            for (int i = 0; i < result.length; i++) {
                result[i] &= term[i];
            }
        }

        /// Subtracts a set from another set.
        private static void subtractFrom(boolean[] result, boolean[] term) {
            for (int i = 0; i < result.length; i++) {
                result[i] &= !term[i];
            }
        }

        /// Returns the only code point in a set, or `-1` when it is not a singleton.
        private static int onlyCodePoint(boolean[] set) {
            int result = -1;
            for (int i = 0; i < set.length; i++) {
                if (!set[i]) {
                    continue;
                }
                if (result >= 0) {
                    return -1;
                }
                result = i;
            }
            return result;
        }

        /// Serializes an ASCII set as Java regular-expression source.
        private static String serialize(boolean[] set) {
            if (isEmpty(set)) {
                return "[^\\s\\S]";
            }

            StringBuilder builder = new StringBuilder("[");
            for (int start = 0; start < set.length; start++) {
                if (!set[start]) {
                    continue;
                }
                int end = start;
                while (end + 1 < set.length && set[end + 1]) {
                    end++;
                }
                if (end - start >= 2) {
                    appendClassChar(builder, start);
                    builder.append('-');
                    appendClassChar(builder, end);
                } else {
                    for (int i = start; i <= end; i++) {
                        appendClassChar(builder, i);
                    }
                }
                start = end;
            }
            return builder.append(']').toString();
        }

        /// Returns whether a set is empty.
        private static boolean isEmpty(boolean[] set) {
            for (boolean value : set) {
                if (value) {
                    return false;
                }
            }
            return true;
        }

        /// Appends a code point escaped for a Java character class.
        private static void appendClassChar(StringBuilder builder, int value) {
            switch (value) {
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                case '\f' -> builder.append("\\f");
                case '\\' -> builder.append("\\\\");
                case ']' -> builder.append("\\]");
                case '^' -> builder.append("\\^");
                case '-' -> builder.append("\\-");
                default -> {
                    if (value < 0x20 || value == 0x7f) {
                        builder.append("\\u00");
                        String hex = Integer.toHexString(value);
                        if (hex.length() == 1) {
                            builder.append('0');
                        }
                        builder.append(hex);
                    } else {
                        builder.append((char) value);
                    }
                }
            }
        }

        /// Creates the standard unsupported-syntax exception.
        private static WebURLPatternSyntaxException unsupported() {
            return new WebURLPatternSyntaxException("Unsupported URLPattern regular-expression syntax");
        }

        /// Supported class-set binary operators.
        private enum Operator {
            /// The `&&` operator.
            INTERSECTION,
            /// The `--` operator.
            SUBTRACTION
        }
    }
}
