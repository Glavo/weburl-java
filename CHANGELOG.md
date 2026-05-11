# Changelog

# 0.2.0 (In development)

- Added `WebURL.resolve(String)` for resolving absolute or relative URL inputs against an existing `WebURL`.
- Changed `WebURLParseException` to a single final exception type with `ErrorType` enum constants, retained input,
  and error indexes.
- Changed `WebURL.getPort()` to return the known default port for schemes such as `http`, `https`, `ws`,
  `wss`, and `ftp` when no non-default port is serialized.
- Added `WebURL.getRawPort()` to expose the normalized serialized port component, or `null` when absent.

# 0.1.0 (2026-05-10)

Initial release.
