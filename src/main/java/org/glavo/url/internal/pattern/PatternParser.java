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

import org.glavo.url.WebURLPatternSyntaxException;
import org.glavo.url.internal.StringUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/// Parses and serializes URLPattern component pattern strings.
@NotNullByDefault
final class PatternParser {
    /// Tokens to parse.
    private final List<PatternTokenizer.Token> tokens;
    /// Component canonicalization callback.
    private final Function<String, String> encodingCallback;
    /// Segment wildcard regular expression.
    private final String segmentWildcardRegexp;
    /// Parsed parts.
    private final ArrayList<PatternPart> parts = new ArrayList<>();
    /// Pending fixed text.
    private final StringBuilder pendingFixedValue = new StringBuilder();
    /// Current token index.
    private int index;
    /// Next numeric group name.
    private int nextNumericName;

    /// Creates a parser.
    private PatternParser(
            List<PatternTokenizer.Token> tokens,
            Function<String, String> encodingCallback,
            String segmentWildcardRegexp
    ) {
        this.tokens = tokens;
        this.encodingCallback = encodingCallback;
        this.segmentWildcardRegexp = segmentWildcardRegexp;
    }

    /// Parses a component pattern into parts.
    static List<PatternPart> parsePatternString(
            String input,
            PatternOptions options,
            Function<String, String> encodingCallback
    ) {
        PatternParser parser = new PatternParser(
                PatternTokenizer.tokenize(input, PatternTokenizer.Policy.STRICT),
                encodingCallback,
                generateSegmentWildcardRegexp(options)
        );

        while (parser.canContinue()) {
            @Nullable PatternTokenizer.Token charToken = parser.tryConsumeToken(PatternTokenizer.TokenType.CHAR);
            @Nullable PatternTokenizer.Token nameToken = parser.tryConsumeToken(PatternTokenizer.TokenType.NAME);
            @Nullable PatternTokenizer.Token regexpOrWildcardToken =
                    parser.tryConsumeRegexpOrWildcardToken(nameToken);
            if (nameToken != null || regexpOrWildcardToken != null) {
                String prefix = charToken == null ? "" : charToken.value();
                if (!prefix.isEmpty() && !prefix.equals(options.prefix())) {
                    parser.pendingFixedValue.append(prefix);
                    prefix = "";
                }
                parser.maybeAddPartFromPendingFixedValue();
                @Nullable PatternTokenizer.Token modifierToken = parser.tryConsumeModifierToken();
                parser.addPart(prefix, nameToken, regexpOrWildcardToken, "", modifierToken);
                continue;
            }

            @Nullable PatternTokenizer.Token fixedToken = charToken;
            if (fixedToken == null) {
                fixedToken = parser.tryConsumeToken(PatternTokenizer.TokenType.ESCAPED_CHAR);
            }
            if (fixedToken != null) {
                parser.pendingFixedValue.append(fixedToken.value());
                continue;
            }

            @Nullable PatternTokenizer.Token openToken = parser.tryConsumeToken(PatternTokenizer.TokenType.OPEN);
            if (openToken != null) {
                String prefix = parser.consumeText();
                nameToken = parser.tryConsumeToken(PatternTokenizer.TokenType.NAME);
                regexpOrWildcardToken = parser.tryConsumeRegexpOrWildcardToken(nameToken);
                String suffix = parser.consumeText();
                if (!parser.consumeRequiredToken(PatternTokenizer.TokenType.CLOSE)) {
                    throw new WebURLPatternSyntaxException("Unclosed URLPattern group");
                }
                @Nullable PatternTokenizer.Token modifierToken = parser.tryConsumeModifierToken();
                parser.addPart(prefix, nameToken, regexpOrWildcardToken, suffix, modifierToken);
                continue;
            }

            parser.maybeAddPartFromPendingFixedValue();
            if (!parser.consumeRequiredToken(PatternTokenizer.TokenType.END)) {
                throw new WebURLPatternSyntaxException("Unexpected URLPattern token");
            }
        }
        return parser.parts;
    }

    /// Returns whether there are more tokens to parse.
    private boolean canContinue() {
        return index < tokens.size();
    }

    /// Consumes a token if it has the requested type.
    private @Nullable PatternTokenizer.Token tryConsumeToken(PatternTokenizer.TokenType type) {
        PatternTokenizer.Token next = tokens.get(index);
        if (next.type() != type) {
            return null;
        }
        index++;
        return next;
    }

