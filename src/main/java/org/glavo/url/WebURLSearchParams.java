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

import org.glavo.url.internal.UrlEncoded;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/// A Java implementation of the WHATWG `URLSearchParams` interface.
@NotNullByDefault
public final class WebURLSearchParams implements Iterable<Map.Entry<String, String>> {
    /// The ordered list of name-value tuples.
    private List<UrlEncoded.Tuple> list;
    /// The update callback for live query synchronization.
    private @Nullable Consumer<@Nullable String> updateCallback;

    /// Creates an empty search parameter list.
    public WebURLSearchParams() {
        this.list = new ArrayList<>();
    }

    /// Creates search parameters from a query string.
    public WebURLSearchParams(String init) {
        this(init, false);
    }

    /// Creates search parameters from map entries.
    public WebURLSearchParams(Map<String, String> init) {
        this.list = new ArrayList<>();
        for (Map.Entry<String, String> entry : init.entrySet()) {
            list.add(new UrlEncoded.Tuple(entry.getKey(), entry.getValue()));
        }
    }

    /// Creates search parameters from iterable map entries.
    public WebURLSearchParams(Iterable<? extends Map.Entry<String, String>> init) {
        this.list = new ArrayList<>();
        for (Map.Entry<String, String> entry : init) {
            list.add(new UrlEncoded.Tuple(entry.getKey(), entry.getValue()));
        }
    }

    /// Creates a detached copy of another search parameter list.
    public WebURLSearchParams(WebURLSearchParams init) {
        this.list = new ArrayList<>(init.list);
    }

    /// Creates search parameters from a query string with optional question-mark preservation.
    WebURLSearchParams(String init, boolean doNotStripQuestionMark) {
        String input = !doNotStripQuestionMark && init.startsWith("?") ? init.substring(1) : init;
        this.list = UrlEncoded.parseUrlencodedString(input);
    }

    /// Creates live search parameters for an internal URL implementation.
    @ApiStatus.Internal
    public static WebURLSearchParams createLiveInternal(String init, Consumer<@Nullable String> updateCallback) {
        WebURLSearchParams params = new WebURLSearchParams(init, true);
        params.updateCallback = updateCallback;
        return params;
    }

    /// Replaces all tuples without running update steps.
    @ApiStatus.Internal
    public void replaceAllInternal(String init) {
        this.list = UrlEncoded.parseUrlencodedString(init);
    }

    /// Returns the number of tuples.
    public int size() {
        return list.size();
    }

    /// Appends a tuple.
    public void append(String name, String value) {
        list.add(new UrlEncoded.Tuple(name, value));
        updateSteps();
    }

    /// Deletes all tuples with the given name.
    public void delete(String name) {
        delete(name, null);
    }

    /// Deletes all tuples with the given name and value.
    public void delete(String name, @Nullable String value) {
        list.removeIf(tuple -> tuple.name().equals(name) && (value == null || tuple.value().equals(value)));
        updateSteps();
    }

    /// Returns the first value for a name, or `null` when absent.
    public @Nullable String get(String name) {
        for (UrlEncoded.Tuple tuple : list) {
            if (tuple.name().equals(name)) {
                return tuple.value();
            }
        }
        return null;
    }

    /// Returns all values for a name.
    public @Unmodifiable List<String> getAll(String name) {
        List<String> output = new ArrayList<>();
        for (UrlEncoded.Tuple tuple : list) {
            if (tuple.name().equals(name)) {
                output.add(tuple.value());
            }
        }
        return Collections.unmodifiableList(output);
    }

    /// Returns whether a tuple with the given name exists.
    public boolean has(String name) {
        return has(name, null);
    }

    /// Returns whether a tuple with the given name and value exists.
    public boolean has(String name, @Nullable String value) {
        for (UrlEncoded.Tuple tuple : list) {
            if (tuple.name().equals(name) && (value == null || tuple.value().equals(value))) {
                return true;
            }
        }
        return false;
    }

    /// Sets a name to one value, removing later duplicates.
    public void set(String name, String value) {
        boolean found = false;
        for (int i = 0; i < list.size(); ) {
            UrlEncoded.Tuple tuple = list.get(i);
            if (tuple.name().equals(name)) {
                if (found) {
                    list.remove(i);
                } else {
                    found = true;
                    list.set(i, new UrlEncoded.Tuple(name, value));
                    i++;
                }
            } else {
                i++;
            }
        }
        if (!found) {
            list.add(new UrlEncoded.Tuple(name, value));
        }
        updateSteps();
    }

    /// Sorts tuples by name while preserving relative order for equal names.
    public void sort() {
        list.sort((left, right) -> left.name().compareTo(right.name()));
        updateSteps();
    }

    /// Runs an action for each tuple in insertion order.
    ///
    /// The first action argument is the value, and the second action argument is the name.
    public void forEach(BiConsumer<String, String> action) {
        for (UrlEncoded.Tuple tuple : list) {
            action.accept(tuple.value(), tuple.name());
        }
    }

    /// Returns an iterable over name-value entries.
    public Iterable<Map.Entry<String, String>> entries() {
        return this;
    }

    /// Returns all names in tuple order.
    public @Unmodifiable List<String> keys() {
        List<String> output = new ArrayList<>();
        for (UrlEncoded.Tuple tuple : list) {
            output.add(tuple.name());
        }
        return Collections.unmodifiableList(output);
    }

    /// Returns all values in tuple order.
    public @Unmodifiable List<String> values() {
        List<String> output = new ArrayList<>();
        for (UrlEncoded.Tuple tuple : list) {
            output.add(tuple.value());
        }
        return Collections.unmodifiableList(output);
    }

    /// Returns an iterator over immutable map entries.
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new EntryIterator(list.iterator());
    }

    /// Serializes the parameter list.
    @Override
    public String toString() {
        return UrlEncoded.serializeUrlencoded(list);
    }

    /// Runs URLSearchParams update steps.
    private void updateSteps() {
        if (updateCallback != null) {
            String serializedQuery = toString();
            updateCallback.accept(serializedQuery.isEmpty() ? null : serializedQuery);
        }
    }

    /// Iterator over immutable search parameter entries.
    @NotNullByDefault
    private static final class EntryIterator implements Iterator<Map.Entry<String, String>> {
        /// The tuple iterator.
        private final Iterator<UrlEncoded.Tuple> iterator;

        /// Creates an entry iterator.
        private EntryIterator(Iterator<UrlEncoded.Tuple> iterator) {
            this.iterator = iterator;
        }

        /// Returns whether another entry exists.
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /// Returns the next entry.
        @Override
        public Map.Entry<String, String> next() {
            UrlEncoded.Tuple tuple = iterator.next();
            return new AbstractMap.SimpleImmutableEntry<>(tuple.name(), tuple.value());
        }
    }
}
