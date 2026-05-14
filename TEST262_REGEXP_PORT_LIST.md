# Test262 RegExp Comprehensive Port List

## Summary

This file lists all `external/test262/test/built-ins/RegExp` sources that should be considered when
porting tests for `ECMAScriptRegExpProcessor`. It also records the current porting rules so the
coverage inventory and migration process stay in one place.

Source commit: `673e9bacbe28590f501e2dcd817aadcc31899191`

Generated Unicode property escape test source commit:
`mathiasbynens/unicode-property-escapes-tests@968e1ac2d80569541b244f85604092338710f6ae`

Inventory from the local checkout:

- Total `RegExp` JavaScript files: 1879
- Sources included in this port plan: 1275
- Sources excluded as JavaScript runtime behavior: 604

Each ported Java test method should include a `/// Source: ...` Javadoc link to the exact original
test262 file with the commit hash above. Generated Unicode property escape dynamic tests are the
exception: they use `DynamicTest` source URIs pointing to the fixed
`unicode-property-escapes-tests` template source that generated the JavaScript file.

## Porting Guidelines

Port only tests that directly map to `ECMAScriptRegExpProcessor` behavior:

- regular-expression grammar and syntax errors
- disjunction, atom, quantifier, character class, and escape behavior
- supported finite ASCII Unicode Sets behavior
- named group syntax that maps to URLPattern regular-expression elements
- matching behavior that can be expressed through the processor's translated Java pattern

Do not port pure JavaScript `RegExp` runtime behavior:

- constructor semantics
- prototype methods
- `lastIndex`, flags properties, species, and cross-realm behavior
- `String.prototype` integration such as replace, split, match, and matchAll

Prefer one JUnit `@Test` method per hand-ported test262 source. Annotate every test method with a
`/// Source: ...` Javadoc link to the original GitHub file, including the fixed test262 commit hash
above. Generated Unicode property escape cases are the exception: use runtime `DynamicTest` cases
based on `external/unicode-property-escapes-tests/output`, and attach fixed GitHub source URIs for
the corresponding `unicode-property-escapes-tests` templates.

If a relevant test cannot pass because the processor intentionally does not support that syntax,
port it as an `@Disabled` test and include a specific reason. Common disabled reasons include:

- capturing groups other than URLPattern outer capture handling
- capturing-group or backreference behavior inside lookbehind assertions
- backreferences
- Unicode property escapes and property-of-strings escapes
- non-ASCII or escaped Unicode group names
- duplicate named-group early-error validation
- dotAll, multiline, sticky, global, modifiers, or other JavaScript flags not modeled by the
  processor
- Unicode Sets features beyond the current finite ASCII subset, including string properties

Use shared helper methods only for common assertions:

- `assertSupported(String regexp)`
- `assertTranslated(String regexp, String expected)`
- `assertUnsupported(String regexp)`
- `assertMatches(String regexp, String input)`
- `assertDoesNotMatch(String regexp, String input)`
- `assertFinds(String regexp, String input, String expected)`

For original test262 tests that use `^` or `$`, keep the assertions when they are part of the tested
regular-expression semantics. For original unanchored `.exec` or `.test` cases, port them only when
they directly validate regular-expression element semantics. Use a helper that simulates JavaScript
search behavior with Java `Pattern.find()`.

Keep the hand-ported test262 source independent from `external/test262`; that checkout is only a
local reference and must not be read at test runtime. The generated Unicode property escape tests
intentionally read `external/unicode-property-escapes-tests/output` at test runtime and are skipped
when that checkout is absent.

## Current Java Source Coverage

Current target test classes:
`src/test/java/org/glavo/url/internal/pattern/ECMAScriptRegExpProcessorTest262*Test.java`

Generated Unicode property escape target class:
`src/test/java/org/glavo/url/internal/pattern/ECMAScriptRegExpProcessorUnicodePropertyEscapesGeneratedTest.java`

Shared helpers:
`src/test/java/org/glavo/url/internal/pattern/ECMAScriptRegExpProcessorTest262Support.java`

