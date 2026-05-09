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
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/// An immutable Java representation of a WHATWG URL.
///
/// A URL has a `scheme` and either an opaque path or a hierarchical structure made from an optional
/// authority, a path, an optional query, and an optional fragment. This interface exposes the same component
/// names and serialization rules as the WHATWG `URL` interface: `protocol` is the scheme plus `:`,
/// `host` is the host plus an optional port, `search` is the query plus a leading `?`, and `hash` is the
/// fragment plus a leading `#`.
///
/// Instances are immutable. Methods whose names start with `with` return a URL whose corresponding component
/// has been updated through the same parser state used by the URL Standard. When the standard says that a
/// component cannot be changed for a given URL form, the original URL is returned unchanged.
///
/// The serialized form returned by `href()`, `toString()`, and `toJSON()` is the WHATWG URL serialization.
/// It is not identical to Java `URI` syntax for all inputs; use `toURI()` to obtain a Java `URI` value.
///
/// Use `WebURLFactory` when URL creation should be configured with an explicit IDNA profile or other factory
/// settings. The static methods on this interface use `WebURLFactory.defaultFactory()`.
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
    static WebURL parseURL(String input) {
        return WebURLFactory.defaultFactory().parseURL(input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The base is parsed first. The input may be either absolute or relative to that base.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails
    static WebURL parseURL(String input, String base) {
        return WebURLFactory.defaultFactory().parseURL(input, base);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The input may be either absolute or relative to the supplied base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    static WebURL parseURL(String input, WebURL base) {
        return WebURLFactory.defaultFactory().parseURL(input, base);
    }

    /// Parses an absolute input string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseURL(String)`, except failures are represented by `null`
    /// instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if parsing fails
    static @Nullable WebURL tryParseURL(String input) {
        return WebURLFactory.defaultFactory().tryParseURL(input);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseURL(String, String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    static @Nullable WebURL tryParseURL(String input, String base) {
        return WebURLFactory.defaultFactory().tryParseURL(input, base);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseURL(String, WebURL)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL, or `null` if the input cannot be parsed against the base
    static @Nullable WebURL tryParseURL(String input, WebURL base) {
        return WebURLFactory.defaultFactory().tryParseURL(input, base);
    }

    /// Returns whether an input string can be parsed as an absolute URL.
    ///
    /// @param input the URL input string
    /// @return `true` if parsing succeeds, otherwise `false`
    static boolean canParseURL(String input) {
        return WebURLFactory.defaultFactory().canParseURL(input);
    }

    /// Returns whether an input string can be parsed against a base URL string.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return `true` if the base parses and the input parses against it, otherwise `false`
    static boolean canParseURL(String input, String base) {
        return WebURLFactory.defaultFactory().canParseURL(input, base);
    }

    /// Returns whether an input string can be parsed against a base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return `true` if the input parses against the base, otherwise `false`
    static boolean canParseURL(String input, WebURL base) {
        return WebURLFactory.defaultFactory().canParseURL(input, base);
    }

    /// Parses a browser address input and returns the parsed URL.
    ///
    /// This method uses `WebURLFactory.defaultFactory().parseAddress(input)`. It accepts standard absolute
    /// URL strings and browser address bar style URL inputs such as bare domain names, `//`-prefixed
    /// authorities, `localhost` with a port, IP addresses, and bracketed IPv6 addresses. Inputs without an
    /// explicit scheme are completed with the default factory's address scheme.
    ///
    /// This method does not implement search fallback. Inputs that are neither URL strings nor recognized
    /// browser address inputs fail instead of being interpreted as search terms.
    ///
    /// @param input the browser address input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails
    static WebURL parseAddress(String input) {
        return WebURLFactory.defaultFactory().parseAddress(input);
    }

    /// Parses a browser address input and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parseAddress(String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the browser address input string
    /// @return the parsed URL, or `null` if parsing fails
    static @Nullable WebURL tryParseAddress(String input) {
        return WebURLFactory.defaultFactory().tryParseAddress(input);
    }

    /// Returns whether a browser address input can be parsed.
    ///
    /// This method has the same parser behavior as `parseAddress(String)`, except it returns a boolean result.
    ///
    /// @param input the browser address input string
    /// @return `true` if parsing succeeds, otherwise `false`
    static boolean canParseAddress(String input) {
        return WebURLFactory.defaultFactory().canParseAddress(input);
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
    /// The scheme is the same logical component exposed by {@link #protocol()}, but without the trailing
    /// colon delimiter. For every URL, `protocol()` is exactly `scheme() + ":"`. Use this method when the
    /// scheme name itself is needed for comparison, dispatch, or Java-style terminology; use
    /// {@link #protocol()} when matching the WHATWG `URL.protocol` attribute.
    ///
    /// @return the scheme component without the trailing colon
    String scheme();

    /// Returns a URL with the scheme component updated.
    ///
    /// The supplied value is interpreted as a URL scheme name. A trailing colon is accepted for consistency
    /// with {@link #withProtocol(String)}, but it is not part of the logical scheme value. This method and
    /// {@link #withProtocol(String)} update the same component and differ only in the preferred spelling of
    /// their input. The change is parsed through the URL Standard scheme state and is ignored when the URL
    /// Standard forbids the transition, for example between special and non-special schemes in cases that
    /// would change URL shape.
    ///
    /// @param value the new scheme value, with or without a trailing colon
    /// @return the updated URL, or this URL when the update is not permitted
    WebURL withScheme(String value);

    /// Returns the protocol component.
    ///
    /// The protocol is the Web API view of the URL scheme: the lower-case scheme name followed by the
    /// trailing colon delimiter. It is not a separate parsed field. For every URL, this method returns exactly
    /// `scheme() + ":"`.
    ///
    /// The name follows the WHATWG `URL.protocol` attribute, where values are serialized with the colon
    /// included, such as `https:` or `data:`. The delimiter belongs to this serialized protocol view, not to
    /// the logical scheme returned by {@link #scheme()}.
    ///
    /// @return the protocol, including the trailing colon
    String protocol();

    /// Returns a URL with the protocol component updated.
    ///
    /// The supplied value is interpreted like assignment to the WHATWG `URL.protocol` attribute. A trailing
    /// colon may be supplied but is not required. This method and {@link #withScheme(String)} update the same
    /// component; this method is named for callers that model the value as a Web API protocol string. The
    /// change is ignored when the URL Standard forbids the transition, for example between special and
    /// non-special schemes in cases that would change URL shape.
    ///
    /// @param value the new protocol value
    /// @return the updated URL, or this URL when the update is not permitted
    WebURL withProtocol(String value);

    /// Returns the username component.
    ///
    /// The username is the percent-encoded user name subcomponent of the authority. It is empty when the URL
    /// has no authority credentials or cannot have credentials.
    ///
    /// @return the username component without the password separator or `@`
    String username();

    /// Returns a URL with the username component updated.
    ///
    /// The value is encoded with the URL Standard userinfo percent-encode set. The update is ignored for URLs
    /// that cannot have credentials, including URLs with no host, an empty host, or the `file` scheme.
    ///
    /// @param value the new username value before percent-encoding
    /// @return the updated URL, or this URL when the URL cannot have credentials
    WebURL withUsername(String value);

    /// Returns the password component.
    ///
    /// The password is the percent-encoded password subcomponent of the authority. It is empty when the URL
    /// has no password or cannot have credentials.
    ///
    /// @return the password component without the leading colon or trailing `@`
    String password();

    /// Returns a URL with the password component updated.
    ///
    /// The value is encoded with the URL Standard userinfo percent-encode set. The update is ignored for URLs
    /// that cannot have credentials, including URLs with no host, an empty host, or the `file` scheme.
    ///
    /// @param value the new password value before percent-encoding
    /// @return the updated URL, or this URL when the URL cannot have credentials
    WebURL withPassword(String value);

    /// Returns the host component.
    ///
    /// The host is the serialized hostname plus `:` and the serialized port when a non-default port is present.
    /// Domain hosts are ASCII-serialized, IPv6 hosts include square brackets, and absent hosts are represented
    /// by the empty string.
    ///
    /// @return the host component, including the port when present
    String host();

    /// Returns a URL with the host component updated.
    ///
    /// The value is parsed through the WHATWG host state and may include a port. The update is ignored for URLs
    /// with an opaque path. For special schemes, host parsing applies domain, IPv4, IPv6, IDNA, and default-port
    /// normalization rules.
    ///
    /// @param value the new host value
    /// @return the updated URL, or this URL when the update is not permitted or parsing fails
    WebURL withHost(String value);

    /// Returns the hostname component.
    ///
    /// The hostname is the serialized host without the port. Domain hosts are ASCII-serialized and IPv6 hosts
    /// include square brackets. An absent host is represented by the empty string.
    ///
    /// @return the hostname component without the port
    String hostname();

    /// Returns a URL with the hostname component updated.
    ///
    /// The value is parsed through the WHATWG hostname state and cannot set a port. The update is ignored for
    /// URLs with an opaque path.
    ///
    /// @param value the new hostname value
    /// @return the updated URL, or this URL when the update is not permitted or parsing fails
    WebURL withHostname(String value);

    /// Returns the port component.
    ///
    /// The port is serialized as decimal digits. It is the empty string when no port is present or when the
    /// parsed port is the default port for the scheme.
    ///
    /// @return the port as a string, or the empty string when absent
    String port();

    /// Returns a URL with the port component updated.
    ///
    /// The value is parsed through the WHATWG port state. Supplying the empty string removes the port. A default
    /// port is normalized away. The update is ignored for URLs that cannot have a username, password, or port.
    ///
    /// @param value the new port value
    /// @return the updated URL, or this URL when the update is not permitted or parsing fails
    WebURL withPort(String value);

    /// Returns the pathname component.
    ///
    /// For hierarchical URLs, the pathname is the serialized path, beginning with `/` when the URL has a path
    /// segment list. For URLs with an opaque path, the pathname is the opaque path string and does not
    /// necessarily begin with `/`.
    ///
    /// @return the serialized pathname
    String pathname();

    /// Returns a URL with the pathname component updated.
    ///
    /// The value is parsed through the WHATWG path-start state after clearing the current path. Dot-segment and
    /// Windows drive-letter rules are applied where required. The update is ignored for URLs with an opaque path.
    ///
    /// @param value the new pathname value
    /// @return the updated URL, or this URL when the URL has an opaque path
    WebURL withPathname(String value);

    /// Returns the search component.
    ///
    /// The search component is the query prefixed by `?`. It is the empty string when the URL has no query or
    /// has an empty query. Use `href()` to distinguish a URL with an empty query from one with no query.
    ///
    /// @return the query prefixed by `?`, or the empty string when absent or empty
    String search();

    /// Returns a URL with the search component updated.
    ///
    /// A leading `?` in the supplied value is ignored. The remaining input is parsed through the WHATWG query
    /// state and encoded with the appropriate query percent-encode set for the URL scheme. Supplying the empty
    /// string removes the query.
    ///
    /// @param value the new search value
    /// @return the updated URL
    WebURL withSearch(String value);

    /// Returns immutable search parameters parsed from the current query.
    ///
    /// The returned object is a detached immutable view of the query interpreted as
    /// `application/x-www-form-urlencoded` data. Mutating operations on that object return a new parameter list
    /// and do not update this URL unless the result is passed to `withSearchParams(WebURLSearchParams)`.
    ///
    /// @return the search parameters in tuple order
    WebURLSearchParams searchParams();

    /// Returns a URL with the query replaced by serialized search parameters.
    ///
    /// The supplied parameters are serialized using the `application/x-www-form-urlencoded` serializer. An empty
    /// serialization removes the query from the returned URL.
    ///
    /// @param value the replacement search parameter list
    /// @return the updated URL
    WebURL withSearchParams(WebURLSearchParams value);

    /// Returns the hash component.
    ///
    /// The hash component is the fragment prefixed by `#`. It is the empty string when the URL has no fragment
    /// or has an empty fragment. Use `href()` to distinguish a URL with an empty fragment from one with no
    /// fragment.
    ///
    /// @return the fragment prefixed by `#`, or the empty string when absent or empty
    String hash();

    /// Returns a URL with the hash component updated.
    ///
    /// A leading `#` in the supplied value is ignored. The remaining input is parsed through the WHATWG fragment
    /// state and encoded with the fragment percent-encode set. Supplying the empty string removes the fragment.
    ///
    /// @param value the new hash value
    /// @return the updated URL
    WebURL withHash(String value);

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
