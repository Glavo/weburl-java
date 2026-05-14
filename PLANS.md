# Test262 RegExp Test Porting Plan

## Summary

Port selected tests from `external/test262/test/built-ins/RegExp` to Java tests for
`ECMAScriptRegExpProcessor`. The ported tests should cover ECMAScript regular-expression element
syntax, translation, rejection, and matching behavior that can be mapped to this processor.

Use test262 commit `673e9bacbe28590f501e2dcd817aadcc31899191` for all source links.

## Steps

1. Add split `org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorTest262*Test` classes.
2. Port only tests that directly map to `ECMAScriptRegExpProcessor` behavior:
   - regular-expression grammar and syntax errors
   - disjunction, atom, quantifier, character class, and escape behavior
   - supported finite ASCII Unicode Sets behavior
   - named group syntax that maps to URLPattern regular-expression elements
3. Do not port pure JavaScript `RegExp` runtime behavior:
   - constructor semantics
   - prototype methods
   - `lastIndex`, flags properties, species, and cross-realm behavior
   - `String.prototype` integration such as replace, split, match, and matchAll
4. Prefer one JUnit `@Test` method per ported test262 case.
5. Annotate every test method with a `/// Source: ...` Javadoc link to the original GitHub file.
   The link must include the fixed commit hash.
6. Avoid dynamic tests for hand-ported test262 cases, so each case is easy to identify from IDE and
   test reports. Generated Unicode property escape cases are the exception: use runtime
   `DynamicTest` cases based on `external/unicode-property-escapes-tests/output`, and attach fixed
   GitHub source URIs for the corresponding `unicode-property-escapes-tests` templates.
7. If a relevant test cannot pass because the processor intentionally does not support that syntax,
   port it as an `@Disabled` test and include a specific reason.
8. Use shared helper methods only for common assertions:
   - `assertSupported(String regexp)`
   - `assertTranslated(String regexp, String expected)`
   - `assertUnsupported(String regexp)`
   - `assertMatches(String regexp, String input)`
   - `assertDoesNotMatch(String regexp, String input)`
   - `assertFinds(String regexp, String input, String expected)`
9. For original test262 tests that use `^...$` only to force full-string matching, remove the outer
   anchors in the ported regexp and use full-match helpers. `ECMAScriptRegExpProcessor` currently
   rejects anchors because URLPattern component matching is already whole-component matching.
10. For original unanchored `.exec` or `.test` cases, port them only when they directly validate
    regular-expression element semantics. Use a helper that simulates JavaScript search behavior with
    Java `Pattern.find()`.
11. Keep the hand-ported test262 source independent from `external/test262`; the `external/test262`
    checkout is only a local reference and must not be read at test runtime. The generated Unicode
    property escape tests intentionally read `external/unicode-property-escapes-tests/output` at
    test runtime and are skipped when that checkout is absent.

## Disabled Case Reasons

Use `@Disabled` with a specific reason for relevant cases requiring unsupported syntax such as:

- capturing groups other than URLPattern outer capture handling
- lookahead or lookbehind assertions
- anchors, word boundaries, or backreferences
- `\s`, `\S`, Unicode property escapes, or Unicode escape syntax
- non-ASCII or escaped Unicode group names
- dotAll, multiline, sticky, global, modifiers, or other JavaScript flags not modeled by the
  processor
- Unicode Sets features beyond the current finite ASCII subset, including string properties

## Comprehensive Port List

The comprehensive source inventory is recorded in [`TEST262_REGEXP_PORT_LIST.md`](TEST262_REGEXP_PORT_LIST.md).
It covers all test262 `RegExp` source groups that should be considered for `ECMAScriptRegExpProcessor`
tests, and separately records the groups excluded as pure JavaScript runtime behavior.

## Validation

Run these commands after implementation:

```shell
./gradlew -g .gradle-user-home test --tests org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorTest262*
./gradlew -g .gradle-user-home test --tests org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorUnicodePropertyEscapesGeneratedTest
./gradlew -g .gradle-user-home test
./gradlew -g .gradle-user-home compileBenchmarkJava
git diff --check
```
