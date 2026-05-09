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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// WPT data-driven tests for the URL Standard's domain-to-ASCII processing.
@NotNullByDefault
public final class UTS46WptTest {
    /// System property pointing to downloaded WPT URL resource JSON files.
    private static final String WPT_RESOURCES_PROPERTY = "org.glavo.url.wpt.resources";

    /// Tests URL Standard domain-to-ASCII behavior against WPT `IdnaTestV2.json`.
    @TestFactory
    public List<DynamicTest> idnaTestV2DomainToAscii() throws IOException {
        JsonArray data = readArray("IdnaTestV2.json");
        List<DynamicTest> tests = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            JsonElement element = data.get(i);
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject testCase = element.getAsJsonObject();
            String input = testCase.get("input").getAsString();
            String displayName = "IdnaTestV2.json" + "[" + i + "] " + displayString(input);
            tests.add(DynamicTest.dynamicTest(displayName, () -> assertDomainToAscii(testCase)));
        }

        return tests;
    }

    /// Asserts one WPT domain-to-ASCII test case.
    private static void assertDomainToAscii(JsonObject testCase) {
        String input = testCase.get("input").getAsString();
        @Nullable String expected = nullableString(testCase, "output");

        UTS46.Result result = UTS46.toAsciiForUrl(input, false);
        if (expected == null) {
            assertTrue(result.error(), "error");
            return;
        }

        assertFalse(result.error(), "error");
        assertEquals(expected, result.value(), "value");
    }

    /// Reads a WPT JSON file as an array.
    private static JsonArray readArray(String resourceName) throws IOException {
        try (Reader reader = Files.newBufferedReader(resourcePath(resourceName), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    /// Resolves a downloaded WPT resource path.
    private static Path resourcePath(String resourceName) {
        String resources = System.getProperty(WPT_RESOURCES_PROPERTY);
        if (resources == null || resources.isEmpty()) {
            throw new IllegalStateException("Missing system property: " + WPT_RESOURCES_PROPERTY);
        }
        return Path.of(resources, resourceName);
    }

    /// Returns a nullable string JSON member.
    private static @Nullable String nullableString(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element == null || element.isJsonNull() ? null : element.getAsString();
    }

    /// Returns a compact display string for dynamic test names.
    private static String displayString(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            if (codePoint == '\\') {
                escaped.append("\\\\");
            } else if (codePoint == '\r') {
                escaped.append("\\r");
            } else if (codePoint == '\n') {
                escaped.append("\\n");
            } else if (codePoint == '\t') {
                escaped.append("\\t");
            } else if (codePoint < 0x20 || codePoint == 0x7f) {
                appendUnicodeEscape(escaped, codePoint);
            } else {
                escaped.appendCodePoint(codePoint);
            }
            i += Character.charCount(codePoint);
        }
        return escaped.length() <= 80 ? escaped.toString() : escaped.substring(0, 77) + "...";
    }

    /// Appends one compact Unicode escape.
    private static void appendUnicodeEscape(StringBuilder output, int codePoint) {
        output.append("\\u");
        for (int shift = 12; shift >= 0; shift -= 4) {
            output.append(Character.forDigit((codePoint >>> shift) & 0xf, 16));
        }
    }
}
