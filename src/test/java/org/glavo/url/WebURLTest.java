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
        WebURL a = new WebURL("/", "https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", a.getHref());

        WebURL b = new WebURL("https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", b.getHref());

        WebURL c = new WebURL("en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", c.getHref());

        WebURL d = new WebURL("/en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", d.getHref());

        WebURL e = new WebURL("/en-US/docs", "https://developer.mozilla.org/fr-FR/toto");
        assertEquals("https://developer.mozilla.org/en-US/docs", e.getHref());
    }

    /// Tests parse and canParse failure handling.
    @Test
    public void reportsParseFailures() {
        assertThrows(IllegalArgumentException.class, () -> new WebURL("/en-US/docs"));
        assertThrows(IllegalArgumentException.class, () -> new WebURL("/en-US/docs", ""));
        assertNull(WebURL.parse("/en-US/docs"));
        assertFalse(WebURL.canParse("/en-US/docs"));
        assertTrue(WebURL.canParse("/en-US/docs", "https://developer.mozilla.org"));
    }

    /// Tests URL getters and setters.
    @Test
    public void updatesComponentsWithSetters() {
        WebURL url = new WebURL("https://user:pass@example.com:443/a/b?x=1#f");
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
        WebURL url = new WebURL("https://example.test/?a=1&a=2");
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
        assertEquals("http://127.0.0.1/", new WebURL("http://127.1").getHref());
        assertEquals("http://[2001:db8::1]/", new WebURL("http://[2001:db8::1]/").getHref());
        assertEquals("https://xn--bcher-kva.example/", new WebURL("https://bücher.example/").getHref());
    }

    /// Tests file URL origin behavior.
    @Test
    public void returnsNullOriginForFileUrls() {
        WebURL url = new WebURL("file:///C:/demo");
        assertEquals("null", url.getOrigin());
    }
}
