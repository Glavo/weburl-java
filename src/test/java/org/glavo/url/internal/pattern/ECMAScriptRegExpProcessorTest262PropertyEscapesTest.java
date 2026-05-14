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

import static org.glavo.url.internal.pattern.ECMAScriptRegExpProcessorTest262Support.*;

/// Tests test262 Unicode property escape cases that are not part of the root generated data set.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorTest262PropertyEscapesTest {
    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_Y.js
    @Disabled("Unicode property escapes are not supported")
    @Test
    public void unicodePropertyEscapeAscii() {
        assertMatches("\\p{ASCII}", "A");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_F-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiFNegated() {
        assertUnsupported("\\P{ASCII=F}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_F.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiF() {
        assertUnsupported("\\p{ASCII=F}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_Invalid-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiInvalidNegated() {
        assertUnsupported("\\P{ASCII=Invalid}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_Invalid.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiInvalid() {
        assertUnsupported("\\p{ASCII=Invalid}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_N-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiNNegated() {
        assertUnsupported("\\P{ASCII=N}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_N.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiN() {
        assertUnsupported("\\p{ASCII=N}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_No-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiNoNegated() {
        assertUnsupported("\\P{ASCII=No}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_No.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiNo() {
        assertUnsupported("\\p{ASCII=No}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_T-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiTNegated() {
        assertUnsupported("\\P{ASCII=T}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_T.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiT() {
        assertUnsupported("\\p{ASCII=T}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_Y-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiYNegated() {
        assertUnsupported("\\P{ASCII=Y}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_Yes-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiYesNegated() {
        assertUnsupported("\\P{ASCII=Yes}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/binary-property-with-value-ASCII_-_Yes.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeBinaryPropertyWithValueAsciiYes() {
        assertUnsupported("\\p{ASCII=Yes}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/character-class-range-end.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeCharacterClassRangeEnd() {
        assertUnsupported("[--\\p{Hex}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/character-class-range-no-dash-end.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeCharacterClassRangeNoDashEnd() {
        assertUnsupported("[\\uFFFF-\\p{Hex}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/character-class-range-no-dash-start.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeCharacterClassRangeNoDashStart() {
        assertUnsupported("[\\p{Hex}-\\uFFFF]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/character-class-range-start.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeCharacterClassRangeStart() {
        assertUnsupported("[\\p{Hex}--]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/character-class.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeCharacterClass() {
        assertUnsupported("[\\p{Hex}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-In-prefix-Block-implicit-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInPrefixBlockImplicitNegated() {
        assertUnsupported("\\P{InAdlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-In-prefix-Block-implicit.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInPrefixBlockImplicit() {
        assertUnsupported("\\p{InAdlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-In-prefix-Script-implicit-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInPrefixScriptImplicitNegated() {
        assertUnsupported("\\P{InAdlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-In-prefix-Script-implicit.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInPrefixScriptImplicit() {
        assertUnsupported("\\p{InAdlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-In-prefix-Script-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInPrefixScriptNegated() {
        assertUnsupported("\\P{InScript=Adlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-In-prefix-Script.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInPrefixScript() {
        assertUnsupported("\\p{InScript=Adlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-Is-prefix-Script-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionIsPrefixScriptNegated() {
        assertUnsupported("\\P{IsScript=Adlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-Is-prefix-Script.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionIsPrefixScript() {
        assertUnsupported("\\p{IsScript=Adlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-circumflex-negation-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionCircumflexNegationNegated() {
        assertUnsupported("\\P{^General_Category=Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-circumflex-negation.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionCircumflexNegation() {
        assertUnsupported("\\p{^General_Category=Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-empty-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionEmptyNegated() {
        assertUnsupported("[\\p{}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-empty.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionEmpty() {
        assertUnsupported("[\\P{}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-invalid-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInvalidNegated() {
        assertUnsupported("[\\P{invalid}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-invalid.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionInvalid() {
        assertUnsupported("[\\p{invalid}]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-no-braces-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionNoBracesNegated() {
        assertUnsupported("\\P");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-no-braces-value-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionNoBracesValueNegated() {
        assertUnsupported("\\PL");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-no-braces-value.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionNoBracesValue() {
        assertUnsupported("\\pL");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-no-braces.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionNoBraces() {
        assertUnsupported("\\p");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-separator-and-value-only-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionSeparatorAndValueOnlyNegated() {
        assertUnsupported("\\P{=Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-separator-and-value-only.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionSeparatorAndValueOnly() {
        assertUnsupported("\\p{=Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-separator-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionSeparatorNegated() {
        assertUnsupported("\\P{General_Category:Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-separator-only-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionSeparatorOnlyNegated() {
        assertUnsupported("\\P{=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-separator-only.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionSeparatorOnly() {
        assertUnsupported("\\p{=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-separator.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionSeparator() {
        assertUnsupported("\\p{General_Category:Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-unclosed-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionUnclosedNegated() {
        assertUnsupported("\\P{");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-unclosed.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionUnclosed() {
        assertUnsupported("\\p{");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-unopened-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionUnopenedNegated() {
        assertUnsupported("\\P}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/grammar-extension-unopened.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeGrammarExtensionUnopened() {
        assertUnsupported("\\p}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-01-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching01Negated() {
        assertUnsupported("\\P{ General_Category=Uppercase_Letter }");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-01.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching01() {
        assertUnsupported("\\p{ General_Category=Uppercase_Letter }");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-02-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching02Negated() {
        assertUnsupported("\\P{ Lowercase }");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-02.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching02() {
        assertUnsupported("\\p{ Lowercase }");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-03-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching03Negated() {
        assertUnsupported("\\P{ANY}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-03.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching03() {
        assertUnsupported("\\p{ANY}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-04-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching04Negated() {
        assertUnsupported("\\P{ASSIGNED}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-04.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching04() {
        assertUnsupported("\\p{ASSIGNED}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-05-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching05Negated() {
        assertUnsupported("\\P{Ascii}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-05.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching05() {
        assertUnsupported("\\p{Ascii}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-06-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching06Negated() {
        assertUnsupported("\\P{General_Category = Uppercase_Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-06.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching06() {
        assertUnsupported("\\p{General_Category = Uppercase_Letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-07-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching07Negated() {
        assertUnsupported("\\P{_-_lOwEr_C-A_S-E_-_}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-07.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching07() {
        assertUnsupported("\\p{_-_lOwEr_C-A_S-E_-_}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-08-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching08Negated() {
        assertUnsupported("\\P{any}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-08.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching08() {
        assertUnsupported("\\p{any}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-09-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching09Negated() {
        assertUnsupported("\\P{ascii}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-09.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching09() {
        assertUnsupported("\\p{ascii}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-10-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching10Negated() {
        assertUnsupported("\\P{assigned}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-10.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching10() {
        assertUnsupported("\\p{assigned}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-11-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching11Negated() {
        assertUnsupported("\\P{gC=uppercase_letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-11.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching11() {
        assertUnsupported("\\p{gC=uppercase_letter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-12-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching12Negated() {
        assertUnsupported("\\P{gc=uppercaseletter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-12.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching12() {
        assertUnsupported("\\p{gc=uppercaseletter}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-13-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching13Negated() {
        assertUnsupported("\\P{lowercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-13.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching13() {
        assertUnsupported("\\p{lowercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-14-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching14Negated() {
        assertUnsupported("\\P{lowercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/loose-matching-14.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeLooseMatching14() {
        assertUnsupported("\\p{lowercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-General_Category-equals-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueGeneralCategoryEqualsNegated() {
        assertUnsupported("\\P{General_Category=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-General_Category-equals.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueGeneralCategoryEquals() {
        assertUnsupported("\\p{General_Category=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-General_Category-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueGeneralCategoryNegated() {
        assertUnsupported("\\P{General_Category}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-General_Category.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueGeneralCategory() {
        assertUnsupported("\\p{General_Category}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script-equals-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptEqualsNegated() {
        assertUnsupported("\\P{Script=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script-equals.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptEquals() {
        assertUnsupported("\\p{Script=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptNegated() {
        assertUnsupported("\\P{Script}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScript() {
        assertUnsupported("\\p{Script}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script_Extensions-equals-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptExtensionsEqualsNegated() {
        assertUnsupported("\\P{Script_Extensions=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script_Extensions-equals.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptExtensionsEquals() {
        assertUnsupported("\\p{Script_Extensions=}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script_Extensions-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptExtensionsNegated() {
        assertUnsupported("\\P{Script_Extensions}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-binary-property-without-value-Script_Extensions.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonBinaryPropertyWithoutValueScriptExtensions() {
        assertUnsupported("\\p{Script_Extensions}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-binary-property-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentBinaryPropertyNegated() {
        assertUnsupported("\\P{UnknownBinaryProperty}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-binary-property.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentBinaryProperty() {
        assertUnsupported("\\p{UnknownBinaryProperty}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-and-value-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyAndValueNegated() {
        assertUnsupported("\\P{Line_Breakz=WAT}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-and-value.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyAndValue() {
        assertUnsupported("\\p{Line_Breakz=WAT}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-existing-value-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyExistingValueNegated() {
        assertUnsupported("\\P{Line_Breakz=Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-existing-value.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyExistingValue() {
        assertUnsupported("\\p{Line_Breakz=Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-value-General_Category-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyValueGeneralCategoryNegated() {
        assertUnsupported("\\\\P{General_Category=WAT}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-value-Script-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyValueScriptNegated() {
        assertUnsupported("\\\\P{Script=FooBarBazInvalid}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-value-Script.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyValueScript() {
        assertUnsupported("\\\\p{Script=FooBarBazInvalid}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-value-Script_Extensions-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyValueScriptExtensionsNegated() {
        assertUnsupported("\\\\P{Script_Extensions=H_e_h}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-value-Script_Extensions.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyValueScriptExtensions() {
        assertUnsupported("\\\\p{Script_Extensions=H_e_h}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/non-existent-property-value-general-category.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeNonExistentPropertyValueGeneralCategory() {
        assertUnsupported("\\\\p{General_Category=WAT}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/special-property-value-Script_Extensions-Unknown.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeSpecialPropertyValueScriptExtensionsUnknown() {
        assertUnsupported("\\p{Script_Extensions=Unknown}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Composition_Exclusion-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyCompositionExclusionNegated() {
        assertUnsupported("\\P{Composition_Exclusion}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Composition_Exclusion.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyCompositionExclusion() {
        assertUnsupported("\\p{Composition_Exclusion}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFC-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfcNegated() {
        assertUnsupported("\\P{Expands_On_NFC}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFC.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfc() {
        assertUnsupported("\\p{Expands_On_NFC}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFD-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfdNegated() {
        assertUnsupported("\\P{Expands_On_NFD}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFD.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfd() {
        assertUnsupported("\\p{Expands_On_NFD}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFKC-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfkcNegated() {
        assertUnsupported("\\P{Expands_On_NFKC}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFKC.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfkc() {
        assertUnsupported("\\p{Expands_On_NFKC}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFKD-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfkdNegated() {
        assertUnsupported("\\P{Expands_On_NFKD}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Expands_On_NFKD.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyExpandsOnNfkd() {
        assertUnsupported("\\p{Expands_On_NFKD}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-FC_NFKC_Closure-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyFcNfkcClosureNegated() {
        assertUnsupported("\\P{FC_NFKC_Closure}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-FC_NFKC_Closure.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyFcNfkcClosure() {
        assertUnsupported("\\p{FC_NFKC_Closure}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Full_Composition_Exclusion-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyFullCompositionExclusionNegated() {
        assertUnsupported("\\P{Full_Composition_Exclusion}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Full_Composition_Exclusion.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyFullCompositionExclusion() {
        assertUnsupported("\\p{Full_Composition_Exclusion}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Grapheme_Link-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyGraphemeLinkNegated() {
        assertUnsupported("\\P{Grapheme_Link}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Grapheme_Link.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyGraphemeLink() {
        assertUnsupported("\\p{Grapheme_Link}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Hyphen-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyHyphenNegated() {
        assertUnsupported("\\P{Hyphen}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Hyphen.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyHyphen() {
        assertUnsupported("\\p{Hyphen}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Alphabetic-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherAlphabeticNegated() {
        assertUnsupported("\\P{Other_Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Alphabetic.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherAlphabetic() {
        assertUnsupported("\\p{Other_Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Default_Ignorable_Code_Point-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherDefaultIgnorableCodePointNegated() {
        assertUnsupported("\\P{Other_Default_Ignorable_Code_Point}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Default_Ignorable_Code_Point.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherDefaultIgnorableCodePoint() {
        assertUnsupported("\\p{Other_Default_Ignorable_Code_Point}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Grapheme_Extend-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherGraphemeExtendNegated() {
        assertUnsupported("\\P{Other_Grapheme_Extend}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Grapheme_Extend.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherGraphemeExtend() {
        assertUnsupported("\\p{Other_Grapheme_Extend}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_ID_Continue-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherIdContinueNegated() {
        assertUnsupported("\\P{Other_ID_Continue}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_ID_Continue.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherIdContinue() {
        assertUnsupported("\\p{Other_ID_Continue}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_ID_Start-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherIdStartNegated() {
        assertUnsupported("\\P{Other_ID_Start}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_ID_Start.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherIdStart() {
        assertUnsupported("\\p{Other_ID_Start}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Lowercase-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherLowercaseNegated() {
        assertUnsupported("\\P{Other_Lowercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Lowercase.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherLowercase() {
        assertUnsupported("\\p{Other_Lowercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Math-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherMathNegated() {
        assertUnsupported("\\P{Other_Math}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Math.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherMath() {
        assertUnsupported("\\p{Other_Math}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Uppercase-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherUppercaseNegated() {
        assertUnsupported("\\P{Other_Uppercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Other_Uppercase.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyOtherUppercase() {
        assertUnsupported("\\p{Other_Uppercase}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Prepended_Concatenation_Mark-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyPrependedConcatenationMarkNegated() {
        assertUnsupported("\\P{Prepended_Concatenation_Mark}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-binary-property-Prepended_Concatenation_Mark.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedBinaryPropertyPrependedConcatenationMark() {
        assertUnsupported("\\p{Prepended_Concatenation_Mark}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-Block-with-value-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyBlockWithValueNegated() {
        assertUnsupported("\\P{Block=Adlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-Block-with-value.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyBlockWithValue() {
        assertUnsupported("\\p{Block=Adlam}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-FC_NFKC_Closure-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyFcNfkcClosureNegated() {
        assertUnsupported("\\P{FC_NFKC_Closure}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-FC_NFKC_Closure.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyFcNfkcClosure() {
        assertUnsupported("\\p{FC_NFKC_Closure}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-Line_Break-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyLineBreakNegated() {
        assertUnsupported("\\P{Line_Break=Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-Line_Break-with-value-negated.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyLineBreakWithValueNegated() {
        assertUnsupported("\\P{Line_Break=Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-Line_Break-with-value.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyLineBreakWithValue() {
        assertUnsupported("\\p{Line_Break=Alphabetic}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/property-escapes/unsupported-property-Line_Break.js
    @Test
    @Disabled("Unicode property escapes are not supported yet")
    public void unicodePropertyEscapeUnsupportedPropertyLineBreak() {
        assertUnsupported("\\p{Line_Break}");
    }
}
