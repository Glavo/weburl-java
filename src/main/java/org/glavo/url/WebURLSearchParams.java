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
///
/// A search parameter list is an ordered sequence of name-value tuples. Names are not required to be unique,
/// and operations such as `get(String)`, `getAll(String)`, `set(String, String)`, and `delete(String)` follow
/// the tuple-order rules of the URL Standard.
///
/// This type is immutable. Methods that would mutate the JavaScript `URLSearchParams` object instead return a
/// new `WebURLSearchParams` instance. The original instance keeps its tuple order and values unchanged.
///
/// Parsing and serialization use the `application/x-www-form-urlencoded` rules. In particular, `+` decodes as
/// space when parsing, and space serializes as `+`.
@NotNullByDefault
public sealed interface WebURLSearchParams
        extends Iterable<Map.Entry<String, String>>
        permits WebURLSearchParamsImpl {
    /// Creates an empty search parameter list.
    ///
    /// @return an empty parameter list
    static WebURLSearchParams of() {
        return WebURLSearchParamsImpl.of();
    }

    /// Creates search parameters from a query string.
    ///
    /// A single leading `?` is ignored, matching the WHATWG constructor behavior. The remaining input is parsed
    /// as `application/x-www-form-urlencoded` data into an ordered tuple list.
    ///
    /// @param init the query string or search string
    /// @return the parsed parameter list
    static WebURLSearchParams of(String init) {
        return WebURLSearchParamsImpl.of(init);
    }

    /// Creates search parameters from map entries.
    ///
    /// Each map entry contributes one tuple. The iteration order of the map's entry set determines the tuple
    /// order in the returned parameter list.
    ///
    /// @param init the source map
    /// @return a parameter list containing one tuple for each map entry
    static WebURLSearchParams of(Map<String, String> init) {
        return WebURLSearchParamsImpl.of(init);
    }

    /// Creates search parameters from iterable map entries.
    ///
    /// Each iterable entry contributes one tuple. Entry keys become parameter names and entry values become
    /// parameter values. The iterable order is preserved.
    ///
    /// @param init the source entries
    /// @return a parameter list containing one tuple for each source entry
    static WebURLSearchParams of(Iterable<? extends Map.Entry<String, String>> init) {
        return WebURLSearchParamsImpl.of(init);
    }

    /// Returns the number of tuples.
    ///
    /// Duplicate names are counted separately.
    ///
    /// @return the tuple count
    int size();

    /// Returns search parameters with a tuple appended.
    ///
    /// The new tuple is added after all existing tuples. Existing tuples are not changed.
    ///
    /// @param name the tuple name
    /// @param value the tuple value
    /// @return a parameter list with the appended tuple
    WebURLSearchParams append(String name, String value);

    /// Returns search parameters without tuples that have the given name.
    ///
    /// All tuples whose name equals `name` are removed. Tuple comparison is exact Java `String` equality.
    ///
    /// @param name the name to remove
    /// @return a parameter list without matching-name tuples
    WebURLSearchParams delete(String name);

    /// Returns search parameters without tuples that have the given name and value.
    ///
    /// When `value` is non-null, only tuples whose name and value both match are removed. When `value` is
    /// `null`, this method is equivalent to `delete(String)`.
    ///
    /// @param name the name to remove
    /// @param value the value to match, or `null` to match any value
    /// @return a parameter list without matching tuples
    WebURLSearchParams delete(String name, @Nullable String value);

    /// Returns the first value for a name.
    ///
    /// Tuple order is significant. If more than one tuple has the requested name, the value from the earliest
    /// tuple is returned.
    ///
    /// @param name the name to look up
    /// @return the first matching value, or `null` when absent
    @Nullable String get(String name);

    /// Returns all values for a name.
    ///
    /// Values are returned in tuple order. The returned list is immutable.
    ///
    /// @param name the name to look up
    /// @return all matching values in tuple order
    @Unmodifiable List<String> getAll(String name);

    /// Returns whether a tuple with the given name exists.
    ///
    /// @param name the name to look up
    /// @return `true` when at least one tuple has the requested name
    boolean has(String name);

    /// Returns whether a tuple with the given name and value exists.
    ///
    /// When `value` is non-null, both name and value must match. When `value` is `null`, this method is
    /// equivalent to `has(String)`.
    ///
    /// @param name the name to look up
    /// @param value the value to match, or `null` to match any value
    /// @return `true` when at least one matching tuple exists
    boolean has(String name, @Nullable String value);

    /// Returns search parameters with a name set to one value.
    ///
    /// If at least one tuple has the requested name, the first such tuple is replaced with `(name, value)` and
    /// later tuples with the same name are removed. If no tuple has the requested name, a new tuple is appended.
    ///
    /// @param name the name to set
    /// @param value the replacement value
    /// @return a parameter list with the updated tuple set
    WebURLSearchParams set(String name, String value);

    /// Returns search parameters sorted by name.
    ///
    /// Sorting is stable: tuples with equal names keep their original relative order. Names are compared using
    /// Java `String.compareTo(String)`, which matches UTF-16 code unit ordering for these strings.
    ///
    /// @return a sorted parameter list
    WebURLSearchParams sort();

    /// Runs an action for each tuple in insertion order.
    ///
    /// The callback argument order matches WHATWG `URLSearchParams.forEach`: the first action argument is the
    /// value, and the second action argument is the name.
    ///
    /// @param action the action to run for each tuple
    void forEach(BiConsumer<String, String> action);

    /// Returns the name-value entries.
    ///
    /// Entries are returned in tuple order. Each returned map entry is immutable and the returned list is
    /// immutable.
    ///
    /// @return immutable entries in tuple order
    @Unmodifiable List<Map.Entry<String, String>> entries();

    /// Returns all names in tuple order.
    ///
    /// Duplicate names are included once for each tuple in which they appear. The returned list is immutable.
    ///
    /// @return immutable names in tuple order
    @Unmodifiable List<String> keys();

    /// Returns all values in tuple order.
    ///
    /// The returned list is immutable.
    ///
    /// @return immutable values in tuple order
    @Unmodifiable List<String> values();

    /// Returns an iterator over immutable map entries.
    ///
    /// The iterator traverses tuples in tuple order and does not support mutation.
    ///
    /// @return an iterator over immutable name-value entries
    @Override
    Iterator<Map.Entry<String, String>> iterator();

    /// Serializes the parameter list.
    ///
    /// The result is an `application/x-www-form-urlencoded` string without a leading `?`. Names and values are
    /// percent-encoded with the form-urlencoded percent-encode set, and space is serialized as `+`.
    ///
    /// @return the serialized parameter list
    @Override
    String toString();
}
