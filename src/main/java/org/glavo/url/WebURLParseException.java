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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Objects;

/// An unchecked exception thrown when a URL string cannot be parsed by a [WebURLParser].
///
/// `WebURLParseException` carries structured information about the parse failure that allows
/// callers to programmatically distinguish between different categories of validation errors.
/// It extends [IllegalArgumentException] and can be used wherever that type is expected.
///
/// # Exception Contents
///
/// Each exception records:
///
/// - **[#getInput()]** — The URL string that was being parsed.
/// - **[#getErrorType()]** — A stable, programmatic classification of the failure via the
///   [ErrorType] enum. Each constant corresponds to a named validation error in the
///   [URL Standard](https://url.spec.whatwg.org/#validation-errors).
/// - **[#getReason()]** — A default or caller-supplied human-readable description of the failure.
/// - **[#getIndex()]** — The UTF-16 code unit index in the input where the error was detected,
///   or `-1` when the error is not localized to a specific character position.
///
/// The inherited exception message (from [Throwable#getMessage()]) combines the reason, index, and
/// input in a format similar to [java.net.URISyntaxException]: `reason at index n: input` when an
/// index is available, or `reason: input` otherwise.
///
/// # Handling Parse Failures
///
/// Callers should use [#getErrorType()] for programmatic decision-making instead of parsing the
/// exception message or inspecting the error name string:
///
/// ```java
/// try {
///     WebURL url = WebURL.parse(input);
/// } catch (WebURLParseException ex) {
///     switch (ex.getErrorType()) {
///         case HOST_MISSING -> System.err.println("URL has no host: " + input);
///         case PORT_OUT_OF_RANGE -> System.err.println("Port is out of range: " + input);
///         case DOMAIN_INVALID_CODE_POINT -> System.err.println("Invalid character in domain: " + input);
///         default -> System.err.println("URL parse failed: " + ex.getMessage());
///     }
/// }
/// ```
///
/// The error type also distinguishes recoverable errors ([ErrorType#isRecoverable()]) from
/// fatal errors. Recoverable errors are accepted by the default parser but rejected by the strict
/// parser obtained via [WebURLParser#getStrict()].
///
/// # Relationship to URISyntaxException
///
/// This type serves a similar purpose to `java.net.URISyntaxException` but carries standard WHATWG
/// validation error types instead of an opaque reason string. It is an unchecked exception,
/// eliminating the mandatory try-catch or throws boilerplate that `URISyntaxException` forces on
/// callers working with URLs that are valid in practice.
///
/// # Immutability
///
/// `WebURLParseException` is immutable once constructed. All getters return stable values and the
/// exception can be safely shared and re-thrown.
///
/// @see WebURLParser
/// @see WebURLParseException.ErrorType
@NotNullByDefault
public final class WebURLParseException extends IllegalArgumentException {
    /// Serialization identifier for this exception type.
    @Serial
    private static final long serialVersionUID = 1L;

    /// A stable URL Standard validation error type.
    ///
    /// Most constants correspond to the validation error names defined by the
    /// [URL Standard](https://url.spec.whatwg.org/#validation-errors). The standard's
    /// `Failure` column determines whether the error must make parsing fail; validation errors
    /// that do not require parser failure are exposed as [#isRecoverable()] errors so a strict
    /// parser can reject them explicitly.
    ///
    /// @since 0.2.0
    @NotNullByDefault
    public enum ErrorType {
        /// The generic `invalid-URL` parse failure.
        ///
        /// This is a library-level fallback used when a parser operation cannot expose a more
        /// specific URL Standard validation error. For example, a wrapper operation that receives
        /// an invalid base URL can report this type while using the exception reason to describe
        /// the failed operation.
        INVALID_URL("invalid-URL", "The input is not a valid URL"),

        /// The `domain-to-ASCII` validation error.
        ///
        /// Unicode ToASCII, as used by the URL Standard's domain-to-ASCII algorithm, recorded an
        /// error or returned the empty string. The URL Standard requires parsing to fail for this
        /// error. For example, a special URL whose host cannot be converted into a valid IDNA ASCII
        /// domain reports this type.
        DOMAIN_TO_ASCII("domain-to-ASCII", "Unicode ToASCII recorded an error or returned an empty string"),

        /// The `domain-invalid-code-point` validation error.
        ///
        /// A special URL's host contains a forbidden domain code point after host
        /// percent-decoding. The URL Standard requires parsing to fail for this error. For example,
        /// `https://exa%23mple.org` decodes the host to `exa#mple.org`, where `#` is forbidden in a
        /// domain.
        DOMAIN_INVALID_CODE_POINT("domain-invalid-code-point", "The host contains a forbidden domain code point"),

