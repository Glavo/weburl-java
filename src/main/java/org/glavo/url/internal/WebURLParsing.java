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

import org.glavo.url.WebURL;
import org.glavo.url.WebURLParseException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal parsing helpers used by `WebURL` static factory methods.
@NotNullByDefault
public final class WebURLParsing {
    /// The scheme used for public-looking browser-style inputs without an explicit scheme.
    private static final String HTTPS_SCHEME = "https";
    /// The scheme used for local or private browser-style inputs without an explicit scheme.
    private static final String HTTP_SCHEME = "http";

    /// Creates no instances.
    private WebURLParsing() {
    }

    /// Parses an input string and returns the parsed URL.
    public static WebURL parse(String input) {
        return parse(input, false);
    }

    /// Parses an input string and returns the parsed URL.
    public static WebURL parse(String input, boolean strictValidation) {
        return parseRequired(input, null, strictValidation, "Invalid URL");
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    public static WebURL parse(String input, String base) {
        return parse(input, base, false);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    public static WebURL parse(String input, String base, boolean strictValidation) {
        return parseRequired(input, parseBaseRequired(base, strictValidation), strictValidation, "Invalid URL");
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    public static WebURL parse(String input, WebURL base) {
        return parse(input, base, false);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    public static WebURL parse(String input, WebURL base, boolean strictValidation) {
        return parseRequired(input, implementation(base), strictValidation, "Invalid URL");
    }

    /// Parses an input string and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input) {
        return tryParse(input, false);
    }

    /// Parses an input string and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, boolean strictValidation) {
        return parseNullable(input, null, strictValidation);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, String base) {
        return tryParse(input, base, false);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, String base, boolean strictValidation) {
        WebURLImpl parsedBase = parseNullable(base, null, strictValidation);
        return parsedBase == null ? null : parseNullable(input, parsedBase, strictValidation);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, WebURL base) {
        return tryParse(input, base, false);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, WebURL base, boolean strictValidation) {
        return parseNullable(input, implementation(base), strictValidation);
    }

    /// Parses a browser-style URL input and returns the parsed URL.
    public static WebURL parseBrowserInput(String input) {
        return parseBrowserInput(input, false);
    }

    /// Parses a browser-style URL input and returns the parsed URL.
    public static WebURL parseBrowserInput(String input, boolean strictValidation) {
        Objects.requireNonNull(input, "input");
        String addressInput = toAddressUrlInput(input);
        return parseRequired(Objects.requireNonNullElse(addressInput, input), null, strictValidation,
                "Invalid browser input");
    }

    /// Parses a browser-style URL input and returns `null` on failure.
    public static @Nullable WebURL tryParseBrowserInput(String input) {
        return tryParseBrowserInput(input, false);
    }

    /// Parses a browser-style URL input and returns `null` on failure.
    public static @Nullable WebURL tryParseBrowserInput(String input, boolean strictValidation) {
        Objects.requireNonNull(input, "input");
        String addressInput = toAddressUrlInput(input);
        return parseNullable(Objects.requireNonNullElse(addressInput, input), null, strictValidation);
    }

    /// Parses an input string and throws when parsing fails.
    private static WebURL parseRequired(
            String input,
            @Nullable WebURLImpl base,
            boolean strictValidation,
            String reason
    ) {
        Objects.requireNonNull(input, "input");
        try {
            return UrlParser.basicParseRequired(input, base, null, null, strictValidation);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new WebURLParseException(input, WebURLParseException.ErrorType.INVALID_URL, reason, -1, exception);
        }
    }

    /// Parses a base URL string and throws when parsing fails.
    private static WebURLImpl parseBaseRequired(String base, boolean strictValidation) {
        Objects.requireNonNull(base, "base");
        try {
            return UrlParser.basicParseRequired(base, null, null, null, strictValidation);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new WebURLParseException(base, WebURLParseException.ErrorType.INVALID_URL, "Invalid base URL", -1, exception);
        }
    }

    /// Parses an input string and returns `null` when parsing fails.
    private static @Nullable WebURLImpl parseNullable(
            String input,
            @Nullable WebURLImpl base,
            boolean strictValidation
    ) {
        Objects.requireNonNull(input, "input");
        return UrlParser.basicParse(input, base, null, null, strictValidation);
    }

    /// Converts a browser-style URL input to an absolute URL input, or returns `null` for standard URL input.
    private static @Nullable String toAddressUrlInput(String input) {
        String text = removeTabsAndNewlines(trimControlChars(input));
        if (text.isEmpty()) {
            return null;
        }
        @Nullable String localPathInput = toLocalPathUrlInput(text);
        if (localPathInput != null) {
            return localPathInput;
        }
        if (text.startsWith("//") || text.startsWith("\\\\")) {
            int authorityEnd = firstAddressAuthorityDelimiter(text, 2);
            String authority = text.substring(2, authorityEnd);
            @Nullable String scheme = addressScheme(authority);
            return scheme == null ? null : completeAddressInput(scheme, text, 2, authorityEnd);
        }
        if (text.charAt(0) == '/' || text.charAt(0) == '\\' || text.charAt(0) == '?' || text.charAt(0) == '#') {
            return null;
        }

        int authorityEnd = firstAddressAuthorityDelimiter(text);
        String authority = text.substring(0, authorityEnd);
        int schemeEnd = validSchemeEnd(authority);
        if (schemeEnd >= 0 && !isAddressHostWithPortPrefix(authority, schemeEnd)) {
            return null;
        }
        @Nullable String scheme = addressScheme(authority);
        return scheme == null ? null : completeAddressInput(scheme, text, 0, authorityEnd);
    }

    /// Converts a local path-like browser input to a file URL input, or returns `null` when it is not a path.
    private static @Nullable String toLocalPathUrlInput(String text) {
        if (startsWithWindowsDriveAbsolutePath(text)) {
            return localPathToFileUrl("file:///", text);
        }
        if (startsWithWindowsUncPath(text)) {
            return windowsUncPathToFileUrl(text);
        }
        if (text.charAt(0) == '/' && !text.startsWith("//")) {
            return localPathToFileUrl("file://", text);
        }
        return null;
    }

    /// Returns whether a string starts with an absolute Windows drive path.
    private static boolean startsWithWindowsDriveAbsolutePath(String text) {
        return text.length() >= 3
                && isAsciiAlpha(text.charAt(0))
                && text.charAt(1) == ':'
                && isLocalPathSeparator(text.charAt(2));
    }

    /// Returns whether a string starts with a Windows UNC path.
    private static boolean startsWithWindowsUncPath(String text) {
        return text.length() >= 3 && text.charAt(0) == '\\' && text.charAt(1) == '\\';
    }

    /// Converts a Windows UNC path-like input to a file URL input.
    private static @Nullable String windowsUncPathToFileUrl(String text) {
        int hostStart = 2;
        int hostEnd = firstLocalPathSeparator(text, hostStart);
        String host = text.substring(hostStart, hostEnd);
        if (!isAddressHost(host)) {
            return null;
        }
        String path = hostEnd < text.length() ? text.substring(hostEnd) : "/";
        return localPathToFileUrl("file://" + host, path);
    }

    /// Converts a local path string to a file URL using the supplied file URL prefix.
    private static String localPathToFileUrl(String prefix, String path) {
        String normalizedPath = path.indexOf('\\') < 0 ? path : path.replace('\\', '/');
        return prefix + PercentEncoding.utf8PercentEncodeString(normalizedPath, WebURLParsing::isLocalFilePathPercentEncode);
    }

    /// Returns the first local path separator at or after the given index.
    private static int firstLocalPathSeparator(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isLocalPathSeparator(text.charAt(i))) {
                return i;
            }
        }
        return text.length();
    }

