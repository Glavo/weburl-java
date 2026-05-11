# WebURL for Java

[![](https://img.shields.io/maven-central/v/org.glavo/weburl?label=Maven%20Central)](https://search.maven.org/artifact/org.glavo/weburl)
[![codecov](https://codecov.io/gh/Glavo/weburl-java/graph/badge.svg?token=YH1WL9LMVO)](https://codecov.io/gh/Glavo/weburl-java)

**WebURL for Java** is a modern URL library for Java that implements the
[WHATWG URL Standard](https://url.spec.whatwg.org/) — the same specification followed by every
major browser. This means WebURL parses, normalizes, and serializes URLs exactly the way
Chrome, Firefox, and Safari do, giving Java applications the same URL behavior as the web platform.

> **"Web"** in the name refers to WHATWG (The Web Hypertext Application Technology Working Group).
> Despite the name, WebURL is not limited to `http`/`https` — it handles any URL scheme,
> including `file`, `ws`, `wss`, `ftp`, `data`, `blob`, `mailto`, `tel`, `urn` and more.

## Why WebURL?

The Java standard library ships two classes for representing URLs: 
[`java.net.URI`](https://docs.oracle.com/en/java/javase/25/docs//api/java.base/java/net/URI.html) 
and [`java.net.URL`](https://docs.oracle.com/en/java/javase/25/docs//api/java.base/java/net/URL.html).
Both have well-known, serious shortcomings.

### 1. Both follow an outdated standard

`URI` and `URL` are both specified against [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt), which was superseded by
[RFC 3986](https://www.ietf.org/rfc/rfc3986.txt) in 2005 and is a far cry from how URLs are handled on the modern
web. Many URLs that browsers accept every day are rejected or mishandled by the standard Java classes.

For example, `URI.create("https://example.com/a b")` throws an `IllegalArgumentException` because 
RFC 2396 does not allow the unencoded space,
even though every major browser accepts this URL without complaint.

### 2. `URI` and `URL` have subtle, incompatible semantics

Despite nominally following the same specification, the two classes behave differently in edge cases, making
round-trip conversion unreliable:

- `new URL("https://example.com/a b")` succeeds (URL is lenient about spaces), but calling `.toURI()` on it
  throws a `URISyntaxException` because URI refuses the unencoded space.
- Conversely, some URLs that `URI` can parse will throw an exception when `URI.toURL()` is called on them — for example,
  URLs with an invalid port such as `https://example.com:not-a-number`, or URLs whose scheme has no registered `URLStreamHandler`.

### 3. No internationalized domain name (IDN) support

Neither class handles internationalized domain names (IDN) correctly. Neither supports IDNA processing.

While both can parse IDN URLs, `URI` fails to recognize the host at all:

```java
URI uri = new URI("https://münchen.de");
uri.getHost(); // null
```

`URL` does recognise the host, but returns the raw Unicode string instead of the ACE form:

```java
URL url = new URL("https://münchen.de");
url.getHost(); // "münchen.de"
```

Although the Java standard library provides `java.net.IDN` for manually converting IDN to the ACE form,
it is based on the outdated IDNA 2003 specification and does not support the newer IDNA 2008 or the
widely used UTS #46 specification.

### 4. Only fully structured URLs are supported

Both `URL` and `URI` require a complete URL structure — the scheme field in particular is mandatory.

This behavior is appropriate when parsing URLs produced by programs, but falls short when handling user input.

Users entering URLs typically expect behavior similar to a browser address bar. For example, entering `example.com`
should resolve to `https://example.com` rather than throw an error, and entering `localhost:8080` should be treated
as `http://localhost:8080` rather than a URL whose scheme is `localhost`.

The absence of a lenient, browser-like parsing mode means that both classes frequently behave in unexpected ways
when used to process user-supplied input.

### 5. `java.net.URL` has dangerous network-aware equality

`URL.equals()` and `URL.hashCode()` may perform DNS resolution to decide whether two URLs are equal (for example,
`http://example.com/` and `http://93.184.216.34/` could be considered equal). This makes `URL` objects unsafe to
use as keys in hash maps or sets, and can cause unexpected latency or failures in network-restricted environments.

### WebURL for Java solves this problem

WebURL for Java is designed to address all of the shortcomings described above.

**Modern standard.** WebURL follows the [WHATWG URL Standard](https://url.spec.whatwg.org/), the same
specification implemented by every major browser. It passes the full
[web-platform-tests](https://github.com/web-platform-tests/wpt/tree/master/url) URL test suite, so
its behavior is tested against thousands of real-world URL inputs that a browser would accept.

**Single, consistent API.** WebURL exposes one core class — `WebURL` — with unambiguous, well-defined
semantics. There is no `URI`/`URL` split to navigate, no surprising round-trip failures, and no
checked exceptions for URLs that are perfectly valid in practice.

**Full IDN support.** WebURL implements UTS #46 (Unicode IDNA Compatibility Processing) directly
from the Unicode-provided mapping tables, without requiring ICU4J or any external dependency.
Internationalized domain names like `münchen.de` are automatically normalized to their ACE form
`xn--mnchen-3ya.de` during parsing.

**Lenient, browser-like input handling.** In addition to the strict standard parser, WebURL
provides `parseBrowserInput()`, which applies the same heuristics a browser address bar uses —
auto-detecting the scheme, handling scheme-free hostnames such as `example.com`, and converting
local file paths into `file://` URLs — making it straightforward to process user-supplied input.

**Safe equality.** `WebURL.equals()` and `WebURL.hashCode()` are purely structural — they compare
the serialized URL string and never touch the network. `WebURL` objects are safe to use as keys in
`HashMap` or `HashSet`.

## Features

- Full conformance with the WHATWG URL Standard, passing
  the [web-platform-tests](https://github.com/web-platform-tests/wpt/tree/master/url) URL test suite.
- No dependencies beyond the `java.base` module.
- API closely mirrors Java `URI`, making it easy to learn and migrate to.
- Good interoperability with Java `URI` and `URL`, with straightforward conversion utilities.
- Full IDN support conforming to UTS #46, implemented from Unicode-provided mapping tables without requiring ICU4J.
- Default parsing strictly follows the specification, with additional convenience methods for parsing user input that
  simulate heuristic browser address-bar behavior, handling scheme-fewer URIs and local file paths intelligently.

## Add to Your Project

Gradle:

```kotlin
dependencies {
    implementation("org.glavo:weburl:0.1.0")
}
```

Maven:

```xml

<dependency>
    <groupId>org.glavo</groupId>
    <artifactId>weburl</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

### Parsing URLs

Parse absolute URLs or resolve relative URLs against a base:

```java
// Absolute URL
WebURL url = WebURL.parse("https://example.com/path");

// Relative URL resolved against a string base
WebURL docs = WebURL.parse("/docs", "https://example.com/");
// -> "https://example.com/docs"

// Relative URL resolved against a WebURL base
WebURL page = WebURL.parse("api/v2", docs);
// -> "https://example.com/api/v2"

// Convenience instance method for resolving against an existing WebURL
WebURL next = docs.resolve("guide/");
// -> "https://example.com/guide/"
```

### URL Components

WebURL provides an API similar to `java.net.URI` for reading URL components: 

```java
WebURL url = WebURL.parse("https://user:pass@example.com:8080/a%2Fb?x=a%26b#frag%23ment");

url.getScheme();        // "https"
url.getUsername();      // "user"
url.getPassword();      // "pass"
url.getHost();          // "example.com"
url.getPort();          // 8080
url.getRawPort();       // "8080"
url.getRawPath();       // "/a%2Fb"
url.getRawQuery();      // "x=a%26b"
url.getRawFragment();   // "frag%23ment"
```

For schemes with known defaults, `getPort()` returns the default port when no non-default port is serialized.
Use `getRawPort()` to inspect the normalized serialized port component.

### URL Normalization

`WebURL` normalizes URLs according to the WHATWG specification during parsing, 
so all `WebURL` instances are always normalized.

You can obtain the normalized URL string via `href()` or `toString()`.

```java
WebURL.parse("HTTP://EXAMPLE.COM:443/a/./b/../c").href();
// -> "http://example.com/a/c"

WebURL.parse("https://bücher.example/").href();
// -> "https://xn--bcher-kva.example/"

WebURL.parse("https://bücher.example/").getHost();
// -> "xn--bcher-kva.example"
```

### Error Handling

Use `parse()` when a valid URL is expected — it throws `WebURLParseException` on failure.
Use `tryParse()` when the input may be invalid — it returns `null` instead of throwing:

```java
// Throws WebURLParseException for invalid input
WebURL url = WebURL.parse("://invalid");

// Returns null for invalid input
WebURL maybeUrl = WebURL.tryParse("://invalid");
if (maybeUrl != null) {
    // use the URL
}
```

### Converting to and from `java.net.URI` / `java.net.URL`

`WebURL` can be easily converted to and from the `java.net.URI`/`java.net.URL`:

```java
// From java.net.URI or java.net.URL
WebURL url1 = WebURL.of(URI.create("https://example.com/a%20b"));
WebURL url2 = WebURL.of(new URL("https://example.com/a%20b"));

// From a file path
WebURL fileUrl = WebURL.of(Path.of("/tmp/data.txt").toAbsolutePath());

// Back to java.net.URI / java.net.URL
URI uri = url.toURI();
URL javaUrl = url.toURL();

// One-liner: parse and convert to URI
URI uri = WebURL.toURI("https://example.com/a b?q=1#f");
```

When converting a `WebURL` to `java.net.URI`/`java.net.URL`, the URL is normalized to the RFC 2396 standard,
so using `WebURL.toURI(String)` instead of `java.net.URI.create(String)` allows parsing a wider range of real-world URLs.

### Parsing User Input

`parse()` requires well-formed URLs. For user-facing input (e.g., an address bar),
use `parseBrowserInput()`, which applies heuristics to handle
scheme-fewer domains and local file paths:

```java
// Domain without scheme — https/auto-detected
WebURL.parseBrowserInput("example.com").href();     // "https://example.com/"
WebURL.parseBrowserInput("127.0.0.1:8080").href();  // "http://127.0.0.1:8080/"

// Local file paths — file:// URLs
WebURL.parseBrowserInput("/tmp/a b#c").href();
// -> "file:///tmp/a%20b%23c"
```

We recommend using this method to parse and normalize user input, but when serializing you should save the
normalized result rather than the user's original input.

Because this method is not part of the WHATWG URL Standard, we may adjust its behavior at any time to better
match user expectations, so you should not assume that its parsing rules are stable across different versions of
WebURL for Java.

This method makes its best effort to match browser address-bar behavior. However, different browsers — or even
different versions of the same browser — may behave differently, so this goal is inherently fuzzy, and we can
only approximate it rather than achieve full consistency.

In addition, the heuristic algorithms used by browser address bars may take into account complex factors such as
browsing history, and can support multi-step operations such as first trying `https` and then falling back to
`http`. `parseBrowserInput`, by design, is a simple, side-effect-free conversion from `String` to `WebURL` that
performs no actual network access and records no history, so some behaviors achievable in a browser address bar
may not be reachable with this method.

### Display

Use `toDisplayString()` for a human-readable representation with
Unicode IDN domains and decoded non-ASCII characters:

```java
WebURL.parse("https://xn--bcher-kva.example/%F0%9F%98%80").toDisplayString();
// -> "https://bücher.example/😀"
```

## License

This project is licensed under the Apache License, Version 2.0.
