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

import org.glavo.url.pattern.WebURLPatternComponentResult;
import org.glavo.url.pattern.WebURLPatternResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal immutable implementation of `WebURLPatternResult`.
@NotNullByDefault
public final class WebURLPatternResultImpl implements WebURLPatternResult {
    /// Protocol component result.
    private final WebURLPatternComponentResult protocol;
    /// Username component result.
    private final WebURLPatternComponentResult username;
    /// Password component result.
    private final WebURLPatternComponentResult password;
    /// Hostname component result.
    private final WebURLPatternComponentResult hostname;
    /// Port component result.
    private final WebURLPatternComponentResult port;
    /// Pathname component result.
    private final WebURLPatternComponentResult pathname;
    /// Search component result.
    private final WebURLPatternComponentResult search;
    /// Hash component result.
    private final WebURLPatternComponentResult hash;

    /// Creates a URLPattern result.
    ///
    /// @param protocol protocol component result
    /// @param username username component result
    /// @param password password component result
    /// @param hostname hostname component result
    /// @param port port component result
    /// @param pathname pathname component result
    /// @param search search component result
    /// @param hash hash component result
    public WebURLPatternResultImpl(
            WebURLPatternComponentResult protocol,
            WebURLPatternComponentResult username,
            WebURLPatternComponentResult password,
            WebURLPatternComponentResult hostname,
            WebURLPatternComponentResult port,
            WebURLPatternComponentResult pathname,
            WebURLPatternComponentResult search,
            WebURLPatternComponentResult hash
    ) {
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
        this.hostname = Objects.requireNonNull(hostname, "hostname");
        this.port = Objects.requireNonNull(port, "port");
        this.pathname = Objects.requireNonNull(pathname, "pathname");
        this.search = Objects.requireNonNull(search, "search");
        this.hash = Objects.requireNonNull(hash, "hash");
    }

    /// Returns the protocol component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult protocol() {
        return protocol;
    }

    /// Returns the username component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult username() {
        return username;
    }

    /// Returns the password component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult password() {
        return password;
    }

    /// Returns the hostname component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult hostname() {
        return hostname;
    }

    /// Returns the port component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult port() {
        return port;
    }

    /// Returns the pathname component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult pathname() {
        return pathname;
    }

    /// Returns the search component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult search() {
        return search;
    }

    /// Returns the hash component result.
    @Override
    @Contract(pure = true)
    public WebURLPatternComponentResult hash() {
        return hash;
    }

    /// Compares this result with another object.
    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURLPatternResult other
                && protocol.equals(other.protocol())
                && username.equals(other.username())
                && password.equals(other.password())
                && hostname.equals(other.hostname())
                && port.equals(other.port())
                && pathname.equals(other.pathname())
                && search.equals(other.search())
                && hash.equals(other.hash());
    }

    /// Returns the hash code of this result.
    @Override
    @Contract(pure = true)
    public int hashCode() {
        int result = protocol.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + pathname.hashCode();
        result = 31 * result + search.hashCode();
        result = 31 * result + hash.hashCode();
        return result;
    }

    /// Returns a string representation of this result.
    @Override
    @Contract(pure = true)
    public String toString() {
        return "Result[protocol=" + protocol
                + ", username=" + username
                + ", password=" + password
                + ", hostname=" + hostname
                + ", port=" + port
                + ", pathname=" + pathname
                + ", search=" + search
                + ", hash=" + hash
                + "]";
    }
}