        /// The `domain-to-Unicode` validation error.
        ///
        /// Unicode ToUnicode, as used by the URL Standard's domain-to-Unicode processing, recorded
        /// an error. The URL Standard does not require URL parsing to fail for this error. For
        /// example, an invalid ASCII Compatible Encoding label such as `xn--` can fail ToUnicode
        /// validation.
        DOMAIN_TO_UNICODE("domain-to-Unicode", "Unicode ToUnicode recorded an error", true),

        /// The `host-invalid-code-point` validation error.
        ///
        /// An opaque host in a non-special URL contains a forbidden host code point. The URL
        /// Standard requires parsing to fail for this error. For example, `foo://exa[mple.org`
        /// contains `[` in an opaque host.
        HOST_INVALID_CODE_POINT("host-invalid-code-point", "The opaque host contains a forbidden host code point"),

        /// The `IPv4-empty-part` validation error.
        ///
        /// An IPv4 address ends with `U+002E` (`.`). The URL Standard does not require URL parsing
        /// to fail for this error. For example, `https://127.0.0.1./` reports this error and can
        /// still be interpreted as `127.0.0.1`.
        IPV4_EMPTY_PART("IPv4-empty-part", "The IPv4 address ends with a dot", true),

        /// The `IPv4-too-many-parts` validation error.
        ///
        /// An IPv4 address has more than four dot-separated parts. The URL Standard requires
        /// parsing to fail for this error. For example, `https://1.2.3.4.5/` has five parts.
        IPV4_TOO_MANY_PARTS("IPv4-too-many-parts", "The IPv4 address has more than four parts"),

        /// The `IPv4-non-numeric-part` validation error.
        ///
        /// An IPv4 address part is not numeric. The URL Standard requires parsing to fail for this
        /// error. For example, `https://test.42` ends in a numeric-looking host, but the `test`
        /// part cannot be parsed as an IPv4 number.
        IPV4_NON_NUMERIC_PART("IPv4-non-numeric-part", "An IPv4 address part is not numeric"),

        /// The `IPv4-non-decimal-part` validation error.
        ///
        /// An IPv4 address contains a number written in hexadecimal or octal notation. The URL
        /// Standard does not require URL parsing to fail for this error. For example,
        /// `https://127.0.0x0.1` uses hexadecimal notation for one part.
        IPV4_NON_DECIMAL_PART(
                "IPv4-non-decimal-part",
                "The IPv4 address contains hexadecimal or octal notation",
                true
        ),

        /// The `IPv4-out-of-range-part` validation error.
        ///
        /// An IPv4 address part exceeds `255`. The URL Standard requires parsing to fail when the
        /// out-of-range value cannot be represented by the IPv4 parser. For example,
        /// `https://255.255.4000.1` reports this error.
        IPV4_OUT_OF_RANGE_PART("IPv4-out-of-range-part", "An IPv4 address part is out of range"),

        /// The `IPv6-unclosed` validation error.
        ///
        /// A bracketed IPv6 address is missing the closing `U+005D` (`]`). The URL Standard
        /// requires parsing to fail for this error. For example, `https://[::1` is missing the
        /// closing bracket.
        IPV6_UNCLOSED("IPv6-unclosed", "The IPv6 address is missing the closing bracket"),

        /// The `IPv6-invalid-compression` validation error.
        ///
        /// An IPv6 address begins with improper compression. The URL Standard requires parsing to
        /// fail for this error. For example, `https://[:1]` starts with a single colon instead of a
        /// valid `::` compression marker.
        IPV6_INVALID_COMPRESSION("IPv6-invalid-compression", "The IPv6 address begins with improper compression"),

        /// The `IPv6-too-many-pieces` validation error.
        ///
        /// An IPv6 address contains more than eight 16-bit pieces. The URL Standard requires
        /// parsing to fail for this error. For example, `https://[1:2:3:4:5:6:7:8:9]` has nine
        /// pieces.
        IPV6_TOO_MANY_PIECES("IPv6-too-many-pieces", "The IPv6 address contains more than eight pieces"),

        /// The `IPv6-multiple-compression` validation error.
        ///
        /// An IPv6 address uses `::` compression in more than one place. The URL Standard requires
        /// parsing to fail for this error. For example, `https://[1::1::1]` has multiple compressed
        /// zero runs.
        IPV6_MULTIPLE_COMPRESSION("IPv6-multiple-compression", "The IPv6 address is compressed in more than one place"),

