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

import org.glavo.url.WebURL;
import org.glavo.url.WebURLPatternSyntaxException;
import org.glavo.url.internal.PercentEncoding;
import org.glavo.url.internal.StringUtils;
import org.glavo.url.internal.UrlParser;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Canonicalizes URLPattern component strings.
@NotNullByDefault
final class URLPatternCanonicalizer {
    /// Creates no instances.
    private URLPatternCanonicalizer() {
    }

    /// Canonicalizes a protocol component.
    static String canonicalizeProtocol(String input) {
        String value = input.endsWith(":") ? input.substring(0, input.length() - 1) : input;
        if (value.isEmpty()) {
            return "";
        }
        if (!StringUtils.isAsciiAlpha(value.charAt(0))) {
            throw invalidComponent("protocol");
        }
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!StringUtils.isAsciiAlphanumeric(c) && c != '+' && c != '-' && c != '.') {
                throw invalidComponent("protocol");
            }
        }
        return value.toLowerCase(Locale.ROOT);
    }

    /// Canonicalizes a username component.
    static String canonicalizeUsername(String input) {
        return input.isEmpty() ? "" : PercentEncoding.utf8PercentEncodeString(
                input, PercentEncoding::isUserinfoPercentEncode);
    }

    /// Canonicalizes a password component.
    static String canonicalizePassword(String input) {
        return input.isEmpty() ? "" : PercentEncoding.utf8PercentEncodeString(
                input, PercentEncoding::isUserinfoPercentEncode);
    }

    /// Canonicalizes a hostname component.
    static String canonicalizeHostname(String input) {
        if (input.isEmpty()) {
            return "";
        }
        if (isSimpleHostname(input)) {
            return input;
        }
        try {
            return WebURL.newBuilder()
                    .setScheme("https")
                    .setRawHost(input)
                    .setRawPath("/")
                    .build()
                    .getWebHostname();
        } catch (RuntimeException exception) {
            throw new WebURLPatternSyntaxException("Invalid URLPattern hostname component", exception);
        }
    }

    /// Canonicalizes an IPv6 hostname literal without full URL host parsing.
    static String canonicalizeIpv6Hostname(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '[' && c != ']' && c != ':' && !StringUtils.isAsciiHex(c)) {
                throw invalidComponent("hostname");
            }
        }
        return input.toLowerCase(Locale.ROOT);
    }

    /// Canonicalizes a port component without a protocol.
    static String canonicalizePort(String input) {
        String value = StringUtils.removeAsciiTabsAndNewlines(input);
        if (value.isEmpty()) {
            return "";
        }
        int digitEnd = digitPrefixEnd(value);
        if (digitEnd == 0) {
            throw invalidComponent("port");
        }
        String digits = value.substring(0, digitEnd);
        if (digits.length() > 5 || digits.length() == 5 && digits.compareTo("65535") > 0) {
            throw invalidComponent("port");
        }
        if (digits.length() > 1 && digits.charAt(0) == '0') {
            throw invalidComponent("port");
        }
        return digits;
    }

    /// Canonicalizes a port component with the current protocol.
    static String canonicalizePortWithProtocol(String input, String protocol) {
        String value = StringUtils.removeAsciiTabsAndNewlines(input);
        if (value.isEmpty()) {
            return "";
        }
        int digitEnd = digitPrefixEnd(value);
        if (digitEnd == 0) {
            throw invalidComponent("port");
        }
        int port = parsePort(value, digitEnd);
        String scheme = protocol.endsWith(":") ? protocol.substring(0, protocol.length() - 1) : protocol;
        if (scheme.isEmpty()) {
            scheme = "fake";
        }
        return UrlParser.defaultPort(scheme) == port ? "" : Integer.toString(port);
    }

    /// Canonicalizes a special or hierarchical pathname component.
    static String canonicalizePathname(String input) {
        if (input.isEmpty()) {
            return "";
        }
        if (isSimplePathname(input)) {
            return input;
        }
        boolean leadingSlash = input.startsWith("/");
        String modifiedValue = leadingSlash ? input : "/-" + input;
        try {
            String pathname = WebURL.parse("fake://fake-url" + modifiedValue).getWebPathname();
            if (!leadingSlash) {
                if (pathname.length() < 2) {
                    throw invalidComponent("pathname");
                }
                return pathname.substring(2);
            }
            return pathname;
        } catch (RuntimeException exception) {
            if (exception instanceof WebURLPatternSyntaxException syntaxException) {
                throw syntaxException;
            }
            throw new WebURLPatternSyntaxException("Invalid URLPattern pathname component", exception);
        }
    }

    /// Canonicalizes an opaque pathname component.
    static String canonicalizeOpaquePathname(String input) {
        if (input.isEmpty()) {
            return "";
        }
        try {
            return WebURL.parse("fake:" + input).getWebPathname();
        } catch (RuntimeException exception) {
            throw new WebURLPatternSyntaxException("Invalid URLPattern pathname component", exception);
        }
    }

    /// Canonicalizes a search component.
    static String canonicalizeSearch(String input) {
        String value = input.startsWith("?") ? input.substring(1) : input;
        value = StringUtils.removeAsciiTabsAndNewlines(value);
        return value.isEmpty() ? "" : PercentEncoding.utf8PercentEncodeString(
                value, PercentEncoding::isQueryPercentEncode);
    }

    /// Canonicalizes a hash component.
    static String canonicalizeHash(String input) {
        String value = input.startsWith("#") ? input.substring(1) : input;
        value = StringUtils.removeAsciiTabsAndNewlines(value);
        return value.isEmpty() ? "" : PercentEncoding.utf8PercentEncodeString(
                value, PercentEncoding::isFragmentPercentEncode);
    }

    /// Processes a base URL component string for the current process type.
    static String processBaseUrlString(String input, URLPatternInit.ProcessType type) {
        return type == URLPatternInit.ProcessType.PATTERN ? PatternParser.escapePatternString(input) : input;
    }

    /// Returns whether a pathname is absolute under URLPattern rules.
    static boolean isAbsolutePathname(String input, URLPatternInit.ProcessType type) {
        if (input.isEmpty()) {
            return false;
        }
        if (input.charAt(0) == '/') {
            return true;
        }
        if (type == URLPatternInit.ProcessType.URL || input.length() < 2) {
            return false;
        }
        return input.charAt(1) == '/' && (input.charAt(0) == '\\' || input.charAt(0) == '{');
    }

    /// Returns whether a hostname can be returned without parser processing.
    private static boolean isSimpleHostname(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '-' || c == '.')) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether a pathname can be returned without parser processing.
    private static boolean isSimplePathname(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!(c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9'
                    || c == '/' || c == '-' || c == '_' || c == '~')) {
                return false;
            }
        }
        return true;
    }

    /// Returns the length of the leading decimal digit run.
    private static int digitPrefixEnd(String input) {
        int end = 0;
        while (end < input.length() && StringUtils.isAsciiDigit(input.charAt(end))) {
            end++;
        }
        return end;
    }

    /// Parses a port prefix as a 16-bit URL port.
    private static int parsePort(String input, int end) {
        int port = 0;
        for (int i = 0; i < end; i++) {
            port = port * 10 + (input.charAt(i) - '0');
            if (port > 65535) {
                throw invalidComponent("port");
            }
        }
        return port;
    }

    /// Creates an invalid component exception.
    private static WebURLPatternSyntaxException invalidComponent(String component) {
        return new WebURLPatternSyntaxException("Invalid URLPattern " + component + " component");
    }
}
