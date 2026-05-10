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

## Quick Start

TODO: Add a quick start guide.

## License

This project is licensed under the Apache License, Version 2.0.