        /// The `IPv6-invalid-code-point` validation error.
        ///
        /// An IPv6 address contains a code point that is neither an ASCII hexadecimal digit nor
        /// `U+003A` (`:`), or the address unexpectedly ends. The URL Standard requires parsing to
        /// fail for this error. For example, `https://[1:2:3!:4]` contains `!`, and
        /// `https://[1:2:3:]` ends unexpectedly.
        IPV6_INVALID_CODE_POINT("IPv6-invalid-code-point", "The IPv6 address contains an invalid code point"),

        /// The `IPv6-too-few-pieces` validation error.
        ///
        /// An uncompressed IPv6 address contains fewer than eight pieces. The URL Standard requires
        /// parsing to fail for this error. For example, `https://[1:2:3]` has too few pieces and
        /// does not use `::` compression.
        IPV6_TOO_FEW_PIECES("IPv6-too-few-pieces", "The uncompressed IPv6 address contains fewer than eight pieces"),

        /// The `IPv4-in-IPv6-too-many-pieces` validation error.
        ///
        /// An IPv6 address with embedded IPv4 syntax has more than six IPv6 pieces before the IPv4
        /// address. The URL Standard requires parsing to fail for this error. For example,
        /// `https://[1:1:1:1:1:1:1:127.0.0.1]` has too many IPv6 pieces.
        IPV4_IN_IPV6_TOO_MANY_PIECES(
                "IPv4-in-IPv6-too-many-pieces",
                "The IPv6 address has too many pieces before an embedded IPv4 address"
        ),

        /// The `IPv4-in-IPv6-invalid-code-point` validation error.
        ///
        /// An IPv6 address with embedded IPv4 syntax has an empty IPv4 part, a non-ASCII-digit
        /// character in an IPv4 part, a leading zero in an IPv4 part, or too many IPv4 parts. The
        /// URL Standard requires parsing to fail for this error. For example,
        /// `https://[ffff::127.0.xyz.1]`, `https://[ffff::127.00.0.1]`, and
        /// `https://[ffff::127.0.0.1.2]` report this type.
        IPV4_IN_IPV6_INVALID_CODE_POINT(
                "IPv4-in-IPv6-invalid-code-point",
                "The embedded IPv4 address contains invalid syntax"
        ),

        /// The `IPv4-in-IPv6-out-of-range-part` validation error.
        ///
        /// An IPv6 address with embedded IPv4 syntax contains an IPv4 part greater than `255`. The
        /// URL Standard requires parsing to fail for this error. For example,
        /// `https://[ffff::127.0.0.4000]` has an out-of-range final IPv4 part.
        IPV4_IN_IPV6_OUT_OF_RANGE_PART(
                "IPv4-in-IPv6-out-of-range-part",
                "An embedded IPv4 address part is out of range"
        ),

        /// The `IPv4-in-IPv6-too-few-parts` validation error.
        ///
        /// An IPv6 address with embedded IPv4 syntax contains fewer than four IPv4 parts. The URL
        /// Standard requires parsing to fail for this error. For example,
        /// `https://[ffff::127.0.0]` has only three IPv4 parts.
        IPV4_IN_IPV6_TOO_FEW_PARTS("IPv4-in-IPv6-too-few-parts", "The embedded IPv4 address contains too few parts"),

        /// The `invalid-URL-unit` validation error.
        ///
        /// The input contains a code point that is not a URL unit. The URL Standard does not
        /// require URL parsing to fail for this error. For example, `https://example.org/>` contains
        /// `>`, `https://example.org/%s` contains a malformed percent escape, and a URL surrounded
        /// by spaces reports this type before trimming.
        INVALID_URL_UNIT("invalid-URL-unit", "The input contains a code point that is not a URL unit", true),

        /// The `special-scheme-missing-following-solidus` validation error.
        ///
        /// A special URL's scheme is not followed by `//`. The URL Standard does not require URL
        /// parsing to fail for this error. For example, `file:c:/my-secret-folder` and
        /// `https:example.org` report this type.
        SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS(
                "special-scheme-missing-following-solidus",
                "The special scheme is not followed by two solidus characters",
                true
        ),

        /// The `missing-scheme-non-relative-URL` validation error.
        ///
        /// The input does not begin with a scheme and no usable base URL is available, or the base
        /// URL has an opaque path and cannot be used as a base. The URL Standard requires parsing
        /// to fail for this error. For example, an empty input without a base URL, or an empty input
        /// relative to `mailto:user@example.org`, reports this type.
        MISSING_SCHEME_NON_RELATIVE_URL(
                "missing-scheme-non-relative-URL",
                "The input is missing a scheme and cannot be parsed relative to a base URL"
        ),

