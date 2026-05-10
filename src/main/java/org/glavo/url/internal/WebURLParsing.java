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
        return parseRequired(input, null, "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    public static WebURL parse(String input, String base) {
        return parseRequired(input, parseBaseRequired(base), "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    public static WebURL parse(String input, WebURL base) {
        return parseRequired(input, implementation(base), "Invalid URL: " + input);
    }

    /// Parses an input string and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input) {
        return parseNullable(input, null);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, String base) {
        WebURLImpl parsedBase = parseNullable(base, null);
        return parsedBase == null ? null : parseNullable(input, parsedBase);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    public static @Nullable WebURL tryParse(String input, WebURL base) {
        return parseNullable(input, implementation(base));
    }

    /// Parses a browser-style URL input and returns the parsed URL.
    public static WebURL parseBrowserInput(String input) {
        Objects.requireNonNull(input, "input");
        String addressInput = toAddressUrlInput(input);
        if (addressInput != null) {
            return parseRequired(addressInput, null, "Invalid browser input: " + input);
        }
        return parseRequired(input, null, "Invalid browser input: " + input);
    }

    /// Parses a browser-style URL input and returns `null` on failure.
    public static @Nullable WebURL tryParseBrowserInput(String input) {
        Objects.requireNonNull(input, "input");
        String addressInput = toAddressUrlInput(input);
        return parseNullable(addressInput == null ? input : addressInput, null);
    }

    /// Parses an input string and throws when parsing fails.
    private static WebURL parseRequired(String input, @Nullable WebURLImpl base, String message) {
        Objects.requireNonNull(input, "input");
        try {
            return UrlParser.basicParseRequired(input, base, null, null);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new WebURLParseException.InvalidURL(message, exception);
        }
    }

    /// Parses a base URL string and throws when parsing fails.
    private static WebURLImpl parseBaseRequired(String base) {
        Objects.requireNonNull(base, "base");
        try {
            return UrlParser.basicParseRequired(base, null, null, null);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new WebURLParseException.InvalidURL("Invalid base URL: " + base, exception);
        }
    }

    /// Parses an input string and returns `null` when parsing fails.
    private static @Nullable WebURLImpl parseNullable(String input, @Nullable WebURLImpl base) {
        Objects.requireNonNull(input, "input");
        return UrlParser.basicParse(input, base, null, null);
    }

    /// Converts a browser-style URL input to an absolute URL input, or returns `null` for standard URL input.
    private static @Nullable String toAddressUrlInput(String input) {
        String text = removeTabsAndNewlines(trimControlChars(input));
        if (text.isEmpty()) {
            return null;
        }
        if (text.startsWith("//") || text.startsWith("\\\\")) {
            @Nullable String scheme = addressScheme(text.substring(2, firstAddressAuthorityDelimiter(text, 2)));
            return scheme == null ? null : scheme + ":" + text;
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
        return scheme == null ? null : scheme + "://" + text;
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
        return isAddressHost(host) ? defaultAddressScheme(host) : null;
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
    private static String defaultAddressScheme(String host) {
        return isIpAddressHost(host) || isSingleLabelHost(host) || isReservedAddressHost(host)
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

    /// Returns whether a host string is recognized as a browser-style input host.
    private static boolean isAddressHost(String host) {
        return isIpAddressHost(host)
                || isSingleLabelHost(host)
                || isReservedAddressHost(host)
                || containsDomainDot(host);
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

    /// Returns whether a substring is an IPv4 number in decimal, octal, or hexadecimal form.
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
            char c = value.charAt(i);
            if (c == '.' || c == '\u3002' || c == '\uff0e' || c == '\uff61') {
                return true;
            }
        }
        return false;
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
