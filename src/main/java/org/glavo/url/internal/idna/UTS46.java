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
package org.glavo.url.internal.idna;

import org.glavo.url.internal.StringUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.Normalizer;
import java.util.ArrayList;

/// Unicode IDNA Compatibility Processing as defined by UTS #46.
@NotNullByDefault
public final class UTS46 {
    /// The ASCII Compatible Encoding prefix for IDNA labels.
    private static final String ACE_PREFIX = "xn--";
    /// Maximum DNS label length in octets.
    private static final int MAX_DNS_LABEL_LENGTH = 63;
    /// Maximum DNS name length in octets without a trailing root dot.
    private static final int MAX_DNS_NAME_LENGTH = 253;

    /// Generated Unicode and IDNA data tables.
    private static final IdnaData DATA = IdnaData.INSTANCE;

    /// Creates no instances.
    private UTS46() {
    }

    /// Converts a domain name to Unicode according to UTS #46.
    public static Result toUnicode(
            String domain,
            boolean checkHyphens,
            boolean checkBidi,
            boolean checkJoiners,
            boolean useStd3AsciiRules,
            boolean transitionalProcessing,
            boolean ignoreInvalidPunycode
    ) {
        MappedResult mapped = mapAndNormalize(domain, transitionalProcessing);
        LabelProcessingResult labels = decodePunycodeLabels(mapped.value(), ignoreInvalidPunycode);
        boolean error = mapped.error() || labels.error;
        error |= validateLabels(
                labels.labels,
                labels.decodedLabels,
                checkHyphens,
                checkBidi,
                checkJoiners,
                useStd3AsciiRules,
                transitionalProcessing,
                false,
                false
        );
        return new Result(joinLabels(labels.labels), error);
    }

    /// Converts a domain name to ASCII according to UTS #46.
    public static Result toAscii(
            String domain,
            boolean checkHyphens,
            boolean checkBidi,
            boolean checkJoiners,
            boolean useStd3AsciiRules,
            boolean transitionalProcessing,
            boolean verifyDnsLength,
            boolean ignoreInvalidPunycode
    ) {
        MappedResult mapped = mapAndNormalize(domain, transitionalProcessing);
        LabelProcessingResult labels = decodePunycodeLabels(mapped.value(), ignoreInvalidPunycode);
        boolean error = mapped.error() || labels.error;
        error |= validateLabels(
                labels.labels,
                labels.decodedLabels,
                checkHyphens,
                checkBidi,
                checkJoiners,
                useStd3AsciiRules,
                transitionalProcessing,
                true,
                verifyDnsLength
        );

        StringBuilder ascii = new StringBuilder(mapped.value().length() + labels.labels.length * ACE_PREFIX.length());
        for (int i = 0; i < labels.labels.length; i++) {
            if (i > 0) {
                ascii.append('.');
            }
            String label = labels.labels[i];
            if (StringUtils.containsNonAscii(label)) {
                @Nullable String encoded = Punycode.encode(label);
                if (encoded == null) {
                    error = true;
                    ascii.append(label);
                } else {
                    ascii.append(ACE_PREFIX).append(encoded);
                }
            } else {
                ascii.append(label);
            }
        }

        String value = ascii.toString();
        if (verifyDnsLength && !hasValidDnsLength(value)) {
            error = true;
        }
        return new Result(value, error);
    }

    /// Converts a domain name using the IDNA options selected by the WHATWG URL Standard.
    public static Result toAsciiForUrl(String domain, boolean strict) {
        return toAscii(domain, strict, true, true, strict, false, strict, false);
    }

    /// Applies UTS #46 mapping and NFC normalization.
    private static MappedResult mapAndNormalize(String domain, boolean transitionalProcessing) {
        StringBuilder mapped = new StringBuilder(domain.length());
        boolean error = false;
        for (int i = 0; i < domain.length(); ) {
            int codePoint = domain.codePointAt(i);
            if (isSurrogateCodePoint(codePoint)) {
                mapped.append('\uFFFD');
                i++;
                error = true;
                continue;
            }

            byte status = DATA.status(codePoint);
            switch (status) {
                case IdnaData.STATUS_VALID -> mapped.appendCodePoint(codePoint);
                case IdnaData.STATUS_IGNORED -> {
                }
                case IdnaData.STATUS_MAPPED -> {
                    String mapping = DATA.mapping(codePoint);
                    mapped.append(transitionalProcessing && codePoint == 0x1E9E ? "ss" : mapping);
                }
                case IdnaData.STATUS_DEVIATION -> {
                    if (transitionalProcessing) {
                        mapped.append(DATA.mapping(codePoint));
                    } else {
                        mapped.appendCodePoint(codePoint);
                    }
                }
                default -> mapped.appendCodePoint(codePoint);
            }
            i += Character.charCount(codePoint);
        }
        return new MappedResult(Normalizer.normalize(mapped, Normalizer.Form.NFC), error);
    }

