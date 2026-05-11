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

/// An unchecked exception thrown when a URL string cannot be parsed.
///
/// A `WebURLParseException` records the input string that was parsed, a stable URL Standard
/// validation error type, a human-readable reason, and the character index where the error was
/// detected when that location is known. The exception message is derived from the reason, index,
/// and input in the same general shape as `java.net.URISyntaxException`; callers that need a stable
/// programmatic classification should use [#getErrorType()] instead of parsing the message.
@NotNullByDefault
public final class WebURLParseException extends IllegalArgumentException {
    /// Serialization identifier for this exception type.
    @Serial
    private static final long serialVersionUID = 1L;

    /// A stable URL Standard validation error type.
    @NotNullByDefault
    public enum ErrorType {
        /// The generic `invalid-URL` parse failure.
        INVALID_URL("invalid-URL", "The input is not a valid URL"),

        /// The `domain-to-ASCII` validation error.
        DOMAIN_TO_ASCII("domain-to-ASCII", "Unicode ToASCII recorded an error or returned an empty string"),

        /// The `domain-invalid-code-point` validation error.
        DOMAIN_INVALID_CODE_POINT("domain-invalid-code-point", "The host contains a forbidden domain code point"),

        /// The `domain-to-Unicode` validation error.
        DOMAIN_TO_UNICODE("domain-to-Unicode", "Unicode ToUnicode recorded an error"),

        /// The `host-invalid-code-point` validation error.
        HOST_INVALID_CODE_POINT("host-invalid-code-point", "The opaque host contains a forbidden host code point"),

        /// The `IPv4-empty-part` validation error.
        IPV4_EMPTY_PART("IPv4-empty-part", "The IPv4 address ends with a dot"),

        /// The `IPv4-too-many-parts` validation error.
        IPV4_TOO_MANY_PARTS("IPv4-too-many-parts", "The IPv4 address has more than four parts"),

        /// The `IPv4-non-numeric-part` validation error.
        IPV4_NON_NUMERIC_PART("IPv4-non-numeric-part", "An IPv4 address part is not numeric"),

        /// The `IPv4-non-decimal-part` validation error.
        IPV4_NON_DECIMAL_PART("IPv4-non-decimal-part", "The IPv4 address contains hexadecimal or octal notation"),

        /// The `IPv4-out-of-range-part` validation error.
        IPV4_OUT_OF_RANGE_PART("IPv4-out-of-range-part", "An IPv4 address part is out of range"),

        /// The `IPv6-unclosed` validation error.
        IPV6_UNCLOSED("IPv6-unclosed", "The IPv6 address is missing the closing bracket"),

        /// The `IPv6-invalid-compression` validation error.
        IPV6_INVALID_COMPRESSION("IPv6-invalid-compression", "The IPv6 address begins with improper compression"),

        /// The `IPv6-too-many-pieces` validation error.
        IPV6_TOO_MANY_PIECES("IPv6-too-many-pieces", "The IPv6 address contains more than eight pieces"),

        /// The `IPv6-multiple-compression` validation error.
        IPV6_MULTIPLE_COMPRESSION("IPv6-multiple-compression", "The IPv6 address is compressed in more than one place"),

        /// The `IPv6-invalid-code-point` validation error.
        IPV6_INVALID_CODE_POINT("IPv6-invalid-code-point", "The IPv6 address contains an invalid code point"),

        /// The `IPv6-too-few-pieces` validation error.
        IPV6_TOO_FEW_PIECES("IPv6-too-few-pieces", "The uncompressed IPv6 address contains fewer than eight pieces"),

        /// The `IPv4-in-IPv6-too-many-pieces` validation error.
        IPV4_IN_IPV6_TOO_MANY_PIECES(
                "IPv4-in-IPv6-too-many-pieces",
                "The IPv6 address has too many pieces before an embedded IPv4 address"
        ),

        /// The `IPv4-in-IPv6-invalid-code-point` validation error.
        IPV4_IN_IPV6_INVALID_CODE_POINT(
                "IPv4-in-IPv6-invalid-code-point",
                "The embedded IPv4 address contains invalid syntax"
        ),

        /// The `IPv4-in-IPv6-out-of-range-part` validation error.
        IPV4_IN_IPV6_OUT_OF_RANGE_PART(
                "IPv4-in-IPv6-out-of-range-part",
                "An embedded IPv4 address part is out of range"
        ),

        /// The `IPv4-in-IPv6-too-few-parts` validation error.
        IPV4_IN_IPV6_TOO_FEW_PARTS("IPv4-in-IPv6-too-few-parts", "The embedded IPv4 address contains too few parts"),

        /// The `invalid-URL-unit` validation error.
        INVALID_URL_UNIT("invalid-URL-unit", "The input contains a code point that is not a URL unit"),

