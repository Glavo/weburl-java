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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorTest262Support.*;

/// Tests test262 lookaround cases for `ECMAScriptRegExpProcessor`.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorTest262LookaroundTest {
    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/simple-fixed-length.js
    @Test
    public void lookbehindSimpleFixedLength() {
        assertDoesNotFind("^.(?<=a)", "b");
        assertDoesNotFind("^f\\w\\w(?<=\\woo)", "boo");
        assertDoesNotFind("^f\\w\\w(?<=\\woo)", "fao");
        assertDoesNotFind("^f\\w\\w(?<=\\woo)", "foa");

        assertFinds("^.(?<=a)", "a", "a");
        assertFinds("^f..(?<=.oo)", "foo1", "foo");
        assertFinds("^f\\w\\w(?<=\\woo)", "foo2", "foo");
        assertFinds("(?<=abc)\\w\\w\\w", "abcdef", "def");
        assertFinds("(?<=a.c)\\w\\w\\w", "abcdef", "def");
        assertFinds("(?<=a\\wc)\\w\\w\\w", "abcdef", "def");
        assertFinds("(?<=a[a-z])\\w\\w\\w", "abcdef", "cde");
        assertFinds("(?<=a[a-z][a-z])\\w\\w\\w", "abcdef", "def");
        assertFinds("(?<=a[a-z]{2})\\w\\w\\w", "abcdef", "def");
        assertFinds("(?<=a{1})\\w\\w\\w", "abcdef", "bcd");
        assertFinds("(?<=a{1}b{1})\\w\\w\\w", "abcdef", "cde");
        assertFinds("(?<=a{1}[a-z]{2})\\w\\w\\w", "abcdef", "def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/alternations.js
    @Test
    @Disabled("Capturing groups inside regular-expression elements are not supported")
    public void test262LookBehindAlternations() {
        assertUnsupported(".*(?<=(..|...|....))(.*)");
        assertUnsupported(".*(?<=(xx|...|....))(.*)");
        assertUnsupported(".*(?<=(xx|...))(.*)");
        assertUnsupported(".*(?<=(xx|xxx))(.*)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/back-references-to-captures.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindBackReferencesToCaptures() {
        assertUnsupported("(?<=\\1(\\w))d");
        assertUnsupported("(?<=\\1([abx]))d");
        assertUnsupported("(?<=\\1(\\w+))c");
        assertUnsupported("(?<=(\\w+)\\1)c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/back-references.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindBackReferences() {
        assertUnsupported("(.)(?<=(\\1\\1))");
        assertUnsupported("(.)(?<=(\\1\\1))");
        assertUnsupported("((\\w)\\w)(?<=\\1\\2\\1)");
        assertUnsupported("(\\w(\\w))(?<=\\1\\2\\1)");
        assertUnsupported("(?=(\\w))(?<=(\\1)).");
        assertUnsupported("(?<=(.))(\\w+)(?=\\1)");
        assertUnsupported("(.)(?<=\\1\\1\\1)");
        assertUnsupported("(..)(?<=\\1\\1\\1)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/captures-negative.js
    @Test
    @Disabled("Capturing groups inside regular-expression elements are not supported")
    public void test262LookBehindCapturesNegative() {
        assertUnsupported("(?<!(^|[ab]))\\w{2}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/captures.js
    @Test
    @Disabled("Capturing groups inside regular-expression elements are not supported")
    public void test262LookBehindCaptures() {
        assertUnsupported("(?<=(c))def");
        assertUnsupported("(?<=(\\w{2}))def");
        assertUnsupported("(?<=(\\w(\\w)))def");
        assertUnsupported("(?<=(\\w){3})def");
        assertUnsupported("(?<=(bc)|(cd)).");
        assertUnsupported("(?<=([ab]{1,2})\\D|(abc))\\w");
        assertUnsupported("\\D(?<=([ab]+))(\\w)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/captures.js
    @Test
    public void test262LookBehindCapturesNonCapturingMatches() {
        assertFindsAll("(?<=b|c)\\w", "abcdef", "c", "d");
        assertFindsAll("(?<=[b-e])\\w{2}", "abcdef", "cd", "ef");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/do-not-backtrack.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindDoNotBacktrack() {
        assertUnsupported("(?<=([abc]+)).\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/greedy-loop.js
    @Test
    @Disabled("Capturing groups inside regular-expression elements are not supported")
    public void test262LookBehindGreedyLoop() {
        assertUnsupported("(?<=(b+))c");
        assertUnsupported("(?<=(b\\d+))c");
        assertUnsupported("(?<=((?:b\\d{2})+))c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/misc.js
    @Test
    public void test262LookBehindMisc() {
        assertDoesNotFind("(?<=$abc)def", "abcdef");
        assertDoesNotFind("^f.o(?<=foo)$", "fno");
        assertDoesNotFind("^foo(?<!foo)$", "foo");
        assertDoesNotFind("^f.o(?<!foo)$", "foo");

        assertFinds("^foo(?<=foo)$", "foo", "foo");
        assertFinds("^f.o(?<=foo)$", "foo", "foo");
        assertFinds("^f.o(?<!foo)$", "fno", "fno");
        assertFinds("^foooo(?<=fo+)$", "foooo", "foooo");
        assertFinds("^foooo(?<=fo*)$", "foooo", "foooo");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/misc.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindMiscBackReferences() {
        assertUnsupported("(abc\\1)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/mutual-recursive.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindMutualRecursive() {
        assertUnsupported("(?<=a(.\\2)b(\\1)).{4}");
        assertUnsupported("(?<=a(\\2)b(..\\1))b");
        assertUnsupported("(?<=(?:\\1b)(aa)).");
        assertUnsupported("(?<=(?:\\1|b)(aa)).");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/negative.js
    @Test
    public void test262LookBehindNegative() {
        assertFinds("(?<!abc)\\w\\w\\w", "abcdef", "abc");
        assertFinds("(?<!a.c)\\w\\w\\w", "abcdef", "abc");
        assertFinds("(?<!a\\wc)\\w\\w\\w", "abcdef", "abc");
        assertFinds("(?<!a[a-z])\\w\\w\\w", "abcdef", "abc");
        assertFinds("(?<!a[a-z]{2})\\w\\w\\w", "abcdef", "abc");
        assertDoesNotFind("(?<!abc)def", "abcdef");
        assertDoesNotFind("(?<!a.c)def", "abcdef");
        assertDoesNotFind("(?<!a\\wc)def", "abcdef");
        assertDoesNotFind("(?<!a[a-z][a-z])def", "abcdef");
        assertDoesNotFind("(?<!a[a-z]{2})def", "abcdef");
        assertDoesNotFind("(?<!a{1}b{1})cde", "abcdef");
        assertDoesNotFind("(?<!a{1}[a-z]{2})def", "abcdef");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/nested-lookaround.js
    @Test
    public void test262LookBehindNestedLookaround() {
        assertFinds("(?<=ab(?=c)\\wd)\\w\\w", "abcdef", "ef");
        assertFinds("^faaao?(?<=^f[oa]+(?=o))", "faaao", "faaa");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/nested-lookaround.js
    @Test
    @Disabled("Capturing groups inside regular-expression elements are not supported")
    public void test262LookBehindNestedLookaroundCaptures() {
        assertUnsupported("(?<=a(?=([^a]{2})d)\\w{3})\\w\\w");
        assertUnsupported("(?<=a(?=([bc]{2}(?<!a{2}))d)\\w{3})\\w\\w");
        assertUnsupported("(?<=a(?=([bc]{2}(?<!a*))d)\\w{3})\\w\\w");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/sliced-strings.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindSlicedStrings() {
        assertUnsupported("(?=(abcdefghijklmn))(?<=\\1)a");
        assertUnsupported("(?=(abcdefghijklmn))(?<=\\1)a");
        assertUnsupported("(?=(abcdefg))(?<=\\1)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/start-of-line.js
    @Test
    public void test262LookBehindStartOfLine() {
        assertDoesNotFind("(?<=^[^a-c]{3})def", "abcdef");
        assertDoesNotFind("\"^foooo(?<=^o+)$", "foooo");
        assertDoesNotFind("\"^foooo(?<=^o*)$", "foooo");

        assertFinds("(?<=^abc)def", "abcdef", "def");
        assertFinds("(?<=^[a-c]{3})def", "abcdef", "def");
        assertFinds("(?<=^[a-c]{3})def", "xyz\nabcdef", "def", Pattern.MULTILINE);
        assertFindsAll("(?<=^)\\w+", "ab\ncd\nefg", Pattern.MULTILINE, "ab", "cd", "efg");
        assertFindsAll("\\w+(?<=$)", "ab\ncd\nefg", Pattern.MULTILINE, "ab", "cd", "efg");
        assertFindsAll("(?<=^)\\w+(?<=$)", "ab\ncd\nefg", Pattern.MULTILINE, "ab", "cd", "efg");

        assertFinds("^foo(?<=^fo+)$", "foo", "foo");
        assertFinds("^foooo(?<=^fo*)", "foooo", "foooo");
        assertFinds("(?<=^\\w+)def", "abcdefdef", "def");
        assertFindsAll("(?<=^\\w+)def", "abcdefdef", "def", "def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/start-of-line.js
    @Test
    @Disabled("Capturing groups and backreferences are not supported")
    public void test262LookBehindStartOfLineCaptures() {
        assertUnsupported("^(f)oo(?<=^\\1o+)$");
        assertUnsupported("^(f)oo(?<=^\\1o+).$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/sticky.js
    @Test
    @Disabled("Capturing groups inside regular-expression elements are not supported")
    public void test262LookBehindSticky() {
        assertUnsupported("(?<=^(\\w+))def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/variable-length.js
    @Test
    public void test262LookBehindVariableLength() {
        assertFinds("(?<=[a|b|c]*)[^a|b|c]{3}", "abcdef", "def");
        assertFinds("(?<=\\w*)[^a|b|c]{3}", "abcdef", "def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/word-boundary.js
    @Test
    public void test262LookBehindWordBoundary() {
        assertFinds("(?<=\\b)[d-f]{3}", "abc def", "def");
        assertFinds("(?<=\\B)\\w{3}", "ab cdef", "def");
        assertFinds("(?<=\\B)(?<=c(?<=\\w))\\w{3}", "ab cdef", "def");
        assertDoesNotFind("(?<=\\b)[d-f]{3}", "abcdef");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookahead-quantifier-match-groups.js
    @Test
    @Disabled("Lookahead assertions and nested capturing groups are not supported")
    public void test262LookaheadQuantifierMatchGroups() {
        assertUnsupported("(?:(?=(abc)))a");
        assertUnsupported("(?:(?=(abc)))?a");
        assertUnsupported("(?:(?=(abc))){1,1}a");
        assertUnsupported("(?:(?=(abc))){0,1}a");
    }
}
