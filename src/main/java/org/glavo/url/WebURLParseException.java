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
/// validation error name, a human-readable reason, and the character index where the error was
/// detected when that location is known. The exception message is derived from the reason, index,
/// and input in the same general shape as `java.net.URISyntaxException`; callers that need a stable
/// programmatic classification should use [#getErrorName()] instead of parsing the message.
@NotNullByDefault
public final class WebURLParseException extends IllegalArgumentException {
    /// Serialization identifier for this exception type.
    @Serial
    private static final long serialVersionUID = 1L;

    /// The generic `invalid-URL` parse failure.
    public static final String INVALID_URL = "invalid-URL";

    /// The `domain-to-ASCII` validation error.
    public static final String DOMAIN_TO_ASCII = "domain-to-ASCII";

    /// The `domain-invalid-code-point` validation error.
    public static final String DOMAIN_INVALID_CODE_POINT = "domain-invalid-code-point";

    /// The `domain-to-Unicode` validation error.
    public static final String DOMAIN_TO_UNICODE = "domain-to-Unicode";

    /// The `host-invalid-code-point` validation error.
    public static final String HOST_INVALID_CODE_POINT = "host-invalid-code-point";

    /// The `IPv4-empty-part` validation error.
    public static final String IPV4_EMPTY_PART = "IPv4-empty-part";

    /// The `IPv4-too-many-parts` validation error.
    public static final String IPV4_TOO_MANY_PARTS = "IPv4-too-many-parts";

    /// The `IPv4-non-numeric-part` validation error.
    public static final String IPV4_NON_NUMERIC_PART = "IPv4-non-numeric-part";

    /// The `IPv4-non-decimal-part` validation error.
    public static final String IPV4_NON_DECIMAL_PART = "IPv4-non-decimal-part";

    /// The `IPv4-out-of-range-part` validation error.
    public static final String IPV4_OUT_OF_RANGE_PART = "IPv4-out-of-range-part";

    /// The `IPv6-unclosed` validation error.
    public static final String IPV6_UNCLOSED = "IPv6-unclosed";

    /// The `IPv6-invalid-compression` validation error.
    public static final String IPV6_INVALID_COMPRESSION = "IPv6-invalid-compression";

    /// The `IPv6-too-many-pieces` validation error.
    public static final String IPV6_TOO_MANY_PIECES = "IPv6-too-many-pieces";

    /// The `IPv6-multiple-compression` validation error.
    public static final String IPV6_MULTIPLE_COMPRESSION = "IPv6-multiple-compression";

    /// The `IPv6-invalid-code-point` validation error.
    public static final String IPV6_INVALID_CODE_POINT = "IPv6-invalid-code-point";

    /// The `IPv6-too-few-pieces` validation error.
    public static final String IPV6_TOO_FEW_PIECES = "IPv6-too-few-pieces";

    /// The `IPv4-in-IPv6-too-many-pieces` validation error.
    public static final String IPV4_IN_IPV6_TOO_MANY_PIECES = "IPv4-in-IPv6-too-many-pieces";

    /// The `IPv4-in-IPv6-invalid-code-point` validation error.
    public static final String IPV4_IN_IPV6_INVALID_CODE_POINT = "IPv4-in-IPv6-invalid-code-point";

    /// The `IPv4-in-IPv6-out-of-range-part` validation error.
    public static final String IPV4_IN_IPV6_OUT_OF_RANGE_PART = "IPv4-in-IPv6-out-of-range-part";

    /// The `IPv4-in-IPv6-too-few-parts` validation error.
    public static final String IPV4_IN_IPV6_TOO_FEW_PARTS = "IPv4-in-IPv6-too-few-parts";

    /// The `invalid-URL-unit` validation error.
    public static final String INVALID_URL_UNIT = "invalid-URL-unit";

    /// The `special-scheme-missing-following-solidus` validation error.
    public static final String SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS = "special-scheme-missing-following-solidus";

    /// The `missing-scheme-non-relative-URL` validation error.
    public static final String MISSING_SCHEME_NON_RELATIVE_URL = "missing-scheme-non-relative-URL";

    /// The `invalid-reverse-solidus` validation error.
    public static final String INVALID_REVERSE_SOLIDUS = "invalid-reverse-solidus";

    /// The `invalid-credentials` validation error.
    public static final String INVALID_CREDENTIALS = "invalid-credentials";

    /// The `host-missing` validation error.
    public static final String HOST_MISSING = "host-missing";

    /// The `port-out-of-range` validation error.
    public static final String PORT_OUT_OF_RANGE = "port-out-of-range";

    /// The `port-invalid` validation error.
    public static final String PORT_INVALID = "port-invalid";

    /// The `file-invalid-Windows-drive-letter` validation error.
    public static final String FILE_INVALID_WINDOWS_DRIVE_LETTER = "file-invalid-Windows-drive-letter";

    /// The `file-invalid-Windows-drive-letter-host` validation error.
    public static final String FILE_INVALID_WINDOWS_DRIVE_LETTER_HOST = "file-invalid-Windows-drive-letter-host";