    /// Returns whether a character is a local path separator recognized by the browser input heuristic.
    private static boolean isLocalPathSeparator(char c) {
        return c == '/' || c == '\\';
    }

    /// Returns whether a byte must be percent-encoded in a local file path URL.
    private static boolean isLocalFilePathPercentEncode(int value) {
        return value == '%' || PercentEncoding.isPathPercentEncode(value);
    }

    /// Completes a browser-style address input with a scheme and authority delimiter.
    private static String completeAddressInput(String scheme, String text, int authorityStart, int authorityEnd) {
        String authority = text.substring(authorityStart, authorityEnd);
        return scheme + "://" + authority + text.substring(authorityEnd);
    }

    /// Returns the default scheme for a browser-address authority, or `null` when it is not recognized.
    private static @Nullable String addressScheme(String authority) {
        if (authority.isEmpty() || containsAddressAuthoritySpace(authority)) {
            return null;
        }
        int at = authority.lastIndexOf('@');
        String hostPort = at < 0 ? authority : authority.substring(at + 1);
        if (hostPort.isEmpty()) {
            return null;
        }
        String host = extractAddressHost(hostPort);
        @Nullable String port = extractAddressPort(hostPort);
        return isAddressHost(host) ? defaultAddressScheme(host, port) : null;
    }

