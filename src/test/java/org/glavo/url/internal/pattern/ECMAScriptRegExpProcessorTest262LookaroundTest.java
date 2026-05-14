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
    @Disabled("Lookbehind assertions are not supported")
    @Test
    public void lookbehindSimpleFixedLength() {
        assertFinds("(?<=a)b", "ab", "b");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/alternations.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindAlternations() {
        assertUnsupported(".*(?<=(..|...|....))(.*)");
        assertUnsupported(".*(?<=(xx|...|....))(.*)");
        assertUnsupported(".*(?<=(xx|...))(.*)");
        assertUnsupported(".*(?<=(xx|xxx))(.*)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/back-references-to-captures.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindBackReferencesToCaptures() {
        assertUnsupported("(?<=\\1(\\w))d");
        assertUnsupported("(?<=\\1([abx]))d");
        assertUnsupported("(?<=\\1(\\w+))c");
        assertUnsupported("(?<=(\\w+)\\1)c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/back-references.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
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
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindCapturesNegative() {
        assertUnsupported("(?<!(^|[ab]))\\w{2}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/captures.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindCaptures() {
        assertUnsupported("(?<=(c))def");
        assertUnsupported("(?<=(\\w{2}))def");
        assertUnsupported("(?<=(\\w(\\w)))def");
        assertUnsupported("(?<=(\\w){3})def");
        assertUnsupported("(?<=(bc)|(cd)).");
        assertUnsupported("(?<=([ab]{1,2})\\D|(abc))\\w");
        assertUnsupported("\\D(?<=([ab]+))(\\w)");
        assertUnsupported("(?<=b|c)\\w");
        assertUnsupported("(?<=[b-e])\\w{2}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/do-not-backtrack.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindDoNotBacktrack() {
        assertUnsupported("(?<=([abc]+)).\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/greedy-loop.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindGreedyLoop() {
        assertUnsupported("(?<=(b+))c");
        assertUnsupported("(?<=(b\\d+))c");
        assertUnsupported("(?<=((?:b\\d{2})+))c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/misc.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindMisc() {
        assertUnsupported("(?<=$abc)def");
        assertUnsupported("^f.o(?<=foo)$");
        assertUnsupported("^foo(?<!foo)$");
        assertUnsupported("^f.o(?<!foo)$");
        assertUnsupported("^foo(?<=foo)$");
        assertUnsupported("^foooo(?<=fo+)$");
        assertUnsupported("^foooo(?<=fo*)$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/mutual-recursive.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindMutualRecursive() {
        assertUnsupported("(?<=a(.\\2)b(\\1)).{4}");
        assertUnsupported("(?<=a(\\2)b(..\\1))b");
        assertUnsupported("(?<=(?:\\1b)(aa)).");
        assertUnsupported("(?<=(?:\\1|b)(aa)).");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/negative.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindNegative() {
        assertUnsupported("(?<!abc)\\w\\w\\w");
        assertUnsupported("(?<!a.c)\\w\\w\\w");
        assertUnsupported("(?<!a\\wc)\\w\\w\\w");
        assertUnsupported("(?<!a[a-z])\\w\\w\\w");
        assertUnsupported("(?<!a[a-z]{2})\\w\\w\\w");
        assertUnsupported("(?<!abc)def");
        assertUnsupported("(?<!a.c)def");
        assertUnsupported("(?<!a\\wc)def");
        assertUnsupported("(?<!a[a-z][a-z])def");
        assertUnsupported("(?<!a[a-z]{2})def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/nested-lookaround.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindNestedLookaround() {
        assertUnsupported("(?<=ab(?=c)\\wd)\\w\\w");
        assertUnsupported("(?<=a(?=([^a]{2})d)\\w{3})\\w\\w");
        assertUnsupported("(?<=a(?=([bc]{2}(?<!a{2}))d)\\w{3})\\w\\w");
        assertUnsupported("^faaao?(?<=^f[oa]+(?=o))");
        assertUnsupported("(?<=a(?=([bc]{2}(?<!a*))d)\\w{3})\\w\\w");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/sliced-strings.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindSlicedStrings() {
        assertUnsupported("(?=(abcdefghijklmn))(?<=\\1)a");
        assertUnsupported("(?=(abcdefghijklmn))(?<=\\1)a");
        assertUnsupported("(?=(abcdefg))(?<=\\1)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/start-of-line.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindStartOfLine() {
        assertUnsupported("(?<=^[^a-c]{3})def");
        assertUnsupported("\"^foooo(?<=^o+)$");
        assertUnsupported("\"^foooo(?<=^o*)$");
        assertUnsupported("(?<=^abc)def");
        assertUnsupported("(?<=^[a-c]{3})def");
        assertUnsupported("(?<=^[a-c]{3})def");
        assertUnsupported("(?<=^)\\w+");
        assertUnsupported("\\w+(?<=$)");
        assertUnsupported("(?<=^)\\w+(?<=$)");
        assertUnsupported("^foo(?<=^fo+)$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/sticky.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindSticky() {
        assertUnsupported("(?<=^(\\w+))def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/variable-length.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindVariableLength() {
        assertUnsupported("(?<=[a|b|c]*)[^a|b|c]{3}");
        assertUnsupported("(?<=\\w*)[^a|b|c]{3}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/lookBehind/word-boundary.js
    @Test
    @Disabled("Lookbehind assertions are not supported")
    public void test262LookBehindWordBoundary() {
        assertUnsupported("(?<=\\b)[d-f]{3}");
        assertUnsupported("(?<=\\B)\\w{3}");
        assertUnsupported("(?<=\\B)(?<=c(?<=\\w))\\w{3}");
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
