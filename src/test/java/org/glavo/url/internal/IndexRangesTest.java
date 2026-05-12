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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for packed index range helpers.
@NotNullByDefault
public final class IndexRangesTest {
    /// Tests packing and unpacking a non-empty range.
    @Test
    public void packsAndUnpacksRange() {
        @IndexRange("value") long range = IndexRanges.of(3, 7);

        assertEquals(3, IndexRanges.start(range));
        assertEquals(7, IndexRanges.end(range));
        assertEquals(4, IndexRanges.length(range));
        assertFalse(IndexRanges.isEmpty(range));
        assertFalse(IndexRanges.isAbsent(range));
        assertTrue(IndexRanges.isPresent(range));
    }

    /// Tests packing and inspecting empty ranges.
    @Test
    public void supportsEmptyRange() {
        @IndexRange("value") long range = IndexRanges.of(5, 5);

        assertEquals(5, IndexRanges.start(range));
        assertEquals(5, IndexRanges.end(range));
        assertEquals(0, IndexRanges.length(range));
        assertTrue(IndexRanges.isEmpty(range));
        assertEquals("", IndexRanges.substring("value", range));
    }

    /// Tests packing the largest valid index values.
    @Test
    public void supportsMaxIndexes() {
        @IndexRange("value") long range = IndexRanges.of(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, IndexRanges.start(range));
        assertEquals(Integer.MAX_VALUE, IndexRanges.end(range));
        assertEquals(0, IndexRanges.length(range));
        assertTrue(IndexRanges.isEmpty(range));
    }

    /// Tests unchecked packing without validation.
    @Test
    public void packsUncheckedRange() {
        @IndexRange("value") long range = IndexRanges.ofUnchecked(7, 3);

        assertEquals(7, IndexRanges.start(range));
        assertEquals(3, IndexRanges.end(range));
        assertEquals(-4, IndexRanges.length(range));
        assertFalse(IndexRanges.isEmpty(range));
        assertEquals(IndexRanges.ABSENT, IndexRanges.ofUnchecked(-1, -1));
    }

    /// Tests the absent range sentinel.
    @Test
    public void identifiesAbsentRange() {
        assertEquals(-1, IndexRanges.start(IndexRanges.ABSENT));
        assertEquals(-1, IndexRanges.end(IndexRanges.ABSENT));
        assertTrue(IndexRanges.isAbsent(IndexRanges.ABSENT));
        assertFalse(IndexRanges.isPresent(IndexRanges.ABSENT));
        assertFalse(IndexRanges.isEmpty(IndexRanges.ABSENT));
        assertThrows(IndexOutOfBoundsException.class, () -> IndexRanges.length(IndexRanges.ABSENT));
        assertThrows(IndexOutOfBoundsException.class, () -> IndexRanges.substring("value", IndexRanges.ABSENT));
    }

    /// Tests substring extraction from a packed range.
    @Test
    public void returnsSubstring() {
        assertEquals("lue", IndexRanges.substring("value", IndexRanges.of(2, 5)));
        assertEquals("", IndexRanges.substring("value", IndexRanges.of(0, 0)));
    }

    /// Tests validation when packing invalid ranges.
    @Test
    public void rejectsInvalidRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> IndexRanges.of(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> IndexRanges.of(4, 3));
    }
}
