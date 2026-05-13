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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
