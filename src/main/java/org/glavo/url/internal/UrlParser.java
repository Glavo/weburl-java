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
package org.glavo.url.internal;

import org.glavo.url.WebURLParseException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/// WHATWG URL parser, serializer, and related internal algorithms.
@NotNullByDefault
public final class UrlParser {
    /// End-of-file marker used by the parser loop.
    private static final int EOF = -1;

    /// Creates no instances.
    private UrlParser() {
    }

    /// Parses a URL without a base URL.
    public static @Nullable UrlRecord parseUrl(String input) {
        return basicParse(input, null, null, null);
    }

    /// Parses a URL with an optional base URL.
    public static @Nullable UrlRecord parseUrl(String input, @Nullable UrlRecord baseUrl) {
        return basicParse(input, baseUrl, null, null);
    }

    /// Runs the basic URL parser.
    public static @Nullable UrlRecord basicParse(
            String input,
            @Nullable UrlRecord baseUrl,
            @Nullable UrlRecord url,
            @Nullable State stateOverride
    ) {
        return basicParseResult(input, baseUrl, url, stateOverride).url();
    }

    /// Runs the basic URL parser and returns the parse error when parsing fails.
    public static ParseResult basicParseResult(
            String input,
            @Nullable UrlRecord baseUrl,
            @Nullable UrlRecord url,
            @Nullable State stateOverride
    ) {
        StateMachine stateMachine = new StateMachine(input, baseUrl, url, stateOverride);
        return new ParseResult(
                stateMachine.failure ? null : stateMachine.url,
                stateMachine.failure ? stateMachine.validationError : null);
    }

    /// The result of a basic URL parser run.
    @NotNullByDefault
    public static final class ParseResult {
        /// The parsed URL record, or `null` when parsing failed.
        private final @Nullable UrlRecord url;
        /// The validation error that caused failure, or `null` when unavailable.
        private final @Nullable WebURLParseException error;

        /// Creates a parser result.
        private ParseResult(@Nullable UrlRecord url, @Nullable WebURLParseException error) {
            this.url = url;
            this.error = error;
        }

        /// Returns the parsed URL record, or `null` when parsing failed.
        public @Nullable UrlRecord url() {
            return url;
        }

        /// Returns the validation error that caused failure, or `null` when unavailable.
        public @Nullable WebURLParseException error() {
            return error;
        }
    }

    /// Serializes a URL record.
    public static String serializeUrl(UrlRecord url) {
        return serializeUrl(url, false);
    }

    /// Serializes a URL record, optionally excluding the fragment.
    public static String serializeUrl(UrlRecord url, boolean excludeFragment) {
        StringBuilder output = new StringBuilder();
        output.append(url.scheme).append(':');
        if (url.host != null) {
            output.append("//");
            if (!url.username.isEmpty() || !url.password.isEmpty()) {
                output.append(url.username);
                if (!url.password.isEmpty()) {
                    output.append(':').append(url.password);
                }
                output.append('@');
            }
            output.append(serializeHost(url.host));
            if (url.port != -1) {
                output.append(':').append(url.port);
            }
        }

        if (url.host == null && !url.hasOpaquePath() && url.path.size() > 1 && url.path.get(0).isEmpty()) {
            output.append("/.");
        }
        output.append(serializePath(url));

        if (url.query != null) {
            output.append('?').append(url.query);
        }
        if (!excludeFragment && url.fragment != null) {
            output.append('#').append(url.fragment);
        }
        return output.toString();
    }

    /// Serializes a URL host.
    public static String serializeHost(UrlHost host) {
        return host.serialize();
    }

    /// Serializes a URL path.
    public static String serializePath(UrlRecord url) {
        if (url.hasOpaquePath()) {
            return url.opaquePath == null ? "" : url.opaquePath;
        }

        StringBuilder output = new StringBuilder();
        for (String segment : url.path) {
            output.append('/').append(segment);
        }
        return output.toString();
    }

    /// Serializes a URL origin.
    public static String serializeOrigin(UrlRecord url) {
        switch (url.scheme) {
            case "blob":
                UrlRecord pathUrl = parseUrl(serializePath(url));
                if (pathUrl == null || (!pathUrl.scheme.equals("http") && !pathUrl.scheme.equals("https"))) {
                    return "null";
                }
                return serializeOrigin(pathUrl);
            case "ftp":
            case "http":
            case "https":
            case "ws":
            case "wss":
                if (url.host == null) {
                    return "null";
                }
                return serializeTupleOrigin(url.scheme, url.host, url.port);
            case "file":
            default:
                return "null";
        }
    }

    /// Sets the username of a URL record.
    public static void setTheUsername(UrlRecord url, String username) {
        url.username = PercentEncoding.utf8PercentEncodeString(username, PercentEncoding::isUserinfoPercentEncode);
    }

    /// Sets the password of a URL record.
    public static void setThePassword(UrlRecord url, String password) {
        url.password = PercentEncoding.utf8PercentEncodeString(password, PercentEncoding::isUserinfoPercentEncode);
    }

