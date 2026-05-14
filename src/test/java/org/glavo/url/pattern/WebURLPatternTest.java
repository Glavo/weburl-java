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

import org.glavo.url.WebURL;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `WebURLPattern`.
@NotNullByDefault
public final class WebURLPatternTest {
    /// Tests component builder compilation and getters.
    @Test
    public void compilesBuilderPatterns() {
        WebURLPattern pattern = WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setUsernamePattern("user")
                .setPasswordPattern("pass")
                .setHostPattern("example.com")
                .setPortPattern("8080")
                .setPathPattern("/books/:id")
                .setQueryPattern("q=:term")
                .setFragmentPattern("section")
                .build();

        assertEquals("https", pattern.getSchemePattern());
        assertEquals("user", pattern.getUsernamePattern());
        assertEquals("pass", pattern.getPasswordPattern());
        assertEquals("example.com", pattern.getHostPattern());
        assertEquals("8080", pattern.getPortPattern());
        assertEquals("/books/:id", pattern.getPathPattern());
        assertEquals("q=:term", pattern.getQueryPattern());
        assertEquals("section", pattern.getFragmentPattern());
    }

    /// Tests shorthand string compilation with a base URL.
    @Test
    public void compilesShorthandWithBaseUrl() {
        WebURLPattern pattern = WebURLPattern.compile("./books/:id", "https://example.com/library/index.html");

        assertEquals("https", pattern.getSchemePattern());
        assertEquals("example.com", pattern.getHostPattern());
        assertEquals("/library/books/:id", pattern.getPathPattern());
        assertTrue(pattern.test("https://example.com/library/books/123"));
        assertFalse(pattern.test("https://example.com/books/123"));
    }

    /// Tests shorthand string compilation for an absolute URL pattern.
    @Test
    public void compilesAbsoluteShorthandString() {
        WebURLPattern pattern = WebURLPattern.compile("https://example.com/users/:id");

        assertEquals("https", pattern.getSchemePattern());
        assertEquals("example.com", pattern.getHostPattern());
        assertEquals("/users/:id", pattern.getPathPattern());
        assertTrue(pattern.test("https://example.com/users/alice"));
        assertFalse(pattern.test("http://example.com/users/alice"));
    }

    /// Tests matching URL strings, parsed URLs, and component builder input.
    @Test
    public void matchesInputsAndCapturesGroups() {
        WebURLPattern pattern = WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPathPattern("/books/:id")
                .setQueryPattern("q=:term")
                .build();

        WebURLPattern.Result stringResult = requireMatch(
                pattern.match("https://example.com/books/42?q=java#ignored"));
        assertEquals("42", stringResult.getPath().getWebGroup("id"));
        assertEquals("java", stringResult.getQuery().getWebGroup("term"));
        assertEquals("ignored", stringResult.getFragment().getWebGroup(0));

        assertTrue(pattern.test(WebURL.parse("https://example.com/books/7?q=url")));

        WebURLPattern.Result componentResult = requireMatch(pattern.match(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPathPattern("/books/99")
                .setQueryPattern("q=patterns")));
        assertEquals("99", componentResult.getPath().getWebGroup("id"));
        assertEquals("patterns", componentResult.getQuery().getWebGroup("term"));
    }

    /// Tests named groups and numeric wildcard groups together.
    @Test
    public void mapsNamedAndNumericCaptureGroups() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern("/:name/*").build();
        WebURLPattern.Result result = requireMatch(pattern.match(WebURLPattern.newBuilder().setPathPattern("/x/y/z")));

