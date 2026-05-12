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

import org.glavo.url.WebURLPattern;
import org.glavo.url.internal.pattern.WebURLPatternEngine;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Internal mutable implementation of `WebURLPattern.Builder`.
@NotNullByDefault
public final class WebURLPatternBuilderImpl implements WebURLPattern.Builder {
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
    }

    /// Sets the protocol or scheme component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setScheme(@Nullable String scheme) {
        this.scheme = scheme;
        return this;
    }

    /// Sets the username component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setUsername(@Nullable String username) {
        this.username = username;
        return this;
    }

    /// Sets the password component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setPassword(@Nullable String password) {
        this.password = password;
        return this;
    }

    /// Sets the hostname or host component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setHost(@Nullable String host) {
        this.host = host;
        return this;
    }

    /// Sets the port component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setPort(@Nullable String port) {
        this.port = port;
        return this;
    }

    /// Sets the pathname or path component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setPath(@Nullable String path) {
        this.path = path;
        return this;
    }

    /// Sets the search or query component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setQuery(@Nullable String query) {
        this.query = query;
        return this;
    }

    /// Sets the hash or fragment component.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setFragment(@Nullable String fragment) {
        this.fragment = fragment;
        return this;
    }

    /// Sets the base URL.
    @Override
    @Contract("_ -> this")
    public WebURLPattern.Builder setBaseURL(@Nullable String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    /// Converts this builder to the internal init type.
    WebURLPatternEngine.Init toEngineInit() {
        return new WebURLPatternEngine.Init(scheme, username, password, host, port, path, query, fragment, baseURL);
    }
}
