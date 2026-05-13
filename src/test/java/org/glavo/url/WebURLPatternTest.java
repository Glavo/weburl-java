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
        assertEquals("42", stringResult.getPath().getWebGroup("id"));
        assertEquals("java", stringResult.getQuery().getWebGroup("term"));
        assertEquals("ignored", stringResult.getFragment().getWebGroup(0));

        assertTrue(pattern.test(WebURL.parse("https://example.com/books/7?q=url")));

        WebURLPattern.Result componentResult = requireMatch(pattern.exec(WebURLPattern.newBuilder()
                .setSchemePattern("https")
                .setHostPattern("example.com")
                .setPathPattern("/books/99")
                .setQueryPattern("q=patterns")));
        assertEquals("99", componentResult.getPath().getWebGroup("id"));
        assertEquals("patterns", componentResult.getQuery().getWebGroup("term"));
    }

    /// Tests default wildcard components and wildcard group `0`.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L209-L276
    @Test
    public void capturesEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/foo/bar"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPathPattern("/foo/bar")));

        assertEquals("", result.getScheme().group());
        assertEquals("", result.getScheme().group(0));
        assertEquals(1, result.getScheme().groupCount());
        assertEquals("", result.getScheme().group(1));
        assertEquals(0, result.getScheme().start());
        assertEquals(0, result.getScheme().end());
        assertEquals(Map.of("0", ""), result.getScheme().getWebGroups());
        assertEquals("", result.getScheme().getWebGroup(0));
        assertEquals("", result.getUsername().group());
        assertEquals(Map.of("0", ""), result.getUsername().getWebGroups());
        assertEquals("", result.getPassword().group());
        assertEquals(Map.of("0", ""), result.getPassword().getWebGroups());
        assertEquals("", result.getHost().group());
        assertEquals(Map.of("0", ""), result.getHost().getWebGroups());
        assertEquals("", result.getPort().group());
        assertEquals(Map.of("0", ""), result.getPort().getWebGroups());
        assertEquals("/foo/bar", result.getPath().group());
        assertEquals(0, result.getPath().groupCount());
        assertEquals(Map.of(), result.getPath().getWebGroups());
        assertThrows(IndexOutOfBoundsException.class, () -> result.getPath().group(1));
        assertNull(result.getPath().getWebGroup(0));
        assertThrows(IndexOutOfBoundsException.class, () -> result.getPath().getWebGroup(-1));
        assertEquals("", result.getQuery().group());
        assertEquals(Map.of("0", ""), result.getQuery().getWebGroups());
        assertEquals("", result.getFragment().group());
        assertEquals(Map.of("0", ""), result.getFragment().getWebGroups());
    }

    /// Tests wildcard captures with a complete URL string.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L280-L303
    @Test
    public void capturesNonEmptyWildcardGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/foo/bar"));
        WebURLPattern.Result result = requireMatch(pattern.exec("https://example.com/foo/bar"));

        assertEquals("https", result.getScheme().group());
        assertEquals(Map.of("0", "https"), result.getScheme().getWebGroups());
        assertEquals("https", result.getScheme().getWebGroup(0));
        assertEquals("https", result.getScheme().group(1));
        assertEquals("example.com", result.getHost().group());
        assertEquals(Map.of("0", "example.com"), result.getHost().getWebGroups());
    }

    /// Tests multiple named groups.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L83-L146
    @Test
    public void mapsMultipleCaptureGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/:a/:b/:c"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPathPattern("/x/y/z")));

        assertEquals("x", result.getPath().getWebGroup("a"));
        assertEquals("y", result.getPath().getWebGroup("b"));
        assertEquals("z", result.getPath().getWebGroup("c"));
        assertEquals("/x/y/z", result.getPath().group());
        assertEquals("x", result.getPath().group(1));
        assertEquals("y", result.getPath().group(2));
        assertEquals("z", result.getPath().group(3));
    }

    /// Tests named groups and numeric wildcard groups together.
    @Test
    public void mapsNamedAndNumericCaptureGroups() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/:name/*"));
        WebURLPattern.Result result = requireMatch(pattern.exec(WebURLPattern.newBuilder().setPathPattern("/x/y/z")));

        assertEquals("x", result.getPath().getWebGroup("name"));
        assertEquals("y/z", result.getPath().getWebGroup(0));
        assertEquals("x", result.getPath().group(1));
        assertEquals("y/z", result.getPath().group(2));
        assertEquals(Map.of("name", "x", "0", "y/z"), result.getPath().getWebGroups());
    }

    /// Tests `hasRegExpGroups`.
    ///
    /// Source: https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt_urlpattern_tests.cpp#L339-L393
    @Test
    public void reportsRegExpGroups() {
        assertFalse(WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz?/b/*"))
                .hasRegExpGroups());
        assertTrue(WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/a/:foo([0-9]+)/b"))
                .hasRegExpGroups());
    }

    /// Tests the default supported regular-expression subset.
    @Test
    public void supportsDefaultRegularExpressionSubset() {
        WebURLPattern pattern = WebURLPattern.compile(WebURLPattern.newBuilder().setPathPattern("/books/:id([0-9]+)"));
        WebURLPattern.Result result = requireMatch(
                pattern.exec(WebURLPattern.newBuilder().setPathPattern("/books/42")));

        assertEquals("42", result.getPath().getWebGroup("id"));
        assertEquals("42", result.getPath().group(1));
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/123")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/abc")));
        assertNull(WebURLPattern.tryCompile(WebURLPattern.newBuilder().setPathPattern("/books/:id(a(?:b))")));
    }

    /// Tests the reject regular-expression policy.
    @Test
    public void rejectsCustomRegularExpressionGroupsWithRejectPolicy() {
        WebURLPatternParser parser = WebURLPatternParser.getDefault()
                .withRegExpPolicy(WebURLPatternParser.RegExpPolicy.REJECT);

        assertThrows(WebURLPatternSyntaxException.class,
                () -> parser.compile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*")));
        assertNull(parser.tryCompile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz([a-z]+)?/b/*")));
        assertFalse(parser.compile(WebURLPattern.newBuilder().setPathPattern("/a/:foo/:baz?/b/*"))
                .hasRegExpGroups());
    }

    /// Tests the Java regular-expression policy.
    @Test
    public void supportsJavaRegularExpressionPolicy() {
        WebURLPatternParser parser = WebURLPatternParser.getDefault()
                .withRegExpPolicy(WebURLPatternParser.RegExpPolicy.JAVA);
        WebURLPattern pattern = parser.compile(WebURLPattern.newBuilder().setPathPattern("/books/:id([0-9]++)"));

        assertTrue(pattern.hasRegExpGroups());
        assertTrue(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/42")));
        assertFalse(pattern.test(WebURLPattern.newBuilder().setPathPattern("/books/abc")));
        assertThrows(WebURLPatternSyntaxException.class,
                () -> parser.compile(WebURLPattern.newBuilder().setPathPattern("/books/:id([)")));
        assertNull(parser.tryCompile(WebURLPattern.newBuilder().setPathPattern("/books/:id([)")));
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
