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

import java.io.Serial;

/// A URL parse exception corresponding to a WHATWG URL validation error.
@NotNullByDefault
public abstract sealed class WebURLParseException extends IllegalArgumentException
        permits WebURLParseException.DomainToASCII,
                WebURLParseException.DomainInvalidCodePoint,
                WebURLParseException.DomainToUnicode,
                WebURLParseException.HostInvalidCodePoint,
                WebURLParseException.IPv4EmptyPart,
                WebURLParseException.IPv4TooManyParts,
                WebURLParseException.IPv4NonNumericPart,
                WebURLParseException.IPv4NonDecimalPart,
                WebURLParseException.IPv4OutOfRangePart,
                WebURLParseException.IPv6Unclosed,
                WebURLParseException.IPv6InvalidCompression,
                WebURLParseException.IPv6TooManyPieces,
                WebURLParseException.IPv6MultipleCompression,
                WebURLParseException.IPv6InvalidCodePoint,
                WebURLParseException.IPv6TooFewPieces,
                WebURLParseException.IPv4InIPv6TooManyPieces,
                WebURLParseException.IPv4InIPv6InvalidCodePoint,
                WebURLParseException.IPv4InIPv6OutOfRangePart,
                WebURLParseException.IPv4InIPv6TooFewParts,
                WebURLParseException.InvalidURLUnit,
                WebURLParseException.SpecialSchemeMissingFollowingSolidus,
                WebURLParseException.MissingSchemeNonRelativeURL,
                WebURLParseException.InvalidReverseSolidus,
                WebURLParseException.InvalidCredentials,
                WebURLParseException.HostMissing,
                WebURLParseException.PortOutOfRange,
                WebURLParseException.PortInvalid,
                WebURLParseException.FileInvalidWindowsDriveLetter,
                WebURLParseException.FileInvalidWindowsDriveLetterHost {
    /// Serialization identifier for this exception type.
    @Serial
    private static final long serialVersionUID = 1L;

    /// The WHATWG URL validation error name.
    private final String errorName;

    /// Creates a URL parse exception with a WHATWG URL validation error name and description.
    protected WebURLParseException(String errorName, String description) {
        super(errorName + ": " + description);
        this.errorName = errorName;
    }

    /// Returns the WHATWG URL validation error name.
    public final String errorName() {
        return errorName;
    }

    /// The `domain-to-ASCII` validation error.
    @NotNullByDefault
    public static final class DomainToASCII extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `domain-to-ASCII` parse exception.
        public DomainToASCII() {
            super("domain-to-ASCII", "Unicode ToASCII recorded an error or returned an empty string.");
        }
    }

    /// The `domain-invalid-code-point` validation error.
    @NotNullByDefault
    public static final class DomainInvalidCodePoint extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `domain-invalid-code-point` parse exception.
        public DomainInvalidCodePoint() {
            super("domain-invalid-code-point", "The host contains a forbidden domain code point.");
        }
    }

    /// The `domain-to-Unicode` validation error.
    @NotNullByDefault
    public static final class DomainToUnicode extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `domain-to-Unicode` parse exception.
        public DomainToUnicode() {
            super("domain-to-Unicode", "Unicode ToUnicode recorded an error.");
        }
    }

    /// The `host-invalid-code-point` validation error.
    @NotNullByDefault
    public static final class HostInvalidCodePoint extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `host-invalid-code-point` parse exception.
        public HostInvalidCodePoint() {
            super("host-invalid-code-point", "The opaque host contains a forbidden host code point.");
        }
    }

    /// The `IPv4-empty-part` validation error.
    @NotNullByDefault
    public static final class IPv4EmptyPart extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-empty-part` parse exception.
        public IPv4EmptyPart() {
            super("IPv4-empty-part", "The IPv4 address ends with a dot.");
        }
    }

    /// The `IPv4-too-many-parts` validation error.
    @NotNullByDefault
    public static final class IPv4TooManyParts extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-too-many-parts` parse exception.
        public IPv4TooManyParts() {
            super("IPv4-too-many-parts", "The IPv4 address has more than four parts.");
        }
    }

    /// The `IPv4-non-numeric-part` validation error.
    @NotNullByDefault
    public static final class IPv4NonNumericPart extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-non-numeric-part` parse exception.
        public IPv4NonNumericPart() {
            super("IPv4-non-numeric-part", "An IPv4 address part is not numeric.");
        }
    }

    /// The `IPv4-non-decimal-part` validation error.
    @NotNullByDefault
    public static final class IPv4NonDecimalPart extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-non-decimal-part` parse exception.
        public IPv4NonDecimalPart() {
            super("IPv4-non-decimal-part", "The IPv4 address contains hexadecimal or octal notation.");
        }
    }

    /// The `IPv4-out-of-range-part` validation error.
    @NotNullByDefault
    public static final class IPv4OutOfRangePart extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-out-of-range-part` parse exception.
        public IPv4OutOfRangePart() {
            super("IPv4-out-of-range-part", "An IPv4 address part is out of range.");
        }
    }

    /// The `IPv6-unclosed` validation error.
    @NotNullByDefault
    public static final class IPv6Unclosed extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv6-unclosed` parse exception.
        public IPv6Unclosed() {
            super("IPv6-unclosed", "The IPv6 address is missing the closing bracket.");
        }
    }

    /// The `IPv6-invalid-compression` validation error.
    @NotNullByDefault
    public static final class IPv6InvalidCompression extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv6-invalid-compression` parse exception.
        public IPv6InvalidCompression() {
            super("IPv6-invalid-compression", "The IPv6 address begins with improper compression.");
        }
    }

    /// The `IPv6-too-many-pieces` validation error.
    @NotNullByDefault
    public static final class IPv6TooManyPieces extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv6-too-many-pieces` parse exception.
        public IPv6TooManyPieces() {
            super("IPv6-too-many-pieces", "The IPv6 address contains more than eight pieces.");
        }
    }

    /// The `IPv6-multiple-compression` validation error.
    @NotNullByDefault
    public static final class IPv6MultipleCompression extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv6-multiple-compression` parse exception.
        public IPv6MultipleCompression() {
            super("IPv6-multiple-compression", "The IPv6 address is compressed in more than one place.");
        }
    }

    /// The `IPv6-invalid-code-point` validation error.
    @NotNullByDefault
    public static final class IPv6InvalidCodePoint extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv6-invalid-code-point` parse exception.
        public IPv6InvalidCodePoint() {
            super("IPv6-invalid-code-point", "The IPv6 address contains an invalid code point.");
        }
    }

    /// The `IPv6-too-few-pieces` validation error.
    @NotNullByDefault
    public static final class IPv6TooFewPieces extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv6-too-few-pieces` parse exception.
        public IPv6TooFewPieces() {
            super("IPv6-too-few-pieces", "The uncompressed IPv6 address contains fewer than eight pieces.");
        }
    }

    /// The `IPv4-in-IPv6-too-many-pieces` validation error.
    @NotNullByDefault
    public static final class IPv4InIPv6TooManyPieces extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-in-IPv6-too-many-pieces` parse exception.
        public IPv4InIPv6TooManyPieces() {
            super("IPv4-in-IPv6-too-many-pieces", "The IPv6 address has too many pieces before an embedded IPv4 address.");
        }
    }

    /// The `IPv4-in-IPv6-invalid-code-point` validation error.
    @NotNullByDefault
    public static final class IPv4InIPv6InvalidCodePoint extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-in-IPv6-invalid-code-point` parse exception.
        public IPv4InIPv6InvalidCodePoint() {
            super("IPv4-in-IPv6-invalid-code-point", "The embedded IPv4 address contains invalid syntax.");
        }
    }

    /// The `IPv4-in-IPv6-out-of-range-part` validation error.
    @NotNullByDefault
    public static final class IPv4InIPv6OutOfRangePart extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-in-IPv6-out-of-range-part` parse exception.
        public IPv4InIPv6OutOfRangePart() {
            super("IPv4-in-IPv6-out-of-range-part", "An embedded IPv4 address part is out of range.");
        }
    }

    /// The `IPv4-in-IPv6-too-few-parts` validation error.
    @NotNullByDefault
    public static final class IPv4InIPv6TooFewParts extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `IPv4-in-IPv6-too-few-parts` parse exception.
        public IPv4InIPv6TooFewParts() {
            super("IPv4-in-IPv6-too-few-parts", "The embedded IPv4 address contains too few parts.");
        }
    }

    /// The `invalid-URL-unit` validation error.
    @NotNullByDefault
    public static final class InvalidURLUnit extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `invalid-URL-unit` parse exception.
        public InvalidURLUnit() {
            super("invalid-URL-unit", "The input contains a code point that is not a URL unit.");
        }
    }

    /// The `special-scheme-missing-following-solidus` validation error.
    @NotNullByDefault
    public static final class SpecialSchemeMissingFollowingSolidus extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `special-scheme-missing-following-solidus` parse exception.
        public SpecialSchemeMissingFollowingSolidus() {
            super("special-scheme-missing-following-solidus", "The special scheme is not followed by two solidus characters.");
        }
    }

    /// The `missing-scheme-non-relative-URL` validation error.
    @NotNullByDefault
    public static final class MissingSchemeNonRelativeURL extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `missing-scheme-non-relative-URL` parse exception.
        public MissingSchemeNonRelativeURL() {
            super("missing-scheme-non-relative-URL", "The input is missing a scheme and cannot be parsed relative to a base URL.");
        }
    }

    /// The `invalid-reverse-solidus` validation error.
    @NotNullByDefault
    public static final class InvalidReverseSolidus extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `invalid-reverse-solidus` parse exception.
        public InvalidReverseSolidus() {
            super("invalid-reverse-solidus", "A special URL uses a reverse solidus instead of a solidus.");
        }
    }

    /// The `invalid-credentials` validation error.
    @NotNullByDefault
    public static final class InvalidCredentials extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an `invalid-credentials` parse exception.
        public InvalidCredentials() {
            super("invalid-credentials", "The input includes credentials.");
        }
    }

    /// The `host-missing` validation error.
    @NotNullByDefault
    public static final class HostMissing extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `host-missing` parse exception.
        public HostMissing() {
            super("host-missing", "The input has a special scheme but does not contain a host.");
        }
    }

    /// The `port-out-of-range` validation error.
    @NotNullByDefault
    public static final class PortOutOfRange extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `port-out-of-range` parse exception.
        public PortOutOfRange() {
            super("port-out-of-range", "The port is out of range.");
        }
    }

    /// The `port-invalid` validation error.
    @NotNullByDefault
    public static final class PortInvalid extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `port-invalid` parse exception.
        public PortInvalid() {
            super("port-invalid", "The port contains invalid syntax.");
        }
    }

    /// The `file-invalid-Windows-drive-letter` validation error.
    @NotNullByDefault
    public static final class FileInvalidWindowsDriveLetter extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `file-invalid-Windows-drive-letter` parse exception.
        public FileInvalidWindowsDriveLetter() {
            super("file-invalid-Windows-drive-letter", "The relative file URL starts with a Windows drive letter.");
        }
    }

    /// The `file-invalid-Windows-drive-letter-host` validation error.
    @NotNullByDefault
    public static final class FileInvalidWindowsDriveLetterHost extends WebURLParseException {
        /// Serialization identifier for this exception type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a `file-invalid-Windows-drive-letter-host` parse exception.
        public FileInvalidWindowsDriveLetterHost() {
            super("file-invalid-Windows-drive-letter-host", "The file URL host is a Windows drive letter.");
        }
    }
}
