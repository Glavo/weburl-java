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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/// WPT data-driven tests for `WebURL`.
@NotNullByDefault
public final class WebURLWptTest {
    /// System property pointing to downloaded WPT URL resource JSON files.
    private static final String WPT_RESOURCES_PROPERTY = "org.glavo.url.wpt.resources";
    /// Base URL with a non-opaque path used by WPT relative-only cases.
    private static final String NON_OPAQUE_PATH_BASE = "https://example.test/base/path";
    /// Opaque base URL used by WPT any-base relative-only cases.
    private static final String OPAQUE_PATH_BASE = "data:text/plain,base";

    /// Tests URL parsing and getters against WPT `urltestdata.json`.
    @TestFactory
    public List<DynamicTest> parsesWptUrlTestData() throws IOException {
        JsonArray data = readArray("urltestdata.json");
        List<DynamicTest> tests = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            JsonElement element = data.get(i);
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject testCase = element.getAsJsonObject();
            String displayName = "urltestdata.json" + "[" + i + "] " + displayString(testCase.get("input").getAsString());
            tests.add(DynamicTest.dynamicTest(displayName, () -> assertUrlTestData(testCase)));
        }

        return tests;
    }

    /// Asserts one WPT URL parsing test case.
    private static void assertUrlTestData(JsonObject testCase) {
        String input = testCase.get("input").getAsString();
        @Nullable String base = nullableString(testCase, "base");

        if (booleanValue(testCase, "failure")) {
            assertNull(tryParse(input, base));
            return;
        }

        @Nullable String relativeTo = nullableString(testCase, "relativeTo");
        if (relativeTo != null) {
            assertRelativeOnlyCase(input, relativeTo);
            return;
        }

        WebURL url = base == null ? WebURL.parse(input) : WebURL.parse(input, base);
        assertExpectedUrlFields(url, testCase);
    }

    /// Asserts one WPT relative-only test case.
    private static void assertRelativeOnlyCase(String input, String relativeTo) {
        if (relativeTo.equals("any-base")) {
            assertNotNull(WebURL.tryParse(input, OPAQUE_PATH_BASE));
            assertNotNull(WebURL.tryParse(input, NON_OPAQUE_PATH_BASE));
        } else if (relativeTo.equals("non-opaque-path-base")) {
            assertNotNull(WebURL.tryParse(input, NON_OPAQUE_PATH_BASE));
        } else {
            throw new AssertionError("Unknown relativeTo value: " + relativeTo);
        }
    }

    /// Asserts all URL fields present in an expected WPT object.
    private static void assertExpectedUrlFields(WebURL url, JsonObject expected) {
        for (Map.Entry<String, JsonElement> entry : expected.entrySet()) {
            String fieldName = entry.getKey();
            if (fieldName.equals("input") || fieldName.equals("base") || fieldName.equals("comment")) {
                continue;
            }
            if (fieldName.equals("failure") || fieldName.equals("relativeTo") || fieldName.equals("searchParams")) {
                continue;
            }

            assertEquals(entry.getValue().getAsString(), urlField(url, fieldName), fieldName);
        }
    }

    /// Returns one public URL field value.
    private static String urlField(WebURL url, String fieldName) {
        switch (fieldName) {
            case "href":
                return url.href();
            case "origin":
                return url.origin();
            case "protocol":
                return url.getWebProtocol();
            case "username":
                return url.getWebUsername();
            case "password":
                return url.getWebPassword();
            case "host":
                return url.getWebHost();
            case "hostname":
                return url.getWebHostname();
            case "port":
                return url.getWebPort();
            case "pathname":
                return url.getWebPathname();
            case "search":
                return url.getWebSearch();
            case "hash":
                return url.getWebHash();
            default:
                throw new AssertionError("Unsupported URL field: " + fieldName);
        }
    }

    /// Tries to parse a URL with an optional base.
    private static @Nullable WebURL tryParse(String input, @Nullable String base) {
        return base == null ? WebURL.tryParse(input) : WebURL.tryParse(input, base);
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

    /// Returns a boolean JSON member, defaulting to false when absent.
    private static boolean booleanValue(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && !element.isJsonNull() && element.getAsBoolean();
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
}
