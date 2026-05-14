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

package org.teavm.classlib.java.text;

import org.jetbrains.annotations.NotNullByDefault;
import org.teavm.interop.Import;
import org.teavm.jso.core.JSString;

/// TeaVM classlib supplement for `java.text.Normalizer` used by the website build.
///
/// TeaVM maps `org.teavm.classlib.java.*` classes to `java.*` classes. This implementation delegates to the
/// browser's ECMAScript `String.prototype.normalize` implementation.
@NotNullByDefault
public final class TNormalizer {
    /// Creates no instances.
    private TNormalizer() {
    }

    /// Normalizes text using the requested Unicode normalization form.
    ///
    /// @param src the source character sequence
    /// @param form the normalization form
    /// @return the normalized string
    public static String normalize(CharSequence src, Form form) {
        return normalize(JSString.valueOf(src.toString()), JSString.valueOf(form.name())).stringValue();
    }

    /// Returns whether text is already normalized with the requested Unicode normalization form.
    ///
    /// @param src the source character sequence
    /// @param form the normalization form
    /// @return `true` when the input is normalized
    public static boolean isNormalized(CharSequence src, Form form) {
        String input = src.toString();
        return input.equals(normalize(input, form));
    }

    /// Calls ECMAScript `String.prototype.normalize`.
    ///
    /// @param input the source string
    /// @param form the normalization form name
    /// @return the normalized string
    @Import(module = "weburlViewer", name = "normalize")
    private static native JSString normalize(JSString input, JSString form);

    /// Unicode normalization forms.
    public enum Form {
        /// Canonical decomposition.
        NFD,

        /// Canonical decomposition followed by canonical composition.
        NFC,

        /// Compatibility decomposition.
        NFKD,

        /// Compatibility decomposition followed by canonical composition.
        NFKC
    }
}
