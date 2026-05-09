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

import org.glavo.url.internal.IDNAProcessor;
import org.jetbrains.annotations.NotNullByDefault;

/// IDNA processing profile for domain host parsing.
///
/// An IDNA profile defines the standards and processing rules used to convert a Unicode domain name to its
/// ASCII form during host parsing. The selected profile is observable in the serialized hostname for any URL
/// whose host contains non-ASCII domain labels or punycode labels.
///
/// Profile selection affects only domain hosts. It does not affect opaque hosts, IPv4 parsing, IPv6 parsing,
/// path parsing, query parsing, or fragment parsing. It also does not expose or require a particular
/// implementation provider as part of the public API.
///
/// `WebURLFactory.defaultFactory()` and the static parsing methods on `WebURL` use `defaultProfile()`.
@NotNullByDefault
public enum IDNAProfile {
    /// Uses the original IDNA 2003 standards.
    ///
    /// This profile follows [RFC 3490](https://www.rfc-editor.org/rfc/rfc3490), with
    /// [Nameprep, RFC 3491](https://www.rfc-editor.org/rfc/rfc3491), and
    /// [Punycode, RFC 3492](https://www.rfc-editor.org/rfc/rfc3492). It is provided for compatibility with
    /// software and data that expect IDNA 2003 processing and may differ from the URL Standard profile for
    /// some domain names.
    IDNA_2003,

    /// Uses Unicode IDNA Compatibility Processing with the URL Standard profile.
    ///
    /// This profile follows [Unicode Technical Standard #46](https://www.unicode.org/reports/tr46/) and the
    /// [WHATWG URL Standard IDNA algorithm](https://url.spec.whatwg.org/#idna). It uses the URL Standard's
    /// non-transitional domain-to-ASCII settings, including bidi and joiner checks.
    UTS_46,
    ;

    /// Returns the default IDNA profile for the current runtime.
    ///
    /// The default profile is inferred from the IDNA processing capabilities available to this module. It
    /// prefers `UTS_46`, the profile used by the URL Standard. If `UTS_46` is not available, it falls back to
    /// `IDNA_2003`, which provides compatibility IDNA processing.
    ///
    /// @return the inferred default IDNA profile
    public static IDNAProfile defaultProfile() {
        return UTS_46.isAvailable() ? UTS_46 : IDNA_2003;
    }

    /// Returns whether this profile can be used in the current runtime.
    ///
    /// Availability is a runtime property of the selected profile. A profile can be selected for parsing only
    /// when this method returns `true`; otherwise parsing a domain that requires that profile fails with
    /// `IllegalStateException`.
    ///
    /// @return `true` if this profile can be selected for full IDNA processing
    public boolean isAvailable() {
        return IDNAProcessor.isAvailable(this);
    }
}
