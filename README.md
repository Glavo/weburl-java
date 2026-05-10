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

### Normalization

`URI` preserves many input details. `WebURL` applies WHATWG URL normalization while parsing:

```java
URI uri = URI.create("HTTP://EXAMPLE.COM:80/a/../b?x=1#top");
System.out.println(uri.toString()); // --> HTTP://EXAMPLE.COM:80/a/../b?x=1#top

WebURL url = WebURL.parse("HTTP://EXAMPLE.COM:80/a/../b?x=1#top");
System.out.println(url.href()); // --> http://example.com/b?x=1#top
```

### Internationalized Domains

`URI` does not apply UTS #46 or URL Standard domain-to-ASCII processing. For non-ASCII domain names, it may create a
URI object but fail to expose a Java host component:

```java
URI uri = URI.create("https://b\u00fccher.example/path");
System.out.println(uri.getHost()); // --> null

WebURL url = WebURL.parse("https://b\u00fccher.example/path");
System.out.println(url.getHost()); // --> xn--bcher-kva.example
System.out.println(url.toDisplayString()); // --> https://bücher.example/path
```

### Relative References

`URI` can hold a relative reference. `WebURL` is always absolute, so relative input must be resolved against an
absolute base URL:

```java
URI relative = URI.create("../guide");
System.out.println(relative.isAbsolute()); // --> false
System.out.println(WebURL.tryParse("../guide")); // --> null

WebURL resolved = WebURL.parse("../guide", "https://example.com/docs/api/");
System.out.println(resolved.href()); // --> https://example.com/docs/guide
```

### Java Interop

Use `WebURL` as the normalized URL value, then convert when another API specifically requires `URI` or `URL`:

```java
WebURL url = WebURL.parse("https://example.com/a b?q=1#f");

URI uri = url.toURI();
System.out.println(uri.toASCIIString()); // --> https://example.com/a%20b?q=1#f

URL javaUrl = url.toURL();
System.out.println(javaUrl.toExternalForm()); // --> https://example.com/a%20b?q=1#f
```

`toURL()` can only succeed when the Java runtime has a URL stream handler for the scheme. `WebURL` itself does not
need such handlers:

```java
WebURL data = WebURL.parse("data:text/plain,hello");
System.out.println(data.href()); // --> data:text/plain,hello
data.toURL(); // --> throws MalformedURLException on a standard JDK without a data: URL handler
```

Avoid using `java.net.URL` equality for URL identity. `URL.equals()` and `URL.hashCode()` may perform DNS lookups;
`WebURL.equals()` is DNS-free and compares only normalized serializations.

## Quick Start

TODO: Add a quick start guide.

## License

This project is licensed under the Apache License, Version 2.0.