    /// Consumes a modifier token if present.
    private @Nullable PatternTokenizer.Token tryConsumeModifierToken() {
        @Nullable PatternTokenizer.Token token = tryConsumeToken(PatternTokenizer.TokenType.OTHER_MODIFIER);
        return token == null ? tryConsumeToken(PatternTokenizer.TokenType.ASTERISK) : token;
    }

    /// Consumes a regular-expression or wildcard token if allowed.
    private @Nullable PatternTokenizer.Token tryConsumeRegexpOrWildcardToken(
            @Nullable PatternTokenizer.Token nameToken
    ) {
        @Nullable PatternTokenizer.Token token = tryConsumeToken(PatternTokenizer.TokenType.REGEXP);
        if (nameToken == null && token == null) {
            token = tryConsumeToken(PatternTokenizer.TokenType.ASTERISK);
        }
        return token;
    }

    /// Consumes fixed text tokens.
    private String consumeText() {
        StringBuilder result = new StringBuilder();
        while (true) {
            @Nullable PatternTokenizer.Token token = tryConsumeToken(PatternTokenizer.TokenType.CHAR);
            if (token == null) {
                token = tryConsumeToken(PatternTokenizer.TokenType.ESCAPED_CHAR);
            }
            if (token == null) {
                break;
            }
            result.append(token.value());
        }
        return result.toString();
    }

    /// Consumes a required token.
    private boolean consumeRequiredToken(PatternTokenizer.TokenType type) {
        return tryConsumeToken(type) != null;
    }

    /// Adds a fixed text part from the pending fixed value.
    private void maybeAddPartFromPendingFixedValue() {
        if (pendingFixedValue.isEmpty()) {
            return;
        }
        String encodedValue = encodingCallback.apply(pendingFixedValue.toString());
        pendingFixedValue.setLength(0);
        parts.add(PatternPart.fixed(encodedValue, PatternPart.Modifier.NONE));
    }

    /// Adds a parsed part.
    private void addPart(
            String prefix,
            @Nullable PatternTokenizer.Token nameToken,
            @Nullable PatternTokenizer.Token regexpOrWildcardToken,
            String suffix,
            @Nullable PatternTokenizer.Token modifierToken
    ) {
        PatternPart.Modifier modifier = PatternPart.Modifier.NONE;
        if (modifierToken != null) {
            modifier = switch (modifierToken.value()) {
                case "?" -> PatternPart.Modifier.OPTIONAL;
                case "*" -> PatternPart.Modifier.ZERO_OR_MORE;
                case "+" -> PatternPart.Modifier.ONE_OR_MORE;
                default -> PatternPart.Modifier.NONE;
            };
        }

        if (nameToken == null && regexpOrWildcardToken == null && modifier == PatternPart.Modifier.NONE) {
            pendingFixedValue.append(prefix);
            return;
        }

        maybeAddPartFromPendingFixedValue();
        if (nameToken == null && regexpOrWildcardToken == null) {
            if (prefix.isEmpty()) {
                return;
            }
            parts.add(PatternPart.fixed(encodingCallback.apply(prefix), modifier));
            return;
        }

        String regexpValue;
        if (regexpOrWildcardToken == null) {
            regexpValue = segmentWildcardRegexp;
        } else if (regexpOrWildcardToken.type() == PatternTokenizer.TokenType.ASTERISK) {
            regexpValue = ".*";
        } else {
            regexpValue = regexpOrWildcardToken.value();
        }

        PatternPart.Type type = PatternPart.Type.REGEXP;
        if (regexpValue.equals(segmentWildcardRegexp)) {
            type = PatternPart.Type.SEGMENT_WILDCARD;
            regexpValue = "";
        } else if (regexpValue.equals(".*")) {
            type = PatternPart.Type.FULL_WILDCARD;
            regexpValue = "";
        }

        String name;
        if (nameToken != null) {
            name = nameToken.value();
        } else {
            name = Integer.toString(nextNumericName);
            nextNumericName++;
        }
        if (hasDuplicateName(name)) {
            throw new WebURLPatternSyntaxException("Duplicate URLPattern group name: " + name);
        }

        parts.add(PatternPart.matching(
                type,
                regexpValue,
                modifier,
                name,
                encodingCallback.apply(prefix),
                encodingCallback.apply(suffix)
        ));
    }

