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
import org.glavo.url.internal.WebURLImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A reusable, immutable factory for WHATWG URLs.
///
/// A factory combines the URL Standard basic URL parser with a small set of caller-controlled creation
/// settings. The standard factory returned by `standard()` uses UTS #46 IDNA processing; it is the factory used
/// by the static parsing methods on `WebURL`.
///
/// A factory does not store a base URL. `parse(String)`, `tryParse(String)`, and `canParse(String)` parse only
/// absolute URL inputs. Overloads that accept a base URL use the supplied base only for that call.
///
/// The factory object is immutable and thread-safe. `Builder` is mutable and is intended to be confined to
/// the thread or construction scope that creates a factory.
@NotNullByDefault
public final class WebURLFactory {
    /// The standard factory used by `WebURL` static parsing methods.
    private static final WebURLFactory STANDARD = new WebURLFactory(IDNAProfile.UTS_46);

    /// The configured IDNA profile.
    private final IDNAProfile idnaProfile;

    /// Creates a factory from validated factory settings.
    private WebURLFactory(IDNAProfile idnaProfile) {
        this.idnaProfile = idnaProfile;
    }

    /// Returns the standard URL factory.
    ///
    /// The standard factory uses `IDNAProfile.UTS_46`. It parses only absolute URLs unless a base is
    /// supplied to an overload that accepts one. This method always returns the same immutable factory instance.
    ///
    /// @return the standard factory
    public static WebURLFactory standard() {
        return STANDARD;
    }

    /// Returns a new factory builder.
    ///
    /// The builder initially uses `IDNAProfile.UTS_46`.
    ///
    /// @return a new mutable builder
    public static Builder builder() {
        return new Builder(IDNAProfile.UTS_46);
    }

    /// Returns the configured IDNA profile.
    ///
    /// The profile controls how Unicode domain labels are converted to ASCII during domain host parsing.
    /// Opaque hosts, IPv4 hosts, and IPv6 hosts are not converted through IDNA.
    ///
    /// @return the configured IDNA profile
    public IDNAProfile idnaProfile() {
        return idnaProfile;
    }

