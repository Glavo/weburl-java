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
package org.glavo.url;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Objects;

/// An unchecked exception thrown when a [WebURLPattern] cannot be compiled.
///
/// This exception is used for URLPattern grammar errors, component canonicalization failures,
/// and failures reported by Java's regular expression compiler. The null-returning
/// `tryCompile` factory methods catch this type and return `null`.
///
/// @since 0.3.0
@NotNullByDefault
public final class WebURLPatternSyntaxException extends IllegalArgumentException {
    /// Serialization identifier for this exception type.
    @Serial
    private static final long serialVersionUID = 1L;

    /// Creates a syntax exception with a message.
    ///
    /// @param message the failure message
    public WebURLPatternSyntaxException(String message) {
        super(Objects.requireNonNull(message, "message"));
    }

    /// Creates a syntax exception with a message and cause.
    ///
    /// @param message the failure message
    /// @param cause the underlying cause, or `null`
    public WebURLPatternSyntaxException(String message, @Nullable Throwable cause) {
        super(Objects.requireNonNull(message, "message"), cause);
    }
}
