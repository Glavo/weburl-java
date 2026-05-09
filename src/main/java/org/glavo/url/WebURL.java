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

import org.glavo.url.internal.WebURLImpl;
import org.glavo.url.internal.WebURLParsing;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/// An immutable Java representation of a WHATWG URL.
///
/// A URL has a `scheme` and either an opaque path or a hierarchical structure made from an optional
/// authority, a path, an optional query, and an optional fragment. Component getters use Java `URI`-style
/// naming. Raw component getters return normalized component strings with percent-encoding preserved, while
/// decoded component getters decode percent triplets as UTF-8. Optional components return `null` when absent.
///
/// The serialized form returned by `href()`, `toString()`, and `toJSON()` is the WHATWG URL serialization.
/// It is not identical to Java `URI` syntax for all inputs; use `toURI()` to obtain a Java `URI` value.
///
/// Equality, hash code, and natural ordering are defined by the complete WHATWG URL serialization returned by
/// `href()`. Two URL objects are equal when their serialized URLs are equal, and `compareTo(WebURL)` orders
/// URLs by the lexicographic order of those serialized strings.
@NotNullByDefault
public sealed interface WebURL extends Comparable<WebURL> permits WebURLImpl {
    /// Parses an absolute input string and returns the parsed URL.
    ///
    /// The input is parsed by the WHATWG basic URL parser with no base URL. Relative inputs therefore fail.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    static WebURL parse(String input) {
        return WebURLParsing.parse(input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The base is parsed first. The input may be either absolute or relative to that base.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails
    static WebURL parse(String input, String base) {
        return WebURLParsing.parse(input, base);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The input may be either absolute or relative to the supplied base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    static WebURL parse(String input, WebURL base) {
        return WebURLParsing.parse(input, base);
    }

    /// Parses an absolute input string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parse(String)`, except failures are represented by `null`
    /// instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if parsing fails
    static @Nullable WebURL tryParse(String input) {
        return WebURLParsing.tryParse(input);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parse(String, String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    static @Nullable WebURL tryParse(String input, String base) {
        return WebURLParsing.tryParse(input, base);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parse(String, WebURL)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL, or `null` if the input cannot be parsed against the base
    static @Nullable WebURL tryParse(String input, WebURL base) {
        return WebURLParsing.tryParse(input, base);
    }

    /// Returns whether an input string can be parsed as an absolute URL.
    ///
    /// @param input the URL input string
    /// @return `true` if parsing succeeds, otherwise `false`
    static boolean canParse(String input) {
        return WebURLParsing.canParse(input);
    }

    /// Returns whether an input string can be parsed against a base URL string.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return `true` if the base parses and the input parses against it, otherwise `false`
    static boolean canParse(String input, String base) {
        return WebURLParsing.canParse(input, base);
    }

    /// Returns whether an input string can be parsed against a base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return `true` if the input parses against the base, otherwise `false`
    static boolean canParse(String input, WebURL base) {
        return WebURLParsing.canParse(input, base);
    }

    /// Parses a browser address input and returns the parsed URL.
    ///
    /// This method accepts standard absolute URL strings and browser address bar style URL inputs such as bare
    /// domain names, `//`-prefixed authorities, `localhost` with a port, IP addresses, and bracketed IPv6
    /// addresses. Inputs without an explicit scheme are completed with `https`.
    ///
    /// This method does not implement search fallback. Inputs that are neither URL strings nor recognized
    /// browser address inputs fail instead of being interpreted as search terms.
    ///
    /// @param input the browser address input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    static WebURL parseAddress(String input) {
        return WebURLParsing.parseAddress(input);
    }

    /// Parses a browser address input and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseAddress(String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the browser address input string
    /// @return the parsed URL, or `null` if parsing fails
    static @Nullable WebURL tryParseAddress(String input) {
        return WebURLParsing.tryParseAddress(input);
    }

    /// Returns whether a browser address input can be parsed.
    ///
    /// This method has the same parser behavior as `parseAddress(String)`, except it returns a boolean result.
    ///
    /// @param input the browser address input string
    /// @return `true` if parsing succeeds, otherwise `false`
    static boolean canParseAddress(String input) {
        return WebURLParsing.canParseAddress(input);
    }

    /// Returns the complete serialized URL.
    ///
    /// The returned string is the WHATWG URL serialization, including the scheme, path, query, and fragment.
    /// Default ports are omitted, percent-encoding uses the URL Standard encode sets, and an empty query or
    /// fragment is preserved when present in the URL record.
    ///
    /// @return the serialized URL
    String href();

    /// Returns the serialized origin.
    ///
    /// For tuple origins such as `http`, `https`, `ws`, `wss`, and `ftp`, the result is
    /// `scheme://host` plus a non-default port when present. For `blob` URLs, the origin is derived from the
    /// URL embedded in the blob path when that embedded URL has an HTTP(S) origin. For `file` URLs, opaque
    /// origins, and other schemes without a tuple origin, the result is the literal string `null`.
    ///
    /// This method never returns Java `null`. The string value `null` is the URL Standard serialization of an
    /// opaque origin.
    ///
    /// @return the non-null serialized origin; the literal string `null` represents an opaque origin
    String origin();

    /// Returns the scheme component without its delimiter.
    ///
    /// The scheme identifies the URL's parsing and serialization rules, such as `http`, `https`, `file`,
    /// or a non-special scheme such as `data`. It is the canonical URL Standard scheme name: it starts with
    /// an ASCII letter, contains only ASCII letters, ASCII digits, plus (`+`), hyphen (`-`), and period
    /// (`.`), and is normalized to lower case during parsing.
    ///
    /// The corresponding WHATWG `URL.protocol` attribute is this scheme followed by a trailing colon. This
    /// method returns only the logical scheme name because Java URL and URI APIs use the term `scheme` for this
    /// component.
    ///
    /// @return the scheme component without the trailing colon
    String getScheme();

    /// Returns the decoded username component.
    ///
    /// This method is the Java `URI`-style decoded view of the URL username. It is equivalent to
    /// {@link #getRawUsername()} with valid percent triplets decoded as UTF-8. It does not apply
    /// `application/x-www-form-urlencoded` rules and therefore does not treat plus (`+`) as a space.
    ///
    /// The result is `null` when the URL has no serialized credentials. When the URL has credentials with an
    /// empty username, the result is the empty string. Percent escapes that are invalid or incomplete are left
    /// unchanged.
    ///
    /// @return the decoded username component, or `null` when absent
    @Nullable String getUsername();

    /// Returns the raw username component.
    ///
    /// This method exposes the normalized, percent-encoded username stored in the URL record. It does not
    /// percent-decode the value. The result is `null` when the URL has no serialized credentials. When the URL
    /// has credentials with an empty username, the result is the empty string.
    ///
    /// The word "raw" has the same meaning as in Java `URI` raw component getters: the returned string is the
    /// serialized component with percent-encoding preserved. It is not necessarily a substring of the original
    /// input because URL parsing may normalize, percent-encode, or otherwise rewrite credentials.
    ///
    /// @return the raw username component, or `null` when absent
    @Nullable String getRawUsername();

    /// Returns the raw username component or the empty string when absent.
    ///
    /// This method is a null-free convenience view over {@link #getRawUsername()}. It does not percent-decode
    /// the returned value.
    ///
    /// @return the raw username component, or the empty string when absent
    String getRawUsernameOrEmpty();

    /// Returns the decoded password component.
    ///
    /// This method is the Java `URI`-style decoded view of the URL password. It is equivalent to
    /// {@link #getRawPassword()} with valid percent triplets decoded as UTF-8. It does not apply
    /// `application/x-www-form-urlencoded` rules and therefore does not treat plus (`+`) as a space.
    ///
    /// The result is `null` when the URL has no password component. Percent escapes that are invalid or
    /// incomplete are left unchanged.
    ///
    /// @return the decoded password component, or `null` when absent
    @Nullable String getPassword();

    /// Returns the raw password component.
    ///
    /// This method exposes the normalized, percent-encoded password stored in the URL record. It does not
    /// percent-decode the value. The result is `null` when the URL has no password component.
    ///
    /// The word "raw" has the same meaning as in Java `URI` raw component getters: the returned string is the
    /// serialized component with percent-encoding preserved. It is not necessarily a substring of the original
    /// input because URL parsing may normalize, percent-encode, or otherwise rewrite credentials.
    ///
    /// @return the raw password component, or `null` when absent
    @Nullable String getRawPassword();

    /// Returns the raw password component or the empty string when absent.
    ///
    /// This method is a null-free convenience view over {@link #getRawPassword()}. It does not percent-decode
    /// the returned value.
    ///
    /// @return the raw password component, or the empty string when absent
    String getRawPasswordOrEmpty();

    /// Returns the decoded user-info component.
    ///
    /// This method is the Java `URI`-style getter for the decoded URL user-info. It is equivalent to
    /// {@link #getRawUserInfo()} with valid percent triplets decoded as UTF-8. It does not include the trailing
    /// at-sign (`@`) that separates user-info from the host.
    ///
    /// User-info is present when the URL serialization contains credentials. When present, it consists of the
    /// username and, if a password component is serialized, a colon (`:`) followed by the password. Percent
    /// escapes that are invalid or incomplete are left unchanged.
    ///
    /// The result is `null` when the URL has no serialized credentials.
    ///
    /// @return the decoded user-info component, or `null` when absent
    @Nullable String getUserInfo();

    /// Returns the raw user-info component.
    ///
    /// This method is the Java `URI`-style getter for the URL user-info with percent-encoding preserved. It
    /// returns the normalized user-info serialization without the trailing at-sign (`@`). The returned value is
    /// not necessarily a substring of the original input because URL parsing may normalize, percent-encode, or
    /// otherwise rewrite credentials.
    ///
    /// User-info is present when the URL serialization contains credentials. When present, it consists of the
    /// raw username and, if a password component is serialized, a colon (`:`) followed by the raw password.
    ///
    /// The result is `null` when the URL has no serialized credentials.
    ///
    /// @return the raw user-info component, or `null` when absent
    @Nullable String getRawUserInfo();

    /// Returns the decoded authority component.
    ///
    /// This method is the Java `URI`-style getter for the decoded URL authority. It is equivalent to
    /// {@link #getRawAuthority()} with valid percent triplets decoded as UTF-8. It does not include the leading
    /// double solidus (`//`).
    ///
    /// The authority is present when the URL record has a host component. When present, it consists of the
    /// serialized credentials followed by at-sign (`@`) when credentials are present, the serialized host, and
    /// the serialized port prefixed by colon (`:`) when a non-default port is present. Domain hosts remain in
    /// their URL Standard ASCII form; this method does not convert Punycode labels back to Unicode. Percent
    /// escapes that are invalid or incomplete are left unchanged.
    ///
    /// The result is `null` when the URL record has no host component. A URL with an explicitly empty authority,
    /// such as a `file` URL with an empty host, returns the empty string.
    ///
    /// @return the decoded authority component, or `null` when absent
    @Nullable String getAuthority();

    /// Returns the raw authority component.
    ///
    /// This method is the Java `URI`-style getter for the URL authority with percent-encoding preserved. It
    /// returns the normalized authority serialization without the leading double solidus (`//`). The returned
    /// value is not necessarily a substring of the original input because URL parsing may normalize,
    /// percent-encode, or otherwise rewrite credentials, hosts, and ports.
    ///
    /// The authority is present when the URL record has a host component. When present, it consists of the raw
    /// username and optional raw password followed by at-sign (`@`) when credentials are present, the serialized
    /// host, and the serialized port prefixed by colon (`:`) when a non-default port is present.
    ///
    /// The result is `null` when the URL record has no host component. A URL with an explicitly empty authority,
    /// such as a `file` URL with an empty host, returns the empty string.
    ///
    /// @return the raw authority component, or `null` when absent
    @Nullable String getRawAuthority();

    /// Returns the host component.
    ///
    /// This method is the Java `URI`-style getter for the URL host. The returned value is the normalized
    /// serialized host without user information or port. Domain hosts are returned after URL Standard domain
    /// processing, so Unicode domain labels are represented in ASCII form. IPv4 hosts are returned in dotted
    /// decimal form. IPv6 hosts are returned in their compressed form enclosed in square brackets.
    ///
    /// The result is `null` when the URL record has no host component, such as for most opaque URLs. A URL with
    /// an explicitly empty host returns the empty string.
    ///
    /// @return the host component, or `null` when absent
    @Nullable String getHost();

    /// Returns the port component as an integer.
    ///
    /// This method follows the Java `URI` convention of returning `-1` when no port is stored in the URL
    /// record. WHATWG URL parsing normalizes default ports away, so an HTTPS URL with an explicit port of 443
    /// also returns `-1`.
    ///
    /// @return the normalized port, or `-1` when absent
    int getPort();

    /// Returns the decoded path component.
    ///
    /// This method is the Java `URI`-style getter for the decoded URL path. It is equivalent to
    /// {@link #getRawPath()} with valid percent triplets decoded as UTF-8. It does not apply
    /// `application/x-www-form-urlencoded` rules and therefore does not treat plus (`+`) as a space.
    ///
    /// Invalid or incomplete percent triplets are left unchanged. `WebURL` currently always has a path string,
    /// so this method never returns `null`.
    ///
    /// @return the decoded path component
    String getPath();

    /// Returns the raw path component.
    ///
    /// This method is the Java `URI`-style getter for the URL path. It returns the normalized, percent-encoded
    /// path serialization without applying percent-decoding.
    ///
    /// @return the raw path component
    String getRawPath();

    /// Returns the decoded query component.
    ///
    /// This method is the Java `URI`-style getter for the decoded URL query. It is equivalent to
    /// {@link #getRawQuery()} with valid percent triplets decoded as UTF-8. It does not include the leading
    /// question mark and does not apply `application/x-www-form-urlencoded` rules, so plus (`+`) remains plus.
    ///
    /// The result is `null` when the query component is absent. An empty query component is returned as the
    /// empty string.
    ///
    /// @return the decoded query component, or `null` when absent
    @Nullable String getQuery();

    /// Returns the raw query component.
    ///
    /// This method exposes the normalized, percent-encoded query stored in the URL record without the leading
    /// question mark. It follows the Java `URI` convention of returning `null` when the query component is
    /// absent. An empty query component is returned as the empty string.
    ///
    /// @return the raw query component, or `null` when absent
    @Nullable String getRawQuery();

    /// Returns the raw query component or the empty string when absent.
    ///
    /// This method is a null-free convenience view over {@link #getRawQuery()}. It does not include the leading
    /// question mark and does not percent-decode the returned value.
    ///
    /// @return the raw query component, or the empty string when absent
    String getRawQueryOrEmpty();

    /// Returns the decoded fragment component.
    ///
    /// This method is the Java `URI`-style getter for the decoded URL fragment. It is equivalent to
    /// {@link #getRawFragment()} with valid percent triplets decoded as UTF-8. It does not include the leading
    /// number sign.
    ///
    /// The result is `null` when the fragment component is absent. An empty fragment component is returned as
    /// the empty string.
    ///
    /// @return the decoded fragment component, or `null` when absent
    @Nullable String getFragment();

    /// Returns the raw fragment component.
    ///
    /// This method exposes the normalized, percent-encoded fragment stored in the URL record without the leading
    /// number sign. It follows the Java `URI` convention of returning `null` when the fragment component is
    /// absent. An empty fragment component is returned as the empty string.
    ///
    /// @return the raw fragment component, or `null` when absent
    @Nullable String getRawFragment();

    /// Returns the raw fragment component or the empty string when absent.
    ///
    /// This method is a null-free convenience view over {@link #getRawFragment()}. It does not include the
    /// leading number sign and does not percent-decode the returned value.
    ///
    /// @return the raw fragment component, or the empty string when absent
    String getRawFragmentOrEmpty();

    /// Returns the serialized URL converted to RFC 2396 URI syntax.
    ///
    /// The returned string is suitable for Java `URI` parsing when this URL has an RFC 2396 representation.
    /// Existing valid percent escapes are preserved, and bare percent signs or characters accepted by WHATWG
    /// URL serialization but rejected by Java URI syntax are percent-encoded.
    ///
    /// Some WHATWG URLs, such as non-special URLs with an empty opaque path and no query, have no corresponding
    /// absolute RFC 2396 URI because Java `URI` requires a non-empty scheme-specific part.
    ///
    /// @return the RFC 2396 URI string
    String toRFC2396String();

    /// Returns the serialized URL as a Java `URI`.
    ///
    /// The URI is constructed from `toRFC2396String()`.
    ///
    /// @return a Java `URI` representing this URL
    /// @throws IllegalStateException when this URL has no RFC 2396 representation accepted by Java `URI`
    URI toURI();

    /// Returns the serialized URL as a Java `URL`.
    ///
    /// The result is obtained from `toURI().toURL()`. Java `URL` supports only schemes for which the runtime has
    /// a URL stream handler, so some valid WHATWG URLs cannot be represented as a Java `URL`.
    ///
    /// @return a Java `URL` representing this URL
    /// @throws MalformedURLException when Java has no URL handler for the scheme or rejects the URL
    URL toURL() throws MalformedURLException;

    /// Compares this URL with another URL by serialized URL string.
    ///
    /// The comparison is equivalent to `this.href().compareTo(other.href())`. It is consistent with
    /// `equals(Object)`: if this method returns zero, the two URL objects are equal according to this
    /// interface's equality contract.
    ///
    /// @param other the URL to compare with this URL
    /// @return a negative integer, zero, or a positive integer as this URL's serialized form is less than,
    /// equal to, or greater than the other URL's serialized form
    @Override
    int compareTo(WebURL other);

    /// Compares this URL with another object for equality.
    ///
    /// A URL is equal to another `WebURL` when both objects have the same complete WHATWG URL serialization.
    /// Objects that do not implement `WebURL`, including Java `null`, are not equal to a URL.
    ///
    /// @param obj the object to compare with this URL, or `null`
    /// @return `true` if the object is a `WebURL` with the same serialized URL, otherwise `false`
    @Override
    boolean equals(@Nullable Object obj);

    /// Returns the hash code of this URL.
    ///
    /// The hash code is the hash code of the complete WHATWG URL serialization returned by `href()`, so equal
    /// URLs have equal hash codes.
    ///
    /// @return the serialized URL hash code
    @Override
    int hashCode();

    /// Returns the serialized URL.
    ///
    /// This is identical to `href()`.
    ///
    /// @return the serialized URL
    @Override
    String toString();
}