- Source-linked Java test methods: 810
- Enabled Java test methods: 276
- Disabled Java test methods: 534
- Unique fixed-commit Test262 source links: 806
- Runtime-generated Unicode property escape dynamic tests: 3520

## Semantic Port Audit

- Dynamically covered from `unicode-property-escapes-tests`:
  `output/property-escapes/generated/*.js`
  (441 source files, 3491 original `testPropertyEscapes` calls plus the `\P{Any}` empty-string
  assertion).
- Dynamically covered from `unicode-property-escapes-tests`:
  `output/property-escapes/generated/strings/*.js`
  (7 positive property-of-strings files with complete `matchStrings` and `nonMatchStrings`, plus
  21 negative template-generated files).
- Needs audit: remaining disabled tests generated from representative regular-expression sources,
  especially `RegExp/unicodeSets/generated`, `RegExp/named-groups`, and dynamically constructed
  `S15.10.2*` cases.

## Included Source Groups

Every source matched by these selectors should be ported, either as an enabled `@Test` or as an
`@Disabled` test with a specific reason. If a source file mixes regexp-element assertions with
JavaScript runtime assertions, port only the regexp-element assertions and omit the runtime-only
parts.

| Source selector | Count | Default handling | Notes |
| --- | ---: | --- | --- |
| [`RegExp/S15.10.1_A1_T*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 16 | Disabled or enabled syntax rejection | Early syntax-error cases such as repeated quantifiers. |
| [`RegExp/S15.10.2*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 291 | Mixed | Core ES5 regexp grammar and matching behavior. |
| [`RegExp/15.10.2*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 2 | Enabled syntax rejection if currently supported | Modernized syntax-error coverage for reversed ranges and quantifiers. |
| [`RegExp/15.10.4.1-2.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/15.10.4.1-2.js) | 1 | Enabled syntax rejection if currently supported | Invalid trailing escape source. |
| [`RegExp/CharacterClassEscapes/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes) | 12 | Mixed | `\d`, `\D`, `\s`, `\S`, `\w`, and `\W` cases are enabled where they map to URLPattern regexp elements. |
| [`RegExp/dotall/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/dotall) | 4 | Disabled | JavaScript `s` flag semantics are not modeled by the processor. |
| [`RegExp/lookBehind/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind) | 17 | Mixed | Lookbehind assertions are enabled when they do not depend on unsupported capture-group or backreference behavior. |
| [`RegExp/named-groups/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups) | 36 | Mixed | ASCII named-group syntax can be enabled; backreferences, duplicate-name semantics, Unicode names, and string-method behavior are disabled or omitted. |
| [`RegExp/property-escapes/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes) | 613 | Disabled | Unicode property escapes are not supported. |
| [`RegExp/regexp-modifiers/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers) | 70 | Disabled | Inline RegExp modifiers are not supported. |
| [`RegExp/unicodeSets/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets) | 114 | Mixed | Finite ASCII class-set operations can be enabled; property escapes and string literals are disabled. |
| [`RegExp/character-class-escape-non-whitespace*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 2 | Enabled | `\S` is translated with the ECMAScript whitespace set. |
| [`RegExp/duplicate-named-capturing-groups-syntax.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/duplicate-named-capturing-groups-syntax.js) | 1 | Disabled | Duplicate named-group early-error validation is not implemented. |
| [`RegExp/early-err-modifiers*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 22 | Disabled | RegExp modifier flag syntax is not supported. |
| [`RegExp/lookahead-quantifier-match-groups.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookahead-quantifier-match-groups.js) | 1 | Disabled | Lookahead assertions and nested capturing groups are not supported. |
| [`RegExp/nullable-quantifier.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/nullable-quantifier.js) | 1 | Enabled if it maps without unsupported captures | Nullable quantifier behavior. |
| [`RegExp/quantifier-integer-limit.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/quantifier-integer-limit.js) | 1 | Disabled or enabled syntax rejection | Quantifier integer bounds. |
| [`RegExp/regexp-class-chars.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-class-chars.js) | 1 | Enabled | Forward slash inside character classes. |
| [`RegExp/syntax-err-arithmetic-modifiers*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 55 | Disabled | RegExp modifier arithmetic is not supported. |
| [`RegExp/u180e.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/u180e.js) | 1 | Enabled | U+180E is treated as non-whitespace. |
| [`RegExp/unicode_*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 14 | Mixed | Backspace class escapes are enabled; remaining Unicode-mode escape restrictions are disabled. |

## Root Unicode Files

Port these root `unicode_*.js` sources as disabled tests unless support is added. The
`unicode_character_class_backspace_escape.js` source is currently enabled.

- [`unicode_full_case_folding.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_full_case_folding.js)
- [`unicode_identity_escape.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_identity_escape.js)
- [`unicode_restricted_brackets.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_brackets.js)
- [`unicode_restricted_character_class_escape.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_character_class_escape.js)
- [`unicode_restricted_identity_escape_alpha.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_alpha.js)
- [`unicode_restricted_identity_escape_c.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_c.js)
- [`unicode_restricted_identity_escape_u.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_u.js)
- [`unicode_restricted_identity_escape_x.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_x.js)
- [`unicode_restricted_identity_escape.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape.js)
- [`unicode_restricted_incomplete_quantifier.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_incomplete_quantifier.js)
- [`unicode_restricted_octal_escape.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_octal_escape.js)
- [`unicode_restricted_quantifiable_assertion.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_quantifiable_assertion.js)
- [`unicode_restricted_quantifier_without_atom.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_quantifier_without_atom.js)

## Excluded Source Groups

These sources should not be ported to `ECMAScriptRegExpProcessor` tests because they exercise
JavaScript `RegExp` objects, object properties, built-ins, string-method integration, or unrelated
runtime behavior rather than regexp-element parsing and matching.

| Source selector | Count | Reason |
| --- | ---: | --- |
| [`RegExp/escape/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/escape) | 20 | Tests `RegExp.escape`, not regexp element processing. |
| [`RegExp/match-indices/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/match-indices) | 14 | Tests JS `d` flag match indices and result objects. |
| [`RegExp/prototype/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/prototype) | 487 | Tests JavaScript `RegExp.prototype` methods and properties. |
| [`RegExp/Symbol.species/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/Symbol.species) | 4 | Tests JS species behavior. |
| Root constructor and object-behavior files | 79 | Tests constructor behavior, flags, object identity, descriptors, or `lastIndex`. |

The excluded root group includes files matching these selectors:

- `RegExp/15.10.4.1-1.js`
- `RegExp/15.10.4.1-3.js`
- `RegExp/15.10.4.1-4.js`
- `RegExp/call_*.js`
- `RegExp/duplicate-flags.js`
- `RegExp/from-regexp-like*.js`
- `RegExp/is-a-constructor.js`
- `RegExp/lastIndex.js`
- `RegExp/prop-desc.js`
- `RegExp/proto-from-ctor-realm.js`
- `RegExp/S15.10.3*.js`
- `RegExp/S15.10.4*.js`
- `RegExp/S15.10.5*.js`
- `RegExp/S15.10.7*.js`
- `RegExp/valid-flags-y.js`

## Implementation Notes

- Use one Java `@Test` method per ported source file whenever practical.
- Split a source file into multiple methods only when it contains several distinct regexp-element
  assertions with different support status.
- Disabled tests must remain source-linked and must state the unsupported syntax or behavior.
- Do not read `external/test262` from test runtime.

## Validation

Run these commands after changing the processor or the ported tests:

```shell
./gradlew -g .gradle-user-home test --tests org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorTest262*
./gradlew -g .gradle-user-home test --tests org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorUnicodePropertyEscapesGeneratedTest
./gradlew -g .gradle-user-home test
./gradlew -g .gradle-user-home compileBenchmarkJava
git diff --check
```
