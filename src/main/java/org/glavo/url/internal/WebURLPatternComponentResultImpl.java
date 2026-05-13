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
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// Internal immutable implementation of `WebURLPattern.ComponentResult`.
@NotNullByDefault
public final class WebURLPatternComponentResultImpl implements WebURLPattern.ComponentResult {
    /// Matched component input.
    private final String input;
    /// Immutable capture group map.
    private final @Unmodifiable Map<String, @Nullable String> groups;

    /// Creates a component result.
    ///
    /// @param input the matched component input
    /// @param groups named and numeric capture groups
    public WebURLPatternComponentResultImpl(String input, Map<String, @Nullable String> groups) {
        this.input = Objects.requireNonNull(input, "input");
        Objects.requireNonNull(groups, "groups");
        this.groups = Collections.unmodifiableMap(new LinkedHashMap<>(groups));
    }

    /// Returns the matched component input.
    @Override
    @Contract(pure = true)
    public String getInput() {
        return input;
    }

    /// Returns named and numeric capture groups.
    @Override
    @Contract(pure = true)
    public @Unmodifiable Map<String, @Nullable String> getGroups() {
        return groups;
    }

    /// Returns a named capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String getGroup(String name) {
        return groups.get(Objects.requireNonNull(name, "name"));
    }

    /// Returns a numeric capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String getGroup(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        return groups.get(Integer.toString(index));
    }

    /// Compares this component result with another object.
    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURLPattern.ComponentResult other
                && input.equals(other.getInput())
                && groups.equals(other.getGroups());
    }

    /// Returns the hash code of this component result.
    @Override
    @Contract(pure = true)
    public int hashCode() {
        return 31 * input.hashCode() + groups.hashCode();
    }

    /// Returns a string representation of this component result.
    @Override
    @Contract(pure = true)
    public String toString() {
        return "ComponentResult[input=" + input + ", groups=" + groups + "]";
    }
}
