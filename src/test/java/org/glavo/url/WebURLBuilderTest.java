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

/// Tests for `WebURL.Builder`.
@NotNullByDefault
public final class WebURLBuilderTest {
    /// Tests constructing a hierarchical URL from individual components.
    @Test
    public void buildsUrlFromComponents() {
        WebURL url = WebURL.newBuilder()
                .setUsername("user name")
                .setPassword("pa:ss")
                .setHost("example.com")
                .setPort(8080)
                .setPath("/a b")
                .setQuery("q=a&b")
                .setFragment("frag ment")
                .setScheme("HTTPS")
                .build();

        assertEquals("https://user%20name:pa%3Ass@example.com:8080/a%20b?q=a&b#frag%20ment", url.href());
        assertEquals("https", url.getScheme());
        assertEquals("user name", url.getUsername());
        assertEquals("pa:ss", url.getPassword());
        assertEquals("example.com", url.getHost());
        assertEquals(8080, url.getPort());
    }

    /// Tests that a builder copied from an existing URL preserves and then independently mutates components.
    @Test
    public void copiesExistingUrl() {
        WebURL original = WebURL.parse("https://user:pass@example.com/a?x=1#f");
        WebURL.Builder builder = WebURL.newBuilder(original);
        WebURL opaque = WebURL.parse("data:text/plain,hi");
        WebURL recoverable = WebURL.parse("https://example.com/%zz");
        WebURL emptyPassword = WebURL.parse("https://user:@example.com/");

        assertEquals(original, builder.build());
        assertEquals(opaque, WebURL.newBuilder(opaque).build());
        assertEquals("https://example.com/%zz#frag",
                WebURL.newBuilder(recoverable).setFragment("frag").build().href());
        assertEquals(emptyPassword, WebURL.newBuilder(emptyPassword).build());

        WebURL modified = builder.setPath("/b").setRawQuery(null).build();
        assertEquals("https://user:pass@example.com/b#f", modified.href());
        assertEquals("https://user:pass@example.com/a?x=1#f", original.href());
    }

    /// Tests decoded setters encode literal characters while raw setters preserve valid escapes.
    @Test
    public void distinguishesDecodedAndRawComponents() {
        WebURL decoded = WebURL.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setPath("/a b/%20")
                .setQuery("x=a#b")
                .setFragment("frag ment")
                .build();
        assertEquals("https://example.com/a%20b/%2520?x=a%23b#frag%20ment", decoded.href());

        WebURL raw = WebURL.newBuilder()
                .setScheme("https")
                .setRawHost("example.com")
                .setRawPath("/a%20b")
                .setRawQuery("x=a%23b")
                .setRawFragment("frag%20ment")
                .build();
        assertEquals("https://example.com/a%20b?x=a%23b#frag%20ment", raw.href());
    }

    /// Tests clearing optional components by passing `null`.
    @Test
    public void clearsOptionalComponentsWithNull() {
        WebURL url = WebURL.newBuilder(WebURL.parse("https://user:pass@example.com:8443/a?x=1#f"))
                .setUsername(null)
                .setPassword(null)
                .setRawPort(null)
                .setQuery(null)
                .setFragment(null)
                .build();

        assertEquals("https://example.com/a", url.href());
        assertNull(url.getRawUsername());
        assertNull(url.getRawPassword());
        assertNull(url.getRawPort());
        assertNull(url.getRawQuery());
        assertNull(url.getRawFragment());

        WebURL withoutHost = WebURL.newBuilder(WebURL.parse("foo://example.com/a"))
                .setHost(null)
                .build();
        assertEquals("foo:/a", withoutHost.href());
        assertNull(withoutHost.getHost());
    }

    /// Tests default port normalization.
    @Test
    public void normalizesDefaultPort() {
        WebURL url = WebURL.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setPort(443)
                .build();

        assertEquals("https://example.com/", url.href());
        assertNull(url.getRawPort());
        assertEquals(443, url.getPort());

        WebURL explicit = WebURL.newBuilder(url).setRawPort("8443").build();
        assertEquals("https://example.com:8443/", explicit.href());
        assertEquals("8443", explicit.getRawPort());
    }

