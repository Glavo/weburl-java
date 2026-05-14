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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// WPT URLPattern data tests.
///
/// Source: https://github.com/web-platform-tests/wpt/blob/ebf8e3069ec4ac6498826bf9066419e46b0f4ac5/urlpattern/resources/urlpatterntestdata.json
@NotNullByDefault
public final class WebURLPatternWptTest {
    /// System property pointing to downloaded WPT resource JSON files.
    private static final String WPT_RESOURCES_PROPERTY = "org.glavo.url.wpt.resources";
    /// Source URL for WPT URLPattern test data.
    private static final String WPT_SOURCE_URL =
            "https://github.com/web-platform-tests/wpt/blob/"
                    + "ebf8e3069ec4ac6498826bf9066419e46b0f4ac5/"
                    + "urlpattern/resources/urlpatterntestdata.json";

    /// Runs WPT URLPattern cases.
    @TestFactory
    public List<DynamicTest> urlPatternWptCases(TestReporter reporter) throws IOException {
        JsonArray data = readData();
        List<DynamicTest> tests = new ArrayList<>();
        for (int index = 0; index < data.size(); index++) {
            JsonElement element = data.get(index);
            if (!element.isJsonObject()) {
                continue;
            }
            int testIndex = index;
            JsonObject testCase = element.getAsJsonObject();
            tests.add(DynamicTest.dynamicTest("urlpatterntestdata.json[" + testIndex + "]",
                    sourceUri(testIndex), () -> assertCase(testIndex, testCase, reporter)));
        }
        return tests;
    }

    /// Asserts one WPT case.
    private static void assertCase(int index, JsonObject testCase, TestReporter reporter) {
        publishMetadata(index, testCase, reporter);
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

        JsonElement expectedMatch = testCase.get("expected_match");
        if (expectedMatch != null && expectedMatch.isJsonPrimitive()
                && expectedMatch.getAsString().equals("error")) {
            assertThrows(IllegalArgumentException.class, () -> match(pattern, inputs));
            return;
        }

        @Nullable WebURLPattern.Result result = match(pattern, inputs);
        if (expectedMatch == null || expectedMatch.isJsonNull()) {
            assertNull(result);
            return;
        }
        assertNotNull(result);
        assertExpectedMatch(result, expectedMatch.getAsJsonObject());
    }

    /// Compiles the WPT `pattern` field.
    private static @Nullable WebURLPattern compilePattern(JsonArray patternArguments) {
        WebURLPatternParser parser = parser(patternOptions(patternArguments));
        if (patternArguments.isEmpty()) {
            return parser.newBuilder().build();
        }

        JsonElement first = patternArguments.get(0);
        try {
            if (first.isJsonPrimitive()) {
                String input = first.getAsString();
                @Nullable String baseURL = patternArguments.size() > 1 && patternArguments.get(1).isJsonPrimitive()
                        ? patternArguments.get(1).getAsString()
                        : null;
                return baseURL == null ? parser.compile(input) : parser.compile(input, baseURL);
            }
            if (patternArguments.size() > 1 && patternArguments.get(1).isJsonPrimitive()) {
                throw new WebURLPatternSyntaxException("URLPattern init object cannot be compiled with a base URL");
            }
            return parser.compile(builder(first.getAsJsonObject()));
        } catch (WebURLPatternSyntaxException ignored) {
            return null;
        }
    }

    /// Matches a compiled pattern against the WPT `inputs` field.
    private static @Nullable WebURLPattern.Result match(WebURLPattern pattern, JsonArray inputs) {
        if (inputs.isEmpty()) {
            return pattern.match(WebURLPattern.newBuilder());
        }
        JsonElement first = inputs.get(0);
        if (first.isJsonPrimitive()) {
            if (inputs.size() > 1 && inputs.get(1).isJsonPrimitive()) {
                return pattern.match(first.getAsString(), inputs.get(1).getAsString());
            }
            return pattern.match(first.getAsString());
        }
        if (inputs.size() > 1 && inputs.get(1).isJsonPrimitive()) {
            throw new IllegalArgumentException("URLPattern init object input cannot be matched with a base URL");
        }
        return pattern.match(builder(first.getAsJsonObject()));
    }

