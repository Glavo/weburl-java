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
package org.glavo.url.internal;

import org.glavo.url.WebURL;
import org.glavo.url.WebURLParseException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// Internal mutable implementation of `WebURL.Builder`.
@NotNullByDefault
public final class WebURLBuilderImpl implements WebURL.Builder {
    /// Recoverable validation errors rejected while parsing individual builder components.
    private static final @Unmodifiable Set<WebURLParseException.ErrorType> REJECTED_VALIDATION_ERRORS =
            Stream.of(WebURLParseException.ErrorType.values())
                    .filter(WebURLParseException.ErrorType::isRecoverable)
                    .collect(Collectors.toUnmodifiableSet());

    /// Current scheme, or `null` before it has been set.
    private @Nullable String scheme;
    /// Last username input, or `null` when absent.
    private @Nullable String usernameInput;
    /// Whether the last username input was raw serialized text.
    private boolean usernameRaw = true;
    /// Whether the current username input came from an existing parsed URL.
    private boolean usernameTrusted;
    /// Last password input, or `null` when absent.
    private @Nullable String passwordInput;
    /// Whether the last password input was raw serialized text.
    private boolean passwordRaw = true;
    /// Whether the current password input came from an existing parsed URL.
    private boolean passwordTrusted;
    /// Last host input, or `null` when absent.
    private @Nullable String hostInput;
    /// Whether the last host input was raw serialized text.
    private boolean hostRaw = true;
    /// Whether the current host input came from an existing parsed URL.
    private boolean hostTrusted;
    /// Last port input, or `null` when absent.
    private @Nullable String portInput;
    /// Last path input.
    private String pathInput = "";
    /// Whether the last path input was raw serialized text.
    private boolean pathRaw = true;
    /// Whether the current path input came from an existing parsed URL.
    private boolean pathTrusted;
    /// Whether the current path should be treated as opaque when the scheme permits it.
    private boolean pathOpaque = true;
    /// Last query input, or `null` when absent.
    private @Nullable String queryInput;
    /// Whether the last query input was raw serialized text.
    private boolean queryRaw = true;
    /// Whether the current query input came from an existing parsed URL.
    private boolean queryTrusted;
    /// Last fragment input, or `null` when absent.
    private @Nullable String fragmentInput;
    /// Whether the last fragment input was raw serialized text.
    private boolean fragmentRaw = true;
    /// Whether the current fragment input came from an existing parsed URL.
    private boolean fragmentTrusted;

    /// Creates an empty builder.
    public WebURLBuilderImpl() {
    }

    /// Creates a builder initialized from an existing URL.
    public WebURLBuilderImpl(WebURL url) {
        WebURLImpl implementation = implementation(Objects.requireNonNull(url, "url"));
        this.scheme = implementation.getScheme();
        this.usernameInput = implementation.getRawUsername();
        this.usernameTrusted = true;
        this.passwordInput = implementation.getRawPassword();
        this.passwordTrusted = true;
        this.hostInput = implementation.getHost();
        this.hostTrusted = true;
        this.portInput = implementation.getRawPort();
        this.pathInput = implementation.getRawPath();
        this.pathTrusted = true;
        this.pathOpaque = implementation.opaquePathValue() != null;
        this.queryInput = implementation.getRawQuery();
        this.queryTrusted = true;
        this.fragmentInput = implementation.getRawFragment();
        this.fragmentTrusted = true;
    }

    /// Sets the URL scheme.
    @Override
    public WebURL.Builder setScheme(String scheme) {
        this.scheme = Objects.requireNonNull(scheme, "scheme");
        return this;
    }

    /// Sets the decoded username.
    @Override
    public WebURL.Builder setUsername(@Nullable String username) {
        return setUsernameInput(username, false);
    }

    /// Sets the raw username.
    @Override
    public WebURL.Builder setRawUsername(@Nullable String username) {
        return setUsernameInput(username, true);
    }

    /// Sets the decoded password.
    @Override
    public WebURL.Builder setPassword(@Nullable String password) {
        return setPasswordInput(password, false);
    }

    /// Sets the raw password.
    @Override
    public WebURL.Builder setRawPassword(@Nullable String password) {
        return setPasswordInput(password, true);
    }

