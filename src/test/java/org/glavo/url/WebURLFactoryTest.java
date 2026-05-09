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
        assertNull(factory.base());
        assertEquals(WebURLFactory.IdnaProvider.AUTOMATIC, factory.idnaProvider());
        assertEquals(WebURL.of("https://example.com/a").href(), factory.of("https://example.com/a").href());
        assertFalse(factory.canParse("../relative"));
    }

    /// Tests a factory with a configured base URL.
    @Test
    public void parsesAgainstConfiguredBase() {
        WebURLFactory factory = WebURLFactory.builder()
                .base("https://example.com/a/b/")
                .build();

        assertEquals("https://example.com/a/c", factory.of("../c").href());
        assertEquals("https://example.com/a/b/d", factory.parse("d").href());
        assertTrue(factory.canParse("?q=1"));
        assertEquals("https://example.com/a/b/", factory.base().href());
    }

    /// Tests that explicit base arguments override the configured base for one call.
    @Test
    public void explicitBaseOverridesConfiguredBase() {
        WebURLFactory factory = WebURLFactory.builder()
                .base("https://example.com/a/b/")
                .build();
        WebURL base = WebURL.of("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", factory.of("../z", base).href());
        assertEquals("https://example.net/z", factory.of("/z", "https://example.net/base").href());
    }

    /// Tests parse and canParse failure handling through a factory.
    @Test
    public void reportsFailuresThroughFactory() {
        WebURLFactory factory = WebURLFactory.standard();

        assertNull(factory.parse("https://example.com:999999/"));
        assertFalse(factory.canParse("https://example.com:999999/"));
        assertNull(factory.parse("/relative", "not a url"));
        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> factory.of("https://example.com:999999/"));
    }

    /// Tests provider selection with the dependency-free JDK provider.
    @Test
    public void parsesWithJdkIdnaProvider() {
        WebURLFactory factory = WebURLFactory.builder()
                .idnaProvider(WebURLFactory.IdnaProvider.JDK)
                .build();

        assertEquals(WebURLFactory.IdnaProvider.JDK, factory.idnaProvider());
        assertEquals("https://xn--bcher-kva.example/", factory.of("https://bücher.example/").href());
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
            assertEquals("https://xn--bcher-kva.example/", factory.of("https://bücher.example/").href());
        } else {
            assertThrows(IllegalStateException.class, () -> WebURLFactory.builder()
                    .idnaProvider(WebURLFactory.IdnaProvider.ICU4J)
                    .build());
        }
    }

    /// Tests factory copy and base clearing helpers.
    @Test
    public void copiesAndClearsFactoryConfiguration() {
        WebURLFactory factory = WebURLFactory.builder()
                .base("https://example.com/a/")
                .idnaProvider(WebURLFactory.IdnaProvider.JDK)
                .build();

        WebURLFactory copied = factory.toBuilder().base("https://example.org/b/").build();
        WebURLFactory withoutBase = copied.withoutBase();

        assertEquals("https://example.org/c", copied.of("../c").href());
        assertEquals(WebURLFactory.IdnaProvider.JDK, copied.idnaProvider());
        assertNull(withoutBase.base());
        assertFalse(withoutBase.canParse("../c"));
        assertEquals("https://example.net/d", withoutBase.withBase(WebURL.of("https://example.net/base/")).of("../d").href());
    }
}
