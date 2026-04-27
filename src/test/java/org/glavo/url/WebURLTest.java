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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `WebURL`.
@NotNullByDefault
public final class WebURLTest {
    /// Tests MDN-style constructor examples.
    @Test
    public void parsesMdnExamples() {
        WebURL a = WebURL.of("/", "https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", a.getHref());

        WebURL b = WebURL.of("https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", b.getHref());

        WebURL c = WebURL.of("en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", c.getHref());

        WebURL d = WebURL.of("/en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", d.getHref());

        WebURL e = WebURL.of("/en-US/docs", "https://developer.mozilla.org/fr-FR/toto");
        assertEquals("https://developer.mozilla.org/en-US/docs", e.getHref());
    }

    /// Tests parse and canParse failure handling.
    @Test
    public void reportsParseFailures() {
        assertThrows(IllegalArgumentException.class, () -> WebURL.of("/en-US/docs"));
        assertThrows(IllegalArgumentException.class, () -> WebURL.of("/en-US/docs", ""));
        assertNull(WebURL.parse("/en-US/docs"));
        assertFalse(WebURL.canParse("/en-US/docs"));
        assertTrue(WebURL.canParse("/en-US/docs", "https://developer.mozilla.org"));
    }

    /// Tests URL getters and setters.
    @Test
    public void updatesComponentsWithSetters() {
        WebURL url = WebURL.of("https://user:pass@example.com:443/a/b?x=1#f");
        assertEquals("https://example.com", url.getOrigin());
        assertEquals("", url.getPort());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());

        url.setProtocol("http");
        url.setHost("example.org:8080");
        url.setUsername("a b");
        url.setPassword("p@ss");
        url.setPathname("/c d");
        url.setSearch("?q=a b&x=1");
        url.setHash("#frag ment");

        assertEquals("http://a%20b:p%40ss@example.org:8080/c%20d?q=a%20b&x=1#frag%20ment", url.getHref());
        assertEquals("?q=a%20b&x=1", url.getSearch());
        assertEquals("#frag%20ment", url.getHash());
        assertEquals("a b", url.getSearchParams().get("q"));
    }

    /// Tests live search parameter synchronization.
    @Test
    public void synchronizesSearchParamsWithUrl() {
        WebURL url = WebURL.of("https://example.test/?a=1&a=2");
        WebURLSearchParams params = url.getSearchParams();

        assertEquals(2, params.size());
        assertEquals("1", params.get("a"));
        assertEquals(2, params.getAll("a").size());

        params.append("b", "x y");
        assertEquals("https://example.test/?a=1&a=2&b=x+y", url.getHref());

        params.set("a", "3");
        assertEquals("https://example.test/?a=3&b=x+y", url.getHref());

        url.setSearch("");
        assertEquals("", url.getSearch());
        assertEquals(0, params.size());
    }

    /// Tests host parsing and serialization.
    @Test
    public void parsesHosts() {
        assertEquals("http://127.0.0.1/", WebURL.of("http://127.1").getHref());
        assertEquals("http://[2001:db8::1]/", WebURL.of("http://[2001:db8::1]/").getHref());
        assertEquals("https://xn--bcher-kva.example/", WebURL.of("https://bücher.example/").getHref());
    }

    /// Tests file URL origin behavior.
    @Test
    public void returnsNullOriginForFileUrls() {
        WebURL url = WebURL.of("file:///C:/demo");
        assertEquals("null", url.getOrigin());
    }

    /// Tests path normalization and percent-encoded dot segments.
    @Test
    public void normalizesPathSegments() {
        assertEquals("http://example.com/foo/baz",
                WebURL.of("http://example.com/foo/./bar/../baz").getHref());
        assertEquals("http://example.com/a/c",
                WebURL.of("http://example.com/a/%2e/b/%2e%2e/c").getHref());
    }

    /// Tests file URL Windows drive-letter normalization.
    @Test
    public void normalizesFileUrls() {
        assertEquals("file:///c:/demo", WebURL.of("file:c|/demo").getHref());
        assertEquals("file:///C:/demo", WebURL.of("file:///C|/demo").getHref());
        assertEquals("file:///C:/demo", WebURL.of("file://localhost/C:/demo").getHref());
    }

    /// Tests percent encoding in path, query, and fragment.
    @Test
    public void encodesUrlComponents() {
        assertEquals("data:text/plain,hi%20?x#%20y", WebURL.of("data:text/plain,hi ?x# y").getHref());
        assertEquals("http://example.com/%zz", WebURL.of("http://example.com/%zz").getHref());
        assertEquals("http://example.com/a%20b?x=1%202#h%20i",
                WebURL.of("http://example.com/a b?x=1 2#h i").getHref());
    }

    /// Tests port parsing and default-port elision.
    @Test
    public void handlesPorts() {
        assertEquals("http://example.com/", WebURL.of("http://example.com:80/").getHref());
        assertThrows(IllegalArgumentException.class, () -> WebURL.of("http://example.com:65536/"));
    }

    /// Tests setter no-op cases from the URL Standard.
    @Test
    public void ignoresSettersWhenUrlCannotAcceptComponent() {
        WebURL file = WebURL.of("file:///tmp/demo");
        file.setUsername("user");
        file.setPassword("pass");
        file.setPort("123");
        assertEquals("file:///tmp/demo", file.getHref());

        WebURL opaque = WebURL.of("data:text/plain,hello");
        opaque.setHost("example.com");
        opaque.setHostname("example.com");
        opaque.setPathname("/ignored");
        assertEquals("data:text/plain,hello", opaque.getHref());
    }

    /// Tests protocol setter constraints.
    @Test
    public void constrainsProtocolSetter() {
        WebURL special = WebURL.of("http://example.com:21/");
        special.setProtocol("ftp");
        assertEquals("ftp://example.com/", special.getHref());

        WebURL cannotBecomeNonSpecial = WebURL.of("http://example.com/");
        cannotBecomeNonSpecial.setProtocol("foo");
        assertEquals("http://example.com/", cannotBecomeNonSpecial.getHref());

        WebURL nonSpecial = WebURL.of("foo://example.com/path");
        nonSpecial.setProtocol("https");
        assertEquals("foo://example.com/path", nonSpecial.getHref());
    }

    /// Tests opaque base URL fragment-only parsing and blob origin serialization.
    @Test
    public void handlesOpaqueAndBlobUrls() {
        assertEquals("data:text/plain,hello#frag", WebURL.of("#frag", "data:text/plain,hello").getHref());
        assertEquals("https://example.com", WebURL.of("blob:https://example.com/id").getOrigin());
        assertEquals("null", WebURL.of("blob:ftp://example.com/id").getOrigin());
    }

    /// Tests non-special authority and credentials handling.
    @Test
    public void handlesAuthorityBoundaries() {
        assertEquals("foo://", WebURL.of("foo://").getHref());
        assertEquals("foo://example.com/path", WebURL.of("foo://example.com/path").getHref());
        assertEquals("foo://user:pass@example.com/path", WebURL.of("foo://user:pass@example.com/path").getHref());
        assertEquals("http://user%40@example.com/", WebURL.of("http://user@@example.com/").getHref());
    }

    /// Tests empty query and fragment preservation.
    @Test
    public void preservesEmptyQueryAndFragment() {
        assertEquals("http://example.com/?x#y", WebURL.of("http://example.com?x#y").getHref());
        assertEquals("http://example.com/#y", WebURL.of("http://example.com#y").getHref());
        assertEquals("http://example.com/?", WebURL.of("http://example.com/?").getHref());
        assertEquals("http://example.com/#", WebURL.of("http://example.com/#").getHref());
    }
}
