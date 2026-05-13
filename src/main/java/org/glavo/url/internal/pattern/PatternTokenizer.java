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

import java.util.ArrayList;
import java.util.List;

/// Tokenizes URLPattern component and constructor strings.
@NotNullByDefault
final class PatternTokenizer {
    /// Input pattern string.
    private final String input;
    /// Tokenization policy.
    private final Policy policy;
    /// Accumulated tokens.
    private final ArrayList<Token> tokens = new ArrayList<>();
    /// Current UTF-16 input index.
    private int index;
    /// Next UTF-16 input index after the last read code point.
    private int nextIndex;
    /// Last read Unicode code point.
    private int codePoint;

    /// Creates a tokenizer.
    private PatternTokenizer(String input, Policy policy) {
        this.input = input;
        this.policy = policy;
    }

    /// Tokenizes an input string.
    static List<Token> tokenize(String input, Policy policy) {
        PatternTokenizer tokenizer = new PatternTokenizer(input, policy);
        tokenizer.run();
        return tokenizer.tokens;
    }

    /// Runs the tokenizer.
    private void run() {
        while (index < input.length()) {
            seekAndGetNextCodePoint(index);
            if (codePoint == '*') {
                addTokenWithDefaults(TokenType.ASTERISK);
                continue;
            }
            if (codePoint == '+' || codePoint == '?') {
                addTokenWithDefaults(TokenType.OTHER_MODIFIER);
                continue;
            }
            if (codePoint == '\\') {
                tokenizeEscapedChar();
                continue;
            }
            if (codePoint == '{') {
                addTokenWithDefaults(TokenType.OPEN);
                continue;
            }
            if (codePoint == '}') {
                addTokenWithDefaults(TokenType.CLOSE);
                continue;
            }
            if (codePoint == ':') {
                tokenizeName();
                continue;
            }
            if (codePoint == '(') {
                tokenizeRegExp();
                continue;
            }
            addTokenWithDefaults(TokenType.CHAR);
        }
        addToken(TokenType.END, index, index, 0);
    }

    /// Tokenizes an escaped character.
    private void tokenizeEscapedChar() {
        if (index == input.length() - 1) {
            processTokenizingError(nextIndex, index);
            return;
        }
        int escapedIndex = nextIndex;
        getNextCodePoint();
        addToken(TokenType.ESCAPED_CHAR, nextIndex, escapedIndex, nextIndex - escapedIndex);
    }

    /// Tokenizes a named group token.
    private void tokenizeName() {
        int namePosition = nextIndex;
        int nameStart = namePosition;
        while (namePosition < input.length()) {
            seekAndGetNextCodePoint(namePosition);
            if (!isValidNameCodePoint(codePoint, namePosition == nameStart)) {
                break;
            }
            namePosition = nextIndex;
        }
        if (namePosition <= nameStart) {
            processTokenizingError(nameStart, index);
            return;
        }
        addToken(TokenType.NAME, namePosition, nameStart, namePosition - nameStart);
    }

    /// Tokenizes a regular-expression token.
    private void tokenizeRegExp() {
        int depth = 1;
        int regexpPosition = nextIndex;
        int regexpStart = regexpPosition;
        boolean error = false;

        while (regexpPosition < input.length()) {
            seekAndGetNextCodePoint(regexpPosition);
            if (codePoint > 0x7f || regexpPosition == regexpStart && codePoint == '?') {
                processTokenizingError(regexpStart, index);
                error = true;
                break;
            }
            if (codePoint == '\\') {
                if (regexpPosition == input.length() - 1) {
                    processTokenizingError(regexpStart, index);
                    error = true;
                    break;
                }
                getNextCodePoint();
                if (codePoint > 0x7f) {
                    processTokenizingError(regexpStart, index);
                    error = true;
                    break;
                }
                regexpPosition = nextIndex;
                continue;
            }
            if (codePoint == ')') {
                depth--;
                if (depth == 0) {
                    regexpPosition = nextIndex;
                    break;
                }
            } else if (codePoint == '(') {
                depth++;
                if (regexpPosition == input.length() - 1) {
                    processTokenizingError(regexpStart, index);
                    error = true;
                    break;
                }
                int temporaryPosition = nextIndex;
                getNextCodePoint();
                if (codePoint != '?') {
                    processTokenizingError(regexpStart, index);
                    error = true;
                    break;
                }
                nextIndex = temporaryPosition;
            }
            regexpPosition = nextIndex;
        }

        if (error) {
            return;
        }
        if (depth != 0) {
            processTokenizingError(regexpStart, index);
            return;
        }
        int regexpLength = regexpPosition - regexpStart - 1;
        if (regexpLength == 0) {
            processTokenizingError(regexpStart, index);
            return;
        }
        addToken(TokenType.REGEXP, regexpPosition, regexpStart, regexpLength);
    }

    /// Reads the next code point from the current next index.
    private void getNextCodePoint() {
        codePoint = input.codePointAt(nextIndex);
        nextIndex += Character.charCount(codePoint);
    }

    /// Moves to an index and reads the next code point.
    private void seekAndGetNextCodePoint(int newIndex) {
        nextIndex = newIndex;
        getNextCodePoint();
    }

    /// Adds a token.
    private void addToken(TokenType type, int nextPosition, int valuePosition, int valueLength) {
        tokens.add(new Token(type, index, input.substring(valuePosition, valuePosition + valueLength)));
        index = nextPosition;
    }

    /// Adds a token whose value ends at the supplied next position.
    private void addTokenWithDefaultLength(TokenType type, int nextPosition, int valuePosition) {
        addToken(type, nextPosition, valuePosition, nextPosition - valuePosition);
    }

    /// Adds a token from the current index to the current next index.
    private void addTokenWithDefaults(TokenType type) {
        addTokenWithDefaultLength(type, nextIndex, index);
    }

    /// Processes a tokenization error.
    private void processTokenizingError(int nextPosition, int valuePosition) {
        if (policy == Policy.STRICT) {
            throw new WebURLPatternSyntaxException("Invalid URLPattern token at index " + valuePosition);
        }
        addTokenWithDefaultLength(TokenType.INVALID_CHAR, nextPosition, valuePosition);
    }

    /// Returns whether a code point can appear in a URLPattern group name.
    private static boolean isValidNameCodePoint(int codePoint, boolean first) {
        if (codePoint == '_' || codePoint == '$') {
            return true;
        }
        if (first) {
            return Character.isUnicodeIdentifierStart(codePoint);
        }
        return Character.isUnicodeIdentifierPart(codePoint);
    }

    /// Tokenization policy.
    enum Policy {
        /// Reject invalid tokens.
        STRICT,
        /// Preserve invalid tokens as invalid-char tokens.
        LENIENT
    }

    /// URLPattern token types.
    enum TokenType {
        /// An invalid character token.
        INVALID_CHAR,
        /// An opening `{` token.
        OPEN,
        /// A closing `}` token.
        CLOSE,
        /// A regular-expression token.
        REGEXP,
        /// A named group token.
        NAME,
        /// A plain character token.
        CHAR,
        /// An escaped character token.
        ESCAPED_CHAR,
        /// A `?` or `+` modifier token.
        OTHER_MODIFIER,
        /// A `*` token.
        ASTERISK,
        /// End of input.
        END
    }

    /// One URLPattern token.
    record Token(TokenType type, int index, String value) {
    }
}
