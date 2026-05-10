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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests for `WebURL` parsing entry points.
@NotNullByDefault
public final class WebURLParsingTest {
    /// Tests explicit base URL string arguments.
    @Test
    public void parsesAgainstExplicitBaseString() {
        assertEquals("https://example.com/a/c", WebURL.parse("../c", "https://example.com/a/b/").href());
        assertEquals("https://example.com/a/b/d", WebURL.tryParse("d", "https://example.com/a/b/").href());
        assertNotNull(WebURL.tryParse("?q=1", "https://example.com/a/b/"));
        assertNull(WebURL.tryParse("?q=1"));
    }

    /// Tests explicit base arguments with an already parsed base URL.
    @Test
    public void parsesAgainstExplicitWebUrlBase() {
        WebURL base = WebURL.parse("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", WebURL.parse("../z", base).href());
        assertEquals("https://example.net/z", WebURL.parse("/z", "https://example.net/base").href());
    }

    /// Tests browser-style URL input parsing.
    @Test
    public void parsesBrowserAddresses() {
        assertEquals("https://www.glavo.site/", WebURL.parseBrowserInput("www.glavo.site").href());
        assertEquals("https://www.glavo.site/path?q=1#f", WebURL.parseBrowserInput("www.glavo.site/path?q=1#f").href());
        assertEquals("https://www.glavo.site/path", WebURL.parseBrowserInput("//www.glavo.site/path").href());
        assertEquals("http://localhost:8080/path", WebURL.parseBrowserInput("localhost:8080/path").href());
        assertEquals("http://127.0.0.1:3000/", WebURL.parseBrowserInput("127.0.0.1:3000").href());
        assertEquals("http://[::1]:8080/", WebURL.parseBrowserInput("[::1]:8080").href());
        assertEquals("http://printer/", WebURL.parseBrowserInput("printer").href());
        assertEquals("http://printer/path", WebURL.parseBrowserInput("printer/path").href());
        assertEquals("http://test/path", WebURL.parseBrowserInput("test/path").href());
        assertEquals("http://app.localhost/", WebURL.parseBrowserInput("app.localhost").href());
        assertEquals("http://[::1]/", WebURL.parseBrowserInput("//[::1]/").href());
        assertEquals("https://xn--r8jz45g.xn--zckzah/",
                WebURL.parseBrowserInput("例え.テスト").href());
        assertEquals("data:text/plain,hi", WebURL.parseBrowserInput("data:text/plain,hi").href());

        assertNull(WebURL.tryParse("www.glavo.site"));
        assertNotNull(WebURL.tryParseBrowserInput("www.glavo.site"));
        assertNull(WebURL.tryParseBrowserInput("not a url"));
        assertThrows(WebURLParseException.class, () -> WebURL.parseBrowserInput("not a url"));
        assertThrows(WebURLParseException.PortInvalid.class,
                () -> WebURL.parseBrowserInput("www.glavo.site:abc"));
    }

    /// Tests parse and tryParse failure handling.
    @Test
    public void reportsParsingFailures() {
        assertNull(WebURL.tryParse("https://example.com:999999/"));
        assertNull(WebURL.tryParse("/relative", "not a url"));
        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> WebURL.parse("https://example.com:999999/"));
    }

    /// Tests UTS #46 domain processing.
    @Test
    public void parsesUnicodeDomainsWithUts46() {
        assertEquals("https://xn--bcher-kva.example/", WebURL.parse("https://bücher.example/").href());
        assertEquals("https://xn--fa-hia.example/", WebURL.parse("https://faß.example/").href());
        assertNotNull(WebURL.tryParse("https://xn--bcher-kva.example/"));
    }
}
