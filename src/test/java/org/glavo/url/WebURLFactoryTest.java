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
    /// Tests the standard factory exposed by `WebURLFactory.standard()`.
    @Test
    public void standardFactoryMatchesWebUrlStaticMethods() {
        WebURLFactory factory = WebURLFactory.standard();

        assertSame(factory, WebURLFactory.standard());
        assertEquals(WebURLFactory.IdnaProvider.AUTOMATIC, factory.idnaProvider());
        assertEquals(WebURL.of("https://example.com/a").href(), factory.parse("https://example.com/a").href());
        assertFalse(factory.canParse("../relative"));
    }

    /// Tests explicit base URL arguments.
    @Test
    public void parsesAgainstExplicitBase() {
        WebURLFactory factory = WebURLFactory.standard();

        assertEquals("https://example.com/a/c", factory.parse("../c", "https://example.com/a/b/").href());
        assertEquals("https://example.com/a/b/d", factory.tryParse("d", "https://example.com/a/b/").href());
        assertTrue(factory.canParse("?q=1", "https://example.com/a/b/"));
        assertFalse(factory.canParse("?q=1"));
    }

    /// Tests explicit base arguments with an already parsed base URL.
    @Test
    public void parsesAgainstExplicitWebUrlBase() {
        WebURLFactory factory = WebURLFactory.standard();
        WebURL base = WebURL.of("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", factory.parse("../z", base).href());
        assertEquals("https://example.net/z", factory.parse("/z", "https://example.net/base").href());
    }

    /// Tests parse and canParse failure handling through a factory.
    @Test
    public void reportsFailuresThroughFactory() {
        WebURLFactory factory = WebURLFactory.standard();

        assertNull(factory.tryParse("https://example.com:999999/"));
        assertFalse(factory.canParse("https://example.com:999999/"));
        assertNull(factory.tryParse("/relative", "not a url"));
        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> factory.parse("https://example.com:999999/"));
    }

    /// Tests provider selection with the dependency-free JDK provider.
    @Test
    public void parsesWithJdkIdnaProvider() {
        WebURLFactory factory = WebURLFactory.builder()
                .idnaProvider(WebURLFactory.IdnaProvider.JDK)
                .build();

        assertEquals(WebURLFactory.IdnaProvider.JDK, factory.idnaProvider());
        assertEquals("https://xn--bcher-kva.example/", factory.parse("https://bücher.example/").href());
        assertTrue(WebURLFactory.IdnaProvider.JDK.isAvailable());
        assertTrue(WebURLFactory.IdnaProvider.AUTOMATIC.isAvailable());
    }

    /// Tests ICU4J provider availability handling.
    @Test
    public void handlesIcu4jIdnaProviderAvailability() {
        if (WebURLFactory.IdnaProvider.ICU4J.isAvailable()) {
            WebURLFactory factory = WebURLFactory.builder()
                    .idnaProvider(WebURLFactory.IdnaProvider.ICU4J)
                    .build();
            assertEquals("https://xn--bcher-kva.example/", factory.parse("https://bücher.example/").href());
        } else {
            assertThrows(IllegalStateException.class, () -> WebURLFactory.builder()
                    .idnaProvider(WebURLFactory.IdnaProvider.ICU4J)
                    .build());
        }
    }

    /// Tests factory copy helpers.
    @Test
    public void copiesFactoryConfiguration() {
        WebURLFactory factory = WebURLFactory.builder()
                .idnaProvider(WebURLFactory.IdnaProvider.JDK)
                .build();

        WebURLFactory copied = factory.toBuilder().build();

        assertEquals(WebURLFactory.IdnaProvider.JDK, copied.idnaProvider());
        assertFalse(copied.canParse("../c"));
        assertEquals("https://example.net/d",
                copied.parse("../d", WebURL.of("https://example.net/base/")).href());
    }
}
