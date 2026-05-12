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

import org.jetbrains.annotations.NotNullByDefault;

/// Utility methods for packed index ranges.
///
/// A packed index range stores the start index in the high 32 bits and the end index in the low 32 bits.
@NotNullByDefault
public final class IndexRanges {
    /// A mask for reading or writing the low 32 bits.
    private static final long LOW_INT_MASK = 0xffff_ffffL;

    /// A sentinel value for an absent range.
    public static final @IndexRange long ABSENT = ofUnchecked(-1, -1);

    /// Creates no instances.
    private IndexRanges() {
    }

    /// Returns a packed index range for the given start and end indexes.
    public static @IndexRange long of(int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start must be non-negative: " + start);
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("end must be greater than or equal to start: " + end + " < " + start);
        }

        return ofUnchecked(start, end);
    }

    /// Returns a packed index range for indexes that have already been validated.
    public static @IndexRange long ofUnchecked(int start, int end) {
        return ((long) start << Integer.SIZE) | (end & LOW_INT_MASK);
    }

    /// Returns the start index of the packed range, or `-1` for `ABSENT`.
    public static int start(@IndexRange long range) {
        return (int) (range >> Integer.SIZE);
    }

    /// Returns the end index of the packed range, or `-1` for `ABSENT`.
    public static int end(@IndexRange long range) {
        return (int) range;
    }

    /// Returns the length of the packed range.
    public static int length(@IndexRange long range) {
        requirePresent(range);
        return end(range) - start(range);
    }

    /// Returns whether the packed range is `ABSENT`.
    public static boolean isAbsent(@IndexRange long range) {
        return range == ABSENT;
    }

    /// Returns whether the packed range is not `ABSENT`.
    public static boolean isPresent(@IndexRange long range) {
        return range != ABSENT;
    }

    /// Returns whether the packed range is present and contains no indexes.
    public static boolean isEmpty(@IndexRange long range) {
        return isPresent(range) && start(range) == end(range);
    }

    /// Returns the substring selected by the packed range.
    public static String substring(String value, @IndexRange("value") long range) {
        requirePresent(range);
        return value.substring(start(range), end(range));
    }

    /// Throws if the packed range is `ABSENT`.
    private static void requirePresent(@IndexRange long range) {
        if (isAbsent(range)) {
            throw new IndexOutOfBoundsException("range is absent");
        }
    }
}
