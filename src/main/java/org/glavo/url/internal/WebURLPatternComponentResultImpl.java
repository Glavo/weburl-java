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
    /// Component input containing all matched ranges.
    private final String input;
    /// Range of the whole matched component input.
    private final @IndexRange("input") long range;
    /// Capture group ranges indexed by `groupIndexes` values.
    private final @IndexRange("input") long @Unmodifiable [] groupRanges;
    /// Public group keys mapped to `groupRanges` indexes.
    private final @Unmodifiable Map<String, Integer> groupIndexes;

    /// Creates a component result.
    ///
    /// @param input the component input containing all matched ranges
    /// @param range the range of the matched component input
    /// @param groupRanges capture group ranges
    /// @param groupIndexes public group keys mapped to `groupRanges` indexes
    public WebURLPatternComponentResultImpl(
            String input,
            @IndexRange("input") long range,
            @IndexRange("input") long[] groupRanges,
            Map<String, Integer> groupIndexes
    ) {
        this.input = Objects.requireNonNull(input, "input");
        this.range = range;
        this.groupRanges = Objects.requireNonNull(groupRanges, "groupRanges").clone();
        this.groupIndexes = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(groupIndexes, "groupIndexes")));
    }

    /// Returns the matched component input.
    @Override
    @Contract(pure = true)
    public String getInput() {
        return IndexRanges.substring(input, range);
    }

    /// Returns named and numeric capture groups.
    @Override
    @Contract(pure = true)
    public @Unmodifiable Map<String, @Nullable String> getGroups() {
        if (groupIndexes.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<String, @Nullable String> groups = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : groupIndexes.entrySet()) {
            groups.put(entry.getKey(), groupValue(entry.getValue()));
        }
        return Collections.unmodifiableMap(groups);
    }

    /// Returns a named capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String getGroup(String name) {
        Integer index = groupIndexes.get(Objects.requireNonNull(name, "name"));
        return index == null ? null : groupValue(index);
    }

    /// Returns a numeric capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String getGroup(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        return getGroup(Integer.toString(index));
    }

    /// Returns the group value for the given capture group index.
    private @Nullable String groupValue(int index) {
        if (index < 0 || index >= groupRanges.length) {
            return null;
        }
        @IndexRange("input") long groupRange = groupRanges[index];
        return IndexRanges.isAbsent(groupRange) ? null : IndexRanges.substring(input, groupRange);
    }

    /// Compares this component result with another object.
    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURLPattern.ComponentResult other
                && getInput().equals(other.getInput())
                && getGroups().equals(other.getGroups());
    }

    /// Returns the hash code of this component result.
    @Override
    @Contract(pure = true)
    public int hashCode() {
        return 31 * getInput().hashCode() + getGroups().hashCode();
    }

    /// Returns a string representation of this component result.
    @Override
    @Contract(pure = true)
    public String toString() {
        return "ComponentResult[input=" + getInput() + ", groups=" + getGroups() + "]";
    }
}