    /// Returns whether a valid scheme-looking prefix should instead be treated as a host before a port.
    private static boolean isAddressHostWithPortPrefix(String authority, int schemeEnd) {
        String host = authority.substring(0, schemeEnd);
        if (!isAddressHost(host)) {
            return false;
        }
        if (containsDomainDot(host) || isIpAddressHost(host) || isReservedAddressHost(host)) {
            return true;
        }
        return isAsciiDecimal(authority, schemeEnd + 1, authority.length());
    }

    /// Returns the default scheme for an address host.
    private static String defaultAddressScheme(String host, @Nullable String port) {
        return isIpAddressHost(host) || isSingleLabelHost(host) || isReservedAddressHost(host)
                || port != null && isAsciiDecimal(port, 0, port.length())
                ? HTTP_SCHEME
                : HTTPS_SCHEME;
    }

    /// Extracts the host portion from an address authority tail.
    private static String extractAddressHost(String hostPort) {
        if (hostPort.startsWith("[")) {
            int end = hostPort.indexOf(']');
            return end < 0 ? hostPort : hostPort.substring(0, end + 1);
        }
        int colon = hostPort.lastIndexOf(':');
        return colon < 0 ? hostPort : hostPort.substring(0, colon);
    }

    /// Extracts the port portion from an address authority tail, or returns `null` when absent.
    private static @Nullable String extractAddressPort(String hostPort) {
        int colon = addressPortColon(hostPort);
        return colon < 0 ? null : hostPort.substring(colon + 1);
    }

    /// Returns the colon before the port in an address authority tail, or `-1` when absent.
    private static int addressPortColon(String hostPort) {
        if (hostPort.startsWith("[")) {
            int end = hostPort.indexOf(']');
            return end >= 0 && end + 1 < hostPort.length() && hostPort.charAt(end + 1) == ':' ? end + 1 : -1;
        }
        return hostPort.lastIndexOf(':');
    }

    /// Returns whether a port string represents the default HTTP port.
    private static boolean isHttpDefaultPort(String port) {
        int value = 0;
        for (int i = 0; i < port.length(); i++) {
            value = value * 10 + port.charAt(i) - '0';
            if (value > 80) {
                return false;
            }
        }
        return value == 80;
    }

    /// Returns whether a host string is recognized as a browser-style input host.
    private static boolean isAddressHost(String host) {
        if (isIpAddressHost(host)) {
            return true;
        }
        return isAddressHostname(host)
                && (isSingleLabelHost(host)
                        || isReservedAddressHost(host)
                        || containsDomainDot(host));
    }

    /// Returns whether a host has the shape of a browser-address hostname.
    private static boolean isAddressHostname(String host) {
        if (host.isEmpty() || host.startsWith("[") || isDomainDot(host.charAt(0))) {
            return false;
        }

        boolean hasLabelCharacter = false;
        for (int i = 0; i < host.length(); ) {
            int codePoint = host.codePointAt(i);
            i += Character.charCount(codePoint);
            if (isDomainDot(codePoint)) {
                if (!hasLabelCharacter) {
                    return false;
                }
                hasLabelCharacter = false;
            } else if (isAddressHostnameCodePoint(codePoint)) {
                if (codePoint != '_' && codePoint != '-') {
                    hasLabelCharacter = true;
                }
            } else {
                return false;
            }
        }
        return hasLabelCharacter;
    }

    /// Returns whether a code point may appear in a browser-address hostname label.
    private static boolean isAddressHostnameCodePoint(int codePoint) {
        return codePoint >= '0' && codePoint <= '9'
                || codePoint >= 'A' && codePoint <= 'Z'
                || codePoint >= 'a' && codePoint <= 'z'
                || codePoint == '-'
                || codePoint == '_'
                || codePoint > 0x7f && !Character.isISOControl(codePoint);
    }

    /// Returns whether a host is an IP address literal or IPv4-number-like host.
    private static boolean isIpAddressHost(String host) {
        return host.startsWith("[") || isIpv4AddressLikeHost(host);
    }

    /// Returns whether a host consists of IPv4 number parts.
    private static boolean isIpv4AddressLikeHost(String host) {
        if (host.isEmpty()) {
            return false;
        }

        int start = 0;
        while (start < host.length()) {
            int end = host.indexOf('.', start);
            if (end < 0) {
                end = host.length();
            }
            if (!isIpv4Number(host, start, end)) {
                return false;
            }
            start = end + 1;
        }
        return true;
    }

