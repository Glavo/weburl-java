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
                .setScheme("https")
                .setUsername("user")
                .setPassword("pass")
                .setHost("example.com")
                .setPort("8080")
                .setPath("/books/:id")
                .setQuery("q=:term")
                .setFragment("section"));

        assertEquals("https", pattern.getScheme());
        assertEquals("user", pattern.getUsername());
        assertEquals("pass", pattern.getPassword());
        assertEquals("example.com", pattern.getHost());
        assertEquals("8080", pattern.getPort());
        assertEquals("/books/:id", pattern.getPath());
        assertEquals("q=:term", pattern.getQuery());
        assertEquals("section", pattern.getFragment());

        assertEquals("https", pattern.getWebProtocol());
        assertEquals("user", pattern.getWebUsername());
        assertEquals("pass", pattern.getWebPassword());
        assertEquals("example.com", pattern.getWebHostname());
        assertEquals("8080", pattern.getWebPort());
        assertEquals("/books/:id", pattern.getWebPathname());
        assertEquals("q=:term", pattern.getWebSearch());
        assertEquals("section", pattern.getWebHash());
    }

    /// Tests shorthand string compilation with a base URL.
    @Test
    public void compilesShorthandWithBaseUrl() {
        WebURLPattern pattern = WebURLPattern.compile("./books/:id", "https://example.com/library/index.html");

        assertEquals("https", pattern.getWebProtocol());
        assertEquals("example.com", pattern.getWebHostname());
        assertEquals("/library/books/:id", pattern.getWebPathname());
        assertTrue(pattern.test("https://example.com/library/books/123"));
        assertFalse(pattern.test("https://example.com/books/123"));
    }

    /// Tests shorthand string compilation for an absolute URL pattern.
    @Test
    public void compilesAbsoluteShorthandString() {
        WebURLPattern pattern = WebURLPattern.compile("https://example.com/users/:id");

        assertEquals("https", pattern.getWebProtocol());
        assertEquals("example.com", pattern.getWebHostname());
        assertEquals("/users/:id", pattern.getWebPathname());
        assertTrue(pattern.test("https://example.com/users/alice"));
        assertFalse(pattern.test("http://example.com/users/alice"));
    }

    /// Tests matching URL strings, parsed URLs, and component builder input.
    @Test
    public void matchesInputsAndCapturesGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setPath("/books/:id")
                .setQuery("q=:term"));

        WebURLPattern.Result stringResult = requireMatch(
                pattern.exec("https://example.com/books/42?q=java#ignored"));
        assertEquals("42", stringResult.pathname().groups().get("id"));
        assertEquals("java", stringResult.search().groups().get("term"));
        assertEquals("ignored", stringResult.hash().groups().get("0"));

        assertTrue(pattern.test(WebURL.parse("https://example.com/books/7?q=url")));

        WebURLPattern.Result componentResult = requireMatch(pattern.exec(WebURLPattern.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setPath("/books/99")
                .setQuery("q=patterns")));
        assertEquals("99", componentResult.pathname().groups().get("id"));
        assertEquals("patterns", componentResult.search().groups().get("term"));
    }

    /// Tests default wildcard components and wildcard group `0`.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L209-L276
    @Test
    public void capturesEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/foo/bar"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPath("/foo/bar")));

        assertEquals("", result.protocol().input());
        assertEquals(Map.of("0", ""), result.protocol().groups());
        assertEquals("", result.username().input());
        assertEquals(Map.of("0", ""), result.username().groups());
        assertEquals("", result.password().input());
        assertEquals(Map.of("0", ""), result.password().groups());
        assertEquals("", result.hostname().input());
        assertEquals(Map.of("0", ""), result.hostname().groups());
        assertEquals("", result.port().input());
        assertEquals(Map.of("0", ""), result.port().groups());
        assertEquals("/foo/bar", result.pathname().input());
        assertEquals(Map.of(), result.pathname().groups());
        assertEquals("", result.search().input());
        assertEquals(Map.of("0", ""), result.search().groups());
        assertEquals("", result.hash().input());
        assertEquals(Map.of("0", ""), result.hash().groups());
    }

    /// Tests wildcard captures with a complete URL string.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L280-L303
    @Test
    public void capturesNonEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/foo/bar"));
        WebURLPattern.Result result = requireMatch(pattern.exec("https://example.com/foo/bar"));

        assertEquals("https", result.protocol().input());
        assertEquals(Map.of("0", "https"), result.protocol().groups());
        assertEquals("example.com", result.hostname().input());
        assertEquals(Map.of("0", "example.com"), result.hostname().groups());
    }

    /// Tests multiple named groups.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L83-L146
    @Test
    public void mapsMultipleCaptureGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/:a/:b/:c"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPath("/x/y/z")));

        assertEquals("x", result.pathname().groups().get("a"));
        assertEquals("y", result.pathname().groups().get("b"));
        assertEquals("z", result.pathname().groups().get("c"));
    }

    /// Tests `hasRegExpGroups` without custom regular-expression support.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L339-L393
    @Test
    public void reportsRegExpGroups() {
        assertFalse(WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/a/:foo/:baz?/b/*"))
                .hasRegExpGroups());
    }

    /// Tests that custom regular-expression groups are rejected until standard-compatible semantics are available.
    @Test
    public void rejectsCustomRegularExpressionGroups() {
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/a/:foo/:baz([a-z]+)?/b/*")));
        assertNull(WebURLPattern.tryCompile(WebURLPattern.newBuilder().setPath("/a/:foo/:baz([a-z]+)?/b/*")));
    }

    /// Tests case-insensitive matching.
    @Test
    public void supportsIgnoreCase() {
        WebURLPattern sensitive = WebURLPattern.compile(
                WebURLPattern.newBuilder().setPath("/Books"),
                WebURLPattern.Options.of(false));
        WebURLPattern insensitive = WebURLPattern.compile(
                WebURLPattern.newBuilder().setPath("/Books"),
                WebURLPattern.Options.of(true));

        assertFalse(sensitive.test(WebURLPattern.newBuilder().setPath("/books")));
        assertTrue(insensitive.test(WebURLPattern.newBuilder().setPath("/books")));
        assertTrue(insensitive.isIgnoreCase());
    }

    /// Tests default port normalization.
    @Test
    public void normalizesDefaultPortPatterns() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setPort("443")
                .setPath("/"));

        assertEquals("", pattern.getWebPort());
        assertTrue(pattern.test("https://example.com/"));
        assertTrue(pattern.test("https://example.com:443/"));
    }

    /// Tests URL port canonicalization in pattern components.
    @Test
    public void canonicalizesPortPatterns() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setPort("080")
                .setPath("/"));

        assertEquals("80", pattern.getWebPort());
        assertTrue(pattern.test("https://example.com:80/"));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPort("80x")));
    }

    /// Tests URL IPv6 host canonicalization in pattern components.
    @Test
    public void canonicalizesIpv6HostPatterns() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder()
                .setScheme("http")
                .setHost("[0:0:0:0:0:0:0:1]")
                .setPath("/"));

        assertEquals("[::1]", pattern.getWebHostname());
        assertTrue(pattern.test("http://[::1]/"));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setHost("[::fffff]")));
    }

    /// Tests explicit empty component patterns.
    @Test
    public void matchesEmptyComponentsExactly() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPort(""));

        assertEquals("", pattern.getWebPort());
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPort("")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPort("8080")));
    }

    /// Tests invalid pattern handling.
    @Test
    public void rejectsInvalidPatterns() {
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/:id((.))")));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setScheme("1bad")));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/:id([)")));
        assertNull(WebURLPattern.tryCompile(WebURLPattern.newBuilder().setPath("/:id((.))")));
    }

    /// Tests invalid match input handling.
    @Test
    public void returnsNullForInvalidInputs() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPath("/foo"));

        assertFalse(pattern.test("not a url"));
        assertNull(pattern.exec("not a url"));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPort("99999")));
        assertNull(pattern.exec(WebURLPattern.newBuilder().setPort("99999")));
    }

    /// Requires a non-null match result.
    private static WebURLPattern.Result requireMatch(@Nullable WebURLPattern.Result result) {
        assertNotNull(result);
        return result;
    }
}
