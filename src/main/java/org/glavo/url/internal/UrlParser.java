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
import org.glavo.url.internal.idna.UTS46;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    public static @Nullable WebURLImpl parseUrl(String input) {
        return basicParse(input, null, null, null);
    }

    /// Runs the basic URL parser.
    public static @Nullable WebURLImpl basicParse(
            String input,
            @Nullable WebURLImpl baseUrl,
            @Nullable UrlRecord url,
            @Nullable State stateOverride
    ) {
        try {
            return basicParseRequired(input, baseUrl, url, stateOverride);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /// Runs the basic URL parser and throws when parsing fails.
    public static WebURLImpl basicParseRequired(
            String input,
            @Nullable WebURLImpl baseUrl,
            @Nullable UrlRecord url,
            @Nullable State stateOverride
    ) {
        if (baseUrl == null && url == null && stateOverride == null) {
            @Nullable WebURLImpl fastUrl = UrlFastParser.parse(input);
            if (fastUrl != null) {
                return fastUrl;
            }
        }
        StateMachine stateMachine = new StateMachine(input, baseUrl, url, stateOverride);
        return stateMachine.toUrl();
    }

    /// Serializes a URL host.
    public static String serializeHost(UrlHost host) {
        return host.serialize();
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

        String domain = containsPercent(input)
                ? Encoding.utf8DecodeWithoutBom(PercentEncoding.percentDecodeString(input))
                : input;
        String asciiDomain = domainToAscii(domain);

        if (endsInANumber(asciiDomain)) {
            return UrlHost.ipv4(parseIpv4(asciiDomain));
        }

        return UrlHost.domain(asciiDomain);
    }

    /// Reconstructs a host from its serialized form.
    static UrlHost parseSerializedHost(String input, boolean opaque) {
        if (input.startsWith("[")) {
            if (!input.endsWith("]")) {
                throw new WebURLParseException.IPv6Unclosed();
            }
            return UrlHost.ipv6(parseIpv6(input.substring(1, input.length() - 1)));
        }
        if (opaque) {
            return UrlHost.opaque(input);
        }
        if (endsInANumber(input)) {
            return UrlHost.ipv4(parseIpv4(input));
        }
        return UrlHost.domain(input);
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
    private static String domainToAscii(String domain) {
        if (isAsciiOnly(domain) && !containsPunycodeLabel(domain)) {
            String result = containsAsciiUppercase(domain) ? domain.toLowerCase(Locale.ROOT) : domain;
            if (result.isEmpty()) {
                throw new WebURLParseException.DomainToASCII();
            }
            if (containsForbiddenDomainCodePoint(result)) {
                throw new WebURLParseException.DomainInvalidCodePoint();
            }
            return result;
        }

        UTS46.Result result = UTS46.toAsciiForUrl(domain, false);
        if (result.error()) {
            throw new WebURLParseException.DomainToASCII();
        }

        if (result.value().isEmpty()) {
            throw new WebURLParseException.DomainToASCII();
        }
        if (containsForbiddenDomainCodePoint(result.value())) {
            throw new WebURLParseException.DomainInvalidCodePoint();
        }
        return result.value();
    }

    /// Returns whether a string contains a percent sign.
    private static boolean containsPercent(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '%') {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a domain contains only ASCII code points.
    private static boolean isAsciiOnly(String domain) {
        for (int i = 0; i < domain.length(); i++) {
            if (domain.charAt(i) > 0x7f) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether a string contains an ASCII upper-case letter.
    private static boolean containsAsciiUppercase(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a domain contains a punycode label.
    private static boolean containsPunycodeLabel(String domain) {
        int labelStart = 0;
        while (labelStart <= domain.length()) {
            if (labelStart + 4 <= domain.length()
                    && domain.regionMatches(true, labelStart, "xn--", 0, 4)) {
                return true;
            }
            int dot = domain.indexOf('.', labelStart);
            if (dot < 0) {
                return false;
            }
            labelStart = dot + 1;
        }
        return false;
    }

    /// Returns whether the domain ends in a numeric label.
    private static boolean endsInANumber(String input) {
        int end = input.length();
        if (end > 1 && input.charAt(end - 1) == '.') {
            end--;
        }
        int start = input.lastIndexOf('.', end - 1) + 1;
        if (parseIpv4Number(input, start, end) != null) {
            return true;
        }
        for (int i = start; i < end; i++) {
            if (!Infra.isAsciiDigit(input.charAt(i))) {
                return false;
            }
        }
        return start < end;
    }

    /// Parses an IPv4 number from a string slice.
    private static @Nullable Long parseIpv4Number(String input, int start, int end) {
        if (start == end) {
            return null;
        }

        int radix = 10;
        int valueStart = start;
        if (end - valueStart >= 2
                && input.charAt(valueStart) == '0'
                && (input.charAt(valueStart + 1) == 'x' || input.charAt(valueStart + 1) == 'X')) {
            valueStart += 2;
            radix = 16;
        } else if (end - valueStart >= 2 && input.charAt(valueStart) == '0') {
            valueStart++;
            radix = 8;
        }

        if (valueStart == end) {
            return 0L;
        }

        long value = 0;
        for (int i = valueStart; i < end; i++) {
            int c = input.charAt(i);
            boolean ok = radix == 10 ? Infra.isAsciiDigit(c)
                    : radix == 16 ? Infra.isAsciiHex(c)
                      : c >= '0' && c <= '7';
            if (!ok) {
                return null;
            }
            int digit = Character.digit(c, radix);
            if (value > (Long.MAX_VALUE - digit) / radix) {
                return Long.MAX_VALUE;
            }
            value = value * radix + digit;
        }
        return value;
    }

    /// Parses an IPv4 address.
    private static long parseIpv4(String input) {
        int end = input.length();
        if (end > 1 && input.charAt(end - 1) == '.') {
            end--;
        }

        long[] numbers = new long[4];
        int numbersCount = 0;
        int partStart = 0;
        while (partStart <= end) {
            if (numbersCount == numbers.length) {
                throw new WebURLParseException.IPv4TooManyParts();
            }

            int partEnd = input.indexOf('.', partStart);
            if (partEnd < 0 || partEnd > end) {
                partEnd = end;
            }

            Long number = parseIpv4Number(input, partStart, partEnd);
            if (number == null) {
                throw new WebURLParseException.IPv4NonNumericPart();
            }
            numbers[numbersCount] = number;
            numbersCount++;

            if (partEnd == end) {
                break;
            }
            partStart = partEnd + 1;
        }

        for (int i = 0; i < numbersCount - 1; i++) {
            if (numbers[i] > 255) {
                throw new WebURLParseException.IPv4OutOfRangePart();
            }
        }

        long lastLimit = 1L << (8 * (5 - numbersCount));
        if (numbers[numbersCount - 1] >= lastLimit) {
            throw new WebURLParseException.IPv4OutOfRangePart();
        }

        long ipv4 = numbers[numbersCount - 1];
        for (int i = 0; i < numbersCount - 1; i++) {
            ipv4 += numbers[i] << (8 * (3 - i));
        }
        return ipv4;
    }

    /// Parses an IPv6 address.
    private static int[] parseIpv6(String inputString) {
        int[] address = new int[8];
        int pieceIndex = 0;
        int compress = -1;
        int pointer = 0;
        String input = inputString;

        if (codePoint(input, pointer) == ':') {
            if (codePoint(input, pointer + 1) != ':') {
                throw new WebURLParseException.IPv6InvalidCompression();
            }
            pointer += 2;
            pieceIndex++;
            compress = pieceIndex;
        }

        while (pointer < input.length()) {
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
        int firstSkipped = -1;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r') {
                firstSkipped = i;
                break;
            }
        }
        if (firstSkipped < 0) {
            return url;
        }

        StringBuilder output = new StringBuilder(url.length() - 1);
        output.append(url, 0, firstSkipped);
        for (int i = firstSkipped + 1; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c != '\t' && c != '\n' && c != '\r') {
                output.append(c);
            }
        }
        return output.toString();
    }

    /// Returns the code point at a UTF-16 index, or EOF.
    private static int codePoint(String input, int index) {
        return index >= 0 && index < input.length() ? input.codePointAt(index) : EOF;
    }

    /// Returns the UTF-16 index of the next code point.
    private static int nextPointer(String input, int index) {
        if (index < 0) {
            return 0;
        }
        if (index >= input.length()) {
            return input.length() + 1;
        }
        return index + Character.charCount(input.codePointAt(index));
    }

    /// Returns the UTF-16 index of the previous code point.
    private static int previousPointer(String input, int index) {
        return index <= 0 ? -1 : input.offsetByCodePoints(index, -1);
    }

    /// Returns whether input starts with a Windows drive letter at the pointer.
    private static boolean startsWithWindowsDriveLetter(String input, int pointer) {
        int first = codePoint(input, pointer);
        int secondPointer = nextPointer(input, pointer);
        int second = codePoint(input, secondPointer);
        if (!isWindowsDriveLetterCodePoints(first, second)) {
            return false;
        }

        int thirdPointer = nextPointer(input, secondPointer);
        int third = codePoint(input, thirdPointer);
        return third == EOF || third == '/' || third == '\\' || third == '?' || third == '#';
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
        STOP
    }

    /// Mutable text buffer that can keep unchanged input as a source slice.
    @NotNullByDefault
    private static final class Buffer {
        /// Source string for an unchanged slice, or `null` when the buffer is empty or materialized.
        private @Nullable String source;
        /// Start index of the source slice.
        private int start;
        /// End index of the source slice.
        private int end;
        /// Materialized builder used after the buffer diverges from a single source slice.
        private @Nullable StringBuilder builder;

        /// Returns whether the buffer is empty.
        boolean isEmpty() {
            @Nullable StringBuilder builderValue = builder;
            return builderValue == null ? start == end : builderValue.isEmpty();
        }

        /// Returns the UTF-16 length of the buffer.
        int length() {
            @Nullable StringBuilder builderValue = builder;
            return builderValue == null ? end - start : builderValue.length();
        }

        /// Clears the buffer.
        void clear() {
            source = null;
            start = 0;
            end = 0;
            builder = null;
        }

        /// Appends an unchanged source slice.
        void append(String value, int sliceStart, int sliceEnd) {
            if (sliceStart == sliceEnd) {
                return;
            }

            @Nullable StringBuilder builderValue = builder;
            if (builderValue != null) {
                builderValue.append(value, sliceStart, sliceEnd);
                return;
            }

            @Nullable String sourceValue = source;
            if (sourceValue == null) {
                source = value;
                start = sliceStart;
                end = sliceEnd;
            } else if (sourceValue.equals(value) && end == sliceStart) {
                end = sliceEnd;
            } else {
                StringBuilder materialized = new StringBuilder(length() + sliceEnd - sliceStart);
                appendTo(materialized);
                materialized.append(value, sliceStart, sliceEnd);
                source = null;
                start = 0;
                end = 0;
                builder = materialized;
            }
        }

        /// Appends a string value.
        void append(String value) {
            if (value.isEmpty()) {
                return;
            }

            StringBuilder builderValue = materializeBuilder(value.length());
            builderValue.append(value);
        }

        /// Appends one ASCII character.
        void appendAscii(int c) {
            StringBuilder builderValue = materializeBuilder(1);
            builderValue.append((char) c);
        }

        /// Prepends a percent-encoded at-sign.
        void prependPercentEncodedAtSign() {
            StringBuilder builderValue = new StringBuilder(3 + length());
            builderValue.append("%40");
            appendTo(builderValue);
            source = null;
            start = 0;
            end = 0;
            builder = builderValue;
        }

        /// Returns whether this buffer equals the supplied string.
        boolean equals(String value) {
            if (value.length() != length()) {
                return false;
            }

            @Nullable StringBuilder builderValue = builder;
            if (builderValue != null) {
                for (int i = 0; i < value.length(); i++) {
                    if (builderValue.charAt(i) != value.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }

            @Nullable String sourceValue = source;
            return sourceValue != null && sourceValue.regionMatches(start, value, 0, value.length());
        }

        /// Returns whether this buffer is a special URL scheme.
        boolean isSpecialScheme() {
            return equals("ftp")
                    || equals("file")
                    || equals("http")
                    || equals("https")
                    || equals("ws")
                    || equals("wss");
        }

        /// Parses a URL port number.
        int parsePort() {
            int value = 0;
            int length = length();
            for (int i = 0; i < length; i++) {
                int c = charAt(i);
                if (!Infra.isAsciiDigit(c)) {
                    throw new NumberFormatException();
                }
                value = value * 10 + (c - '0');
                if (value > 65535) {
                    throw new NumberFormatException();
                }
            }
            return value;
        }

        /// Returns the buffer as a string.
        @Override
        public String toString() {
            @Nullable StringBuilder builderValue = builder;
            if (builderValue != null) {
                return builderValue.toString();
            }

            @Nullable String sourceValue = source;
            return sourceValue == null ? "" : sourceValue.substring(start, end);
        }

        /// Returns a character at the buffer index.
        private char charAt(int index) {
            @Nullable StringBuilder builderValue = builder;
            if (builderValue != null) {
                return builderValue.charAt(index);
            }

            @Nullable String sourceValue = source;
            if (sourceValue == null) {
                throw new IndexOutOfBoundsException(index);
            }
            return sourceValue.charAt(start + index);
        }

        /// Materializes this buffer into a builder with room for additional characters.
        private StringBuilder materializeBuilder(int additionalLength) {
            @Nullable StringBuilder builderValue = builder;
            if (builderValue != null) {
                return builderValue;
            }

            StringBuilder materialized = new StringBuilder(length() + additionalLength);
            appendTo(materialized);
            source = null;
            start = 0;
            end = 0;
            builder = materialized;
            return materialized;
        }

        /// Appends the current content into another builder.
        private void appendTo(StringBuilder output) {
            @Nullable StringBuilder builderValue = builder;
            if (builderValue != null) {
                output.append(builderValue);
                return;
            }

            @Nullable String sourceValue = source;
            if (sourceValue != null) {
                output.append(sourceValue, start, end);
            }
        }
    }

    /// One run of the URL state machine.
    @NotNullByDefault
    private static final class StateMachine {
        /// Input pointer.
        private int pointer;
        /// Parsed input string.
        private final String input;
        /// Base URL.
        private final @Nullable WebURLImpl base;
        /// State override.
        private final @Nullable State stateOverride;
        /// Mutable URL record produced by this parser run.
        private final UrlRecord record;
        /// Current parser state.
        private State state;
        /// Temporary parser buffer.
        private final Buffer buffer = new Buffer();
        /// Whether an at-sign was seen in authority.
        private boolean atSignSeen;
        /// Whether the host parser is inside IPv6 brackets.
        private boolean insideBrackets;
        /// Whether userinfo parsing reached the password token.
        private boolean passwordTokenSeen;

        /// Creates and runs the state machine.
        StateMachine(
                String inputText,
                @Nullable WebURLImpl base,
                @Nullable UrlRecord url,
                @Nullable State stateOverride
        ) {
            this.pointer = 0;
            this.base = base;
            this.stateOverride = stateOverride;
            this.record = url == null ? new UrlRecord() : url;
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
            this.input = withoutTabsAndNewlines;

            run();
        }

        /// Creates an immutable URL from the parser state.
        private WebURLImpl toUrl() {
            return UrlSerializer.toUrl(record, input);
        }

        /// Runs the parser loop.
        private void run() {
            while (pointer <= input.length()) {
                int c = codePoint(input, pointer);
                Result result = execute(c);
                if (result == Result.STOP) {
                    break;
                }
                pointer = nextPointer(input, pointer);
            }
        }

        /// Returns the code point at an offset from the current input pointer.
        private int codePointAtOffset(int offset) {
            int index = pointer;
            for (int i = 0; i < offset; i++) {
                index = nextPointer(input, index);
            }
            return codePoint(input, index);
        }

        /// Advances the input pointer by one code point.
        private void advancePointer() {
            pointer = nextPointer(input, pointer);
        }

        /// Rewinds the input pointer by one code point.
        private void rewindPointer() {
            pointer = previousPointer(input, pointer);
        }

        /// Rewinds the input pointer so the authority buffer is reprocessed as a host.
        private void rewindAuthorityBuffer(int authorityBufferLength) {
            int hostStart = pointer - authorityBufferLength;
            pointer = previousPointer(input, hostStart);
        }

        /// Records a validation error without forcing parser failure.
        private void recordValidationError(WebURLParseException error) {
        }

        /// Throws a parser failure with a corresponding public validation error.
        private Result fail(WebURLParseException error) {
            throw error;
        }

        /// Throws a generic parser failure.
        private Result failApiValidation() {
            throw new WebURLParseException.InvalidURL();
        }

        /// Returns a parser failure for `host-missing`.
        private Result failHostMissing() {
            return fail(new WebURLParseException.HostMissing());
        }

        /// Shortens the current path.
        private void shortenPath() {
            if (record.path.isEmpty()) {
                return;
            }
            if (record.scheme.equals("file")
                    && record.path.size() == 1
                    && isNormalizedWindowsDriveLetterString(record.path.get(0))) {
                return;
            }
            record.path.remove(record.path.size() - 1);
        }

        /// Returns whether the current URL includes credentials.
        private boolean includesCredentials() {
            return !record.username.isEmpty() || !record.password.isEmpty();
        }

        /// Returns whether the current URL has a special scheme.
        private boolean isSpecial() {
            return isSpecialScheme(record.scheme);
        }

        /// Returns whether the current URL has a non-special scheme.
        private boolean isNotSpecial() {
            return !isSpecial();
        }

        /// Executes the current parser state.
        private Result execute(int c) {
            switch (state) {
                case SCHEME_START:
                    return parseSchemeStart(c);
                case SCHEME:
                    return parseScheme(c);
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
                    return parseAuthority(c);
                case HOST:
                case HOSTNAME:
                    return parseHostName(c);
                case PORT:
                    return parsePort(c);
                case FILE:
                    return parseFile(c);
                case FILE_SLASH:
                    return parseFileSlash(c);
                case FILE_HOST:
                    return parseFileHost(c);
                case PATH_START:
                    return parsePathStart(c);
                case PATH:
                    return parsePath(c);
                case OPAQUE_PATH:
                    return parseOpaquePath(c);
                case QUERY:
                    return parseQuery(c);
                case FRAGMENT:
                    return parseFragment(c);
                default:
                    throw new AssertionError(state);
            }
        }

        /// Appends the current input code point to the temporary buffer unchanged.
        private void appendCurrentCodePoint() {
            if (pointer >= 0 && pointer < input.length()) {
                buffer.append(input, pointer, pointer + Character.charCount(input.codePointAt(pointer)));
            }
        }

        /// Appends the current ASCII code point to the temporary buffer in lower case.
        private void appendLowercaseAscii(int c) {
            if (c >= 'A' && c <= 'Z') {
                buffer.appendAscii(c + 0x20);
            } else {
                appendCurrentCodePoint();
            }
        }

        /// Appends the current code point with the URL percent-encode set.
        private void appendPercentEncoded(int c, PercentEncoding.BytePredicate predicate) {
            if (c <= 0x7f && !predicate.test(c)) {
                appendCurrentCodePoint();
            } else {
                buffer.append(PercentEncoding.utf8PercentEncodeCodePoint(c, predicate));
            }
        }

        /// Appends the buffered component text to an existing component string.
        private String appendBufferedComponent(@Nullable String current) {
            String value = buffer.toString();
            buffer.clear();
            if (current == null || current.isEmpty()) {
                return value;
            }
            return value.isEmpty() ? current : current + value;
        }

        /// Parses the scheme start state.
        private Result parseSchemeStart(int c) {
            if (Infra.isAsciiAlpha(c)) {
                appendLowercaseAscii(c);
                state = State.SCHEME;
            } else if (stateOverride == null) {
                state = State.NO_SCHEME;
                rewindPointer();
            } else {
                return failApiValidation();
            }
            return Result.CONTINUE;
        }

        /// Parses the scheme state.
        private Result parseScheme(int c) {
            if (Infra.isAsciiAlphanumeric(c) || c == '+' || c == '-' || c == '.') {
                appendLowercaseAscii(c);
            } else if (c == ':') {
                if (stateOverride != null) {
                    if (isSpecial() && !buffer.isSpecialScheme()) {
                        return Result.STOP;
                    }
                    if (!isSpecial() && buffer.isSpecialScheme()) {
                        return Result.STOP;
                    }
                    if ((includesCredentials() || record.port != -1) && buffer.equals("file")) {
                        return Result.STOP;
                    }
                    if (record.scheme.equals("file") && record.host != null && record.host.isEmptyDomain()) {
                        return Result.STOP;
                    }
                }
                record.scheme = buffer.toString();
                if (stateOverride != null) {
                    int defaultPort = defaultPort(record.scheme);
                    if (defaultPort == record.port) {
                        record.port = -1;
                    }
                    return Result.STOP;
                }
                buffer.clear();
                if (record.scheme.equals("file")) {
                    if (codePointAtOffset(1) != '/' || codePointAtOffset(2) != '/') {
                        recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
                    }
                    state = State.FILE;
                } else if (isSpecial() && base != null && base.schemeEquals(record.scheme)) {
                    state = State.SPECIAL_RELATIVE_OR_AUTHORITY;
                } else if (isSpecial()) {
                    state = State.SPECIAL_AUTHORITY_SLASHES;
                } else if (codePointAtOffset(1) == '/') {
                    state = State.PATH_OR_AUTHORITY;
                    advancePointer();
                } else {
                    record.opaquePath = "";
                    record.path.clear();
                    state = State.OPAQUE_PATH;
                }
            } else if (stateOverride == null) {
                buffer.clear();
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
                record.scheme = base.getScheme();
                record.opaquePath = base.opaquePathValue();
                record.path = base.pathSegments();
                record.query = base.queryValue();
                record.fragment = "";
                state = State.FRAGMENT;
            } else if (base.schemeEquals("file")) {
                state = State.FILE;
                rewindPointer();
            } else {
                state = State.RELATIVE;
                rewindPointer();
            }
            return Result.CONTINUE;
        }

        /// Parses the special-relative-or-authority state.
        private Result parseSpecialRelativeOrAuthority(int c) {
            if (c == '/' && codePointAtOffset(1) == '/') {
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
                advancePointer();
            } else {
                recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
                state = State.RELATIVE;
                rewindPointer();
            }
            return Result.CONTINUE;
        }

        /// Parses the path-or-authority state.
        private Result parsePathOrAuthority(int c) {
            if (c == '/') {
                state = State.AUTHORITY;
            } else {
                state = State.PATH;
                rewindPointer();
            }
            return Result.CONTINUE;
        }

        /// Parses the relative state.
        private Result parseRelative(int c) {
            if (base == null) {
                return failApiValidation();
            }
            record.scheme = base.getScheme();
            if (c == '/') {
                state = State.RELATIVE_SLASH;
            } else if (isSpecial() && c == '\\') {
                recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                state = State.RELATIVE_SLASH;
            } else {
                record.username = base.getUsernameOrEmpty();
                record.password = base.getPasswordOrEmpty();
                record.host = base.hostValue();
                record.port = base.portValue();
                record.path = base.pathSegments();
                record.opaquePath = base.opaquePathValue();
                record.query = base.queryValue();
                if (c == '?') {
                    record.query = "";
                    state = State.QUERY;
                } else if (c == '#') {
                    record.fragment = "";
                    state = State.FRAGMENT;
                } else if (c != EOF) {
                    record.query = null;
                    if (!record.path.isEmpty()) {
                        record.path.remove(record.path.size() - 1);
                    }
                    state = State.PATH;
                    rewindPointer();
                }
            }
            return Result.CONTINUE;
        }

        /// Parses the relative-slash state.
        private Result parseRelativeSlash(int c) {
            if (base == null) {
                return failApiValidation();
            }
            if (isSpecial() && (c == '/' || c == '\\')) {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
            } else if (c == '/') {
                state = State.AUTHORITY;
            } else {
                record.username = base.getUsernameOrEmpty();
                record.password = base.getPasswordOrEmpty();
                record.host = base.hostValue();
                record.port = base.portValue();
                state = State.PATH;
                rewindPointer();
            }
            return Result.CONTINUE;
        }

        /// Parses the special-authority-slashes state.
        private Result parseSpecialAuthoritySlashes(int c) {
            if (c == '/' && codePointAtOffset(1) == '/') {
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
                advancePointer();
            } else {
                recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
                state = State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
                rewindPointer();
            }
            return Result.CONTINUE;
        }

        /// Parses the special-authority-ignore-slashes state.
        private Result parseSpecialAuthorityIgnoreSlashes(int c) {
            if (c != '/' && c != '\\') {
                state = State.AUTHORITY;
                rewindPointer();
            } else {
                recordValidationError(new WebURLParseException.SpecialSchemeMissingFollowingSolidus());
            }
            return Result.CONTINUE;
        }

        /// Parses the authority state.
        private Result parseAuthority(int c) {
            if (c == '@') {
                recordValidationError(new WebURLParseException.InvalidCredentials());
                if (atSignSeen) {
                    buffer.prependPercentEncodedAtSign();
                }
                atSignSeen = true;

                String authorityBuffer = buffer.toString();
                authorityBuffer.codePoints().forEach(codePoint -> {
                    if (codePoint == ':' && !passwordTokenSeen) {
                        passwordTokenSeen = true;
                    } else {
                        String encoded = PercentEncoding.utf8PercentEncodeCodePoint(
                                codePoint, PercentEncoding::isUserinfoPercentEncode);
                        if (passwordTokenSeen) {
                            record.password += encoded;
                        } else {
                            record.username += encoded;
                        }
                    }
                });
                buffer.clear();
            } else if (c == EOF || c == '/' || c == '?' || c == '#' || (isSpecial() && c == '\\')) {
                if (atSignSeen && buffer.isEmpty()) {
                    return failHostMissing();
                }
                rewindAuthorityBuffer(buffer.length());
                buffer.clear();
                state = State.HOST;
            } else {
                appendCurrentCodePoint();
            }
            return Result.CONTINUE;
        }

        /// Parses the host or hostname state.
        private Result parseHostName(int c) {
            if (stateOverride != null && record.scheme.equals("file")) {
                rewindPointer();
                state = State.FILE_HOST;
            } else if (c == ':' && !insideBrackets) {
                if (buffer.isEmpty()) {
                    return failHostMissing();
                }
                if (stateOverride == State.HOSTNAME) {
                    return failApiValidation();
                }
                record.host = parseHost(buffer.toString(), isNotSpecial());
                buffer.clear();
                state = State.PORT;
            } else if (c == EOF || c == '/' || c == '?' || c == '#' || (isSpecial() && c == '\\')) {
                rewindPointer();
                if (isSpecial() && buffer.isEmpty()) {
                    return failHostMissing();
                } else if (stateOverride != null && buffer.isEmpty()
                        && (includesCredentials() || record.port != -1)) {
                    return failApiValidation();
                }
                record.host = parseHost(buffer.toString(), isNotSpecial());
                buffer.clear();
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
                appendCurrentCodePoint();
            }
            return Result.CONTINUE;
        }

        /// Parses the port state.
        private Result parsePort(int c) {
            if (Infra.isAsciiDigit(c)) {
                appendCurrentCodePoint();
            } else if (c == EOF || c == '/' || c == '?' || c == '#' || (isSpecial() && c == '\\')
                    || stateOverride != null) {
                if (!buffer.isEmpty()) {
                    int parsedPort;
                    try {
                        parsedPort = buffer.parsePort();
                    } catch (NumberFormatException ignored) {
                        if (stateOverride == State.HOST) {
                            buffer.clear();
                            return Result.STOP;
                        }
                        return fail(new WebURLParseException.PortOutOfRange());
                    }
                    record.port = defaultPort(record.scheme) == parsedPort ? -1 : parsedPort;
                    buffer.clear();
                    if (stateOverride != null) {
                        return Result.STOP;
                    }
                }
                if (stateOverride != null) {
                    return stateOverride == State.HOST ? Result.STOP : failApiValidation();
                }
                state = State.PATH_START;
                rewindPointer();
            } else {
                return fail(new WebURLParseException.PortInvalid());
            }
            return Result.CONTINUE;
        }

        /// Parses the file state.
        private Result parseFile(int c) {
            record.scheme = "file";
            record.host = UrlHost.domain("");

            if (c == '/' || c == '\\') {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.FILE_SLASH;
            } else if (base != null && base.schemeEquals("file")) {
                record.host = base.hostValue();
                record.path = base.pathSegments();
                record.query = base.queryValue();
                if (c == '?') {
                    record.query = "";
                    state = State.QUERY;
                } else if (c == '#') {
                    record.fragment = "";
                    state = State.FRAGMENT;
                } else if (c != EOF) {
                    record.query = null;
                    if (!startsWithWindowsDriveLetter(input, pointer)) {
                        shortenPath();
                    } else {
                        recordValidationError(new WebURLParseException.FileInvalidWindowsDriveLetter());
                        record.path = new ArrayList<>();
                    }
                    state = State.PATH;
                    rewindPointer();
                }
            } else {
                state = State.PATH;
                rewindPointer();
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
                if (base != null && base.schemeEquals("file")) {
                    if (!startsWithWindowsDriveLetter(input, pointer)
                            && base.firstPathSegment() != null
                            && isNormalizedWindowsDriveLetterString(base.firstPathSegment())) {
                        record.path.add(base.firstPathSegment());
                    }
                    record.host = base.hostValue();
                }
                state = State.PATH;
                rewindPointer();
            }
            return Result.CONTINUE;
        }

        /// Parses the file-host state.
        private Result parseFileHost(int c) {
            if (c == EOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                rewindPointer();
                String fileHost = buffer.toString();
                if (stateOverride == null && isWindowsDriveLetterString(fileHost)) {
                    recordValidationError(new WebURLParseException.FileInvalidWindowsDriveLetterHost());
                    state = State.PATH;
                } else if (fileHost.isEmpty()) {
                    record.host = UrlHost.domain("");
                    if (stateOverride != null) {
                        return Result.STOP;
                    }
                    state = State.PATH_START;
                } else {
                    UrlHost parsedHost = parseHost(fileHost, isNotSpecial());
                    if (serializeHost(parsedHost).equals("localhost")) {
                        parsedHost = UrlHost.domain("");
                    }
                    record.host = parsedHost;
                    if (stateOverride != null) {
                        return Result.STOP;
                    }
                    buffer.clear();
                    state = State.PATH_START;
                }
            } else {
                appendCurrentCodePoint();
            }
            return Result.CONTINUE;
        }

        /// Parses the path-start state.
        private Result parsePathStart(int c) {
            if (isSpecial()) {
                if (c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }
                state = State.PATH;
                if (c != '/' && c != '\\') {
                    rewindPointer();
                }
            } else if (stateOverride == null && c == '?') {
                record.query = "";
                state = State.QUERY;
            } else if (stateOverride == null && c == '#') {
                record.fragment = "";
                state = State.FRAGMENT;
            } else if (c != EOF) {
                state = State.PATH;
                if (c != '/') {
                    rewindPointer();
                }
            } else if (stateOverride != null && record.host == null) {
                record.path.add("");
            }
            return Result.CONTINUE;
        }

        /// Parses the path state.
        private Result parsePath(int c) {
            if (c == EOF || c == '/' || (isSpecial() && c == '\\')
                    || (stateOverride == null && (c == '?' || c == '#'))) {
                if (isSpecial() && c == '\\') {
                    recordValidationError(new WebURLParseException.InvalidReverseSolidus());
                }

                String segment = buffer.toString();
                if (isDoubleDot(segment)) {
                    shortenPath();
                    if (c != '/' && !(isSpecial() && c == '\\')) {
                        record.path.add("");
                    }
                } else if (isSingleDot(segment) && c != '/' && !(isSpecial() && c == '\\')) {
                    record.path.add("");
                } else if (!isSingleDot(segment)) {
                    if (record.scheme.equals("file") && record.path.isEmpty() && isWindowsDriveLetterString(segment)) {
                        segment = segment.charAt(0) + ":";
                    }
                    record.path.add(segment);
                }
                buffer.clear();
                if (c == '?') {
                    record.query = "";
                    state = State.QUERY;
                }
                if (c == '#') {
                    record.fragment = "";
                    state = State.FRAGMENT;
                }
            } else {
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                appendPercentEncoded(c, PercentEncoding::isPathPercentEncode);
            }
            return Result.CONTINUE;
        }

        /// Parses the opaque-path state.
        private Result parseOpaquePath(int c) {
            if (c == '?') {
                record.opaquePath = appendBufferedComponent(record.opaquePath);
                record.query = "";
                state = State.QUERY;
            } else if (c == '#') {
                record.opaquePath = appendBufferedComponent(record.opaquePath);
                record.fragment = "";
                state = State.FRAGMENT;
            } else if (c == ' ') {
                int remaining = codePointAtOffset(1);
                if (remaining == '?' || remaining == '#') {
                    buffer.append("%20");
                } else {
                    appendCurrentCodePoint();
                }
            } else if (c == EOF) {
                record.opaquePath = appendBufferedComponent(record.opaquePath);
            } else {
                if (c != '%') {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                appendPercentEncoded(c, PercentEncoding::isC0ControlPercentEncode);
            }
            return Result.CONTINUE;
        }

        /// Parses the query state.
        private Result parseQuery(int c) {
            if (!isSpecial() || record.scheme.equals("ws") || record.scheme.equals("wss")) {
                // Only UTF-8 is currently supported by this Java port.
            }

            if ((stateOverride == null && c == '#') || c == EOF) {
                record.query = appendBufferedComponent(record.query);
                if (c == '#') {
                    record.fragment = "";
                    state = State.FRAGMENT;
                }
            } else {
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                appendPercentEncoded(c, isSpecial()
                        ? PercentEncoding::isSpecialQueryPercentEncode
                        : PercentEncoding::isQueryPercentEncode);
            }
            return Result.CONTINUE;
        }

        /// Parses the fragment state.
        private Result parseFragment(int c) {
            if (c == EOF) {
                record.fragment = appendBufferedComponent(record.fragment);
            } else {
                if (PercentEncoding.startsInvalidPercentTriplet(input, pointer)) {
                    recordValidationError(new WebURLParseException.InvalidURLUnit());
                }
                appendPercentEncoded(c, PercentEncoding::isFragmentPercentEncode);
            }
            return Result.CONTINUE;
        }
    }
}