    /// Tests host and path forms beyond ordinary domain URLs.
    @Test
    public void buildsSpecialHostAndPathForms() {
        WebURL idn = WebURL.newBuilder()
                .setScheme("https")
                .setHost("你好.世界")
                .build();
        assertEquals("https://xn--6qq79v.xn--rhqv96g/", idn.href());

        WebURL ipv6 = WebURL.newBuilder()
                .setScheme("http")
                .setRawHost("[::1]")
                .build();
        assertEquals("http://[::1]/", ipv6.href());

        WebURL file = WebURL.newBuilder()
                .setScheme("file")
                .setPath("/tmp/a b")
                .build();
        assertEquals("file:///tmp/a%20b", file.href());

        WebURL opaque = WebURL.newBuilder()
                .setScheme("mailto")
                .setPath("user@example.com")
                .setQuery("subject=Hi There")
                .build();
        assertEquals("mailto:user@example.com?subject=Hi%20There", opaque.href());
        assertNull(opaque.getHost());
    }

    /// Tests that changing the scheme revalidates and re-encodes scheme-sensitive components.
    @Test
    public void revalidatesComponentsAfterSchemeChange() {
        WebURL.Builder builder = WebURL.newBuilder()
                .setHost("example.com")
                .setRawPath("/a")
                .setQuery("q='")
                .setScheme("foo");

        assertEquals("foo://example.com/a?q='", builder.build().href());
        assertEquals("https://example.com/a?q=%27", builder.setScheme("https").build().href());
    }

    /// Tests that setters can be called before a scheme is available.
    @Test
    public void acceptsComponentsBeforeScheme() {
        WebURL url = WebURL.newBuilder()
                .setHost("example.com")
                .setPath("/a b")
                .setQuery("q='")
                .setFragment("top")
                .setPort(443)
                .setScheme("https")
                .build();

        assertEquals("https://example.com/a%20b?q=%27#top", url.href());
        assertNull(url.getRawPort());

        WebURL opaque = WebURL.newBuilder()
                .setPath("user@example.com")
                .setQuery("subject=Hi There")
                .setScheme("mailto")
                .build();
        assertEquals("mailto:user@example.com?subject=Hi%20There", opaque.href());
    }

    /// Tests that invalid component inputs are validated when the URL is built.
    @Test
    public void validatesComponentsWhenBuilt() {
        WebURL.Builder invalidHost = WebURL.newBuilder().setHost("exa mple.com").setScheme("https");
        assertThrows(IllegalArgumentException.class, invalidHost::build);

        WebURL.Builder invalidRawPath = WebURL.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setRawPath("/a b");
        assertThrows(IllegalArgumentException.class, invalidRawPath::build);

        WebURL.Builder invalidRawQuery = WebURL.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setRawQuery("x=#");
        assertThrows(IllegalArgumentException.class, invalidRawQuery::build);

        WebURL.Builder invalidPercentEscape = WebURL.newBuilder()
                .setScheme("https")
                .setHost("example.com")
                .setRawPath("/%zz");
        assertThrows(IllegalArgumentException.class, invalidPercentEscape::build);
    }

    /// Tests that invalid intermediate values may be replaced before build-time validation.
    @Test
    public void allowsInvalidIntermediateComponentsToBeCorrected() {
        WebURL url = WebURL.newBuilder()
                .setHost("exa mple.com")
                .setRawPath("/a b")
                .setRawQuery("x=#")
                .setScheme("https")
                .setHost("example.com")
                .setRawPort("443")
                .setRawPath("/a%20b")
                .setRawQuery("x=%23")
                .build();

        assertEquals("https://example.com/a%20b?x=%23", url.href());
    }

    /// Tests builder error reporting.
    @Test
    public void reportsBuilderErrors() {
        assertThrows(IllegalStateException.class, () -> WebURL.newBuilder().setHost("example.com").build());
        assertThrows(IllegalStateException.class, () -> WebURL.newBuilder().setScheme("https").build());
        assertThrows(IllegalStateException.class, () -> WebURL.newBuilder().setScheme("foo").setPort(1).build());

        assertThrows(IllegalArgumentException.class, () -> WebURL.newBuilder().setScheme("1https"));
        assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setRawPort("7z"));
        assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setHost("exa mple.com").build());
        assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setHost("example.com").setRawPath("/a b").build());
        assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setHost("example.com").setRawQuery("x=#").build());
        assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setHost("example.com").setPort(70000));
        assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setHost("example.com").setPort(-2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> WebURL.newBuilder().setScheme("https").setHost("example.com").setRawPath("/%zz").build());
        assertFalse(exception.getMessage().isEmpty());
    }
}