    /// Returns whether a name already appears in the part list.
    private boolean hasDuplicateName(String name) {
        for (PatternPart part : parts) {
            if (part.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /// Generates a serialized pattern string from parts.
    static String generatePatternString(List<PatternPart> parts, PatternOptions options) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < parts.size(); index++) {
            PatternPart part = parts.get(index);
            @Nullable PatternPart previousPart = index == 0 ? null : parts.get(index - 1);
            @Nullable PatternPart nextPart = index + 1 < parts.size() ? parts.get(index + 1) : null;

            if (part.type() == PatternPart.Type.FIXED_TEXT) {
                if (part.modifier() == PatternPart.Modifier.NONE) {
                    result.append(escapePatternString(part.value()));
                } else {
                    result.append('{').append(escapePatternString(part.value())).append('}')
                            .append(modifierString(part.modifier()));
                }
                continue;
            }

            boolean customName = !StringUtils.isAsciiDigit(part.name().charAt(0));
            boolean needsGrouping = !part.suffix().isEmpty()
                    || !part.prefix().isEmpty()
                    && !options.prefix().isEmpty()
                    && part.prefix().charAt(0) != options.prefix().charAt(0);

            if (!needsGrouping
                    && customName
                    && part.type() == PatternPart.Type.SEGMENT_WILDCARD
                    && part.modifier() == PatternPart.Modifier.NONE
                    && nextPart != null
                    && nextPart.prefix().isEmpty()
                    && nextPart.suffix().isEmpty()) {
                if (nextPart.type() == PatternPart.Type.FIXED_TEXT) {
                    needsGrouping = !nextPart.value().isEmpty()
                            && isValidNameContinuation(nextPart.value().codePointAt(0));
                } else {
                    needsGrouping = !nextPart.name().isEmpty()
                            && StringUtils.isAsciiDigit(nextPart.name().charAt(0));
                }
            }

            if (!needsGrouping
                    && part.prefix().isEmpty()
                    && previousPart != null
                    && previousPart.type() == PatternPart.Type.FIXED_TEXT
                    && !previousPart.value().isEmpty()
                    && !options.prefix().isEmpty()
                    && previousPart.value().charAt(previousPart.value().length() - 1) == options.prefix().charAt(0)) {
                needsGrouping = true;
            }

            if (needsGrouping) {
                result.append('{');
            }
            result.append(escapePatternString(part.prefix()));
            if (customName) {
                result.append(':').append(part.name());
            }
            if (part.type() == PatternPart.Type.REGEXP) {
                result.append('(').append(part.value()).append(')');
            } else if (part.type() == PatternPart.Type.SEGMENT_WILDCARD && !customName) {
                result.append('(').append(generateSegmentWildcardRegexp(options)).append(')');
            } else if (part.type() == PatternPart.Type.FULL_WILDCARD) {
                if (!customName
                        && (previousPart == null
                        || previousPart.type() == PatternPart.Type.FIXED_TEXT
                        || previousPart.modifier() != PatternPart.Modifier.NONE
                        || needsGrouping
                        || !part.prefix().isEmpty())) {
                    result.append('*');
                } else {
                    result.append("(.*)");
                }
            }
            if (part.type() == PatternPart.Type.SEGMENT_WILDCARD
                    && customName
                    && !part.suffix().isEmpty()
                    && isValidNameContinuation(part.suffix().codePointAt(0))) {
                result.append('\\');
            }
            result.append(escapePatternString(part.suffix()));
            if (needsGrouping) {
                result.append('}');
            }
            result.append(modifierString(part.modifier()));
        }
        return result.toString();
    }

    /// Generates a regular expression and capture group names.
    static GeneratedRegExp generateRegularExpressionAndNameList(List<PatternPart> parts, PatternOptions options) {
        StringBuilder regexp = new StringBuilder("^");
        ArrayList<String> names = new ArrayList<>();
        @Nullable String segmentWildcardRegexp = null;

        for (PatternPart part : parts) {
            if (part.type() == PatternPart.Type.FIXED_TEXT) {
                if (part.modifier() == PatternPart.Modifier.NONE) {
                    regexp.append(escapeRegexpString(part.value()));
                } else {
                    regexp.append("(?:").append(escapeRegexpString(part.value())).append(')')
                            .append(modifierString(part.modifier()));
                }
                continue;
            }

            names.add(part.name());
            String regexpValue = part.value();
            if (part.type() == PatternPart.Type.SEGMENT_WILDCARD) {
                if (segmentWildcardRegexp == null) {
                    segmentWildcardRegexp = generateSegmentWildcardRegexp(options);
                }
                regexpValue = segmentWildcardRegexp;
            } else if (part.type() == PatternPart.Type.FULL_WILDCARD) {
                regexpValue = ".*";
            }

            if (part.prefix().isEmpty() && part.suffix().isEmpty()) {
                if (part.modifier() == PatternPart.Modifier.NONE
                        || part.modifier() == PatternPart.Modifier.OPTIONAL) {
                    regexp.append('(').append(regexpValue).append(')').append(modifierString(part.modifier()));
                } else {
                    regexp.append("((?:").append(regexpValue).append(')')
                            .append(modifierString(part.modifier())).append(')');
                }
                continue;
            }

            if (part.modifier() == PatternPart.Modifier.NONE || part.modifier() == PatternPart.Modifier.OPTIONAL) {
                regexp.append("(?:")
                        .append(escapeRegexpString(part.prefix()))
                        .append('(').append(regexpValue).append(')')
                        .append(escapeRegexpString(part.suffix()))
                        .append(')').append(modifierString(part.modifier()));
                continue;
            }

            regexp.append("(?:")
                    .append(escapeRegexpString(part.prefix()))
                    .append("((?:").append(regexpValue).append(")(?:")
                    .append(escapeRegexpString(part.suffix()))
                    .append(escapeRegexpString(part.prefix()))
                    .append("(?:").append(regexpValue).append("))*)")
                    .append(escapeRegexpString(part.suffix()))
                    .append(')');
            if (part.modifier() == PatternPart.Modifier.ZERO_OR_MORE) {
                regexp.append('?');
            }
        }
        regexp.append('$');
        return new GeneratedRegExp(regexp.toString(), names);
    }

    /// Escapes a string for pattern serialization.
    static String escapePatternString(String input) {
        StringBuilder result = null;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (shouldEscapePatternChar(c)) {
                if (result == null) {
                    result = new StringBuilder(input.length() + 1);
                    result.append(input, 0, i);
                }
                result.append('\\');
            }
            if (result != null) {
                result.append(c);
            }
        }
        return result == null ? input : result.toString();
    }