    /// The parsed input string.
    private final String input;

    /// The URL Standard validation error name.
    private final String errorName;

    /// The human-readable parse failure reason.
    private final String reason;

    /// The character index where the parse failure was detected, or `-1` when unknown.
    private final int index;

    /// Creates a URL parse exception with a standard reason for the supplied error name.
    ///
    /// The index is a UTF-16 character index into the input string. Use `-1` when the error is not
    /// associated with a specific input position.
    ///
    /// @throws IllegalArgumentException if `index` is less than `-1`
    public WebURLParseException(String input, String errorName, int index) {
        this(input, errorName, defaultReason(errorName), index, null);
    }

    /// Creates a URL parse exception with an explicit reason.
    ///
    /// The index is a UTF-16 character index into the input string. Use `-1` when the error is not
    /// associated with a specific input position.
    ///
    /// @throws IllegalArgumentException if `index` is less than `-1`
    public WebURLParseException(String input, String errorName, String reason, int index) {
        this(input, errorName, reason, index, null);
    }

    /// Creates a URL parse exception with an explicit reason and cause.
    ///
    /// The index is a UTF-16 character index into the input string. Use `-1` when the error is not
    /// associated with a specific input position.
    ///
    /// @throws IllegalArgumentException if `index` is less than `-1`
    public WebURLParseException(
            String input,
            String errorName,
            String reason,
            int index,
            @Nullable Throwable cause
    ) {
        super(message(input, reason, index), cause);
        this.input = Objects.requireNonNull(input, "input");
        this.errorName = Objects.requireNonNull(errorName, "errorName");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.index = checkIndex(index);
    }

    /// Returns the parsed input string.
    public String getInput() {
        return input;
    }

    /// Returns the stable URL Standard validation error name.
    public String getErrorName() {
        return errorName;
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

    /// Returns the default human-readable reason for a standard error name.
    private static String defaultReason(String errorName) {
        return switch (Objects.requireNonNull(errorName, "errorName")) {
            case INVALID_URL -> "The input is not a valid URL";
            case DOMAIN_TO_ASCII -> "Unicode ToASCII recorded an error or returned an empty string";
            case DOMAIN_INVALID_CODE_POINT -> "The host contains a forbidden domain code point";
            case DOMAIN_TO_UNICODE -> "Unicode ToUnicode recorded an error";
            case HOST_INVALID_CODE_POINT -> "The opaque host contains a forbidden host code point";
            case IPV4_EMPTY_PART -> "The IPv4 address ends with a dot";
            case IPV4_TOO_MANY_PARTS -> "The IPv4 address has more than four parts";
            case IPV4_NON_NUMERIC_PART -> "An IPv4 address part is not numeric";
            case IPV4_NON_DECIMAL_PART -> "The IPv4 address contains hexadecimal or octal notation";
            case IPV4_OUT_OF_RANGE_PART -> "An IPv4 address part is out of range";
            case IPV6_UNCLOSED -> "The IPv6 address is missing the closing bracket";
            case IPV6_INVALID_COMPRESSION -> "The IPv6 address begins with improper compression";
            case IPV6_TOO_MANY_PIECES -> "The IPv6 address contains more than eight pieces";
            case IPV6_MULTIPLE_COMPRESSION -> "The IPv6 address is compressed in more than one place";
            case IPV6_INVALID_CODE_POINT -> "The IPv6 address contains an invalid code point";
            case IPV6_TOO_FEW_PIECES -> "The uncompressed IPv6 address contains fewer than eight pieces";
            case IPV4_IN_IPV6_TOO_MANY_PIECES
                    -> "The IPv6 address has too many pieces before an embedded IPv4 address";
            case IPV4_IN_IPV6_INVALID_CODE_POINT -> "The embedded IPv4 address contains invalid syntax";
            case IPV4_IN_IPV6_OUT_OF_RANGE_PART -> "An embedded IPv4 address part is out of range";
            case IPV4_IN_IPV6_TOO_FEW_PARTS -> "The embedded IPv4 address contains too few parts";
            case INVALID_URL_UNIT -> "The input contains a code point that is not a URL unit";
            case SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS
                    -> "The special scheme is not followed by two solidus characters";
            case MISSING_SCHEME_NON_RELATIVE_URL
                    -> "The input is missing a scheme and cannot be parsed relative to a base URL";
            case INVALID_REVERSE_SOLIDUS -> "A special URL uses a reverse solidus instead of a solidus";
            case INVALID_CREDENTIALS -> "The input includes credentials";
            case HOST_MISSING -> "The input has a special scheme but does not contain a host";
            case PORT_OUT_OF_RANGE -> "The port is out of range";
            case PORT_INVALID -> "The port contains invalid syntax";
            case FILE_INVALID_WINDOWS_DRIVE_LETTER -> "The relative file URL starts with a Windows drive letter";
            case FILE_INVALID_WINDOWS_DRIVE_LETTER_HOST -> "The file URL host is a Windows drive letter";
            default -> "The input is not a valid URL";
        };
    }
}
