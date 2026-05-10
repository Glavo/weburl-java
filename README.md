# WebURL for Java

[![codecov](https://codecov.io/gh/Glavo/weburl-java/graph/badge.svg?token=YH1WL9LMVO)](https://codecov.io/gh/Glavo/weburl-java)

**WebURL for Java** is a URL library for Java, providing URL support that
conforms to [the WHATWG URL Standard](https://url.spec.whatwg.org/).

Despite the name WebURL, this library is not limited to `http`/`https`. It also handles `file`, `ftp`, `data`,
`mailto`, `tel`, `urn`, and any other URL scheme.
The word "Web" in the name refers to WHATWG (The Web Hypertext Application Technology Working Group).

## Why WebURL?

The Java standard library ships two classes for representing URLs: 
[`java.net.URI`](https://docs.oracle.com/en/java/javase/25/docs//api/java.base/java/net/URI.html) 
and [`java.net.URL`](https://docs.oracle.com/en/java/javase/25/docs//api/java.base/java/net/URL.html).
Both have well-known, serious shortcomings.

### 1. Both follow an outdated standard

`URI` and `URL` are both specified against [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt), which was superseded by
[RFC 3986](https://www.ietf.org/rfc/rfc3986.txt) in 2005 and is a far cry from how URLs are handled on the modern
web. Many URLs that browsers accept every day are rejected or mishandled by the standard Java classes.

For example, `URI.create("https://example.com/a b")` throws an `IllegalArgumentException` because the unencoded
space is not allowed by RFC 2396, even though every major browser accepts this URL without complaint.

### 2. `URI` and `URL` have subtle, incompatible semantics

Despite nominally following the same specification, the two classes behave differently in edge cases, making
round-trip conversion unreliable:

- `new URL("https://example.com/a b")` succeeds (URL is lenient about spaces), but calling `.toURI()` on it
  throws a `URISyntaxException` because URI refuses the unencoded space.
- Conversely, some URIs that `URI` can parse successfully will cause `URI.toURL()` to throw, depending on the
  scheme or the presence of a registered `URLStreamHandler`.

### 3. `java.net.URL` has dangerous network-aware equality

`URL.equals()` and `URL.hashCode()` may perform DNS resolution to decide whether two URLs are equal (for example,
`http://example.com/` and `http://93.184.216.34/` could be considered equal). This makes `URL` objects unsafe to
use as keys in hash maps or sets, and can cause unexpected latency or failures in network-restricted environments.

### 4. No internationalized domain name (IDN) support

Neither class handles internationalized domain names (IDN) correctly. Neither supports IDNA processing.

While both can parse IDN URLs, `URI` fails to recognize the host at all:

```java
URI uri = new URI("https://münchen.de");
System.out.

println(uri.getHost()); // --> null
```

`URL` does recognise the host, but returns the raw Unicode string instead of the ACE form:

```java
URL url = new URL("https://münchen.de");
System.out.

println(url.getHost()); // --> "münchen.de"
```

Although the Java standard library provides `java.net.IDN` for manually converting IDN to the ACE form,
it is based on the outdated IDNA 2003 specification and does not support the newer IDNA 2008 or the
widely used UTS #46 specification.

### The WHATWG URL Standard solves all of this

The [WHATWG URL Standard](https://url.spec.whatwg.org/) was designed specifically to capture real-world URL
parsing behavior as implemented by browsers. It defines a single, unambiguous parsing algorithm that handles
percent-encoding normalization, IDN via UTS #46, IPv4/IPv6 address normalization, default-port elision, and
dot-segment resolution.

WebURL for Java implements this standard faithfully, passing the full
[web-platform-tests](https://github.com/web-platform-tests/wpt/tree/master/url) URL test suite.

In addition, WebURL for Java supports parsing WHATWG URLs into `java.net.URI` or `java.net.URL`.
Even if you still need to use those two classes to represent URLs, WebURL for Java can serve as a drop-in replacement
for `new URI(String)` to improve compatibility.

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

TODO: Add a quick start guide.

## License

This project is licensed under the Apache License, Version 2.0.