    /// Parses an input string and returns the parsed URL.
    ///
    /// The input must be an absolute URL. Use `parse(String, String)` or `parse(String, WebURL)` to parse a
    /// relative input against an explicit base URL.
    ///
    /// @param input the URL input string
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails with a known URL validation error
    /// @throws IllegalArgumentException when parsing fails without a specific public validation error
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public WebURL parse(String input) {
        return parseRequired(input, null, "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The supplied base string is parsed first with no base URL and with this factory's IDNA profile. The
    /// input is then parsed relative to that base.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails with a known URL validation error
    /// @throws IllegalArgumentException when either input fails without a specific public validation error
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public WebURL parse(String input, String base) {
        return parseRequired(input, parseBaseRequired(base), "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL and returns the parsed URL.
    ///
    /// The supplied base URL is used only for this call.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL
    /// @throws WebURLParseException when parsing fails with a known URL validation error
    /// @throws IllegalArgumentException when parsing fails without a specific public validation error
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public WebURL parse(String input, WebURL base) {
        return parseRequired(input, implementation(base), "Invalid URL: " + input);
    }

    /// Parses an input string and returns `null` on failure.
    ///
    /// This method has the same parser behavior as `parse(String)`, except URL parse failures are represented
    /// by `null` instead of an exception.
    ///
    /// @param input the URL input string
    /// @return the parsed URL, or `null` if parsing fails
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public @Nullable WebURL tryParse(String input) {
        return parseNullable(input, null);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// The supplied base string is parsed with no base URL and with this factory's IDNA profile.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public @Nullable WebURL tryParse(String input, String base) {
        WebURLImpl parsedBase = parseBaseNullable(base);
        return parsedBase == null ? null : parseNullable(input, parsedBase);
    }

    /// Parses an input string against a base URL and returns `null` on failure.
    ///
    /// The supplied base URL is used only for this call.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return the parsed URL, or `null` if parsing fails
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public @Nullable WebURL tryParse(String input, WebURL base) {
        return parseNullable(input, implementation(base));
    }

    /// Returns whether an input string can be parsed.
    ///
    /// The input must be an absolute URL. Use `canParse(String, String)` or `canParse(String, WebURL)` to test
    /// a relative input against an explicit base URL.
    ///
    /// @param input the URL input string
    /// @return `true` if parsing succeeds, otherwise `false`
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public boolean canParse(String input) {
        return tryParse(input) != null;
    }

    /// Returns whether an input string can be parsed against a base URL string.
    ///
    /// The supplied base string is parsed with no base URL and with this factory's IDNA profile.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return `true` if the base parses and the input parses against it, otherwise `false`
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public boolean canParse(String input, String base) {
        return tryParse(input, base) != null;
    }

    /// Returns whether an input string can be parsed against a base URL.
    ///
    /// The supplied base URL is used only for this call.
    ///
    /// @param input the URL input string
    /// @param base the base URL
    /// @return `true` if parsing succeeds, otherwise `false`
    /// @throws IllegalStateException when this factory requires an unavailable IDNA profile implementation
    public boolean canParse(String input, WebURL base) {
        return tryParse(input, base) != null;
    }

    /// Returns a mutable builder initialized from this factory.
    ///
    /// Changes to the returned builder do not affect this factory.
    ///
    /// @return a new mutable builder containing this factory's settings
    public Builder toBuilder() {
        return new Builder(idnaProfile);
    }

    /// Parses an input string and throws when parsing fails.
    private WebURL parseRequired(String input, @Nullable WebURLImpl base, String message) {
        Objects.requireNonNull(input, "input");
        try {
            return UrlParser.basicParseRequired(input, base, null, null, idnaProfile);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(message, exception);
        }
    }

    /// Parses a base URL string and throws when parsing fails.
    private WebURLImpl parseBaseRequired(String base) {
        Objects.requireNonNull(base, "base");
        try {
            return UrlParser.basicParseRequired(base, null, null, null, idnaProfile);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid base URL: " + base, exception);
        }
    }

    /// Parses an input string and returns `null` when parsing fails.
    private @Nullable WebURLImpl parseNullable(String input, @Nullable WebURLImpl base) {
        Objects.requireNonNull(input, "input");
        return UrlParser.basicParse(input, base, null, null, idnaProfile);
    }

    /// Parses a base URL string and returns `null` when parsing fails.
    private @Nullable WebURLImpl parseBaseNullable(String base) {
        return parseNullable(base, null);
    }

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) Objects.requireNonNull(url, "url");
    }

    /// IDNA processing profile for domain host parsing.
    ///
    /// The URL Standard's domain-to-ASCII operation is observable in the serialized hostname for any URL whose
    /// host contains non-ASCII domain labels or punycode labels. Profile selection affects only domain hosts.
    /// It does not affect opaque hosts, IPv4 parsing, IPv6 parsing, path parsing, query parsing, or fragment
    /// parsing.
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

    /// A mutable builder for `WebURLFactory`.
    ///
    /// A new builder starts with the same configuration as `standard()`: `IDNAProfile.UTS_46`. Builder
    /// methods mutate and return this builder so calls can be chained.
    @NotNullByDefault
    public static final class Builder {
        /// The configured IDNA profile for the factory being built.
        private IDNAProfile idnaProfile;

        /// Creates a builder initialized from factory settings.
        private Builder(IDNAProfile idnaProfile) {
            this.idnaProfile = idnaProfile;
        }

        /// Returns the configured IDNA profile.
        ///
        /// @return the configured IDNA profile
        public IDNAProfile idnaProfile() {
            return idnaProfile;
        }

        /// Sets the IDNA profile.
        ///
        /// The profile affects domain-to-ASCII conversion for all parsing methods on factories created by this
        /// builder, including overloads that first parse an explicit base URL string.
        ///
        /// @param idnaProfile the IDNA profile
        /// @return this builder
        public Builder idnaProfile(IDNAProfile idnaProfile) {
            this.idnaProfile = Objects.requireNonNull(idnaProfile, "idnaProfile");
            return this;
        }

        /// Creates an immutable factory from the current builder state.
        ///
        /// @return a configured immutable factory
        public WebURLFactory build() {
            return new WebURLFactory(idnaProfile);
        }
    }
}
