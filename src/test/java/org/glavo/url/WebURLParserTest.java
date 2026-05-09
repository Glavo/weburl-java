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

/// Tests for `WebURLParser`.
@NotNullByDefault
public final class WebURLParserTest {
    /// Tests the standard parser exposed by `WebURLParser.standard()`.
    @Test
    public void standardParserMatchesWebUrlStaticMethods() {
        WebURLParser parser = WebURLParser.standard();

        assertSame(parser, WebURLParser.standard());
        assertNull(parser.base());
        assertEquals(WebURLParser.IdnaProvider.AUTOMATIC, parser.idnaProvider());
        assertEquals(WebURL.of("https://example.com/a").href(), parser.of("https://example.com/a").href());
        assertFalse(parser.canParse("../relative"));
    }

    /// Tests a parser with a configured base URL.
    @Test
    public void parsesAgainstConfiguredBase() {
        WebURLParser parser = WebURLParser.builder()
                .base("https://example.com/a/b/")
                .build();

        assertEquals("https://example.com/a/c", parser.of("../c").href());
        assertEquals("https://example.com/a/b/d", parser.parse("d").href());
        assertTrue(parser.canParse("?q=1"));
        assertEquals("https://example.com/a/b/", parser.base().href());
    }

    /// Tests that explicit base arguments override the configured base for one call.
    @Test
    public void explicitBaseOverridesConfiguredBase() {
        WebURLParser parser = WebURLParser.builder()
                .base("https://example.com/a/b/")
                .build();
        WebURL base = WebURL.of("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", parser.of("../z", base).href());
        assertEquals("https://example.net/z", parser.of("/z", "https://example.net/base").href());
    }

    /// Tests parse and canParse failure handling through a parser.
    @Test
    public void reportsFailuresThroughParser() {
        WebURLParser parser = WebURLParser.standard();

        assertNull(parser.parse("https://example.com:999999/"));
        assertFalse(parser.canParse("https://example.com:999999/"));
        assertNull(parser.parse("/relative", "not a url"));
        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> parser.of("https://example.com:999999/"));
    }

    /// Tests provider selection with the dependency-free JDK provider.
    @Test
    public void parsesWithJdkIdnaProvider() {
        WebURLParser parser = WebURLParser.builder()
                .idnaProvider(WebURLParser.IdnaProvider.JDK)
                .build();

        assertEquals(WebURLParser.IdnaProvider.JDK, parser.idnaProvider());
        assertEquals("https://xn--bcher-kva.example/", parser.of("https://bücher.example/").href());
        assertTrue(WebURLParser.IdnaProvider.JDK.isAvailable());
        assertTrue(WebURLParser.IdnaProvider.AUTOMATIC.isAvailable());
    }

    /// Tests ICU4J provider availability handling.
    @Test
    public void handlesIcu4jIdnaProviderAvailability() {
        if (WebURLParser.IdnaProvider.ICU4J.isAvailable()) {
            WebURLParser parser = WebURLParser.builder()
                    .idnaProvider(WebURLParser.IdnaProvider.ICU4J)
                    .build();
            assertEquals("https://xn--bcher-kva.example/", parser.of("https://bücher.example/").href());
        } else {
            assertThrows(IllegalStateException.class, () -> WebURLParser.builder()
                    .idnaProvider(WebURLParser.IdnaProvider.ICU4J)
                    .build());
        }
    }

    /// Tests parser copy and base clearing helpers.
    @Test
    public void copiesAndClearsParserConfiguration() {
        WebURLParser parser = WebURLParser.builder()
                .base("https://example.com/a/")
                .idnaProvider(WebURLParser.IdnaProvider.JDK)
                .build();

        WebURLParser copied = parser.toBuilder().base("https://example.org/b/").build();
        WebURLParser withoutBase = copied.withoutBase();

        assertEquals("https://example.org/c", copied.of("../c").href());
        assertEquals(WebURLParser.IdnaProvider.JDK, copied.idnaProvider());
        assertNull(withoutBase.base());
        assertFalse(withoutBase.canParse("../c"));
        assertEquals("https://example.net/d", withoutBase.withBase(WebURL.of("https://example.net/base/")).of("../d").href());
    }
}
