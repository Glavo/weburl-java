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

/// Tests test262 inline regular-expression modifier cases for `ECMAScriptRegExpProcessor`.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorTest262RegExpModifiersTest {
    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase.js
    @Disabled("Inline RegExp modifiers are not supported")
    @Test
    public void regexpModifiersIgnoreCase() {
        assertFinds("(?i:a)", "A", "A");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-code-point-repeat-i-1.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersCodePointRepeatI1() {
        assertUnsupported("(?ii:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-code-point-repeat-i-2.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersCodePointRepeatI2() {
        assertUnsupported("(?imsi:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-arbitrary.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointArbitrary() {
        assertUnsupported("(?1:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-combining-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointCombiningI() {
        assertUnsupported("(?i\u0365:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-combining-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointCombiningM() {
        assertUnsupported("(?m\u036b:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-combining-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointCombiningS() {
        assertUnsupported("(?s\u0300:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-d.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointD() {
        assertUnsupported("(?d:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-g.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointG() {
        assertUnsupported("(?g:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-non-display-1.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointNonDisplay1() {
        assertUnsupported("(?s\u0000:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-non-display-2.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointNonDisplay2() {
        assertUnsupported("(?s\u200e:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-non-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointNonFlag() {
        assertUnsupported("(?Q:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-u.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointU() {
        assertUnsupported("(?u:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-uppercase-I.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointUppercaseI() {
        assertUnsupported("(?I:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-y.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointY() {
        assertUnsupported("(?y:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-zwj.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointZwj() {
        assertUnsupported("(?s\u200d:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-zwnbsp.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointZwnbsp() {
        assertUnsupported("(?s\ufeff:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-other-code-point-zwnj.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersOtherCodePointZwnj() {
        assertUnsupported("(?s\u200c:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-should-not-case-fold-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersShouldNotCaseFoldI() {
        assertUnsupported("(?I:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-should-not-case-fold-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersShouldNotCaseFoldM() {
        assertUnsupported("(?M:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-should-not-case-fold-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersShouldNotCaseFoldS() {
        assertUnsupported("(?S:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-should-not-unicode-case-fold-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersShouldNotUnicodeCaseFoldI() {
        assertUnsupported("(?\u0130:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/early-err-modifiers-should-not-unicode-case-fold-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262EarlyErrModifiersShouldNotUnicodeCaseFoldS() {
        assertUnsupported("(?\u017f:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-dotAll-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddDotAllDoesNotAffectAlternativesOutside() {
        assertUnsupported("a.a|b.b|(?s:c.c)|d.d|e.e");
        assertUnsupported("(a.a)|(?:b.b)|(?s:c.c)|(?:d.d)|(e.e)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-dotAll-does-not-affect-dotAll-property.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddDotAllDoesNotAffectDotAllProperty() {
        assertUnsupported("(?s:)");
        assertUnsupported("(?s-:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-dotAll-does-not-affect-ignoreCase-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddDotAllDoesNotAffectIgnoreCaseFlag() {
        assertUnsupported("(?s:.es)");
        assertUnsupported("(?s:.es)");
        assertUnsupported("(?s-:.es)");
        assertUnsupported("(?s-:.es)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-dotAll-does-not-affect-multiline-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddDotAllDoesNotAffectMultilineFlag() {
        assertUnsupported("(?s:.es$)");
        assertUnsupported("(?s:.es$)");
        assertUnsupported("(?s-:.es$)");
        assertUnsupported("(?s-:.es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-dotAll.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddDotAll() {
        assertUnsupported("(?s:^.$)");
        assertUnsupported("(?s-:^.$)");
        assertUnsupported("a.(?s:b.b).c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-backreferences.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsBackreferences() {
        assertUnsupported("(a)(?i:\\1)");
        assertUnsupported("(a)(?i-:\\1)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-characterClasses.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsCharacterClasses() {
        assertUnsupported("(?i:[ab])c");
        assertUnsupported("(?i-:[ab])c");
        assertUnsupported("(?i:[^ab])c");
        assertUnsupported("(?i-:[^ab])c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-characterEscapes.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsCharacterEscapes() {
        assertUnsupported("(?i:\\x61)b");
        assertUnsupported("(?i:\\u0061)b");
        assertUnsupported("(?i:\\u{0061})b");
        assertUnsupported("(?i-:\\x61)b");
        assertUnsupported("(?i-:\\u0061)b");
        assertUnsupported("(?i-:\\u{0061})b");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-slash-lower-b.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsSlashLowerB() {
        assertUnsupported("(?i:\\b)");
        assertUnsupported("(?i:\\b)");
        assertUnsupported("(?i-:\\b)");
        assertUnsupported("(?i-:\\b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-slash-lower-p.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsSlashLowerP() {
        assertUnsupported("(?i:\\p{Lu})");
        assertUnsupported("(?i-:\\p{Lu})");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-slash-lower-w.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsSlashLowerW() {
        assertUnsupported("(?i:\\w)");
        assertUnsupported("(?i:\\w)");
        assertUnsupported("(?i-:\\w)");
        assertUnsupported("(?i-:\\w)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-slash-upper-b.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsSlashUpperB() {
        assertUnsupported("(?i:Z\\B)");
        assertUnsupported("(?i-:Z\\B)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-slash-upper-p.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsSlashUpperP() {
        assertUnsupported("(?i:\\P{Lu})");
        assertUnsupported("(?i-:\\P{Lu})");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-affects-slash-upper-w.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseAffectsSlashUpperW() {
        assertUnsupported("(?i:\\W)");
        assertUnsupported("(?i-:\\W)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseDoesNotAffectAlternativesOutside() {
        assertUnsupported("a|b|(?i:c)|d|e");
        assertUnsupported("(a)|(?:b)|(?i:c)|(?:d)|(e)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-does-not-affect-dotAll-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseDoesNotAffectDotAllFlag() {
        assertUnsupported("(?i:.es)");
        assertUnsupported("(?i:.es)");
        assertUnsupported("(?i-:.es)");
        assertUnsupported("(?i-:.es)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-does-not-affect-ignoreCase-property.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseDoesNotAffectIgnoreCaseProperty() {
        assertUnsupported("(?i:)");
        assertUnsupported("(?i-:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-ignoreCase-does-not-affect-multiline-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddIgnoreCaseDoesNotAffectMultilineFlag() {
        assertUnsupported("(?i:es$)");
        assertUnsupported("(?i:es$)");
        assertUnsupported("(?i-:es$)");
        assertUnsupported("(?i-:es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-multiline-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddMultilineDoesNotAffectAlternativesOutside() {
        assertUnsupported("^a$|^b$|(?m:^c$)|^d$|^e$");
        assertUnsupported("(^a$)|(?:^b$)|(?m:^c$)|(?:^d$)|(^e$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-multiline-does-not-affect-dotAll-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddMultilineDoesNotAffectDotAllFlag() {
        assertUnsupported("(?m:es.$)");
        assertUnsupported("(?m:es.$)");
        assertUnsupported("(?m-:es.$)");
        assertUnsupported("(?m-:es.$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-multiline-does-not-affect-ignoreCase-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddMultilineDoesNotAffectIgnoreCaseFlag() {
        assertUnsupported("(?m:es$)");
        assertUnsupported("(?m:es$)");
        assertUnsupported("(?m-:es$)");
        assertUnsupported("(?m-:es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-multiline-does-not-affect-multiline-property.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddMultilineDoesNotAffectMultilineProperty() {
        assertUnsupported("(?m:)");
        assertUnsupported("(?m-:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-multiline.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddMultiline() {
        assertUnsupported("(?m:es$)");
        assertUnsupported("(?m-:es$)");
        assertUnsupported("^a\\n(?m:^b$)\\nc$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/add-remove-modifiers.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersAddRemoveModifiers() {
        assertUnsupported("(?m-i:^a$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/changing-dotAll-flag-does-not-affect-dotAll-modifier.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersChangingDotAllFlagDoesNotAffectDotAllModifier() {
        assertUnsupported("(?s:^.$)");
        assertUnsupported("(?-s:^.$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/changing-ignoreCase-flag-does-not-affect-ignoreCase-modifier.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersChangingIgnoreCaseFlagDoesNotAffectIgnoreCaseModifier() {
        assertUnsupported("(?i:aB)");
        assertUnsupported("(?-i:aB)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/changing-multiline-flag-does-not-affect-multiline-modifier.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersChangingMultilineFlagDoesNotAffectMultilineModifier() {
        assertUnsupported("(?m:es$)");
        assertUnsupported("^(?-m:es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nested-add-remove-modifiers.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestedAddRemoveModifiers() {
        assertUnsupported("(?m:^(?-i:a)$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-add-dotAll-within-remove-dotAll.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingAddDotAllWithinRemoveDotAll() {
        assertUnsupported("(?-s:(?s:^.$))");
        assertUnsupported("(?-s:(?s-:^.$))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-add-ignoreCase-within-remove-ignoreCase.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingAddIgnoreCaseWithinRemoveIgnoreCase() {
        assertUnsupported("(?-i:a(?i:b))c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-add-multiline-within-remove-multiline.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingAddMultilineWithinRemoveMultiline() {
        assertUnsupported("(?-m:es(?m:$)|js$)");
        assertUnsupported("(?-m:es(?m-:$)|js$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-dotAll-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingDotAllDoesNotAffectAlternativesOutside() {
        assertUnsupported("a.a|(?-s:b.b|(?s:c.c)|d.d|(?-s:e.e)|f.f)|g.g|(?s:h.h)|k.k");
        assertUnsupported("a.a|(?s:b.b|(?-s:c.c)|d.d|(?s:e.e)|f.f)|g.g|(?-s:h.h)|k.k");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-ignoreCase-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingIgnoreCaseDoesNotAffectAlternativesOutside() {
        assertUnsupported("a|(?-i:b|(?i:c)|d|(?-i:e)|f)|g|(?i:h)|k");
        assertUnsupported("a|(?i:b|(?-i:c)|d|(?i:e)|f)|g|(?-i:h)|k");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-multiline-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingMultilineDoesNotAffectAlternativesOutside() {
        assertUnsupported("^a$|(?-m:^b$|(?m:^c$)|^d$|(?-m:^e$)|^f$)|^g$|(?m:^h$)|^k$");
        assertUnsupported("^a$|(?m:^b$|(?-m:^c$)|^d$|(?m:^e$)|^f$)|^g$|(?-m:^h$)|^k$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-remove-dotAll-within-add-dotAll.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingRemoveDotAllWithinAddDotAll() {
        assertUnsupported("(?s:(?-s:^.$))");
        assertUnsupported("(?s-:(?-s:^.$))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-remove-ignoreCase-within-add-ignoreCase.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingRemoveIgnoreCaseWithinAddIgnoreCase() {
        assertUnsupported("(?i:a(?-i:b))c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/nesting-remove-multiline-within-add-multiline.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersNestingRemoveMultilineWithinAddMultiline() {
        assertUnsupported("(?m:es$|(?-m:js$))");
        assertUnsupported("(?m-:es$|(?-m:js$))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-dotAll-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveDotAllDoesNotAffectAlternativesOutside() {
        assertUnsupported("a.a|b.b|(?-s:c.c)|d.d|e.e");
        assertUnsupported("(a.a)|(?:b.b)|(?-s:c.c)|(?:d.d)|(e.e)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-dotAll-does-not-affect-dotAll-property.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveDotAllDoesNotAffectDotAllProperty() {
        assertUnsupported("(?-s:^.$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-dotAll-does-not-affect-ignoreCase-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveDotAllDoesNotAffectIgnoreCaseFlag() {
        assertUnsupported("(?-s:.es)");
        assertUnsupported("(?-s:.es)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-dotAll-does-not-affect-multiline-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveDotAllDoesNotAffectMultilineFlag() {
        assertUnsupported("(?-s:.es$)");
        assertUnsupported("(?-s:.es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-dotAll.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveDotAll() {
        assertUnsupported("(?-s:^.$)");
        assertUnsupported("a.(?-s:b.b).c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-backreferences.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsBackreferences() {
        assertUnsupported("(a)(?-i:\\1)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-characterClasses.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsCharacterClasses() {
        assertUnsupported("(?-i:[ab])c");
        assertUnsupported("(?-i:[^ab])c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-characterEscapes.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsCharacterEscapes() {
        assertUnsupported("(?-i:\\x61)b");
        assertUnsupported("(?-i:\\u0061)b");
        assertUnsupported("(?-i:\\u{0061})b");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-slash-lower-b.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsSlashLowerB() {
        assertUnsupported("(?-i:\\b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-slash-lower-p.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsSlashLowerP() {
        assertUnsupported("(?-i:\\p{Lu})");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-slash-lower-w.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsSlashLowerW() {
        assertUnsupported("(?-i:\\w)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-slash-upper-b.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsSlashUpperB() {
        assertUnsupported("(?-i:Z\\B)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-slash-upper-p.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsSlashUpperP() {
        assertUnsupported("(?-i:\\P{Lu})");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-affects-slash-upper-w.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseAffectsSlashUpperW() {
        assertUnsupported("(?-i:\\W)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseDoesNotAffectAlternativesOutside() {
        assertUnsupported("a|b|(?-i:c)|d|e");
        assertUnsupported("(a)|(?:b)|(?-i:c)|(?:d)|(e)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-does-not-affect-dotAll-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseDoesNotAffectDotAllFlag() {
        assertUnsupported("(?-i:.es)");
        assertUnsupported("(?-i:.es)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-does-not-affect-ignoreCase-property.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseDoesNotAffectIgnoreCaseProperty() {
        assertUnsupported("(?-i:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase-does-not-affect-multiline-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCaseDoesNotAffectMultilineFlag() {
        assertUnsupported("(?-i:es$)");
        assertUnsupported("(?-i:es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-ignoreCase.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveIgnoreCase() {
        assertUnsupported("(?-i:fo)o");
        assertUnsupported("b(?-i:ar)");
        assertUnsupported("b(?-i:a)z");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-multiline-does-not-affect-alternatives-outside.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveMultilineDoesNotAffectAlternativesOutside() {
        assertUnsupported("^a$|^b$|(?-m:^c$)|^d$|^e$");
        assertUnsupported("(^a$)|(?:^b$)|(?-m:^c$)|(?:^d$)|(^e$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-multiline-does-not-affect-dotAll-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveMultilineDoesNotAffectDotAllFlag() {
        assertUnsupported("(?-m:es.$)");
        assertUnsupported("(?-m:es.$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-multiline-does-not-affect-ignoreCase-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveMultilineDoesNotAffectIgnoreCaseFlag() {
        assertUnsupported("(?-m:es$)");
        assertUnsupported("(?-m:es$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-multiline-does-not-affect-multiline-property.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveMultilineDoesNotAffectMultilineProperty() {
        assertUnsupported("(?-m:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/remove-multiline.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersRemoveMultiline() {
        assertUnsupported("^(?-m:es$)");
        assertUnsupported("(?-m:^es)$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/add-and-remove-modifiers-can-have-empty-remove-modifiers.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidAddAndRemoveModifiersCanHaveEmptyRemoveModifiers() {
        assertUnsupported("(?i-:)");
        assertUnsupported("(?is-:)");
        assertUnsupported("(?im-:)");
        assertUnsupported("(?s-:)");
        assertUnsupported("(?si-:)");
        assertUnsupported("(?sm-:)");
        assertUnsupported("(?m-:)");
        assertUnsupported("(?mi-:)");
        assertUnsupported("(?ms-:)");
        assertUnsupported("(?ims-:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/add-and-remove-modifiers.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidAddAndRemoveModifiers() {
        assertUnsupported("(?i-s:)");
        assertUnsupported("(?i-sm:)");
        assertUnsupported("(?i-m:)");
        assertUnsupported("(?i-ms:)");
        assertUnsupported("(?s-i:)");
        assertUnsupported("(?s-im:)");
        assertUnsupported("(?s-m:)");
        assertUnsupported("(?s-mi:)");
        assertUnsupported("(?m-i:)");
        assertUnsupported("(?m-is:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/add-modifiers-when-nested.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidAddModifiersWhenNested() {
        assertUnsupported("(?i:(?i:))");
        assertUnsupported("(?s:(?s:))");
        assertUnsupported("(?m:(?m:))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/add-modifiers-when-not-set-as-flags.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidAddModifiersWhenNotSetAsFlags() {
        assertUnsupported("(?i:)");
        assertUnsupported("(?is:)");
        assertUnsupported("(?im:)");
        assertUnsupported("(?s:)");
        assertUnsupported("(?si:)");
        assertUnsupported("(?sm:)");
        assertUnsupported("(?m:)");
        assertUnsupported("(?mi:)");
        assertUnsupported("(?ms:)");
        assertUnsupported("(?ims:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/add-modifiers-when-set-as-flags.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidAddModifiersWhenSetAsFlags() {
        assertUnsupported("(?i:)");
        assertUnsupported("(?s:)");
        assertUnsupported("(?m:)");
        assertUnsupported("(?ims:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/remove-modifiers-when-nested.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidRemoveModifiersWhenNested() {
        assertUnsupported("(?-i:(?-i:))");
        assertUnsupported("(?-s:(?-s:))");
        assertUnsupported("(?-m:(?-m:))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/remove-modifiers-when-not-set-as-flags.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidRemoveModifiersWhenNotSetAsFlags() {
        assertUnsupported("(?-i:)");
        assertUnsupported("(?-is:)");
        assertUnsupported("(?-im:)");
        assertUnsupported("(?-s:)");
        assertUnsupported("(?-si:)");
        assertUnsupported("(?-sm:)");
        assertUnsupported("(?-m:)");
        assertUnsupported("(?-mi:)");
        assertUnsupported("(?-ms:)");
        assertUnsupported("(?-ims:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-modifiers/syntax/valid/remove-modifiers-when-set-as-flags.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262RegexpModifiersSyntaxValidRemoveModifiersWhenSetAsFlags() {
        assertUnsupported("(?-i:)");
        assertUnsupported("(?-is:)");
        assertUnsupported("(?-im:)");
        assertUnsupported("(?-s:)");
        assertUnsupported("(?-si:)");
        assertUnsupported("(?-sm:)");
        assertUnsupported("(?-m:)");
        assertUnsupported("(?-mi:)");
        assertUnsupported("(?-ms:)");
        assertUnsupported("(?-ims:)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-add-remove-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersAddRemoveI() {
        assertUnsupported("(?i-i:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-add-remove-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersAddRemoveM() {
        assertUnsupported("(?m-m:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-add-remove-multi-duplicate.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersAddRemoveMultiDuplicate() {
        assertUnsupported("(?ims-m:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-add-remove-s-escape.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersAddRemoveSEscape() {
        assertUnsupported("(?s-s:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-add-remove-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersAddRemoveS() {
        assertUnsupported("(?s-s:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-both-empty.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersBothEmpty() {
        assertUnsupported("(?-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-code-point-repeat-i-1.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersCodePointRepeatI1() {
        assertUnsupported("(?-ii:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-code-point-repeat-i-2.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersCodePointRepeatI2() {
        assertUnsupported("(?-imsi:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-arbitrary.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointArbitrary() {
        assertUnsupported("(?-1:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-combining-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointCombiningI() {
        assertUnsupported("(?-i\u0365:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-combining-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointCombiningM() {
        assertUnsupported("(?-m\u036b:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-combining-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointCombiningS() {
        assertUnsupported("(?-s\u0300:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-d.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointD() {
        assertUnsupported("(?-d:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-g.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointG() {
        assertUnsupported("(?-g:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-non-display-1.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointNonDisplay1() {
        assertUnsupported("(?-s\u0000:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-non-display-2.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointNonDisplay2() {
        assertUnsupported("(?-s\u200e:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-non-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointNonFlag() {
        assertUnsupported("(?-Q:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-u.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointU() {
        assertUnsupported("(?-u:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-uppercase-I.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointUppercaseI() {
        assertUnsupported("(?-I:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-y.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointY() {
        assertUnsupported("(?-y:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-zwj.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointZwj() {
        assertUnsupported("(?-s\u200d:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-zwnbsp.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointZwnbsp() {
        assertUnsupported("(?-s\ufeff:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-other-code-point-zwnj.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersOtherCodePointZwnj() {
        assertUnsupported("(?-s\u200c:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-add-remove-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseAddRemoveI() {
        assertUnsupported("(?i-i:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-add-remove-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseAddRemoveM() {
        assertUnsupported("(?m-m:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-add-remove-multi-duplicate.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseAddRemoveMultiDuplicate() {
        assertUnsupported("(?m-ims:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-add-remove-s-escape.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseAddRemoveSEscape() {
        assertUnsupported("(?s-s:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-add-remove-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseAddRemoveS() {
        assertUnsupported("(?s-s:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-code-point-repeat-i-1.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseCodePointRepeatI1() {
        assertUnsupported("(?ii-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-code-point-repeat-i-2.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseCodePointRepeatI2() {
        assertUnsupported("(?imsi-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-arbitrary.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointArbitrary() {
        assertUnsupported("(?1-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-combining-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointCombiningI() {
        assertUnsupported("(?i\u0365-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-combining-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointCombiningM() {
        assertUnsupported("(?m\u036b-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-combining-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointCombiningS() {
        assertUnsupported("(?s\u0300-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-d.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointD() {
        assertUnsupported("(?d-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-g.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointG() {
        assertUnsupported("(?g-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-non-display-1.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointNonDisplay1() {
        assertUnsupported("(?s\u0000-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-non-display-2.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointNonDisplay2() {
        assertUnsupported("(?s\u200e-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-non-flag.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointNonFlag() {
        assertUnsupported("(?Q-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-u.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointU() {
        assertUnsupported("(?u-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-uppercase-I.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointUppercaseI() {
        assertUnsupported("(?I-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-y.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointY() {
        assertUnsupported("(?y-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-zwj.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointZwj() {
        assertUnsupported("(?s\u200d-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-zwnbsp.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointZwnbsp() {
        assertUnsupported("(?s\ufeff-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-other-code-point-zwnj.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseOtherCodePointZwnj() {
        assertUnsupported("(?s\u200c-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-should-not-case-fold-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseShouldNotCaseFoldI() {
        assertUnsupported("(?I-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-should-not-case-fold-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseShouldNotCaseFoldM() {
        assertUnsupported("(?M-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-should-not-case-fold-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseShouldNotCaseFoldS() {
        assertUnsupported("(?S-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-should-not-unicode-case-fold-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseShouldNotUnicodeCaseFoldI() {
        assertUnsupported("(?\u0130-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-reverse-should-not-unicode-case-fold-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersReverseShouldNotUnicodeCaseFoldS() {
        assertUnsupported("(?\u017f-:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-should-not-case-fold-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersShouldNotCaseFoldI() {
        assertUnsupported("(?-I:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-should-not-case-fold-m.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersShouldNotCaseFoldM() {
        assertUnsupported("(?-M:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-should-not-case-fold-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersShouldNotCaseFoldS() {
        assertUnsupported("(?-S:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-should-not-unicode-case-fold-i.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersShouldNotUnicodeCaseFoldI() {
        assertUnsupported("(?-\u0130:a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/syntax-err-arithmetic-modifiers-should-not-unicode-case-fold-s.js
    @Test
    @Disabled("Inline RegExp modifiers are not supported")
    public void test262SyntaxErrArithmeticModifiersShouldNotUnicodeCaseFoldS() {
        assertUnsupported("(?-\u017f:a)");
    }
}
