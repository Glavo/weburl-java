# WebURL for Java

[WebURL for Java](https://github.com/Glavo/weburl-java) is a WHATWG URL library for Java, providing URL support that
conforms to [the WHATWG URL Standard](https://url.spec.whatwg.org/).

Because [`java.net.URI`](https://docs.oracle.com/en/java/javase/25/docs//api/java.base/java/net/URI.html) is based on
the outdated [RFC 2396 specification](http://www.ietf.org/rfc/rfc2396.txt), it cannot parse many real-world URLs.

WebURL for Java is based on the WHATWG URL Standard, which is the most widely adopted URL specification today,
and it handles URLs in the same way browsers do.

Despite the name WebURL, this library is not limited to `http`/`https`. It also handles `file`, `ftp`, `data`,
`mailto`, `tel`, `urn`, and any other URL scheme.
The word "Web" in the name refers to WHATWG (The Web Hypertext Application Technology Working Group).

## Features

- Full conformance with the WHATWG URL Standard, passing
  the [web-platform-tests](https://github.com/web-platform-tests/wpt/tree/master/url) URL test suite.
- No dependencies beyond the `java.base` module.
- API closely mirrors Java `URI`, making it easy to learn and migrate to.
- Good interoperability with Java `URI` and `URL`, with straightforward conversion utilities.
- Full IDN support conforming to UTS #46, implemented from Unicode-provided mapping tables without requiring ICU4J.
- Default parsing strictly follows the specification, with additional convenience methods for parsing user input that
  simulate heuristic browser address-bar behavior, handling scheme-less URIs and local file paths intelligently.

## Add to Your Project

TODO: Publish to Maven Central.

## Comparison with Java `URI`/`URL`

Java provides two built-in types for working with URLs: `java.net.URI` and `java.net.URL`.
Both have significant limitations that `WebURL` is designed to address.

### `java.net.URI`

| Feature | `java.net.URI` | `WebURL` |
|---|---|---|
| Specification | RFC 2396 (obsolete) | WHATWG URL Standard |
| Absolute only | No – can represent relative references | Yes – every `WebURL` is absolute |
| Input normalization | Minimal; preserves original spelling | Full: scheme/host lowercased, IDNA applied, default ports removed, dot segments resolved, percent-encoding normalized |
| IDN / internationalized domains | Not supported | Full UTS #46 support, no ICU4J required |
| IPv4/IPv6 normalization | Not performed | IPv4 → dotted decimal; IPv6 → compressed bracketed form |
| Real-world URL compatibility | Rejects many valid real-world URLs | Accepts URLs exactly as browsers do |
| Browser-style input | Not supported | `parseBrowserInput()` handles bare domains, local paths, etc. |
| Equality | Structural, scheme-sensitive comparison | Defined solely by the WHATWG serialization (`href()`) |
| Interoperability | N/A | `WebURL.of(URI)`, `webURL.toURI()`, `webURL.toRFC2396String()` |

`java.net.URI` is based on RFC 2396, which has been superseded by RFC 3986 and has never matched
browser URL-parsing behavior. It rejects many URLs that are perfectly valid in the real world — for
example, URLs with non-ASCII characters in the host, IPv6 addresses with zone identifiers, or paths
that require IDNA processing. `WebURL` parses and normalizes all of these correctly.

`URI` is also a generic syntax object that can represent relative references such as `../foo`.
`WebURL` is always absolute: relative inputs must be resolved against a base URL before a `WebURL` is
returned, making it impossible to accidentally hold a relative reference where an absolute URL is expected.

**Example:**

```java
// URI rejects this valid internationalized URL
URI.create("https://münchen.de/path");   // throws IllegalArgumentException

// WebURL handles it, applying IDNA normalization
WebURL url = WebURL.parse("https://münchen.de/path");
System.out.println(url.href());            // https://xn--mnchen-3ya.de/path
System.out.println(url.toDisplayString()); // https://münchen.de/path
```

### `java.net.URL`

| Feature | `java.net.URL` | `WebURL` |
|---|---|---|
| Specification | Scheme-handler–specific; no single standard | WHATWG URL Standard |
| Network I/O | `equals()` and `hashCode()` may perform DNS resolution | Never performs network I/O |
| Equality | Host-name–resolution–based (DNS-dependent, slow, unreliable offline) | Always defined by serialized URL string only |
| Protocol handlers | Required; limited to registered schemes | No protocol handlers; works with any scheme |
| Thread safety | Not guaranteed | Immutable; fully thread-safe |
| Serialization | Supported, but handler-dependent | Implements `Serializable`; handler-independent |
| Interoperability | N/A | `WebURL.of(URL)`, `webURL.toURL()` |

`java.net.URL` is notorious for defining `equals()` and `hashCode()` in terms of IP-address resolution.
Two `URL` objects with different hostnames that resolve to the same IP address are considered equal,
making `URL` unsafe to use in collections, caches, or security checks without wrapping it first.
`WebURL` defines equality entirely by the serialized URL string, with no network access ever performed.

`URL` also requires a registered URL stream handler for every scheme. Schemes without a handler —
such as `data`, `mailto`, `tel`, or custom schemes — throw `MalformedURLException` when you try to
create a `URL` object. `WebURL` works with any syntactically valid scheme.

**Example:**

```java
// URL.equals() may block while performing DNS resolution, and
// two hosts that resolve to the same IP are considered "equal"
URL a = new URL("http://example.com/");
URL b = new URL("http://93.184.216.34/");
a.equals(b); // may return true after a DNS lookup — almost certainly not what you want

// WebURL.equals() is always fast and purely string-based
WebURL wa = WebURL.parse("http://example.com/");
WebURL wb = WebURL.parse("http://93.184.216.34/");
wa.equals(wb); // always false — different serializations
```

### Summary

Use `WebURL` when:

- You need to parse URLs the same way browsers do.
- You work with internationalized domain names.
- You need fast, DNS-free, collection-safe URL equality and hashing.
- You handle non-HTTP schemes such as `data`, `mailto`, or custom schemes.
- You want a strict, always-absolute URL value rather than a generic URI syntax object.

`WebURL` provides `WebURL.of(URI)`, `WebURL.of(URL)`, `webURL.toURI()`, and `webURL.toURL()` for
straightforward interoperability with existing Java APIs that require `URI` or `URL` objects.

## Quick Start

TODO: Add a quick start guide.

## License

This project is licensed under the Apache License, Version 2.0.
