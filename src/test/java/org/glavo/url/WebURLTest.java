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
        WebURL a = WebURL.parse("/", "https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", a.href());

        WebURL b = WebURL.parse("https://developer.mozilla.org");
        assertEquals("https://developer.mozilla.org/", b.href());

        WebURL c = WebURL.parse("en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", c.href());

        WebURL d = WebURL.parse("/en-US/docs", b);
        assertEquals("https://developer.mozilla.org/en-US/docs", d.href());

        WebURL e = WebURL.parse("/en-US/docs", "https://developer.mozilla.org/fr-FR/toto");
        assertEquals("https://developer.mozilla.org/en-US/docs", e.href());
    }

    /// Tests parse and canParse failure handling.
    @Test
    public void reportsParseFailures() {
        assertThrows(IllegalArgumentException.class, () -> WebURL.parse("/en-US/docs"));
        assertThrows(IllegalArgumentException.class, () -> WebURL.parse("/en-US/docs", ""));
        assertNull(WebURL.tryParse("/en-US/docs"));
        assertFalse(WebURL.canParse("/en-US/docs"));
        assertTrue(WebURL.canParse("/en-US/docs", "https://developer.mozilla.org"));
    }

    /// Tests typed parse exceptions for selected URL Standard validation errors.
    @Test
    public void reportsTypedParseExceptions() {
        WebURLParseException missingScheme =
                assertThrows(WebURLParseException.MissingSchemeNonRelativeURL.class,
                        () -> WebURL.parse("/en-US/docs"));
        assertEquals("missing-scheme-non-relative-URL", missingScheme.errorName());

        assertThrows(WebURLParseException.PortOutOfRange.class,
                () -> WebURL.parse("http://example.com:65536/"));
        assertThrows(WebURLParseException.PortInvalid.class,
                () -> WebURL.parse("http://example.com:7z/"));
        assertThrows(WebURLParseException.HostMissing.class,
                () -> WebURL.parse("https://:443"));
        assertThrows(WebURLParseException.IPv6Unclosed.class,
                () -> WebURL.parse("https://[::1"));
        assertThrows(WebURLParseException.IPv4TooManyParts.class,
                () -> WebURL.parse("https://1.2.3.4.5/"));
    }

    /// Tests URL getters and setters.
    @Test
    public void updatesComponentsWithSetters() {
        WebURL url = WebURL.parse("https://user:pass@example.com:443/a/b?x=1#f");
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
        WebURL url = WebURL.parse("https://example.test/?a=1&a=2");
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
        assertEquals("http://127.0.0.1/", WebURL.parse("http://127.1").href());
        assertEquals("http://[2001:db8::1]/", WebURL.parse("http://[2001:db8::1]/").href());
        assertEquals("https://xn--bcher-kva.example/", WebURL.parse("https://bücher.example/").href());
    }

    /// Tests file URL origin behavior.
    @Test
    public void returnsNullOriginForFileUrls() {
        WebURL url = WebURL.parse("file:///C:/demo");
        assertEquals("null", url.origin());
    }

    /// Tests path normalization and percent-encoded dot segments.
    @Test
    public void normalizesPathSegments() {
        assertEquals("http://example.com/foo/baz",
                WebURL.parse("http://example.com/foo/./bar/../baz").href());
        assertEquals("http://example.com/a/c",
                WebURL.parse("http://example.com/a/%2e/b/%2e%2e/c").href());
    }

    /// Tests file URL Windows drive-letter normalization.
    @Test
    public void normalizesFileUrls() {
        assertEquals("file:///c:/demo", WebURL.parse("file:c|/demo").href());
        assertEquals("file:///C:/demo", WebURL.parse("file:///C|/demo").href());
        assertEquals("file:///C:/demo", WebURL.parse("file://localhost/C:/demo").href());
    }

    /// Tests percent encoding in path, query, and fragment.
    @Test
    public void encodesUrlComponents() {
        assertEquals("data:text/plain,hi%20?x#%20y", WebURL.parse("data:text/plain,hi ?x# y").href());
        assertEquals("http://example.com/%zz", WebURL.parse("http://example.com/%zz").href());
        assertEquals("http://example.com/a%20b?x=1%202#h%20i",
                WebURL.parse("http://example.com/a b?x=1 2#h i").href());
    }

    /// Tests port parsing and default-port elision.
    @Test
    public void handlesPorts() {
        assertEquals("http://example.com/", WebURL.parse("http://example.com:80/").href());
        assertThrows(IllegalArgumentException.class, () -> WebURL.parse("http://example.com:65536/"));
    }

    /// Tests setter no-op cases from the URL Standard.
    @Test
    public void ignoresSettersWhenUrlCannotAcceptComponent() {
        WebURL file = WebURL.parse("file:///tmp/demo");
        WebURL changedFile = file.withUsername("user").withPassword("pass").withPort("123");
        assertEquals("file:///tmp/demo", changedFile.href());

        WebURL opaque = WebURL.parse("data:text/plain,hello");
        WebURL changedOpaque = opaque.withHost("example.com").withHostname("example.com").withPathname("/ignored");
        assertEquals("data:text/plain,hello", changedOpaque.href());
    }

    /// Tests protocol setter constraints.
    @Test
    public void constrainsProtocolSetter() {
        WebURL special = WebURL.parse("http://example.com:21/");
        assertEquals("ftp://example.com/", special.withProtocol("ftp").href());

        WebURL cannotBecomeNonSpecial = WebURL.parse("http://example.com/");
        assertEquals("http://example.com/", cannotBecomeNonSpecial.withProtocol("foo").href());

        WebURL nonSpecial = WebURL.parse("foo://example.com/path");
        assertEquals("foo://example.com/path", nonSpecial.withProtocol("https").href());
    }

    /// Tests opaque base URL fragment-only parsing and blob origin serialization.
    @Test
    public void handlesOpaqueAndBlobUrls() {
        assertEquals("data:text/plain,hello#frag", WebURL.parse("#frag", "data:text/plain,hello").href());
        assertEquals("https://example.com", WebURL.parse("blob:https://example.com/id").origin());
        assertEquals("null", WebURL.parse("blob:ftp://example.com/id").origin());
    }

    /// Tests non-special authority and credentials handling.
    @Test
    public void handlesAuthorityBoundaries() {
        assertEquals("foo://", WebURL.parse("foo://").href());
        assertEquals("foo://example.com/path", WebURL.parse("foo://example.com/path").href());
        assertEquals("foo://user:pass@example.com/path", WebURL.parse("foo://user:pass@example.com/path").href());
        assertEquals("http://user%40@example.com/", WebURL.parse("http://user@@example.com/").href());
    }

    /// Tests empty query and fragment preservation.
    @Test
    public void preservesEmptyQueryAndFragment() {
        assertEquals("http://example.com/?x#y", WebURL.parse("http://example.com?x#y").href());
        assertEquals("http://example.com/#y", WebURL.parse("http://example.com#y").href());
        assertEquals("http://example.com/?", WebURL.parse("http://example.com/?").href());
        assertEquals("http://example.com/#", WebURL.parse("http://example.com/#").href());
    }

    /// Tests conversion to Java networking types.
    @Test
    public void convertsToJavaNetTypes() throws Exception {
        WebURL url = WebURL.parse("https://example.com/a b?q=1#f");

        assertEquals(new URI("https://example.com/a%20b?q=1#f"), url.toURI());
        assertEquals("https://example.com/a%20b?q=1#f", url.toURL().toExternalForm());
        assertThrows(MalformedURLException.class, () -> WebURL.parse("non-special:opaque").toURL());
    }

    /// Tests Java URI conversion for URLs whose WHATWG serialization is not Java URI syntax.
    @Test
    public void convertsToJavaUriSyntax() {
        assertEquals("http://!%22$&'()*+,-.;=_%60%7B%7D~/",
                WebURL.parse("http://!\"$&'()*+,-.;=_`{}~/").toURI().toASCIIString());
        assertEquals("http://example.com/%25zz",
                WebURL.parse("http://example.com/%zz").toURI().toASCIIString());
        assertEquals("data:text/plain,hi%20?x#%20y",
                WebURL.parse("data:text/plain,hi ?x# y").toURI().toASCIIString());
    }

    /// Tests Java URI conversion preserves existing percent escapes.
    @Test
    public void preservesPercentEscapesInJavaUriConversion() {
        WebURL url = WebURL.parse("https://user%40name:pa%3Ass@example.com/a%2Fb?x=a%26b#frag%23ment");
        URI uri = url.toURI();

        assertEquals("https://user%40name:pa%3Ass@example.com/a%2Fb?x=a%26b#frag%23ment",
                uri.toASCIIString());
        assertEquals("/a%2Fb", uri.getRawPath());
        assertEquals("x=a%26b", uri.getRawQuery());
        assertEquals("frag%23ment", uri.getRawFragment());
    }

    /// Tests Java URI conversion escapes bare percent signs in every raw component.
    @Test
    public void escapesBarePercentInJavaUriConversion() {
        assertEquals("http://example.com/%25zz?x=%25zz#%25zz",
                WebURL.parse("http://example.com/%zz?x=%zz#%zz").toURI().toASCIIString());
    }

    /// Tests Java URI conversion escapes characters outside RFC 2396.
    @Test
    public void escapesNonRfc2396CharactersInJavaUriConversion() {
        assertEquals("http://example.com/%5B%5D?x=%5B%5D%7B%7D%7C%60#a%5B%5D%7B%7D%7C%60%23b",
                WebURL.parse("http://example.com/[]?x=[]{}|`#a[]{}|`#b").toURI().toASCIIString());
    }

    /// Tests Java URI conversion rejects WHATWG URLs that have no RFC 2396 representation.
    @Test
    public void rejectsUrlsWithoutRfc2396Representation() {
        assertThrows(IllegalStateException.class, () -> WebURL.parse("non-special:").toURI());
        assertThrows(IllegalStateException.class, () -> WebURL.parse("non-special:#fragment").toURI());
    }
}
