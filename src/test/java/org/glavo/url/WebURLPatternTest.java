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

import org.glavo.url.pattern.WebURLPattern;
import org.glavo.url.pattern.WebURLPatternParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
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
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setUsernamePattern("user")
                .setPasswordPattern("pass")
                .setHostPattern("example.com")
                .setPortPattern("8080")
                .setPathPattern("/books/:id")
                .setQueryPattern("q=:term")
                .setFragmentPattern("section"));

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
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPathPattern("/books/:id")
                .setQueryPattern("q=:term"));

        WebURLPattern.Result stringResult = requireMatch(
                pattern.exec("https://example.com/books/42?q=java#ignored"));
        assertEquals("42", stringResult.pathname().getWebGroup("id"));
        assertEquals("java", stringResult.search().getWebGroup("term"));
        assertEquals("ignored", stringResult.hash().getWebGroup(0));

        assertTrue(pattern.test(WebURL.parse("https://example.com/books/7?q=url")));

        WebURLPattern.Result componentResult = requireMatch(pattern.exec(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPathPattern("/books/99")
                .setQueryPattern("q=patterns")));
        assertEquals("99", componentResult.pathname().getWebGroup("id"));
        assertEquals("patterns", componentResult.search().getWebGroup("term"));
    }

    /// Tests default wildcard components and wildcard group `0`.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L209-L276
    @Test
    public void capturesEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/foo/bar"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPathPattern("/foo/bar")));

        assertEquals("", result.protocol().group());
        assertEquals("", result.protocol().group(0));
        assertEquals(1, result.protocol().groupCount());
        assertEquals("", result.protocol().group(1));
        assertEquals(0, result.protocol().start());
        assertEquals(0, result.protocol().end());
        assertEquals(Map.of("0", ""), result.protocol().getWebGroups());
        assertEquals("", result.protocol().getWebGroup(0));
        assertEquals("", result.username().group());
        assertEquals(Map.of("0", ""), result.username().getWebGroups());
        assertEquals("", result.password().group());
        assertEquals(Map.of("0", ""), result.password().getWebGroups());
        assertEquals("", result.hostname().group());
        assertEquals(Map.of("0", ""), result.hostname().getWebGroups());
        assertEquals("", result.port().group());
        assertEquals(Map.of("0", ""), result.port().getWebGroups());
        assertEquals("/foo/bar", result.pathname().group());
        assertEquals(0, result.pathname().groupCount());
        assertEquals(Map.of(), result.pathname().getWebGroups());
        assertThrows(IndexOutOfBoundsException.class, () -> result.pathname().group(1));
        assertNull(result.pathname().getWebGroup(0));
        assertThrows(IndexOutOfBoundsException.class, () -> result.pathname().getWebGroup(-1));
        assertEquals("", result.search().group());
        assertEquals(Map.of("0", ""), result.search().getWebGroups());
        assertEquals("", result.hash().group());
        assertEquals(Map.of("0", ""), result.hash().getWebGroups());
    }

    /// Tests wildcard captures with a complete URL string.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L280-L303
    @Test
    public void capturesNonEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/foo/bar"));
        WebURLPattern.Result result = requireMatch(pattern.exec("https://example.com/foo/bar"));

        assertEquals("https", result.protocol().group());
        assertEquals(Map.of("0", "https"), result.protocol().getWebGroups());
        assertEquals("https", result.protocol().getWebGroup(0));
        assertEquals("https", result.protocol().group(1));
        assertEquals("example.com", result.hostname().group());
        assertEquals(Map.of("0", "example.com"), result.hostname().getWebGroups());
    }

    /// Tests multiple named groups.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L83-L146
    @Test
    public void mapsMultipleCaptureGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/:a/:b/:c"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPathPattern("/x/y/z")));

        assertEquals("x", result.pathname().getWebGroup("a"));
        assertEquals("y", result.pathname().getWebGroup("b"));
        assertEquals("z", result.pathname().getWebGroup("c"));
        assertEquals("/x/y/z", result.pathname().group());
        assertEquals("x", result.pathname().group(1));
        assertEquals("y", result.pathname().group(2));
        assertEquals("z", result.pathname().group(3));
    }

    /// Tests named groups and numeric wildcard groups together.
    @Test
    public void mapsNamedAndNumericCaptureGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/:name/*"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPathPattern("/x/y/z")));

        assertEquals("x", result.pathname().getWebGroup("name"));
        assertEquals("y/z", result.pathname().getWebGroup(0));
        assertEquals("x", result.pathname().group(1));
        assertEquals("y/z", result.pathname().group(2));
        assertEquals(Map.of("name", "x", "0", "y/z"), result.pathname().getWebGroups());
    }

    /// Tests `hasRegExpGroups` without custom regular-expression support.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L339-L393
    @Test
    public void reportsRegExpGroups() {
        assertFalse(WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz?/b/*"))
                .hasRegExpGroups());
    }

    /// Tests that custom regular-expression groups are rejected until standard-compatible semantics are available.
    @Test
    public void rejectsCustomRegularExpressionGroups() {
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*")));
        assertNull(WebURLPattern.tryCompile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*")));
    }

    /// Tests case-insensitive matching.
    @Test
    public void supportsIgnoreCase() {
        WebURLPattern sensitive = WebURLPatternParser.getDefault()
                .compile(WebURLPattern.newBuilder().setPathPattern("/Books"));
        WebURLPattern insensitive = WebURLPatternParser.getDefault().withIgnoreCase()
                .compile(WebURLPattern.newBuilder().setPathPattern("/Books"));

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
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPortPattern("443")
                .setPathPattern("/"));

        assertEquals("", pattern.getPortPattern());
        assertTrue(pattern.test("https://example.com/"));
        assertTrue(pattern.test("https://example.com:443/"));
    }

    /// Tests URL port canonicalization in pattern components.
    @Test
    public void canonicalizesPortPatterns() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPortPattern("080")
                .setPathPattern("/"));

        assertEquals("80", pattern.getPortPattern());
        assertTrue(pattern.test("https://example.com:80/"));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPortPattern("80x")));
    }

    /// Tests URL IPv6 host canonicalization in pattern components.
    @Test
    public void canonicalizesIpv6HostPatterns() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setSchemePattern("http")
                .setHostPattern("[0:0:0:0:0:0:0:1]")
                .setPathPattern("/"));

        assertEquals("[::1]", pattern.getHostPattern());
        assertTrue(pattern.test("http://[::1]/"));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setHostPattern("[::fffff]")));
    }

    /// Tests explicit empty component patterns.
    @Test
    public void matchesEmptyComponentsExactly() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPortPattern(""));

        assertEquals("", pattern.getPortPattern());
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPortPattern("")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPortPattern("8080")));
    }

    /// Tests invalid pattern handling.
    @Test
    public void rejectsInvalidPatterns() {
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/:id((.))")));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setSchemePattern("1bad")));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/:id([)")));
        assertNull(WebURLPattern.tryCompile(WebURLPattern.newBuilder().setPathPattern("/:id((.))")));
    }

    /// Tests invalid match input handling.
    @Test
    public void returnsNullForInvalidInputs() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/foo"));

        assertFalse(pattern.test("not a url"));
        assertNull(pattern.exec("not a url"));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPortPattern("99999")));
        assertNull(pattern.exec(WebURLPattern.newBuilder().setPortPattern("99999")));
    }

    /// Requires a non-null match result.
    private static WebURLPattern.Result requireMatch(@Nullable WebURLPattern.Result result) {
        assertNotNull(result);
        return result;
    }
}
