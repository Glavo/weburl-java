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
import org.junit.jupiter.api.Assumptions;
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
import java.util.function.Predicate;

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

    /// Runs WPT URLPattern cases.
    @TestFactory
    public List<DynamicTest> urlPatternWptCases() throws IOException {
        JsonArray data = readData();
        List<DynamicTest> tests = new ArrayList<>();
        for (int index = 0; index < data.size(); index++) {
            JsonElement element = data.get(index);
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject testCase = element.getAsJsonObject();
            tests.add(DynamicTest.dynamicTest("urlpatterntestdata.json[" + index + "]",
                    () -> assertCase(testCase)));
        }
        return tests;
    }

    /// Asserts one WPT case.
    private static void assertCase(JsonObject testCase) {
        @Nullable String unsupportedReason = unsupportedReason(testCase);
        Assumptions.assumeTrue(unsupportedReason == null, unsupportedReason);
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
            assertThrows(IllegalArgumentException.class, () -> exec(pattern, inputs));
            return;
        }

        @Nullable WebURLPattern.Result result = exec(pattern, inputs);
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
            return parser.compile(WebURLPattern.newBuilder());
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

    /// Executes a compiled pattern against the WPT `inputs` field.
    private static @Nullable WebURLPattern.Result exec(WebURLPattern pattern, JsonArray inputs) {
        if (inputs.isEmpty()) {
            return pattern.exec(WebURLPattern.newBuilder());
        }
        JsonElement first = inputs.get(0);
        if (first.isJsonPrimitive()) {
            if (inputs.size() > 1 && inputs.get(1).isJsonPrimitive()) {
                return pattern.exec(first.getAsString(), inputs.get(1).getAsString());
            }
            return pattern.exec(first.getAsString());
        }
        if (inputs.size() > 1 && inputs.get(1).isJsonPrimitive()) {
            throw new IllegalArgumentException("URLPattern init object input cannot be matched with a base URL");
        }
        return pattern.exec(builder(first.getAsJsonObject()));
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

    /// Returns the reason why this WPT case is not supported.
    private static @Nullable String unsupportedReason(JsonObject testCase) {
        if (containsNonAsciiHostname(testCase)) {
            return "IDNA hostname canonicalization in URLPattern matching is not fully implemented";
        }
        if (containsString(testCase, value -> value.indexOf('\ufffd') >= 0 || value.contains("%EF%BF%BD"))) {
            return "Replacement-character URLPattern canonicalization is not fully implemented";
        }
        if (containsUnsupportedPortCanonicalization(testCase)) {
            return "URLPattern port truncation and space handling are not fully implemented";
        }
        if (containsString(patternArguments(testCase), value -> value.contains("(?:")
                || value.contains("(?<")
                || value.contains("[[")
                || value.contains("&&")
                || value.contains("--"))) {
            return "Unsupported JavaScript regular-expression syntax";
        }
        if (containsString(patternArguments(testCase), value -> value.startsWith("file:///"))) {
            return "file: constructor string handling is not fully implemented";
        }
        if (containsString(patternArguments(testCase), value -> value.contains("\\:") && value.contains("@"))) {
            return "Escaped userinfo delimiters in constructor strings are not fully implemented";
        }
        if (containsString(patternArguments(testCase), WebURLPatternWptTest::isIpv6HostnamePatternSyntax)) {
            return "IPv6 hostname pattern syntax is not fully implemented";
        }
        if (containsHostnameAsciiTabsOrNewlines(testCase)) {
            return "Hostname ASCII tab and newline canonicalization is not fully implemented";
        }
        if (containsString(patternArguments(testCase), value -> value.contains("{}"))) {
            return "Empty URLPattern parts are not fully implemented";
        }
        return null;
    }

    /// Returns whether a case contains a non-ASCII hostname.
    private static boolean containsNonAsciiHostname(JsonObject testCase) {
        return containsComponentString(testCase, "hostname", WebURLPatternWptTest::containsNonAscii)
                || containsString(testCase, WebURLPatternWptTest::urlAuthorityContainsNonAscii);
    }

    /// Returns whether a case contains unsupported port canonicalization behavior.
    private static boolean containsUnsupportedPortCanonicalization(JsonObject testCase) {
        return containsComponentString(patternArguments(testCase), "port", value -> value.indexOf(' ') >= 0)
                || containsComponentString(inputArguments(testCase), "port", value ->
                value.contains("?") || value.contains("\\") || startsWithDigitAndContainsLetter(value));
    }

    /// Returns whether a case contains hostname ASCII tabs or newlines.
    private static boolean containsHostnameAsciiTabsOrNewlines(JsonObject testCase) {
        return containsComponentString(testCase, "hostname", value ->
                value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\t') >= 0);
    }

    /// Returns whether a pattern string contains IPv6 hostname pattern syntax.
    private static boolean isIpv6HostnamePatternSyntax(String value) {
        return value.contains("[\\") || value.contains("[:") || value.contains("[*")
                || value.contains("[") && value.contains("\\:");
    }

    /// Returns whether a string contains a non-ASCII character.
    private static boolean containsNonAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 0x7f) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a URL-like string contains a non-ASCII authority host.
    private static boolean urlAuthorityContainsNonAscii(String value) {
        int schemeEnd = value.indexOf("://");
        if (schemeEnd < 0) {
            return false;
        }
        int authorityStart = schemeEnd + 3;
        int authorityEnd = value.length();
        for (int i = authorityStart; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '/' || c == '?' || c == '#') {
                authorityEnd = i;
                break;
            }
        }
        int hostStart = value.lastIndexOf('@', authorityEnd - 1);
        hostStart = hostStart >= authorityStart ? hostStart + 1 : authorityStart;
        for (int i = hostStart; i < authorityEnd; i++) {
            if (value.charAt(i) > 0x7f) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a string starts with a digit and contains an ASCII letter.
    private static boolean startsWithDigitAndContainsLetter(String value) {
        if (value.isEmpty() || !Character.isDigit(value.charAt(0))) {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
                return true;
            }
        }
        return false;
    }

    /// Returns the WPT `pattern` arguments.
    private static JsonArray patternArguments(JsonObject testCase) {
        return testCase.getAsJsonArray("pattern");
    }

    /// Returns the WPT `inputs` arguments.
    private static @Nullable JsonArray inputArguments(JsonObject testCase) {
        return testCase.getAsJsonArray("inputs");
    }

    /// Returns whether a component field has a string matching a predicate.
    private static boolean containsComponentString(JsonElement element, String field, Predicate<String> predicate) {
        if (element == null || element.isJsonNull()) {
            return false;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (containsComponentString(child, field, predicate)) {
                    return true;
                }
            }
            return false;
        }
        if (!element.isJsonObject()) {
            return false;
        }

        JsonObject object = element.getAsJsonObject();
        JsonElement value = object.get(field);
        if (value != null && value.isJsonPrimitive() && predicate.test(value.getAsString())) {
            return true;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (containsComponentString(entry.getValue(), field, predicate)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether any string in a JSON value matches a predicate.
    private static boolean containsString(@Nullable JsonElement element, Predicate<String> predicate) {
        if (element == null || element.isJsonNull()) {
            return false;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsJsonPrimitive().isString() && predicate.test(element.getAsString());
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (containsString(child, predicate)) {
                    return true;
                }
            }
            return false;
        }
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            if (containsString(entry.getValue(), predicate)) {
                return true;
            }
        }
        return false;
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