    /// Escapes a string for Java regular expressions.
    static String escapeRegexpString(String input) {
        StringBuilder result = null;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (shouldEscapeRegexpChar(c)) {
                if (result == null) {
                    result = new StringBuilder(input.length() + 1);
                    result.append(input, 0, i);
                }
                result.append('\\');
            }
            if (result != null) {
                result.append(c);
            }
        }
        return result == null ? input : result.toString();
    }

    /// Returns whether a character needs escaping in a pattern string.
    private static boolean shouldEscapePatternChar(char c) {
        return c == '+' || c == '*' || c == '?' || c == ':' || c == '{'
                || c == '}' || c == '(' || c == ')' || c == '\\';
    }

    /// Returns whether a character needs escaping in a Java regular expression.
    private static boolean shouldEscapeRegexpChar(char c) {
        return c == '.' || c == '+' || c == '*' || c == '?' || c == '^' || c == '$'
                || c == '{' || c == '}' || c == '(' || c == ')' || c == '[' || c == ']'
                || c == '|' || c == '/' || c == '\\';
    }

    /// Generates the segment wildcard regular expression for component options.
    static String generateSegmentWildcardRegexp(PatternOptions options) {
        return options.delimiter().isEmpty() ? ".+?" : "[^" + escapeRegexpString(options.delimiter()) + "]+?";
    }

    /// Converts a part modifier to its pattern syntax.
    static String modifierString(PatternPart.Modifier modifier) {
        return switch (modifier) {
            case ZERO_OR_MORE -> "*";
            case OPTIONAL -> "?";
            case ONE_OR_MORE -> "+";
            case NONE -> "";
        };
    }

    /// Returns whether a code point can continue a URLPattern group name.
    private static boolean isValidNameContinuation(int codePoint) {
        return codePoint == '_' || codePoint == '$' || Character.isUnicodeIdentifierPart(codePoint);
    }

    /// Generated regular expression and capture group names.
    record GeneratedRegExp(String regexp, List<String> names) {
    }
}
