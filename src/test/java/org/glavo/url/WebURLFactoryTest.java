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
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
        assertEquals(IDNAProfile.defaultProfile(), factory.idnaProfile());
        assertSame(factory, factory.withIDNAProfile(IDNAProfile.defaultProfile()));
        assertEquals(WebURL.parseURL("https://example.com/a").href(), factory.parseURL("https://example.com/a").href());
        assertFalse(factory.canParseURL("../relative"));
    }

    /// Tests default IDNA profile inference.
    @Test
    public void infersDefaultIDNAProfile() {
        IDNAProfile defaultProfile = IDNAProfile.defaultProfile();

        assertTrue(defaultProfile.isAvailable());
        if (IDNAProfile.UTS_46.isAvailable()) {
            assertEquals(IDNAProfile.UTS_46, defaultProfile);
        } else {
            assertEquals(IDNAProfile.IDNA_2003, defaultProfile);
        }
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

    /// Tests IDNA 2003 profile selection with the dependency-free JDK implementation.
    @Test
    public void parsesWithIDNA2003Profile() {
        WebURLFactory factory = WebURLFactory.defaultFactory().withIDNAProfile(IDNAProfile.IDNA_2003);

        assertEquals(IDNAProfile.IDNA_2003, factory.idnaProfile());
        assertEquals("https://xn--bcher-kva.example/", factory.parseURL("https://bücher.example/").href());
        assertTrue(IDNAProfile.IDNA_2003.isAvailable());
    }

    /// Tests UTS #46 availability handling.
    @Test
    public void handlesUts46Availability() {
        if (IDNAProfile.UTS_46.isAvailable()) {
            WebURLFactory factory = WebURLFactory.defaultFactory().withIDNAProfile(IDNAProfile.UTS_46);

            assertEquals("https://xn--bcher-kva.example/", factory.parseURL("https://bücher.example/").href());
        } else {
            assertThrows(IllegalStateException.class,
                    () -> WebURLFactory.defaultFactory().withIDNAProfile(IDNAProfile.UTS_46));
        }
    }

    /// Tests immutable factory derivation.
    @Test
    public void derivesFactoryConfiguration() {
        WebURLFactory factory = WebURLFactory.defaultFactory().withIDNAProfile(IDNAProfile.IDNA_2003);
        WebURLFactory copied = factory.withIDNAProfile(IDNAProfile.IDNA_2003);

        assertSame(factory, copied);
        assertEquals(IDNAProfile.IDNA_2003, copied.idnaProfile());
        if (IDNAProfile.UTS_46.isAvailable()) {
            WebURLFactory changed = factory.withIDNAProfile(IDNAProfile.UTS_46);

            assertNotSame(factory, changed);
            assertEquals(IDNAProfile.UTS_46, changed.idnaProfile());
        }
        assertFalse(copied.canParseURL("../c"));
        assertEquals("https://example.net/d",
                copied.parseURL("../d", WebURL.parseURL("https://example.net/base/")).href());
    }
}
