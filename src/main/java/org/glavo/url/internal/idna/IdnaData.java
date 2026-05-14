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
package org.glavo.url.internal.idna;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/// Runtime view of the generated UTS #46 data tables.
@NotNullByDefault
final class IdnaData {
    /// Magic header used by the generated binary file.
    private static final int MAGIC = 0x49444E41;
    /// Binary format version read by this class.
    private static final int VERSION = 2;
    /// Resource path for the generated data file.
    private static final String RESOURCE_NAME = "IdnaData.bin";

    /// IDNA mapping status for disallowed code points.
    static final byte STATUS_DISALLOWED = 0;
    /// IDNA mapping status for valid code points.
    static final byte STATUS_VALID = 1;
    /// IDNA mapping status for ignored code points.
    static final byte STATUS_IGNORED = 2;
    /// IDNA mapping status for mapped code points.
    static final byte STATUS_MAPPED = 3;
    /// IDNA mapping status for deviation code points.
    static final byte STATUS_DEVIATION = 4;

    /// Joining type for code points that join on the left side.
    static final byte JOINING_TYPE_LEFT = 1;
    /// Joining type for code points that join on the right side.
    static final byte JOINING_TYPE_RIGHT = 2;
    /// Joining type for code points that join on both sides.
    static final byte JOINING_TYPE_DUAL = 3;
    /// Joining type for transparent code points.
    static final byte JOINING_TYPE_TRANSPARENT = 4;

    /// Bidi class `L`.
    static final byte BIDI_CLASS_LEFT_TO_RIGHT = 1;
    /// Bidi class `R`.
    static final byte BIDI_CLASS_RIGHT_TO_LEFT = 2;
    /// Bidi class `AL`.
    static final byte BIDI_CLASS_ARABIC_LETTER = 3;
    /// Bidi class `EN`.
    static final byte BIDI_CLASS_EUROPEAN_NUMBER = 4;
    /// Bidi class `ES`.
    static final byte BIDI_CLASS_EUROPEAN_SEPARATOR = 5;
    /// Bidi class `ET`.
    static final byte BIDI_CLASS_EUROPEAN_TERMINATOR = 6;
    /// Bidi class `AN`.
    static final byte BIDI_CLASS_ARABIC_NUMBER = 7;
    /// Bidi class `CS`.
    static final byte BIDI_CLASS_COMMON_SEPARATOR = 8;
    /// Bidi class `BN`.
    static final byte BIDI_CLASS_BOUNDARY_NEUTRAL = 9;
    /// Bidi class `ON`.
    static final byte BIDI_CLASS_OTHER_NEUTRAL = 10;
    /// Bidi class `NSM`.
    static final byte BIDI_CLASS_NONSPACING_MARK = 11;

    /// Shared data table instance.
    static final IdnaData INSTANCE = load();

    /// Start code point for each IDNA mapping range.
    private final int @Unmodifiable [] mappingStarts;
    /// End code point for each IDNA mapping range.
    private final int @Unmodifiable [] mappingEnds;
    /// Mapping status for each IDNA mapping range.
    private final byte @Unmodifiable [] mappingStatuses;
    /// UTF-8 mapping pool offset for each IDNA mapping range.
    private final int @Unmodifiable [] mappingOffsets;
    /// UTF-8 mapping byte length for each IDNA mapping range.
    private final int @Unmodifiable [] mappingLengths;
    /// Shared UTF-8 bytes for all non-empty mappings.
    private final byte @Unmodifiable [] mappingPool;
    /// Start code point for each canonical combining class Virama range.
    private final int @Unmodifiable [] viramaStarts;
    /// End code point for each canonical combining class Virama range.
    private final int @Unmodifiable [] viramaEnds;
    /// Start code point for each Unicode mark range.
    private final int @Unmodifiable [] markStarts;
    /// End code point for each Unicode mark range.
    private final int @Unmodifiable [] markEnds;
    /// Start code point for each bidi class range.
    private final int @Unmodifiable [] bidiStarts;
    /// End code point for each bidi class range.
    private final int @Unmodifiable [] bidiEnds;
    /// Bidi class for each bidi class range.
    private final byte @Unmodifiable [] bidiClasses;
    /// Start code point for each joining type range.
    private final int @Unmodifiable [] joiningStarts;
    /// End code point for each joining type range.
    private final int @Unmodifiable [] joiningEnds;
    /// Joining type for each joining type range.
    private final byte @Unmodifiable [] joiningTypes;

    /// Creates a runtime data table from decoded arrays.
    private IdnaData(
            int @Unmodifiable [] mappingStarts,
            int @Unmodifiable [] mappingEnds,
            byte @Unmodifiable [] mappingStatuses,
            int @Unmodifiable [] mappingOffsets,
            int @Unmodifiable [] mappingLengths,
            byte @Unmodifiable [] mappingPool,
            int @Unmodifiable [] viramaStarts,
            int @Unmodifiable [] viramaEnds,
            int @Unmodifiable [] markStarts,
            int @Unmodifiable [] markEnds,
            int @Unmodifiable [] bidiStarts,
            int @Unmodifiable [] bidiEnds,
            byte @Unmodifiable [] bidiClasses,
            int @Unmodifiable [] joiningStarts,
            int @Unmodifiable [] joiningEnds,
            byte @Unmodifiable [] joiningTypes
    ) {
        this.mappingStarts = mappingStarts;
        this.mappingEnds = mappingEnds;
        this.mappingStatuses = mappingStatuses;
        this.mappingOffsets = mappingOffsets;
        this.mappingLengths = mappingLengths;
        this.mappingPool = mappingPool;
        this.viramaStarts = viramaStarts;
        this.viramaEnds = viramaEnds;
        this.markStarts = markStarts;
        this.markEnds = markEnds;
        this.bidiStarts = bidiStarts;
        this.bidiEnds = bidiEnds;
        this.bidiClasses = bidiClasses;
        this.joiningStarts = joiningStarts;
        this.joiningEnds = joiningEnds;
        this.joiningTypes = joiningTypes;
    }

