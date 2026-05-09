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

import org.glavo.url.internal.UrlParser;
import org.jetbrains.annotations.NotNullByDefault;

/// IDNA processing profile for domain host parsing.
///
/// The URL Standard's domain-to-ASCII operation is observable in the serialized hostname for any URL whose
/// host contains non-ASCII domain labels or punycode labels. Profile selection affects only domain hosts.
/// It does not affect opaque hosts, IPv4 parsing, IPv6 parsing, path parsing, query parsing, or fragment
/// parsing.
///
/// `WebURLFactory.standard()` and the static parsing methods on `WebURL` use `UTS_46`.
@NotNullByDefault
public enum IDNAProfile {
    /// Uses UTS #46 non-transitional processing.
    ///
    /// This is the profile used by the URL Standard and by `WebURL` static parsing methods. It requires the
    /// optional ICU4J IDNA classes to be visible at runtime when a domain actually needs IDNA processing.
    /// ASCII domains that do not contain punycode labels use the parser's ASCII fast path and do not load
    /// ICU4J.
    UTS_46,

    /// Uses the JDK `java.net.IDN` implementation, which is based on IDNA 2003.
    ///
    /// This profile has no runtime dependencies outside `java.base`. It may differ from the URL Standard's
    /// UTS #46 non-transitional processing for some names, but it is always available on a Java runtime.
    IDNA_2003;

    /// Returns whether this profile can be used in the current runtime.
    ///
    /// `IDNA_2003` is always available. `UTS_46` is available only when the ICU4J IDNA classes can be loaded
    /// and invoked by this module.
    ///
    /// @return `true` if this profile can be selected for full IDNA processing
    public boolean isAvailable() {
        return UrlParser.isIDNAProfileAvailable(this);
    }
}
