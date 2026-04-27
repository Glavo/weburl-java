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

/// An immutable Java representation of WHATWG `URLSearchParams`.
@NotNullByDefault
public final class WebURLSearchParams implements Iterable<Map.Entry<String, String>> {
    /// The ordered list of name-value tuples.
    private final @Unmodifiable List<UrlEncoded.Tuple> list;

    /// Creates an empty search parameter list.
    public WebURLSearchParams() {
        this.list = List.of();
    }

    /// Creates search parameters from a query string.
    public WebURLSearchParams(String init) {
        this(init, false);
    }

    /// Creates search parameters from map entries.
    public WebURLSearchParams(Map<String, String> init) {
        ArrayList<UrlEncoded.Tuple> tuples = new ArrayList<>();
        for (Map.Entry<String, String> entry : init.entrySet()) {
            tuples.add(new UrlEncoded.Tuple(entry.getKey(), entry.getValue()));
        }
        this.list = immutableTuples(tuples);
    }

    /// Creates search parameters from iterable map entries.
    public WebURLSearchParams(Iterable<? extends Map.Entry<String, String>> init) {
        ArrayList<UrlEncoded.Tuple> tuples = new ArrayList<>();
        for (Map.Entry<String, String> entry : init) {
            tuples.add(new UrlEncoded.Tuple(entry.getKey(), entry.getValue()));
        }
        this.list = immutableTuples(tuples);
    }

    /// Creates a detached copy of another search parameter list.
    public WebURLSearchParams(WebURLSearchParams init) {
        this.list = immutableTuples(init.list);
    }

    /// Creates search parameters from a query string with optional question-mark preservation.
    WebURLSearchParams(String init, boolean doNotStripQuestionMark) {
        String input = !doNotStripQuestionMark && init.startsWith("?") ? init.substring(1) : init;
        this.list = immutableTuples(UrlEncoded.parseUrlencodedString(input));
    }

    /// Creates search parameters from an already-parsed URL query.
    @ApiStatus.Internal
    public static WebURLSearchParams fromQueryInternal(String init) {
        return new WebURLSearchParams(init, true);
    }

    /// Returns the number of tuples.
    public int size() {
        return list.size();
    }

    /// Returns search parameters with a tuple appended.
    public WebURLSearchParams append(String name, String value) {
        ArrayList<UrlEncoded.Tuple> tuples = mutableTuples();
        tuples.add(new UrlEncoded.Tuple(name, value));
        return new WebURLSearchParams(tuples, true);
    }

    /// Returns search parameters without tuples that have the given name.
    public WebURLSearchParams delete(String name) {
        return delete(name, null);
    }

    /// Returns search parameters without tuples that have the given name and value.
    public WebURLSearchParams delete(String name, @Nullable String value) {
        ArrayList<UrlEncoded.Tuple> tuples = mutableTuples();
        tuples.removeIf(tuple -> tuple.name().equals(name) && (value == null || tuple.value().equals(value)));
        return new WebURLSearchParams(tuples, true);
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

    /// Returns search parameters with a name set to one value, removing later duplicates.
    public WebURLSearchParams set(String name, String value) {
        ArrayList<UrlEncoded.Tuple> tuples = mutableTuples();
        boolean found = false;
        for (int i = 0; i < tuples.size(); ) {
            UrlEncoded.Tuple tuple = tuples.get(i);
            if (tuple.name().equals(name)) {
                if (found) {
                    tuples.remove(i);
                } else {
                    found = true;
                    tuples.set(i, new UrlEncoded.Tuple(name, value));
                    i++;
                }
            } else {
                i++;
            }
        }
        if (!found) {
            tuples.add(new UrlEncoded.Tuple(name, value));
        }
        return new WebURLSearchParams(tuples, true);
    }

    /// Returns search parameters sorted by name while preserving relative order for equal names.
    public WebURLSearchParams sort() {
        ArrayList<UrlEncoded.Tuple> tuples = mutableTuples();
        tuples.sort((left, right) -> left.name().compareTo(right.name()));
        return new WebURLSearchParams(tuples, true);
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

    /// Creates immutable tuple storage.
    private static List<UrlEncoded.Tuple> immutableTuples(List<UrlEncoded.Tuple> tuples) {
        return Collections.unmodifiableList(new ArrayList<>(tuples));
    }

    /// Creates mutable tuple storage from this parameter list.
    private ArrayList<UrlEncoded.Tuple> mutableTuples() {
        return new ArrayList<>(list);
    }

    /// Creates search parameters from tuple storage.
    private WebURLSearchParams(List<UrlEncoded.Tuple> tuples, boolean ignored) {
        this.list = immutableTuples(tuples);
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
