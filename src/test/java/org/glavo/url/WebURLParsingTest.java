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

/// Tests for `WebURL` parsing entry points.
@NotNullByDefault
public final class WebURLParsingTest {
    /// Tests explicit base URL string arguments.
    @Test
    public void parsesAgainstExplicitBaseString() {
        assertEquals("https://example.com/a/c", WebURL.parseURL("../c", "https://example.com/a/b/").href());
        assertEquals("https://example.com/a/b/d", WebURL.tryParseURL("d", "https://example.com/a/b/").href());
        assertTrue(WebURL.canParseURL("?q=1", "https://example.com/a/b/"));
        assertFalse(WebURL.canParseURL("?q=1"));
    }

    /// Tests explicit base arguments with an already parsed base URL.
    @Test
    public void parsesAgainstExplicitWebUrlBase() {
        WebURL base = WebURL.parseURL("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", WebURL.parseURL("../z", base).href());
        assertEquals("https://example.net/z", WebURL.parseURL("/z", "https://example.net/base").href());
    }

    /// Tests browser address parsing.
    @Test
    public void parsesBrowserAddresses() {
        assertEquals("https://www.glavo.site/", WebURL.parseAddress("www.glavo.site").href());
        assertEquals("https://www.glavo.site/path?q=1#f", WebURL.parseAddress("www.glavo.site/path?q=1#f").href());
        assertEquals("https://www.glavo.site/path", WebURL.parseAddress("//www.glavo.site/path").href());
        assertEquals("https://localhost:8080/path", WebURL.parseAddress("localhost:8080/path").href());
        assertEquals("https://127.0.0.1:3000/", WebURL.parseAddress("127.0.0.1:3000").href());
        assertEquals("https://[::1]:8080/", WebURL.parseAddress("[::1]:8080").href());
        assertEquals("https://xn--r8jz45g.xn--zckzah/",
                WebURL.parseAddress("例え.テスト").href());
        assertEquals("data:text/plain,hi", WebURL.parseAddress("data:text/plain,hi").href());

        assertFalse(WebURL.canParseURL("www.glavo.site"));
        assertTrue(WebURL.canParseAddress("www.glavo.site"));
        assertNull(WebURL.tryParseAddress("not a url"));
        assertThrows(WebURLParseException.class, () -> WebURL.parseAddress("not a url"));
        assertThrows(WebURLParseException.PortInvalid.class,
                () -> WebURL.parseAddress("www.glavo.site:abc"));
    }

    /// Tests parseURL and canParseURL failure handling.
    @Test
    public void reportsParsingFailures() {
        assertNull(WebURL.tryParseURL("https://example.com:999999/"));
        assertFalse(WebURL.canParseURL("https://example.com:999999/"));
        assertNull(WebURL.tryParseURL("/relative", "not a url"));
        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> WebURL.parseURL("https://example.com:999999/"));
    }

    /// Tests UTS #46 domain processing.
    @Test
    public void parsesUnicodeDomainsWithUts46() {
        assertEquals("https://xn--bcher-kva.example/", WebURL.parseURL("https://bücher.example/").href());
        assertEquals("https://xn--fa-hia.example/", WebURL.parseURL("https://faß.example/").href());
        assertTrue(WebURL.canParseURL("https://xn--bcher-kva.example/"));
    }
}
