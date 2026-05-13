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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// Internal immutable implementation of `WebURLPatternComponentResult`.
@NotNullByDefault
public final class WebURLPatternComponentResultImpl implements WebURLPatternComponentResult {
    /// Sentinel value stored in group caches for unmatched capture groups.
    @SuppressWarnings("StringOperationCanBeSimplified")
    private static final String UNMATCHED_GROUP = new String();

    /// Component input containing all matched ranges.
    private final String input;
    /// Range of the whole matched component input.
    private final @IndexRange("input") long range;
    /// Capture group ranges indexed by `groupIndexes` values.
    private final @IndexRange("input") long @Unmodifiable [] groupRanges;
    /// Public group keys mapped to `groupRanges` indexes.
    private final @Unmodifiable Map<String, Integer> groupIndexes;
    /// These caches may be rebuilt concurrently; correctness does not depend on preserving writes.
    /// Cached whole component match.
    private @Nullable String match;
    /// Cached capture group values indexed by `groupRanges`.
    private String @Nullable [] groupValues;
    /// Cached URLPattern groups object view.
    private @Nullable @Unmodifiable Map<String, @Nullable String> webGroups;

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

    /// Returns the start index of the whole component match.
    @Override
    @Contract(pure = true)
    public int start() {
        return IndexRanges.start(range);
    }

    /// Returns the start index of one Java-style capture group.
    @Override
    @Contract(pure = true)
    public int start(int group) {
        return group == 0 ? start() : groupRangeStart(group - 1);
    }

    /// Returns the end index of the whole component match.
    @Override
    @Contract(pure = true)
    public int end() {
        return IndexRanges.end(range);
    }

    /// Returns the end index of one Java-style capture group.
    @Override
    @Contract(pure = true)
    public int end(int group) {
        return group == 0 ? end() : groupRangeEnd(group - 1);
    }

    /// Returns the whole component match.
    @Override
    @Contract(pure = true)
    public String group() {
        @Nullable String result = match;
        if (result == null) {
            result = IndexRanges.substring(input, range);
            match = result;
        }
        return result;
    }

    /// Returns one Java-style capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String group(int group) {
        if (group == 0) {
            return group();
        }
        return groupValue(matchResultGroupIndex(group));
    }

    /// Returns the number of Java-style capture groups.
    @Override
    @Contract(pure = true)
    public int groupCount() {
        return groupRanges.length;
    }

    /// Returns URLPattern named and numeric capture groups.
    @Override
    @Contract(pure = true)
    public @Unmodifiable Map<String, @Nullable String> getWebGroups() {
        @Nullable Map<String, @Nullable String> result = webGroups;
        if (result != null) {
            return result;
        }

        if (groupIndexes.isEmpty()) {
            result = Map.of();
            webGroups = result;
            return result;
        }

        LinkedHashMap<String, @Nullable String> groups = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : groupIndexes.entrySet()) {
            groups.put(entry.getKey(), groupValue(entry.getValue()));
        }
        result = Collections.unmodifiableMap(groups);
        webGroups = result;
        return result;
    }

    /// Returns a named URLPattern capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String getWebGroup(String name) {
        Integer index = groupIndexes.get(Objects.requireNonNull(name, "name"));
        return index == null ? null : groupValue(index);
    }

    /// Returns a numeric URLPattern capture group.
    @Override
    @Contract(pure = true)
    public @Nullable String getWebGroup(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        return getWebGroup(Integer.toString(index));
    }

    /// Returns the group value for the given capture group index.
    private @Nullable String groupValue(int index) {
        if (index < 0 || index >= groupRanges.length) {
            return null;
        }
        String value = cachedGroupValue(index);
        return value == UNMATCHED_GROUP ? null : value;
    }

    /// Returns the cached group value or the unmatched-group sentinel.
    private String cachedGroupValue(int index) {
        @Nullable String[] currentValues = groupValues;
        if (currentValues != null) {
            @Nullable String value = currentValues[index];
            if (value != null) {
                return value;
            }
        }

        @IndexRange("input") long groupRange = groupRanges[index];
        String value = IndexRanges.isAbsent(groupRange) ? UNMATCHED_GROUP : IndexRanges.substring(input, groupRange);

        String[] nextValues = currentValues == null ? new String[groupRanges.length] : currentValues.clone();
        nextValues[index] = value;
        groupValues = nextValues;
        return value;
    }

    /// Returns the Java-style group range start.
    private int groupRangeStart(int index) {
        @IndexRange("input") long groupRange = groupRanges[matchResultGroupIndex(index + 1)];
        return IndexRanges.isAbsent(groupRange) ? -1 : IndexRanges.start(groupRange);
    }

    /// Returns the Java-style group range end.
    private int groupRangeEnd(int index) {
        @IndexRange("input") long groupRange = groupRanges[matchResultGroupIndex(index + 1)];
        return IndexRanges.isAbsent(groupRange) ? -1 : IndexRanges.end(groupRange);
    }

    /// Converts a Java-style group index to an internal capture group index.
    private int matchResultGroupIndex(int group) {
        if (group < 0 || group > groupRanges.length) {
            throw new IndexOutOfBoundsException("group: " + group);
        }
        return group - 1;
    }

    /// Compares this component result with another object.
    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURLPatternComponentResult other
                && group().equals(other.group())
                && getWebGroups().equals(other.getWebGroups());
    }

    /// Returns the hash code of this component result.
    @Override
    @Contract(pure = true)
    public int hashCode() {
        return 31 * group().hashCode() + getWebGroups().hashCode();
    }

    /// Returns a string representation of this component result.
    @Override
    @Contract(pure = true)
    public String toString() {
        return "ComponentResult[match=" + group() + ", webGroups=" + getWebGroups() + "]";
    }
}
