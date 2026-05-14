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

import org.glavo.url.pattern.WebURLPatternParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Shared helpers for the test262 ECMAScriptRegExpProcessor tests.
@NotNullByDefault
final class ECMAScriptRegExpProcessorTest262Support {
    /// Prevents instantiation.
    private ECMAScriptRegExpProcessorTest262Support() {
    }

    /// Creates a single-code-point string.
    ///
    /// @param codePoint the Unicode code point
    /// @return the string containing the code point
    static String stringFromCodePoint(int codePoint) {
        return new String(Character.toChars(codePoint));
    }

    /// Asserts the semantics of a test262 `testPropertyOfStrings` call.
    ///
    /// @param regexp the regular-expression source without JavaScript delimiters
    /// @param expression the original property escape expression
    /// @param matchStrings the matching strings
    /// @param nonMatchStrings the non-matching strings
    static void assertPropertyOfStrings(
            String regexp,
            String expression,
            String[] matchStrings,
            String[] nonMatchStrings
    ) {
        assertPropertyOfStrings(compileProcessed(regexp), expression, matchStrings, nonMatchStrings);
    }

    /// Asserts the semantics of a test262 `testPropertyOfStrings` call.
    ///
    /// @param pattern the compiled Java regular expression
    /// @param expression the original property escape expression
    /// @param matchStrings the matching strings
    /// @param nonMatchStrings the non-matching strings
    static void assertPropertyOfStrings(
            Pattern pattern,
            String expression,
            String[] matchStrings,
            String[] nonMatchStrings
    ) {
        String allStrings = String.join("", matchStrings);
        if (!pattern.matcher(allStrings).matches()) {
            for (String string : matchStrings) {
                assertTrue(pattern.matcher(string).matches(),
                        () -> "`" + expression + "` should match " + formatCodePoints(string));
            }
        }

        if (nonMatchStrings.length == 0) {
            return;
        }

        String allNonMatchStrings = String.join("", nonMatchStrings);
        if (pattern.matcher(allNonMatchStrings).matches()) {
            for (String string : nonMatchStrings) {
                assertFalse(pattern.matcher(string).matches(),
                        () -> "`" + expression + "` should not match " + formatCodePoints(string));
            }
        }
    }

    /// Asserts the semantics of a test262 `testPropertyEscapes` call.
    ///
    /// @param regexp the regular-expression source without JavaScript delimiters
    /// @param loneCodePoints the space-separated six-digit hexadecimal lone code points
    /// @param ranges the semicolon-separated inclusive six-digit hexadecimal code point ranges
    /// @param expression the original property escape expression
    static void assertPropertyEscapes(String regexp, String loneCodePoints, String ranges, String expression) {
        Pattern pattern = compileProcessed(regexp);
        String input = buildCodePointString(loneCodePoints, ranges);
        if (pattern.matcher(input).matches()) {
            return;
        }

        forEachCodePoint(loneCodePoints, codePoint -> assertTrue(pattern.matcher(stringFromCodePoint(codePoint)).matches(),
                () -> "`" + expression + "` should match " + formatCodePoint(codePoint)));
        forEachRangeCodePoint(ranges, codePoint -> assertTrue(pattern.matcher(stringFromCodePoint(codePoint)).matches(),
                () -> "`" + expression + "` should match " + formatCodePoint(codePoint)));
    }

    /// Builds a string from test262 encoded code point data.
    ///
    /// @param loneCodePoints the space-separated six-digit hexadecimal lone code points
    /// @param ranges the semicolon-separated inclusive six-digit hexadecimal code point ranges
    /// @return the string containing the encoded code points
    static String buildCodePointString(String loneCodePoints, String ranges) {
        StringBuilder builder = new StringBuilder();
        forEachCodePoint(loneCodePoints, codePoint -> appendCodePoint(builder, codePoint));
        forEachRangeCodePoint(ranges, codePoint -> appendCodePoint(builder, codePoint));
        return builder.toString();
    }

    /// Appends a code point to a builder.
    ///
    /// @param builder the target builder
    /// @param codePoint the Unicode code point
    static void appendCodePoint(StringBuilder builder, int codePoint) {
        builder.appendCodePoint(codePoint);
    }

    /// Visits each lone code point encoded in a test262 data string.
    ///
    /// @param codePoints the space-separated six-digit hexadecimal code points
    /// @param action the action to perform for each code point
    static void forEachCodePoint(String codePoints, CodePointConsumer action) {
        if (codePoints.isEmpty()) {
            return;
        }

        int start = 0;
        while (start < codePoints.length()) {
            int end = codePoints.indexOf(' ', start);
            if (end < 0) {
                end = codePoints.length();
            }
            action.accept(parseHexCodePoint(codePoints, start, end));
            start = end + 1;
        }
    }

    /// Visits each code point in each range encoded in a test262 data string.
    ///
    /// @param ranges the semicolon-separated inclusive six-digit hexadecimal code point ranges
    /// @param action the action to perform for each code point
    static void forEachRangeCodePoint(String ranges, CodePointConsumer action) {
        if (ranges.isEmpty()) {
            return;
        }

        int start = 0;
        while (start < ranges.length()) {
            int end = ranges.indexOf(';', start);
            if (end < 0) {
                end = ranges.length();
            }

            int separator = ranges.indexOf('-', start);
            int first = parseHexCodePoint(ranges, start, separator);
            int last = parseHexCodePoint(ranges, separator + 1, end);
            for (int codePoint = first; codePoint <= last; codePoint++) {
                action.accept(codePoint);
            }
            start = end + 1;
        }
    }