    /// Sets the decoded host.
    @Override
    public WebURL.Builder setHost(@Nullable String host) {
        return setHostInput(host, false);
    }

    /// Sets the raw host.
    @Override
    public WebURL.Builder setRawHost(@Nullable String host) {
        return setHostInput(host, true);
    }

    /// Sets the numeric port.
    @Override
    public WebURL.Builder setPort(int port) {
        return setPortInput(port == -1 ? null : Integer.toString(port));
    }

    /// Sets the raw port.
    @Override
    public WebURL.Builder setRawPort(@Nullable String port) {
        return setPortInput(port);
    }

    /// Sets the decoded path.
    @Override
    public WebURL.Builder setPath(String path) {
        return setPathInput(Objects.requireNonNull(path, "path"), false);
    }

    /// Sets the raw path.
    @Override
    public WebURL.Builder setRawPath(String path) {
        return setPathInput(Objects.requireNonNull(path, "path"), true);
    }

    /// Sets the decoded query.
    @Override
    public WebURL.Builder setQuery(@Nullable String query) {
        return setQueryInput(query, false);
    }

    /// Sets the raw query.
    @Override
    public WebURL.Builder setRawQuery(@Nullable String query) {
        return setQueryInput(query, true);
    }

    /// Sets the decoded fragment.
    @Override
    public WebURL.Builder setFragment(@Nullable String fragment) {
        return setFragmentInput(fragment, false);
    }

    /// Sets the raw fragment.
    @Override
    public WebURL.Builder setRawFragment(@Nullable String fragment) {
        return setFragmentInput(fragment, true);
    }

    /// Builds an immutable URL.
    @Override
    public WebURL build() {
        UrlRecord output = createRecord();
        String schemeValue = output.scheme;
        if (output.host == null && (!output.username.isEmpty() || !output.password.isEmpty())) {
            throw new IllegalStateException("Credentials require a host");
        }
        if (output.host == null && output.port != -1) {
            throw new IllegalStateException("Port requires a host");
        }
        if (output.scheme.equals("file") && output.host == null) {
            output.host = UrlHost.EMPTY_DOMAIN;
        }
        if (UrlParser.isSpecialScheme(schemeValue) && !schemeValue.equals("file") && output.host == null) {
            throw new IllegalStateException("Special URL scheme requires a host");
        }
        if (output.host != null && output.opaquePath != null) {
            throw new IllegalStateException("URLs with a host cannot have an opaque path");
        }
        if (output.host != null && output.path.isEmpty()) {
            output.path.add("");
        }
        if (output.opaquePath == null && output.path.isEmpty() && UrlParser.isSpecialScheme(schemeValue)) {
            output.path.add("");
        }

        String href = UrlSerializer.toUrl(output).href();
        try {
            return UrlParser.basicParseRequired(href, null, null, null);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Current components cannot form a valid URL", exception);
        }
    }

    /// Sets the username input.
    private WebURL.Builder setUsernameInput(@Nullable String value, boolean raw) {
        usernameInput = value;
        usernameRaw = raw;
        usernameTrusted = false;
        return this;
    }

    /// Sets the password input.
    private WebURL.Builder setPasswordInput(@Nullable String value, boolean raw) {
        passwordInput = value;
        passwordRaw = raw;
        passwordTrusted = false;
        return this;
    }

    /// Sets the host input.
    private WebURL.Builder setHostInput(@Nullable String value, boolean raw) {
        hostInput = value;
        hostRaw = raw;
        hostTrusted = false;
        return this;
    }

    /// Sets the port input.
    private WebURL.Builder setPortInput(@Nullable String value) {
        portInput = value;
        return this;
    }

    /// Sets the path input.
    private WebURL.Builder setPathInput(String value, boolean raw) {
        pathInput = value;
        pathRaw = raw;
        pathTrusted = false;
        pathOpaque = shouldPreferOpaquePath(value);
        return this;
    }

    /// Sets the query input.
    private WebURL.Builder setQueryInput(@Nullable String value, boolean raw) {
        queryInput = value;
        queryRaw = raw;
        queryTrusted = false;
        return this;
    }

    /// Sets the fragment input.
    private WebURL.Builder setFragmentInput(@Nullable String value, boolean raw) {
        fragmentInput = value;
        fragmentRaw = raw;
        fragmentTrusted = false;
        return this;
    }

