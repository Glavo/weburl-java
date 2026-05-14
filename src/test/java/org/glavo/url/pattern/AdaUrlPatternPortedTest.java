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

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests ported from Ada URLPattern tests.
///
/// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp
@NotNullByDefault
public final class AdaUrlPatternPortedTest {
    /// Tests Ada's regression for matching a bare query input against a base-compiled pattern.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L74-L81
    @Test
    public void rejectsBareQueryInputWithoutThrowing() {
        WebURLPattern pattern = WebURLPattern.compile("/foo", "http://example.com");

        assertFalse(pattern.test("?"));
        assertNull(pattern.match("?"));
    }

    /// Tests Ada's multiple capture group mapping regressions.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L83-L146
    @Test
    public void mapsMultipleCaptureGroups() {
        assertPathGroups("/:a/:b", "/foo/bar", "a", "foo", "b", "bar");
        assertPathGroups("/:a([a-z]+)/:b", "/hello/world", "a", "hello", "b", "world");
        assertPathGroups("/:a/:b/:c", "/x/y/z", "a", "x", "b", "y", "c", "z");
    }

    /// Tests Ada's nested parenthesis rejection cases.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L150-L176
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            /:id((.))
            /((.))
            /:id(a(b)c)
            """)
    public void rejectsNestedParenthesesPatterns(String pathPattern) {
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.newBuilder().setPathPattern(pathPattern).build());
    }

    /// Tests Ada's wildcard group regression for empty component inputs.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L179-L276
    @Test
    public void capturesEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern("/foo/bar").build();
        WebURLPattern.Result result = requireMatch(pattern.match(WebURLPattern.newBuilder().setPathPattern("/foo/bar")));

        assertWildcardComponent(result.getScheme(), "");
        assertWildcardComponent(result.getUsername(), "");
        assertWildcardComponent(result.getPassword(), "");
        assertWildcardComponent(result.getHost(), "");
        assertWildcardComponent(result.getPort(), "");
        assertLiteralComponent(result.getPath(), "/foo/bar");
        assertWildcardComponent(result.getQuery(), "");
        assertWildcardComponent(result.getFragment(), "");
    }

    /// Tests Ada's wildcard group regression for non-empty URL string inputs.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L280-L303
    @Test
    public void capturesNonEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern("/foo/bar").build();
        WebURLPattern.Result result = requireMatch(pattern.match("https://example.com/foo/bar"));

        assertWildcardComponent(result.getScheme(), "https");
        assertWildcardComponent(result.getHost(), "example.com");
        assertLiteralComponent(result.getPath(), "/foo/bar");
    }

    /// Tests Ada's basic component pattern getter case.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L321-L334
    @Test
    public void compilesBasicPathnamePattern() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern("/books").build();

        assertEquals("*", pattern.getSchemePattern());
        assertEquals("*", pattern.getHostPattern());
        assertEquals("*", pattern.getUsernamePattern());
        assertEquals("*", pattern.getPasswordPattern());
        assertEquals("*", pattern.getPortPattern());
        assertEquals("/books", pattern.getPathPattern());
        assertEquals("*", pattern.getQueryPattern());
        assertEquals("*", pattern.getFragmentPattern());
        assertFalse(pattern.hasRegExpGroups());
    }

    /// Tests Ada's `has_regexp_groups` component matrix.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L339-L393
    @ParameterizedTest
    @CsvSource({
            "scheme",
            "username",
            "password",
            "host",
            "port",
            "path",
            "query",
            "fragment"
    })
    public void reportsRegExpGroupsForComponents(String component) {
        assertFalse(patternWith(component, "*").hasRegExpGroups());
        assertFalse(patternWith(component, ":foo").hasRegExpGroups());
        assertFalse(patternWith(component, ":foo?").hasRegExpGroups());
        assertTrue(patternWith(component, ":foo(hi)").hasRegExpGroups());
        assertTrue(patternWith(component, "(hi)").hasRegExpGroups());

        if (!component.equals("scheme") && !component.equals("port")) {
            assertFalse(patternWith(component, "a-{:hello}-z-*-a").hasRegExpGroups());
            assertTrue(patternWith(component, "a-(hi)-z-(lo)-a").hasRegExpGroups());
        }
    }

    /// Tests Ada's `has_regexp_groups` composite pathname cases.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L385-L391
    @Test
    public void reportsRegExpGroupsForCompositePathPatterns() {
        assertFalse(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz?/b/*").build()
                .hasRegExpGroups());
        assertTrue(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*").build()
                .hasRegExpGroups());
    }

    /// Creates a pattern with one component.
    private static WebURLPattern patternWith(String component, String pattern) {
        return builderWith(component, pattern).build();
    }

    /// Creates a builder with one component.
    private static WebURLPattern.Builder builderWith(String component, String pattern) {
        WebURLPattern.Builder builder = WebURLPattern.newBuilder();
        switch (component) {
            case "scheme" -> builder.setSchemePattern(pattern);
            case "username" -> builder.setUsernamePattern(pattern);
            case "password" -> builder.setPasswordPattern(pattern);
            case "host" -> builder.setHostPattern(pattern);
            case "port" -> builder.setPortPattern(pattern);
            case "path" -> builder.setPathPattern(pattern);
            case "query" -> builder.setQueryPattern(pattern);
            case "fragment" -> builder.setFragmentPattern(pattern);
            default -> throw new AssertionError("Unsupported component: " + component);
        }
        return builder;
    }

    /// Asserts ordered path capture groups.
    private static void assertPathGroups(String patternText, String input, String... expectedPairs) {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern(patternText).build();
        WebURLPattern.Result result = requireMatch(pattern.match(WebURLPattern.newBuilder().setPathPattern(input)));

        WebURLPattern.ComponentResult path = result.getPath();
        assertEquals(input, path.group());
        assertEquals(expectedPairs.length / 2, path.groupCount());
        for (int i = 0; i < expectedPairs.length; i += 2) {
            String name = expectedPairs[i];
            String value = expectedPairs[i + 1];
            assertEquals(value, path.getWebGroup(name));
            assertEquals(value, path.group((i / 2) + 1));
        }
    }

    /// Asserts a wildcard component match.
    private static void assertWildcardComponent(WebURLPattern.ComponentResult component, String input) {
        assertEquals(input, component.group());
        assertEquals(input, component.group(0));
        assertEquals(1, component.groupCount());
        assertEquals(input, component.group(1));
        assertEquals(0, component.start());
        assertEquals(input.length(), component.end());
        assertEquals(Map.of("0", input), component.getWebGroups());
        assertEquals(input, component.getWebGroup(0));
    }

    /// Asserts a literal component match.
    private static void assertLiteralComponent(WebURLPattern.ComponentResult component, String input) {
        assertEquals(input, component.group());
        assertEquals(input, component.group(0));
        assertEquals(0, component.groupCount());
        assertEquals(Map.of(), component.getWebGroups());
        assertNull(component.getWebGroup(0));
        assertThrows(IndexOutOfBoundsException.class, () -> component.group(1));
        assertThrows(IndexOutOfBoundsException.class, () -> component.getWebGroup(-1));
    }

    /// Requires a non-null match result.
    private static WebURLPattern.Result requireMatch(@Nullable WebURLPattern.Result result) {
        assertNotNull(result);
        return result;
    }
}
