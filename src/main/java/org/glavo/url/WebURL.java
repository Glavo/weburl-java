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
@NotNullByDefault
public sealed interface WebURL permits WebURLImpl {
    /// Creates a URL by parsing an absolute input string.
    ///
    /// The input is parsed by the WHATWG basic URL parser with no base URL. Relative inputs therefore fail.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails with a known URL validation error
    /// @throws IllegalArgumentException when parsing fails without a specific public validation error
    static WebURL of(String input) {
        return WebURLImpl.of(input);
    }

    /// Creates a URL by parsing an input string against a base URL string.
    ///
    /// The base is parsed first. The input may be either absolute or relative to that base.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails with a known URL validation error
    /// @throws IllegalArgumentException when either input fails without a specific public validation error
    static WebURL of(String input, String base) {
        return WebURLImpl.of(input, base);
    }

    /// Creates a URL by parsing an input string against a base URL.
    ///
    /// The input may be either absolute or relative to the supplied base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails with a known URL validation error
    /// @throws IllegalArgumentException when parsing fails without a specific public validation error
    static WebURL of(String input, WebURL base) {
        return WebURLImpl.of(input, base);
    }

    /// Parses an absolute input string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `of(String)`, except failures are represented by `null`
    /// instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if parsing fails
    static @Nullable WebURL parse(String input) {
        return WebURLImpl.parse(input);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `of(String, String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    static @Nullable WebURL parse(String input, String base) {
        return WebURLImpl.parse(input, base);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `of(String, WebURL)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL, or `null` if the input cannot be parsed against the base
    static @Nullable WebURL parse(String input, WebURL base) {
        return WebURLImpl.parse(input, base);
    }

    /// Returns whether an input string can be parsed as an absolute URL.
    ///
    /// @param input the URL input string
    /// @return `true` if parsing succeeds, otherwise `false`
    static boolean canParse(String input) {
        return WebURLImpl.canParse(input);
    }

    /// Returns whether an input string can be parsed against a base URL string.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return `true` if the base parses and the input parses against it, otherwise `false`
    static boolean canParse(String input, String base) {
        return WebURLImpl.canParse(input, base);
    }

    /// Returns whether an input string can be parsed against a base URL.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return `true` if the input parses against the base, otherwise `false`
    static boolean canParse(String input, WebURL base) {
        return WebURLImpl.canParse(input, base);
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
    /// @return the serialized origin, or `null` as a string for an opaque origin
    String origin();

    /// Returns the protocol component.
    ///
    /// The protocol is the URL scheme followed by a trailing colon. The scheme itself is normalized to lower
    /// case during parsing.
    ///
    /// @return the protocol, including the trailing colon
    String protocol();

    /// Returns a URL with the protocol component updated.
    ///
    /// The supplied value is interpreted like assignment to the WHATWG `URL.protocol` attribute. A trailing
    /// colon may be supplied but is not required. The change is ignored when the URL Standard forbids the
    /// transition, for example between special and non-special schemes in cases that would change URL shape.
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

    /// Returns the serialized URL as a Java `URI`.
    ///
    /// The URI is constructed from this URL's parsed components rather than by reparsing `href()`. Components
    /// that are already percent-encoded in the URL record are supplied to Java `URI` as component values so that
    /// valid escapes are not double-encoded. Characters accepted by WHATWG URL serialization but rejected by
    /// Java URI syntax are percent-encoded in the returned URI.
    ///
    /// @return a Java `URI` representing this URL
    URI toURI();

    /// Returns the serialized URL as a Java `URL`.
    ///
    /// The result is obtained from `toURI().toURL()`. Java `URL` supports only schemes for which the runtime has
    /// a URL stream handler, so some valid WHATWG URLs cannot be represented as a Java `URL`.
    ///
    /// @return a Java `URL` representing this URL
    /// @throws MalformedURLException when Java has no URL handler for the scheme or rejects the URL
    URL toURL() throws MalformedURLException;

    /// Returns the JSON representation of this URL.
    ///
    /// This is identical to `href()`, matching the WHATWG `URL.toJSON()` operation.
    ///
    /// @return the serialized URL
    String toJSON();

    /// Returns the serialized URL.
    ///
    /// This is identical to `href()`.
    ///
    /// @return the serialized URL
    @Override
    String toString();
}
