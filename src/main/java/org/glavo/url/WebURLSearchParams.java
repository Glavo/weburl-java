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

import org.glavo.url.internal.WebURLSearchParamsImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/// An immutable Java representation of WHATWG `URLSearchParams`.
@NotNullByDefault
public sealed interface WebURLSearchParams
        extends Iterable<Map.Entry<String, String>>
        permits WebURLSearchParamsImpl {
    /// Creates an empty search parameter list.
    static WebURLSearchParams of() {
        return WebURLSearchParamsImpl.of();
    }

    /// Creates search parameters from a query string.
    static WebURLSearchParams of(String init) {
        return WebURLSearchParamsImpl.of(init);
    }

    /// Creates search parameters from map entries.
    static WebURLSearchParams of(Map<String, String> init) {
        return WebURLSearchParamsImpl.of(init);
    }

    /// Creates search parameters from iterable map entries.
    static WebURLSearchParams of(Iterable<? extends Map.Entry<String, String>> init) {
        return WebURLSearchParamsImpl.of(init);
    }

    /// Returns the number of tuples.
    int size();

    /// Returns search parameters with a tuple appended.
    WebURLSearchParams append(String name, String value);

    /// Returns search parameters without tuples that have the given name.
    WebURLSearchParams delete(String name);

    /// Returns search parameters without tuples that have the given name and value.
    WebURLSearchParams delete(String name, @Nullable String value);

    /// Returns the first value for a name, or `null` when absent.
    @Nullable String get(String name);

    /// Returns all values for a name.
    @Unmodifiable List<String> getAll(String name);

    /// Returns whether a tuple with the given name exists.
    boolean has(String name);

    /// Returns whether a tuple with the given name and value exists.
    boolean has(String name, @Nullable String value);

    /// Returns search parameters with a name set to one value, removing later duplicates.
    WebURLSearchParams set(String name, String value);

    /// Returns search parameters sorted by name while preserving relative order for equal names.
    WebURLSearchParams sort();

    /// Runs an action for each tuple in insertion order.
    ///
    /// The first action argument is the value, and the second action argument is the name.
    void forEach(BiConsumer<String, String> action);

    /// Returns an iterable over name-value entries.
    @Unmodifiable List<Map.Entry<String, String>> entries();

    /// Returns all names in tuple order.
    @Unmodifiable List<String> keys();

    /// Returns all values in tuple order.
    @Unmodifiable List<String> values();

    /// Returns an iterator over immutable map entries.
    @Override
    Iterator<Map.Entry<String, String>> iterator();

    /// Serializes the parameter list.
    @Override
    String toString();
}
