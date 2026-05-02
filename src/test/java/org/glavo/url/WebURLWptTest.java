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
        return urlTestDataTests("urltestdata.json");
    }

    /// Tests URL setters against WPT `setters_tests.json`.
    @TestFactory
    public List<DynamicTest> appliesWptSetterTests() throws IOException {
        JsonObject root = readObject("setters_tests.json");
        List<DynamicTest> tests = new ArrayList<>();

        for (Map.Entry<String, JsonElement> section : root.entrySet()) {
            String attribute = section.getKey();
            if (attribute.equals("comment") || attribute.equals("href")) {
                continue;
            }

            JsonArray cases = section.getValue().getAsJsonArray();
            for (int i = 0; i < cases.size(); i++) {
                JsonObject testCase = cases.get(i).getAsJsonObject();
                String displayName = "setters_tests.json " + attribute + "[" + i + "] "
                        + displayString(testCase.get("href").getAsString());
                tests.add(DynamicTest.dynamicTest(displayName, () -> assertSetterTest(attribute, testCase)));
            }
        }

        return tests;
    }

    /// Creates dynamic tests from a WPT URL test data JSON array.
    private static List<DynamicTest> urlTestDataTests(String resourceName) throws IOException {
        JsonArray data = readArray(resourceName);
        List<DynamicTest> tests = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            JsonElement element = data.get(i);
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject testCase = element.getAsJsonObject();
            String displayName = resourceName + "[" + i + "] " + displayString(testCase.get("input").getAsString());
            tests.add(DynamicTest.dynamicTest(displayName, () -> assertUrlTestData(testCase)));
        }

        return tests;
    }

    /// Asserts one WPT URL parsing test case.
    private static void assertUrlTestData(JsonObject testCase) {
        String input = testCase.get("input").getAsString();
        @Nullable String base = nullableString(testCase, "base");

        if (booleanValue(testCase, "failure")) {
            assertNull(parse(input, base));
            return;
        }

        @Nullable String relativeTo = nullableString(testCase, "relativeTo");
        if (relativeTo != null) {
            assertRelativeOnlyCase(input, relativeTo);
            return;
        }

        WebURL url = base == null ? WebURL.of(input) : WebURL.of(input, base);
        assertExpectedUrlFields(url, testCase);
    }

    /// Asserts one WPT relative-only test case.
    private static void assertRelativeOnlyCase(String input, String relativeTo) {
        if (relativeTo.equals("any-base")) {
            assertNotNull(WebURL.parse(input, OPAQUE_PATH_BASE));
            assertNotNull(WebURL.parse(input, NON_OPAQUE_PATH_BASE));
        } else if (relativeTo.equals("non-opaque-path-base")) {
            assertNotNull(WebURL.parse(input, NON_OPAQUE_PATH_BASE));
        } else {
            throw new AssertionError("Unknown relativeTo value: " + relativeTo);
        }
    }

    /// Asserts one WPT URL setter test case.
    private static void assertSetterTest(String attribute, JsonObject testCase) {
        WebURL original = WebURL.of(testCase.get("href").getAsString());
        WebURL updated = applySetter(original, attribute, testCase.get("new_value").getAsString());
        assertExpectedUrlFields(updated, testCase.get("expected").getAsJsonObject());
    }

    /// Applies a `WebURL` immutable setter.
    private static WebURL applySetter(WebURL url, String attribute, String value) {
        switch (attribute) {
            case "protocol":
                return url.withProtocol(value);
            case "username":
                return url.withUsername(value);
            case "password":
                return url.withPassword(value);
            case "host":
                return url.withHost(value);
            case "hostname":
                return url.withHostname(value);
            case "port":
                return url.withPort(value);
            case "pathname":
                return url.withPathname(value);
            case "search":
                return url.withSearch(value);
            case "hash":
                return url.withHash(value);
            default:
                throw new AssertionError("Unsupported setter attribute: " + attribute);
        }
    }

    /// Asserts all URL fields present in an expected WPT object.
    private static void assertExpectedUrlFields(WebURL url, JsonObject expected) {
        for (Map.Entry<String, JsonElement> entry : expected.entrySet()) {
            String fieldName = entry.getKey();
            if (fieldName.equals("input") || fieldName.equals("base") || fieldName.equals("comment")) {
                continue;
            }
            if (fieldName.equals("failure") || fieldName.equals("relativeTo")) {
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
                return url.protocol();
            case "username":
                return url.username();
            case "password":
                return url.password();
            case "host":
                return url.host();
            case "hostname":
                return url.hostname();
            case "port":
                return url.port();
            case "pathname":
                return url.pathname();
            case "search":
                return url.search();
            case "searchParams":
                return url.searchParams().toString();
            case "hash":
                return url.hash();
            default:
                throw new AssertionError("Unsupported URL field: " + fieldName);
        }
    }

    /// Parses a URL with an optional base.
    private static @Nullable WebURL parse(String input, @Nullable String base) {
        return base == null ? WebURL.parse(input) : WebURL.parse(input, base);
    }

    /// Reads a WPT JSON file as an array.
    private static JsonArray readArray(String resourceName) throws IOException {
        try (Reader reader = Files.newBufferedReader(resourcePath(resourceName), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    /// Reads a WPT JSON file as an object.
    private static JsonObject readObject(String resourceName) throws IOException {
        try (Reader reader = Files.newBufferedReader(resourcePath(resourceName), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
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