        assertEquals("x", result.getPath().getWebGroup("name"));
        assertEquals("y/z", result.getPath().getWebGroup(0));
        assertEquals("x", result.getPath().group(1));
        assertEquals("y/z", result.getPath().group(2));
        assertEquals(Map.of("name", "x", "0", "y/z"), result.getPath().getWebGroups());
    }

    /// Tests the default supported regular-expression subset.
    @Test
    public void supportsDefaultRegularExpressionSubset() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern("/books/:id([0-9]+)").build();
        WebURLPattern.Result result = requireMatch(
                pattern.match(WebURLPattern.newBuilder().setPathPattern("/books/42")));

        assertEquals("42", result.getPath().getWebGroup("id"));
        assertEquals("42", result.getPath().group(1));
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/123")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/abc")));
        assertTrue(pattern.isStandardCompatible());

        WebURLPattern nonCapturing = WebURLPattern.newBuilder().setPathPattern("/books/:id(a(?:b))").build();
        WebURLPattern.Result nonCapturingResult = requireMatch(
                nonCapturing.match(WebURLPattern.newBuilder().setPathPattern("/books/ab")));
        assertEquals("ab", nonCapturingResult.getPath().getWebGroup("id"));

        WebURLPattern lookbehind = WebURLPattern.newBuilder()
                .setPathPattern("/books/:id([a-z]{3}(?<=foo)[0-9]+)")
                .build();
        assertTrue(lookbehind.isStandardCompatible());
        assertTrue(lookbehind.test(WebURLPattern.newBuilder().setPathPattern("/books/foo42")));
        assertFalse(lookbehind.test(WebURLPattern.newBuilder().setPathPattern("/books/bar42")));

        WebURLPattern negativeLookbehind = WebURLPattern.newBuilder()
                .setPathPattern("/books/:id([a-z]{3}(?<!bar)[0-9]+)")
                .build();
        assertTrue(negativeLookbehind.test(WebURLPattern.newBuilder().setPathPattern("/books/foo42")));
        assertFalse(negativeLookbehind.test(WebURLPattern.newBuilder().setPathPattern("/books/bar42")));
    }

    /// Tests the reject regular-expression policy.
    @Test
    public void rejectsCustomRegularExpressionGroupsWithRejectPolicy() {
        WebURLPatternParser parser = WebURLPatternParser.getDefault()
                .withRegExpPolicy(WebURLPatternParser.RegExpPolicy.REJECT);

        assertThrows(WebURLPatternSyntaxException.class,
                () -> parser.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*").build());
        assertNull(parser.tryCompile(parser.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*")));
        assertFalse(parser.newBuilder().setPathPattern("/a/:foo/:baz?/b/*").build()
                .hasRegExpGroups());
    }

    /// Tests the Java regular-expression policy.
    @Test
    public void supportsJavaRegularExpressionPolicy() {
        WebURLPatternParser parser = WebURLPatternParser.getDefault()
                .withRegExpPolicy(WebURLPatternParser.RegExpPolicy.JAVA);
        WebURLPattern pattern = parser.newBuilder().setPathPattern("/books/:id([0-9]++)").build();

        assertTrue(pattern.hasRegExpGroups());
        assertFalse(pattern.isStandardCompatible());
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/42")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/abc")));
        assertTrue(parser.newBuilder().setPathPattern("/books/:id").build()
                .isStandardCompatible());
        assertThrows(WebURLPatternSyntaxException.class,
                () -> parser.newBuilder().setPathPattern("/books/:id([)").build());
        assertNull(parser.tryCompile(parser.newBuilder().setPathPattern("/books/:id([)")));
    }

    /// Tests parser regular-expression policy configuration.
    @Test
    public void configuresRegularExpressionPolicy() {
        WebURLPatternParser javaParser = WebURLPatternParser.getDefault()
                .withRegExpPolicy(WebURLPatternParser.RegExpPolicy.JAVA);
        WebURLPatternParser insensitiveJavaParser = javaParser.withIgnoreCase();

        assertEquals(WebURLPatternParser.RegExpPolicy.SUPPORTED,
                WebURLPatternParser.getDefault().getRegExpPolicy());
        assertEquals(WebURLPatternParser.RegExpPolicy.JAVA, javaParser.getRegExpPolicy());
        assertFalse(javaParser.isIgnoreCase());
        assertTrue(insensitiveJavaParser.isIgnoreCase());
        assertEquals(WebURLPatternParser.RegExpPolicy.JAVA, insensitiveJavaParser.getRegExpPolicy());
        assertEquals(insensitiveJavaParser, WebURLPatternParser.getDefault()
                .withIgnoreCase()
                .withRegExpPolicy(WebURLPatternParser.RegExpPolicy.JAVA));
    }

    /// Tests case-insensitive matching.
    @Test
    public void supportsIgnoreCase() {
        WebURLPattern sensitive = WebURLPatternParser.getDefault()
                .newBuilder()
                .setPathPattern("/Books")
                .build();
        WebURLPattern insensitive = WebURLPatternParser.getDefault().withIgnoreCase()
                .newBuilder()
                .setPathPattern("/Books")
                .build();

        assertFalse(sensitive.test(WebURLPattern.newBuilder().setPathPattern("/books")));
        assertFalse(sensitive.isIgnoreCase());
        assertTrue(insensitive.test(WebURLPattern.newBuilder().setPathPattern("/books")));
        assertTrue(WebURLPatternParser.getDefault().withIgnoreCase(true).isIgnoreCase());
        assertEquals(WebURLPatternParser.getDefault(), WebURLPatternParser.getDefault().withIgnoreCase(false));
        assertTrue(insensitive.isIgnoreCase());
    }

    /// Tests default port normalization.
    @Test
    public void normalizesDefaultPortPatterns() {
        WebURLPattern pattern = WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPortPattern("443")
                .setPathPattern("/")
                .build();

        assertEquals("", pattern.getPortPattern());
        assertTrue(pattern.test("https://example.com/"));
        assertTrue(pattern.test("https://example.com:443/"));
    }

    /// Tests URL port canonicalization in pattern components.
    @Test
    public void canonicalizesPortPatterns() {
        WebURLPattern pattern = WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPortPattern("080")
                .setPathPattern("/")
                .build();

        assertEquals("80", pattern.getPortPattern());
        assertTrue(pattern.test("https://example.com:80/"));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.newBuilder().setPortPattern("80x").build());
    }

    /// Tests URL IPv6 host canonicalization in pattern components.
    @Test
    public void canonicalizesIpv6HostPatterns() {
        WebURLPattern pattern = WebURLPattern.newBuilder()
                .setSchemePattern("http")
                .setHostPattern("[0\\:0\\:0\\:0\\:0\\:0\\:0\\:1]")
                .setPathPattern("/")
                .build();

        assertEquals("[\\:\\:1]", pattern.getHostPattern());
        assertTrue(pattern.test("http://[::1]/"));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.newBuilder().setHostPattern("[\\:\\:fffff]").build());
    }

    /// Tests explicit empty component patterns.
    @Test
    public void matchesEmptyComponentsExactly() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPortPattern("").build();

        assertEquals("", pattern.getPortPattern());
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPortPattern("")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPortPattern("8080")));
    }

    /// Tests invalid pattern handling.
    @Test
    public void rejectsInvalidPatterns() {
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.newBuilder().setPathPattern("/:id((.))").build());
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.newBuilder().setSchemePattern("1bad").build());
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.newBuilder().setPathPattern("/:id([)").build());
        assertNull(WebURLPattern.tryCompile(WebURLPattern.newBuilder().setPathPattern("/:id((.))")));
    }

    /// Tests invalid match input handling.
    @Test
    public void returnsNullForInvalidInputs() {
        WebURLPattern pattern = WebURLPattern.newBuilder().setPathPattern("/foo").build();

        assertFalse(pattern.test("not a url"));
        assertNull(pattern.match("not a url"));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPortPattern("99999")));
        assertNull(pattern.match(WebURLPattern.newBuilder().setPortPattern("99999")));
    }

    /// Requires a non-null match result.
    private static WebURLPattern.Result requireMatch(@Nullable WebURLPattern.Result result) {
        assertNotNull(result);
        return result;
    }
}
