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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Data-driven tests for UTS #46 processing.
@NotNullByDefault
public final class UTS46Test {
    /// Resource name copied from the Unicode IDNA test data.
    private static final String TEST_RESOURCE_NAME = "IdnaTestV2.txt";

    /// Tests UTS #46 ToUnicode and ToASCII operations against `IdnaTestV2.txt`.
    @TestFactory
    public List<DynamicTest> idnaTestV2Conformance() throws IOException {
        List<TestCase> testCases = readTestCases();
        List<DynamicTest> tests = new ArrayList<>(testCases.size());
        for (TestCase testCase : testCases) {
            tests.add(DynamicTest.dynamicTest(
                    TEST_RESOURCE_NAME + "[" + testCase.lineNumber() + "] " + displayString(testCase.source()),
                    () -> assertTestCase(testCase)
            ));
        }
        return tests;
    }

    /// Asserts one Unicode IDNA test row.
    private static void assertTestCase(TestCase testCase) {
        UTS46.Result toUnicode = UTS46.toUnicode(
                testCase.source(),
                true,
                true,
                true,
                true,
                false,
                false
        );
        assertEquals(testCase.toUnicode(), toUnicode.value(), "toUnicode");
        assertEquals(testCase.toUnicodeError(), toUnicode.error(), "toUnicode error");

        UTS46.Result toAsciiN = UTS46.toAscii(
                testCase.source(),
                true,
                true,
                true,
                true,
                false,
                true,
                false
        );
        assertEquals(testCase.toAsciiN(), toAsciiN.value(), "toAsciiN");
        assertEquals(testCase.toAsciiNError(), toAsciiN.error(), "toAsciiN error");

        UTS46.Result toAsciiT = UTS46.toAscii(
                testCase.source(),
                true,
                true,
                true,
                true,
                true,
                true,
                false
        );
        assertEquals(testCase.toAsciiT(), toAsciiT.value(), "toAsciiT");
        assertEquals(testCase.toAsciiTError(), toAsciiT.error(), "toAsciiT error");
    }

    /// Reads all usable test cases from the Unicode IDNA test resource.
    private static List<TestCase> readTestCases() throws IOException {
        @Nullable InputStream input = UTS46Test.class.getResourceAsStream(TEST_RESOURCE_NAME);
        if (input == null) {
            throw new IOException("Missing test resource: " + TEST_RESOURCE_NAME);
        }

        List<TestCase> testCases = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                @Nullable TestCase testCase = parseTestCase(line, lineNumber);
                if (testCase != null) {
                    testCases.add(testCase);
                }
            }
        }
        return testCases;
    }

    /// Parses one test data row, or returns `null` for comments, blank rows, and unsupported cases.
    private static @Nullable TestCase parseTestCase(String line, int lineNumber) {
        if (containsSurrogateEscape(line)) {
            return null;
        }

        String data = line.substring(0, commentStart(line)).trim();
        if (data.isEmpty()) {
            return null;
        }

        String[] fields = data.split(";", -1);
        if (fields.length == 0) {
            return null;
        }

        String source = decodeField(field(fields, 0));
        String toUnicode = field(fields, 1).isEmpty() ? source : decodeField(field(fields, 1));
        String toUnicodeStatus = field(fields, 2);
        String toAsciiN = field(fields, 3).isEmpty() ? toUnicode : decodeField(field(fields, 3));
        String toAsciiNStatus = field(fields, 4).isEmpty() ? toUnicodeStatus : field(fields, 4);
        String toAsciiT = field(fields, 5).isEmpty() ? toAsciiN : decodeField(field(fields, 5));
        String toAsciiTStatus = field(fields, 6).isEmpty() ? toAsciiNStatus : field(fields, 6);

        return new TestCase(
                lineNumber,
                source,
                toUnicode,
                hasErrorStatus(toUnicodeStatus),
                toAsciiN,
                hasErrorStatus(toAsciiNStatus),
                toAsciiT,
                hasErrorStatus(toAsciiTStatus)
        );
    }

    /// Returns the comment start offset for a test data line.
    private static int commentStart(String line) {
        int index = line.indexOf('#');
        return index < 0 ? line.length() : index;
    }

    /// Returns a trimmed field by index, or an empty field when absent.
    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index].trim() : "";
    }

    /// Returns whether a status field represents any error.
    private static boolean hasErrorStatus(String status) {
        return !status.isEmpty() && !status.equals("[]");
    }

    /// Decodes Unicode test data string escapes.
    private static String decodeField(String field) {
        if (field.equals("\"\"")) {
            return "";
        }

        StringBuilder output = new StringBuilder(field.length());
        for (int i = 0; i < field.length(); ) {
            char c = field.charAt(i);
            if (c == '\\' && i + 1 < field.length()) {
                char type = field.charAt(i + 1);
                if (type == 'u' && i + 6 <= field.length()) {
                    output.append((char) Integer.parseInt(field.substring(i + 2, i + 6), 16));
                    i += 6;
                    continue;
                }
                if (type == 'x' && i + 3 < field.length() && field.charAt(i + 2) == '{') {
                    int end = field.indexOf('}', i + 3);
                    if (end >= 0) {
                        output.appendCodePoint(Integer.parseInt(field.substring(i + 3, end), 16));
                        i = end + 1;
                        continue;
                    }
                }
            }

            output.append(c);
            i++;
        }
        return output.toString();
    }

    /// Returns whether a line contains an ill-formed UTF-16 surrogate escape.
    private static boolean containsSurrogateEscape(String line) {
        for (int i = 0; i + 5 < line.length(); i++) {
            if (line.charAt(i) == '\\'
                    && line.charAt(i + 1) == 'u'
                    && line.charAt(i + 2) == 'D'
                    && isLowSurrogateHexLead(line.charAt(i + 3))) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a hex digit is the second digit of a UTF-16 surrogate code unit.
    private static boolean isLowSurrogateHexLead(char c) {
        return c >= '8' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
    }

    /// Returns a compact display string for dynamic test names.
    private static String displayString(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
        return escaped.length() <= 80 ? escaped : escaped.substring(0, 77) + "...";
    }

    /// Parsed Unicode IDNA test row.
    ///
    /// @param lineNumber the source line number in `IdnaTestV2.txt`
    /// @param source the input domain
    /// @param toUnicode the expected ToUnicode output
    /// @param toUnicodeError whether ToUnicode is expected to report an error
    /// @param toAsciiN the expected non-transitional ToASCII output
    /// @param toAsciiNError whether non-transitional ToASCII is expected to report an error
    /// @param toAsciiT the expected transitional ToASCII output
    /// @param toAsciiTError whether transitional ToASCII is expected to report an error
    private record TestCase(
            int lineNumber,
            String source,
            String toUnicode,
            boolean toUnicodeError,
            String toAsciiN,
            boolean toAsciiNError,
            String toAsciiT,
            boolean toAsciiTError
    ) {
    }
}
