# Test262 RegExp Comprehensive Port List

## Summary

This file lists all `external/test262/test/built-ins/RegExp` sources that should be considered when
porting tests for `ECMAScriptRegExpProcessor`.

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

## Current Java Source Coverage

Current target test classes:
`src/test/java/org/glavo/url/internal/pattern/ECMAScriptRegExpProcessorTest262*Test.java`

Generated Unicode property escape target class:
`src/test/java/org/glavo/url/internal/pattern/ECMAScriptRegExpProcessorUnicodePropertyEscapesGeneratedTest.java`

Shared helpers:
`src/test/java/org/glavo/url/internal/pattern/ECMAScriptRegExpProcessorTest262Support.java`

- Source-linked Java test methods: 806
- Enabled Java test methods: 186
- Disabled Java test methods: 620
- Unique fixed-commit Test262 source links: 806
- Runtime-generated Unicode property escape dynamic tests: 3520

Latest porting increment:

- Replaced the generated `RegExp/property-escapes/generated/*.js` and
  `RegExp/property-escapes/generated/strings/*.js` Java ports with runtime dynamic tests that read
  `external/unicode-property-escapes-tests/output`.
- The dynamic tests parse the generated JavaScript files and attach fixed GitHub source URIs for the
  corresponding `unicode-property-escapes-tests` templates instead of linking to copied test262
  generated output files.

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
| [`RegExp/CharacterClassEscapes/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes) | 12 | Mixed | `\d`, `\D`, `\w`, and `\W` should be enabled; `\s` and `\S` are disabled until supported. |
| [`RegExp/dotall/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/dotall) | 4 | Disabled | JavaScript `s` flag semantics are not modeled by the processor. |
| [`RegExp/lookBehind/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind) | 17 | Disabled | Lookbehind assertions are not supported. |
| [`RegExp/named-groups/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups) | 36 | Mixed | ASCII named-group syntax can be enabled; backreferences, duplicate-name semantics, Unicode names, and string-method behavior are disabled or omitted. |
| [`RegExp/property-escapes/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes) | 613 | Disabled | Unicode property escapes are not supported. |
| [`RegExp/regexp-modifiers/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers) | 70 | Disabled | Inline RegExp modifiers are not supported. |
| [`RegExp/unicodeSets/**/*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets) | 114 | Mixed | Finite ASCII class-set operations can be enabled; property escapes and string literals are disabled. |
| [`RegExp/character-class-escape-non-whitespace*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 2 | Disabled | `\S` is not supported. |
| [`RegExp/duplicate-named-capturing-groups-syntax.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/duplicate-named-capturing-groups-syntax.js) | 1 | Disabled | Duplicate named-group early-error validation is not implemented. |
| [`RegExp/early-err-modifiers*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 22 | Disabled | RegExp modifier flag syntax is not supported. |
| [`RegExp/lookahead-quantifier-match-groups.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookahead-quantifier-match-groups.js) | 1 | Disabled | Lookahead assertions and nested capturing groups are not supported. |
| [`RegExp/nullable-quantifier.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/nullable-quantifier.js) | 1 | Enabled if it maps without unsupported captures | Nullable quantifier behavior. |
| [`RegExp/quantifier-integer-limit.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/quantifier-integer-limit.js) | 1 | Disabled or enabled syntax rejection | Quantifier integer bounds. |
| [`RegExp/regexp-class-chars.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-class-chars.js) | 1 | Enabled | Forward slash inside character classes. |
| [`RegExp/syntax-err-arithmetic-modifiers*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 55 | Disabled | RegExp modifier arithmetic is not supported. |
| [`RegExp/u180e.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/u180e.js) | 1 | Disabled | Depends on unsupported whitespace-class semantics. |
| [`RegExp/unicode_*.js`](https://github.com/tc39/test262/tree/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp) | 14 | Disabled | Unicode-mode escape restrictions are not currently modeled. |

## Root Unicode Files

Port these root `unicode_*.js` sources as disabled tests unless support is added:

- [`unicode_character_class_backspace_escape.js`](https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_character_class_backspace_escape.js)
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