    /// Returns whether the URL cannot have credentials or a port.
    public static boolean cannotHaveAUsernamePasswordPort(UrlRecord url) {
        return url.host == null || url.host.isEmptyDomain() || url.scheme.equals("file");
    }

    /// Returns whether the scheme is a special URL scheme.
    public static boolean isSpecialScheme(String scheme) {
        switch (scheme) {
            case "ftp":
            case "file":
            case "http":
            case "https":
            case "ws":
            case "wss":
                return true;
            default:
                return false;
        }
    }

    /// Returns the default port for a special scheme.
    static int defaultPort(String scheme) {
        switch (scheme) {
            case "ftp":
                return 21;
            case "http":
            case "ws":
                return 80;
            case "https":
            case "wss":
                return 443;
            default:
                return -1;
        }
    }

    /// Serializes a tuple origin.
    private static String serializeTupleOrigin(String scheme, UrlHost host, int port) {
        StringBuilder output = new StringBuilder();
        output.append(scheme).append("://").append(serializeHost(host));
        if (port != -1) {
            output.append(':').append(port);
        }
        return output.toString();
    }

    /// Parses a host string.
    private static UrlHost parseHost(String input, boolean opaque) {
        if (input.startsWith("[")) {
            if (!input.endsWith("]")) {
                throw new WebURLParseException.IPv6Unclosed();
            }
            int[] address = parseIpv6(input.substring(1, input.length() - 1));
            return UrlHost.ipv6(address);
        }

        if (opaque) {
            return parseOpaqueHost(input);
        }

        String domain = Encoding.utf8DecodeWithoutBom(PercentEncoding.percentDecodeString(input));
        String asciiDomain = domainToAscii(domain, false);

        if (endsInANumber(asciiDomain)) {
            return UrlHost.ipv4(parseIpv4(asciiDomain));
        }

        return UrlHost.domain(asciiDomain);
    }

    /// Parses an opaque host.
    private static UrlHost parseOpaqueHost(String input) {
        if (containsForbiddenHostCodePoint(input)) {
            throw new WebURLParseException.HostInvalidCodePoint();
        }
        return UrlHost.opaque(PercentEncoding.utf8PercentEncodeString(
                input, PercentEncoding::isC0ControlPercentEncode));
    }

    /// Converts a domain to ASCII.
    private static String domainToAscii(String domain, boolean strict) {
        String result = IdnaProcessor.toAscii(domain, strict);
        if (result == null) {
            throw new WebURLParseException.DomainToASCII();
        }
        if (!strict) {
            if (result.isEmpty()) {
                throw new WebURLParseException.DomainToASCII();
            }
            if (containsForbiddenDomainCodePoint(result)) {
                throw new WebURLParseException.DomainInvalidCodePoint();
            }
        }
        return result;
    }

    /// Returns whether the domain ends in a numeric label.
    private static boolean endsInANumber(String input) {
        List<String> parts = new ArrayList<>(Arrays.asList(input.split("\\.", -1)));
        if (parts.get(parts.size() - 1).isEmpty()) {
            if (parts.size() == 1) {
                return false;
            }
            parts.remove(parts.size() - 1);
        }

        String last = parts.get(parts.size() - 1);
        if (parseIpv4Number(last) != null) {
            return true;
        }
        for (int i = 0; i < last.length(); i++) {
            if (!Infra.isAsciiDigit(last.charAt(i))) {
                return false;
            }
        }
        return !last.isEmpty();
    }

