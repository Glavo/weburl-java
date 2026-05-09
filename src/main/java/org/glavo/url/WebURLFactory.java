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
/// settings. The standard factory returned by `standard()` uses the automatic IDNA provider; it is the factory
/// used by the static parsing methods on `WebURL`.
///
/// A factory does not store a base URL. `parse(String)`, `tryParse(String)`, and `canParse(String)` parse only
/// absolute URL inputs. Overloads that accept a base URL use the supplied base only for that call.
///
/// The factory object is immutable and thread-safe. `Builder` is mutable and is intended to be confined to
/// the thread or construction scope that creates a factory.
@NotNullByDefault
public final class WebURLFactory {
    /// The standard factory used by `WebURL` static parsing methods.
    private static final WebURLFactory STANDARD = new WebURLFactory(IdnaProvider.AUTOMATIC);

    /// The configured IDNA provider.
    private final IdnaProvider idnaProvider;

    /// Creates a factory from validated factory settings.
    private WebURLFactory(IdnaProvider idnaProvider) {
        this.idnaProvider = idnaProvider;
    }

    /// Returns the standard URL factory.
    ///
    /// The standard factory uses `IdnaProvider.AUTOMATIC`. It parses only absolute URLs unless a base is
    /// supplied to an overload that accepts one. This method always returns the same immutable factory instance.
    ///
    /// @return the standard factory
    public static WebURLFactory standard() {
        return STANDARD;
    }

    /// Returns a new factory builder.
    ///
    /// The builder initially uses `IdnaProvider.AUTOMATIC`.
    ///
    /// @return a new mutable builder
    public static Builder builder() {
        return new Builder(IdnaProvider.AUTOMATIC);
    }

