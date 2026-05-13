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

import java.util.List;

/// Parses shorthand URLPattern constructor strings into component patterns.
@NotNullByDefault
final class ConstructorStringParser {
    /// Original input string.
    private final String input;
    /// Lenient token list.
    private final List<PatternTokenizer.Token> tokens;
    /// Result init.
    private final URLPatternInit result = new URLPatternInit();
    /// Start token index for the current component.
    private int componentStart;
    /// Current token index.
    private int tokenIndex;
    /// Current group nesting depth.
    private int groupDepth;
    /// IPv6 bracket nesting depth while parsing hostname.
    private int hostnameIpv6BracketDepth;
    /// Whether the parsed protocol pattern can match a special scheme.
    private boolean protocolMatchesSpecialScheme;
    /// Current parser state.
    private State state = State.INIT;

    /// Creates a constructor string parser.
    private ConstructorStringParser(String input, List<PatternTokenizer.Token> tokens) {
        this.input = input;
        this.tokens = tokens;
    }

    /// Parses a shorthand pattern string.
    static URLPatternInit parse(String input) {
        ConstructorStringParser parser = new ConstructorStringParser(
                input,
                PatternTokenizer.tokenize(input, PatternTokenizer.Policy.LENIENT)
        );
        parser.run();
        return parser.result;
    }

    /// Runs the constructor string parser.
    private void run() {
        while (tokenIndex < tokens.size()) {
            PatternTokenizer.Token token = tokens.get(tokenIndex);
            if (token.type() == PatternTokenizer.TokenType.END) {
                handleEndToken();
                continue;
            }

            if (groupDepth > 0) {
                if (token.type() == PatternTokenizer.TokenType.OPEN) {
                    groupDepth++;
                } else if (token.type() == PatternTokenizer.TokenType.CLOSE) {
                    groupDepth--;
                }
                tokenIndex++;
                continue;
            }
            if (token.type() == PatternTokenizer.TokenType.OPEN) {
                groupDepth = 1;
                tokenIndex++;
                continue;
            }

            switch (state) {
                case INIT -> {
                    if (isProtocolSuffix()) {
                        rewind();
                        changeState(State.PROTOCOL, 0);
                    }
                }
                case PROTOCOL -> {
                    if (isProtocolSuffix()) {
                        computeProtocolMatchesSpecialSchemeFlag();
                        State nextState = State.PATHNAME;
                        int skip = 1;
                        if (nextIsAuthoritySlashes()) {
                            nextState = State.AUTHORITY;
                            skip = 3;
                        } else if (protocolMatchesSpecialScheme) {
                            nextState = State.AUTHORITY;
                        }
                        changeState(nextState, skip);
                    }
                }
                case AUTHORITY -> {
                    if (isIdentityTerminator()) {
                        rewind();
                        if (isPasswordPrefix()) {
                            result.username = "";
                            changeState(State.PASSWORD, 1);
                        } else {
                            changeState(State.USERNAME, 0);
                        }
                    } else if (isPathnameStart() || isSearchPrefix() || isHashPrefix()) {
                        if (tokenIndex == componentStart || isNonSpecialPatternChar(componentStart, '/')) {
                            if (tokenIndex != componentStart) {
                                tokenIndex = componentStart;
                            }
                            result.hostname = "";
                            if (isPathnameStart()) {
                                changeState(State.PATHNAME, 0);
                            } else if (isSearchPrefix()) {
                                changeState(State.SEARCH, 1);
                            } else {
                                changeState(State.HASH, 1);
                            }
                        } else {
                            rewind();
                            changeState(State.HOSTNAME, 0);
                        }
                    }
                }
                case USERNAME -> {
                    if (isPasswordPrefix()) {
                        changeState(State.PASSWORD, 1);
                    } else if (isIdentityTerminator()) {
                        changeState(State.HOSTNAME, 1);
                    }
                }
                case PASSWORD -> {
                    if (isIdentityTerminator()) {
                        changeState(State.HOSTNAME, 1);
                    }
                }
                case HOSTNAME -> {
                    if (isIpv6Open()) {
                        hostnameIpv6BracketDepth++;
                    } else if (isIpv6Close()) {
                        hostnameIpv6BracketDepth--;
                    } else if (isPortPrefix() && hostnameIpv6BracketDepth == 0) {
                        changeState(State.PORT, 1);
                    } else if (isPathnameStart()) {
                        changeState(State.PATHNAME, 0);
                    } else if (isSearchPrefix()) {
                        changeState(State.SEARCH, 1);
                    } else if (isHashPrefix()) {
                        changeState(State.HASH, 1);
                    }
                }
                case PORT -> {
                    if (isPathnameStart()) {
                        changeState(State.PATHNAME, 0);
                    } else if (isSearchPrefix()) {
                        changeState(State.SEARCH, 1);
                    } else if (isHashPrefix()) {
                        changeState(State.HASH, 1);
                    }
                }
                case PATHNAME -> {
                    if (isSearchPrefix()) {
                        changeState(State.SEARCH, 1);
                    } else if (isHashPrefix()) {
                        changeState(State.HASH, 1);
                    }
                }
                case SEARCH -> {
                    if (isHashPrefix()) {
                        changeState(State.HASH, 1);
                    }
                }
                case HASH -> {
                }
                case DONE -> throw new WebURLPatternSyntaxException("Unexpected URLPattern constructor state");
            }
            tokenIndex++;
        }

        if (result.hostname != null && result.port == null) {
            result.port = "";
        }
    }

