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
package org.glavo.url.pattern;

import org.glavo.url.internal.WebURLPatternComponentResultImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.regex.MatchResult;

/// Result for one matched URLPattern component.
///
/// This interface extends [MatchResult] with Java-style capture group semantics and also exposes
/// URLPattern groups object semantics through [#getWebGroups()] and [#getWebGroup(String)].
///
/// @since 0.3.0
@NotNullByDefault
public sealed interface WebURLPatternComponentResult extends MatchResult permits WebURLPatternComponentResultImpl {
    /// Returns the start index of the whole component match.
    ///
    /// The index is relative to the component input represented by this result.
    ///
    /// @return the start index of the whole component match
    @Override
    @Contract(pure = true)
    int start();

    /// Returns the start index of a Java-style capture group.
    ///
    /// Group `0` is the whole component match. Groups `1` through [#groupCount()] are URLPattern
    /// capture groups in matching order. If the requested group exists but did not match, this
    /// method returns `-1`.
    ///
    /// @param group the Java-style capture group index
    /// @return the start index of the group, or `-1` when the group did not match
    /// @throws IndexOutOfBoundsException when `group` is negative or greater than [#groupCount()]
    @Override
    @Contract(pure = true)
    int start(int group);

    /// Returns the end index of the whole component match.
    ///
    /// The index is relative to the component input represented by this result.
    ///
    /// @return the end index of the whole component match
    @Override
    @Contract(pure = true)
    int end();

    /// Returns the end index of a Java-style capture group.
    ///
    /// Group `0` is the whole component match. Groups `1` through [#groupCount()] are URLPattern
    /// capture groups in matching order. If the requested group exists but did not match, this
    /// method returns `-1`.
    ///
    /// @param group the Java-style capture group index
    /// @return the end index of the group, or `-1` when the group did not match
    /// @throws IndexOutOfBoundsException when `group` is negative or greater than [#groupCount()]
    @Override
    @Contract(pure = true)
    int end(int group);

    /// Returns the whole component match.
    ///
    /// This is equivalent to `group(0)`.
    ///
    /// @return the whole component match
    @Override
    @Contract(pure = true)
    String group();

    /// Returns a Java-style capture group.
    ///
    /// Group `0` is the whole component match. Groups `1` through [#groupCount()] are URLPattern
    /// capture groups in matching order. If the requested group exists but did not match, this
    /// method returns `null`.
    ///
    /// @param group the Java-style capture group index
    /// @return the group value, or `null` when the group did not match
    /// @throws IndexOutOfBoundsException when `group` is negative or greater than [#groupCount()]
    @Override
    @Contract(pure = true)
    @Nullable String group(int group);

    /// Returns the number of Java-style capture groups.
    ///
    /// The returned value excludes group `0`, which is always the whole component match.
    ///
    /// @return the number of capture groups
    @Override
    @Contract(pure = true)
    int groupCount();

    /// Returns URLPattern named and numeric capture groups.
    ///
    /// Unmatched optional groups are represented by `null` values.
    ///
    /// This map follows URLPattern groups object semantics. Numeric keys such as `"0"` refer to
    /// anonymous URLPattern groups, while [#group(int)] follows `java.util.regex.MatchResult`
    /// semantics where group `0` is the whole component match.
    ///
    /// @return immutable URLPattern capture groups
    @Contract(pure = true)
    @Unmodifiable Map<String, @Nullable String> getWebGroups();

    /// Returns a named URLPattern capture group.
    ///
    /// This method returns `null` when the group is absent or when the group is present but did not match.
    ///
    /// @param name the group name
    /// @return the group value, or `null` when absent or unmatched
    @Contract(pure = true)
    @Nullable String getWebGroup(String name);

    /// Returns a numeric URLPattern capture group.
    ///
    /// URLPattern numeric groups are exposed with decimal string keys such as `"0"` and `"1"`.
    /// Unlike [#group(int)], index `0` means URLPattern's first anonymous capture group, not the
    /// entire component match.
    ///
    /// @param index the numeric URLPattern capture group index
    /// @return the group value, or `null` when absent or unmatched
    /// @throws IndexOutOfBoundsException when `index` is negative
    @Contract(pure = true)
    @Nullable String getWebGroup(int index);
}