        /// The `invalid-reverse-solidus` validation error.
        ///
        /// A special URL uses `U+005C` (`\`) where a solidus (`/`) is expected. The URL Standard
        /// does not require URL parsing to fail for this error. For example,
        /// `https://example.org\path\to\file` is interpreted using `/` path separators.
        INVALID_REVERSE_SOLIDUS(
                "invalid-reverse-solidus",
                "A special URL uses a reverse solidus instead of a solidus",
                true
        ),

        /// The `invalid-credentials` validation error.
        ///
        /// The input includes a username or password before the host. The URL Standard does not
        /// require URL parsing to fail for this error. For example, `https://user@example.org` and
        /// `ssh://user@example.org` include credentials.
        INVALID_CREDENTIALS("invalid-credentials", "The input includes credentials", true),

        /// The `host-missing` validation error.
        ///
        /// The input has a special scheme but does not contain a host. The URL Standard requires
        /// parsing to fail for this error. For example, `https://#fragment`, `https://:443`, and
        /// `https://user:pass@` all lack a host.
        HOST_MISSING("host-missing", "The input has a special scheme but does not contain a host"),

        /// The `port-out-of-range` validation error.
        ///
        /// The input's port is too large to fit in the URL Standard's 16-bit unsigned port range.
        /// The URL Standard requires parsing to fail for this error. For example,
        /// `https://example.org:70000` exceeds `65535`.
        PORT_OUT_OF_RANGE("port-out-of-range", "The port is out of range"),

        /// The `port-invalid` validation error.
        ///
        /// The input's port contains invalid syntax. The URL Standard requires parsing to fail for
        /// this error. For example, `https://example.org:7z` contains a non-digit in the port.
        PORT_INVALID("port-invalid", "The port contains invalid syntax"),

        /// The `file-invalid-Windows-drive-letter` validation error.
        ///
        /// The input is a relative-URL string that starts with a Windows drive letter while the
        /// base URL has the `file` scheme. The URL Standard does not require URL parsing to fail
        /// for this error. For example, `/c:/path/to/file` relative to `file:///c:/` reports this
        /// type.
        FILE_INVALID_WINDOWS_DRIVE_LETTER(
                "file-invalid-Windows-drive-letter",
                "The relative file URL starts with a Windows drive letter",
                true
        ),

        /// The `file-invalid-Windows-drive-letter-host` validation error.
        ///
        /// A `file:` URL's host is a Windows drive letter. The URL Standard does not require URL
        /// parsing to fail for this error. For example, `file://c:` reports this type and is
        /// normalized according to the file URL parser rules.
        FILE_INVALID_WINDOWS_DRIVE_LETTER_HOST(
                "file-invalid-Windows-drive-letter-host",
                "The file URL host is a Windows drive letter",
                true
        );

        /// The URL Standard validation error name.
        private final String errorName;

        /// The default human-readable parse failure reason.
        private final String reason;

        /// Whether this error can be accepted by a non-strict parser.
        private final boolean recoverable;

        /// Creates an error type.
        ErrorType(String errorName, String reason) {
            this(errorName, reason, false);
        }

        /// Creates an error type.
        ErrorType(String errorName, String reason, boolean recoverable) {
            this.errorName = errorName;
            this.reason = reason;
            this.recoverable = recoverable;
        }

        /// Returns the URL Standard validation error name.
        @Contract(pure = true)
        public String getErrorName() {
            return errorName;
        }

        /// Returns the default human-readable parse failure reason.
        @Contract(pure = true)
        public String getReason() {
            return reason;
        }

        /// Returns whether this validation error is recoverable by a non-strict parser.
        @Contract(pure = true)
        public boolean isRecoverable() {
            return recoverable;
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
        this(input, errorType, Objects.requireNonNull(errorType, "errorType").getReason(), index, null);
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
    @Contract(pure = true)
    public String getInput() {
        return input;
    }

    /// Returns the stable parse failure type.
    @Contract(pure = true)
    public ErrorType getErrorType() {
        return errorType;
    }

    /// Returns the URL Standard validation error name.
    @Contract(pure = true)
    public String getErrorName() {
        return errorType.getErrorName();
    }

    /// Returns a human-readable reason for the parse failure.
    @Contract(pure = true)
    public String getReason() {
        return reason;
    }

    /// Returns the character index where the parse failure was detected, or `-1` when unknown.
    ///
    /// The index is measured in UTF-16 code units, matching Java `String` indexes.
    @Contract(pure = true)
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
