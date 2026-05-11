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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

/// An immutable, absolute URL value parsed and normalized according to the
/// [WHATWG URL Standard](https://url.spec.whatwg.org/).
///
/// # Overview
///
/// `WebURL` provides browser-compatible URL handling in Java. It implements the same URL parsing,
/// normalization, and serialization rules used by Chrome, Firefox, and Safari, making it a modern
/// alternative to `java.net.URI` and `java.net.URL` when WHATWG URL conformance matters.
///
/// Every `WebURL` is **absolute**: it has a non-empty scheme, and any relative reference passed
/// to a base-aware {@code parse} or {@code tryParse} method is resolved against the base *before*
/// a `WebURL` is returned. Once created, a `WebURL` is deeply immutable — its complete identity is
/// the [serialized URL](https://url.spec.whatwg.org/#concept-url-serializer) returned by
/// {@link #href()}.
///
/// This library has **no runtime dependencies** beyond the `java.base` module.
///
/// # Quick Start
///
/// The simplest way to create a `WebURL` is {@link #parse(String)}:
///
/// ```java
/// WebURL url = WebURL.parse("https://example.com/path?q=1#frag");
/// ```
///
/// Resolve relative references against a base URL with {@link #parse(String, String)} or
/// {@link #parse(String, WebURL)}:
///
/// ```java
/// WebURL base = WebURL.parse("https://example.com/a/b/c");
/// WebURL resolved = WebURL.parse("../d?e#f", base);
/// // resolved.href() == "https://example.com/a/d?e#f"
/// ```
///
/// The {@link #tryParse} variants return `null` on failure instead of throwing
/// {@link WebURLParseException}:
///
/// ```java
/// WebURL url = WebURL.tryParse(userInput);
/// if (url == null) {
///     // handle invalid input
/// }
/// ```
///
/// {@link #parseBrowserInput(String)} handles user-typed address-bar-style input, automatically
/// prepending `https://` or `http://` when appropriate. It is intended for interactive text
/// entry only, *not* for stable serialization:
///
/// ```java
/// WebURL url = WebURL.parseBrowserInput("example.com/search?q=hello");
/// // url.href() == "https://example.com/search?q=hello"
/// ```
///
/// # Parser Modes
///
/// The static convenience methods on this interface use the **default parser**, which accepts
/// recoverable validation errors per the WHATWG URL Standard. For stricter parsing, obtain a strict
/// parser from {@link WebURLParser#getStrict()}:
///
/// ```java
/// // Default: recovers from non-fatal issues
/// WebURL url = WebURL.parse("https://example.com:443/");
///
/// // Strict: rejects all validation errors
/// WebURLParser strict = WebURLParser.getStrict();
/// WebURL url = strict.parse("https://example.com/path");
/// ```
///
/// The default and strict parsers are both immutable and thread-safe.
///
/// # URL Components
///
/// A WHATWG URL has these logical components, each with dedicated getters:
///
/// | Component  | Always present? | Raw getter        | Decoded getter  | Null when absent?       |
/// |------------|:---------------:|-------------------|-----------------|-------------------------|
/// | scheme     | Yes             | `getScheme()`     | —               | Never                   |
/// | username   | No              | `getRawUsername()`| `getUsername()` | Yes                     |
/// | password   | No              | `getRawPassword()`| `getPassword()` | Yes                     |
/// | host       | No              | `getHost()`       | —               | Yes                     |
/// | port       | No              | `getRawPort()`    | `getPort()`     | `getRawPort()` only     |
/// | path       | Yes             | `getRawPath()`    | `getPath()`     | Never                   |
/// | query      | No              | `getRawQuery()`   | `getQuery()`    | Yes                     |
/// | fragment   | No              | `getRawFragment()`| `getFragment()` | Yes                     |
///
/// Getters named `Raw` return the component exactly as serialized, with percent-encoding
/// preserved. Decoded getters (without `Raw`) decode valid percent triplets as UTF-8; invalid
/// or incomplete triplets are left unchanged. The {@code application/x-www-form-urlencoded}
/// rules are **never** applied — plus (`+`) remains plus.
///
/// The main serialization shape for a URL with a host is:
///
/// ```
/// scheme://[username[:password]@]host[:port]path[?query][#fragment]
/// ```
///
/// ### Scheme
///
/// Identifies the URL's processing and serialization rules (e.g., `http`, `https`, `file`,
/// `data`, `blob`, `ws`). The scheme is always present, stored in lowercase ASCII, and returned
/// by {@link #getScheme()} without the trailing colon. Special schemes recognized by the URL
/// Standard (`http`, `https`, `file`, `ws`, `wss`, `ftp`) receive special processing rules for
/// hosts, ports, and path serialization.
///
/// ### Username and Password
///
/// The optional credentials are stored and serialized as `username[:password]@` before the
/// host. When the URL was constructed without credentials, {@link #getRawUsername()} returns
/// `null`. A present but empty username (e.g., `://@host` or `://:p@host`) returns the empty
/// string.
///
/// ### Host
///
/// An optional component that makes the URL hierarchical and gives it an authority portion.
/// Four host types exist:
///
/// - **Domain hosts** are processed with IDNA domain-to-ASCII rules (Punycode encoding for
///   non-ASCII labels). The returned host string is ASCII, even when the original input
///   contained Unicode characters.
/// - **IPv4 hosts** are always normalized to dotted decimal form. Non-decimal forms such as
///   hex (`0x7f000001`), octal, and integer (decimal) are all normalized to dotted decimal
///   (e.g., `127.0.0.1`).
/// - **IPv6 hosts** are compressed per RFC 5952 and enclosed in square brackets.
/// - **Opaque hosts** are used by non-special URLs and are rendered without brackets.
///
/// Some hierarchical URLs (such as `file:///path`) have an empty host. Opaque URLs (such as
/// `data:text/plain,hi`) have no host at all. {@link #getHost()} returns `null` when there is
/// no host component, and the empty string when the host is explicitly empty.
///
/// ### Port
///
/// An optional decimal port number. The URL Standard removes default ports during
/// normalization — `https://example.com:443/` has no serialized port component even though
/// the underlying port is 443. Use {@link #getRawPort()} to check whether a port is stored
/// in the serialized URL; use {@link #getPort()} to obtain the effective port (stored port,
/// default port for the scheme, or `-1`).
///
/// ### Path
///
/// Always present. Hierarchical URLs (those with a host) have a slash-separated path made
/// from path segments. Special URLs always serialize the path with a leading `/`. Non-special
/// URLs can have an opaque path whose syntax is not a slash-separated hierarchy.
///
/// ### Query and Fragment
///
/// Both are optional. An absent query/fragment returns `null`; a present but empty one
/// (e.g., trailing `?` or `#`) returns the empty string. Raw getters do not include the
/// leading delimiters (`?` and `#`).
///
/// # Normalization
///
/// Creating a `WebURL` applies the following normalizations, which may change the input
/// string substantially:
///
/// - **Scheme** is lowercased to ASCII.
/// - **Tab (`\t`) and newline (`\n`, `\r`)** are stripped from the input before parsing.
/// - **Backslash (`\`)** is converted to forward slash (`/`) in special URLs.
/// - **Domain hosts** are processed through IDNA ToASCII: Unicode labels are mapped and
///   Punycode-encoded. The input `https://münchen.de/` becomes `https://xn--mnchen-3ya.de/`.
/// - **IPv4 hosts** are normalized to dotted decimal: hex, octal, and integer forms are
///   all reduced to `a.b.c.d` notation.
/// - **IPv6 hosts** are compressed per RFC 5952.
/// - **Default ports** are removed from the serialization. The scheme's default port is
///   still available via {@link #getPort()}.
/// - **Dot segments** (`..` and `.`) in the path are resolved.
/// - **Percent-encoding** is applied according to URL Standard encode sets, and certain
///   already-encoded characters may be re-encoded when they belong to a component's encode
///   set.
///
/// As a result, the input `"HTTPS://EXAMPLE.COM:443/../a/./b?c"` becomes
/// `"https://example.com/a/b?c"`.
///
/// This normalization means that `WebURL` **does not preserve the original input spelling**.
/// Use `java.net.URI` when round-tripping the original input string is required, or serialize
/// with {@link #href()} and parse back for a stable canonical form.
///
/// # Comparison with `java.net.URI`
///
/// `WebURL` and {@link java.net.URI} differ in specification, normalization, and semantics.
/// Understanding these differences is essential when choosing between them.
///
/// ## Specification and Philosophy
///
/// | Aspect             | `WebURL`                              | `java.net.URI`                             |
/// |--------------------|---------------------------------------|--------------------------------------------|
/// | Specification      | WHATWG URL Living Standard            | RFC 2396, updated by RFC 2732             |
/// | Design goal        | Match browser behavior for the web    | Model the generic hierarchical URI syntax  |
/// | Relative references | Resolved against a base; `WebURL` is always absolute | URIs can be either absolute or relative |
/// | Input preservation | Always normalized; original input may be rewritten | Preserves original input string        |
///
/// ## Normalization Differences
///
/// | Behavior               | `WebURL`                                            | `java.net.URI`                                       |
/// |------------------------|-----------------------------------------------------|------------------------------------------------------|
/// | Scheme case            | Always lowercased                                   | Preserved as-is                                      |
/// | Domain host            | IDNA ToASCII (Punycode) applied to non-ASCII labels  | No IDNA processing; `new URI(String)` treats non-ASCII host as opaque |
/// | Non-ASCII in host      | Converted to Punycode (e.g., `münchen` → `xn--mnchen-3ya`) | Rejected by `new URI(String)` unless percent-encoded |
/// | IPv4 normalization     | Always dotted decimal (hex/octal/integer → dotted)  | Does not normalize hex/octal/decimal forms          |
/// | IPv6 compression       | RFC 5952 compression applied                        | Does not apply RFC 5952 compression                 |
/// | Default port           | Removed from serialization                          | Preserved                                            |
/// | Backslash in path      | Converted to `/` in special URLs                    | Treated literally                                    |
/// | Tab and newline        | Stripped from input                                 | Treated literally; passed through                    |
/// | Percent-encoding       | Applied per URL Standard encode sets; may re-encode | Minimal encoding by `new URI(String)`; raw constructors preserve input |
///
/// ## Component Semantics
///
/// | Aspect              | `WebURL`                                                      | `java.net.URI`                                |
/// |---------------------|---------------------------------------------------------------|-----------------------------------------------|
/// | Empty vs absent host | Hierarchical URLs can have an empty host; opaque URLs have no host | Host is always `null` for opaque URIs    |
/// | Path                | Always present; never `null`                                  | Always present; never `null`                  |
/// | Username/Password   | Separate `username` and `password` components                 | Single `user-info` string; `:` separates them |
/// | Query and Fragment  | `null` when absent, empty string when delimiter is present with nothing after | Same convention |
///
/// ### Equality Semantics
///
/// `WebURL` equality is defined by the complete serialized URL string ({@link #href()}).
/// Two `WebURL` objects are equal when their serializations are character-by-character identical.
///
/// `java.net.URI` equality is component-based: two URIs are equal when their schemes,
/// authorities, paths, queries, and fragments are all equal. `URI.equals()` uses case-insensitive
/// host comparison per RFC 3986 and may produce different results from `WebURL.equals()` for the
/// same logical URL.
///
/// ```java
/// // WebURL: serialization-based equality
/// WebURL a = WebURL.parse("https://EXAMPLE.COM/");  // → "https://example.com/"
/// WebURL b = WebURL.parse("https://example.com/");  // → "https://example.com/"
/// assert a.equals(b);  // true — both serialize to the same string
///
/// // URI: component-based equality
/// URI ua = new URI("https://EXAMPLE.COM/");
/// URI ub = new URI("https://example.com/");
/// assert ua.equals(ub);  // true — host comparison is case-insensitive
/// assert !ua.toString().equals(ub.toString());  // toString() preserves original case
/// ```
///
/// ### Opaque and Non-Special URLs
///
/// `URI` has a concept of "opaque" URIs (those without a scheme-specific-part starting with
/// `/`). `WebURL` has "non-special" URLs whose host handling and path serialization differ
/// from special URLs. An opaque `URI` and a non-special `WebURL` for the same input string
/// may have different component representations.
///
/// ### Conversion
///
/// Use {@link #toURI()} or {@link #toRFC2396String()} when a `java.net.URI` is required:
///
/// ```java
/// WebURL url = WebURL.parse("https://example.com/path?a=1");
/// URI uri = url.toURI();
/// ```
///
/// A one-step static convenience method is also available:
///
/// ```java
/// URI uri = WebURL.toURI("https://example.com/path");
/// ```
///
/// Not all WHATWG URLs have a valid RFC 2396 representation. Non-special URLs with an empty
/// opaque path and no query, for example, have no absolute RFC 2396 URI (Java `URI` requires a
/// non-empty scheme-specific part). Calling `toURI()` or `toRFC2396String()` on such URLs throws
/// {@link IllegalStateException}.
///
/// # Comparison with `java.net.URL`
///
/// `WebURL` differs from {@link java.net.URL} even more substantially than from `URI`:
///
/// - **No network I/O.** `java.net.URL` may perform host name resolution during `equals()`,
///   `hashCode()`, and `sameFile()`. `WebURL` never performs network I/O of any kind.
/// - **No protocol handlers.** `java.net.URL` requires a `URLStreamHandler` for each scheme.
///   Schemes without a handler cannot be constructed. `WebURL` supports all schemes.
/// - **No DNS caching.** `java.net.URL` caches DNS results internally, making it unsafe for
///   DNS rebinding protection and problematic in concurrent environments. `WebURL` does not
///   cache any network state.
/// - **Equality.** `java.net.URL` equality can be hostname-resolution-dependent: two URLs
///   with different host names that resolve to the same IP address may compare equal.
///   `WebURL` equality is always based on the serialized string alone.
///
/// Use `WebURL` for any URL handling beyond low-level `URL.openConnection()` calls. Convert
/// via {@link #toURL()} when a `java.net.URL` is required by an existing API.
///
/// # Thread Safety
///
/// `WebURL` objects are immutable and safe for concurrent use from multiple threads without
/// external synchronization. All state is fixed at construction time. The parsers returned
/// by {@link WebURLParser#getDefault()} and {@link WebURLParser#getStrict()} are also
/// thread-safe.
///
/// # Serialization
///
/// `WebURL` implements {@link Serializable}. It uses the serialization proxy pattern: only
/// the `href()` string is written, and deserialization re-parses it through
/// {@link #parse(String)}. This ensures cross-version compatibility without exposing
/// internal representation details.
///
/// # Equality, Hash Code, and Ordering
///
/// Equality, hash code, and natural ordering are all defined by the complete WHATWG URL
/// serialization returned by {@link #href()}. Two `WebURL` objects are equal when their
/// serialized URLs are equal character by character. {@link #compareTo(WebURL)} orders URLs
/// by the lexicographic order of those serialized strings. These definitions are consistent:
/// `compareTo` returning zero implies `equals` returning `true`.
@NotNullByDefault
public sealed interface WebURL extends Comparable<WebURL>, Serializable
        permits WebURLImpl {
    /// Creates a URL from a Java `URI`.
    ///
    /// The URI is converted with {@link URI#toString()} and then processed as an absolute URL input. Relative
    /// URI values and URI values whose string form is not accepted as a WHATWG URL fail in the same way as
    /// {@link #parse(String)}.
    ///
    /// This method does not use Java `URI` component getters. Existing percent escapes, Unicode characters,
    /// opaque URI syntax, and authority syntax are interpreted through the same URL processing rules as string
    /// input.
    ///
    /// @param uri the Java URI to convert
    /// @return the converted URL
    /// @throws WebURLParseException when the URI string is not accepted as an absolute URL
    static WebURL of(URI uri) {
        return parse(uri.toString());
    }

    /// Creates a URL from a Java `URL`.
    ///
    /// The URL is converted with {@link URL#toExternalForm()} and then processed as an absolute URL input. This
    /// method does not open a connection, resolve the host name, or use Java `URL` equality semantics.
    ///
    /// @param url the Java URL to convert
    /// @return the converted URL
    /// @throws WebURLParseException when the URL's external form is not accepted as an absolute URL
    static WebURL of(URL url) {
        return parse(url.toExternalForm());
    }

    /// Creates a file URL from a path.
    ///
    /// The path is first converted with {@link Path#toUri()}, then converted as a Java `URI` by
    /// {@link #of(URI)}. The path does not need to exist. The path provider determines the exact URI form used
    /// for conversion.
    ///
    /// @param path the path to convert
    /// @return the converted file URL
    /// @throws WebURLParseException when the path URI is not accepted as an absolute URL
    static WebURL of(Path path) {
        return of(path.toUri());
    }

    /// Parses an absolute URL string and returns it as a Java `URI`.
    ///
    /// This is a convenience method equivalent to `WebURL.parse(input).toURI()`. The input is processed as a
    /// WHATWG URL first, then converted to Java's RFC 2396-oriented `URI` syntax.
    ///
    /// @param input the URL input string
    /// @return a Java `URI` representing the parsed URL
    /// @throws WebURLParseException  when the input is not a valid absolute URL
    /// @throws IllegalStateException when the parsed URL has no RFC 2396 representation accepted by Java `URI`
    static URI toURI(String input) {
        return parse(input).toURI();
    }

    /// Parses an absolute URL string and returns it as a Java `URL`.
    ///
    /// This is a convenience method equivalent to `WebURL.parse(input).toURL()`. Java {@link URL} supports only
    /// schemes for which the runtime has a URL stream handler, so some valid WHATWG URLs cannot be represented
    /// as a Java `URL`.
    ///
    /// @param input the URL input string
    /// @return a Java `URL` representing the parsed URL
    /// @throws WebURLParseException  when the input is not a valid absolute URL
    /// @throws IllegalStateException when the parsed URL has no RFC 2396 representation accepted by Java `URI`
    /// @throws MalformedURLException when Java has no URL handler for the scheme or rejects the URL
    static URL toURL(String input) throws MalformedURLException {
        return parse(input).toURL();
    }

    /// Parses an absolute input string and returns the parsed URL.
    ///
    /// The input must be an absolute URL. Relative inputs fail; use a base-aware overload when relative URL
    /// references should be accepted.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input is not a valid absolute URL
    static WebURL parse(String input) {
        return WebURLParser.getDefault().parse(input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The base string must be a valid absolute URL. The input may be either absolute or relative to that base.
    ///
    /// @param input the URL input string
    /// @param base  the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails
    static WebURL parse(String input, String base) {
        return WebURLParser.getDefault().parse(input, base);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The input may be either absolute or relative to the supplied base URL.
    ///
    /// @param input the URL input string
    /// @param base  the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when the input cannot be resolved against the base URL
    static WebURL parse(String input, WebURL base) {
        return WebURLParser.getDefault().parse(input, base);
    }

    /// Resolves an input string against this URL and returns the parsed URL.
    ///
    /// This is a convenience method equivalent to `WebURL.parse(input, this)`. The input may be either an
    /// absolute URL or a relative URL reference. Relative references are resolved using this URL as the base.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input cannot be resolved against this URL
    default WebURL resolve(String input) {
        return WebURLParser.getDefault().parse(input, this);
    }

    /// Parses an absolute input string and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as `parse(String)`, except failures are represented by
    /// `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if the input is not a valid absolute URL
    static @Nullable WebURL tryParse(String input) {
        return WebURLParser.getDefault().tryParse(input);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as `parse(String, String)`, except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base  the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    static @Nullable WebURL tryParse(String input, String base) {
        return WebURLParser.getDefault().tryParse(input, base);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// This method has the same URL processing behavior as `parse(String, WebURL)`, except failures are
    /// represented by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @param base  the base URL
    /// @return the parsed URL, or `null` if the input cannot be parsed against the base
    static @Nullable WebURL tryParse(String input, WebURL base) {
        return WebURLParser.getDefault().tryParse(input, base);
    }

    /// Parses a user-entered browser-style URL input and returns the parsed URL.
    ///
    /// This method is intended only for handling interactive text entered by a user, such as text copied from
    /// or typed into a browser address bar. It may apply browser-like heuristics before normal URL processing,
    /// and those heuristics may change in future releases as browser behavior and project policy evolve.
    ///
    /// Do not use this method for stored URL data, protocol messages, cache keys, database values, or any other
    /// serialization format that requires stable round-tripping. Serialize a `WebURL` with {@link #href()}, and
    /// parse serialized URL strings with {@link #parse(String)}.
    ///
    /// Examples:
    ///
    /// | Input | Result |
    /// | --- | --- |
    /// | `https://example.com/a?b#c` | `https://example.com/a?b#c` |
    /// | `example.com` | `https://example.com/` |
    /// | `example.com:8080` | `http://example.com:8080/` |
    /// | `localhost/path` | `http://localhost/path` |
    /// | `localhost:8080/path` | `http://localhost:8080/path` |
    /// | `127.0.0.1:3000` | `http://127.0.0.1:3000/` |
    /// | `C:\Users\Alice\file.txt` | `file:///C:/Users/Alice/file.txt` |
    /// | `/tmp/a b#c?d` | `file:///tmp/a%20b%23c%3Fd` |
    /// | `not a url` | throws `WebURLParseException` |
    ///
    /// @param input the browser-style URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when the input is not accepted as a browser-style URL input
    /// @implSpec The current implementation accepts standard absolute URL strings and a deterministic subset of
    /// browser address bar style URL inputs, such as bare domain names, `//`-prefixed authorities, single-label
    /// hosts, IP addresses, bracketed IPv6 addresses, and absolute local paths. Public-looking domain names
    /// recognized as URL like but lacking an explicit scheme are completed with `https` before normal URL
    /// processing. When such a domain has an explicit decimal port, it is completed with `http`. IP addresses,
    /// single-label hosts, and reserved local or test hosts such as `localhost`, `test`, and names below
    /// `.localhost` are completed with `http`. Windows drive paths, Windows UNC paths, and POSIX-style absolute
    /// paths are completed as `file` URLs. This method is not a complete browser omnibox or navigation
    /// algorithm: it performs no DNS lookup, no filesystem lookup, no HTTP(S) probing, no HSTS or policy checks,
    /// no history or search suggestion lookup, no search fallback, and no HTTPS-to-HTTP fallback after network
    /// failure. Inputs that are neither URL strings nor recognized browser-style URL inputs fail instead of
    /// being interpreted as search terms.
    static WebURL parseBrowserInput(String input) {
        return WebURLParser.getDefault().parseBrowserInput(input);
    }

    /// Parses a user-entered browser-style URL input and returns `null` on failure.
    ///
    /// This method is intended only for handling interactive text entered by a user. Like
    /// {@link #parseBrowserInput(String)}, it may apply browser-like heuristics that can change in future
    /// releases. Do not use this method for stable serialization or round-tripping; serialize a `WebURL` with
    /// {@link #href()}, and parse serialized URL strings with {@link #parse(String)}.
    ///
    /// @param input the browser-style URL input string
    /// @return the parsed URL, or `null` if the input is not accepted as a browser-style URL input
    /// @implSpec This method has the same URL processing behavior as {@link #parseBrowserInput(String)}, except
    /// failures are represented by `null` instead of an exception.
    static @Nullable WebURL tryParseBrowserInput(String input) {
        return WebURLParser.getDefault().tryParseBrowserInput(input);
    }

    /// Returns the complete serialized URL.
    ///
    /// The returned string is the WHATWG URL serialization of this URL. It includes the scheme and path,
    /// and includes authority, query, and fragment delimiters when those components are present. Default ports
    /// are omitted, host syntax is normalized, path dot segments have been resolved where the URL Standard
    /// requires it, and percent-encoding uses URL Standard encode sets.
    ///
    /// This string is canonical for this object but is not necessarily the same string that was supplied to the
    /// input. It is also not guaranteed to be accepted directly by Java {@link URI}; use {@link #toURI()} or
    /// {@link #toRFC2396String()} for that conversion.
    ///
    /// @return the serialized URL
    String href();

    /// Returns a human-readable display string for this URL.
    ///
    /// The returned string is intended for user interfaces that want a browser-like address presentation while
    /// retaining this URL's structure. It starts from the WHATWG URL serialization and decodes valid
    /// percent-encoded UTF-8 sequences in credentials, path, query, and fragment only when they represent
    /// non-ASCII printable Unicode characters. Percent escapes for ASCII bytes are preserved, so delimiters and
    /// visually ambiguous characters such as `%2F`, `%3F`, `%23`, `%25`, and `%20` remain escaped.
    ///
    /// Domain hosts that contain IDNA ASCII Compatible Encoding labels are displayed in Unicode when UTS #46
    /// ToUnicode succeeds and the Unicode form round-trips back to the same ASCII host. IPv4 hosts, IPv6 hosts,
    /// opaque hosts, and domain hosts that do not pass that check remain in their serialized form. Browser IDN
    /// display policies are security-sensitive and differ between browsers, locales, platforms, and versions;
    /// this method provides a conservative display form, not an exact reproduction of any browser's policy. The
    /// result also does not hide credentials, remove schemes, elide `www`, or apply any browser omnibox policy.
    ///
    /// This method is for display only. The result is not a canonical serialization, is not guaranteed to be
    /// accepted by {@link #parse(String)}, and must not be used as an authority for security decisions. Use
    /// {@link #href()} when stable URL serialization is required.
    ///
    /// @return a human-readable display string
    String toDisplayString();

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
    /// The scheme identifies the URL's processing and serialization rules, such as `http`, `https`, `file`,
    /// or a non-special scheme such as `data`. It is the canonical URL Standard scheme name: it starts with
    /// an ASCII letter, contains only ASCII letters, ASCII digits, plus (`+`), hyphen (`-`), and period
    /// (`.`), and is stored in lower case.
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
    /// empty username, the result is the empty string. The percentage of escapes that are invalid or incomplete is left
    /// unchanged.
    ///
    /// @return the decoded username component, or `null` when absent
    @Nullable String getUsername();

    /// Returns the raw username component.
    ///
    /// This method exposes the normalized, percent-encoded username stored in the URL. It does not
    /// percent-decode the value. The result is `null` when the URL has no serialized credentials. When the URL
    /// has credentials with an empty username, the result is the empty string.
    ///
    /// The word "raw" has the same meaning as in Java `URI` raw component getters: the returned string is the
    /// serialized component with percent-encoding preserved. It is not necessarily a substring of the original
    /// input because URL processing may normalize, percent-encode, or otherwise rewrite credentials.
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
    /// The result is `null` when the URL has no password component. The percentage of escapes that are invalid or
    /// incomplete is left unchanged.
    ///
    /// @return the decoded password component, or `null` when absent
    @Nullable String getPassword();

    /// Returns the raw password component.
    ///
    /// This method exposes the normalized, percent-encoded password stored in the URL. It does not
    /// percent-decode the value. The result is `null` when the URL has no password component.
    ///
    /// The word "raw" has the same meaning as in Java `URI` raw component getters: the returned string is the
    /// serialized component with percent-encoding preserved. It is not necessarily a substring of the original
    /// input because URL processing may normalize, percent-encode, or otherwise rewrite credentials.
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
    /// username and, if a password component is serialized, a colon (`:`) followed by the password. The percentage of
    /// escapes that are invalid or incomplete is left unchanged.
    ///
    /// The result is `null` when the URL has no serialized credentials.
    ///
    /// @return the decoded user-info component, or `null` when absent
    @Nullable String getUserInfo();

    /// Returns the raw user-info component.
    ///
    /// This method is the Java `URI`-style getter for the URL user-info with percent-encoding preserved. It
    /// returns the normalized user-info serialization without the trailing at-sign (`@`). The returned value is
    /// not necessarily a substring of the original input because URL processing may normalize, percent-encode, or
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
    /// The authority is present when this URL has a host component. When present, it consists of the
    /// serialized credentials followed by at-sign (`@`) when credentials are present, the serialized host, and
    /// the serialized port prefixed by colon (`:`) when a non-default port is present. Domain hosts remain in
    /// their URL Standard ASCII form; this method does not convert Punycode labels back to Unicode. The percentage of
    /// escapes that are invalid or incomplete is left unchanged.
    ///
    /// The result is `null` when this URL has no host component. A URL with an explicitly empty authority,
    /// such as a `file` URL with an empty host, returns the empty string.
    ///
    /// @return the decoded authority component, or `null` when absent
    @Nullable String getAuthority();

    /// Returns the raw authority component.
    ///
    /// This method is the Java `URI`-style getter for the URL authority with percent-encoding preserved. It
    /// returns the normalized authority serialization without the leading double solidus (`//`). The returned
    /// value is not necessarily a substring of the original input because URL processing may normalize,
    /// percent-encode, or otherwise rewrite credentials, hosts, and ports.
    ///
    /// The authority is present when this URL has a host component. When present, it consists of the raw
    /// username and optional raw password followed by at-sign (`@`) when credentials are present, the serialized
    /// host, and the serialized port prefixed by colon (`:`) when a non-default port is present.
    ///
    /// The result is `null` when this URL has no host component. A URL with an explicitly empty authority,
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
    /// The result is `null` when this URL has no host component, such as for most opaque URLs. A URL with
    /// an explicitly empty host returns the empty string.
    ///
    /// @return the host component, or `null` when absent
    @Nullable String getHost();

    /// Returns the port as an integer.
    ///
    /// This method returns the normalized port component when one is stored in this URL. If no port component
    /// is stored but the scheme has a known default port, this method returns that default port. Otherwise, it
    /// returns `-1`.
    ///
    /// URL Standard normalization removes default ports from the serialized URL. Therefore
    /// `WebURL.parse("https://example.com:443/").getRawPort()` returns `null`, but this method returns `443`.
    ///
    /// @return the stored port, the known default port, or `-1` when neither is available
    int getPort();

    /// Returns the raw port component.
    ///
    /// This method exposes the normalized serialized port component without the leading colon. It returns
    /// `null` when the URL has no stored port component. Default ports are removed during URL normalization, so
    /// this method does not preserve an explicitly written default port from the original input.
    ///
    /// Unlike other `Raw` component getters, the port component has no percent-encoded form and is always a
    /// decimal ASCII string when present. The name is used to distinguish the serialized component from
    /// {@link #getPort()}, which may return a scheme default port even when no port component is serialized.
    ///
    /// @return the normalized raw port component, or `null` when absent
    /// @since 0.2.0
    @Nullable String getRawPort();

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
    /// The result is `null` when the query component is absent. An empty query component is returned as an
    /// empty string.
    ///
    /// @return the decoded query component, or `null` when absent
    @Nullable String getQuery();

    /// Returns the raw query component.
    ///
    /// This method exposes the normalized, percent-encoded query stored in the URL without the leading
    /// question mark. It follows the Java `URI` convention of returning `null` when the query component is
    /// absent. An empty query component is returned as an empty string.
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
    /// This method exposes the normalized, percent-encoded fragment stored in the URL without the leading
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
    /// The returned string is suitable for Java {@link URI} construction when this URL has an RFC 2396
    /// representation. Existing valid percent escapes are preserved, and bare percent signs or characters
    /// accepted by WHATWG URL serialization but rejected by Java URI syntax are percent-encoded component by
    /// component.
    ///
    /// Some WHATWG URLs, such as non-special URLs with an empty opaque path and no query, have no corresponding
    /// absolute RFC 2396 URI because Java `URI` requires a non-empty scheme-specific part.
    ///
    /// @return the RFC 2396 URI string
    String toRFC2396String();

    /// Returns the serialized URL as a Java `URI`.
    ///
    /// The URI is constructed from {@link #toRFC2396String()}. This method does not reparse the original input;
    /// it converts the normalized WHATWG URL serialization into Java's RFC 2396-oriented `URI` syntax and then
    /// constructs a `URI` from that string.
    ///
    /// @return a Java `URI` representing this URL
    /// @throws IllegalStateException when this URL has no RFC 2396 representation accepted by Java `URI`
    URI toURI();

    /// Returns the serialized URL as a Java `URL`.
    ///
    /// The result is obtained from `toURI().toURL()`. Java {@link URL} supports only schemes for which the
    /// runtime has a URL stream handler, so some valid WHATWG URLs cannot be represented as a Java `URL`.
    /// Creating the `URL` object does not make this class perform network I/O.
    ///
    /// @return a Java `URL` representing this URL
    /// @throws MalformedURLException when Java has no URL handler for the scheme or rejects the URL
    URL toURL() throws MalformedURLException;

    /// Compares this URL with another URL by serialized URL string.
    ///
    /// The comparison is equivalent to `this.href().compareTo(other.href())`. It is consistent with
    /// `equals(Object)`: if this method returns zero, the two URL objects are equal, according to this
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
