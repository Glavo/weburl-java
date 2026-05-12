# Changelog

## 0.2.0 (In development)

### Breaking Changes

- `WebURL.getPort()` now returns the default port for known schemes (`http`, `https`, `ws`, `wss`, `ftp`)
  when no port is explicitly written. Use the new `WebURL.getRawPort()` to get the raw port component as
  written, or `null` if absent.
- Removed nested subclasses of `WebURLParseException` (e.g. `PortInvalid`, `HostMissing`). Use
  `WebURLParseException.getErrorType()` with the new `ErrorType` enum constants instead.
- Renamed `WebURLParseException.errorName()` to `getErrorName()`.
- `WebURL.toURI()` now throws `URISyntaxException` when the URL has no Java `URI` representation.
  The static `WebURL.toURI(String)` helper reports the same conversion failure as `IllegalArgumentException`,
  not `IllegalStateException`, while `WebURL.toURL()` continues to throw `MalformedURLException` for URI
  conversion and URL handler failures.

### New APIs

- `WebURL.resolve(String)` — resolves an absolute or relative URL string against this URL.
- `WebURL.newBuilder()` and `WebURL.newBuilder(WebURL)` — create mutable `WebURL.Builder` instances for
  constructing URLs from components or copying and modifying an existing URL.
- `WebURL.getRawPort()` — returns the port component as written, or `null` if absent.
- `WebURL.parseBrowserInputToURI(String)` and `WebURL.parseBrowserInputToURL(String)` — parse browser-style
  user input and convert the result directly to Java networking types.
- `WebURLParser` — a reusable parser interface with two built-in instances:
  - `WebURLParser.getDefault()` — lenient parser that accepts recoverable validation errors (previous behavior).
  - `WebURLParser.getStrict()` — strict parser that rejects recoverable validation errors.
- `WebURLParseException.ErrorType` — enum covering every URL Standard validation error type, with a standard
  error name, a human-readable reason, and a flag indicating whether the error is recoverable.
- `WebURLParseException` now records the input string, a reason, and the character index of the error,
  similar to `java.net.URISyntaxException`.

### Bug Fixes

- Fixed fast-path parsing of URLs with empty password markers such as `https://user:@example.com/`, ensuring
  they normalize consistently with the full WHATWG parser and remain stable when copied through `WebURL.Builder`.

### Improvements

- Reduced memory allocations in URL host parsing, IDNA processing, and percent-encoding paths.
- Expanded Javadoc for `WebURL`, `WebURLParser`, `WebURLParseException`, and `ErrorType`.
- Expanded README with a Quick Start guide and additional usage examples.

---

## 0.1.0 (2026-05-10)

Initial release.
