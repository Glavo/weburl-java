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

/// Tests test262 named capturing group cases for `ECMAScriptRegExpProcessor`.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorTest262NamedGroupsTest {
    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/non-unicode-match.js
    @Test
    public void asciiNamedCapturingGroupSyntax() {
        assertTranslated("(?<a>a)", "(?:a)");
        assertTranslated("(?<a42>a)", "(?:a)");
        assertTranslated("(?<_>a)", "(?:a)");
        assertTranslated("(?<$>a)", "(?:a)");
        assertFinds("(?<a>a)", "bab", "a");
        assertFinds("(?<a42>a)", "bab", "a");
        assertFinds("(?<_>a)", "bab", "a");
        assertFinds("(?<$>a)", "bab", "a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/non-unicode-references.js
    @Disabled("Named backreferences are not supported")
    @Test
    public void namedBackreferences() {
        assertFinds("(?<b>.)\\k<b>", "bab", "bab");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/unicode-property-names-valid.js
    @Disabled("Non-ASCII and escaped Unicode group names are not supported")
    @Test
    public void unicodeNamedGroupNames() {
        assertFinds("(?<" + "\u72f8" + ">fox)", "fox", "fox");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-exec.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesExec() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
        assertTranslated("(?<x>b)|(?<x>a)", "(?:b)|(?:a)");
        assertUnsupported("(?:(?<x>a)|(?<x>b))\\k<x>");
        assertUnsupported("(?:(?:(?<x>a)|(?<x>b))\\k<x>){2}");
        assertUnsupported("^(?:(?<a>x)|(?<a>y)|z)\\k<a>$");
        assertUnsupported("(?<a>x)|(?:zy\\k<a>)");
        assertUnsupported("^(?:(?<a>x)|(?<a>y)|z){2}\\k<a>$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-group-property-enumeration-order.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesGroupPropertyEnumerationOrder() {
        assertTranslated("(?<y>a)(?<x>a)|(?<x>b)(?<y>b)", "(?:a)(?:a)|(?:b)(?:b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-match-indices.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesMatchIndices() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-match.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesMatch() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
        assertTranslated("(?<x>b)|(?<x>a)", "(?:b)|(?:a)");
        assertUnsupported("(?:(?<x>a)|(?<x>b))\\k<x>");
        assertUnsupported("(?:(?:(?<x>a)|(?<x>b))\\k<x>){2}");
        assertUnsupported("^(?:(?<a>x)|(?<a>y)|z)\\k<a>$");
        assertUnsupported("(?<a>x)|(?:zy\\k<a>)");
        assertUnsupported("^(?:(?<a>x)|(?<a>y)|z){2}\\k<a>$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-matchall.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesMatchall() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
        assertTranslated("(?<x>b)|(?<x>a)", "(?:b)|(?:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-replace.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesReplace() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-replaceall.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesReplaceall() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-search.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesSearch() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-split.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesSplit() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/duplicate-names-test.js
    @Test
    @Disabled("Duplicate named-group semantics are not implemented")
    public void test262NamedGroupsDuplicateNamesTest() {
        assertTranslated("(?<x>a)|(?<x>b)", "(?:a)|(?:b)");
        assertTranslated("(?<x>b)|(?<x>a)", "(?:b)|(?:a)");
        assertUnsupported("(?:(?<x>a)|(?<x>b))\\k<x>");
        assertUnsupported("(?:(?:(?<x>a)|(?<x>b))\\k<x>){2}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/functional-replace-global.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsFunctionalReplaceGlobal() {
        assertTranslated("(?<fst>.)(?<snd>.)", "(?:.)(?:.)");
        assertTranslated("(?<fst>.)|(?<snd>.)", "(?:.)|(?:.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/functional-replace-non-global.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsFunctionalReplaceNonGlobal() {
        assertTranslated("(?<fst>.)(?<snd>.)", "(?:.)(?:.)");
        assertTranslated("(?<fst>.)|(?<snd>.)", "(?:.)|(?:.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/groups-object-subclass-sans.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsGroupsObjectSubclassSans() {
        assertTranslated("(?<a>a)", "(?:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/groups-object-subclass.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsGroupsObjectSubclass() {
        assertTranslated("(?<a>a)", "(?:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/groups-object-undefined.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsGroupsObjectUndefined() {
        assertUnsupported(".");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/groups-object-unmatched.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsGroupsObjectUnmatched() {
        assertTranslated("(?<a>a).|(?<x>x)", "(?:a).|(?:x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/groups-object.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsGroupsObject() {
        assertTranslated("(?<x>.)", "(?:.)");
        assertTranslated("(?<__proto__>.)", "(?:.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/groups-properties.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsGroupsProperties() {
        assertTranslated("(?<fst>.)|(?<snd>.)", "(?:.)|(?:.)");
        assertTranslated("(?<x>.)", "(?:.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/lookbehind.js
    @Test
    @Disabled("Named-group capture result semantics inside lookbehind are not modeled")
    public void test262NamedGroupsLookbehind() {
        assertUnsupported("(?<=(?<a>\\w){3})f");
        assertUnsupported("(?<=(?<a>\\w){4})f");
        assertUnsupported("(?<=(?<a>\\w)+)f");
        assertUnsupported("(?<=(?<a>\\w){6})f");
        assertUnsupported("((?<=\\w{3}))f");
        assertTranslated("(?<a>(?<=\\w{3}))f", "(?:(?<=\\w{3}))f");
        assertUnsupported("(?<!(?<a>\\d){3})f");
        assertUnsupported("(?<!(?<a>\\D){3})f");
        assertUnsupported("(?<!(?<a>\\D){3})f|f");
        assertTranslated("(?<a>(?<!\\D{3}))f|f", "(?:(?<!\\D{3}))f|f");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/non-unicode-property-names-invalid.js
    @Test
    @Disabled("Non-ASCII and escaped Unicode group names are not supported")
    public void test262NamedGroupsNonUnicodePropertyNamesInvalid() {
        assertUnsupported("(?<\ud83e\udd8a>fox)");
        assertUnsupported("(?<\ud83d\udc15>dog)");
        assertUnsupported("(?<\ud83d \udc15>dog)");
        assertUnsupported("(?<\ud835\udfdathe>the)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/non-unicode-property-names-valid.js
    @Test
    @Disabled("Non-ASCII and escaped Unicode group names are not supported")
    public void test262NamedGroupsNonUnicodePropertyNamesValid() {
        assertTranslated("(?<animal>fox|dog)", "(?:fox|dog)");
        assertUnsupported("(?<\ud835\udc53\ud835\udc5c\ud835\udc65>fox).*(?<\ud835\udcd3\ud835\udcf8\ud835\udcf0>dog)");
        assertUnsupported("(?<\u72f8>fox).*(?<\u72d7>dog)");
        assertUnsupported("(?<\ud835\udcd1\ud835\udcfb\ud835\udcf8\ud835\udd00\ud835\udcf7>brown)");
        assertUnsupported("(?<\\u{1d4d1}\\u{1d4fb}\\u{1d4f8}\\u{1d500}\\u{1d4f7}>brown)");
        assertTranslated("(?<\\ud835\\udcd1\\ud835\\udcfb\\ud835\\udcf8\\ud835\\udd00\\ud835\\udcf7>brown)", "(?:brown)");
        assertUnsupported("(?<\ud835\uddb0\ud835\udda1\ud835\udda5>q\\w*\\W\\w*\\W\\w*)");
        assertUnsupported("(?<\ud835\uddb0\ud835\udda1\\u{1d5a5}>q\\w*\\W\\w*\\W\\w*)");
        assertUnsupported("(?<\ud835\uddb0\\u{1d5a1}\ud835\udda5>q\\w*\\W\\w*\\W\\w*)");
        assertUnsupported("(?<\ud835\uddb0\\u{1d5a1}\\u{1d5a5}>q\\w*\\W\\w*\\W\\w*)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/non-unicode-property-names.js
    @Test
    @Disabled("Non-ASCII and escaped Unicode group names are not supported")
    public void test262NamedGroupsNonUnicodePropertyNames() {
        assertUnsupported("(?<\u03c0>a)");
        assertTranslated("(?<$>a)", "(?:a)");
        assertTranslated("(?<_>a)", "(?:a)");
        assertTranslated("(?<_\\u200C>a)", "(?:a)");
        assertTranslated("(?<_\\u200D>a)", "(?:a)");
        assertUnsupported("(?<\u0ca0_\u0ca0>a)");
        assertUnsupported("(?<\\u0041>.)");
        assertTranslated("(?<A>.)", "(?:.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-escaped.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceEscaped() {
        assertTranslated("(?<fst>.)", "(?:.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-get.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceGet() {
        assertTranslated("(?<fst>.)(?<snd>.)|(?<thd>x)", "(?:.)(?:.)|(?:x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-missing.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceMissing() {
        assertTranslated("(?<fst>.)(?<snd>.)|(?<thd>x)", "(?:.)(?:.)|(?:x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-nocaptures.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceNocaptures() {
        assertUnsupported("(.)(.)|(x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-numbered.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceNumbered() {
        assertTranslated("(?<fst>.)(?<snd>.)|(?<thd>x)", "(?:.)(?:.)|(?:x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-unclosed.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceUnclosed() {
        assertTranslated("(?<fst>.)(?<snd>.)|(?<thd>x)", "(?:.)(?:.)|(?:x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/string-replace-undefined.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsStringReplaceUndefined() {
        assertTranslated("(?<fst>.)(?<snd>.)|(?<thd>x)", "(?:.)(?:.)|(?:x)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/unicode-match.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsUnicodeMatch() {
        assertTranslated("(?<a>a)", "(?:a)");
        assertTranslated("(?<a42>a)", "(?:a)");
        assertTranslated("(?<_>a)", "(?:a)");
        assertTranslated("(?<$>a)", "(?:a)");
        assertTranslated(".(?<$>a).", ".(?:a).");
        assertUnsupported(".(?<a>a)(.)");
        assertTranslated(".(?<a>a)(?<b>.)", ".(?:a)(?:.)");
        assertTranslated(".(?<a>\\w\\w)", ".(?:\\w\\w)");
        assertTranslated("(?<a>\\w\\w\\w)", "(?:\\w\\w\\w)");
        assertTranslated("(?<a>\\w\\w)(?<b>\\w)", "(?:\\w\\w)(?:\\w)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/unicode-property-names-invalid.js
    @Test
    @Disabled("Non-ASCII and escaped Unicode group names are not supported")
    public void test262NamedGroupsUnicodePropertyNamesInvalid() {
        assertUnsupported("(?<\ud83e\udd8a>fox)");
        assertUnsupported("(?<\ud83d\udc15>dog)");
        assertUnsupported("(?<\ud83d \udc15>dog)");
        assertUnsupported("(?<\ud835\udfdathe>the)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/unicode-property-names.js
    @Test
    @Disabled("Non-ASCII and escaped Unicode group names are not supported")
    public void test262NamedGroupsUnicodePropertyNames() {
        assertUnsupported("(?<\u03c0>a)");
        assertUnsupported("(?<\\u{03C0}>a)");
        assertTranslated("(?<$>a)", "(?:a)");
        assertTranslated("(?<_>a)", "(?:a)");
        assertUnsupported("(?<$\ud801\udca4>a)");
        assertTranslated("(?<_\\u200C>a)", "(?:a)");
        assertTranslated("(?<_\\u200D>a)", "(?:a)");
        assertUnsupported("(?<\u0ca0_\u0ca0>a)");
        assertTranslated("(?<a\\uD801\\uDCA4>.)", "(?:.)");
        assertUnsupported("(?<\\u0041>.)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/named-groups/unicode-references.js
    @Test
    @Disabled("JavaScript named-groups runtime result and replacement behavior is outside ECMAScriptRegExpProcessor")
    public void test262NamedGroupsUnicodeReferences() {
        assertUnsupported("(?<b>.).\\k<b>");
        assertUnsupported("(?<a>\\k<a>\\w)..");
        assertUnsupported("\\k<a>(?<a>b)\\w\\k<a>");
        assertUnsupported("(?<b>b)\\k<a>(?<a>a)\\k<b>");
        assertUnsupported("\\k<a>(?<a>b)\\w\\k<a>");
        assertUnsupported("(?<b>b)\\k<a>(?<a>a)\\k<b>");
        assertUnsupported("(?<a>a)(?<b>b)\\k<a>");
        assertUnsupported("(?<a>a)(?<b>b)\\k<a>|(?<c>c)");
    }
}