    /// Handles the end token.
    private void handleEndToken() {
        if (state == State.INIT) {
            rewind();
            if (isHashPrefix()) {
                changeState(State.HASH, 1);
            } else if (isSearchPrefix()) {
                changeState(State.SEARCH, 1);
            } else {
                changeState(State.PATHNAME, 0);
            }
            tokenIndex++;
            return;
        }
        if (state == State.AUTHORITY) {
            rewind();
            changeState(State.HOSTNAME, 0);
            tokenIndex++;
            return;
        }
        changeState(State.DONE, 0);
        tokenIndex = tokens.size();
    }

    /// Rewinds to the current component start.
    private void rewind() {
        tokenIndex = componentStart;
    }

    /// Changes parser state and stores the current component.
    private void changeState(State newState, int skip) {
        if (state != State.INIT && state != State.AUTHORITY && state != State.DONE) {
            setComponent(state, makeComponentString());
        }

        if (state != State.INIT && newState != State.DONE
                && (state == State.PROTOCOL || state == State.AUTHORITY
                || state == State.USERNAME || state == State.PASSWORD)
                && (newState == State.PORT || newState == State.PATHNAME
                || newState == State.SEARCH || newState == State.HASH)
                && result.hostname == null) {
            result.hostname = "";
        }

        if ((state == State.PROTOCOL || state == State.AUTHORITY
                || state == State.USERNAME || state == State.PASSWORD
                || state == State.HOSTNAME || state == State.PORT)
                && (newState == State.SEARCH || newState == State.HASH)
                && result.pathname == null) {
            result.pathname = protocolMatchesSpecialScheme ? "/" : "";
        }

        if ((state == State.PROTOCOL || state == State.AUTHORITY
                || state == State.USERNAME || state == State.PASSWORD
                || state == State.HOSTNAME || state == State.PORT || state == State.PATHNAME)
                && newState == State.HASH && result.search == null) {
            result.search = "";
        }

        int nextComponentStart = tokenIndex + skip;
        hostnameIpv6BracketDepth = newState == State.HOSTNAME && isNonSpecialPatternChar(nextComponentStart, '[')
                ? 1
                : 0;

        state = newState;
        tokenIndex = nextComponentStart;
        componentStart = tokenIndex;
    }

    /// Stores a component on the result init.
    private void setComponent(State component, String value) {
        switch (component) {
            case PROTOCOL -> result.protocol = value;
            case USERNAME -> result.username = value;
            case PASSWORD -> result.password = value;
            case HOSTNAME -> result.hostname = value;
            case PORT -> result.port = value;
            case PATHNAME -> result.pathname = value;
            case SEARCH -> result.search = value;
            case HASH -> result.hash = value;
            default -> throw new WebURLPatternSyntaxException("Invalid URLPattern constructor component");
        }
    }

