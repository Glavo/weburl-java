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
package org.glavo.url.internal;

import org.jetbrains.annotations.NotNullByDefault;

/// ASCII helper predicates used by the WHATWG URL algorithms.
@NotNullByDefault
final class Infra {
    /// Creates no instances.
    private Infra() {
    }

    /// Returns whether the code point is an ASCII digit.
    static boolean isAsciiDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    /// Returns whether the code point is an ASCII alphabetic code point.
    static boolean isAsciiAlpha(int codePoint) {
        return (codePoint >= 'A' && codePoint <= 'Z') || (codePoint >= 'a' && codePoint <= 'z');
    }

    /// Returns whether the code point is an ASCII alphanumeric code point.
    static boolean isAsciiAlphanumeric(int codePoint) {
        return isAsciiAlpha(codePoint) || isAsciiDigit(codePoint);
    }

    /// Returns whether the code point is an ASCII hexadecimal digit.
    static boolean isAsciiHex(int codePoint) {
        return isAsciiDigit(codePoint)
                || (codePoint >= 'A' && codePoint <= 'F')
                || (codePoint >= 'a' && codePoint <= 'f');
    }
}