    /// Parses a hexadecimal code point.
    ///
    /// @param value the string containing the hexadecimal digits
    /// @param start the inclusive start index
    /// @param end the exclusive end index
    /// @return the parsed code point
    static int parseHexCodePoint(String value, int start, int end) {
        return Integer.parseInt(value, start, end, 16);
    }

    /// Formats a code point like test262's `printCodePoint` helper.
    ///
    /// @param codePoint the Unicode code point
    /// @return the formatted code point
    static String formatCodePoint(int codePoint) {
        String hex = Integer.toHexString(codePoint).toUpperCase();
        return "U+" + "0".repeat(Math.max(0, 6 - hex.length())) + hex;
    }

    /// Formats the code points in a string.
    ///
    /// @param string the string
    /// @return the formatted code points
    static String formatCodePoints(String string) {
        StringBuilder builder = new StringBuilder();
        string.codePoints().forEach(codePoint -> {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(formatCodePoint(codePoint));
        });
        return builder.toString();
    }

    /// Consumes a Unicode code point.
    @FunctionalInterface
    private interface CodePointConsumer {
        /// Performs this operation on the given code point.
        ///
        /// @param codePoint the Unicode code point
        void accept(int codePoint);
    }

    /// Asserts that a processed regular-expression element matches every ASCII digit.
    ///
    /// @param regexp the regular-expression element source
    static void assertAsciiDigitsMatch(String regexp) {
        for (char c = '0'; c <= '9'; c++) {
            assertMatches(regexp, Character.toString(c));
        }
    }

    /// Asserts that a processed regular-expression element does not match any ASCII digit.
    ///
    /// @param regexp the regular-expression element source
    static void assertAsciiDigitsDoNotMatch(String regexp) {
        for (char c = '0'; c <= '9'; c++) {
            assertDoesNotMatch(regexp, Character.toString(c));
        }
    }

    /// Asserts that the supported policy accepts a regular-expression element unchanged.
    ///
    /// @param regexp the regular-expression element source
    static void assertSupported(String regexp) {
        assertEquals(regexp, RegExpElementProcessor.SUPPORTED.process(regexp));
    }

    /// Asserts that the supported policy translates a regular-expression element.
    ///
    /// @param regexp the regular-expression element source
    /// @param expected the expected Java-compatible source
    static void assertTranslated(String regexp, String expected) {
        assertEquals(expected, RegExpElementProcessor.SUPPORTED.process(regexp));
    }

    /// Asserts that the supported policy rejects a regular-expression element.
    ///
    /// @param regexp the regular-expression element source
    static void assertUnsupported(String regexp) {
        assertThrows(WebURLPatternSyntaxException.class, () -> RegExpElementProcessor.SUPPORTED.process(regexp));
    }

    /// Asserts that component compilation rejects a regular-expression element.
    ///
    /// @param regexp the regular-expression element source
    static void assertCompileUnsupported(String regexp) {
        assertThrows(WebURLPatternSyntaxException.class, () -> compileSupportedComponent(regexp));
    }

    /// Asserts that a processed regular-expression element matches the whole input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the input
    static void assertMatches(String regexp, String input) {
        assertTrue(compileProcessed(regexp).matcher(input).matches(), () -> regexp + " should match " + input);
    }

    /// Asserts that a processed regular-expression element does not match the whole input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the input
    static void assertDoesNotMatch(String regexp, String input) {
        assertFalse(compileProcessed(regexp).matcher(input).matches(), () -> regexp + " should not match " + input);
    }

    /// Asserts that a processed regular-expression element can be found in the input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the input
    /// @param expected the expected match
    static void assertFinds(String regexp, String input, String expected) {
        assertFinds(regexp, input, expected, 0);
    }

    /// Asserts that a processed regular-expression element can be found in the input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the input
    /// @param expected the expected match
    /// @param flags Java regular-expression flags
    static void assertFinds(String regexp, String input, String expected, int flags) {
        Matcher matcher = compileProcessed(regexp, flags).matcher(input);
        assertTrue(matcher.find(), () -> regexp + " should be found in " + input);
        assertEquals(expected, matcher.group());
    }

    /// Asserts that a processed regular-expression element cannot be found in the input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the input
    static void assertDoesNotFind(String regexp, String input) {
        assertFalse(compileProcessed(regexp).matcher(input).find(), () -> regexp + " should not be found in " + input);
    }

    /// Compiles a processed regular-expression element.
    ///
    /// @param regexp the regular-expression element source
    /// @return the Java regular expression
    static Pattern compileProcessed(String regexp) {
        return compileProcessed(regexp, 0);
    }

    /// Compiles a processed regular-expression element.
    ///
    /// @param regexp the regular-expression element source
    /// @param flags Java regular-expression flags
    /// @return the Java regular expression
    static Pattern compileProcessed(String regexp, int flags) {
        return Pattern.compile(RegExpElementProcessor.SUPPORTED.process(regexp), flags);
    }

    /// Compiles a single named regular-expression component with the supported policy.
    ///
    /// @param regexp the regular-expression element source
    /// @return the compiled component
    static PatternComponent compileSupportedComponent(String regexp) {
        return PatternComponent.compile(":value(" + regexp + ")", value -> value,
                PatternOptions.DEFAULT.withRegExpPolicy(WebURLPatternParser.RegExpPolicy.SUPPORTED));
    }
}