    /// Decodes labels with the `xn--` prefix.
    private static LabelProcessingResult decodePunycodeLabels(String domain, boolean ignoreInvalidPunycode) {
        String[] labels = splitLabels(domain);
        boolean[] decodedLabels = new boolean[labels.length];
        boolean error = false;

        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            if (!startsWithAcePrefix(label)) {
                continue;
            }
            if (!StringUtils.isAsciiOnly(label)) {
                error = true;
                continue;
            }

            @Nullable String decoded = Punycode.decode(label.substring(ACE_PREFIX.length()));
            if (decoded == null) {
                if (!ignoreInvalidPunycode) {
                    error = true;
                }
                continue;
            }

            labels[i] = decoded;
            decodedLabels[i] = true;
            if (decoded.isEmpty() || StringUtils.isAsciiOnly(decoded)) {
                error = true;
            }
        }

        return new LabelProcessingResult(labels, decodedLabels, error);
    }

    /// Validates all labels after UTS #46 processing.
    private static boolean validateLabels(
            String @Unmodifiable [] labels,
            boolean @Unmodifiable [] decodedLabels,
            boolean checkHyphens,
            boolean checkBidi,
            boolean checkJoiners,
            boolean useStd3AsciiRules,
            boolean transitionalProcessing,
            boolean toAscii,
            boolean verifyDnsLength
    ) {
        boolean error = false;
        boolean bidiDomain = checkBidi && isBidiDomain(labels);
        for (int i = 0; i < labels.length; i++) {
            boolean finalEmptyRootLabel = i == labels.length - 1 && i > 0 && labels[i].isEmpty();
            if (labels[i].isEmpty()) {
                if ((toAscii && verifyDnsLength) || (!toAscii && !finalEmptyRootLabel)) {
                    error = true;
                }
                continue;
            }
            error |= validateLabel(
                    labels[i],
                    decodedLabels[i],
                    checkHyphens,
                    bidiDomain,
                    checkJoiners,
                    useStd3AsciiRules,
                    transitionalProcessing
            );
        }
        return error;
    }

    /// Validates one label after UTS #46 processing.
    private static boolean validateLabel(
            String label,
            boolean decodedLabel,
            boolean checkHyphens,
            boolean bidiDomain,
            boolean checkJoiners,
            boolean useStd3AsciiRules,
            boolean transitionalProcessing
    ) {
        boolean error = !Normalizer.isNormalized(label, Normalizer.Form.NFC);

        if (checkHyphens && hasInvalidHyphenPlacement(label)) {
            error = true;
        }

        if (!checkHyphens && startsWithAcePrefix(label)) {
            error = true;
        }

        if (label.indexOf('.') >= 0) {
            error = true;
        }

        int[] codePoints = toCodePoints(label);
        if (codePoints.length > 0 && DATA.isMark(codePoints[0])) {
            error = true;
        }

        boolean allowDeviation = decodedLabel || !transitionalProcessing;
        for (int i = 0; i < codePoints.length; i++) {
            int codePoint = codePoints[i];
            if (isSurrogateCodePoint(codePoint)) {
                error = true;
                continue;
            }
            byte status = DATA.status(codePoint);
            if (status == IdnaData.STATUS_DEVIATION) {
                if (!allowDeviation) {
                    error = true;
                }
            } else if (status != IdnaData.STATUS_VALID) {
                error = true;
            }

            if (useStd3AsciiRules && codePoint < 0x80 && !isStd3AsciiCodePoint(codePoint)) {
                error = true;
            }

            if (checkJoiners && (codePoint == 0x200C || codePoint == 0x200D)
                    && !hasValidContextJ(codePoints, i)) {
                error = true;
            }
        }

        if (bidiDomain && !hasValidBidiLabel(codePoints)) {
            error = true;
        }
        return error;
    }

    /// Returns whether a label has invalid hyphen placement.
    private static boolean hasInvalidHyphenPlacement(String label) {
        int length = label.length();
        return length >= 4 && label.charAt(2) == '-' && label.charAt(3) == '-'
                || label.charAt(0) == '-'
                || label.charAt(length - 1) == '-';
    }

    /// Returns whether a ZERO WIDTH JOINER or ZERO WIDTH NON-JOINER is valid in its context.
    private static boolean hasValidContextJ(int @Unmodifiable [] label, int index) {
        if (index > 0 && DATA.isVirama(label[index - 1])) {
            return true;
        }
        if (label[index] == 0x200D) {
            return false;
        }

        int before = index - 1;
        while (before >= 0 && DATA.joiningType(label[before]) == IdnaData.JOINING_TYPE_TRANSPARENT) {
            before--;
        }
        if (before < 0) {
            return false;
        }
        byte beforeJoiningType = DATA.joiningType(label[before]);
        if (beforeJoiningType != IdnaData.JOINING_TYPE_LEFT
                && beforeJoiningType != IdnaData.JOINING_TYPE_DUAL) {
            return false;
        }

        int after = index + 1;
        while (after < label.length && DATA.joiningType(label[after]) == IdnaData.JOINING_TYPE_TRANSPARENT) {
            after++;
        }
        if (after >= label.length) {
            return false;
        }
        byte afterJoiningType = DATA.joiningType(label[after]);
        return afterJoiningType == IdnaData.JOINING_TYPE_RIGHT
                || afterJoiningType == IdnaData.JOINING_TYPE_DUAL;
    }

    /// Returns whether a domain contains any RTL bidi label.
    private static boolean isBidiDomain(String @Unmodifiable [] labels) {
        for (String label : labels) {
            for (int i = 0; i < label.length(); ) {
                int codePoint = label.codePointAt(i);
                byte bidiClass = DATA.bidiClass(codePoint);
                if (bidiClass == IdnaData.BIDI_CLASS_RIGHT_TO_LEFT
                        || bidiClass == IdnaData.BIDI_CLASS_ARABIC_LETTER
                        || bidiClass == IdnaData.BIDI_CLASS_ARABIC_NUMBER) {
                    return true;
                }
                i += Character.charCount(codePoint);
            }
        }
        return false;
    }

    /// Returns whether one label satisfies RFC 5893 bidi rules.
    private static boolean hasValidBidiLabel(int @Unmodifiable [] label) {
        if (label.length == 0) {
            return false;
        }

        byte firstClass = DATA.bidiClass(label[0]);
        if (firstClass == IdnaData.BIDI_CLASS_RIGHT_TO_LEFT
                || firstClass == IdnaData.BIDI_CLASS_ARABIC_LETTER) {
            return hasValidRightToLeftBidiLabel(label);
        }
        if (firstClass == IdnaData.BIDI_CLASS_LEFT_TO_RIGHT) {
            return hasValidLeftToRightBidiLabel(label);
        }
        return false;
    }

    /// Returns whether a label that starts with `R` or `AL` satisfies bidi rules.
    private static boolean hasValidRightToLeftBidiLabel(int @Unmodifiable [] label) {
        boolean hasEuropeanNumber = false;
        boolean hasArabicNumber = false;
        for (int codePoint : label) {
            byte bidiClass = DATA.bidiClass(codePoint);
            if (!isAllowedRightToLeftBidiClass(bidiClass)) {
                return false;
            }
            hasEuropeanNumber |= bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_NUMBER;
            hasArabicNumber |= bidiClass == IdnaData.BIDI_CLASS_ARABIC_NUMBER;
        }

        if (hasEuropeanNumber && hasArabicNumber) {
            return false;
        }

        byte lastClass = lastNonSpacingMarkBidiClass(label);
        return lastClass == IdnaData.BIDI_CLASS_RIGHT_TO_LEFT
                || lastClass == IdnaData.BIDI_CLASS_ARABIC_LETTER
                || lastClass == IdnaData.BIDI_CLASS_EUROPEAN_NUMBER
                || lastClass == IdnaData.BIDI_CLASS_ARABIC_NUMBER;
    }

    /// Returns whether a label that starts with `L` satisfies bidi rules.
    private static boolean hasValidLeftToRightBidiLabel(int @Unmodifiable [] label) {
        for (int codePoint : label) {
            byte bidiClass = DATA.bidiClass(codePoint);
            if (!isAllowedLeftToRightBidiClass(bidiClass)) {
                return false;
            }
        }

        byte lastClass = lastNonSpacingMarkBidiClass(label);
        return lastClass == IdnaData.BIDI_CLASS_LEFT_TO_RIGHT
                || lastClass == IdnaData.BIDI_CLASS_EUROPEAN_NUMBER;
    }

    /// Returns the bidi class of the last code point that is not `NSM`.
    private static byte lastNonSpacingMarkBidiClass(int @Unmodifiable [] label) {
        for (int i = label.length - 1; i >= 0; i--) {
            byte bidiClass = DATA.bidiClass(label[i]);
            if (bidiClass != IdnaData.BIDI_CLASS_NONSPACING_MARK) {
                return bidiClass;
            }
        }
        return IdnaData.BIDI_CLASS_NONSPACING_MARK;
    }

    /// Returns whether a bidi class is allowed in an RTL label.
    private static boolean isAllowedRightToLeftBidiClass(byte bidiClass) {
        return bidiClass == IdnaData.BIDI_CLASS_RIGHT_TO_LEFT
                || bidiClass == IdnaData.BIDI_CLASS_ARABIC_LETTER
                || bidiClass == IdnaData.BIDI_CLASS_ARABIC_NUMBER
                || bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_NUMBER
                || bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_SEPARATOR
                || bidiClass == IdnaData.BIDI_CLASS_COMMON_SEPARATOR
                || bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_TERMINATOR
                || bidiClass == IdnaData.BIDI_CLASS_BOUNDARY_NEUTRAL
                || bidiClass == IdnaData.BIDI_CLASS_OTHER_NEUTRAL
                || bidiClass == IdnaData.BIDI_CLASS_NONSPACING_MARK;
    }

    /// Returns whether a bidi class is allowed in an LTR label.
    private static boolean isAllowedLeftToRightBidiClass(byte bidiClass) {
        return bidiClass == IdnaData.BIDI_CLASS_LEFT_TO_RIGHT
                || bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_NUMBER
                || bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_SEPARATOR
                || bidiClass == IdnaData.BIDI_CLASS_COMMON_SEPARATOR
                || bidiClass == IdnaData.BIDI_CLASS_EUROPEAN_TERMINATOR
                || bidiClass == IdnaData.BIDI_CLASS_BOUNDARY_NEUTRAL
                || bidiClass == IdnaData.BIDI_CLASS_OTHER_NEUTRAL
                || bidiClass == IdnaData.BIDI_CLASS_NONSPACING_MARK;
    }

    /// Returns whether a DNS name satisfies UTS #46 DNS length checks.
    private static boolean hasValidDnsLength(String domain) {
        String checkedDomain = domain.endsWith(".") ? domain.substring(0, domain.length() - 1) : domain;
        if (checkedDomain.isEmpty() || checkedDomain.length() > MAX_DNS_NAME_LENGTH) {
            return false;
        }
        String[] labels = splitLabels(domain);
        for (String label : labels) {
            if (label.isEmpty() || label.length() > MAX_DNS_LABEL_LENGTH) {
                return false;
            }
        }
        return true;
    }

    /// Splits a domain into dot-separated labels while preserving empty labels.
    private static String @Unmodifiable [] splitLabels(String domain) {
        ArrayList<String> labels = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < domain.length(); i++) {
            if (domain.charAt(i) == '.') {
                labels.add(domain.substring(start, i));
                start = i + 1;
            }
        }
        labels.add(domain.substring(start));
        return labels.toArray(String[]::new);
    }

    /// Joins dot-separated labels into a domain.
    private static String joinLabels(String @Unmodifiable [] labels) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                output.append('.');
            }
            output.append(labels[i]);
        }
        return output.toString();
    }

    /// Converts a label to code points.
    private static int @Unmodifiable [] toCodePoints(String label) {
        int[] codePoints = new int[label.codePointCount(0, label.length())];
        int outputIndex = 0;
        for (int i = 0; i < label.length(); ) {
            int codePoint = label.codePointAt(i);
            codePoints[outputIndex++] = codePoint;
            i += Character.charCount(codePoint);
        }
        return codePoints;
    }

    /// Returns whether a label starts with the ACE prefix.
    private static boolean startsWithAcePrefix(String label) {
        return label.length() >= ACE_PREFIX.length()
                && label.charAt(0) == 'x'
                && label.charAt(1) == 'n'
                && label.charAt(2) == '-'
                && label.charAt(3) == '-';
    }

    /// Returns whether an ASCII code point is allowed by STD3 host syntax.
    private static boolean isStd3AsciiCodePoint(int codePoint) {
        return codePoint == '-'
                || codePoint >= '0' && codePoint <= '9'
                || codePoint >= 'a' && codePoint <= 'z';
    }

    /// Returns whether a code point is a surrogate code point.
    private static boolean isSurrogateCodePoint(int codePoint) {
        return codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE;
    }

    /// Result of one UTS #46 operation.
    ///
    /// @param value the converted domain name
    /// @param error whether the operation observed at least one UTS #46 error
    public record Result(String value, boolean error) {
    }

    /// Result of the mapping and normalization phase.
    ///
    /// @param value the mapped and normalized domain name
    /// @param error whether invalid UTF-16 input was observed
    private record MappedResult(String value, boolean error) {
    }

    /// Result of punycode label decoding.
    ///
    /// @param labels        Processed labels.
    /// @param decodedLabels Whether each label was decoded from an A-label.
    /// @param error         Whether punycode processing produced an error.
    @NotNullByDefault
    private record LabelProcessingResult(String @Unmodifiable [] labels,
                                         boolean @Unmodifiable [] decodedLabels,
                                         boolean error) {
    }
}
