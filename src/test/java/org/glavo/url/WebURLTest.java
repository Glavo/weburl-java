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

import java.net.MalformedURLException;
import java.net.URI;

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
        assertEquals("https://developer.mozilla.org/", a.href());

        WebURL b = WebURL.of("https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", b.href());

        WebURL c = WebURL.of("en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", c.href());

        WebURL d = WebURL.of("/en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", d.href());

        WebURL e = WebURL.of("/en-US/docs", "https://developer.mozilla.org/fr-FR/toto");
        assertEquals("https://developer.mozilla.org/en-US/docs", e.href());
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

    /// Tests typed parse exceptions for selected URL Standard validation errors.
    @Test
    public void reportsTypedParseExceptions() {
        WebURLParseException missingScheme =
                assertThrows(WebURLParseException.MissingSchemeNonRelativeURL.class,
                        () -> WebURL.of("/en-US/docs"));
        assertEquals("missing-scheme-non-relative-URL", missingScheme.errorName());

        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> WebURL.of("http://example.com:65536/"));
        assertThrows(WebURLParseException.PortInvalid.class,
                () -> WebURL.of("http://example.com:7z/"));
        assertThrows(WebURLParseException.HostMissing.class,
                () -> WebURL.of("https://:443"));
        assertThrows(WebURLParseException.IPv6Unclosed.class,
                () -> WebURL.of("https://[::1"));
        assertThrows(WebURLParseException.IPv4TooManyParts.class,
                () -> WebURL.of("https://1.2.3.4.5/"));
    }

    /// Tests URL getters and setters.
    @Test
    public void updatesComponentsWithSetters() {
        WebURL url = WebURL.of("https://user:pass@example.com:443/a/b?x=1#f");
        assertEquals("https://example.com", url.origin());
        assertEquals("", url.port());
        assertEquals("user", url.username());
        assertEquals("pass", url.password());

        WebURL updated = url
                .withProtocol("http")
                .withHost("example.org:8080")
                .withUsername("a b")
                .withPassword("p@ss")
                .withPathname("/c d")
                .withSearch("?q=a b&x=1")
                .withHash("#frag ment");

        assertEquals("https://user:pass@example.com/a/b?x=1#f", url.href());
        assertEquals("http://a%20b:p%40ss@example.org:8080/c%20d?q=a%20b&x=1#frag%20ment", updated.href());
        assertEquals("?q=a%20b&x=1", updated.search());
        assertEquals("#frag%20ment", updated.hash());
        assertEquals("a b", updated.searchParams().get("q"));
    }

    /// Tests immutable search parameter updates.
    @Test
    public void updatesSearchParamsImmutably() {
        WebURL url = WebURL.of("https://example.test/?a=1&a=2");
        WebURLSearchParams params = url.searchParams();

        assertEquals(2, params.size());
        assertEquals("1", params.get("a"));
        assertEquals(2, params.getAll("a").size());

        WebURLSearchParams appended = params.append("b", "x y");
        assertEquals("https://example.test/?a=1&a=2", url.href());
        assertEquals("a=1&a=2", params.toString());

        WebURL updated = url.withSearchParams(appended.set("a", "3"));
        assertEquals("https://example.test/?a=3&b=x+y", updated.href());

        WebURL withoutSearch = updated.withSearch("");
        assertEquals("", withoutSearch.search());
        assertEquals(2, params.size());
    }

    /// Tests host parsing and serialization.
    @Test
    public void parsesHosts() {
        assertEquals("http://127.0.0.1/", WebURL.of("http://127.1").href());
        assertEquals("http://[2001:db8::1]/", WebURL.of("http://[2001:db8::1]/").href());
        assertEquals("https://xn--bcher-kva.example/", WebURL.of("https://bücher.example/").href());
    }

    /// Tests file URL origin behavior.
    @Test
    public void returnsNullOriginForFileUrls() {
        WebURL url = WebURL.of("file:///C:/demo");
        assertEquals("null", url.origin());
    }

    /// Tests path normalization and percent-encoded dot segments.
    @Test
    public void normalizesPathSegments() {
        assertEquals("http://example.com/foo/baz",
                WebURL.of("http://example.com/foo/./bar/../baz").href());
        assertEquals("http://example.com/a/c",
                WebURL.of("http://example.com/a/%2e/b/%2e%2e/c").href());
    }

    /// Tests file URL Windows drive-letter normalization.
    @Test
    public void normalizesFileUrls() {
        assertEquals("file:///c:/demo", WebURL.of("file:c|/demo").href());
        assertEquals("file:///C:/demo", WebURL.of("file:///C|/demo").href());
        assertEquals("file:///C:/demo", WebURL.of("file://localhost/C:/demo").href());
    }

    /// Tests percent encoding in path, query, and fragment.
    @Test
    public void encodesUrlComponents() {
        assertEquals("data:text/plain,hi%20?x#%20y", WebURL.of("data:text/plain,hi ?x# y").href());
        assertEquals("http://example.com/%zz", WebURL.of("http://example.com/%zz").href());
        assertEquals("http://example.com/a%20b?x=1%202#h%20i",
                WebURL.of("http://example.com/a b?x=1 2#h i").href());
    }

    /// Tests port parsing and default-port elision.
    @Test
    public void handlesPorts() {
        assertEquals("http://example.com/", WebURL.of("http://example.com:80/").href());
        assertThrows(IllegalArgumentException.class, () -> WebURL.of("http://example.com:65536/"));
    }

    /// Tests setter no-op cases from the URL Standard.
    @Test
    public void ignoresSettersWhenUrlCannotAcceptComponent() {
        WebURL file = WebURL.of("file:///tmp/demo");
        WebURL changedFile = file.withUsername("user").withPassword("pass").withPort("123");
        assertEquals("file:///tmp/demo", changedFile.href());

        WebURL opaque = WebURL.of("data:text/plain,hello");
        WebURL changedOpaque = opaque.withHost("example.com").withHostname("example.com").withPathname("/ignored");
        assertEquals("data:text/plain,hello", changedOpaque.href());
    }

    /// Tests protocol setter constraints.
    @Test
    public void constrainsProtocolSetter() {
        WebURL special = WebURL.of("http://example.com:21/");
        assertEquals("ftp://example.com/", special.withProtocol("ftp").href());

        WebURL cannotBecomeNonSpecial = WebURL.of("http://example.com/");
        assertEquals("http://example.com/", cannotBecomeNonSpecial.withProtocol("foo").href());

        WebURL nonSpecial = WebURL.of("foo://example.com/path");
        assertEquals("foo://example.com/path", nonSpecial.withProtocol("https").href());
    }

    /// Tests opaque base URL fragment-only parsing and blob origin serialization.
    @Test
    public void handlesOpaqueAndBlobUrls() {
        assertEquals("data:text/plain,hello#frag", WebURL.of("#frag", "data:text/plain,hello").href());
        assertEquals("https://example.com", WebURL.of("blob:https://example.com/id").origin());
        assertEquals("null", WebURL.of("blob:ftp://example.com/id").origin());
    }

    /// Tests non-special authority and credentials handling.
    @Test
    public void handlesAuthorityBoundaries() {
        assertEquals("foo://", WebURL.of("foo://").href());
        assertEquals("foo://example.com/path", WebURL.of("foo://example.com/path").href());
        assertEquals("foo://user:pass@example.com/path", WebURL.of("foo://user:pass@example.com/path").href());
        assertEquals("http://user%40@example.com/", WebURL.of("http://user@@example.com/").href());
    }

    /// Tests empty query and fragment preservation.
    @Test
    public void preservesEmptyQueryAndFragment() {
        assertEquals("http://example.com/?x#y", WebURL.of("http://example.com?x#y").href());
        assertEquals("http://example.com/#y", WebURL.of("http://example.com#y").href());
        assertEquals("http://example.com/?", WebURL.of("http://example.com/?").href());
        assertEquals("http://example.com/#", WebURL.of("http://example.com/#").href());
    }

    /// Tests conversion to Java networking types.
    @Test
    public void convertsToJavaNetTypes() throws Exception {
        WebURL url = WebURL.of("https://example.com/a b?q=1#f");

        assertEquals(new URI("https://example.com/a%20b?q=1#f"), url.toURI());
        assertEquals("https://example.com/a%20b?q=1#f", url.toURL().toExternalForm());
        assertThrows(MalformedURLException.class, () -> WebURL.of("non-special:opaque").toURL());
    }

    /// Tests Java URI conversion for URLs whose WHATWG serialization is not Java URI syntax.
    @Test
    public void convertsToJavaUriSyntax() {
        assertEquals("http://!%22$&'()*+,-.;=_%60%7B%7D~/",
                WebURL.of("http://!\"$&'()*+,-.;=_`{}~/").toURI().toASCIIString());
        assertEquals("http://example.com/%25zz",
                WebURL.of("http://example.com/%zz").toURI().toASCIIString());
        assertEquals("data:text/plain,hi%20?x#%20y",
                WebURL.of("data:text/plain,hi ?x# y").toURI().toASCIIString());
    }
}
