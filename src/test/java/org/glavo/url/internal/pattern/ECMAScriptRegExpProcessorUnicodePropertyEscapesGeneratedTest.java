/*
 * Copyright 2026 Glavo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glavo.url.internal.pattern;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorTest262Support.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/// Runs generated Unicode property escape tests from `unicode-property-escapes-tests` at test runtime.
///
/// The dynamic tests keep the generated Java sources small while still exercising the full generated data set.
/// Each factory uses the fixed GitHub source URI for the template that generated the JavaScript input file.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorUnicodePropertyEscapesGeneratedTest {
    /// The pinned `unicode-property-escapes-tests` commit used for template links.
    private static final String UNICODE_PROPERTY_ESCAPES_TESTS_COMMIT =
            "968e1ac2d80569541b244f85604092338710f6ae";

    /// The pinned `unicode-property-escapes-tests` template directory URL.
    private static final String UNICODE_PROPERTY_ESCAPES_TESTS_TEMPLATES_URL =
            "https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/" +
                    UNICODE_PROPERTY_ESCAPES_TESTS_COMMIT + "/templates/";

    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/character-property.template
    private static final URI CHARACTER_PROPERTY_TEMPLATE_URI =
            URI.create(UNICODE_PROPERTY_ESCAPES_TESTS_TEMPLATES_URL + "character-property.template");

    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings.template
    private static final URI PROPERTY_OF_STRINGS_TEMPLATE_URI =
            URI.create(UNICODE_PROPERTY_ESCAPES_TESTS_TEMPLATES_URL + "property-of-strings.template");

    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings-negative-P.template
    private static final URI PROPERTY_OF_STRINGS_NEGATIVE_P_TEMPLATE_URI =
            URI.create(UNICODE_PROPERTY_ESCAPES_TESTS_TEMPLATES_URL + "property-of-strings-negative-P.template");

    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings-negative-CharacterClass.template
    private static final URI PROPERTY_OF_STRINGS_NEGATIVE_CHARACTER_CLASS_TEMPLATE_URI = URI.create(
            UNICODE_PROPERTY_ESCAPES_TESTS_TEMPLATES_URL + "property-of-strings-negative-CharacterClass.template"
    );

    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings-negative-u.template
    private static final URI PROPERTY_OF_STRINGS_NEGATIVE_U_TEMPLATE_URI =
            URI.create(UNICODE_PROPERTY_ESCAPES_TESTS_TEMPLATES_URL + "property-of-strings-negative-u.template");

    /// The checked-out `unicode-property-escapes-tests` directory.
    private static final Path EXTERNAL_ROOT = Path.of("external", "unicode-property-escapes-tests");

    /// The generated character property escape output directory.
    private static final Path CHARACTER_PROPERTY_OUTPUT =
            EXTERNAL_ROOT.resolve("output").resolve("property-escapes").resolve("generated");

    /// The generated property-of-strings output directory.
    private static final Path PROPERTY_OF_STRINGS_OUTPUT = CHARACTER_PROPERTY_OUTPUT.resolve("strings");

    /// Matches generated `testPropertyEscapes(...)` calls.
    private static final Pattern PROPERTY_ESCAPE_CALL = Pattern.compile(
            "testPropertyEscapes\\(\\s*/((?:\\\\.|[^/\\\\])*)/([a-z]*)\\s*,\\s*" +
                    "(matchSymbols|nonMatchSymbols)\\s*,\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*\\);",
            Pattern.DOTALL
    );

    /// Matches the generated `\P{Any}` empty-string assertion.
    private static final Pattern EMPTY_NEGATED_PROPERTY_ESCAPE_ASSERTION = Pattern.compile(
            "assert\\(\\s*!/((?:\\\\.|[^/\\\\])*)/([a-z]*)\\.test\\(\"\"\\),",
            Pattern.DOTALL
    );

    /// Matches a JavaScript regular-expression literal stored in an object property.
    private static final Pattern REGEXP_PROPERTY = Pattern.compile(
            "regExp:\\s*/((?:\\\\.|[^/\\\\])*)/([a-z]*),",
            Pattern.DOTALL
    );

    /// Matches a JavaScript string literal stored in an object property.
    private static final Pattern EXPRESSION_PROPERTY = Pattern.compile(
            "expression:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
            Pattern.DOTALL
    );

    /// Matches the regular-expression literal in negative generated files.
    private static final Pattern NEGATIVE_REGEXP_LITERAL = Pattern.compile(
            "\\$DONOTEVALUATE\\(\\);\\s*/((?:\\\\.|[^/\\\\])*)/([a-z]*);",
            Pattern.DOTALL
    );

    /// Matches JavaScript string literals inside generated arrays.
    private static final Pattern STRING_LITERAL = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

    /// Matches generated hexadecimal code point literals.
    private static final Pattern HEX_CODE_POINT = Pattern.compile("0x([0-9A-Fa-f]+)");

    /// Matches generated hexadecimal code point range literals.
    private static final Pattern HEX_CODE_POINT_RANGE = Pattern.compile(
            "\\[\\s*0x([0-9A-Fa-f]+)\\s*,\\s*0x([0-9A-Fa-f]+)\\s*]"
    );

    /// Prevents instantiation.
    private ECMAScriptRegExpProcessorUnicodePropertyEscapesGeneratedTest() {
    }

    /// Creates dynamic tests for files generated from `character-property.template`.
    ///
    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/character-property.template
    ///
    /// @return the dynamic tests
    /// @throws IOException if the generated files cannot be read
    @TestFactory
    public List<DynamicTest> characterPropertyEscapes() throws IOException {
        requireDirectory(CHARACTER_PROPERTY_OUTPUT);

        ArrayList<DynamicTest> tests = new ArrayList<>();
        for (Path file : listFiles(CHARACTER_PROPERTY_OUTPUT)) {
            for (PropertyEscapeTestCase testCase : parsePropertyEscapeTestCases(file)) {
                tests.add(DynamicTest.dynamicTest(testCase.displayName(), CHARACTER_PROPERTY_TEMPLATE_URI, () -> {
                    assumeUnicodePropertyEscapesSupported();
                    assertPropertyEscapes(
                            testCase.regexp(),
                            testCase.symbols().loneCodePoints(),
                            testCase.symbols().ranges(),
                            testCase.expression()
                    );
                }));
            }

            EmptyNegatedPropertyTestCase testCase = parseEmptyNegatedPropertyTestCase(file);
            if (testCase != null) {
                tests.add(DynamicTest.dynamicTest(testCase.displayName(), CHARACTER_PROPERTY_TEMPLATE_URI, () -> {
                    assumeUnicodePropertyEscapesSupported();
                    assertFalse(
                            compileProcessed(testCase.regexp()).matcher("").find(),
                            () -> "`" + testCase.expression() + "` should match nothing"
                    );
                }));
            }
        }
        return tests;
    }

    /// Creates dynamic tests for files generated from `property-of-strings.template`.
    ///
    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings.template
    ///
    /// @return the dynamic tests
    /// @throws IOException if the generated files cannot be read
    @TestFactory
    public List<DynamicTest> propertiesOfStrings() throws IOException {
        requireDirectory(PROPERTY_OF_STRINGS_OUTPUT);

        ArrayList<DynamicTest> tests = new ArrayList<>();
        for (Path file : listFiles(PROPERTY_OF_STRINGS_OUTPUT)) {
            if (isNegativePropertyOfStringsFile(file)) {
                continue;
            }

            PropertyOfStringsTestCase testCase = parsePropertyOfStringsTestCase(file);
            tests.add(DynamicTest.dynamicTest(testCase.displayName(), PROPERTY_OF_STRINGS_TEMPLATE_URI, () -> {
                assumePropertiesOfStringsSupported();
                assertPropertyOfStrings(
                        testCase.regexp(),
                        testCase.expression(),
                        testCase.matchStrings(),
                        testCase.nonMatchStrings()
                );
            }));
        }
        return tests;
    }

    /// Creates dynamic tests for files generated from `property-of-strings-negative-P.template`.
    ///
    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings-negative-P.template
    ///
    /// @return the dynamic tests
    /// @throws IOException if the generated files cannot be read
    @TestFactory
    public List<DynamicTest> negatedPropertiesOfStringsWithUpperP() throws IOException {
        return negativePropertiesOfStrings(
                "-negative-P.js",
                PROPERTY_OF_STRINGS_NEGATIVE_P_TEMPLATE_URI
        );
    }

    /// Creates dynamic tests for files generated from `property-of-strings-negative-CharacterClass.template`.
    ///
    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings-negative-CharacterClass.template
    ///
    /// @return the dynamic tests
    /// @throws IOException if the generated files cannot be read
    @TestFactory
    public List<DynamicTest> negatedPropertiesOfStringsInCharacterClasses() throws IOException {
        return negativePropertiesOfStrings(
                "-negative-CharacterClass.js",
                PROPERTY_OF_STRINGS_NEGATIVE_CHARACTER_CLASS_TEMPLATE_URI
        );
    }

    /// Creates dynamic tests for files generated from `property-of-strings-negative-u.template`.
    ///
    /// Source: https://github.com/mathiasbynens/unicode-property-escapes-tests/blob/968e1ac2d80569541b244f85604092338710f6ae/templates/property-of-strings-negative-u.template
    ///
    /// @return the dynamic tests
    /// @throws IOException if the generated files cannot be read
    @TestFactory
    public List<DynamicTest> propertiesOfStringsWithUnicodeFlag() throws IOException {
        return negativePropertiesOfStrings(
                "-negative-u.js",
                PROPERTY_OF_STRINGS_NEGATIVE_U_TEMPLATE_URI
        );
    }

    /// Creates dynamic tests for generated negative property-of-strings files.
    ///
    /// @param fileSuffix the generated file suffix
    /// @param sourceUri the fixed GitHub template source URI
    /// @return the dynamic tests
    /// @throws IOException if the generated files cannot be read
    private static List<DynamicTest> negativePropertiesOfStrings(String fileSuffix, URI sourceUri)
            throws IOException {
        requireDirectory(PROPERTY_OF_STRINGS_OUTPUT);

        ArrayList<DynamicTest> tests = new ArrayList<>();
        for (Path file : listFiles(PROPERTY_OF_STRINGS_OUTPUT)) {
            if (!file.getFileName().toString().endsWith(fileSuffix)) {
                continue;
            }

            InvalidRegExpTestCase testCase = parseInvalidRegExpTestCase(file);
            tests.add(DynamicTest.dynamicTest(testCase.displayName(), sourceUri, () -> {
                assumeUnicodePropertyEscapesSupported();
                assertUnsupported(testCase.regexp());
            }));
        }
        return tests;
    }

    /// Requires a generated source directory.
    ///
    /// @param directory the directory to require
    private static void requireDirectory(Path directory) {
        Assumptions.assumeTrue(
                Files.isDirectory(directory),
                () -> "Missing external unicode-property-escapes-tests directory: " + directory.toAbsolutePath()
        );
    }

    /// Lists files in a deterministic order.
    ///
    /// @param directory the directory to list
    /// @return the regular files in lexical file-name order
    /// @throws IOException if the directory cannot be listed
    private static List<Path> listFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    /// Parses all `testPropertyEscapes(...)` calls in a generated character property file.
    ///
    /// @param file the generated file
    /// @return the parsed test cases
    /// @throws IOException if the file cannot be read
    private static List<PropertyEscapeTestCase> parsePropertyEscapeTestCases(Path file) throws IOException {
        String source = Files.readString(file);
        String relativePath = relativeExternalPath(file);
        CodePointData matchSymbols = parseBuildString(source, "matchSymbols");
        @Nullable CodePointData nonMatchSymbols = parseBuildStringOrNull(source, "nonMatchSymbols");

        ArrayList<PropertyEscapeTestCase> tests = new ArrayList<>();
        Matcher matcher = PROPERTY_ESCAPE_CALL.matcher(source);
        while (matcher.find()) {
            CodePointData symbols = switch (matcher.group(3)) {
                case "matchSymbols" -> matchSymbols;
                case "nonMatchSymbols" -> requireNonNull(nonMatchSymbols, file, "nonMatchSymbols");
                default -> throw parseError(file, "unknown symbol variable: " + matcher.group(3));
            };
            String regexp = decodeRegExpLiteralSource(matcher.group(1));
            String flags = matcher.group(2);
            String expression = decodeJavaScriptString(matcher.group(4));
            tests.add(new PropertyEscapeTestCase(
                    relativePath,
                    regexp,
                    flags,
                    expression,
                    symbols
            ));
        }

        if (tests.isEmpty()) {
            throw parseError(file, "no testPropertyEscapes call found");
        }
        return tests;
    }

    /// Parses the generated `\P{Any}` empty-string assertion when present.
    ///
    /// @param file the generated file
    /// @return the parsed test case, or `null` when the file does not contain this assertion
    /// @throws IOException if the file cannot be read
    private static @Nullable EmptyNegatedPropertyTestCase parseEmptyNegatedPropertyTestCase(Path file)
            throws IOException {
        String source = Files.readString(file);
        Matcher matcher = EMPTY_NEGATED_PROPERTY_ESCAPE_ASSERTION.matcher(source);
        if (!matcher.find()) {
            return null;
        }

        String regexp = decodeRegExpLiteralSource(matcher.group(1));
        return new EmptyNegatedPropertyTestCase(
                relativeExternalPath(file),
                regexp,
                matcher.group(2),
                regexp
        );
    }

    /// Parses a generated positive property-of-strings test file.
    ///
    /// @param file the generated file
    /// @return the parsed test case
    /// @throws IOException if the file cannot be read
    private static PropertyOfStringsTestCase parsePropertyOfStringsTestCase(Path file)
            throws IOException {
        String source = Files.readString(file);
        RegExpLiteral regexp = parseRegExpProperty(source, file);
        String expression = decodeJavaScriptString(requireMatch(EXPRESSION_PROPERTY, source, file, "expression").group(1));
        String[] matchStrings = parseStringArray(extractArrayBody(source, "matchStrings", file));
        String[] nonMatchStrings = parseStringArray(extractArrayBody(source, "nonMatchStrings", file));
        return new PropertyOfStringsTestCase(
                relativeExternalPath(file),
                regexp.source(),
                regexp.flags(),
                expression,
                matchStrings,
                nonMatchStrings
        );
    }

    /// Parses a generated negative regular-expression test file.
    ///
    /// @param file the generated file
    /// @return the parsed test case
    /// @throws IOException if the file cannot be read
    private static InvalidRegExpTestCase parseInvalidRegExpTestCase(Path file) throws IOException {
        String source = Files.readString(file);
        Matcher matcher = requireMatch(NEGATIVE_REGEXP_LITERAL, source, file, "negative regular-expression literal");
        return new InvalidRegExpTestCase(
                relativeExternalPath(file),
                decodeRegExpLiteralSource(matcher.group(1)),
                matcher.group(2)
        );
    }

    /// Parses a generated `regExp` object property.
    ///
    /// @param source the JavaScript source
    /// @param file the generated file
    /// @return the parsed regular-expression literal
    private static RegExpLiteral parseRegExpProperty(String source, Path file) {
        Matcher matcher = requireMatch(REGEXP_PROPERTY, source, file, "regExp");
        return new RegExpLiteral(decodeRegExpLiteralSource(matcher.group(1)), matcher.group(2));
    }

    /// Parses a generated `buildString(...)` call.
    ///
    /// @param source the JavaScript source
    /// @param variable the generated variable name
    /// @return the parsed code point data
    private static CodePointData parseBuildString(String source, String variable) {
        return requireNonNull(parseBuildStringOrNull(source, variable), Path.of(variable), variable);
    }

    /// Parses a generated `buildString(...)` call when present.
    ///
    /// @param source the JavaScript source
    /// @param variable the generated variable name
    /// @return the parsed code point data, or `null` when the variable is absent
    private static @Nullable CodePointData parseBuildStringOrNull(String source, String variable) {
        String prefix = "const " + variable + " = buildString(";
        int start = source.indexOf(prefix);
        if (start < 0) {
            return null;
        }

        start += prefix.length();
        int end = source.indexOf(");", start);
        if (end < 0) {
            throw parseError(Path.of(variable), "unterminated buildString call");
        }

        String object = source.substring(start, end);
        String loneCodePoints = parseLoneCodePoints(extractArrayBody(object, "loneCodePoints", Path.of(variable)));
        String ranges = parseRanges(extractArrayBody(object, "ranges", Path.of(variable)));
        return new CodePointData(loneCodePoints, ranges);
    }

    /// Parses generated lone code point data.
    ///
    /// @param arrayBody the JavaScript array body
    /// @return the encoded code point list used by the shared assertion helper
    private static String parseLoneCodePoints(String arrayBody) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = HEX_CODE_POINT.matcher(arrayBody);
        while (matcher.find()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(formatHexCodePoint(Integer.parseInt(matcher.group(1), 16)));
        }
        return builder.toString();
    }

    /// Parses generated code point range data.
    ///
    /// @param arrayBody the JavaScript array body
    /// @return the encoded range list used by the shared assertion helper
    private static String parseRanges(String arrayBody) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = HEX_CODE_POINT_RANGE.matcher(arrayBody);
        while (matcher.find()) {
            if (!builder.isEmpty()) {
                builder.append(';');
            }
            builder.append(formatHexCodePoint(Integer.parseInt(matcher.group(1), 16)));
            builder.append('-');
            builder.append(formatHexCodePoint(Integer.parseInt(matcher.group(2), 16)));
        }
        return builder.toString();
    }

    /// Extracts a JavaScript array body following a generated property name.
    ///
    /// @param source the JavaScript source
    /// @param property the property name
    /// @param file the generated file
    /// @return the array body
    private static String extractArrayBody(String source, String property, Path file) {
        int propertyStart = source.indexOf(property + ":");
        if (propertyStart < 0) {
            throw parseError(file, "missing array property: " + property);
        }

        int open = source.indexOf('[', propertyStart);
        if (open < 0) {
            throw parseError(file, "missing array start for: " + property);
        }

        int close = findMatchingBracket(source, open, file);
        return source.substring(open + 1, close);
    }

    /// Finds the matching bracket for a JavaScript array.
    ///
    /// @param source the JavaScript source
    /// @param open the open bracket index
    /// @param file the generated file
    /// @return the matching close bracket index
    private static int findMatchingBracket(String source, int open, Path file) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = open; index < source.length(); index++) {
            char ch = source.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }

            if (ch == '"') {
                inString = true;
            } else if (ch == '[') {
                depth++;
            } else if (ch == ']') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        throw parseError(file, "unterminated array");
    }

    /// Parses a generated JavaScript string array body.
    ///
    /// @param arrayBody the JavaScript array body
    /// @return the decoded Java strings
    private static String[] parseStringArray(String arrayBody) {
        ArrayList<String> strings = new ArrayList<>();
        Matcher matcher = STRING_LITERAL.matcher(arrayBody);
        while (matcher.find()) {
            strings.add(decodeJavaScriptString(matcher.group(1)));
        }
        return strings.toArray(String[]::new);
    }

    /// Decodes a JavaScript string literal body.
    ///
    /// @param value the string literal body without quotes
    /// @return the decoded Java string
    private static String decodeJavaScriptString(String value) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            if (ch != '\\') {
                builder.append(ch);
                continue;
            }

            if (++index >= value.length()) {
                throw new IllegalArgumentException("Trailing JavaScript string escape");
            }

            ch = value.charAt(index);
            switch (ch) {
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'v' -> builder.append('\u000B');
                case '0' -> builder.append('\0');
                case 'u' -> index = decodeUnicodeEscape(value, index + 1, builder);
                default -> builder.append(ch);
            }
        }
        return builder.toString();
    }

    /// Decodes a JavaScript Unicode escape.
    ///
    /// @param value the string literal body
    /// @param start the index after `u`
    /// @param builder the target builder
    /// @return the final consumed index
    private static int decodeUnicodeEscape(String value, int start, StringBuilder builder) {
        if (start < value.length() && value.charAt(start) == '{') {
            int end = value.indexOf('}', start + 1);
            if (end < 0) {
                throw new IllegalArgumentException("Unterminated JavaScript Unicode code point escape");
            }
            builder.appendCodePoint(Integer.parseInt(value, start + 1, end, 16));
            return end;
        }

        int end = start + 4;
        if (end > value.length()) {
            throw new IllegalArgumentException("Short JavaScript Unicode escape");
        }
        builder.append((char) Integer.parseInt(value, start, end, 16));
        return end - 1;
    }

    /// Decodes a JavaScript regular-expression literal source.
    ///
    /// @param value the regular-expression literal source without delimiters
    /// @return the regular-expression source
    private static String decodeRegExpLiteralSource(String value) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            if (ch == '\\' && index + 1 < value.length() && value.charAt(index + 1) == '/') {
                builder.append('/');
                index++;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    /// Formats a code point as a six-digit uppercase hexadecimal value.
    ///
    /// @param codePoint the code point
    /// @return the formatted code point
    private static String formatHexCodePoint(int codePoint) {
        return String.format(Locale.ROOT, "%06X", codePoint);
    }

    /// Checks whether a generated property-of-strings file is negative.
    ///
    /// @param file the generated file
    /// @return `true` if the file was generated from a negative template
    private static boolean isNegativePropertyOfStringsFile(Path file) {
        return file.getFileName().toString().contains("-negative-");
    }

    /// Assumes that character property escapes are supported by the current processor.
    private static void assumeUnicodePropertyEscapesSupported() {
        Assumptions.assumeTrue(
                canCompileProcessed("^\\p{ASCII}+$"),
                "Unicode property escapes are not supported yet"
        );
    }

    /// Assumes that properties of strings are supported by the current processor.
    private static void assumePropertiesOfStringsSupported() {
        Assumptions.assumeTrue(
                canCompileProcessed("^\\p{Basic_Emoji}+$"),
                "Unicode property escapes of strings are not supported yet"
        );
    }

    /// Tests whether the current processor can compile a source.
    ///
    /// @param regexp the regular-expression source
    /// @return `true` if the source can be processed and compiled
    private static boolean canCompileProcessed(String regexp) {
        try {
            compileProcessed(regexp);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /// Requires a non-null parser result.
    ///
    /// @param value the nullable value
    /// @param file the generated file
    /// @param label the value label
    /// @return the non-null value
    private static CodePointData requireNonNull(@Nullable CodePointData value, Path file, String label) {
        if (value == null) {
            throw parseError(file, "missing " + label);
        }
        return value;
    }

    /// Requires a regular-expression match.
    ///
    /// @param pattern the pattern to match
    /// @param source the source to search
    /// @param file the generated file
    /// @param label the matched value label
    /// @return the matcher positioned at the first match
    private static Matcher requireMatch(Pattern pattern, String source, Path file, String label) {
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            throw parseError(file, "missing " + label);
        }
        return matcher;
    }

    /// Creates a generated-file parse error.
    ///
    /// @param file the generated file
    /// @param message the error message
    /// @return the parse error
    private static IllegalArgumentException parseError(Path file, String message) {
        return new IllegalArgumentException(file + ": " + message);
    }

    /// Returns an external-repository-relative display path.
    ///
    /// @param file the generated file
    /// @return the display path
    private static String relativeExternalPath(Path file) {
        return EXTERNAL_ROOT.relativize(file).toString().replace('\\', '/');
    }

    /// A parsed generated `testPropertyEscapes(...)` call.
    ///
    /// @param path the external-repository-relative source file path
    /// @param regexp the regular-expression source without delimiters
    /// @param flags the JavaScript regular-expression flags
    /// @param expression the property escape expression
    /// @param symbols the generated input symbols
    private record PropertyEscapeTestCase(
            String path,
            String regexp,
            String flags,
            String expression,
            CodePointData symbols
    ) {
        /// Returns the JUnit display name.
        ///
        /// @return the display name
        String displayName() {
            return path + " :: " + expression + " /" + flags;
        }
    }

    /// A parsed generated empty-string non-match assertion.
    ///
    /// @param path the external-repository-relative source file path
    /// @param regexp the regular-expression source without delimiters
    /// @param flags the JavaScript regular-expression flags
    /// @param expression the property escape expression
    private record EmptyNegatedPropertyTestCase(
            String path,
            String regexp,
            String flags,
            String expression
    ) {
        /// Returns the JUnit display name.
        ///
        /// @return the display name
        String displayName() {
            return path + " :: " + expression + " /" + flags;
        }
    }

    /// A parsed generated `testPropertyOfStrings(...)` call.
    ///
    /// @param path the external-repository-relative source file path
    /// @param regexp the regular-expression source without delimiters
    /// @param flags the JavaScript regular-expression flags
    /// @param expression the property escape expression
    /// @param matchStrings the matching strings
    /// @param nonMatchStrings the non-matching strings
    private record PropertyOfStringsTestCase(
            String path,
            String regexp,
            String flags,
            String expression,
            String[] matchStrings,
            String[] nonMatchStrings
    ) {
        /// Returns the JUnit display name.
        ///
        /// @return the display name
        String displayName() {
            return path + " :: " + expression + " /" + flags;
        }
    }

    /// A parsed generated negative regular-expression test.
    ///
    /// @param path the external-repository-relative source file path
    /// @param regexp the regular-expression source without delimiters
    /// @param flags the JavaScript regular-expression flags
    private record InvalidRegExpTestCase(
            String path,
            String regexp,
            String flags
    ) {
        /// Returns the JUnit display name.
        ///
        /// @return the display name
        String displayName() {
            return path + " :: /" + regexp + "/" + flags;
        }
    }

    /// Parsed generated code point data.
    ///
    /// @param loneCodePoints the encoded lone code points
    /// @param ranges the encoded inclusive ranges
    private record CodePointData(String loneCodePoints, String ranges) {
    }

    /// A parsed JavaScript regular-expression literal.
    ///
    /// @param source the regular-expression source without delimiters
    /// @param flags the JavaScript regular-expression flags
    private record RegExpLiteral(String source, String flags) {
    }
}
