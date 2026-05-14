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

import org.glavo.url.internal.pattern.URLPatternInit;
import org.glavo.url.pattern.WebURLPattern;
import org.glavo.url.pattern.WebURLPatternParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal mutable implementation of `WebURLPattern.Builder`.
@NotNullByDefault
public final class WebURLPatternBuilderImpl implements WebURLPattern.Builder {
    /// Parser used by `build()`.
    private final WebURLPatternParser parser;
    /// Protocol or scheme component.
    private @Nullable String scheme;
    /// Username component.
    private @Nullable String username;
    /// Password component.
    private @Nullable String password;
    /// Hostname component.
    private @Nullable String host;
    /// Port component.
    private @Nullable String port;
    /// Pathname or path component.
    private @Nullable String path;
    /// Search or query component.
    private @Nullable String query;
    /// Hash or fragment component.
    private @Nullable String fragment;
    /// Base URL string.
    private @Nullable String baseURL;

    /// Creates an empty builder.
    public WebURLPatternBuilderImpl() {
        this(WebURLPatternParser.getDefault());
    }

    /// Creates an empty builder bound to the given parser.
    ///
    /// @param parser the parser used by `build()`
    public WebURLPatternBuilderImpl(WebURLPatternParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    /// Sets the scheme component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setSchemePattern(@Nullable String schemePattern) {
        this.scheme = schemePattern;
        return this;
    }

    /// Sets the username component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setUsernamePattern(@Nullable String usernamePattern) {
        this.username = usernamePattern;
        return this;
    }

    /// Sets the password component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setPasswordPattern(@Nullable String passwordPattern) {
        this.password = passwordPattern;
        return this;
    }

    /// Sets the host component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setHostPattern(@Nullable String hostPattern) {
        this.host = hostPattern;
        return this;
    }

    /// Sets the port component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setPortPattern(@Nullable String portPattern) {
        this.port = portPattern;
        return this;
    }

    /// Sets the path component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setPathPattern(@Nullable String pathPattern) {
        this.path = pathPattern;
        return this;
    }

    /// Sets the query component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setQueryPattern(@Nullable String queryPattern) {
        this.query = queryPattern;
        return this;
    }

    /// Sets the fragment component pattern string.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setFragmentPattern(@Nullable String fragmentPattern) {
        this.fragment = fragmentPattern;
        return this;
    }

    /// Sets the base URL.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setBaseURL(@Nullable String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    /// Builds an immutable URLPattern with this builder's parser.
    @Override
    @Contract("-> new")
    public WebURLPattern build() {
        return parser.compile(this);
    }

    /// Converts this builder to the internal URLPattern init value.
    URLPatternInit toPatternInit() {
        return new URLPatternInit(scheme, username, password, host, port, path, query, fragment, baseURL);
    }
}