    /// Converts a WPT init object into a builder.
    private static WebURLPattern.Builder builder(JsonObject object) {
        WebURLPattern.Builder builder = WebURLPattern.newBuilder();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            switch (entry.getKey()) {
                case "protocol" -> builder.setSchemePattern(entry.getValue().getAsString());
                case "username" -> builder.setUsernamePattern(entry.getValue().getAsString());
                case "password" -> builder.setPasswordPattern(entry.getValue().getAsString());
                case "hostname" -> builder.setHostPattern(entry.getValue().getAsString());
                case "port" -> builder.setPortPattern(entry.getValue().getAsString());
                case "pathname" -> builder.setPathPattern(entry.getValue().getAsString());
                case "search" -> builder.setQueryPattern(entry.getValue().getAsString());
                case "hash" -> builder.setFragmentPattern(entry.getValue().getAsString());
                case "baseURL" -> builder.setBaseURL(entry.getValue().getAsString());
                case "ignoreCase" -> {
                    // WPT option objects may contain this field, but init objects do not consume it here.
                }
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
    private static void assertExpectedMatch(WebURLPattern.Result result, JsonObject expectedMatch) {
        for (Map.Entry<String, JsonElement> entry : expectedMatch.entrySet()) {
            if (entry.getKey().equals("inputs")) {
                continue;
            }
            WebURLPattern.ComponentResult actual = resultComponent(result, entry.getKey());
            JsonObject expectedComponent = entry.getValue().getAsJsonObject();
            assertEquals(expectedComponent.get("input").getAsString(), actual.group(), entry.getKey());

            JsonObject expectedGroups = expectedComponent.getAsJsonObject("groups");
            for (Map.Entry<String, JsonElement> expectedGroup : expectedGroups.entrySet()) {
                @Nullable String expectedGroupValue = expectedGroup.getValue().isJsonNull()
                        ? null
                        : expectedGroup.getValue().getAsString();
                assertEquals(expectedGroupValue, actual.getWebGroup(expectedGroup.getKey()),
                        entry.getKey() + "." + expectedGroup.getKey());
            }
        }
    }

    /// Returns the parser for WPT URLPattern options.
    private static WebURLPatternParser parser(@Nullable JsonObject options) {
        WebURLPatternParser parser = WebURLPatternParser.getDefault();
        return options != null && booleanValue(options, "ignoreCase") ? parser.withIgnoreCase() : parser;
    }

    /// Returns the WPT URLPattern options object.
    private static @Nullable JsonObject patternOptions(JsonArray patternArguments) {
        if (patternArguments.isEmpty()) {
            return null;
        }
        JsonElement last = patternArguments.get(patternArguments.size() - 1);
        if (!last.isJsonObject()) {
            return null;
        }
        JsonObject object = last.getAsJsonObject();
        return object.has("ignoreCase") ? object : null;
    }

    /// Returns a boolean JSON member, defaulting to false when absent.
    private static boolean booleanValue(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && !element.isJsonNull() && element.getAsBoolean();
    }

    /// Publishes metadata for one WPT case.
    private static void publishMetadata(
            int index,
            JsonObject testCase,
            TestReporter reporter
    ) {
        reporter.publishEntry(Map.of(
                "wpt.index", Integer.toString(index),
                "wpt.source", sourceUri(index).toString()
        ));
        reporter.publishFile("urlpatterntestdata-" + index + ".json", MediaType.APPLICATION_JSON,
                path -> Files.writeString(path, asciiJson(testCase), StandardCharsets.UTF_8));
    }

    /// Returns the source URI for one WPT case.
    private static URI sourceUri(int index) {
        return URI.create(WPT_SOURCE_URL + "#case-" + index);
    }

    /// Returns an ASCII-only JSON representation suitable for test report attachments.
    private static String asciiJson(JsonElement element) {
        String json = element.toString();
        StringBuilder builder = new StringBuilder(json.length());
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c >= 0x20 && c <= 0x7e) {
                builder.append(c);
            } else {
                builder.append("\\u");
                String hex = Integer.toHexString(c);
                for (int j = hex.length(); j < 4; j++) {
                    builder.append('0');
                }
                builder.append(hex);
            }
        }
        return builder.toString();
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
    private static WebURLPattern.ComponentResult resultComponent(WebURLPattern.Result result, String field) {
        return switch (field) {
            case "protocol" -> result.getScheme();
            case "username" -> result.getUsername();
            case "password" -> result.getPassword();
            case "hostname" -> result.getHost();
            case "port" -> result.getPort();
            case "pathname" -> result.getPath();
            case "search" -> result.getQuery();
            case "hash" -> result.getFragment();
            default -> throw new AssertionError("Unsupported URLPattern match field: " + field);
        };
    }

    /// Reads WPT URLPattern data.
    private static JsonArray readData() throws IOException {
        try (Reader reader = Files.newBufferedReader(resourcePath("urlpatterntestdata.json"),
                StandardCharsets.UTF_8)) {
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
}