    /// Loads the generated UTS #46 data file from module resources.
    private static IdnaData load() {
        @Nullable InputStream input = IdnaData.class.getResourceAsStream(RESOURCE_NAME);
        if (input == null) {
            throw new IllegalStateException("Missing generated IDNA data resource: " + RESOURCE_NAME);
        }

        try (input) {
            byte[] bytes = input.readAllBytes();
            ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

            int magic = data.getInt();
            int version = data.getInt();
            if (magic != MAGIC || version != VERSION) {
                throw new IOException("Unsupported IDNA data format");
            }

            int mappingRangeCount = data.getInt();
            int[] mappingStarts = new int[mappingRangeCount];
            int[] mappingEnds = new int[mappingRangeCount];
            byte[] mappingStatuses = new byte[mappingRangeCount];
            int[] mappingOffsets = new int[mappingRangeCount];
            int[] mappingLengths = new int[mappingRangeCount];
            for (int i = 0; i < mappingRangeCount; i++) {
                mappingStarts[i] = data.getInt();
                mappingEnds[i] = data.getInt();
                mappingStatuses[i] = data.get();
                mappingOffsets[i] = data.getInt();
                mappingLengths[i] = Short.toUnsignedInt(data.getShort());
            }

            byte[] mappingPool = new byte[data.getInt()];
            data.get(mappingPool);

            int viramaRangeCount = data.getInt();
            int[] viramaStarts = new int[viramaRangeCount];
            int[] viramaEnds = new int[viramaRangeCount];
            readSimpleRanges(data, viramaStarts, viramaEnds);

            int markRangeCount = data.getInt();
            int[] markStarts = new int[markRangeCount];
            int[] markEnds = new int[markRangeCount];
            readSimpleRanges(data, markStarts, markEnds);

            int bidiRangeCount = data.getInt();
            int[] bidiStarts = new int[bidiRangeCount];
            int[] bidiEnds = new int[bidiRangeCount];
            byte[] bidiClasses = new byte[bidiRangeCount];
            readTypedRanges(data, bidiStarts, bidiEnds, bidiClasses);

            int joiningRangeCount = data.getInt();
            int[] joiningStarts = new int[joiningRangeCount];
            int[] joiningEnds = new int[joiningRangeCount];
            byte[] joiningTypes = new byte[joiningRangeCount];
            readTypedRanges(data, joiningStarts, joiningEnds, joiningTypes);

            return new IdnaData(
                    mappingStarts,
                    mappingEnds,
                    mappingStatuses,
                    mappingOffsets,
                    mappingLengths,
                    mappingPool,
                    viramaStarts,
                    viramaEnds,
                    markStarts,
                    markEnds,
                    bidiStarts,
                    bidiEnds,
                    bidiClasses,
                    joiningStarts,
                    joiningEnds,
                    joiningTypes
            );
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Unable to load generated IDNA data resource", e);
        }
    }

    /// Reads ranges without an associated byte value.
    private static void readSimpleRanges(
            ByteBuffer data,
            int @Unmodifiable [] starts,
            int @Unmodifiable [] ends
    ) {
        for (int i = 0; i < starts.length; i++) {
            starts[i] = data.getInt();
            ends[i] = data.getInt();
        }
    }

    /// Reads ranges with an associated byte value.
    private static void readTypedRanges(
            ByteBuffer data,
            int @Unmodifiable [] starts,
            int @Unmodifiable [] ends,
            byte @Unmodifiable [] values
    ) {
        for (int i = 0; i < starts.length; i++) {
            starts[i] = data.getInt();
            ends[i] = data.getInt();
            values[i] = data.get();
        }
    }

    /// Returns the UTS #46 mapping status for a code point.
    byte status(int codePoint) {
        int index = find(mappingStarts, mappingEnds, codePoint);
        return index >= 0 ? mappingStatuses[index] : STATUS_DISALLOWED;
    }

    /// Returns the UTS #46 mapping string for a mapped code point.
    String mapping(int codePoint) {
        int index = find(mappingStarts, mappingEnds, codePoint);
        if (index < 0 || mappingLengths[index] == 0) {
            return "";
        }
        return new String(mappingPool, mappingOffsets[index], mappingLengths[index], StandardCharsets.UTF_8);
    }

    /// Returns whether a code point has canonical combining class Virama.
    boolean isVirama(int codePoint) {
        return find(viramaStarts, viramaEnds, codePoint) >= 0;
    }

    /// Returns whether a code point has a mark general category.
    boolean isMark(int codePoint) {
        return find(markStarts, markEnds, codePoint) >= 0;
    }

    /// Returns the bidi class for a code point.
    byte bidiClass(int codePoint) {
        int index = find(bidiStarts, bidiEnds, codePoint);
        return index >= 0 ? bidiClasses[index] : BIDI_CLASS_LEFT_TO_RIGHT;
    }

    /// Returns the joining type for a code point, or zero when the type is not relevant to ContextJ.
    byte joiningType(int codePoint) {
        int index = find(joiningStarts, joiningEnds, codePoint);
        return index >= 0 ? joiningTypes[index] : 0;
    }

    /// Finds the range that contains a code point.
    private static int find(int @Unmodifiable [] starts, int @Unmodifiable [] ends, int codePoint) {
        int low = 0;
        int high = starts.length - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (codePoint < starts[middle]) {
                high = middle - 1;
            } else if (codePoint > ends[middle]) {
                low = middle + 1;
            } else {
                return middle;
            }
        }
        return -1;
    }
}
