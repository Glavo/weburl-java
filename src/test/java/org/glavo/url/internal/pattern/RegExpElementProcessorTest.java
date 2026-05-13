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

import org.glavo.url.internal.IndexRanges;
import org.glavo.url.pattern.WebURLPatternParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `RegExpElementProcessor`.
@NotNullByDefault
public final class RegExpElementProcessorTest {
    /// Tests policy lookup.
    @Test
    public void returnsProcessorForPolicy() {
        assertSame(RegExpElementProcessor.SUPPORTED,
                RegExpElementProcessor.forPolicy(WebURLPatternParser.RegExpPolicy.SUPPORTED));
        assertSame(RegExpElementProcessor.REJECT,
                RegExpElementProcessor.forPolicy(WebURLPatternParser.RegExpPolicy.REJECT));
        assertSame(RegExpElementProcessor.JAVA,
                RegExpElementProcessor.forPolicy(WebURLPatternParser.RegExpPolicy.JAVA));
    }

    /// Tests regular-expression syntax accepted by the supported subset.
    @Test
    public void supportedPolicyAcceptsSupportedSubset() {
        assertSupported("abc");
        assertSupported(".");
        assertSupported("a|b");
        assertSupported("[a-z]");
        assertSupported("[^abc]");
        assertSupported("[\\d\\D\\w\\W\\-]");
        assertSupported("\\d+\\D*\\w?\\W{2}");
        assertSupported("\\n\\r\\t\\f");
        assertSupported("\\.\\+\\*\\?\\^\\$\\{\\}\\(\\)\\|\\[\\]\\\\\\/");
        assertSupported("a{1}b{2,}c{3,5}?");
        assertSupported("a+?");
    }

    /// Tests literal, wildcard, and alternative matching with the supported policy.
    @Test
    public void supportedPolicyMatchesLiteralsWildcardsAndAlternatives() {
        assertMatches("abc", "abc");
        assertDoesNotMatch("abc", "ab");
        assertDoesNotMatch("abc", "abcd");

        assertMatches(".", "a");
        assertMatches(".", "-");
        assertDoesNotMatch(".", "");
        assertDoesNotMatch(".", "ab");
        assertDoesNotMatch(".", "\n");

        assertMatches("cat|dog", "cat");
        assertMatches("cat|dog", "dog");
        assertDoesNotMatch("cat|dog", "cow");
        assertDoesNotMatch("cat|dog", "catdog");
    }

    /// Tests character class matching with the supported policy.
    @Test
    public void supportedPolicyMatchesCharacterClasses() {
        assertMatches("[a-z]", "m");
        assertDoesNotMatch("[a-z]", "M");
        assertDoesNotMatch("[a-z]", "0");

        assertMatches("[^abc]", "d");
        assertMatches("[^abc]", "0");
        assertDoesNotMatch("[^abc]", "a");

        assertMatches("[a-c][^0-9]", "az");
        assertMatches("[a-c][^0-9]", "c_");
        assertDoesNotMatch("[a-c][^0-9]", "d_");
        assertDoesNotMatch("[a-c][^0-9]", "a5");
    }

    /// Tests escape matching with the supported policy.
    @Test
    public void supportedPolicyMatchesEscapes() {
        assertMatches("\\d+", "123");
        assertDoesNotMatch("\\d+", "abc");

        assertMatches("\\D+", "abc");
        assertDoesNotMatch("\\D+", "123");

        assertMatches("\\w+", "a_Z9");
        assertDoesNotMatch("\\w+", "-");

        assertMatches("\\W+", "-!");
        assertDoesNotMatch("\\W+", "a");

        assertMatches("\\n\\r\\t\\f", "\n\r\t\f");
        assertDoesNotMatch("\\n\\r\\t\\f", "nr");

        assertMatches("\\.\\+\\*\\?\\^\\$\\{\\}\\(\\)\\|\\[\\]\\\\\\/", ".+*?^${}()|[]\\/");
        assertDoesNotMatch("\\.\\+\\*\\?\\^\\$\\{\\}\\(\\)\\|\\[\\]\\\\\\/", ".+*?^${}()|[]/");
    }

    /// Tests quantifier matching with the supported policy.
    @Test
    public void supportedPolicyMatchesQuantifiers() {
        assertMatches("a*", "");
        assertMatches("a*", "aaa");
        assertDoesNotMatch("a*", "b");

        assertMatches("a+", "a");
        assertMatches("a+", "aaa");
        assertDoesNotMatch("a+", "");

        assertMatches("a?", "");
        assertMatches("a?", "a");
        assertDoesNotMatch("a?", "aa");

        assertMatches("a{2}", "aa");
        assertDoesNotMatch("a{2}", "a");
        assertDoesNotMatch("a{2}", "aaa");

        assertMatches("a{2,}", "aa");
        assertMatches("a{2,}", "aaaa");
        assertDoesNotMatch("a{2,}", "a");

        assertMatches("a{2,4}", "aa");
        assertMatches("a{2,4}", "aaaa");
        assertDoesNotMatch("a{2,4}", "a");
        assertDoesNotMatch("a{2,4}", "aaaaa");

        assertMatches("a+?", "aaa");
        assertMatches("a{2,4}?", "aaa");
    }

