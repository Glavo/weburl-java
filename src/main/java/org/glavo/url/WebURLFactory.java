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
package org.glavo.url;

import org.glavo.url.internal.UrlParser;
import org.glavo.url.internal.WebURLImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A reusable, immutable factory for WHATWG URLs.
///
/// A factory combines the URL Standard basic URL parser with browser address parsing helpers. The default
/// factory returned by `defaultFactory()` is the factory used by the static parsing methods on `WebURL`.
///
/// A factory does not store a base URL. `parseURL(String)`, `tryParseURL(String)`, and
/// `canParseURL(String)` parse only absolute URL inputs. Overloads that accept a base URL use the supplied
/// base only for that call. `parseAddress(String)`, `tryParseAddress(String)`, and
/// `canParseAddress(String)` additionally accept browser address bar style URL inputs such as bare domain
/// names and host-port pairs, completing them with the `https` scheme.
///
/// The factory object is immutable and thread-safe.
@NotNullByDefault
public final class WebURLFactory {
    /// The scheme used for browser address inputs without an explicit scheme.
    private static final String ADDRESS_SCHEME = "https";

    /// The default factory used by `WebURL` static parsing methods.
    private static final WebURLFactory DEFAULT = new WebURLFactory();

    /// Creates a URL factory.
    private WebURLFactory() {
    }

    /// Returns the default URL factory.
    ///
    /// The default factory parses only absolute URLs unless a base is supplied to an overload that accepts one.
    /// This method always returns the same immutable factory instance.
    ///
    /// @return the default factory
    public static WebURLFactory defaultFactory() {
        return DEFAULT;
    }

