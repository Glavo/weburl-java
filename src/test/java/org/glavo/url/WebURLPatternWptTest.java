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
import org.glavo.url.pattern.WebURLPattern;
import org.glavo.url.pattern.WebURLPatternComponentResult;
import org.glavo.url.pattern.WebURLPatternResult;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
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

/// Selected WPT URLPattern data tests.
///
/// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt/urlpatterntestdata.json
@NotNullByDefault
public final class WebURLPatternWptTest {
    /// System property pointing to downloaded Ada URLPattern WPT resource JSON files.
    private static final String ADA_URL_PATTERN_WPT_RESOURCES_PROPERTY =
            "org.glavo.url.ada.urlpattern.wpt.resources";

    /// Test indexes selected to cover literal, wildcard, base URL, and named group behavior.
    /// Cases with custom regular-expression groups are skipped until standard-compatible regex semantics are available.
    private static final int[] SELECTED_INDEXES = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            12, 13, 14, 15, 16, 17, 18, 19,
            29, 31, 32, 33, 35
    };

    /// Runs selected WPT URLPattern cases.
    @TestFactory
    public List<DynamicTest> selectedUrlPatternWptCases() throws IOException {
        JsonArray data = readData();
        List<DynamicTest> tests = new ArrayList<>();
        for (int index : SELECTED_INDEXES) {
            JsonObject testCase = data.get(index).getAsJsonObject();
            tests.add(DynamicTest.dynamicTest("urlpatterntestdata.json[" + index + "]",
                    () -> assertCase(testCase)));
        }
        return tests;
    }

    /// Asserts one selected WPT case.
    private static void assertCase(JsonObject testCase) {
        @Nullable WebURLPattern pattern = compilePattern(testCase.getAsJsonArray("pattern"));
        JsonElement expectedObject = testCase.get("expected_obj");
        if (expectedObject != null && expectedObject.isJsonPrimitive()
                && expectedObject.getAsString().equals("error")) {
            assertNull(pattern);
            return;
        }

        assertNotNull(pattern);
        assertExpectedObject(pattern, expectedObject);

        JsonArray inputs = testCase.getAsJsonArray("inputs");
        if (inputs == null) {
            return;
        }

        @Nullable WebURLPatternResult result = exec(pattern, inputs);
        JsonElement expectedMatch = testCase.get("expected_match");
        if (expectedMatch == null || expectedMatch.isJsonNull()) {
            assertNull(result);
            return;
        }
        assertNotNull(result);
        assertExpectedMatch(result, expectedMatch.getAsJsonObject());
    }

    /// Compiles the WPT `pattern` field.
    private static @Nullable WebURLPattern compilePattern(JsonArray patternArguments) {
        JsonElement first = patternArguments.get(0);
        try {
            if (first.isJsonPrimitive()) {
                String input = first.getAsString();
                @Nullable String baseURL = patternArguments.size() > 1 && patternArguments.get(1).isJsonPrimitive()
                        ? patternArguments.get(1).getAsString()
                        : null;
                return baseURL == null ? WebURLPattern.compile(input) : WebURLPattern.compile(input, baseURL);
            }
            return WebURLPattern.compile(builder(first.getAsJsonObject()));
        } catch (WebURLPatternSyntaxException ignored) {
            return null;
        }
    }

    /// Executes a compiled pattern against the WPT `inputs` field.
    private static @Nullable WebURLPatternResult exec(WebURLPattern pattern, JsonArray inputs) {
        JsonElement first = inputs.get(0);
        if (first.isJsonPrimitive()) {
            if (inputs.size() > 1 && inputs.get(1).isJsonPrimitive()) {
                return pattern.exec(first.getAsString(), inputs.get(1).getAsString());
            }
            return pattern.exec(first.getAsString());
        }
        return pattern.exec(builder(first.getAsJsonObject()));
    }

    /// Converts a WPT init object into a builder.
    private static WebURLPattern.Builder builder(JsonObject object) {
        WebURLPattern.Builder builder = WebURLPattern.newBuilder();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String value = entry.getValue().getAsString();
            switch (entry.getKey()) {
                case "protocol" -> builder.setSchemePattern(value);
                case "username" -> builder.setUsernamePattern(value);
                case "password" -> builder.setPasswordPattern(value);
                case "hostname" -> builder.setHostPattern(value);
                case "port" -> builder.setPortPattern(value);
                case "pathname" -> builder.setPathPattern(value);
                case "search" -> builder.setQueryPattern(value);
                case "hash" -> builder.setFragmentPattern(value);
                case "baseURL" -> builder.setBaseURL(value);
                default -> throw new AssertionError("Unsupported URLPattern field: " + entry.getKey());
            }
        }
        return builder;
    }

    /// Asserts expected compiled object fields.
    private static void assertExpectedObject(WebURLPattern pattern, @Nullable JsonElement expectedObject) {
        if (expectedObject == null || expectedObject.isJsonNull() || expectedObject.isJsonPrimitive()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : expectedObject.getAsJsonObject().entrySet()) {
            assertEquals(entry.getValue().getAsString(), patternField(pattern, entry.getKey()), entry.getKey());
        }
    }

    /// Asserts expected match fields.
    private static void assertExpectedMatch(WebURLPatternResult result, JsonObject expectedMatch) {
        for (Map.Entry<String, JsonElement> entry : expectedMatch.entrySet()) {
            if (entry.getKey().equals("inputs")) {
                continue;
            }
            WebURLPatternComponentResult actual = resultComponent(result, entry.getKey());
            JsonObject expectedComponent = entry.getValue().getAsJsonObject();
            assertEquals(expectedComponent.get("input").getAsString(), actual.group(), entry.getKey());

            JsonObject expectedGroups = expectedComponent.getAsJsonObject("groups");
            for (Map.Entry<String, JsonElement> expectedGroup : expectedGroups.entrySet()) {
                assertEquals(expectedGroup.getValue().getAsString(), actual.getWebGroup(expectedGroup.getKey()),
                        entry.getKey() + "." + expectedGroup.getKey());
            }
        }
    }

    /// Returns one compiled pattern field.
    private static String patternField(WebURLPattern pattern, String field) {
        return switch (field) {
            case "protocol" -> pattern.getSchemePattern();
            case "username" -> pattern.getUsernamePattern();
            case "password" -> pattern.getPasswordPattern();
            case "hostname" -> pattern.getHostPattern();
            case "port" -> pattern.getPortPattern();
            case "pathname" -> pattern.getPathPattern();
            case "search" -> pattern.getQueryPattern();
            case "hash" -> pattern.getFragmentPattern();
            default -> throw new AssertionError("Unsupported URLPattern result field: " + field);
        };
    }

    /// Returns one component result.
    private static WebURLPatternComponentResult resultComponent(WebURLPatternResult result, String field) {
        return switch (field) {
            case "protocol" -> result.protocol();
            case "username" -> result.username();
            case "password" -> result.password();
            case "hostname" -> result.hostname();
            case "port" -> result.port();
            case "pathname" -> result.pathname();
            case "search" -> result.search();
            case "hash" -> result.hash();
            default -> throw new AssertionError("Unsupported URLPattern match field: " + field);
        };
    }

    /// Reads Ada's vendored WPT data.
    private static JsonArray readData() throws IOException {
        try (Reader reader = Files.newBufferedReader(resourcePath("urlpatterntestdata.json"),
                StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    /// Resolves a downloaded Ada URLPattern WPT resource path.
    private static Path resourcePath(String resourceName) {
        String resources = System.getProperty(ADA_URL_PATTERN_WPT_RESOURCES_PROPERTY);
        if (resources == null || resources.isEmpty()) {
            throw new IllegalStateException("Missing system property: " + ADA_URL_PATTERN_WPT_RESOURCES_PROPERTY);
        }
        return Path.of(resources, resourceName);
    }
}