    /// Tests regular-expression syntax rejected by the supported subset.
    @Test
    public void supportedPolicyRejectsUnsupportedSyntax() {
        assertUnsupported("(ab)");
        assertUnsupported("(?:ab)");
        assertUnsupported("(?=ab)");
        assertUnsupported("(?<=ab)");
        assertUnsupported("^abc");
        assertUnsupported("abc$");
        assertUnsupported("\\b");
        assertUnsupported("\\p{ASCII}");
        assertUnsupported("\\u0061");
        assertUnsupported("\\1");
        assertUnsupported("a*+");
        assertUnsupported("a**");
        assertUnsupported("a{2,1}");
        assertUnsupported("a{}");
        assertUnsupported("[]");
        assertUnsupported("[a&&b]");
        assertUnsupported("[a[b]");
        assertUnsupported("\\");
    }

    /// Tests that the reject policy rejects every custom regular-expression element.
    @Test
    public void rejectPolicyRejectsEveryRegularExpressionElement() {
        assertThrows(WebURLPatternSyntaxException.class, () -> RegExpElementProcessor.REJECT.process("abc"));
        assertThrows(WebURLPatternSyntaxException.class, () -> RegExpElementProcessor.REJECT.process("[a-z]+"));
    }

    /// Tests Java policy capture-group validation.
    @Test
    public void javaPolicyRejectsCapturingGroups() {
        assertJava("(?:ab)");
        assertJava("(?=ab)");
        assertJava("(?<=ab)");
        assertJava("(?<!ab)");
        assertJava("\\(ab\\)");
        assertJava("[(]");

        assertThrows(WebURLPatternSyntaxException.class, () -> RegExpElementProcessor.JAVA.process("(ab)"));
        assertThrows(WebURLPatternSyntaxException.class, () -> RegExpElementProcessor.JAVA.process("(?<name>ab)"));
    }

    /// Asserts that the supported policy accepts a regular-expression element unchanged.
    ///
    /// @param regexp the regular-expression element source
    private static void assertSupported(String regexp) {
        assertEquals(regexp, RegExpElementProcessor.SUPPORTED.process(regexp));
    }

    /// Asserts that the supported policy rejects a regular-expression element.
    ///
    /// @param regexp the regular-expression element source
    private static void assertUnsupported(String regexp) {
        assertThrows(WebURLPatternSyntaxException.class, () -> RegExpElementProcessor.SUPPORTED.process(regexp));
    }

    /// Asserts that the Java policy accepts a regular-expression element unchanged.
    ///
    /// @param regexp the regular-expression element source
    private static void assertJava(String regexp) {
        assertEquals(regexp, RegExpElementProcessor.JAVA.process(regexp));
    }

    /// Asserts that the supported policy matches an input and captures the whole input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the component input
    private static void assertMatches(String regexp, String input) {
        PatternComponent component = compileSupportedComponent(regexp);
        assertTrue(component.test(input), () -> regexp + " should match " + input);

        WebURLPatternEngine.ComponentMatch match = component.match(input);
        assertNotNull(match);
        assertEquals(input, IndexRanges.substring(match.input(), match.range()));

        long[] groupRanges = match.groupRanges();
        assertEquals(1, groupRanges.length);
        assertEquals(input, IndexRanges.substring(match.input(), groupRanges[0]));
        assertEquals(0, match.groupIndexes().get("value"));
    }

    /// Asserts that the supported policy does not match an input.
    ///
    /// @param regexp the regular-expression element source
    /// @param input the component input
    private static void assertDoesNotMatch(String regexp, String input) {
        PatternComponent component = compileSupportedComponent(regexp);
        assertFalse(component.test(input), () -> regexp + " should not match " + input);
        assertNull(component.match(input));
    }

    /// Compiles a single named regular-expression component with the supported policy.
    ///
    /// @param regexp the regular-expression element source
    /// @return the compiled component
    private static PatternComponent compileSupportedComponent(String regexp) {
        return PatternComponent.compile(":value(" + regexp + ")", value -> value,
                PatternOptions.DEFAULT.withRegExpPolicy(WebURLPatternParser.RegExpPolicy.SUPPORTED));
    }
}
