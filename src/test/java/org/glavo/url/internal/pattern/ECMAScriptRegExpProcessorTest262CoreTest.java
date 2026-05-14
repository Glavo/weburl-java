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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests core test262 regular-expression cases for `ECMAScriptRegExpProcessor`.
@NotNullByDefault
public final class ECMAScriptRegExpProcessorTest262CoreTest {
    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T1.js
    @Test
    public void repeatedStarQuantifierIsRejected() {
        assertUnsupported("a**");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T2.js
    @Test
    public void tripledStarQuantifierIsRejected() {
        assertUnsupported("a***");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T3.js
    @Test
    public void repeatedPlusQuantifierIsRejected() {
        assertUnsupported("a++");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T4.js
    @Test
    public void tripledPlusQuantifierIsRejected() {
        assertUnsupported("a+++");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T5.js
    @Test
    public void tripledQuestionQuantifierIsRejected() {
        assertUnsupported("a???");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T6.js
    @Test
    public void quadrupledQuestionQuantifierIsRejected() {
        assertUnsupported("a????");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T7.js
    @Test
    public void leadingStarQuantifierIsRejected() {
        assertUnsupported("*a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T8.js
    @Test
    public void leadingRepeatedStarQuantifierIsRejected() {
        assertUnsupported("**a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T9.js
    @Test
    public void leadingPlusQuantifierIsRejected() {
        assertUnsupported("+a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T10.js
    @Test
    public void leadingRepeatedPlusQuantifierIsRejected() {
        assertUnsupported("++a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T11.js
    @Test
    public void leadingQuestionQuantifierIsRejected() {
        assertUnsupported("?a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T12.js
    @Test
    public void leadingRepeatedQuestionQuantifierIsRejected() {
        assertUnsupported("??a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T13.js
    @Test
    public void exactQuantifierFollowedByOpenRangeQuantifierIsRejected() {
        assertUnsupported("x{1}{1,}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T14.js
    @Test
    public void rangeQuantifierFollowedByExactQuantifierIsRejected() {
        assertUnsupported("x{1,2}{1}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T15.js
    @Test
    public void openRangeQuantifierFollowedByExactQuantifierIsRejected() {
        assertUnsupported("x{1,}{1}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.1_A1_T16.js
    @Test
    public void optionalRangeQuantifierFollowedByOpenRangeQuantifierIsRejected() {
        assertUnsupported("x{0,1}{1,}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T1.js
    @Test
    public void disjunctionChoosesLeftAlternative() {
        assertFinds("a|ab", "abc", "a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T3.js
    @Test
    public void disjunctionFindsLaterCharacterClassAlternative() {
        assertFinds("\\d{3}|[a-z]{4}", "2, 12 and of course repeat 12", "cour");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T4.js
    @Test
    public void disjunctionFindsEarlierDigitAlternative() {
        assertFinds("\\d{3}|[a-z]{4}", "2, 12 and 234 AND of course repeat 12", "234");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T5.js
    @Test
    public void disjunctionDoesNotMatchWhenNoAlternativeMatches() {
        assertDoesNotFind("\\d{3}|[a-z]{4}", "2, 12 and 23 AND 0.00.1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T6.js
    @Test
    public void caseInsensitiveDisjunctionFindsMiddleAlternative() {
        assertFinds("ab|cd|ef", "AEKFCD", "CD", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T7.js
    @Test
    public void caseSensitiveDisjunctionDoesNotFindUppercaseAlternative() {
        assertDoesNotFind("ab|cd|ef", "AEKFCD");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T8.js
    @Test
    public void caseInsensitiveDisjunctionWithNonCapturingGroup() {
        assertFinds("(?:(?:ab|cd)+|ef)", "AEKFCD", "CD", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T9.js
    @Test
    public void caseInsensitiveDisjunctionWithRepeatedGroupConsumesSequence() {
        assertFinds("(?:(?:ab|cd)+|ef)", "AEKFCDab", "CDab", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T10.js
    @Test
    public void caseInsensitiveDisjunctionFindsEarlierShortAlternative() {
        assertFinds("(?:(?:ab|cd)+|ef)", "AEKeFCDab", "eF", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T11.js
    @Test
    public void disjunctionChoosesLongerLeftAlternative() {
        assertFinds("11111|111", "1111111111111111", "11111");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T12.js
    @Test
    public void disjunctionFallsBackToWildcardAlternative() {
        assertFinds("xyz|...", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.5_A1_T1.js
    @Test
    public void boundedQuantifierMatchesGreedily() {
        assertFinds("a[a-z]{2,4}", "abcdefghi", "abcde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.5_A1_T2.js
    @Test
    public void lazyBoundedQuantifierMatchesMinimally() {
        assertFinds("a[a-z]{2,4}?", "abcdefghi", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T1.js
    @Test
    public void digitBoundedQuantifierFindsDigits() {
        assertFinds("\\d{2,4}", "the answer is 42", "42");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T2.js
    @Test
    public void digitBoundedQuantifierRejectsSingleDigit() {
        assertDoesNotFind("\\d{2,4}", "the 7 movie");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T3.js
    @Test
    public void digitBoundedQuantifierFindsMaximumLengthMatch() {
        assertFinds("\\d{2,4}", "the 20000 Leagues Under the Sea book", "2000");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T4.js
    @Test
    public void digitBoundedQuantifierFindsMiddleLengthMatch() {
        assertFinds("\\d{2,4}", "the Fahrenheit 451 book", "451");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T5.js
    @Test
    public void digitBoundedQuantifierFindsFourDigits() {
        assertFinds("\\d{2,4}", "the 1984 novel", "1984");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T6.js
    @Test
    public void digitBoundedQuantifierFindsThreeDigitsAfterNonDigit() {
        assertFinds("\\d{2,4}", "0a011b", "011");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T7.js
    @Test
    public void digitBoundedQuantifierFindsFourDigitsFromLongerRun() {
        assertFinds("\\d{2,4}", "0a01122b", "0112");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T8.js
    @Test
    public void literalBoundedQuantifierFindsMaximumBeforeSuffix() {
        assertFinds("b{2,3}c", "aaabbbbcccddeeeefffff", "bbbc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T9.js
    @Test
    public void literalBoundedQuantifierDoesNotMatchInsufficientRun() {
        assertDoesNotFind("b{42,93}c", "aaabbbbcccddeeeefffff");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T10.js
    @Test
    public void zeroMinimumBoundedQuantifierConsumesBeforeSuffix() {
        assertFinds("b{0,93}c", "aaabbbbcccddeeeefffff", "bbbbc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T11.js
    @Test
    public void zeroMinimumBoundedQuantifierMayBeAbsentBeforeSuffix() {
        assertFinds("bx{0,93}c", "aaabbbbcccddeeeefffff", "bc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A1_T12.js
    @Test
    public void dotBoundedQuantifierConsumesWholeInput() {
        assertFinds(".{0,93}", "weirwerdf", "weirwerdf");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A2_T1.js
    @Test
    public void optionalDigitAfterWordQuantifierMatchesWhenPresent() {
        assertFinds("\\w{3}\\d?", "CE\uffffL\uffddbox127", "box1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A2_T2.js
    @Test
    public void optionalDigitAfterWordQuantifierMayBeAbsent() {
        assertFinds("\\w{3}\\d?", "CELL\uffddbox127", "CEL");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A2_T3.js
    @Test
    public void exactQuantifierFindsRequiredCount() {
        assertFinds("b{2}c", "aaabbbbcccddeeeefffff", "bbc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A2_T4.js
    @Test
    public void exactQuantifierDoesNotMatchInsufficientCount() {
        assertDoesNotFind("b{8}", "aaabbbbcccddeeeefffff");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T5.js
    @Test
    public void plusQuantifierWithLettersAndDigitsFindsFirstAdjacentRun() {
        assertFinds("[a-z]+\\d+", "x 2 ff 55 x2 as1 z12 abc12.0", "x2");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T6.js
    @Test
    public void plusQuantifierWithLettersAndDigitsFindsLongRun() {
        assertFinds("[a-z]+\\d+", "__abc123.0", "abc123");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T9.js
    @Test
    public void plusQuantifierConsumesRepeatedLiteralRun() {
        assertFinds("d+", "abcdddddefg", "ddddd");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T10.js
    @Test
    public void plusQuantifierDoesNotMatchAbsentLiteral() {
        assertDoesNotFind("o+", "abcdefg");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T11.js
    @Test
    public void plusQuantifierMatchesSingleLiteral() {
        assertFinds("d+", "abcdefg", "d");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T14.js
    @Test
    public void adjacentStarAndPlusQuantifiersConsumeRun() {
        assertFinds("b*b+", "abbbbbbbc", "bbbbbbb");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T1.js
    @Test
    public void negatedCharacterClassStarCanMatchEmptyString() {
        assertFinds("[^\"]*", "\"beast\"-nickname", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T2.js
    @Test
    public void negatedQuoteClassStarMatchesUntilQuote() {
        assertFinds("[^\"]*", "alice said: \"don't\"", "alice said: ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T3.js
    @Test
    public void negatedQuoteClassStarConsumesWholeStringWithoutQuote() {
        assertFinds("[^\"]*", "before'i'start", "before'i'start");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T4.js
    @Test
    public void negatedQuoteClassStarStopsBeforeDoubleQuote() {
        assertFinds("[^\"]*", "alice \"sweep\": \"don't\"", "alice ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T5.js
    @Test
    public void negatedQuoteClassStarStopsBeforeEscapedDoubleQuoteLiteral() {
        assertFinds("[^\"]*", "alice \"sweep\": \"don't\"", "alice ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T6.js
    @Test
    public void quotedStringPatternFindsDoubleQuotedString() {
        assertFinds("[\"'][^\"']*[\"']", "alice \"sweep\": \"don't\"", "\"sweep\"");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T7.js
    @Test
    public void quotedStringPatternFindsSingleQuotedPrefix() {
        assertFinds("[\"'][^\"']*[\"']", "alice cries out: 'don't'", "'don'");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T8.js
    @Test
    public void quotedStringPatternDoesNotMatchUnclosedQuote() {
        assertDoesNotFind("[\"'][^\"']*[\"']", "alice cries out: don't");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T9.js
    @Test
    public void quotedStringPatternMatchesEmptyDoubleQuotedString() {
        assertFinds("[\"'][^\"']*[\"']", "alice cries out:\"\"", "\"\"");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T10.js
    @Test
    public void starQuantifierCanMatchEmptyAtStart() {
        assertFinds("d*", "abcddddefg", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T11.js
    @Test
    public void starQuantifierConsumesAfterPrefix() {
        assertFinds("cd*", "abcddddefg", "cdddd");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T12.js
    @Test
    public void starQuantifierCanMatchZeroRepetitionsBetweenLiterals() {
        assertFinds("cx*d", "abcdefg", "cd");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T20.js
    @Test
    public void dotStarConsumesWholeAlphanumericInput() {
        assertFinds(".*", "a1b2c3", "a1b2c3");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T21.js
    @Test
    public void starQuantifierClassDoesNotFindMissingSuffix() {
        assertDoesNotFind("[xyz]*1", "a0.b2.c3");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T4.js
    @Test
    public void optionalLiteralMatchesWhenPresent() {
        assertFinds("cd?e", "abcdef", "cde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T5.js
    @Test
    public void optionalLiteralMayBeAbsent() {
        assertFinds("cdx?e", "abcdef", "cde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T6.js
    @Test
    public void leadingOptionalLiteralMayBeAbsent() {
        assertFinds("o?pqrst", "pqrstuvw", "pqrst");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T7.js
    @Test
    public void optionalLiteralSequenceCanMatchEmptyAtStart() {
        assertFinds("x?y?z?", "abcd", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T8.js
    @Test
    public void optionalLiteralSequenceMatchesRequiredLiterals() {
        assertFinds("x?ay?bz?c", "abcd", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T9.js
    @Test
    public void optionalLiteralsGreedilyConsumeBeforeRequiredLiteral() {
        assertFinds("b?b?b?b", "abbbbc", "bbbb");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T10.js
    @Test
    public void optionalLiteralsMaySkipMiddleRun() {
        assertFinds("ab?c?d?x?y?z", "123az789", "az");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T11.js
    @Test
    public void escapedOptionalQuestionLiteralsConsumeInput() {
        assertFinds("\\??\\??\\??\\??\\??", "?????", "?????");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T12.js
    @Test
    public void optionalDotsConsumeAvailableInput() {
        assertFinds(".?.?.?.?.?.?.?", "test", "test");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A6_T1.js
    @Test
    public void openUpperBoundQuantifierMatchesGreedily() {
        assertFinds("b{2,}c", "aaabbbbcccddeeeefffff", "bbbbc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A6_T2.js
    @Test
    public void openUpperBoundQuantifierDoesNotMatchInsufficientCount() {
        assertDoesNotFind("b{8,}c", "aaabbbbcccddeeeefffff");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A6_T3.js
    @Test
    public void openUpperBoundDigitQuantifierFindsDigits() {
        assertFinds("\\d{1,}", "wqe456646dsff", "456646");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A6_T6.js
    @Test
    public void adjacentQuantifiersConsumeWholeRun() {
        assertFinds("x{1,2}x{1,}", "xxxxxxx", "xxxxxxx");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A1.1_T1.js
    @Test
    public void tabEscapeMatchesTab() {
        assertFinds("\\t", "\t", "\t");
        assertFinds("\\t\\t", "a\t\tb", "\t\t");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A1.2_T1.js
    @Test
    public void lineFeedEscapeMatchesLineFeed() {
        assertFinds("\\n", "\n", "\n");
        assertFinds("\\n\\n", "a\n\nb", "\n\n");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A1.4_T1.js
    @Test
    public void formFeedEscapeMatchesFormFeed() {
        assertFinds("\\f", "\f", "\f");
        assertFinds("\\f\\f", "a\f\fb", "\f\f");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A1.5_T1.js
    @Test
    public void carriageReturnEscapeMatchesCarriageReturn() {
        assertFinds("\\r", "\r", "\r");
        assertFinds("\\r\\r", "a\r\rb", "\r\r");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/15.10.2.5-3-1.js
    @Test
    public void reversedQuantifierRangeIsRejected() {
        assertUnsupported("0{2,1}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/15.10.2.15-6-1.js
    @Test
    public void reversedCharacterRangeIsRejectedByComponentCompilation() {
        assertCompileUnsupported("[z-a]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T1.js
    @Test
    public void invalidCharacterRangeAfterReversedRangePrefixIsRejected() {
        assertCompileUnsupported("[b-ac-e]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T2.js
    @Test
    public void invalidCharacterRangeAfterValidRangePrefixIsRejected() {
        assertCompileUnsupported("[a-dc-b]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T3.js
    @Test
    public void invalidCharacterRangeAfterDigitClassEscapeIsRejected() {
        assertCompileUnsupported("[\\db-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T4.js
    @Test
    public void invalidCharacterRangeAfterNonDigitClassEscapeIsRejected() {
        assertCompileUnsupported("[\\Db-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T5.js
    @Test
    public void invalidCharacterRangeAfterWhitespaceClassEscapeIsRejected() {
        assertCompileUnsupported("[\\sb-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T6.js
    @Test
    public void invalidCharacterRangeAfterNonWhitespaceClassEscapeIsRejected() {
        assertCompileUnsupported("[\\Sb-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T7.js
    @Test
    public void invalidCharacterRangeAfterWordClassEscapeIsRejected() {
        assertCompileUnsupported("[\\wb-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T8.js
    @Test
    public void invalidCharacterRangeAfterNonWordClassEscapeIsRejected() {
        assertCompileUnsupported("[\\Wb-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T9.js
    @Test
    public void invalidCharacterRangeAfterNulEscapeIsRejected() {
        assertCompileUnsupported("[\\0b-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T10.js
    @Test
    public void invalidCharacterRangeAfterDecimalEscapeIsRejected() {
        assertCompileUnsupported("[\\10b-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T11.js
    @Test
    public void invalidCharacterRangeAfterBackspaceEscapeIsRejected() {
        assertCompileUnsupported("[\\bd-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T12.js
    @Test
    public void invalidCharacterRangeAfterNonWordBoundaryEscapeIsRejected() {
        assertCompileUnsupported("[\\Bd-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T13.js
    @Test
    public void invalidCharacterRangeAfterTabEscapeIsRejected() {
        assertCompileUnsupported("[\\td-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T14.js
    @Test
    public void invalidCharacterRangeAfterLineFeedEscapeIsRejected() {
        assertCompileUnsupported("[\\nd-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T15.js
    @Test
    public void invalidCharacterRangeAfterVerticalTabEscapeIsRejected() {
        assertCompileUnsupported("[\\vd-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T16.js
    @Test
    public void invalidCharacterRangeAfterFormFeedEscapeIsRejected() {
        assertCompileUnsupported("[\\fd-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T17.js
    @Test
    public void invalidCharacterRangeAfterCarriageReturnEscapeIsRejected() {
        assertCompileUnsupported("[\\rd-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T18.js
    @Test
    public void invalidCharacterRangeAfterControlEscapeIsRejected() {
        assertCompileUnsupported("[\\c0001d-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T19.js
    @Test
    public void invalidCharacterRangeAfterHexEscapeIsRejected() {
        assertCompileUnsupported("[\\x0061d-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T20.js
    @Test
    public void invalidCharacterRangeAfterUnicodeEscapeIsRejected() {
        assertCompileUnsupported("[\\u0061d-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T21.js
    @Test
    public void invalidCharacterRangeAfterIdentityEscapeIsRejected() {
        assertCompileUnsupported("[\\ad-G]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T22.js
    @Test
    public void invalidCharacterRangeAfterLaterValidRangePrefixIsRejected() {
        assertCompileUnsupported("[c-eb-a]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T23.js
    @Test
    public void invalidCharacterRangeBeforeDigitClassEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\d]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T24.js
    @Test
    public void invalidCharacterRangeBeforeNonDigitClassEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\D]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T25.js
    @Test
    public void invalidCharacterRangeBeforeWhitespaceClassEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\s]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T26.js
    @Test
    public void invalidCharacterRangeBeforeNonWhitespaceClassEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\S]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T27.js
    @Test
    public void invalidCharacterRangeBeforeWordClassEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\w]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T28.js
    @Test
    public void invalidCharacterRangeBeforeNonWordClassEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\W]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T29.js
    @Test
    public void invalidCharacterRangeBeforeNulEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\0]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T30.js
    @Test
    public void invalidCharacterRangeBeforeDecimalEscapeIsRejected() {
        assertCompileUnsupported("[b-G\\10]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T31.js
    @Test
    public void invalidCharacterRangeBeforeBackspaceEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\b]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T32.js
    @Test
    public void invalidCharacterRangeBeforeNonWordBoundaryEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\B]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T33.js
    @Test
    public void invalidCharacterRangeBeforeTabEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\t]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T34.js
    @Test
    public void invalidCharacterRangeBeforeLineFeedEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\n]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T35.js
    @Test
    public void invalidCharacterRangeBeforeVerticalTabEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\v]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T36.js
    @Test
    public void invalidCharacterRangeBeforeFormFeedEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\f]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T37.js
    @Test
    public void invalidCharacterRangeBeforeCarriageReturnEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\r]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T38.js
    @Test
    public void invalidCharacterRangeBeforeControlEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\c0001]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T39.js
    @Test
    public void invalidCharacterRangeBeforeHexEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\x0061]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T40.js
    @Test
    public void invalidCharacterRangeBeforeUnicodeEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\u0061]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.15_A1_T41.js
    @Test
    public void invalidCharacterRangeBeforeIdentityEscapeIsRejected() {
        assertCompileUnsupported("[d-G\\a]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T10.js
    @Test
    public void characterClassUnionWithDigitEscapeMatchesGreedily() {
        assertFinds("[a-c\\d]+", "\n\nabc324234\n", "abc324234");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T6.js
    @Test
    public void characterClassMatchesOneListedCharacter() {
        assertFinds("ab[ercst]de", "abcde", "abcde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T7.js
    @Test
    public void characterClassDoesNotMatchUnlistedCharacter() {
        assertDoesNotFind("ab[erst]de", "abcde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T8.js
    @Test
    public void characterRangeClassMatchesRun() {
        assertFinds("[d-h]+", "abcdefghijkl", "defgh");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T9.js
    @Test
    public void characterClassFollowedByWildcards() {
        assertFinds("[1234567].{2}", "abc6defghijkl", "6de");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T11.js
    @Test
    public void optionalDotCharacterClassMayBeAbsent() {
        assertFinds("ab[.]?c", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T12.js
    @Test
    public void singleCharacterClassMatchesLiteral() {
        assertFinds("a[b]c", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T13.js
    @Test
    public void negatedDigitRangeClassFindsNonDigitMiddle() {
        assertFinds("[a-z][^1-9][a-z]", "a1b  b2c  c3d  def  f4g", "def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T14.js
    @Test
    public void punctuationCharacterClassWithQuantifier() {
        assertFinds("[*&$]{3}", "123*&$abc", "*&$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T15.js
    @Test
    public void characterClassEscapesInSequence() {
        assertFinds("[\\d][\\n][^\\d]", "line1\nline2", "1\nl");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T5.js
    @Test
    public void negatedDigitRangeClassMatchesLetter() {
        assertFinds("a[^1-9]c", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T6.js
    @Test
    public void negatedSingleCharacterClassDoesNotMatchExcludedCharacter() {
        assertDoesNotFind("a[^b]c", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T7.js
    @Test
    public void negatedLetterRangeClassMatchesPunctuationRun() {
        assertFinds("[^a-z]{4}", "abc#$%def%&*@ghi", "%&*@");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/15.10.4.1-2.js
    @Test
    public void trailingEscapeIsRejected() {
        assertUnsupported("\\");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-digit-class-escape-positive-cases.js
    @Test
    public void digitClassEscapePositiveCases() {
        for (char c = '0'; c <= '9'; c++) {
            assertMatches("\\d", Character.toString(c));
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-digit-class-escape-negative-cases.js
    @Test
    public void digitClassEscapeNegativeCases() {
        assertDoesNotMatch("\\d", "a");
        assertDoesNotMatch("\\d", "Z");
        assertDoesNotMatch("\\d", "_");
        assertDoesNotMatch("\\d", "/");
        assertDoesNotMatch("\\d", ":");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-non-digit-class-escape-positive-cases.js
    @Test
    public void nonDigitClassEscapePositiveCases() {
        assertMatches("\\D", "a");
        assertMatches("\\D", "Z");
        assertMatches("\\D", "_");
        assertMatches("\\D", "/");
        assertMatches("\\D", ":");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-non-digit-class-escape-negative-cases.js
    @Test
    public void nonDigitClassEscapeNegativeCases() {
        for (char c = '0'; c <= '9'; c++) {
            assertDoesNotMatch("\\D", Character.toString(c));
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-word-class-escape-positive-cases.js
    @Test
    public void wordClassEscapePositiveCases() {
        assertMatches("\\w", "_");
        for (char c = '0'; c <= '9'; c++) {
            assertMatches("\\w", Character.toString(c));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            assertMatches("\\w", Character.toString(c));
        }
        for (char c = 'a'; c <= 'z'; c++) {
            assertMatches("\\w", Character.toString(c));
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-word-class-escape-negative-cases.js
    @Test
    public void wordClassEscapeNegativeCases() {
        assertDoesNotMatch("\\w", "-");
        assertDoesNotMatch("\\w", " ");
        assertDoesNotMatch("\\w", "/");
        assertDoesNotMatch("\\w", ".");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-non-word-class-escape-positive-cases.js
    @Test
    public void nonWordClassEscapePositiveCases() {
        assertMatches("\\W", "-");
        assertMatches("\\W", " ");
        assertMatches("\\W", "/");
        assertMatches("\\W", ".");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-non-word-class-escape-negative-cases.js
    @Test
    public void nonWordClassEscapeNegativeCases() {
        assertDoesNotMatch("\\W", "_");
        for (char c = '0'; c <= '9'; c++) {
            assertDoesNotMatch("\\W", Character.toString(c));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            assertDoesNotMatch("\\W", Character.toString(c));
        }
        for (char c = 'a'; c <= 'z'; c++) {
            assertDoesNotMatch("\\W", Character.toString(c));
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.12_A3_T5.js
    @Test
    public void wordClassEscapeRejectsNonWordCharacters() {
        String nonWord = "\f\n\r\t\u000b~`!@#$%^&*()-+={[}]|\\:;'<,>./? \"";
        for (int i = 0; i < nonWord.length(); i++) {
            assertDoesNotMatch("\\w", Character.toString(nonWord.charAt(i)));
        }

        String word = "_0123456789_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < word.length(); i++) {
            assertMatches("\\w", Character.toString(word.charAt(i)));
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.12_A4_T5.js
    @Test
    public void nonWordClassEscapeMatchesNonWordCharacters() {
        String nonWord = "\f\n\r\t\u000b~`!@#$%^&*()-+={[}]|\\:;'<,>./? \"";
        for (int i = 0; i < nonWord.length(); i++) {
            assertMatches("\\W", Character.toString(nonWord.charAt(i)));
        }

        String word = "_0123456789_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < word.length(); i++) {
            assertDoesNotMatch("\\W", Character.toString(word.charAt(i)));
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T1.js
    @Test
    public void dotMatchesSingleNonLineTerminator() {
        assertFinds("ab.de", "abcde", "abcde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T2.js
    @Test
    public void dotPlusStopsBeforeLineTerminator() {
        assertFinds(".+", "line 1\nline 2", "line 1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T3.js
    @Test
    public void dotStarAroundLiteralConsumesWholeString() {
        assertFinds(".*a.*", "this is a test", "this is a test");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T4.js
    @Test
    public void dotPlusMatchesPunctuationString() {
        assertFinds(".+", "this is a *&^%$# test", "this is a *&^%$# test");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T5.js
    @Test
    public void dotPlusMatchesDotRun() {
        assertFinds(".+", "....", "....");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T6.js
    @Test
    public void dotPlusMatchesLowercaseAlphabet() {
        assertFinds(".+", "abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T7.js
    @Test
    public void dotPlusMatchesUppercaseAlphabet() {
        assertFinds(".+", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T8.js
    @Test
    public void dotPlusMatchesKeyboardPunctuation() {
        assertFinds(".+", "`1234567890-=~!@#$%^&*()_+", "`1234567890-=~!@#$%^&*()_+");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A4_T9.js
    @Test
    public void dotPlusMatchesEscapedPunctuationInput() {
        assertFinds(".+", "|\\[{]};:\"',<>.?/", "|\\[{]};:\"',<>.?/");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A5_T1.js
    @Test
    public void caseInsensitiveCharacterClassMatchesUppercaseWord() {
        assertFinds("[a-z]+", "ABC def ghi", "ABC", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A5_T2.js
    @Test
    public void caseSensitiveCharacterClassSkipsUppercaseWord() {
        assertFinds("[a-z]+", "ABC def ghi", "def");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/regexp-class-chars.js
    @Test
    public void forwardSlashIsAllowedInCharacterClass() {
        assertMatches("[/]", "/");
        assertDoesNotMatch("[/]", "x");
        assertMatches("[//]", "/");
        assertDoesNotMatch("[//]", "x");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T2.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void capturingGroupsInDisjunction() {
        assertFinds("((a)|(ab))((c)|(bc))", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T13.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void disjunctionWithCapturingGroupBeforeAlternative() {
        assertFinds("(.)..|abc", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.5_A1_T3.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void repeatedCapturingDisjunctionQuantifier() {
        assertFinds("(aa|aabaac|ba|b|c)*", "aabaac", "aaba");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.5_A1_T4.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void nestedOptionalCapturingGroupsInRepeatedGroup() {
        assertFinds("(z)((a+)?(b+)?(c))*", "zaacbbbcac", "zaacbbbcac");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.5_A1_T5.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void quantifiedCaptureBackreference() {
        assertFinds("(a*)b\\1+", "baaaac", "b");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T14.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void disjunctionInsideCapturingGroup() {
        assertFinds(".+: gr(a|e)y", "color: grey", "color: grey");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T15.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void disjunctionWithSeveralCapturingAlternatives() {
        assertFinds("(Rob)|(Bob)|(Robert)|(Bobby)", "Hi Bob", "Bob");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T16.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void emptyCapturingAlternativeBeforeEmptyAlternative() {
        assertFinds("()|", "", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.3_A1_T17.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void emptyAlternativeBeforeEmptyCapturingAlternative() {
        assertFinds("|()", "", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A1_T1.js
    @Disabled("Capturing groups inside lookahead assertions are not supported")
    @Test
    public void lookaheadAssertion() {
        assertFinds("(?=(a+))", "baaabac", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A1_T2.js
    @Disabled("Capturing groups and backreferences around lookahead assertions are not supported")
    @Test
    public void lookaheadAssertionWithBackreference() {
        assertFinds("(?=(a+))a*b\\1", "baaabac", "aba");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A1_T3.js
    @Disabled("Capturing groups around lookahead assertions are not supported")
    @Test
    public void lookaheadAssertionAfterOptionalCaptureMatchesJavascript() {
        assertFinds("[Jj]ava([Ss]cript)?(?=\\:)", "just Javascript: the way af jedi", "Javascript");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A1_T4.js
    @Disabled("Capturing groups around lookahead assertions are not supported")
    @Test
    public void lookaheadAssertionAfterOptionalCaptureMatchesJava() {
        assertFinds("[Jj]ava([Ss]cript)?(?=\\:)", "taste of java: the cookbook ", "java");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A1_T5.js
    @Disabled("Capturing groups around lookahead assertions are not supported")
    @Test
    public void lookaheadAssertionAfterOptionalCaptureRejectsMissingColon() {
        assertDoesNotFind("[Jj]ava([Ss]cript)?(?=\\:)", "rhino is JavaScript engine");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T1.js
    @Disabled("Capturing groups and backreferences around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionWithBackreferences() {
        assertFinds("(.*?)a(?!(a+)b\\2c)\\2(.*)", "baaabaac", "baaabaac");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T2.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionMatchesJavaBeans() {
        assertFinds("Java(?!Script)([A-Z]\\w*)", "using of JavaBeans technology", "JavaBeans");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T3.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionRejectsMissingCapitalSuffix() {
        assertDoesNotFind("Java(?!Script)([A-Z]\\w*)", "using of Java language");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T4.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionRejectsJavaScripter() {
        assertDoesNotFind("Java(?!Script)([A-Z]\\w*)", "i'm a JavaScripter ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T5.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionMatchesPartialJavaScr() {
        assertFinds("Java(?!Script)([A-Z]\\w*)", "JavaScr oops ipt ", "JavaScr");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T6.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionMatchesNonComDot() {
        assertFinds("(\\.(?!com|org)|\\/)", "ah.info", ".");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T7.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionMatchesSlashAlternative() {
        assertFinds("(\\.(?!com|org)|\\/)", "ah/info", "/");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T8.js
    @Disabled("Capturing groups around negative lookahead assertions are not supported")
    @Test
    public void negativeLookaheadAssertionRejectsComDot() {
        assertDoesNotFind("(\\.(?!com|org)|\\/)", "ah.com");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T9.js
    @Test
    public void negativeLookaheadAlternativeMatchesEmptyInput() {
        assertFinds("(?!a|b)|c", "", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T10.js
    @Test
    public void negativeLookaheadAlternativeMatchesAfterRejectedPrefix() {
        assertFinds("(?!a|b)|c", "bc", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A2_T11.js
    @Test
    public void negativeLookaheadAlternativeMatchesOtherInput() {
        assertFinds("(?!a|b)|c", "d", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T1.js
    @Disabled("Capturing groups and whitespace class escapes are not supported")
    @Test
    public void nestedCapturingGroupsWithWhitespaceEscapesMatchJavascript() {
        assertFinds("([Jj]ava([Ss]cript)?)\\sis\\s(fun\\w*)", "Learning javaScript is funny, really",
                "javaScript is funny");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T2.js
    @Disabled("Capturing groups and whitespace class escapes are not supported")
    @Test
    public void nestedCapturingGroupsWithWhitespaceEscapesMatchJava() {
        assertFinds("([Jj]ava([Ss]cript)?)\\sis\\s(fun\\w*)", "Developing with Java is fun, try it",
                "Java is fun");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T3.js
    @Disabled("Capturing groups and whitespace class escapes are not supported")
    @Test
    public void nestedCapturingGroupsWithWhitespaceEscapesRejectDangerous() {
        assertDoesNotFind("([Jj]ava([Ss]cript)?)\\sis\\s(fun\\w*)",
                "Developing with JavaScript is dangerous, do not try it without assistance");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T4.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void simpleCapturingGroupMatch() {
        assertFinds("(abc)", "abc", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T5.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void multipleCapturingGroupsMatch() {
        assertFinds("a(bc)d(ef)g", "abcdefg", "abcdefg");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T1.js
    @Test
    public void anchorAssertion() {
        assertDoesNotFind("^m", "pairs\nmakes\tdouble");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T1.js
    @Test
    public void wordBoundaryAssertion() {
        assertFinds("\\Bevil\\B", "devils arise\tfor\nevil", "evil");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T1.js
    @Test
    public void plusQuantifierWithWhitespaceEscape() {
        assertFinds("\\s+java\\s+", "language  java\n", "  java\n");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T2.js
    @Test
    public void plusQuantifierWithWhitespaceEscapeFindsTabbedInput() {
        assertFinds("\\s+java\\s+", "\t java object", "\t java ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T3.js
    @Test
    public void plusQuantifierWithWhitespaceEscapeDoesNotMatchPrefixOnlyInput() {
        assertDoesNotFind("\\s+java\\s+", "\t javax package");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T4.js
    @Test
    public void plusQuantifierWithWhitespaceEscapeDoesNotMatchMissingLeadingWhitespace() {
        assertDoesNotFind("\\s+java\\s+", "java\n\nobject");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T7.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void plusQuantifierWithCapturedDigitsFindsFirstAdjacentRun() {
        assertFinds("[a-z]+(\\d+)", "x 2 ff 55 x2 as1 z12 abc12.0", "x2");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T8.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void plusQuantifierWithCapturedDigitsFindsLongRun() {
        assertFinds("[a-z]+(\\d+)", "__abc123.0", "abc123");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T12.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void adjacentCapturedPlusQuantifiersBacktrackGreedily() {
        assertFinds("(b+)(b+)(b+)", "abbbbbbbc", "bbbbbbb");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A3_T13.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void capturedPlusThenStarQuantifiersConsumeRun() {
        assertFinds("(b+)(b*)", "abbbbbbbc", "bbbbbbb");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T13.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void capturedStarAndPlusQuantifiersSplitRun() {
        assertFinds("(x*)(x+)", "xxxxxxx", "xxxxxxx");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T14.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void capturedDigitStarAndPlusQuantifiersSplitRun() {
        assertFinds("(\\d*)(\\d+)", "1234567890", "1234567890");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T15.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void capturedDigitStarBacktracksBeforeRequiredDigit() {
        assertFinds("(\\d*)\\d(\\d+)", "1234567890", "1234567890");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T16.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void capturedPlusThenStarMayLeaveEmptyStarCapture() {
        assertFinds("(x+)(x*)", "xxxxxxx", "xxxxxxx");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T17.js
    @Test
    public void starThenPlusQuantifierWithEndAnchor() {
        assertFinds("x*y+$", "xxxxxxyyyyyy", "xxxxxxyyyyyy");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T18.js
    @Test
    public void starQuantifierWithDigitAndWhitespaceClassesBeforeLiteral() {
        assertFinds("[\\d]*[\\s]*bc.", "abcdef", "bcd");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A4_T19.js
    @Test
    public void starQuantifierWithDigitAndWhitespaceClassesAfterLiteral() {
        assertFinds("bc..[\\d]*[\\s]*", "abcdef", "bcde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T1.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void optionalCapturingGroupQuantifier() {
        assertFinds("java(script)?", "state: javascript is extension of ecma script", "javascript");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T2.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void optionalCapturingGroupQuantifierMayBeAbsent() {
        assertFinds("java(script)?", "state: java and javascript are vastly different", "java");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A5_T3.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void optionalCapturingGroupQuantifierIsCaseSensitive() {
        assertDoesNotFind("java(script)?", "state: both Java and JavaScript used in web development");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A6_T4.js
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    @Test
    public void repeatedCapturingLiteralGroupWithOpenUpperBound() {
        assertFinds("(123){1,}", "123123", "123123");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.7_A6_T5.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void repeatedCapturingLiteralGroupWithBackreference() {
        assertFinds("(123){1,}x\\1", "123123x123", "123123x123");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A1.3_T1.js
    @Test
    public void verticalTabEscape() {
        assertFinds("\\v", "\u000b", "\u000b");
        assertFinds("\\v\\v", "a\u000b\u000bb", "\u000b\u000b");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A2.1_T1.js
    @Test
    public void uppercaseControlLetterEscapes() {
        for (char c = 'A'; c <= 'Z'; c++) {
            String expected = Character.toString((char) (c % 32));
            assertFinds("\\c" + c, expected, expected);
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A2.1_T2.js
    @Test
    public void lowercaseControlLetterEscapes() {
        for (char c = 'a'; c <= 'z'; c++) {
            String expected = Character.toString((char) (c % 32));
            assertFinds("\\c" + c, expected, expected);
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A3.1_T1.js
    @Test
    public void hexadecimalEscapes() {
        assertFinds("\\x00", "\u0000", "\u0000");
        assertFinds("\\x01", "\u0001", "\u0001");
        assertFinds("\\x0A", "\n", "\n");
        assertFinds("\\xFF", "\u00ff", "\u00ff");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A4.1_T1.js
    @Test
    public void unicodeEscapes() {
        assertFinds("\\u0000", "\u0000", "\u0000");
        assertFinds("\\u0001", "\u0001", "\u0001");
        assertFinds("\\u000A", "\n", "\n");
        assertFinds("\\u00FF", "\u00ff", "\u00ff");
        assertFinds("\\u0FFF", "\u0fff", "\u0fff");
        assertFinds("\\uFFFF", "\uffff", "\uffff");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A5.1_T1.js
    @Disabled("General identity escapes beyond the supported syntax-escape subset are not supported")
    @Test
    public void nonIdentifierSyntaxEscapes() {
        assertFinds("\\,", ",", ",");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T1.js
    @Test
    public void nulEscape() {
        assertFinds("\\0", "\u0000", "\u0000");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T4.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void decimalBackreferenceAfterCapture() {
        assertFinds("(A)\\1", "AA", "AA");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T5.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void decimalEscapeBeforeCapture() {
        assertFinds("\\1(A)", "AA", "A");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T6.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void decimalBackreferencesAfterTwoCaptures() {
        assertFinds("(A)\\1(B)\\2", "AABB", "AABB");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T7.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void decimalBackreferenceBeforeFirstCapture() {
        assertFinds("\\1(A)(B)\\2", "ABB", "ABB");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T8.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void tenNestedCapturesWithForwardBackreferences() {
        assertFinds("((((((((((A))))))))))\\1\\2\\3\\4\\5\\6\\7\\8\\9\\10", "AAAAAAAAAAA", "AAAAAAAAAAA");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.11_A1_T9.js
    @Disabled("Capturing groups and backreferences inside user-written regular-expression elements are not supported")
    @Test
    public void tenNestedCapturesWithReverseBackreferences() {
        assertFinds("((((((((((A))))))))))\\10\\9\\8\\7\\6\\5\\4\\3\\2\\1", "AAAAAAAAAAA", "AAAAAAAAAAA");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T1.js
    @Test
    public void emptyCharacterClass() {
        assertDoesNotFind("[]a", "\u0000a\u0000a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T2.js
    @Test
    public void trailingEmptyCharacterClass() {
        assertDoesNotFind("a[]", "\u0000a\u0000a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T3.js
    @Test
    public void characterClassFollowedByLookahead() {
        assertFinds("q[ax-zb](?=\\s+)", "qYqy ", "qy");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T4.js
    @Test
    public void characterClassFollowedByLookaheadWithLaterMatch() {
        assertFinds("q[ax-zb](?=\\s+)", "tqaqy ", "qy");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T5.js
    @Test
    public void characterClassFollowedByLookaheadWithTabMatch() {
        assertFinds("q[ax-zb](?=\\s+)", "tqa\t  qy ", "qa");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A1_T17.js
    @Test
    public void emptyCharacterClassDoesNotMatchMixedInput() {
        assertDoesNotFind("[]", "a[b\n[]\tc]d");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T1.js
    @Test
    public void emptyNegatedCharacterClass() {
        assertFinds("[^]a", "a\naba", "\na", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T2.js
    @Test
    public void emptyNegatedCharacterClassAfterLiteral() {
        assertFinds("a[^]", "   a\t\n", "a\t");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T3.js
    @Test
    public void negatedRangeClassFollowedByWhitespaceEscape() {
        assertFinds("a[^b-z]\\s+", "ab an az aY n", "aY ");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T4.js
    @Test
    public void negatedBackspaceCharacterClass() {
        assertFinds("[^\\b]+", "easy\bto\u0008ride", "easy");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A2_T8.js
    @Test
    public void emptyNegatedCharacterClassMatchesFirstCharacter() {
        assertFinds("[^]", "abc#$%def%&*@ghi", "a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A3_T1.js
    @Test
    public void backspaceCharacterClass() {
        assertFinds(".[\\b].", "abc\bdef", "c\bd");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A3_T2.js
    @Test
    public void repeatedBackspaceCharacterClass() {
        assertFinds("c[\\b]{3}d", "abc\b\b\bdef", "c\b\b\bd");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A3_T3.js
    @Test
    public void negatedBracketAndBackspaceClassStopsAtBackspace() {
        assertFinds("[^\\[\\b\\]]+", "abc\bdef", "abc");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.13_A3_T4.js
    @Test
    public void negatedBracketAndBackspaceClassMatchesPlainInput() {
        assertFinds("[^\\[\\b\\]]+", "abcdef", "abcdef");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-whitespace-class-escape-positive-cases.js
    @Test
    public void whitespaceClassEscapePositiveCases() {
        assertMatches("\\s", " ");
        assertMatches("\\s", "\n");
        assertMatches("\\s", "\t");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-whitespace-class-escape-negative-cases.js
    @Test
    public void whitespaceClassEscapeNegativeCases() {
        assertDoesNotMatch("\\s", "a");
        assertDoesNotMatch("\\s", "0");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-non-whitespace-class-escape-positive-cases.js
    @Test
    public void nonWhitespaceClassEscapePositiveCases() {
        assertMatches("\\S", "a");
        assertMatches("\\S", "0");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/CharacterClassEscapes/character-class-non-whitespace-class-escape-negative-cases.js
    @Test
    public void nonWhitespaceClassEscapeNegativeCases() {
        assertDoesNotMatch("\\S", " ");
        assertDoesNotMatch("\\S", "\n");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/duplicate-named-capturing-groups-syntax.js
    @Disabled("Duplicate named-group early-error validation is not implemented")
    @Test
    public void duplicateNamedGroupsSyntax() {
        assertUnsupported("(?<x>a)(?<x>b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/dotall/with-dotall.js
    @Disabled("The processor does not model JavaScript flags such as dotAll")
    @Test
    public void dotAllFlag() {
        assertFinds(".", "\n", "\n", Pattern.DOTALL);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/nullable-quantifier.js
    @Disabled("The source uses a capturing group inside a quantified atom, which is not supported")
    @Test
    public void nullableQuantifier() {
        assertFinds("(a?b??)*", "ab", "ab");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/quantifier-integer-limit.js
    @Disabled("Quantifier bounds beyond the Java int range are not supported")
    @Test
    public void quantifierIntegerLimit() {
        assertDoesNotFind("b{9007199254740991}", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/character-class-escape-non-whitespace-u180e.js
    @Test
    public void test262CharacterClassEscapeNonWhitespaceU180e() {
        assertMatches("\\S+", "\u180e");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/character-class-escape-non-whitespace.js
    @Test
    public void test262CharacterClassEscapeNonWhitespace() {
        Pattern pattern = compileProcessed("\\S+");
        for (int codePoint = 0; codePoint < 0x10000; codePoint++) {
            if (codePoint == 0x180e || codePoint == 0xfeff) {
                continue;
            }

            String input = Character.toString((char) codePoint);
            int currentCodePoint = codePoint;
            if (isTest262WhitespaceCodePoint(currentCodePoint)) {
                assertFalse(pattern.matcher(input).matches(),
                        () -> "\\S+ should not match whitespace charCode " + currentCodePoint);
            } else {
                assertTrue(pattern.matcher(input).matches(),
                        () -> "\\S+ should match non-whitespace charCode " + currentCodePoint);
            }
        }
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/dotall/with-dotall-unicode.js
    @Test
    @Disabled("The processor does not model JavaScript flags such as dotAll")
    public void test262DotallWithDotallUnicode() {
        assertUnsupported("^.$");
        assertUnsupported("^.$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/dotall/without-dotall-unicode.js
    @Test
    @Disabled("The processor does not model JavaScript flags such as dotAll")
    public void test262DotallWithoutDotallUnicode() {
        assertUnsupported("^.$");
        assertUnsupported("^.$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/dotall/without-dotall.js
    @Test
    @Disabled("The processor does not model JavaScript flags such as dotAll")
    public void test262DotallWithoutDotall() {
        assertUnsupported("^.$");
        assertUnsupported("^.$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/u180e.js
    @Test
    public void test262U180e() {
        assertDoesNotMatch("\\s+", "\u180e");
        assertMatches("\\S+", "\u180e");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_character_class_backspace_escape.js
    @Test
    public void test262UnicodeCharacterClassBackspaceEscape() {
        assertMatches("[\\b]", "\u0008");
        assertMatches("[\\b-A]", "A");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_full_case_folding.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeFullCaseFolding() {
        assertUnsupported("[\\u0390]");
        assertUnsupported("[\\u1fd3]");
        assertUnsupported("[\\u03b0]");
        assertUnsupported("[\\u1fe3]");
        assertUnsupported("[\\ufb05]");
        assertUnsupported("[\\ufb06]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_identity_escape.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeIdentityEscape() {
        assertUnsupported("\\^");
        assertUnsupported("\\$");
        assertUnsupported("\\\\");
        assertUnsupported("\\.");
        assertUnsupported("\\*");
        assertUnsupported("\\+");
        assertUnsupported("\\?");
        assertUnsupported("\\(");
        assertUnsupported("\\)");
        assertUnsupported("\\[");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_brackets.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedBrackets() {
        assertUnsupported("(");
        assertUnsupported(")");
        assertUnsupported("[");
        assertUnsupported("]");
        assertUnsupported("{");
        assertUnsupported("}");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_character_class_escape.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedCharacterClassEscape() {
        assertUnsupported("[\\d-a]");
        assertUnsupported("[\\D-a]");
        assertUnsupported("[\\s-a]");
        assertUnsupported("[\\S-a]");
        assertUnsupported("[\\w-a]");
        assertUnsupported("[\\W-a]");
        assertUnsupported("[a-\\d]");
        assertUnsupported("[a-\\D]");
        assertUnsupported("[a-\\s]");
        assertUnsupported("[a-\\S]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedIdentityEscape() {
        assertUnsupported("\\");
        assertUnsupported("[\\");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_alpha.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedIdentityEscapeAlpha() {
        assertUnsupported("\\");
        assertUnsupported("[\\");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_c.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedIdentityEscapeC() {
        assertUnsupported("\\c");
        assertUnsupported("\\c");
        assertUnsupported("[\\c]");
        assertUnsupported("[\\c");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_u.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedIdentityEscapeU() {
        assertUnsupported("\\u");
        assertUnsupported("\\u1");
        assertUnsupported("\\u12");
        assertUnsupported("\\u123");
        assertUnsupported("\\u{");
        assertUnsupported("\\u{}");
        assertUnsupported("\\u{1");
        assertUnsupported("\\u{12");
        assertUnsupported("\\u{123");
        assertUnsupported("[\\u]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_identity_escape_x.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedIdentityEscapeX() {
        assertUnsupported("\\x");
        assertUnsupported("\\x1");
        assertUnsupported("[\\x]");
        assertUnsupported("[\\x1]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_incomplete_quantifier.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedIncompleteQuantifier() {
        assertUnsupported("a{");
        assertUnsupported("a{1");
        assertUnsupported("a{1,");
        assertUnsupported("a{1,2");
        assertUnsupported("{");
        assertUnsupported("{1");
        assertUnsupported("{1,");
        assertUnsupported("{1,2");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_octal_escape.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedOctalEscape() {
        assertUnsupported("\\1");
        assertUnsupported("\\2");
        assertUnsupported("\\3");
        assertUnsupported("\\4");
        assertUnsupported("\\5");
        assertUnsupported("\\6");
        assertUnsupported("\\7");
        assertUnsupported("\\8");
        assertUnsupported("\\9");
        assertUnsupported("[\\1]");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_quantifiable_assertion.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedQuantifiableAssertion() {
        assertUnsupported("(?=.)*");
        assertUnsupported("(?=.)+");
        assertUnsupported("(?=.)?");
        assertUnsupported("(?=.){1}");
        assertUnsupported("(?=.){1,}");
        assertUnsupported("(?=.){1,2}");
        assertUnsupported("(?=.)*?");
        assertUnsupported("(?=.)+?");
        assertUnsupported("(?=.)??");
        assertUnsupported("(?=.){1}?");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/unicode_restricted_quantifier_without_atom.js
    @Test
    @Disabled("Unicode-mode escape restrictions are not currently modeled")
    public void test262UnicodeRestrictedQuantifierWithoutAtom() {
        assertUnsupported("*");
        assertUnsupported("+");
        assertUnsupported("?");
        assertUnsupported("{1}");
        assertUnsupported("{1,}");
        assertUnsupported("{1,2}");
        assertUnsupported("*?");
        assertUnsupported("+?");
        assertUnsupported("??");
        assertUnsupported("{1}?");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A3.1_T2.js
    @Test
    @Disabled("Hexadecimal and Unicode escape matching is not currently supported")
    public void test262S1510210A31T2() {
        assertUnsupported("\\x41");
        assertUnsupported("\\x42");
        assertUnsupported("\\x43");
        assertUnsupported("\\x44");
        assertUnsupported("\\x45");
        assertUnsupported("\\x56");
        assertUnsupported("\\x57");
        assertUnsupported("\\x58");
        assertUnsupported("\\x59");
        assertUnsupported("\\x5A");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A4.1_T2.js
    @Test
    @Disabled("Hexadecimal and Unicode escape matching is not currently supported")
    public void test262S1510210A41T2() {
        assertUnsupported("\\u0041");
        assertUnsupported("\\u0042");
        assertUnsupported("\\u0043");
        assertUnsupported("\\u0044");
        assertUnsupported("\\u0045");
        assertUnsupported("\\u0056");
        assertUnsupported("\\u0057");
        assertUnsupported("\\u0058");
        assertUnsupported("\\u0059");
        assertUnsupported("\\u005A");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.10_A4.1_T3.js
    @Test
    @Disabled("Hexadecimal and Unicode escape matching is not currently supported")
    public void test262S1510210A41T3() {
        assertUnsupported("\\u0410");
        assertUnsupported("\\u0411");
        assertUnsupported("\\u0412");
        assertUnsupported("\\u0413");
        assertUnsupported("\\u0414");
        assertUnsupported("\\u042C");
        assertUnsupported("\\u042D");
        assertUnsupported("\\u042E");
        assertUnsupported("\\u042F");
        assertUnsupported("\\u0401");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A1_T1.js
    @Test
    public void test262S151026A1T1() {
        assertDoesNotFind("s$", "pairs\nmakes\tdouble");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A1_T2.js
    @Test
    public void test262S151026A1T2() {
        assertFinds("e$", "pairs\nmakes\tdouble", "e");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A1_T3.js
    @Test
    public void test262S151026A1T3() {
        assertFinds("s$", "pairs\nmakes\tdouble", "s", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A1_T4.js
    @Test
    public void test262S151026A1T4() {
        assertFinds("[^e]$", "pairs\nmakes\tdouble", "s", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A1_T5.js
    @Test
    public void test262S151026A1T5() {
        assertFinds("es$", "pairs\nmakes\tdoubles", "es", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T10.js
    @Test
    public void test262S151026A2T10() {
        assertFinds("^\\d+", "abc\n123xyz", "123", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T2.js
    @Test
    public void test262S151026A2T2() {
        assertFinds("^m", "pairs\nmakes\tdouble", "m", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T3.js
    @Test
    public void test262S151026A2T3() {
        assertFinds("^p[a-z]", "pairs\nmakes\tdouble\npesos", "pa");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T4.js
    @Test
    public void test262S151026A2T4() {
        assertFinds("^p[b-z]", "pairs\nmakes\tdouble\npesos", "pe", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T5.js
    @Test
    public void test262S151026A2T5() {
        assertFinds("^[^p]", "pairs\nmakes\tdouble\npesos", "m", Pattern.MULTILINE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T6.js
    @Test
    public void test262S151026A2T6() {
        assertFinds("^ab", "abcde", "ab");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T7.js
    @Test
    public void test262S151026A2T7() {
        assertDoesNotFind("^..^e", "ab\ncde");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T8.js
    @Test
    public void test262S151026A2T8() {
        assertDoesNotFind("^xxx", "yyyyy");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A2_T9.js
    @Test
    public void test262S151026A2T9() {
        assertFinds("^\\^+", "^^^x", "^^^");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T1.js
    @Test
    public void test262S151026A3T1() {
        assertFinds("\\bp", "pilot\nsoviet robot\topenoffice", "p");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T10.js
    @Test
    public void test262S151026A3T10() {
        assertFinds("\\brobot\\b", "pilot\nsoviet robot\topenoffice", "robot");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T11.js
    @Test
    public void test262S151026A3T11() {
        assertFinds("\\b\\w{5}\\b", "pilot\nsoviet robot\topenoffice", "pilot");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T12.js
    @Test
    public void test262S151026A3T12() {
        assertFinds("\\bop", "pilot\nsoviet robot\topenoffice", "op");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T13.js
    @Test
    public void test262S151026A3T13() {
        assertDoesNotFind("op\\b", "pilot\nsoviet robot\topenoffice");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T14.js
    @Test
    public void test262S151026A3T14() {
        assertFinds("e\\b", "pilot\nsoviet robot\topenoffic\u0065", "e");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T15.js
    @Test
    public void test262S151026A3T15() {
        assertDoesNotFind("\\be", "pilot\nsoviet robot\topenoffic\u0065");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T2.js
    @Test
    public void test262S151026A3T2() {
        assertFinds("ot\\b", "pilot\nsoviet robot\topenoffice", "ot");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T3.js
    @Test
    public void test262S151026A3T3() {
        assertDoesNotFind("\\bot", "pilot\nsoviet robot\topenoffice");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T4.js
    @Test
    public void test262S151026A3T4() {
        assertFinds("\\bso", "pilot\nsoviet robot\topenoffice", "so");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T5.js
    @Test
    public void test262S151026A3T5() {
        assertDoesNotFind("so\\b", "pilot\nsoviet robot\topenoffice");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T6.js
    @Test
    public void test262S151026A3T6() {
        assertFinds("[^o]t\\b", "pilOt\nsoviet robot\topenoffice", "Ot");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T7.js
    @Test
    public void test262S151026A3T7() {
        assertFinds("[^o]t\\b", "pilOt\nsoviet robot\topenoffice", "et", Pattern.CASE_INSENSITIVE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T8.js
    @Test
    public void test262S151026A3T8() {
        assertFinds("\\bro", "pilot\nsoviet robot\topenoffice", "ro");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A3_T9.js
    @Test
    public void test262S151026A3T9() {
        assertDoesNotFind("r\\b", "pilot\nsoviet robot\topenoffice");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T2.js
    @Test
    public void test262S151026A4T2() {
        assertFinds("[f-z]e\\B", "devils arise\tfor\nrevil", "re");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T3.js
    @Test
    public void test262S151026A4T3() {
        assertFinds("\\Bo\\B", "devils arise\tfOr\nrevil", "O", Pattern.CASE_INSENSITIVE);
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T4.js
    @Test
    public void test262S151026A4T4() {
        assertFinds("\\B\\w\\B", "devils arise\tfor\nrevil", "e");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T5.js
    @Test
    public void test262S151026A4T5() {
        assertFinds("\\w\\B", "devils arise\tfor\nrevil", "d");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T6.js
    @Test
    public void test262S151026A4T6() {
        assertFinds("\\B\\w", "devils arise\tfor\nrevil", "e");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T7.js
    @Test
    public void test262S151026A4T7() {
        assertFinds("\\B[^z]{4}\\B", "devil arise\tforzzx\nevils", "il a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A4_T8.js
    @Test
    public void test262S151026A4T8() {
        assertFinds("\\B\\w{4}\\B", "devil arise\tforzzx\nevils", "orzz");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A5_T1.js
    @Test
    public void test262S151026A5T1() {
        assertFinds("^^^^^^^robot$$", "robot", "robot");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A5_T2.js
    @Test
    public void test262S151026A5T2() {
        assertFinds("\\B\\B\\B\\B\\B\\Bbot\\b\\b\\b\\b\\b\\b\\b", "robot wall-e", "bot");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A6_T1.js
    @Test
    public void test262S151026A6T1() {
        assertFinds("^.*?$", "Hello World", "Hello World");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A6_T2.js
    @Test
    public void test262S151026A6T2() {
        assertFinds("^.*?", "Hello World", "");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A6_T3.js
    @Test
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    public void test262S151026A6T3() {
        assertUnsupported("^.*?(:|$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.6_A6_T4.js
    @Test
    @Disabled("Capturing groups inside user-written regular-expression elements are not supported")
    public void test262S151026A6T4() {
        assertUnsupported("^.*(:|$)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T10.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T10() {
        assertUnsupported("(\\d{3})(\\d{3})\\1\\2");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T11.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T11() {
        assertUnsupported("a(..(..)..)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T12.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T12() {
        assertUnsupported("(a(b(c)))(d(e(f)))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T13.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T13() {
        assertUnsupported("(a(b(c)))(d(e(f)))\\2\\5");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T14.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T14() {
        assertUnsupported("a(.?)b\\1c\\1d\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T15.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T15() {
        assertUnsupported("");
        assertUnsupported("((hello))");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T16.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T16() {
        assertMatches("", "hello");
        assertMatches("(?:hello)", "hello");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T17.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T17() {
        assertUnsupported("<body.*>((.*\\n?)*?)<\\/body>");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T18.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T18() {
        assertUnsupported("(\\|)([\\w\\x81-\\xff ]*)(\\|)([\\/a-z][\\w:\\/\\.]*\\.[a-z]{3,4})(\\|)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T19.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T19() {
        assertUnsupported("([\\S]+([ \\t]+[\\S]+)*)[ \\t]*=[ \\t]*[\\S]+");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T20.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T20() {
        assertUnsupported("^(A)?(A.*)$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T21.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T21() {
        assertUnsupported("^(A)?(A.*)$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T22.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T22() {
        assertUnsupported("^(A)?(A.*)$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T23.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T23() {
        assertUnsupported("(A)?(A.*)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T24.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T24() {
        assertUnsupported("(A)?(A.*)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T25.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T25() {
        assertUnsupported("(A)?(A.*)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T26.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T26() {
        assertUnsupported("(a)?a");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T27.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T27() {
        assertUnsupported("a|(b)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T28.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T28() {
        assertUnsupported("(a)?(a)");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T29.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T29() {
        assertUnsupported("^([a-z]+)*[a-z]$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T30.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T30() {
        assertUnsupported("^([a-z]+)*[a-z]$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T31.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T31() {
        assertUnsupported("^([a-z]+)*[a-z]$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T32.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T32() {
        assertUnsupported("^(([a-z]+)*[a-z]\\.)+[a-z]{2,}$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T33.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T33() {
        assertUnsupported("^(([a-z]+)*([a-z])\\.)+[a-z]{2,}$");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T6.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T6() {
        assertUnsupported("(.{3})(.{4})");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T7.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T7() {
        assertUnsupported("(aa)bcd\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T8.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T8() {
        assertUnsupported("(aa).+\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.8_A3_T9.js
    @Test
    @Disabled("Capturing-group result array behavior is outside the current processor support")
    public void test262S151028A3T9() {
        assertUnsupported("(.{2}).+\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.9_A1_T1.js
    @Test
    @Disabled("Backreferences and capture result behavior are not supported")
    public void test262S151029A1T1() {
        assertUnsupported("\\b(\\w+) \\1\\b");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.9_A1_T2.js
    @Test
    @Disabled("Backreferences and capture result behavior are not supported")
    public void test262S151029A1T2() {
        assertUnsupported("([xu]\\d{2}([A-H]{2})?)\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.9_A1_T3.js
    @Test
    @Disabled("Backreferences and capture result behavior are not supported")
    public void test262S151029A1T3() {
        assertUnsupported("([xu]\\d{2}([A-H]{2})?)\\1");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2.9_A1_T5.js
    @Test
    @Disabled("Backreferences and capture result behavior are not supported")
    public void test262S151029A1T5() {
        assertUnsupported("(a*)b\\1+");
    }

    /// Source: https://github.com/tc39/test262/blob/673e9bacbe28590f501e2dcd817aadcc31899191/test/built-ins/RegExp/S15.10.2_A1_T1.js
    @Test
    @Disabled("The XML shallow parsing test relies on large dynamically constructed ECMAScript regular expressions")
    public void test262S15102A1T1() {
        assertUnsupported("[^<]+");
        assertUnsupported("[^-]*-");
        assertUnsupported("[ \\n\\t\\r]+");
        assertUnsupported("[A-Za-z_:]|[^\\x00-\\x7F]");
        assertUnsupported("([A-Za-z_:]|[^\\x00-\\x7F])([A-Za-z0-9_:.-]|[^\\x00-\\x7F])*");
    }

    /// Returns whether a code point is listed as whitespace by the ported test262 test.
    ///
    /// @param codePoint the BMP code point
    /// @return `true` when the code point is in the test262 whitespace list
    private static boolean isTest262WhitespaceCodePoint(int codePoint) {
        return switch (codePoint) {
            case 0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x0020, 0x00a0, 0x1680,
                    0x2028, 0x2029, 0x202f, 0x205f, 0x3000 -> true;
            default -> codePoint >= 0x2000 && codePoint <= 0x200a;
        };
    }
}
