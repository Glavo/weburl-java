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

package org.glavo.url.build;

import org.jetbrains.annotations.NotNullByDefault;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Generates the compact binary IDNA data resource used by the main library.
@NotNullByDefault
public final class IdnaDataGenerator {
    /// The magic number written at the start of the generated resource.
    private static final int IDNA_DATA_MAGIC = 0x49444E41;

    /// The binary resource format version.
    private static final int IDNA_DATA_VERSION = 2;

    /// The encoded value for the Unicode `disallowed` IDNA mapping status.
    private static final int IDNA_STATUS_DISALLOWED = 0;

    /// The encoded value for the Unicode `valid` IDNA mapping status.
    private static final int IDNA_STATUS_VALID = 1;

    /// The encoded value for the Unicode `ignored` IDNA mapping status.
    private static final int IDNA_STATUS_IGNORED = 2;

    /// The encoded value for the Unicode `mapped` IDNA mapping status.
    private static final int IDNA_STATUS_MAPPED = 3;

    /// The encoded value for the Unicode `deviation` IDNA mapping status.
    private static final int IDNA_STATUS_DEVIATION = 4;

    /// The encoded value for the Unicode left joining type.
    private static final int JOINING_TYPE_LEFT = 1;

    /// The encoded value for the Unicode right joining type.
    private static final int JOINING_TYPE_RIGHT = 2;

    /// The encoded value for the Unicode dual joining type.
    private static final int JOINING_TYPE_DUAL = 3;

    /// The encoded value for the Unicode transparent joining type.
    private static final int JOINING_TYPE_TRANSPARENT = 4;

    /// The encoded value for the Unicode left-to-right bidirectional class.
    private static final int BIDI_CLASS_LEFT_TO_RIGHT = 1;

    /// The encoded value for the Unicode right-to-left bidirectional class.
    private static final int BIDI_CLASS_RIGHT_TO_LEFT = 2;

    /// The encoded value for the Unicode Arabic letter bidirectional class.
    private static final int BIDI_CLASS_ARABIC_LETTER = 3;

    /// The encoded value for the Unicode European number bidirectional class.
    private static final int BIDI_CLASS_EUROPEAN_NUMBER = 4;

    /// The encoded value for the Unicode European separator bidirectional class.
    private static final int BIDI_CLASS_EUROPEAN_SEPARATOR = 5;

    /// The encoded value for the Unicode European terminator bidirectional class.
    private static final int BIDI_CLASS_EUROPEAN_TERMINATOR = 6;

    /// The encoded value for the Unicode Arabic number bidirectional class.
    private static final int BIDI_CLASS_ARABIC_NUMBER = 7;

    /// The encoded value for the Unicode common separator bidirectional class.
    private static final int BIDI_CLASS_COMMON_SEPARATOR = 8;

    /// The encoded value for the Unicode boundary neutral bidirectional class.
    private static final int BIDI_CLASS_BOUNDARY_NEUTRAL = 9;

    /// The encoded value for the Unicode other neutral bidirectional class.
    private static final int BIDI_CLASS_OTHER_NEUTRAL = 10;

    /// The encoded value for the Unicode nonspacing mark bidirectional class.
    private static final int BIDI_CLASS_NONSPACING_MARK = 11;

    /// Prevents instantiation.
    private IdnaDataGenerator() {
    }