    /// Builds the current component string.
    private String makeComponentString() {
        int endIndex = tokens.get(tokenIndex).index();
        int startIndex = safeToken(componentStart).index();
        return input.substring(startIndex, endIndex);
    }

    /// Computes whether the protocol component can match a special URL scheme.
    private void computeProtocolMatchesSpecialSchemeFlag() {
        String protocolString = makeComponentString();
        PatternComponent protocolComponent = PatternComponent.compile(
                protocolString,
                URLPatternCanonicalizer::canonicalizeProtocol,
                PatternOptions.DEFAULT
        );
        protocolMatchesSpecialScheme = protocolComponent.matchesSpecialScheme();
    }

    /// Returns whether the current token is a hash prefix.
    private boolean isHashPrefix() {
        return isNonSpecialPatternChar(tokenIndex, '#');
    }

    /// Returns whether the current token is a search prefix.
    private boolean isSearchPrefix() {
        if (isNonSpecialPatternChar(tokenIndex, '?')) {
            return true;
        }
        if (!safeToken(tokenIndex).value().equals("?")) {
            return false;
        }
        if (tokenIndex == 0) {
            return true;
        }
        PatternTokenizer.Token previous = safeToken(tokenIndex - 1);
        return previous.type() != PatternTokenizer.TokenType.NAME
                && previous.type() != PatternTokenizer.TokenType.REGEXP
                && previous.type() != PatternTokenizer.TokenType.CLOSE
                && previous.type() != PatternTokenizer.TokenType.ASTERISK;
    }

    /// Returns whether the current token is a protocol suffix.
    private boolean isProtocolSuffix() {
        return isNonSpecialPatternChar(tokenIndex, ':');
    }

    /// Returns whether the next tokens are `//`.
    private boolean nextIsAuthoritySlashes() {
        return isNonSpecialPatternChar(tokenIndex + 1, '/') && isNonSpecialPatternChar(tokenIndex + 2, '/');
    }

    /// Returns whether the current token is an identity terminator.
    private boolean isIdentityTerminator() {
        return isNonSpecialPatternChar(tokenIndex, '@');
    }

    /// Returns whether the current token starts a pathname.
    private boolean isPathnameStart() {
        return isNonSpecialPatternChar(tokenIndex, '/');
    }

    /// Returns whether the current token starts a password.
    private boolean isPasswordPrefix() {
        return isNonSpecialPatternChar(tokenIndex, ':');
    }

    /// Returns whether the current token opens an IPv6 host.
    private boolean isIpv6Open() {
        return isNonSpecialPatternChar(tokenIndex, '[');
    }

    /// Returns whether the current token closes an IPv6 host.
    private boolean isIpv6Close() {
        return isNonSpecialPatternChar(tokenIndex, ']');
    }

    /// Returns whether the current token starts a port.
    private boolean isPortPrefix() {
        return isNonSpecialPatternChar(tokenIndex, ':');
    }

    /// Returns whether a token is a non-special pattern character with the supplied value.
    private boolean isNonSpecialPatternChar(int index, int value) {
        PatternTokenizer.Token token = safeToken(index);
        if (!token.value().isEmpty() && token.value().codePointAt(0) != value) {
            return false;
        }
        return token.type() == PatternTokenizer.TokenType.CHAR
                || token.type() == PatternTokenizer.TokenType.ESCAPED_CHAR
                || token.type() == PatternTokenizer.TokenType.INVALID_CHAR;
    }

    /// Returns a token or the final end token when the index is out of range.
    private PatternTokenizer.Token safeToken(int index) {
        if (index >= 0 && index < tokens.size()) {
            return tokens.get(index);
        }
        return tokens.get(tokens.size() - 1);
    }

    /// Constructor string parser state.
    private enum State {
        /// Initial state.
        INIT,
        /// Protocol state.
        PROTOCOL,
        /// Authority state.
        AUTHORITY,
        /// Username state.
        USERNAME,
        /// Password state.
        PASSWORD,
        /// Hostname state.
        HOSTNAME,
        /// Port state.
        PORT,
        /// Pathname state.
        PATHNAME,
        /// Search state.
        SEARCH,
        /// Hash state.
        HASH,
        /// Done state.
        DONE
    }
}
