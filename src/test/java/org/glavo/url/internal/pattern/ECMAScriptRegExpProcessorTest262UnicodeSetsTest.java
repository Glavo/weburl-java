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

/// Tests test262 Unicode Sets cases for `ECMAScriptRegExpProcessor`.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorTest262UnicodeSetsTest {
    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-difference-character.js
    @Test
    public void unicodeSetClassDifferenceCharacter() {
        assertMatches("[[0-9]--_]", "0");
        assertMatches("[[0-9]--_]", "9");
        assertDoesNotMatch("[[0-9]--_]", "_");
        assertDoesNotMatch("[[0-9]--_]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-intersection-character.js
    @Test
    public void unicodeSetClassIntersectionCharacter() {
        assertDoesNotMatch("[[0-9]&&_]", "0");
        assertDoesNotMatch("[[0-9]&&_]", "9");
        assertDoesNotMatch("[[0-9]&&_]", "_");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-union-character.js
    @Test
    public void unicodeSetClassUnionCharacter() {
        assertMatches("[[0-9]_]", "0");
        assertMatches("[[0-9]_]", "9");
        assertMatches("[[0-9]_]", "_");
        assertDoesNotMatch("[[0-9]_]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-union-character.js
    @Test
    public void unicodeSetCharacterUnionCharacter() {
        assertMatches("[__]", "_");
        assertDoesNotMatch("[__]", "7");
        assertDoesNotMatch("[__]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-union-character-class.js
    @Test
    public void unicodeSetCharacterUnionCharacterClass() {
        assertAsciiDigitsMatch("[_[0-9]]");
        assertMatches("[_[0-9]]", "_");
        assertDoesNotMatch("[_[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-union-character-class-escape.js
    @Test
    public void unicodeSetCharacterUnionCharacterClassEscape() {
        assertAsciiDigitsMatch("[_\\d]");
        assertMatches("[_\\d]", "_");
        assertDoesNotMatch("[_\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-difference-character.js
    @Test
    public void unicodeSetCharacterDifferenceCharacter() {
        assertDoesNotMatch("[_--_]", "_");
        assertDoesNotMatch("[_--_]", "7");
        assertDoesNotMatch("[_--_]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-difference-character-class.js
    @Test
    public void unicodeSetCharacterDifferenceCharacterClass() {
        assertMatches("[_--[0-9]]", "_");
        assertAsciiDigitsDoNotMatch("[_--[0-9]]");
        assertDoesNotMatch("[_--[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-difference-character-class-escape.js
    @Test
    public void unicodeSetCharacterDifferenceCharacterClassEscape() {
        assertMatches("[_--\\d]", "_");
        assertAsciiDigitsDoNotMatch("[_--\\d]");
        assertDoesNotMatch("[_--\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-intersection-character.js
    @Test
    public void unicodeSetCharacterIntersectionCharacter() {
        assertMatches("[_&&_]", "_");
        assertDoesNotMatch("[_&&_]", "7");
        assertDoesNotMatch("[_&&_]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-intersection-character-class.js
    @Test
    public void unicodeSetCharacterIntersectionCharacterClass() {
        assertDoesNotMatch("[_&&[0-9]]", "_");
        assertAsciiDigitsDoNotMatch("[_&&[0-9]]");
        assertDoesNotMatch("[_&&[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-intersection-character-class-escape.js
    @Test
    public void unicodeSetCharacterIntersectionCharacterClassEscape() {
        assertDoesNotMatch("[_&&\\d]", "_");
        assertAsciiDigitsDoNotMatch("[_&&\\d]");
        assertDoesNotMatch("[_&&\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-union-character-class.js
    @Test
    public void unicodeSetCharacterClassUnionCharacterClass() {
        assertAsciiDigitsMatch("[[0-9][0-9]]");
        assertDoesNotMatch("[[0-9][0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-union-character-class-escape.js
    @Test
    public void unicodeSetCharacterClassUnionCharacterClassEscape() {
        assertAsciiDigitsMatch("[[0-9]\\d]");
        assertDoesNotMatch("[[0-9]\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-difference-character-class.js
    @Test
    public void unicodeSetCharacterClassDifferenceCharacterClass() {
        assertAsciiDigitsDoNotMatch("[[0-9]--[0-9]]");
        assertDoesNotMatch("[[0-9]--[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-difference-character-class-escape.js
    @Test
    public void unicodeSetCharacterClassDifferenceCharacterClassEscape() {
        assertAsciiDigitsDoNotMatch("[[0-9]--\\d]");
        assertDoesNotMatch("[[0-9]--\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-intersection-character-class.js
    @Test
    public void unicodeSetCharacterClassIntersectionCharacterClass() {
        assertAsciiDigitsMatch("[[0-9]&&[0-9]]");
        assertDoesNotMatch("[[0-9]&&[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-intersection-character-class-escape.js
    @Test
    public void unicodeSetCharacterClassIntersectionCharacterClassEscape() {
        assertAsciiDigitsMatch("[[0-9]&&\\d]");
        assertDoesNotMatch("[[0-9]&&\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-union-character.js
    @Test
    public void unicodeSetCharacterClassEscapeUnionCharacter() {
        assertAsciiDigitsMatch("[\\d_]");
        assertMatches("[\\d_]", "_");
        assertDoesNotMatch("[\\d_]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-union-character-class.js
    @Test
    public void unicodeSetCharacterClassEscapeUnionCharacterClass() {
        assertAsciiDigitsMatch("[\\d[0-9]]");
        assertDoesNotMatch("[\\d[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-difference-character.js
    @Test
    public void unicodeSetCharacterClassEscapeDifferenceCharacter() {
        assertAsciiDigitsMatch("[\\d--_]");
        assertDoesNotMatch("[\\d--_]", "_");
        assertDoesNotMatch("[\\d--_]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-difference-character-class.js
    @Test
    public void unicodeSetCharacterClassEscapeDifferenceCharacterClass() {
        assertAsciiDigitsDoNotMatch("[\\d--[0-9]]");
        assertDoesNotMatch("[\\d--[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-intersection-character-class.js
    @Test
    public void unicodeSetCharacterClassEscapeIntersectionCharacterClass() {
        assertAsciiDigitsMatch("[\\d&&[0-9]]");
        assertDoesNotMatch("[\\d&&[0-9]]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-intersection-character-class-escape.js
    @Test
    public void unicodeSetCharacterClassEscapeIntersectionCharacterClassEscape() {
        assertAsciiDigitsMatch("[\\d&&\\d]");
        assertDoesNotMatch("[\\d&&\\d]", "C");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-intersection-character.js
    @Test
    public void unicodeSetEscapeIntersectionCharacter() {
        assertDoesNotMatch("[\\d&&_]", "0");
        assertDoesNotMatch("[\\d&&_]", "9");
        assertDoesNotMatch("[\\d&&_]", "_");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-difference-character-property-escape.js
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    @Test
    public void unicodeSetPropertyEscapeDifference() {
        assertDoesNotMatch("[[0-9]--\\p{ASCII_Hex_Digit}]", "0");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-union-string-literal.js
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    @Test
    public void unicodeSetStringLiteralUnion() {
        assertMatches("[[0-9]\\q{ab}]", "ab");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-difference-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassDifferencePropertyOfStringsEscape() {
        assertUnsupported("^[[0-9]--\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-difference-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassDifferenceStringLiteral() {
        assertUnsupported("^[[0-9]--\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-difference-character-class-escape.js
    @Test
    @Disabled("This Unicode Sets case is outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeDifferenceCharacterClassEscape() {
        assertUnsupported("^[\\d--\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-difference-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeDifferenceCharacterPropertyEscape() {
        assertUnsupported("^[\\d--\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-difference-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeDifferencePropertyOfStringsEscape() {
        assertUnsupported("^[\\d--\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-difference-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeDifferenceStringLiteral() {
        assertUnsupported("^[\\d--\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-intersection-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeIntersectionCharacterPropertyEscape() {
        assertUnsupported("^[\\d&&\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-intersection-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeIntersectionPropertyOfStringsEscape() {
        assertUnsupported("^[\\d&&\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-intersection-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeIntersectionStringLiteral() {
        assertUnsupported("^[\\d&&\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-union-character-class-escape.js
    @Test
    @Disabled("This Unicode Sets case is outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeUnionCharacterClassEscape() {
        assertUnsupported("^[\\d\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-union-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeUnionCharacterPropertyEscape() {
        assertUnsupported("^[\\d\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-union-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeUnionPropertyOfStringsEscape() {
        assertUnsupported("^[\\d\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-escape-union-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassEscapeUnionStringLiteral() {
        assertUnsupported("^[\\d\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-intersection-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterClassIntersectionCharacterPropertyEscape() {
        assertUnsupported("^[[0-9]&&\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-intersection-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassIntersectionPropertyOfStringsEscape() {
        assertUnsupported("^[[0-9]&&\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-intersection-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassIntersectionStringLiteral() {
        assertUnsupported("^[[0-9]&&\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-union-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterClassUnionCharacterPropertyEscape() {
        assertUnsupported("^[[0-9]\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-class-union-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterClassUnionPropertyOfStringsEscape() {
        assertUnsupported("^[[0-9]\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-difference-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterDifferenceCharacterPropertyEscape() {
        assertUnsupported("^[_--\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-difference-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterDifferencePropertyOfStringsEscape() {
        assertUnsupported("^[_--\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-difference-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterDifferenceStringLiteral() {
        assertUnsupported("^[_--\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-intersection-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterIntersectionCharacterPropertyEscape() {
        assertUnsupported("^[_&&\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-intersection-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterIntersectionPropertyOfStringsEscape() {
        assertUnsupported("^[_&&\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-intersection-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterIntersectionStringLiteral() {
        assertUnsupported("^[_&&\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-difference-character-class-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeDifferenceCharacterClassEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}--\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-difference-character-class.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeDifferenceCharacterClass() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}--[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-difference-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeDifferenceCharacterPropertyEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}--\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-difference-character.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeDifferenceCharacter() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}--_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-difference-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeDifferencePropertyOfStringsEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}--\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-difference-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeDifferenceStringLiteral() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}--\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-intersection-character-class-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeIntersectionCharacterClassEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}&&\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-intersection-character-class.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeIntersectionCharacterClass() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}&&[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-intersection-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeIntersectionCharacterPropertyEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}&&\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-intersection-character.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeIntersectionCharacter() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}&&_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-intersection-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeIntersectionPropertyOfStringsEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}&&\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-intersection-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeIntersectionStringLiteral() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}&&\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-union-character-class-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeUnionCharacterClassEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-union-character-class.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeUnionCharacterClass() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-union-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeUnionCharacterPropertyEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-union-character.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeUnionCharacter() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-union-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeUnionPropertyOfStringsEscape() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-property-escape-union-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterPropertyEscapeUnionStringLiteral() {
        assertUnsupported("^[\\p{ASCII_Hex_Digit}\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-union-character-property-escape.js
    @Test
    @Disabled("Unicode property escapes inside v-mode class sets are not supported")
    public void test262UnicodeSetsGeneratedCharacterUnionCharacterPropertyEscape() {
        assertUnsupported("^[_\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-union-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterUnionPropertyOfStringsEscape() {
        assertUnsupported("^[_\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/character-union-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedCharacterUnionStringLiteral() {
        assertUnsupported("^[_\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-difference-character-class-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeDifferenceCharacterClassEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}--\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-difference-character-class.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeDifferenceCharacterClass() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}--[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-difference-character-property-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeDifferenceCharacterPropertyEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}--\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-difference-character.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeDifferenceCharacter() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}--_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-difference-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeDifferencePropertyOfStringsEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}--\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-difference-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeDifferenceStringLiteral() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}--\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-intersection-character-class-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeIntersectionCharacterClassEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}&&\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-intersection-character-class.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeIntersectionCharacterClass() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}&&[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-intersection-character-property-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeIntersectionCharacterPropertyEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}&&\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-intersection-character.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeIntersectionCharacter() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}&&_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-intersection-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeIntersectionPropertyOfStringsEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}&&\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-intersection-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeIntersectionStringLiteral() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}&&\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-union-character-class-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeUnionCharacterClassEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-union-character-class.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeUnionCharacterClass() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-union-character-property-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeUnionCharacterPropertyEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-union-character.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeUnionCharacter() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-union-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeUnionPropertyOfStringsEscape() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/property-of-strings-escape-union-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedPropertyOfStringsEscapeUnionStringLiteral() {
        assertUnsupported("^[\\p{Emoji_Keycap_Sequence}\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/rgi-emoji-13.1.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedRgiEmoji131() {
        assertUnsupported("^\\p{RGI_Emoji}+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/rgi-emoji-14.0.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedRgiEmoji140() {
        assertUnsupported("^\\p{RGI_Emoji}+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/rgi-emoji-15.0.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedRgiEmoji150() {
        assertUnsupported("^\\p{RGI_Emoji}+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/rgi-emoji-15.1.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedRgiEmoji151() {
        assertUnsupported("^\\p{RGI_Emoji}+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/rgi-emoji-16.0.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedRgiEmoji160() {
        assertUnsupported("^\\p{RGI_Emoji}+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/rgi-emoji-17.0.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedRgiEmoji170() {
        assertUnsupported("^\\p{RGI_Emoji}+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-difference-character-class-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralDifferenceCharacterClassEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}--\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-difference-character-class.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralDifferenceCharacterClass() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}--[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-difference-character-property-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralDifferenceCharacterPropertyEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}--\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-difference-character.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralDifferenceCharacter() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}--_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-difference-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralDifferencePropertyOfStringsEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}--\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-difference-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralDifferenceStringLiteral() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}--\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-intersection-character-class-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralIntersectionCharacterClassEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}&&\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-intersection-character-class.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralIntersectionCharacterClass() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}&&[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-intersection-character-property-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralIntersectionCharacterPropertyEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}&&\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-intersection-character.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralIntersectionCharacter() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}&&_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-intersection-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralIntersectionPropertyOfStringsEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}&&\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-intersection-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralIntersectionStringLiteral() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}&&\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-union-character-class-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralUnionCharacterClassEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}\\d]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-union-character-class.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralUnionCharacterClass() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}[0-9]]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-union-character-property-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralUnionCharacterPropertyEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}\\p{ASCII_Hex_Digit}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-union-character.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralUnionCharacter() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}_]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-union-property-of-strings-escape.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralUnionPropertyOfStringsEscape() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}\\p{Emoji_Keycap_Sequence}]+$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicodeSets/generated/string-literal-union-string-literal.js
    @Test
    @Disabled("String-literal Unicode Sets are outside the current finite ASCII subset")
    public void test262UnicodeSetsGeneratedStringLiteralUnionStringLiteral() {
        assertUnsupported("^[\\q{0|2|4|9\\uFE0F\\u20E3}\\q{0|2|4|9\\uFE0F\\u20E3}]+$");
    }
}
