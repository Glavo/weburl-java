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

import org.glavo.url.pattern.WebURLPattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal immutable implementation of `WebURLPattern.Result`.
@NotNullByDefault
public final class WebURLPatternResultImpl implements WebURLPattern.Result {
    /// Scheme component result.
    private final WebURLPattern.ComponentResult scheme;
    /// Username component result.
    private final WebURLPattern.ComponentResult username;
    /// Password component result.
    private final WebURLPattern.ComponentResult password;
    /// Host component result.
    private final WebURLPattern.ComponentResult host;
    /// Port component result.
    private final WebURLPattern.ComponentResult port;
    /// Path component result.
    private final WebURLPattern.ComponentResult path;
    /// Query component result.
    private final WebURLPattern.ComponentResult query;
    /// Fragment component result.
    private final WebURLPattern.ComponentResult fragment;

    /// Creates a URLPattern result.
    ///
    /// @param scheme scheme component result
    /// @param username username component result
    /// @param password password component result
    /// @param host host component result
    /// @param port port component result
    /// @param path path component result
    /// @param query query component result
    /// @param fragment fragment component result
    public WebURLPatternResultImpl(
            WebURLPattern.ComponentResult scheme,
            WebURLPattern.ComponentResult username,
            WebURLPattern.ComponentResult password,
            WebURLPattern.ComponentResult host,
            WebURLPattern.ComponentResult port,
            WebURLPattern.ComponentResult path,
            WebURLPattern.ComponentResult query,
            WebURLPattern.ComponentResult fragment
    ) {
        this.scheme = Objects.requireNonNull(scheme, "scheme");
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
        this.host = Objects.requireNonNull(host, "host");
        this.port = Objects.requireNonNull(port, "port");
        this.path = Objects.requireNonNull(path, "path");
        this.query = Objects.requireNonNull(query, "query");
        this.fragment = Objects.requireNonNull(fragment, "fragment");
    }

    /// Returns the scheme component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getScheme() {
        return scheme;
    }

    /// Returns the username component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getUsername() {
        return username;
    }

    /// Returns the password component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getPassword() {
        return password;
    }

    /// Returns the host component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getHost() {
        return host;
    }

    /// Returns the port component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getPort() {
        return port;
    }

    /// Returns the path component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getPath() {
        return path;
    }

    /// Returns the query component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getQuery() {
        return query;
    }

    /// Returns the fragment component result.
    @Override
    @Contract(pure = true)
    public WebURLPattern.ComponentResult getFragment() {
        return fragment;
    }

    /// Compares this result with another object.
    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURLPattern.Result other
                && scheme.equals(other.getScheme())
                && username.equals(other.getUsername())
                && password.equals(other.getPassword())
                && host.equals(other.getHost())
                && port.equals(other.getPort())
                && path.equals(other.getPath())
                && query.equals(other.getQuery())
                && fragment.equals(other.getFragment());
    }

    /// Returns the hash code of this result.
    @Override
    @Contract(pure = true)
    public int hashCode() {
        int result = scheme.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + query.hashCode();
        result = 31 * result + fragment.hashCode();
        return result;
    }

    /// Returns a string representation of this result.
    @Override
    @Contract(pure = true)
    public String toString() {
        return "Result[scheme=" + scheme
                + ", username=" + username
                + ", password=" + password
                + ", host=" + host
                + ", port=" + port
                + ", path=" + path
                + ", query=" + query
                + ", fragment=" + fragment
                + "]";
    }
}
