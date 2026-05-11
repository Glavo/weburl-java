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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    /// Tests parse and tryParse failure handling.
    @Test
    public void reportsParseFailures() {
        assertThrows(WebURLParseException.class, () -> WebURL.parse("/en-US/docs"));
        assertThrows(WebURLParseException.class, () -> WebURL.parse("/en-US/docs", ""));
        assertNull(WebURL.tryParse("/en-US/docs"));
        assertNotNull(WebURL.tryParse("/en-US/docs", "https://developer.mozilla.org"));
    }

    /// Tests conversions from Java URL-like types.
    @Test
    public void createsFromJavaUrlTypes() throws Exception {
        URI uri = URI.create("https://example.com/a%20b?q=1#f");
        assertEquals("https://example.com/a%20b?q=1#f", WebURL.of(uri).href());

        URL url = new URL("https://example.com/a%20b?q=1#f");
        assertEquals("https://example.com/a%20b?q=1#f", WebURL.of(url).href());

        Path path = Path.of("build", "weburl of", "file.txt").toAbsolutePath();
        WebURL pathUrl = WebURL.of(path);
        assertEquals(WebURL.of(path.toUri()), pathUrl);
        assertEquals("file", pathUrl.getScheme());

        assertThrows(WebURLParseException.class, () -> WebURL.of(URI.create("../relative")));
    }

    /// Tests parse exception details for selected URL Standard validation errors.
    @Test
    public void reportsParseExceptionDetails() {
        WebURLParseException missingScheme =
                assertThrows(WebURLParseException.class, () -> WebURL.parse("/en-US/docs"));
        assertEquals(WebURLParseException.MISSING_SCHEME_NON_RELATIVE_URL, missingScheme.getErrorName());
        assertEquals("/en-US/docs", missingScheme.getInput());
        assertEquals(0, missingScheme.getIndex());
        assertEquals("The input is missing a scheme and cannot be parsed relative to a base URL",
                missingScheme.getReason());
        assertEquals("The input is missing a scheme and cannot be parsed relative to a base URL at index 0: /en-US/docs",
                missingScheme.getMessage());

        assertEquals(WebURLParseException.PORT_OUT_OF_RANGE,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("http://example.com:65536/")).getErrorName());
        assertEquals(WebURLParseException.PORT_INVALID,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("http://example.com:7z/")).getErrorName());
        assertEquals(WebURLParseException.HOST_MISSING,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("https://:443")).getErrorName());
        assertEquals(WebURLParseException.IPV6_UNCLOSED,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("https://[::1")).getErrorName());
        assertEquals(WebURLParseException.IPV4_TOO_MANY_PARTS,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("https://1.2.3.4.5/")).getErrorName());
    }

    /// Tests that already canonical input strings are adopted as the URL serialization.
    @Test
    public void reusesCanonicalInputSerialization() {
        String basic = "https://user:pass@example.com:8080/a/b?x=1#f";
        assertSame(basic, WebURL.parse(basic).href());

        String ipv6 = "http://[2001:db8::1]/";
        assertSame(ipv6, WebURL.parse(ipv6).href());
    }

    /// Tests that repeated computed getters return cached values.
    @Test
    public void cachesComputedValues() {
        WebURL url = WebURL.parse("https://user:pass@example.com:8080/a/b?x=1#f");

        assertSame(url.href(), url.href());
        assertSame(url.origin(), url.origin());
        assertSame(url.getScheme(), url.getScheme());
        assertSame(url.getUsername(), url.getUsername());
        assertSame(url.getRawUsername(), url.getRawUsername());
        assertSame(url.getRawUsernameOrEmpty(), url.getRawUsernameOrEmpty());
        assertSame(url.getPassword(), url.getPassword());
        assertSame(url.getRawPassword(), url.getRawPassword());
        assertSame(url.getRawPasswordOrEmpty(), url.getRawPasswordOrEmpty());
        assertSame(url.getUserInfo(), url.getUserInfo());
        assertSame(url.getRawUserInfo(), url.getRawUserInfo());
        assertSame(url.getAuthority(), url.getAuthority());
        assertSame(url.getRawAuthority(), url.getRawAuthority());
        assertSame(url.getHost(), url.getHost());
        assertSame(url.getPath(), url.getPath());
        assertSame(url.getRawPath(), url.getRawPath());
        assertSame(url.getQuery(), url.getQuery());
        assertSame(url.getRawQuery(), url.getRawQuery());
        assertSame(url.getRawQueryOrEmpty(), url.getRawQueryOrEmpty());
        assertSame(url.getFragment(), url.getFragment());
        assertSame(url.getRawFragment(), url.getRawFragment());
        assertSame(url.getRawFragmentOrEmpty(), url.getRawFragmentOrEmpty());
        assertSame(url.href(), url.toDisplayString());
        assertSame(url.toDisplayString(), url.toDisplayString());
        assertSame(url.href(), url.toRFC2396String());
        assertSame(url.toRFC2396String(), url.toRFC2396String());
        assertSame(url.toURI(), url.toURI());
    }

    /// Tests Java-style raw component getters.
    @Test
    public void readsJavaStyleRawComponents() {
        WebURL url = WebURL.parse("https://user:pass@example.com:8080/a%2Fb?x=a%26b#frag%23ment");

        assertEquals("https", url.getScheme());
        assertEquals("user", url.getUsername());
        assertEquals("user", url.getRawUsername());
        assertEquals("user", url.getRawUsernameOrEmpty());
        assertEquals("pass", url.getPassword());
        assertEquals("pass", url.getRawPassword());
        assertEquals("pass", url.getRawPasswordOrEmpty());
        assertEquals("user:pass", url.getUserInfo());
        assertEquals("user:pass", url.getRawUserInfo());
        assertEquals("user:pass@example.com:8080", url.getAuthority());
        assertEquals("user:pass@example.com:8080", url.getRawAuthority());
        assertEquals("example.com", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("8080", url.getRawPort());
        assertEquals("/a%2Fb", url.getRawPath());
        assertEquals("x=a%26b", url.getRawQuery());
        assertEquals("x=a%26b", url.getRawQueryOrEmpty());
        assertEquals("frag%23ment", url.getRawFragment());
        assertEquals("frag%23ment", url.getRawFragmentOrEmpty());

        WebURL absentComponents = WebURL.parse("https://example.com/path");
        assertNull(absentComponents.getUsername());
        assertNull(absentComponents.getRawUsername());
        assertEquals("", absentComponents.getRawUsernameOrEmpty());
        assertNull(absentComponents.getPassword());
        assertNull(absentComponents.getRawPassword());
        assertEquals("", absentComponents.getRawPasswordOrEmpty());
        assertNull(absentComponents.getUserInfo());
        assertNull(absentComponents.getRawUserInfo());
        assertEquals("example.com", absentComponents.getAuthority());
        assertEquals("example.com", absentComponents.getRawAuthority());
        assertEquals("example.com", absentComponents.getHost());
        assertEquals(443, absentComponents.getPort());
        assertNull(absentComponents.getRawPort());
        assertEquals("/path", absentComponents.getRawPath());
        assertNull(absentComponents.getRawQuery());
        assertEquals("", absentComponents.getRawQueryOrEmpty());
        assertNull(absentComponents.getRawFragment());
        assertEquals("", absentComponents.getRawFragmentOrEmpty());

        WebURL defaultPort = WebURL.parse("https://example.com:443/path");
        assertEquals(443, defaultPort.getPort());
        assertNull(defaultPort.getRawPort());
        assertEquals("", WebURL.parse("https://example.com/path?").getRawQuery());
        assertEquals("", WebURL.parse("https://example.com/path#").getRawFragment());

        WebURL emptyUsername = WebURL.parse("https://:pass@example.com/");
        assertEquals("", emptyUsername.getUsername());
        assertEquals("", emptyUsername.getRawUsername());
        assertEquals("pass", emptyUsername.getPassword());
        assertEquals("pass", emptyUsername.getRawPassword());
        assertEquals(":pass", emptyUsername.getUserInfo());
        assertEquals(":pass", emptyUsername.getRawUserInfo());

        WebURL emptyAuthority = WebURL.parse("file:///C:/demo");
        assertNull(emptyAuthority.getUserInfo());
        assertNull(emptyAuthority.getRawUserInfo());
        assertEquals("", emptyAuthority.getAuthority());
        assertEquals("", emptyAuthority.getRawAuthority());

        WebURL noAuthority = WebURL.parse("data:text/plain,hi");
        assertNull(noAuthority.getUserInfo());
        assertNull(noAuthority.getRawUserInfo());
        assertNull(noAuthority.getAuthority());
        assertNull(noAuthority.getRawAuthority());
    }

    /// Tests Java-style decoded component getters.
    @Test
    public void readsJavaStyleDecodedComponents() {
        WebURL url = WebURL.parse(
                "https://example.com/a%20b/%E8%B7%AF%E5%BE%84?plus=a+b&encoded=a%2Bb&space=a%20b#frag%23ment%20x");

        assertEquals("/a b/路径", url.getPath());
        assertEquals("plus=a+b&encoded=a+b&space=a b", url.getQuery());
        assertEquals("frag#ment x", url.getFragment());

        WebURL credentials = WebURL.parse("https://u%20ser:p%40ss@example.com/");
        assertEquals("u%20ser", credentials.getRawUsername());
        assertEquals("u ser", credentials.getUsername());
        assertEquals("p%40ss", credentials.getRawPassword());
        assertEquals("p@ss", credentials.getPassword());
        assertEquals("u%20ser:p%40ss", credentials.getRawUserInfo());
        assertEquals("u ser:p@ss", credentials.getUserInfo());
        assertEquals("u%20ser:p%40ss@example.com", credentials.getRawAuthority());
        assertEquals("u ser:p@ss@example.com", credentials.getAuthority());

        WebURL absentComponents = WebURL.parse("https://example.com/path");
        assertNull(absentComponents.getQuery());
        assertNull(absentComponents.getFragment());

        assertEquals("", WebURL.parse("https://example.com/path?").getQuery());
        assertEquals("", WebURL.parse("https://example.com/path#").getFragment());
    }

    /// Tests decoded component getters preserve invalid percent triplets literally.
    @Test
    public void preservesInvalidPercentTripletsInDecodedComponents() {
        WebURL url = WebURL.parse("http://example.com/%zz?x=%zz#%zz");
        URI uri = url.toURI();

        assertEquals("/%zz", url.getPath());
        assertEquals("x=%zz", url.getQuery());
        assertEquals("%zz", url.getFragment());
        assertEquals(uri.getPath(), url.getPath());
        assertEquals(uri.getQuery(), url.getQuery());
        assertEquals(uri.getFragment(), url.getFragment());
    }

    /// Tests equality, hash code, and natural ordering by serialized URL.
    @Test
    public void comparesBySerializedUrl() {
        WebURL canonical = WebURL.parse("https://example.com/a");
        WebURL normalized = WebURL.parse("HTTPS://EXAMPLE.COM:443/a");
        WebURL different = WebURL.parse("https://example.com/b");

        assertEquals(canonical, normalized);
        assertEquals(canonical.hashCode(), normalized.hashCode());
        assertEquals(0, canonical.compareTo(normalized));

        assertNotEquals(canonical, different);
        Object serialized = canonical.href();
        assertNotEquals(serialized, canonical);
        assertNotEquals(nullObject(), canonical);
        assertTrue(canonical.compareTo(different) < 0);
        assertTrue(different.compareTo(canonical) > 0);

        List<WebURL> urls = new ArrayList<>(List.of(
                WebURL.parse("https://example.org/"),
                different,
                canonical
        ));
        urls.sort(null);

        assertEquals(List.of(
                canonical,
                different,
                WebURL.parse("https://example.org/")
        ), urls);
    }

    /// Tests Java serialization round-trips by the canonical URL serialization.
    @Test
    public void serializesByHref() throws Exception {
        for (String input : List.of(
                "https://user%40name:pa%3Ass@example.com/a%2Fb?x=a%26b#frag%23ment",
                "https://xn--6qq79v.xn--rhqv96g/%E8%B7%AF%E5%BE%84?q=%E5%80%BC#%E7%89%87",
                "http://[2001:db8::1]:8080/api",
                "file:///C:/Users/Glavo/project/file.txt",
                "data:text/plain,hello%20world",
                "non-special:"
        )) {
            WebURL url = WebURL.parse(input);
            url.toDisplayString();
            WebURL deserialized = serializeRoundTrip(url);

            assertNotSame(url, deserialized);
            assertEquals(url, deserialized);
            assertEquals(url.hashCode(), deserialized.hashCode());
            assertEquals(0, url.compareTo(deserialized));
            assertEquals(url.href(), deserialized.href());
            assertEquals(url.origin(), deserialized.origin());
            assertEquals(url.toDisplayString(), deserialized.toDisplayString());
            assertEquals(url.getRawAuthority(), deserialized.getRawAuthority());
            assertEquals(url.getRawPort(), deserialized.getRawPort());
            assertEquals(url.getRawPath(), deserialized.getRawPath());
            assertEquals(url.getRawQuery(), deserialized.getRawQuery());
            assertEquals(url.getRawFragment(), deserialized.getRawFragment());
            assertEquals(url.toRFC2396String(), deserialized.toRFC2396String());
        }
    }

    /// Tests host parsing and serialization.
    @Test
    public void parsesHosts() {
        assertEquals("http://127.0.0.1/", WebURL.parse("http://127.1").href());
        assertEquals("127.0.0.1", WebURL.parse("http://127.1").getHost());
        assertEquals("http://[2001:db8::1]/", WebURL.parse("http://[2001:db8::1]/").href());
        assertEquals("[2001:db8::1]", WebURL.parse("http://[2001:db8::1]/").getHost());
        assertEquals("https://xn--bcher-kva.example/", WebURL.parse("https://bücher.example/").href());
        assertEquals("xn--bcher-kva.example", WebURL.parse("https://bücher.example/").getHost());
        assertEquals("", WebURL.parse("file:///C:/demo").getHost());
        assertNull(WebURL.parse("data:text/plain,hi").getHost());
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
        WebURL defaultHttpPort = WebURL.parse("http://example.com:80/");
        assertEquals("http://example.com/", defaultHttpPort.href());
        assertEquals(80, defaultHttpPort.getPort());
        assertNull(defaultHttpPort.getRawPort());

        WebURL explicitHttpPort = WebURL.parse("http://example.com:8080/");
        assertEquals(8080, explicitHttpPort.getPort());
        assertEquals("8080", explicitHttpPort.getRawPort());

        WebURL defaultFtpPort = WebURL.parse("ftp://example.com/");
        assertEquals(21, defaultFtpPort.getPort());
        assertNull(defaultFtpPort.getRawPort());

        WebURL nonSpecialWithoutDefaultPort = WebURL.parse("foo://example.com/");
        assertEquals(-1, nonSpecialWithoutDefaultPort.getPort());
        assertNull(nonSpecialWithoutDefaultPort.getRawPort());

        assertEquals(WebURLParseException.PORT_OUT_OF_RANGE,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("http://example.com:65536/")).getErrorName());
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

    /// Tests browser-like display serialization.
    @Test
    public void returnsDisplayString() {
        WebURL url = WebURL.parse(
                "https://user:%E5%AF%86@example.com/%E8%B7%AF%E5%BE%84?q=%E5%80%BC+%2F#%E7%89%87%23");
        assertEquals("https://user:密@example.com/路径?q=值+%2F#片%23", url.toDisplayString());

        WebURL idn = WebURL.parse("https://bücher.example/%F0%9F%98%80");
        assertEquals("https://bücher.example/😀", idn.toDisplayString());

        assertEquals("http://例え.テスト/",
                WebURL.parse("http://xn--r8jz45g.xn--zckzah/").toDisplayString());
        assertEquals("https://你好.世界/",
                WebURL.parse("https://xn--6qq79v.xn--rhqv96g/").toDisplayString());
        assertEquals("http://[2001:db8::1]/路径",
                WebURL.parse("http://[2001:db8::1]/%E8%B7%AF%E5%BE%84").toDisplayString());

        assertEquals("http://example.com/%zz?x=%E8%28#%E2%80%AE",
                WebURL.parse("http://example.com/%zz?x=%E8%28#%E2%80%AE").toDisplayString());
    }

    /// Tests conversion to Java networking types.
    @Test
    public void convertsToJavaNetTypes() throws Exception {
        WebURL url = WebURL.parse("https://example.com/a b?q=1#f");

        assertEquals(new URI("https://example.com/a%20b?q=1#f"), url.toURI());
        assertEquals(new URI("https://example.com/a%20b?q=1#f"), WebURL.toURI("https://example.com/a b?q=1#f"));
        assertEquals("https://example.com/a%20b?q=1#f", url.toURL().toExternalForm());
        assertEquals("https://example.com/a%20b?q=1#f", WebURL.toURL("https://example.com/a b?q=1#f").toExternalForm());
        assertThrows(MalformedURLException.class, () -> WebURL.parse("non-special:opaque").toURL());
        assertThrows(MalformedURLException.class, () -> WebURL.toURL("non-special:opaque"));
    }

    /// Tests Java URI conversion for URLs whose WHATWG serialization is not Java URI syntax.
    @Test
    public void convertsToJavaUriSyntax() {
        assertEquals("http://!%22$&'()*+,-.;=_%60%7B%7D~/",
                WebURL.parse("http://!\"$&'()*+,-.;=_`{}~/").toRFC2396String());
        assertEquals("http://example.com/%25zz",
                WebURL.parse("http://example.com/%zz").toRFC2396String());
        assertEquals("data:text/plain,hi%20?x#%20y",
                WebURL.parse("data:text/plain,hi ?x# y").toRFC2396String());
    }

    /// Tests Java URI conversion preserves existing percent escapes.
    @Test
    public void preservesPercentEscapesInJavaUriConversion() {
        WebURL url = WebURL.parse("https://user%40name:pa%3Ass@example.com/a%2Fb?x=a%26b#frag%23ment");
        URI uri = url.toURI();

        assertEquals("https://user%40name:pa%3Ass@example.com/a%2Fb?x=a%26b#frag%23ment",
                url.toRFC2396String());
        assertEquals(url.toRFC2396String(),
                uri.toASCIIString());
        assertEquals("/a%2Fb", uri.getRawPath());
        assertEquals("x=a%26b", uri.getRawQuery());
        assertEquals("frag%23ment", uri.getRawFragment());
    }

    /// Tests Java URI conversion escapes bare percent signs in every raw component.
    @Test
    public void escapesBarePercentInJavaUriConversion() {
        assertEquals("http://example.com/%25zz?x=%25zz#%25zz",
                WebURL.parse("http://example.com/%zz?x=%zz#%zz").toRFC2396String());
    }

    /// Tests Java URI conversion escapes characters outside RFC 2396.
    @Test
    public void escapesNonRfc2396CharactersInJavaUriConversion() {
        assertEquals("http://example.com/%5B%5D?x=%5B%5D%7B%7D%7C%60#a%5B%5D%7B%7D%7C%60%23b",
                WebURL.parse("http://example.com/[]?x=[]{}|`#a[]{}|`#b").toRFC2396String());
    }

    /// Tests Java URI conversion rejects WHATWG URLs that have no RFC 2396 representation.
    @Test
    public void rejectsUrlsWithoutRfc2396Representation() {
        WebURL emptyOpaque = WebURL.parse("non-special:");
        WebURL emptyOpaqueWithFragment = WebURL.parse("non-special:#fragment");

        assertEquals("non-special:", emptyOpaque.toRFC2396String());
        assertEquals("non-special:#fragment", emptyOpaqueWithFragment.toRFC2396String());
        assertThrows(IllegalStateException.class, emptyOpaque::toURI);
        assertThrows(IllegalStateException.class, emptyOpaqueWithFragment::toURI);
    }

    /// Serializes and deserializes a `WebURL`.
    private static WebURL serializeRoundTrip(WebURL url) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(url);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (WebURL) input.readObject();
        }
    }

    /// Returns a nullable object for equality contract assertions.
    private static @Nullable Object nullObject() {
        return null;
    }
}
