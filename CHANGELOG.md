# Changelog

# 0.2.0 (In development)

- Changed `WebURL.getPort()` to return the known default port for schemes such as `http`, `https`, `ws`,
  `wss`, and `ftp` when no non-default port is serialized.
- Added `WebURL.getRawPort()` to expose the normalized serialized port component, or `null` when absent.

# 0.1.0 (2026-05-10)

Initial release.