    /// Parses an input string and returns the parsed URL.
    ///
    /// The input must be an absolute URL. Use `parseURL(String, String)` or `parseURL(String, WebURL)` to
    /// parse a relative input against an explicit base URL.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    public WebURL parseURL(String input) {
        return parseRequired(input, null, "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The supplied base string is parsed first with no base URL. The input is then parsed relative to that base.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails
    public WebURL parseURL(String input, String base) {
        return parseRequired(input, parseBaseRequired(base), "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The supplied base URL is used only for this call.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    public WebURL parseURL(String input, WebURL base) {
        return parseRequired(input, implementation(base), "Invalid URL: " + input);
    }

    /// Parses an input string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseURL(String)`, except URL parse failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if parsing fails
    public @Nullable WebURL tryParseURL(String input) {
        return parseNullable(input, null);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// The supplied base string is parsed with no base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    public @Nullable WebURL tryParseURL(String input, String base) {
        WebURLImpl parsedBase = parseBaseNullable(base);
        return parsedBase == null ? null : parseNullable(input, parsedBase);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// The supplied base URL is used only for this call.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL, or `null` if parsing fails
    public @Nullable WebURL tryParseURL(String input, WebURL base) {
        return parseNullable(input, implementation(base));
    }

    /// Returns whether an input string can be parsed.
    ///
    /// The input must be an absolute URL. Use `canParseURL(String, String)` or `canParseURL(String, WebURL)`
    /// to test a relative input against an explicit base URL.
    ///
    /// @param input the URL input string
    /// @return `true` if parsing succeeds, otherwise `false`
    public boolean canParseURL(String input) {
        return tryParseURL(input) != null;
    }

    /// Returns whether an input string can be parsed against a base URL string.
    ///
    /// The supplied base string is parsed with no base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return `true` if the base parses and the input parses against it, otherwise `false`
    public boolean canParseURL(String input, String base) {
        return tryParseURL(input, base) != null;
    }

    /// Returns whether an input string can be parsed against a base URL.
    ///
    /// The supplied base URL is used only for this call.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return `true` if parsing succeeds, otherwise `false`
    public boolean canParseURL(String input, WebURL base) {
        return tryParseURL(input, base) != null;
    }

    /// Parses a browser address input and returns the parsed URL.
    ///
    /// This method first recognizes address-bar style URL inputs that are not absolute URL strings, such as
    /// `www.example.com`, `example.com/path`, `//example.com/path`, `localhost:8080`, `127.0.0.1:3000`, and
    /// `[::1]:8080`. Such inputs are converted to absolute URL strings by prepending `https`. Inputs with an
    /// explicit URL scheme, such as `https://example.com/`, `data:text/plain,hi`, or
    /// `mailto:user@example.com`, are parsed as standard URLs.
    ///
    /// This method does not implement search fallback. Inputs that are neither URL strings nor recognized
    /// browser address inputs fail instead of being interpreted as search terms.
    ///
    /// @param input the browser address input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    public WebURL parseAddress(String input) {
        Objects.requireNonNull(input, "input");
        String addressInput = toAddressUrlInput(input);
        if (addressInput != null) {
            return parseRequired(addressInput, null, "Invalid address: " + input);
        }
        return parseRequired(input, null, "Invalid address: " + input);
    }

    /// Parses a browser address input and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseAddress(String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the browser address input string
    /// @return the parsed URL, or `null` if parsing fails
    public @Nullable WebURL tryParseAddress(String input) {
        Objects.requireNonNull(input, "input");
        String addressInput = toAddressUrlInput(input);
        return parseNullable(addressInput == null ? input : addressInput, null);
    }

    /// Returns whether a browser address input can be parsed.
    ///
    /// This method has the same parser behavior as `parseAddress(String)`, except it returns a boolean result.
    ///
    /// @param input the browser address input string
    /// @return `true` if parsing succeeds, otherwise `false`
    public boolean canParseAddress(String input) {
        return tryParseAddress(input) != null;
    }

    /// Parses an input string and throws when parsing fails.
    private WebURL parseRequired(String input, @Nullable WebURLImpl base, String message) {
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
    private WebURLImpl parseBaseRequired(String base) {
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
    private @Nullable WebURLImpl parseNullable(String input, @Nullable WebURLImpl base) {
        Objects.requireNonNull(input, "input");
        return UrlParser.basicParse(input, base, null, null);
    }

    /// Parses a base URL string and returns `null` when parsing fails.
    private @Nullable WebURLImpl parseBaseNullable(String base) {
        return parseNullable(base, null);
    }

    /// Converts a browser address input to an absolute URL input, or returns `null` for standard URL input.
    private @Nullable String toAddressUrlInput(String input) {
        String text = removeTabsAndNewlines(trimControlChars(input));
        if (text.isEmpty()) {
            return null;
        }
        if (text.startsWith("//") || text.startsWith("\\\\")) {
            return ADDRESS_SCHEME + ":" + text;
        }
        if (text.charAt(0) == '/' || text.charAt(0) == '\\' || text.charAt(0) == '?' || text.charAt(0) == '#') {
            return null;
        }

        int authorityEnd = firstAddressAuthorityDelimiter(text);
        String authority = text.substring(0, authorityEnd);
        int schemeEnd = validSchemeEnd(authority);
        if (schemeEnd >= 0 && !isAddressHost(authority.substring(0, schemeEnd))) {
            return null;
        }
        return isAddressAuthority(authority) ? ADDRESS_SCHEME + "://" + text : null;
    }

    /// Returns whether a string is a browser-address authority.
    private static boolean isAddressAuthority(String authority) {
        if (authority.isEmpty() || containsAddressAuthoritySpace(authority)) {
            return false;
        }
        int at = authority.lastIndexOf('@');
        String hostPort = at < 0 ? authority : authority.substring(at + 1);
        if (hostPort.isEmpty()) {
            return false;
        }
        return isAddressHost(extractAddressHost(hostPort));
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

    /// Returns whether a host string is recognized as a browser address host.
    private static boolean isAddressHost(String host) {
        return host.startsWith("[")
                || host.equalsIgnoreCase("localhost")
                || containsDomainDot(host);
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
        for (int i = 0; i < input.length(); i++) {
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

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) Objects.requireNonNull(url, "url");
    }
}
