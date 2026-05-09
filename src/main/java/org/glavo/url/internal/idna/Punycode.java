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

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Punycode encoder and decoder for IDNA labels.
@NotNullByDefault
final class Punycode {
    /// Bootstring base.
    private static final int BASE = 36;
    /// Minimum threshold.
    private static final int T_MIN = 1;
    /// Maximum threshold.
    private static final int T_MAX = 26;
    /// Bias skew.
    private static final int SKEW = 38;
    /// Initial bias dampening factor.
    private static final int DAMP = 700;
    /// Initial bias.
    private static final int INITIAL_BIAS = 72;
    /// Initial non-basic code point.
    private static final int INITIAL_N = 0x80;
    /// Punycode delimiter.
    private static final char DELIMITER = '-';

    /// Creates no instances.
    private Punycode() {
    }

    /// Decodes a Punycode payload without the `xn--` prefix.
    static @Nullable String decode(String input) {
        StringBuilder output = new StringBuilder(input.length());
        int inputIndex = 0;
        int delimiter = input.lastIndexOf(DELIMITER);
        if (delimiter >= 0) {
            if (delimiter == 0) {
                return null;
            }
            for (int i = 0; i < delimiter; i++) {
                char c = input.charAt(i);
                if (c >= 0x80) {
                    return null;
                }
                output.append(c);
            }
            inputIndex = delimiter + 1;
        }

        int n = INITIAL_N;
        int i = 0;
        int bias = INITIAL_BIAS;
        int outputLength = output.length();

        while (inputIndex < input.length()) {
            int oldI = i;
            int w = 1;
            for (int k = BASE; ; k += BASE) {
                if (inputIndex >= input.length()) {
                    return null;
                }
                int digit = decodeDigit(input.charAt(inputIndex++));
                if (digit < 0 || digit > (Integer.MAX_VALUE - i) / w) {
                    return null;
                }
                i += digit * w;

                int threshold = threshold(k, bias);
                if (digit < threshold) {
                    break;
                }
                if (w > Integer.MAX_VALUE / (BASE - threshold)) {
                    return null;
                }
                w *= BASE - threshold;
            }

            int length = outputLength + 1;
            bias = adapt(i - oldI, length, oldI == 0);
            if (i / length > Character.MAX_CODE_POINT - n) {
                return null;
            }
            n += i / length;
            if (!isScalarValue(n)) {
                return null;
            }

            int codePointIndex = i % length;
            int charIndex = output.offsetByCodePoints(0, codePointIndex);
            output.insert(charIndex, Character.toChars(n));
            outputLength++;
            i = codePointIndex + 1;
        }

        return output.toString();
    }

    /// Encodes a Unicode label into a Punycode payload without the `xn--` prefix.
    static @Nullable String encode(String input) {
        StringBuilder output = new StringBuilder(input.length() + 8);
        int codePointCount = 0;
        int handled = 0;
        for (int index = 0; index < input.length(); ) {
            int codePoint = checkedCodePointAt(input, index);
            if (codePoint < 0) {
                return null;
            }
            if (codePoint < 0x80) {
                output.append((char) codePoint);
                handled++;
            }
            codePointCount++;
            index += Character.charCount(codePoint);
        }

        int basicCount = handled;
        if (basicCount > 0 && handled < codePointCount) {
            output.append(DELIMITER);
        }

        int n = INITIAL_N;
        int delta = 0;
        int bias = INITIAL_BIAS;

        while (handled < codePointCount) {
            int minimum = Character.MAX_CODE_POINT;
            for (int index = 0; index < input.length(); ) {
                int codePoint = input.codePointAt(index);
                if (codePoint >= n && codePoint < minimum) {
                    minimum = codePoint;
                }
                index += Character.charCount(codePoint);
            }

            int handledPlusOne = handled + 1;
            if (minimum - n > (Integer.MAX_VALUE - delta) / handledPlusOne) {
                return null;
            }
            delta += (minimum - n) * handledPlusOne;
            n = minimum;

            for (int index = 0; index < input.length(); ) {
                int codePoint = input.codePointAt(index);
                if (codePoint < n) {
                    if (delta == Integer.MAX_VALUE) {
                        return null;
                    }
                    delta++;
                } else if (codePoint == n) {
                    int q = delta;
                    for (int k = BASE; ; k += BASE) {
                        int threshold = threshold(k, bias);
                        if (q < threshold) {
                            break;
                        }
                        output.append(encodeDigit(threshold + (q - threshold) % (BASE - threshold)));
                        q = (q - threshold) / (BASE - threshold);
                    }
                    output.append(encodeDigit(q));
                    bias = adapt(delta, handledPlusOne, handled == basicCount);
                    delta = 0;
                    handled++;
                }
                index += Character.charCount(codePoint);
            }

            if (delta == Integer.MAX_VALUE || n == Character.MAX_CODE_POINT) {
                return null;
            }
            delta++;
            n++;
        }

        return output.toString();
    }

    /// Returns the code point at an index, or `-1` when the index starts an ill-formed UTF-16 sequence.
    private static int checkedCodePointAt(String input, int index) {
        char c = input.charAt(index);
        if (Character.isHighSurrogate(c)) {
            if (index + 1 >= input.length()) {
                return -1;
            }

            char low = input.charAt(index + 1);
            return Character.isLowSurrogate(low) ? Character.toCodePoint(c, low) : -1;
        }

        return Character.isLowSurrogate(c) ? -1 : c;
    }

    /// Returns whether a code point is a Unicode scalar value.
    private static boolean isScalarValue(int codePoint) {
        return codePoint >= Character.MIN_CODE_POINT
                && codePoint <= Character.MAX_CODE_POINT
                && (codePoint < Character.MIN_SURROGATE || codePoint > Character.MAX_SURROGATE);
    }

    /// Decodes one Punycode digit.
    private static int decodeDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0' + 26;
        }
        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        }
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        }
        return -1;
    }

    /// Encodes one Punycode digit.
    private static char encodeDigit(int digit) {
        return (char) (digit < 26 ? 'a' + digit : '0' + digit - 26);
    }

    /// Computes the Bootstring threshold for an encoding step.
    private static int threshold(int k, int bias) {
        if (k <= bias + T_MIN) {
            return T_MIN;
        }
        if (k >= bias + T_MAX) {
            return T_MAX;
        }
        return k - bias;
    }

    /// Adapts the Bootstring bias after encoding or decoding one code point.
    private static int adapt(int delta, int numberOfPoints, boolean firstTime) {
        delta = firstTime ? delta / DAMP : delta / 2;
        delta += delta / numberOfPoints;

        int k = 0;
        while (delta > ((BASE - T_MIN) * T_MAX) / 2) {
            delta /= BASE - T_MIN;
            k += BASE;
        }
        return k + ((BASE - T_MIN + 1) * delta) / (delta + SKEW);
    }
}
