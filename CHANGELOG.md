# Changelog

## 0.2.0 (In development)

### Breaking Changes

- **`WebURL.getPort()` now returns the effective port.**
  For schemes with a known default port (`http`, `https`, `ws`, `wss`, `ftp`), `getPort()` now returns the
  default port even when no port is explicitly written in the URL. For example, both `https://example.com/`
  and `https://example.com:443/` now return `443`. Use the new `WebURL.getRawPort()` to retrieve only an
  explicitly written port component.

- **`WebURLParseException` is now a single final class.**
  The nested subclasses (`WebURLParseException.PortInvalid`, `WebURLParseException.HostMissing`, etc.) have
  been removed. Use `WebURLParseException.getErrorType()` and compare against `WebURLParseException.ErrorType`
  enum constants for stable programmatic error classification.

- **`WebURLParseException.errorName()` renamed to `getErrorName()`.**
  The exception message no longer embeds the URL Standard error name as a prefix; use `getErrorName()` or
  `getErrorType()` for stable programmatic access to the error identity.

### New APIs

- **`WebURL.resolve(String)`** — Resolves an absolute or relative URL input string against this `WebURL` and
  returns the resulting `WebURL`. Equivalent to calling `WebURLParser.getDefault().parse(input, this)`.

- **`WebURL.getRawPort()`** — Returns the serialized port component exactly as it appears in the URL string,
  or `null` when the port is absent or has been elided because it equals the scheme default.

- **`WebURLParser` interface** — A reusable, thread-safe parser object that encapsulates a URL validation
  policy. Two built-in instances are provided:
  - `WebURLParser.getDefault()` — accepts recoverable validation errors (matches 0.1.0 behavior).
  - `WebURLParser.getStrict()` — rejects all recoverable validation errors, throwing `WebURLParseException`
    for inputs the default parser would silently normalize.

  Both instances expose the full `parse` / `tryParse` / `parseBrowserInput` / `tryParseBrowserInput`
  family of methods, and `getRejectedValidationErrors()` returns the set of recoverable errors the parser
  treats as hard failures.

- **`WebURLParseException.ErrorType` enum** — A comprehensive, stable catalog of every URL Standard
  validation error type. Each constant exposes:
  - `getErrorName()` — the URL Standard error name string (e.g., `"host-missing"`).
  - `getReason()` — a human-readable default reason sentence.
  - `isRecoverable()` — whether the URL Standard allows parsing to continue past this error
    (i.e., the error can be configured per-parser).

- **`WebURLParseException` enriched fields** — Parse exceptions now record the input string
  (`getInput()`), a human-readable reason (`getReason()`), and a UTF-16 character index
  (`getIndex()`, or `-1` when the position is unknown), matching the shape of
  `java.net.URISyntaxException`.

### Performance Improvements

- Refactored the internal URL host representation into sealed subtypes (`IPv4`, `IPv6`, domain, opaque),
  eliminating redundant string allocations during host parsing and serialization.
- IDNA / UTS #46 label processing no longer allocates intermediate code-point arrays.
- Frequently used domain host values are cached on first use, avoiding repeated serialization.
- URL host serialization now writes directly into an existing `StringBuilder`, reducing object churn
  throughout the parse path.
- Consolidated internal ASCII utility helpers to reduce class-loading overhead.

### Documentation

- Expanded README with a Quick Start guide covering parsing, component access, normalization, error handling,
  `java.net.URI`/`URL` interoperability, browser-style input parsing, and display strings.
- Expanded Javadoc for `WebURL`, `WebURLParser`, `WebURLParseException`, and all
  `WebURLParseException.ErrorType` constants with per-error examples drawn from the URL Standard.

## 0.1.0 (2026-05-10)

Initial release.
