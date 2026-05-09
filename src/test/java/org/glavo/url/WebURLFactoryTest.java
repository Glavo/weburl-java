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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `WebURLFactory`.
@NotNullByDefault
public final class WebURLFactoryTest {
    /// Tests the default factory exposed by `WebURLFactory.defaultFactory()`.
    @Test
    public void defaultFactoryMatchesWebUrlStaticMethods() {
        WebURLFactory factory = WebURLFactory.defaultFactory();

        assertSame(factory, WebURLFactory.defaultFactory());
        assertEquals(WebURL.parseURL("https://example.com/a").href(), factory.parseURL("https://example.com/a").href());
        assertEquals(WebURL.parseAddress("example.com").href(), factory.parseAddress("example.com").href());
        assertFalse(factory.canParseURL("../relative"));
    }

    /// Tests explicit base URL arguments.
    @Test
    public void parsesAgainstExplicitBase() {
        WebURLFactory factory = WebURLFactory.defaultFactory();

        assertEquals("https://example.com/a/c", factory.parseURL("../c", "https://example.com/a/b/").href());
        assertEquals("https://example.com/a/b/d", factory.tryParseURL("d", "https://example.com/a/b/").href());
        assertTrue(factory.canParseURL("?q=1", "https://example.com/a/b/"));
        assertFalse(factory.canParseURL("?q=1"));
    }

    /// Tests explicit base arguments with an already parsed base URL.
    @Test
    public void parsesAgainstExplicitWebUrlBase() {
        WebURLFactory factory = WebURLFactory.defaultFactory();
        WebURL base = WebURL.parseURL("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", factory.parseURL("../z", base).href());
        assertEquals("https://example.net/z", factory.parseURL("/z", "https://example.net/base").href());
    }

    /// Tests browser address parsing.
    @Test
    public void parsesBrowserAddresses() {
        WebURLFactory factory = WebURLFactory.defaultFactory();

        assertEquals("https://www.glavo.site/", factory.parseAddress("www.glavo.site").href());
        assertEquals("https://www.glavo.site/path?q=1#f", factory.parseAddress("www.glavo.site/path?q=1#f").href());
        assertEquals("https://www.glavo.site/path", factory.parseAddress("//www.glavo.site/path").href());
        assertEquals("https://localhost:8080/path", factory.parseAddress("localhost:8080/path").href());
        assertEquals("https://127.0.0.1:3000/", factory.parseAddress("127.0.0.1:3000").href());
        assertEquals("https://[::1]:8080/", factory.parseAddress("[::1]:8080").href());
        assertEquals("https://xn--r8jz45g.xn--zckzah/",
                factory.parseAddress("例え.テスト").href());
        assertEquals("data:text/plain,hi", factory.parseAddress("data:text/plain,hi").href());

        assertFalse(factory.canParseURL("www.glavo.site"));
        assertTrue(factory.canParseAddress("www.glavo.site"));
        assertNull(factory.tryParseAddress("not a url"));
        assertThrows(WebURLParseException.class, () -> factory.parseAddress("not a url"));
        assertThrows(WebURLParseException.PortInvalid.class,
                () -> factory.parseAddress("www.glavo.site:abc"));
    }

    /// Tests parseURL and canParseURL failure handling through a factory.
    @Test
    public void reportsFailuresThroughFactory() {
        WebURLFactory factory = WebURLFactory.defaultFactory();

        assertNull(factory.tryParseURL("https://example.com:999999/"));
        assertFalse(factory.canParseURL("https://example.com:999999/"));
        assertNull(factory.tryParseURL("/relative", "not a url"));
        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> factory.parseURL("https://example.com:999999/"));
    }

    /// Tests UTS #46 domain processing.
    @Test
    public void parsesUnicodeDomainsWithUts46() {
        WebURLFactory factory = WebURLFactory.defaultFactory();

        assertEquals("https://xn--bcher-kva.example/", factory.parseURL("https://bücher.example/").href());
        assertEquals("https://xn--fa-hia.example/", factory.parseURL("https://faß.example/").href());
        assertTrue(factory.canParseURL("https://xn--bcher-kva.example/"));
    }

    /// Tests immutable default factory reuse.
    @Test
    public void reusesDefaultFactory() {
        WebURLFactory factory = WebURLFactory.defaultFactory();
        WebURLFactory copied = WebURLFactory.defaultFactory();

        assertSame(factory, copied);
        assertFalse(copied.canParseURL("../c"));
        assertEquals("https://example.net/d",
                copied.parseURL("../d", WebURL.parseURL("https://example.net/base/")).href());
    }
}