    /// Parses an IPv4 number.
    private static @Nullable Long parseIpv4Number(String input) {
        if (input.isEmpty()) {
            return null;
        }

        int radix = 10;
        String value = input;
        if (value.length() >= 2 && value.charAt(0) == '0' && (value.charAt(1) == 'x' || value.charAt(1) == 'X')) {
            value = value.substring(2);
            radix = 16;
        } else if (value.length() >= 2 && value.charAt(0) == '0') {
            value = value.substring(1);
            radix = 8;
        }

        if (value.isEmpty()) {
            return 0L;
        }

        for (int i = 0; i < value.length(); i++) {
            int c = value.charAt(i);
            boolean ok = radix == 10 ? Infra.isAsciiDigit(c)
                    : radix == 16 ? Infra.isAsciiHex(c)
                      : c >= '0' && c <= '7';
            if (!ok) {
                return null;
            }
        }

        try {
            return Long.parseLong(value, radix);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /// Parses an IPv4 address.
    private static long parseIpv4(String input) {
        List<String> parts = new ArrayList<>(Arrays.asList(input.split("\\.", -1)));
        if (parts.get(parts.size() - 1).isEmpty() && parts.size() > 1) {
            parts.remove(parts.size() - 1);
        }
        if (parts.size() > 4) {
            throw new WebURLParseException.IPv4TooManyParts();
        }

        List<Long> numbers = new ArrayList<>();
        for (String part : parts) {
            Long number = parseIpv4Number(part);
            if (number == null) {
                throw new WebURLParseException.IPv4NonNumericPart();
            }
            numbers.add(number);
        }

        for (int i = 0; i < numbers.size() - 1; i++) {
            if (numbers.get(i) > 255) {
                throw new WebURLParseException.IPv4OutOfRangePart();
            }
        }

        long lastLimit = 1L << (8 * (5 - numbers.size()));
        if (numbers.get(numbers.size() - 1) >= lastLimit) {
            throw new WebURLParseException.IPv4OutOfRangePart();
        }

        long ipv4 = numbers.remove(numbers.size() - 1);
        int counter = 0;
        for (long number : numbers) {
            ipv4 += number << (8 * (3 - counter));
            counter++;
        }
        return ipv4;
    }

    /// Parses an IPv6 address.
    private static int[] parseIpv6(String inputString) {
        int[] address = new int[8];
        int pieceIndex = 0;
        int compress = -1;
        int pointer = 0;
        int[] input = inputString.codePoints().toArray();

        if (codePoint(input, pointer) == ':') {
            if (codePoint(input, pointer + 1) != ':') {
                throw new WebURLParseException.IPv6InvalidCompression();
            }
            pointer += 2;
            pieceIndex++;
            compress = pieceIndex;
        }

        while (pointer < input.length) {
            if (pieceIndex == 8) {
                throw new WebURLParseException.IPv6TooManyPieces();
            }

            if (codePoint(input, pointer) == ':') {
                if (compress != -1) {
                    throw new WebURLParseException.IPv6MultipleCompression();
                }
                pointer++;
                pieceIndex++;
                compress = pieceIndex;
                continue;
            }

            int value = 0;
            int length = 0;
            while (length < 4 && Infra.isAsciiHex(codePoint(input, pointer))) {
                value = value * 0x10 + Character.digit(codePoint(input, pointer), 16);
                pointer++;
                length++;
            }

            if (codePoint(input, pointer) == '.') {
                if (length == 0) {
                    throw new WebURLParseException.IPv4InIPv6InvalidCodePoint();
                }
                if (pieceIndex > 6) {
                    throw new WebURLParseException.IPv4InIPv6TooManyPieces();
                }
                pointer -= length;
                int numbersSeen = 0;
                while (codePoint(input, pointer) != EOF) {
                    Integer ipv4Piece = null;
                    if (numbersSeen > 0) {
                        if (codePoint(input, pointer) == '.' && numbersSeen < 4) {
                            pointer++;
                        } else {
                            throw new WebURLParseException.IPv4InIPv6InvalidCodePoint();
                        }
                    }
                    if (!Infra.isAsciiDigit(codePoint(input, pointer))) {
                        throw new WebURLParseException.IPv4InIPv6InvalidCodePoint();
                    }
                    while (Infra.isAsciiDigit(codePoint(input, pointer))) {
                        int number = Character.digit(codePoint(input, pointer), 10);
                        if (ipv4Piece == null) {
                            ipv4Piece = number;
                        } else if (ipv4Piece == 0) {
                            throw new WebURLParseException.IPv4InIPv6InvalidCodePoint();
                        } else {
                            ipv4Piece = ipv4Piece * 10 + number;
                        }
                        if (ipv4Piece > 255) {
                            throw new WebURLParseException.IPv4InIPv6OutOfRangePart();
                        }
                        pointer++;
                    }
                    address[pieceIndex] = address[pieceIndex] * 0x100 + ipv4Piece;
                    numbersSeen++;
                    if (numbersSeen == 2 || numbersSeen == 4) {
                        pieceIndex++;
                    }
                }
                if (numbersSeen != 4) {
                    throw new WebURLParseException.IPv4InIPv6TooFewParts();
                }
                break;
            } else if (codePoint(input, pointer) == ':') {
                pointer++;
                if (codePoint(input, pointer) == EOF) {
                    throw new WebURLParseException.IPv6InvalidCodePoint();
                }
            } else if (codePoint(input, pointer) != EOF) {
                throw new WebURLParseException.IPv6InvalidCodePoint();
            }

            address[pieceIndex] = value;
            pieceIndex++;
        }

        if (compress != -1) {
            int swaps = pieceIndex - compress;
            pieceIndex = 7;
            while (pieceIndex != 0 && swaps > 0) {
                int temp = address[compress + swaps - 1];
                address[compress + swaps - 1] = address[pieceIndex];
                address[pieceIndex] = temp;
                pieceIndex--;
                swaps--;
            }
        } else if (pieceIndex != 8) {
            throw new WebURLParseException.IPv6TooFewPieces();
        }

        return address;
    }

    /// Returns whether a string is a single-dot path segment.
    private static boolean isSingleDot(String buffer) {
        return buffer.equals(".") || buffer.equalsIgnoreCase("%2e");
    }

    /// Returns whether a string is a double-dot path segment.
    private static boolean isDoubleDot(String buffer) {
        String lower = buffer.toLowerCase(Locale.ROOT);
        return lower.equals("..") || lower.equals("%2e.") || lower.equals(".%2e") || lower.equals("%2e%2e");
    }

    /// Returns whether two code points form a Windows drive letter.
    private static boolean isWindowsDriveLetterCodePoints(int cp1, int cp2) {
        return Infra.isAsciiAlpha(cp1) && (cp2 == ':' || cp2 == '|');
    }

    /// Returns whether the string is a Windows drive letter.
    private static boolean isWindowsDriveLetterString(String string) {
        return string.length() == 2
                && Infra.isAsciiAlpha(string.charAt(0))
                && (string.charAt(1) == ':' || string.charAt(1) == '|');
    }

    /// Returns whether the string is a normalized Windows drive letter.
    private static boolean isNormalizedWindowsDriveLetterString(String string) {
        return string.length() == 2 && Infra.isAsciiAlpha(string.charAt(0)) && string.charAt(1) == ':';
    }

    /// Returns whether a string contains a forbidden host code point.
    private static boolean containsForbiddenHostCodePoint(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == 0 || c == '\t' || c == '\n' || c == '\r' || c == ' ' || c == '#'
                    || c == '/' || c == ':' || c == '<' || c == '>' || c == '?' || c == '@'
                    || c == '[' || c == '\\' || c == ']' || c == '^' || c == '|') {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a string contains a forbidden domain code point.
    private static boolean containsForbiddenDomainCodePoint(String string) {
        if (containsForbiddenHostCodePoint(string)) {
            return true;
        }
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c <= 0x1f || c == '%' || c == 0x7f) {
                return true;
            }
        }
        return false;
    }

    /// Trims leading and trailing C0 controls and spaces.
    private static String trimControlChars(String string) {
        int start = 0;
        int end = string.length();
        while (start < end && string.charAt(start) <= 0x20) {
            start++;
        }
        while (end > start && string.charAt(end - 1) <= 0x20) {
            end--;
        }
        return string.substring(start, end);
    }

    /// Removes ASCII tabs and newlines.
    private static String trimTabAndNewline(String url) {
        StringBuilder output = new StringBuilder(url.length());
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c != '\t' && c != '\n' && c != '\r') {
                output.append(c);
            }
        }
        return output.toString();
    }

    /// Shortens the path of a URL record.
    private static void shortenPath(UrlRecord url) {
        if (url.path.isEmpty()) {
            return;
        }
        if (url.scheme.equals("file") && url.path.size() == 1 && isNormalizedWindowsDriveLetterString(url.path.get(0))) {
            return;
        }
        url.path.remove(url.path.size() - 1);
    }

    /// Returns whether a URL includes credentials.
    private static boolean includesCredentials(UrlRecord url) {
        return !url.username.isEmpty() || !url.password.isEmpty();
    }

    /// Returns whether the URL has a special scheme.
    private static boolean isSpecial(UrlRecord url) {
        return isSpecialScheme(url.scheme);
    }

    /// Returns whether the URL has a non-special scheme.
    private static boolean isNotSpecial(UrlRecord url) {
        return !isSpecial(url);
    }

    /// Returns the code point at an index or EOF.
    private static int codePoint(int[] input, int index) {
        return index >= 0 && index < input.length ? input[index] : EOF;
    }

    /// Returns the number of code points in a string.
    private static int countSymbols(String string) {
        return string.codePointCount(0, string.length());
    }

    /// Returns whether input starts with a Windows drive letter at the pointer.
    private static boolean startsWithWindowsDriveLetter(int[] input, int pointer) {
        int length = input.length - pointer;
        return length >= 2
                && isWindowsDriveLetterCodePoints(codePoint(input, pointer), codePoint(input, pointer + 1))
                && (length == 2 || codePoint(input, pointer + 2) == '/'
                || codePoint(input, pointer + 2) == '\\'
                || codePoint(input, pointer + 2) == '?'
                || codePoint(input, pointer + 2) == '#');
    }

    /// Parser states that can also be used as state overrides.
    @NotNullByDefault
    public enum State {
        /// Scheme start state.
        SCHEME_START,
        /// Scheme state.
        SCHEME,
        /// No scheme state.
        NO_SCHEME,
        /// Special relative or authority state.
        SPECIAL_RELATIVE_OR_AUTHORITY,
        /// Path or authority state.
        PATH_OR_AUTHORITY,
        /// Relative state.
        RELATIVE,
        /// Relative slash state.
        RELATIVE_SLASH,
        /// Special authority slashes state.
        SPECIAL_AUTHORITY_SLASHES,
        /// Special authority ignore slashes state.
        SPECIAL_AUTHORITY_IGNORE_SLASHES,
        /// Authority state.
        AUTHORITY,
        /// Host state.
        HOST,
        /// Hostname state.
        HOSTNAME,
        /// Port state.
        PORT,
        /// File state.
        FILE,
        /// File slash state.
        FILE_SLASH,
        /// File host state.
        FILE_HOST,
        /// Path start state.
        PATH_START,
        /// Path state.
        PATH,
        /// Opaque path state.
        OPAQUE_PATH,
        /// Query state.
        QUERY,
        /// Fragment state.
        FRAGMENT
    }

    /// Parser transition result.
    @NotNullByDefault
    private enum Result {
        /// Continue parsing.
        CONTINUE,
        /// Stop successfully.
        STOP,
        /// Stop with failure.
        FAILURE
    }

    /// One run of the URL state machine.
    @NotNullByDefault
    private static final class StateMachine {
        /// Input pointer.
        private int pointer;
        /// Parsed input code points.
        private int[] input;
        /// Base URL.
        private final @Nullable UrlRecord base;
        /// State override.
        private final @Nullable State stateOverride;
        /// URL record being built or modified.
        private final UrlRecord url;
        /// Whether parsing failed.
        private boolean failure;
        /// The most recent validation error recorded by this parser run.
        private @Nullable WebURLParseException validationError;
        /// Current parser state.
        private State state;
        /// Temporary parser buffer.
        private String buffer = "";
        /// Whether an at-sign was seen in authority.
        private boolean atSignSeen;
        /// Whether the host parser is inside IPv6 brackets.
        private boolean insideBrackets;
        /// Whether userinfo parsing reached the password token.
        private boolean passwordTokenSeen;

        /// Creates and runs the state machine.
        StateMachine(String inputText, @Nullable UrlRecord base, @Nullable UrlRecord url, @Nullable State stateOverride) {
            this.pointer = 0;
            this.base = base;
            this.stateOverride = stateOverride;
            this.url = url == null ? new UrlRecord() : url;
            this.state = stateOverride == null ? State.SCHEME_START : stateOverride;

            String text = inputText;
            if (url == null) {
                String trimmed = trimControlChars(text);
                if (!trimmed.equals(text)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                text = trimmed;
            }

            String withoutTabsAndNewlines = trimTabAndNewline(text);
            if (!withoutTabsAndNewlines.equals(text)) {
                recordValidationError(new WebURLParseException.InvalidURLUnit());
            }
            this.input = withoutTabsAndNewlines.codePoints().toArray();

            run();
        }

        /// Runs the parser loop.
        private void run() {
            try {
                for (; pointer <= input.length; pointer++) {
                    int c = pointer == input.length ? EOF : input[pointer];
                    String cStr = c == EOF ? "" : new String(Character.toChars(c));
                    Result result = execute(c, cStr);
                    if (result == Result.STOP) {
                        break;
                    }
                    if (result == Result.FAILURE) {
                        failure = true;
                        break;
                    }
                }
            } catch (WebURLParseException exception) {
                validationError = exception;
                failure = true;
            }
        }

        /// Records a validation error without forcing parser failure.
        private void recordValidationError(WebURLParseException error) {
            validationError = error;
        }

        /// Records a validation error and returns a parser failure result.
        private Result fail(WebURLParseException error) {
            validationError = error;
            return Result.FAILURE;
        }

        /// Returns a parser failure result without a corresponding public validation error.
        private Result failApiValidation() {
            return Result.FAILURE;
        }

        /// Returns a parser failure for `host-missing`.
        private Result failHostMissing() {
            return fail(new WebURLParseException.HostMissing());
        }

        /// Executes the current parser state.
        private Result execute(int c, String cStr) {
            switch (state) {
                case SCHEME_START:
                    return parseSchemeStart(c, cStr);
                case SCHEME:
                    return parseScheme(c, cStr);
                case NO_SCHEME:
                    return parseNoScheme(c);
                case SPECIAL_RELATIVE_OR_AUTHORITY:
                    return parseSpecialRelativeOrAuthority(c);
                case PATH_OR_AUTHORITY:
                    return parsePathOrAuthority(c);
                case RELATIVE:
                    return parseRelative(c);
                case RELATIVE_SLASH:
                    return parseRelativeSlash(c);
                case SPECIAL_AUTHORITY_SLASHES:
                    return parseSpecialAuthoritySlashes(c);
                case SPECIAL_AUTHORITY_IGNORE_SLASHES:
                    return parseSpecialAuthorityIgnoreSlashes(c);
                case AUTHORITY:
                    return parseAuthority(c, cStr);
                case HOST:
                case HOSTNAME:
                    return parseHostName(c, cStr);
                case PORT:
                    return parsePort(c, cStr);
                case FILE:
                    return parseFile(c);
                case FILE_SLASH:
                    return parseFileSlash(c);
                case FILE_HOST:
                    return parseFileHost(c, cStr);
                case PATH_START:
                    return parsePathStart(c);
                case PATH:
                    return parsePath(c);
                case OPAQUE_PATH:
                    return parseOpaquePath(c);
                case QUERY:
                    return parseQuery(c, cStr);
                case FRAGMENT:
                    return parseFragment(c);
                default:
                    throw new AssertionError(state);
            }
        }

        /// Parses the scheme start state.
        private Result parseSchemeStart(int c, String cStr) {
            if (Infra.isAsciiAlpha(c)) {
                buffer += cStr.toLowerCase(Locale.ROOT);
                state = State.SCHEME;
            } else if (stateOverride == null) {
                state = State.NO_SCHEME;
                pointer--;
            } else {
                return failApiValidation();
            }
            return Result.CONTINUE;
        }

        /// Parses the scheme state.
        private Result parseScheme(int c, String cStr) {
            if (Infra.isAsciiAlphanumeric(c) || c == '+' || c == '-' || c == '.') {
                buffer += cStr.toLowerCase(Locale.ROOT);
            } else if (c == ':') {
                if (stateOverride != null) {
                    if (isSpecial(url) && !isSpecialScheme(buffer)) {
                        return Result.STOP;
                    }
                    if (!isSpecial(url) && isSpecialScheme(buffer)) {
                        return Result.STOP;
                    }
                    if ((includesCredentials(url) || url.port != -1) && buffer.equals("file")) {
                        return Result.STOP;
                    }
                    if (url.scheme.equals("file") && url.host != null && url.host.isEmptyDomain()) {
                        return Result.STOP;
                    }
                }
                url.scheme = buffer;
                if (stateOverride != null) {
                    int defaultPort = defaultPort(url.scheme);
                    if (defaultPort == url.port) {
                        url.port = -1;
                    }
                    return Result.STOP;
                }
                buffer = "";
                if (url.scheme.equals("file")) {
                    if (codePoint(input, pointer + 1) != '/' || codePoint(input, pointer + 2) != '/') {
                        recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
                    }
                    state = State.FILE;
                } else if (isSpecial(url) && base != null && base.scheme.equals(url.scheme)) {
                    state = State.SPECIAL_RELATIVE_OR_AUTHORITY;
                } else if (isSpecial(url)) {
                    state = State.SPECIAL_AUTHORITY_SLASHES;
                } else if (codePoint(input, pointer + 1) == '/') {
                    state = State.PATH_OR_AUTHORITY;
                    pointer++;
                } else {
                    url.opaquePath = "";
                    url.path.clear();
                    state = State.OPAQUE_PATH;
                }
            } else if (stateOverride == null) {
                buffer = "";
                state = State.NO_SCHEME;
                pointer = -1;
            } else {
                return failApiValidation();
            }
            return Result.CONTINUE;
        }

        /// Parses the no-scheme state.
        private Result parseNoScheme(int c) {
            if (base == null || (base.hasOpaquePath() && c != '#')) {
                return fail(new WebURLParseException.MissingSchemeNonRelativeURL());
            } else if (base.hasOpaquePath() && c == '#') {
                url.scheme = base.scheme;
                url.opaquePath = base.opaquePath;
                url.path = new ArrayList<>(base.path);
                url.query = base.query;
                url.fragment = "";
                state = State.FRAGMENT;
            } else if (base.scheme.equals("file")) {
                state = State.FILE;
                pointer--;
            } else {
                state = State.RELATIVE;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the special-relative-or-authority state.
        private Result parseSpecialRelativeOrAuthority(int c) {
            if (c == '/' && codePoint(input, pointer + 1) == '/') {
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
                pointer++;
            } else {
                recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
                state = State.RELATIVE;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the path-or-authority state.
        private Result parsePathOrAuthority(int c) {
            if (c == '/') {
                state = State.AUTHORITY;
            } else {
                state = State.PATH;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the relative state.
        private Result parseRelative(int c) {
            if (base == null) {
                return failApiValidation();
            }
            url.scheme = base.scheme;
            if (c == '/') {
                state = State.RELATIVE_SLASH;
            } else if (isSpecial(url) && c == '\\') {
                recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                state = State.RELATIVE_SLASH;
            } else {
                url.username = base.username;
                url.password = base.password;
                url.host = base.host;
                url.port = base.port;
                url.path = new ArrayList<>(base.path);
                url.opaquePath = base.opaquePath;
                url.query = base.query;
                if (c == '?') {
                    url.query = "";
                    state = State.QUERY;
                } else if (c == '#') {
                    url.fragment = "";
                    state = State.FRAGMENT;
                } else if (c != EOF) {
                    url.query = null;
                    if (!url.path.isEmpty()) {
                        url.path.remove(url.path.size() - 1);
                    }
                    state = State.PATH;
                    pointer--;
                }
            }
            return Result.CONTINUE;
        }

        /// Parses the relative-slash state.
        private Result parseRelativeSlash(int c) {
            if (base == null) {
                return failApiValidation();
            }
            if (isSpecial(url) && (c == '/' || c == '\\')) {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
            } else if (c == '/') {
                state = State.AUTHORITY;
            } else {
                url.username = base.username;
                url.password = base.password;
                url.host = base.host;
                url.port = base.port;
                state = State.PATH;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the special-authority-slashes state.
        private Result parseSpecialAuthoritySlashes(int c) {
            if (c == '/' && codePoint(input, pointer + 1) == '/') {
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
                pointer++;
            } else {
                recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the special-authority-ignore-slashes state.
        private Result parseSpecialAuthorityIgnoreSlashes(int c) {
            if (c != '/' && c != '\\') {
                state = State.AUTHORITY;
                pointer--;
            } else {
                recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
            }
            return Result.CONTINUE;
        }

        /// Parses the authority state.
        private Result parseAuthority(int c, String cStr) {
            if (c == '@') {
                recordValidationError(new WebURLParseException.InvalidCredentials());
                if (atSignSeen) {
                    buffer = "%40" + buffer;
                }
                atSignSeen = true;

                buffer.codePoints().forEach(codePoint -> {
                    if (codePoint == ':' && !passwordTokenSeen) {
                        passwordTokenSeen = true;
                    } else {
                        String encoded = PercentEncoding.utf8PercentEncodeCodePoint(
                                codePoint, PercentEncoding::isUserinfoPercentEncode);
                        if (passwordTokenSeen) {
                            url.password += encoded;
                        } else {
                            url.username += encoded;
                        }
                    }
                });
                buffer = "";
            } else if (c == EOF || c == '/' || c == '?' || c == '#' || (isSpecial(url) && c == '\\')) {
                if (atSignSeen && buffer.isEmpty()) {
                    return failHostMissing();
                }
                pointer -= countSymbols(buffer) + 1;
                buffer = "";
                state = State.HOST;
            } else {
                buffer += cStr;
            }
            return Result.CONTINUE;
        }

        /// Parses the host or hostname state.
        private Result parseHostName(int c, String cStr) {
            if (stateOverride != null && url.scheme.equals("file")) {
                pointer--;
                state = State.FILE_HOST;
            } else if (c == ':' && !insideBrackets) {
                if (buffer.isEmpty()) {
                    return failHostMissing();
                }
                if (stateOverride == State.HOSTNAME) {
                    return failApiValidation();
                }
                UrlHost host = parseHost(buffer, isNotSpecial(url));
                url.host = host;
                buffer = "";
                state = State.PORT;
            } else if (c == EOF || c == '/' || c == '?' || c == '#' || (isSpecial(url) && c == '\\')) {
                pointer--;
                if (isSpecial(url) && buffer.isEmpty()) {
                    return failHostMissing();
                } else if (stateOverride != null && buffer.isEmpty()
                        && (includesCredentials(url) || url.port != -1)) {
                    return failApiValidation();
                }
                UrlHost host = parseHost(buffer, isNotSpecial(url));
                url.host = host;
                buffer = "";
                state = State.PATH_START;
                if (stateOverride != null) {
                    return Result.STOP;
                }
            } else {
                if (c == '[') {
                    insideBrackets = true;
                } else if (c == ']') {
                    insideBrackets = false;
                }
                buffer += cStr;
            }
            return Result.CONTINUE;
        }

        /// Parses the port state.
        private Result parsePort(int c, String cStr) {
            if (Infra.isAsciiDigit(c)) {
                buffer += cStr;
            } else if (c == EOF || c == '/' || c == '?' || c == '#' || (isSpecial(url) && c == '\\')
                    || stateOverride != null) {
                if (!buffer.isEmpty()) {
                    int port;
                    try {
                        port = Integer.parseInt(buffer);
                    } catch (NumberFormatException ignored) {
                        return fail(new WebURLParseException.PortOutOfRange());
                    }
                    if (port > 65535) {
                        return fail(new WebURLParseException.PortOutOfRange());
                    }
                    url.port = defaultPort(url.scheme) == port ? -1 : port;
                    buffer = "";
                    if (stateOverride != null) {
                        return Result.STOP;
                    }
                }
                if (stateOverride != null) {
                    return failApiValidation();
                }
                state = State.PATH_START;
                pointer--;
            } else {
                return fail(new WebURLParseException.PortInvalid());
            }
            return Result.CONTINUE;
        }

        /// Parses the file state.
        private Result parseFile(int c) {
            url.scheme = "file";
            url.host = UrlHost.domain("");

            if (c == '/' || c == '\\') {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.FILE_SLASH;
            } else if (base != null && base.scheme.equals("file")) {
                url.host = base.host;
                url.path = new ArrayList<>(base.path);
                url.query = base.query;
                if (c == '?') {
                    url.query = "";
                    state = State.QUERY;
                } else if (c == '#') {
                    url.fragment = "";
                    state = State.FRAGMENT;
                } else if (c != EOF) {
                    url.query = null;
                    if (!startsWithWindowsDriveLetter(input, pointer)) {
                        shortenPath(url);
                    } else {
                        recordValidationError(new WebURLParseException.FileInvalidWindowsDriveLetter());
                        url.path = new ArrayList<>();
                    }
                    state = State.PATH;
                    pointer--;
                }
            } else {
                state = State.PATH;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the file-slash state.
        private Result parseFileSlash(int c) {
            if (c == '/' || c == '\\') {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.FILE_HOST;
            } else {
                if (base != null && base.scheme.equals("file")) {
                    if (!startsWithWindowsDriveLetter(input, pointer)
                            && !base.path.isEmpty()
                            && isNormalizedWindowsDriveLetterString(base.path.get(0))) {
                        url.path.add(base.path.get(0));
                    }
                    url.host = base.host;
                }
                state = State.PATH;
                pointer--;
            }
            return Result.CONTINUE;
        }

        /// Parses the file-host state.
        private Result parseFileHost(int c, String cStr) {
            if (c == EOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                pointer--;
                if (stateOverride == null && isWindowsDriveLetterString(buffer)) {
                    recordValidationError(new WebURLParseException.FileInvalidWindowsDriveLetterHost());
                    state = State.PATH;
                } else if (buffer.isEmpty()) {
                    url.host = UrlHost.domain("");
                    if (stateOverride != null) {
                        return Result.STOP;
                    }
                    state = State.PATH_START;
                } else {
                    UrlHost host = parseHost(buffer, isNotSpecial(url));
                    if (serializeHost(host).equals("localhost")) {
                        host = UrlHost.domain("");
                    }
                    url.host = host;
                    if (stateOverride != null) {
                        return Result.STOP;
                    }
                    buffer = "";
                    state = State.PATH_START;
                }
            } else {
                buffer += cStr;
            }
            return Result.CONTINUE;
        }

        /// Parses the path-start state.
        private Result parsePathStart(int c) {
            if (isSpecial(url)) {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.PATH;
                if (c != '/' && c != '\\') {
                    pointer--;
                }
            } else if (stateOverride == null && c == '?') {
                url.query = "";
                state = State.QUERY;
            } else if (stateOverride == null && c == '#') {
                url.fragment = "";
                state = State.FRAGMENT;
            } else if (c != EOF) {
                state = State.PATH;
                if (c != '/') {
                    pointer--;
                }
            } else if (stateOverride != null && url.host == null) {
                url.path.add("");
            }
            return Result.CONTINUE;
        }

        /// Parses the path state.
        private Result parsePath(int c) {
            if (c == EOF || c == '/' || (isSpecial(url) && c == '\\')
                    || (stateOverride == null && (c == '?' || c == '#'))) {
                if (isSpecial(url) && c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }

                if (isDoubleDot(buffer)) {
                    shortenPath(url);
                    if (c != '/' && !(isSpecial(url) && c == '\\')) {
                        url.path.add("");
                    }
                } else if (isSingleDot(buffer) && c != '/' && !(isSpecial(url) && c == '\\')) {
                    url.path.add("");
                } else if (!isSingleDot(buffer)) {
                    if (url.scheme.equals("file") && url.path.isEmpty() && isWindowsDriveLetterString(buffer)) {
                        buffer = buffer.charAt(0) + ":";
                    }
                    url.path.add(buffer);
                }
                buffer = "";
                if (c == '?') {
                    url.query = "";
                    state = State.QUERY;
                }
                if (c == '#') {
                    url.fragment = "";
                    state = State.FRAGMENT;
                }
            } else {
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                buffer += PercentEncoding.utf8PercentEncodeCodePoint(c, PercentEncoding::isPathPercentEncode);
            }
            return Result.CONTINUE;
        }

        /// Parses the opaque-path state.
        private Result parseOpaquePath(int c) {
            if (c == '?') {
                url.query = "";
                state = State.QUERY;
            } else if (c == '#') {
                url.fragment = "";
                state = State.FRAGMENT;
            } else if (c == ' ') {
                int remaining = codePoint(input, pointer + 1);
                url.opaquePath = (url.opaquePath == null ? "" : url.opaquePath)
                        + (remaining == '?' || remaining == '#' ? "%20" : " ");
            } else {
                if (c != EOF && c != '%') {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                if (c != EOF && PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                if (c != EOF) {
                    url.opaquePath = (url.opaquePath == null ? "" : url.opaquePath)
                            + PercentEncoding.utf8PercentEncodeCodePoint(c, PercentEncoding::isC0ControlPercentEncode);
                }
            }
            return Result.CONTINUE;
        }

        /// Parses the query state.
        private Result parseQuery(int c, String cStr) {
            if (!isSpecial(url) || url.scheme.equals("ws") || url.scheme.equals("wss")) {
                // Only UTF-8 is currently supported by this Java port.
            }

            if ((stateOverride == null && c == '#') || c == EOF) {
                String encoded = PercentEncoding.percentEncodeQuery(buffer, isSpecial(url));
                url.query = (url.query == null ? "" : url.query) + encoded;
                buffer = "";
                if (c == '#') {
                    url.fragment = "";
                    state = State.FRAGMENT;
                }
            } else {
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                buffer += cStr;
            }
            return Result.CONTINUE;
        }

        /// Parses the fragment state.
        private Result parseFragment(int c) {
            if (c != EOF) {
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                url.fragment = (url.fragment == null ? "" : url.fragment)
                        + PercentEncoding.utf8PercentEncodeCodePoint(c, PercentEncoding::isFragmentPercentEncode);
            }
            return Result.CONTINUE;
        }
    }
}