        /// The `special-scheme-missing-following-solidus` validation error.
        SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS(
                "special-scheme-missing-following-solidus",
                "The special scheme is not followed by two solidus characters"
        ),

        /// The `missing-scheme-non-relative-URL` validation error.
        MISSING_SCHEME_NON_RELATIVE_URL(
                "missing-scheme-non-relative-URL",
                "The input is missing a scheme and cannot be parsed relative to a base URL"
        ),

        /// The `invalid-reverse-solidus` validation error.
        INVALID_REVERSE_SOLIDUS("invalid-reverse-solidus", "A special URL uses a reverse solidus instead of a solidus"),

        /// The `invalid-credentials` validation error.
        INVALID_CREDENTIALS("invalid-credentials", "The input includes credentials"),

        /// The `host-missing` validation error.
        HOST_MISSING("host-missing", "The input has a special scheme but does not contain a host"),

        /// The `port-out-of-range` validation error.
        PORT_OUT_OF_RANGE("port-out-of-range", "The port is out of range"),

        /// The `port-invalid` validation error.
        PORT_INVALID("port-invalid", "The port contains invalid syntax"),

        /// The `file-invalid-Windows-drive-letter` validation error.
        FILE_INVALID_WINDOWS_DRIVE_LETTER(
                "file-invalid-Windows-drive-letter",
                "The relative file URL starts with a Windows drive letter"
        ),

        /// The `file-invalid-Windows-drive-letter-host` validation error.
        FILE_INVALID_WINDOWS_DRIVE_LETTER_HOST(
                "file-invalid-Windows-drive-letter-host",
                "The file URL host is a Windows drive letter"
        );

        /// The URL Standard validation error name.
        private final String errorName;

        /// The default human-readable parse failure reason.
        private final String reason;

        /// Creates an error type.
        ErrorType(String errorName, String reason) {
            this.errorName = errorName;
            this.reason = reason;
        }

        /// Returns the URL Standard validation error name.
        public String getErrorName() {
            return errorName;
        }

        /// Returns the default human-readable parse failure reason.
        private String reason() {
            return reason;
        }
    }

    /// The parsed input string.
    private final String input;

    /// The stable parse failure type.
    private final ErrorType errorType;

    /// The human-readable parse failure reason.
    private final String reason;

    /// The character index where the parse failure was detected, or `-1` when unknown.
    private final int index;

    /// Creates a URL parse exception with the default reason for the supplied error type.
    ///
    /// The index is a UTF-16 character index into the input string. Use `-1` when the error is not
    /// associated with a specific input position.
    ///
    /// @throws IllegalArgumentException if `index` is less than `-1`
    public WebURLParseException(String input, ErrorType errorType, int index) {
        this(input, errorType, Objects.requireNonNull(errorType, "errorType").reason(), index, null);
    }

    /// Creates a URL parse exception with an explicit reason.
    ///
    /// The index is a UTF-16 character index into the input string. Use `-1` when the error is not
    /// associated with a specific input position.
    ///
    /// @throws IllegalArgumentException if `index` is less than `-1`
    public WebURLParseException(String input, ErrorType errorType, String reason, int index) {
        this(input, errorType, reason, index, null);
    }

    /// Creates a URL parse exception with an explicit reason and cause.
    ///
    /// The index is a UTF-16 character index into the input string. Use `-1` when the error is not
    /// associated with a specific input position.
    ///
    /// @throws IllegalArgumentException if `index` is less than `-1`
    public WebURLParseException(
            String input,
            ErrorType errorType,
            String reason,
            int index,
            @Nullable Throwable cause
    ) {
        super(message(input, reason, index), cause);
        this.input = Objects.requireNonNull(input, "input");
        this.errorType = Objects.requireNonNull(errorType, "errorType");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.index = checkIndex(index);
    }

    /// Returns the parsed input string.
    public String getInput() {
        return input;
    }

    /// Returns the stable parse failure type.
    public ErrorType getErrorType() {
        return errorType;
    }

    /// Returns the URL Standard validation error name.
    public String getErrorName() {
        return errorType.getErrorName();
    }

    /// Returns a human-readable reason for the parse failure.
    public String getReason() {
        return reason;
    }

    /// Returns the character index where the parse failure was detected, or `-1` when unknown.
    ///
    /// The index is measured in UTF-16 code units, matching Java `String` indexes.
    public int getIndex() {
        return index;
    }

    /// Builds the inherited exception message.
    private static String message(String input, String reason, int index) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(reason, "reason");
        checkIndex(index);
        return index < 0 ? reason + ": " + input : reason + " at index " + index + ": " + input;
    }

    /// Checks an exception index and returns it unchanged.
    private static int checkIndex(int index) {
        if (index < -1) {
            throw new IllegalArgumentException("index must be -1 or greater");
        }
        return index;
    }
}