    /// Creates a normalized URL record from the current component inputs.
    private UrlRecord createRecord() {
        String schemeValue = normalizeScheme(requireScheme());
        UrlRecord result = new UrlRecord();
        result.scheme = schemeValue;
        result.username = normalizeStringComponent(usernameInput, usernameRaw,
                PercentEncoding::isUserinfoPercentEncode, "username", usernameTrusted);
        result.password = normalizeStringComponent(passwordInput, passwordRaw,
                PercentEncoding::isUserinfoPercentEncode, "password", passwordTrusted);
        applyHost(result);
        applyPort(result);
        applyPath(result);
        applyQuery(result);
        applyFragment(result);
        return result;
    }

    /// Applies the host input to the supplied record.
    private void applyHost(UrlRecord result) {
        @Nullable String value = hostInput;
        if (value == null) {
            return;
        }
        if (hostRaw && !hostTrusted) {
            validateRawHost(value);
        }
        parseComponent(value, result, UrlParser.State.HOSTNAME, "host");
    }

    /// Applies the port input to the supplied record.
    private void applyPort(UrlRecord result) {
        @Nullable String value = portInput;
        if (value == null) {
            result.port = -1;
            return;
        }
        if (value.isEmpty()) {
            throw invalidComponent("port", null);
        }
        int port = parsePort(value);
        result.port = UrlParser.defaultPort(result.scheme) == port ? -1 : port;
    }

    /// Applies the path input to the supplied record.
    private void applyPath(UrlRecord result) {
        result.path.clear();
        result.opaquePath = null;
        boolean opaque = shouldUseOpaquePath(result);
        String value = normalizePathInput(pathInput, pathRaw, opaque, pathTrusted);
        if (opaque) {
            result.opaquePath = "";
            parseComponent(value, result, UrlParser.State.OPAQUE_PATH, "path", false);
        } else {
            parseComponent(value, result, UrlParser.State.PATH_START, "path", !pathTrusted);
        }
    }

    /// Applies the query input to the supplied record.
    private void applyQuery(UrlRecord result) {
        @Nullable String value = queryInput;
        if (value == null) {
            result.query = null;
            return;
        }
        result.query = normalizeStringComponent(value, queryRaw, queryEncodePredicate(result.scheme), "query",
                queryTrusted);
    }

    /// Applies the fragment input to the supplied record.
    private void applyFragment(UrlRecord result) {
        @Nullable String value = fragmentInput;
        if (value == null) {
            result.fragment = null;
            return;
        }
        result.fragment = normalizeStringComponent(value, fragmentRaw,
                PercentEncoding::isFragmentPercentEncode, "fragment", fragmentTrusted);
    }

    /// Parses a builder component with a URL parser state override.
    private static void parseComponent(String value, UrlRecord record, UrlParser.State state, String component) {
        parseComponent(value, record, state, component, true);
    }

    /// Parses a builder component with a URL parser state override.
    private static void parseComponent(
            String value,
            UrlRecord record,
            UrlParser.State state,
            String component,
            boolean rejectValidationErrors
    ) {
        try {
            UrlParser.basicParseRequired(value, null, record, state,
                    rejectValidationErrors ? REJECTED_VALIDATION_ERRORS : Set.of());
        } catch (WebURLParseException exception) {
            throw invalidComponent(component, exception);
        } catch (IllegalArgumentException exception) {
            throw invalidComponent(component, exception);
        }
    }

    /// Normalizes a raw or decoded string component.
    private static String normalizeStringComponent(
            @Nullable String value,
            boolean raw,
            PercentEncoding.BytePredicate predicate,
            String component,
            boolean trusted
    ) {
        if (value == null) {
            return "";
        }
        if (raw) {
            if (!trusted) {
                validateRawComponent(value, predicate, component);
            }
            return value;
        }
        return PercentEncoding.utf8PercentEncodeDecodedString(value, predicate);
    }

    /// Normalizes a raw or decoded path component.
    private static String normalizePathInput(String value, boolean raw, boolean opaque, boolean trusted) {
        PercentEncoding.BytePredicate predicate = opaque
                ? WebURLBuilderImpl::isOpaquePathPercentEncode
                : PercentEncoding::isPathPercentEncode;
        if (raw) {
            if (!trusted) {
                validateRawComponent(value, predicate, "path");
            }
            return value;
        }
        return PercentEncoding.utf8PercentEncodeDecodedString(value, predicate);
    }