    /// Returns whether a substring is an IPv4 number in decimal or hexadecimal form.
    private static boolean isIpv4Number(String value, int start, int end) {
        if (start >= end) {
            return false;
        }
        if (start + 2 < end && value.charAt(start) == '0'
                && (value.charAt(start + 1) == 'x' || value.charAt(start + 1) == 'X')) {
            return isAsciiHex(value, start + 2, end);
        }
        return isAsciiDecimal(value, start, end);
    }

    /// Returns whether a host has a single label.
    private static boolean isSingleLabelHost(String host) {
        return !host.isEmpty() && !host.startsWith("[") && !containsDomainDot(host);
    }

    /// Returns whether a host uses a reserved local or test name.
    private static boolean isReservedAddressHost(String host) {
        return equalsIgnoreCase(host, "localhost")
                || equalsIgnoreCase(host, "test")
                || equalsIgnoreCase(host, "example")
                || equalsIgnoreCase(host, "invalid")
                || endsWithReservedSuffix(host, ".localhost")
                || endsWithReservedSuffix(host, ".test")
                || endsWithReservedSuffix(host, ".example")
                || endsWithReservedSuffix(host, ".invalid");
    }

    /// Returns whether a host ends with a reserved suffix.
    private static boolean endsWithReservedSuffix(String host, String suffix) {
        return host.length() > suffix.length() && host.regionMatches(true,
                host.length() - suffix.length(), suffix, 0, suffix.length());
    }

    /// Returns whether a string contains a domain-label separator.
    private static boolean containsDomainDot(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (isDomainDot(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a code point is a domain-label separator.
    private static boolean isDomainDot(int codePoint) {
        return codePoint == '.' || codePoint == '\u3002' || codePoint == '\uff0e' || codePoint == '\uff61';
    }

    /// Returns whether an address authority contains an ASCII space or control character.
    private static boolean containsAddressAuthoritySpace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) <= 0x20) {
                return true;
            }
        }
        return false;
    }

    /// Returns the first delimiter after the address authority.
    private static int firstAddressAuthorityDelimiter(String input) {
        return firstAddressAuthorityDelimiter(input, 0);
    }

    /// Returns the first delimiter after the address authority starting at an index.
    private static int firstAddressAuthorityDelimiter(String input, int start) {
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '/' || c == '\\' || c == '?' || c == '#') {
                return i;
            }
        }
        return input.length();
    }

    /// Returns the end index of a valid scheme in a string prefix, or `-1` when absent.
    private static int validSchemeEnd(String value) {
        int colon = value.indexOf(':');
        if (colon <= 0 || !isAsciiAlpha(value.charAt(0))) {
            return -1;
        }
        for (int i = 1; i < colon; i++) {
            char c = value.charAt(i);
            if (!isAsciiAlphanumeric(c) && c != '+' && c != '-' && c != '.') {
                return -1;
            }
        }
        return colon;
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
    private static String removeTabsAndNewlines(String value) {
        int firstSkipped = -1;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r') {
                firstSkipped = i;
                break;
            }
        }
        if (firstSkipped < 0) {
            return value;
        }

        StringBuilder output = new StringBuilder(value.length() - 1);
        output.append(value, 0, firstSkipped);
        for (int i = firstSkipped + 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\t' && c != '\n' && c != '\r') {
                output.append(c);
            }
        }
        return output.toString();
    }

    /// Returns whether a character is an ASCII letter.
    private static boolean isAsciiAlpha(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    /// Returns whether a character is an ASCII letter or digit.
    private static boolean isAsciiAlphanumeric(char c) {
        return isAsciiAlpha(c) || c >= '0' && c <= '9';
    }

    /// Returns whether two strings are equal ignoring ASCII case.
    private static boolean equalsIgnoreCase(String left, String right) {
        return left.equalsIgnoreCase(right);
    }

    /// Returns whether a substring contains only ASCII decimal digits.
    private static boolean isAsciiDecimal(String value, int start, int end) {
        if (start >= end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /// Returns whether a substring contains only ASCII hexadecimal digits.
    private static boolean isAsciiHex(String value, int start, int end) {
        if (start >= end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            char c = value.charAt(i);
            if (!isAsciiAlphanumeric(c)
                    || c > '9' && c < 'A'
                    || c > 'F' && c < 'a'
                    || c > 'f') {
                return false;
            }
        }
        return true;
    }

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) Objects.requireNonNull(url, "url");
    }
}