    /// Returns the configured IDNA provider.
    ///
    /// The provider controls how Unicode domain labels are converted to ASCII during domain host parsing.
    /// Opaque hosts, IPv4 hosts, and IPv6 hosts are not converted through IDNA.
    ///
    /// @return the configured IDNA provider
    public IdnaProvider idnaProvider() {
        return idnaProvider;
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
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
    public WebURL parse(String input) {
        return parseRequired(input, null, "Invalid URL: " + input);
    }

    /// Parses an input string against a base URL string and returns the parsed URL.
    ///
    /// The supplied base string is parsed first with no base URL and with this factory's IDNA provider. The
    /// input is then parsed relative to that base.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL
    /// @throws WebURLParseException when either input fails with a known URL validation error
    /// @throws IllegalArgumentException when either input fails without a specific public validation error
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
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
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
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
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
    public @Nullable WebURL tryParse(String input) {
        return parseNullable(input, null);
    }

    /// Parses an input string against a base URL string and returns `null` on failure.
    ///
    /// The supplied base string is parsed with no base URL and with this factory's IDNA provider.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return the parsed URL, or `null` if either string cannot be parsed
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
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
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
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
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
    public boolean canParse(String input) {
        return tryParse(input) != null;
    }

    /// Returns whether an input string can be parsed against a base URL string.
    ///
    /// The supplied base string is parsed with no base URL and with this factory's IDNA provider.
    ///
    /// @param input the URL input string
    /// @param base the base URL string
    /// @return `true` if the base parses and the input parses against it, otherwise `false`
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
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
    /// @throws IllegalStateException when this factory requires an unavailable IDNA provider
    public boolean canParse(String input, WebURL base) {
        return tryParse(input, base) != null;
    }

    /// Returns a mutable builder initialized from this factory.
    ///
    /// Changes to the returned builder do not affect this factory.
    ///
    /// @return a new mutable builder containing this factory's settings
    public Builder toBuilder() {
        return new Builder(idnaProvider);
    }

    /// Parses an input string and throws when parsing fails.
    private WebURL parseRequired(String input, @Nullable WebURLImpl base, String message) {
        Objects.requireNonNull(input, "input");
        ensureIdnaProviderAvailable(idnaProvider);
        try {
            return UrlParser.basicParseRequired(input, base, null, null, idnaProvider);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(message, exception);
        }
    }

    /// Parses a base URL string and throws when parsing fails.
    private WebURLImpl parseBaseRequired(String base) {
        Objects.requireNonNull(base, "base");
        ensureIdnaProviderAvailable(idnaProvider);
        try {
            return UrlParser.basicParseRequired(base, null, null, null, idnaProvider);
        } catch (WebURLParseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid base URL: " + base, exception);
        }
    }

    /// Parses an input string and returns `null` when parsing fails.
    private @Nullable WebURLImpl parseNullable(String input, @Nullable WebURLImpl base) {
        Objects.requireNonNull(input, "input");
        ensureIdnaProviderAvailable(idnaProvider);
        return UrlParser.basicParse(input, base, null, null, idnaProvider);
    }

    /// Parses a base URL string and returns `null` when parsing fails.
    private @Nullable WebURLImpl parseBaseNullable(String base) {
        return parseNullable(base, null);
    }

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) Objects.requireNonNull(url, "url");
    }

    /// Ensures that an IDNA provider can be used.
    private static void ensureIdnaProviderAvailable(IdnaProvider idnaProvider) {
        if (idnaProvider == IdnaProvider.ICU4J && !idnaProvider.isAvailable()) {
            throw new IllegalStateException("ICU4J IDNA provider is not available");
        }
    }

    /// IDNA provider selection for domain host parsing.
    ///
    /// The URL Standard's domain-to-ASCII operation is observable in the serialized hostname for any URL whose
    /// host contains non-ASCII domain labels or punycode labels. Provider selection affects only domain hosts.
    /// It does not affect opaque hosts, IPv4 parsing, IPv6 parsing, path parsing, query parsing, or fragment
    /// parsing.
    @NotNullByDefault
    public enum IdnaProvider {
        /// Selects ICU4J when it is available and otherwise falls back to the JDK `java.net.IDN` implementation.
        ///
        /// This is the default provider and the provider used by `WebURL` static parsing methods. When ICU4J is
        /// available, it is used through reflection and follows UTS #46 non-transitional processing. When ICU4J
        /// is not available, parsing remains dependency-free and uses the JDK implementation.
        AUTOMATIC,

        /// Uses ICU4J for IDNA processing.
        ///
        /// This provider requires ICU4J to be visible at runtime. `Builder.build()` rejects this provider with
        /// `IllegalStateException` when ICU4J cannot be loaded.
        ICU4J,

        /// Uses the JDK `java.net.IDN` implementation for IDNA processing.
        ///
        /// This provider has no runtime dependencies outside `java.base`. It may differ from the URL Standard's
        /// UTS #46 non-transitional processing for some names, but it is always available on a Java runtime.
        JDK;

        /// Returns whether this provider can be used in the current runtime.
        ///
        /// `AUTOMATIC` and `JDK` are always available. `ICU4J` is available only when the ICU4J IDNA classes can
        /// be loaded and invoked by this module.
        ///
        /// @return `true` if this provider can be selected
        public boolean isAvailable() {
            return UrlParser.isIdnaProviderAvailable(this);
        }
    }

    /// A mutable builder for `WebURLFactory`.
    ///
    /// A new builder starts with the same configuration as `standard()`: `IdnaProvider.AUTOMATIC`. Builder
    /// methods mutate and return this builder so calls can be chained.
    @NotNullByDefault
    public static final class Builder {
        /// The configured IDNA provider for the factory being built.
        private IdnaProvider idnaProvider;

        /// Creates a builder initialized from factory settings.
        private Builder(IdnaProvider idnaProvider) {
            this.idnaProvider = idnaProvider;
        }

        /// Returns the configured IDNA provider.
        ///
        /// @return the configured IDNA provider
        public IdnaProvider idnaProvider() {
            return idnaProvider;
        }

        /// Sets the IDNA provider.
        ///
        /// The provider affects domain-to-ASCII conversion for all parsing methods on factories created by this
        /// builder, including overloads that first parse an explicit base URL string.
        ///
        /// @param idnaProvider the IDNA provider
        /// @return this builder
        public Builder idnaProvider(IdnaProvider idnaProvider) {
            this.idnaProvider = Objects.requireNonNull(idnaProvider, "idnaProvider");
            return this;
        }

        /// Creates an immutable factory from the current builder state.
        ///
        /// @return a configured immutable factory
        /// @throws IllegalStateException when the configured IDNA provider is unavailable
        public WebURLFactory build() {
            ensureIdnaProviderAvailable(idnaProvider);
            return new WebURLFactory(idnaProvider);
        }
    }
}