    /// Validates raw serialized component text.
    private static void validateRawComponent(
            String value,
            PercentEncoding.BytePredicate predicate,
            String component
    ) {
        for (int index = 0; index < value.length(); ) {
            int codePoint = value.codePointAt(index);
            if (codePoint == '%') {
                if (!PercentEncoding.isValidPercentTriplet(value, index, value.length())) {
                    throw invalidComponent(component, null);
                }
                index += 3;
            } else {
                if (predicate.test(codePoint)) {
                    throw invalidComponent(component, null);
                }
                index += Character.charCount(codePoint);
            }
        }
    }

    /// Validates raw serialized host text before URL host parsing.
    private static void validateRawHost(String value) {
        for (int index = 0; index < value.length(); ) {
            int codePoint = value.codePointAt(index);
            if (codePoint == '%') {
                if (!PercentEncoding.isValidPercentTriplet(value, index, value.length())) {
                    throw invalidComponent("host", null);
                }
                index += 3;
            } else {
                if (PercentEncoding.isC0ControlPercentEncode(codePoint) || codePoint == ' ') {
                    throw invalidComponent("host", null);
                }
                index += Character.charCount(codePoint);
            }
        }
    }

    /// Returns the query encode predicate for a scheme.
    private static PercentEncoding.BytePredicate queryEncodePredicate(String scheme) {
        return UrlParser.isSpecialScheme(scheme)
                ? PercentEncoding::isSpecialQueryPercentEncode
                : PercentEncoding::isQueryPercentEncode;
    }

    /// Parses a complete decimal port string.
    private static int parsePort(String value) {
        int port = 0;
        for (int i = 0; i < value.length(); i++) {
            int c = value.charAt(i);
            if (!StringUtils.isAsciiDigit(c)) {
                throw invalidComponent("port", null);
            }
            port = port * 10 + (c - '0');
            if (port > 65535) {
                throw invalidComponent("port", null);
            }
        }
        return port;
    }

    /// Returns whether an opaque-path code point must be percent-encoded by this builder.
    private static boolean isOpaquePathPercentEncode(int value) {
        return PercentEncoding.isC0ControlPercentEncode(value) || value == '?' || value == '#';
    }

    /// Returns whether the current path input should prefer opaque path parsing.
    private boolean shouldUseOpaquePath(UrlRecord result) {
        return result.host == null
                && !UrlParser.isSpecialScheme(result.scheme)
                && pathOpaque
                && !pathInput.startsWith("/");
    }

    /// Returns whether a new path value should prefer opaque path parsing.
    private boolean shouldPreferOpaquePath(String value) {
        return hostInput == null && !value.startsWith("/");
    }

    /// Returns the current scheme or throws when it is missing.
    private String requireScheme() {
        @Nullable String value = scheme;
        if (value == null) {
            throw new IllegalStateException("Scheme has not been set");
        }
        return value;
    }

    /// Normalizes a URL scheme.
    private static String normalizeScheme(String scheme) {
        if (scheme.isEmpty() || !StringUtils.isAsciiAlpha(scheme.charAt(0))) {
            throw new IllegalArgumentException("Invalid URL scheme");
        }
        for (int i = 1; i < scheme.length(); i++) {
            char c = scheme.charAt(i);
            if (!StringUtils.isAsciiAlphanumeric(c) && c != '+' && c != '-' && c != '.') {
                throw new IllegalArgumentException("Invalid URL scheme");
            }
        }
        return scheme.toLowerCase(java.util.Locale.ROOT);
    }

    /// Creates an invalid component exception.
    private static IllegalArgumentException invalidComponent(String component, @Nullable Throwable cause) {
        return new IllegalArgumentException("Invalid URL " + component, cause);
    }

    /// Casts a public URL to its internal implementation.
    private static WebURLImpl implementation(WebURL url) {
        if (url instanceof WebURLImpl implementation) {
            return implementation;
        }
        throw new IllegalArgumentException("Unsupported WebURL implementation");
    }
}
