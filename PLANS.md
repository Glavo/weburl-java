# WHATWG URL Java Port Plan

## Summary

This plan ports `external/whatwg-url` 16.0.1 to Java 17 as the core implementation for this project.

The target public API is a complete URL API centered on the sealed `org.glavo.url.WebURL` interface, plus `WebURLSearchParams`. The internal implementation should preserve the WHATWG URL algorithms closely enough to be validated by fixed-version Web Platform Tests.

Reference baseline:

- `external/whatwg-url` commit: `569898d259128238ae4e6037ada4652d6d10f935`
- npm package version: `16.0.1`
- WHATWG URL Standard commit noted by upstream: `b6b3251fe911ab33d68fb051efe0e4d39ae4145e`

## Public API

Implement the sealed `org.glavo.url.WebURL` interface with the observable behavior of the WHATWG `URL` interface:

- Static `of`, `parse`, and `canParse` methods.
- A non-exported implementation class under `org.glavo.url.internal`.
- Accessors and mutators for `href`, `protocol`, `username`, `password`, `host`, `hostname`, `port`, `pathname`, `search`, and `hash`.
- Read-only `origin` and live `searchParams`.
- `toString` and `toJSON` returning the serialized URL.

Implement `org.glavo.url.WebURLSearchParams` with the observable behavior of the WHATWG `URLSearchParams` interface:

- Constructors from query strings and iterable/name-value data where practical for Java.
- `size`, `append`, `delete`, `get`, `getAll`, `has`, `set`, `sort`, iteration, and `toString`.
- Live synchronization with the owning `WebURL` query when created from a URL.

Keep low-level parsing and serialization APIs internal for now. They may be exposed later only after the public shape is stable.

## Implementation Plan

Build the port in layers:

1. Port the low-level helpers from `infra.js`, `encoding.js`, `percent-encoding.js`, and `urlencoded.js`.
   - ASCII classification.
   - UTF-8 encode/decode.
   - Percent decode and UTF-8 percent encode sets.
   - `application/x-www-form-urlencoded` parse and serialize.

2. Port the URL record and serializer model.
   - Represent host as internal domain, IPv4, IPv6, or opaque host variants.
   - Represent path as either an opaque path string or a list of path segments.
   - Implement URL, host, path, integer, and origin serialization.
   - Preserve `file:` origin behavior from upstream: serialize as `"null"`.

3. Port `url-state-machine.js`.
   - Use Java enum/state methods for the WHATWG parser states.
   - Preserve behavior for special schemes, relative URLs, file URLs, credentials, ports, path shortening, query, and fragment parsing.
   - Avoid leaking mutable URL-record state through the public API.

4. Build `WebURL` and `WebURLSearchParams` on top of the internal record.
   - Setters should invoke the same state override paths as upstream.
   - `searchParams` updates must rewrite the owning URL query.
   - Assigning `href` must replace the URL record and refresh `searchParams`.

5. Add IDNA provider support.
   - Define an internal `IdnaProcessor`.
   - Prefer ICU4J at runtime when available.
   - Fall back to `java.net.IDN` when ICU4J is absent.
   - Do not expose provider selection through the normal public API.
   - Add package-private or test-only hooks to force ICU and JDK providers in tests.

## Build And Dependency Plan

Main code must continue to run with no required runtime dependency outside `java.base`.

Use compile-only dependencies for optional/static typing support:

- `compileOnly("org.jetbrains:annotations:26.1.0")`
- `compileOnly("com.ibm.icu:icu4j:78.3")`

Update `module-info.java` so the module:

- Exports only `org.glavo.url`.
- Does not export internal implementation packages.
- Uses static requirements only where needed for compile-time optional dependencies.

All Java classes must follow the repository style rules:

- Annotate every class with `@NotNullByDefault`.
- Use explicit `@Nullable` for nullable values.
- Do not use `Optional`.
- Use `record` where it fits the data model.
- Add `///` Markdown Javadocs for every class, field, and method.
- Keep production comments and messages in English.

## Test Plan

Use WPT-driven conformance with fixed data files checked into the test tree.

Import or vendor the needed resources from the same WPT revision used by upstream:

- `urltestdata.json`
- `urltestdata-javascript-only.json`
- `setters_tests.json`
- `percent-encoding.json`
- `toascii.json`
- `IdnaTestV2.json`
- `IdnaTestV2-removed.json`

Create JUnit tests covering:

- MDN factory and relative URL examples.
- Parse failures and `canParse`.
- URL serialization and origin serialization.
- Special schemes, non-special schemes, `file:`, opaque paths, IPv4, IPv6, credentials, default ports, query, and fragment behavior.
- All public setters, including no-op setter cases.
- `WebURLSearchParams` parsing, mutation, sorting, serialization, and live URL synchronization.
- Percent decoding and `application/x-www-form-urlencoded` behavior.
- IDNA behavior with ICU available and JDK fallback behavior without ICU.

Track JDK fallback differences from ICU/WHATWG using an explicit allowlist. The ICU provider should be the conformance target for IDNA tests.

Run verification with:

```powershell
./gradlew -g .gradle-user-home compileJava compileTestJava
./gradlew -g .gradle-user-home test
```

Use a ten-minute timeout for Gradle `test`.

## Assumptions

- The main public URL type remains `WebURL`, now as a sealed interface with its implementation in `org.glavo.url.internal`.
- `WebURLSearchParams` is added as a public companion class.
- IDNA provider selection remains internal and automatic.
- Non-UTF-8 document encoding support for low-level parsing is not a first-stage public API commitment.
- The first implementation should prioritize correctness and testability over micro-optimizations.

## Suggested Milestones

1. Foundation helpers and tests for encoding, percent encoding, and form-urlencoded behavior.
2. URL record, host model, and serializers.
3. Basic parser for absolute, relative, special, non-special, and `file:` URLs.
4. Public `WebURL` API and setters.
5. `WebURLSearchParams` and live query synchronization.
6. ICU/JDK IDNA provider layer and WPT IDNA conformance.
7. Full WPT data test pass, fallback-difference documentation, and cleanup.
