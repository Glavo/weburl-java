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

import org.glavo.url.WebURLSearchParams;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/// Internal implementation of `WebURLSearchParams`.
@NotNullByDefault
public final class WebURLSearchParamsImpl implements WebURLSearchParams {
    /// The ordered list of name-value tuples.
    private final @Unmodifiable List<Map.Entry<String, String>> list;

    /// Creates an empty search parameter list.
    public static WebURLSearchParams of() {
        return new WebURLSearchParamsImpl();
    }

    /// Creates search parameters from a query string.
    public static WebURLSearchParams of(String init) {
        return new WebURLSearchParamsImpl(init, false);
    }

    /// Creates search parameters from map entries.
    public static WebURLSearchParams of(Map<String, String> init) {
        return new WebURLSearchParamsImpl(init.entrySet());
    }

    /// Creates search parameters from iterable map entries.
    public static WebURLSearchParams of(Iterable<? extends Map.Entry<String, String>> init) {
        return new WebURLSearchParamsImpl(init);
    }

    /// Creates search parameters from an already-parsed URL query.
    public static WebURLSearchParams fromQueryInternal(String init) {
        return new WebURLSearchParamsImpl(init, true);
    }

    /// Creates an empty search parameter list.
    private WebURLSearchParamsImpl() {
        this.list = List.of();
    }

    /// Creates search parameters from a query string with optional question-mark preservation.
    private WebURLSearchParamsImpl(String init, boolean doNotStripQuestionMark) {
        String input = !doNotStripQuestionMark && init.startsWith("?") ? init.substring(1) : init;
        this.list = immutableEntries(UrlEncoded.parseUrlencodedString(input));
    }

    /// Creates search parameters from iterable map entries.
    private WebURLSearchParamsImpl(Iterable<? extends Map.Entry<String, String>> init) {
        this.list = immutableEntries(init);
    }

    /// Creates search parameters from entry storage.
    private WebURLSearchParamsImpl(List<Map.Entry<String, String>> entries, boolean ignored) {
        this.list = immutableEntries(entries);
    }

    /// Returns the number of tuples.
    @Override
    public int size() {
        return list.size();
    }

    /// Returns search parameters with a tuple appended.
    @Override
    public WebURLSearchParams append(String name, String value) {
        ArrayList<Map.Entry<String, String>> tuples = mutableEntries();
        tuples.add(Map.entry(name, value));
        return new WebURLSearchParamsImpl(tuples, true);
    }

    /// Returns search parameters without tuples that have the given name.
    @Override
    public WebURLSearchParams delete(String name) {
        return delete(name, null);
    }

    /// Returns search parameters without tuples that have the given name and value.
    @Override
    public WebURLSearchParams delete(String name, @Nullable String value) {
        ArrayList<Map.Entry<String, String>> tuples = mutableEntries();
        tuples.removeIf(tuple -> tuple.getKey().equals(name) && (value == null || tuple.getValue().equals(value)));
        return new WebURLSearchParamsImpl(tuples, true);
    }

    /// Returns the first value for a name, or `null` when absent.
    @Override
    public @Nullable String get(String name) {
        for (Map.Entry<String, String> tuple : list) {
            if (tuple.getKey().equals(name)) {
                return tuple.getValue();
            }
        }
        return null;
    }

    /// Returns all values for a name.
    @Override
    public @Unmodifiable List<String> getAll(String name) {
        List<String> output = new ArrayList<>();
        for (Map.Entry<String, String> tuple : list) {
            if (tuple.getKey().equals(name)) {
                output.add(tuple.getValue());
            }
        }
        return Collections.unmodifiableList(output);
    }

    /// Returns whether a tuple with the given name exists.
    @Override
    public boolean has(String name) {
        return has(name, null);
    }

    /// Returns whether a tuple with the given name and value exists.
    @Override
    public boolean has(String name, @Nullable String value) {
        for (Map.Entry<String, String> tuple : list) {
            if (tuple.getKey().equals(name) && (value == null || tuple.getValue().equals(value))) {
                return true;
            }
        }
        return false;
    }

    /// Returns search parameters with a name set to one value, removing later duplicates.
    @Override
    public WebURLSearchParams set(String name, String value) {
        ArrayList<Map.Entry<String, String>> tuples = mutableEntries();
        boolean found = false;
        for (int i = 0; i < tuples.size(); ) {
            Map.Entry<String, String> tuple = tuples.get(i);
            if (tuple.getKey().equals(name)) {
                if (found) {
                    tuples.remove(i);
                } else {
                    found = true;
                    tuples.set(i, Map.entry(name, value));
                    i++;
                }
            } else {
                i++;
            }
        }
        if (!found) {
            tuples.add(Map.entry(name, value));
        }
        return new WebURLSearchParamsImpl(tuples, true);
    }

    /// Returns search parameters sorted by name while preserving relative order for equal names.
    @Override
    public WebURLSearchParams sort() {
        ArrayList<Map.Entry<String, String>> tuples = mutableEntries();
        tuples.sort((left, right) -> left.getKey().compareTo(right.getKey()));
        return new WebURLSearchParamsImpl(tuples, true);
    }

    /// Runs an action for each tuple in insertion order.
    ///
    /// The first action argument is the value, and the second action argument is the name.
    @Override
    public void forEach(BiConsumer<String, String> action) {
        for (Map.Entry<String, String> tuple : list) {
            action.accept(tuple.getValue(), tuple.getKey());
        }
    }

    /// Returns an iterable over name-value entries.
    @Override
    public @Unmodifiable List<Map.Entry<String, String>> entries() {
        return list;
    }

    /// Returns all names in tuple order.
    @Override
    public @Unmodifiable List<String> keys() {
        List<String> output = new ArrayList<>();
        for (Map.Entry<String, String> tuple : list) {
            output.add(tuple.getKey());
        }
        return Collections.unmodifiableList(output);
    }

    /// Returns all values in tuple order.
    @Override
    public @Unmodifiable List<String> values() {
        List<String> output = new ArrayList<>();
        for (Map.Entry<String, String> tuple : list) {
            output.add(tuple.getValue());
        }
        return Collections.unmodifiableList(output);
    }

    /// Returns an iterator over immutable map entries.
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return list.iterator();
    }

    /// Serializes the parameter list.
    @Override
    public String toString() {
        return UrlEncoded.serializeUrlencoded(list);
    }

    /// Creates immutable entry storage.
    private static List<Map.Entry<String, String>> immutableEntries(
            Iterable<? extends Map.Entry<String, String>> entries
    ) {
        ArrayList<Map.Entry<String, String>> copy =
                entries instanceof Collection<?> collection
                        ? new ArrayList<>(collection.size())
                        : new ArrayList<>();
        for (Map.Entry<String, String> entry : entries) {
            copy.add(Map.entry(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(copy);
    }

    /// Creates mutable entry storage from this parameter list.
    private ArrayList<Map.Entry<String, String>> mutableEntries() {
        return new ArrayList<>(list);
    }
}