    /// Generates an IDNA binary resource from Unicode source data files.
    ///
    /// @param mappingFile the Unicode IDNA mapping table
    /// @param bidiClassFile the Unicode derived bidirectional class table
    /// @param combiningClassFile the Unicode derived combining class table
    /// @param generalCategoryFile the Unicode derived general category table
    /// @param joiningTypeFile the Unicode derived joining type table
    /// @param outputFile the generated binary resource destination
    /// @throws IOException if any input file cannot be read or the output file cannot be written
    public static void generate(
            File mappingFile,
            File bidiClassFile,
            File combiningClassFile,
            File generalCategoryFile,
            File joiningTypeFile,
            File outputFile
    ) throws IOException {
        List<IdnaMappingRange> mappingRanges = parseIdnaMappingRanges(mappingFile);
        List<CodePointRange> viramaRanges = parseViramaRanges(combiningClassFile);
        List<CodePointRange> markRanges = parseMarkRanges(generalCategoryFile);
        List<BidiClassRange> bidiClassRanges = parseBidiClassRanges(bidiClassFile);
        List<JoiningTypeRange> joiningTypeRanges = parseJoiningTypeRanges(joiningTypeFile);

        ByteArrayOutputStream mappingPool = new ByteArrayOutputStream();
        Map<String, Integer> mappingOffsets = new LinkedHashMap<>();
        ArrayList<BinaryIdnaMappingRange> binaryMappingRanges = new ArrayList<>(mappingRanges.size());
        for (IdnaMappingRange range : mappingRanges) {
            if (range.mapping().isEmpty()) {
                binaryMappingRanges.add(new BinaryIdnaMappingRange(range.start(), range.end(), range.status(), -1, 0));
            } else {
                String mapping = range.mapping();
                byte[] mappingBytes = mapping.getBytes(StandardCharsets.UTF_8);
                Integer existingOffset = mappingOffsets.get(mapping);
                int offset;
                if (existingOffset != null) {
                    offset = existingOffset;
                } else {
                    offset = mappingPool.size();
                    mappingOffsets.put(mapping, offset);
                    mappingPool.write(mappingBytes);
                }

                int length = mappingBytes.length;
                binaryMappingRanges.add(new BinaryIdnaMappingRange(range.start(), range.end(), range.status(), offset, length));
            }
        }

        File parent = outputFile.getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }

        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(outputFile.toPath()))) {
            writeIntLittleEndian(output, IDNA_DATA_MAGIC);
            writeIntLittleEndian(output, IDNA_DATA_VERSION);

            writeIntLittleEndian(output, binaryMappingRanges.size());
            for (BinaryIdnaMappingRange range : binaryMappingRanges) {
                writeIntLittleEndian(output, range.start());
                writeIntLittleEndian(output, range.end());
                output.write(range.status());
                writeIntLittleEndian(output, range.mappingOffset());
                writeShortLittleEndian(output, range.mappingLength());
            }

            byte[] mappingPoolBytes = mappingPool.toByteArray();
            writeIntLittleEndian(output, mappingPoolBytes.length);
            output.write(mappingPoolBytes);

            writeIntLittleEndian(output, viramaRanges.size());
            for (CodePointRange range : viramaRanges) {
                writeIntLittleEndian(output, range.start());
                writeIntLittleEndian(output, range.end());
            }

            writeIntLittleEndian(output, markRanges.size());
            for (CodePointRange range : markRanges) {
                writeIntLittleEndian(output, range.start());
                writeIntLittleEndian(output, range.end());
            }

            writeIntLittleEndian(output, bidiClassRanges.size());
            for (BidiClassRange range : bidiClassRanges) {
                writeIntLittleEndian(output, range.start());
                writeIntLittleEndian(output, range.end());
                output.write(range.bidiClass());
            }

            writeIntLittleEndian(output, joiningTypeRanges.size());
            for (JoiningTypeRange range : joiningTypeRanges) {
                writeIntLittleEndian(output, range.start());
                writeIntLittleEndian(output, range.end());
                output.write(range.joiningType());
            }
        }
    }

    /// Writes a 32-bit integer in little-endian byte order.
    private static void writeIntLittleEndian(OutputStream output, int value) throws IOException {
        output.write(value & 0xff);
        output.write(value >>> 8 & 0xff);
        output.write(value >>> 16 & 0xff);
        output.write(value >>> 24 & 0xff);
    }

    /// Writes a 16-bit integer in little-endian byte order.
    private static void writeShortLittleEndian(OutputStream output, int value) throws IOException {
        output.write(value & 0xff);
        output.write(value >>> 8 & 0xff);
    }

    /// Parses the Unicode IDNA mapping table.
    private static List<IdnaMappingRange> parseIdnaMappingRanges(File file) throws IOException {
        ArrayList<IdnaMappingRange> ranges = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String data = dataPart(line);
            if (data.isEmpty()) {
                continue;
            }

            String[] fields = splitFields(data);
            if (fields.length < 2) {
                continue;
            }

            CodePointRange range = parseCodePointRange(fields[0]);
            int status = switch (fields[1]) {
                case "valid" -> IDNA_STATUS_VALID;
                case "ignored" -> IDNA_STATUS_IGNORED;
                case "mapped" -> IDNA_STATUS_MAPPED;
                case "deviation" -> IDNA_STATUS_DEVIATION;
                case "disallowed" -> IDNA_STATUS_DISALLOWED;
                default -> throw new IllegalStateException("Unknown IDNA status '" + fields[1] + "' in " + file);
            };
            String mapping = fields.length >= 3 ? parseCodePointSequence(fields[2]) : "";
            ranges.add(new IdnaMappingRange(range.start(), range.end(), status, mapping));
        }
        return ranges;
    }

    /// Parses virama code point ranges from the Unicode derived combining class table.
    private static List<CodePointRange> parseViramaRanges(File file) throws IOException {
        ArrayList<CodePointRange> ranges = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String data = dataPart(line);
            if (data.isEmpty()) {
                continue;
            }

            String[] fields = splitFields(data);
            if (fields.length >= 2 && fields[1].equals("9")) {
                ranges.add(parseCodePointRange(fields[0]));
            }
        }
        return mergeRanges(ranges);
    }

    /// Parses combining mark code point ranges from the Unicode derived general category table.
    private static List<CodePointRange> parseMarkRanges(File file) throws IOException {
        ArrayList<CodePointRange> ranges = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String data = dataPart(line);
            if (data.isEmpty()) {
                continue;
            }

            String[] fields = splitFields(data);
            if (fields.length >= 2 && isCombiningMarkCategory(fields[1])) {
                ranges.add(parseCodePointRange(fields[0]));
            }
        }
        return mergeRanges(ranges);
    }

    /// Parses bidirectional class ranges from the Unicode derived bidirectional class table.
    private static List<BidiClassRange> parseBidiClassRanges(File file) throws IOException {
        ArrayList<BidiClassRange> ranges = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String data = dataPart(line);
            if (data.isEmpty()) {
                continue;
            }

            String[] fields = splitFields(data);
            if (fields.length < 2) {
                continue;
            }

            int bidiClass = switch (fields[1]) {
                case "L" -> BIDI_CLASS_LEFT_TO_RIGHT;
                case "R" -> BIDI_CLASS_RIGHT_TO_LEFT;
                case "AL" -> BIDI_CLASS_ARABIC_LETTER;
                case "EN" -> BIDI_CLASS_EUROPEAN_NUMBER;
                case "ES" -> BIDI_CLASS_EUROPEAN_SEPARATOR;
                case "ET" -> BIDI_CLASS_EUROPEAN_TERMINATOR;
                case "AN" -> BIDI_CLASS_ARABIC_NUMBER;
                case "CS" -> BIDI_CLASS_COMMON_SEPARATOR;
                case "BN" -> BIDI_CLASS_BOUNDARY_NEUTRAL;
                case "ON" -> BIDI_CLASS_OTHER_NEUTRAL;
                case "NSM" -> BIDI_CLASS_NONSPACING_MARK;
                default -> 0;
            };
            if (bidiClass != 0) {
                CodePointRange range = parseCodePointRange(fields[0]);
                ranges.add(new BidiClassRange(range.start(), range.end(), bidiClass));
            }
        }
        return mergeBidiClassRanges(ranges);
    }

    /// Parses joining type ranges from the Unicode derived joining type table.
    private static List<JoiningTypeRange> parseJoiningTypeRanges(File file) throws IOException {
        ArrayList<JoiningTypeRange> ranges = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String data = dataPart(line);
            if (data.isEmpty()) {
                continue;
            }

            String[] fields = splitFields(data);
            if (fields.length < 2) {
                continue;
            }

            int type = switch (fields[1]) {
                case "L" -> JOINING_TYPE_LEFT;
                case "R" -> JOINING_TYPE_RIGHT;
                case "D" -> JOINING_TYPE_DUAL;
                case "T" -> JOINING_TYPE_TRANSPARENT;
                default -> 0;
            };
            if (type != 0) {
                CodePointRange range = parseCodePointRange(fields[0]);
                ranges.add(new JoiningTypeRange(range.start(), range.end(), type));
            }
        }
        return mergeJoiningTypeRanges(ranges);
    }

    /// Returns the data part of a Unicode text data line with comments removed.
    private static String dataPart(String line) {
        int comment = line.indexOf('#');
        return (comment < 0 ? line : line.substring(0, comment)).trim();
    }

    /// Splits a semicolon-delimited Unicode data line into trimmed fields.
    private static String[] splitFields(String data) {
        String[] fields = data.split(";");
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }

    /// Returns whether a general category is one of the Unicode mark categories.
    private static boolean isCombiningMarkCategory(String category) {
        return category.equals("Mn") || category.equals("Mc") || category.equals("Me");
    }

    /// Parses a single code point or an inclusive code point range.
    private static CodePointRange parseCodePointRange(String value) {
        int delimiter = value.indexOf("..");
        if (delimiter < 0) {
            int codePoint = Integer.parseInt(value, 16);
            return new CodePointRange(codePoint, codePoint);
        }

        int start = Integer.parseInt(value.substring(0, delimiter), 16);
        int end = Integer.parseInt(value.substring(delimiter + 2), 16);
        return new CodePointRange(start, end);
    }

    /// Parses a whitespace-delimited sequence of hexadecimal code points.
    private static String parseCodePointSequence(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        for (String item : trimmed.split("\\s+")) {
            if (!item.isEmpty()) {
                output.appendCodePoint(Integer.parseInt(item, 16));
            }
        }
        return output.toString();
    }

    /// Merges adjacent plain code point ranges.
    private static List<CodePointRange> mergeRanges(List<CodePointRange> input) {
        if (input.isEmpty()) {
            return input;
        }

        ArrayList<CodePointRange> output = new ArrayList<>();
        ArrayList<CodePointRange> sorted = new ArrayList<>(input);
        sorted.sort(Comparator.comparingInt(CodePointRange::start).thenComparingInt(CodePointRange::end));

        CodePointRange current = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            CodePointRange range = sorted.get(i);
            if (current.end() + 1 == range.start()) {
                current = new CodePointRange(current.start(), range.end());
            } else {
                output.add(current);
                current = range;
            }
        }
        output.add(current);
        return output;
    }

    /// Merges adjacent joining type ranges when they have the same type.
    private static List<JoiningTypeRange> mergeJoiningTypeRanges(List<JoiningTypeRange> input) {
        if (input.isEmpty()) {
            return input;
        }

        ArrayList<JoiningTypeRange> output = new ArrayList<>();
        ArrayList<JoiningTypeRange> sorted = new ArrayList<>(input);
        sorted.sort(Comparator.comparingInt(JoiningTypeRange::start).thenComparingInt(JoiningTypeRange::end));

        JoiningTypeRange current = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            JoiningTypeRange range = sorted.get(i);
            if (current.end() + 1 == range.start() && current.joiningType() == range.joiningType()) {
                current = new JoiningTypeRange(current.start(), range.end(), current.joiningType());
            } else {
                output.add(current);
                current = range;
            }
        }
        output.add(current);
        return output;
    }

    /// Merges adjacent bidirectional class ranges when they have the same class.
    private static List<BidiClassRange> mergeBidiClassRanges(List<BidiClassRange> input) {
        if (input.isEmpty()) {
            return input;
        }

        ArrayList<BidiClassRange> output = new ArrayList<>();
        ArrayList<BidiClassRange> sorted = new ArrayList<>(input);
        sorted.sort(Comparator.comparingInt(BidiClassRange::start).thenComparingInt(BidiClassRange::end));

        BidiClassRange current = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            BidiClassRange range = sorted.get(i);
            if (current.end() + 1 == range.start() && current.bidiClass() == range.bidiClass()) {
                current = new BidiClassRange(current.start(), range.end(), current.bidiClass());
            } else {
                output.add(current);
                current = range;
            }
        }
        output.add(current);
        return output;
    }

    /// A range of Unicode scalar values.
    private record CodePointRange(int start, int end) {
    }

    /// A parsed IDNA mapping table range.
    private record IdnaMappingRange(int start, int end, int status, String mapping) {
    }

    /// A binary IDNA mapping table range.
    private record BinaryIdnaMappingRange(int start, int end, int status, int mappingOffset, int mappingLength) {
    }

    /// A parsed joining type range.
    private record JoiningTypeRange(int start, int end, int joiningType) {
    }

    /// A parsed bidirectional class range.
    private record BidiClassRange(int start, int end, int bidiClass) {
    }
}
