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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests for `WebURL` parsing entry points.
@NotNullByDefault
public final class WebURLParsingTest {
    /// Tests explicit base URL string arguments.
    @Test
    public void parsesAgainstExplicitBaseString() {
        assertEquals("https://example.com/a/c", WebURL.parse("../c", "https://example.com/a/b/").href());
        assertEquals("https://example.com/a/b/d", WebURL.tryParse("d", "https://example.com/a/b/").href());
        assertNotNull(WebURL.tryParse("?q=1", "https://example.com/a/b/"));
        assertNull(WebURL.tryParse("?q=1"));
    }

    /// Tests explicit base arguments with an already parsed base URL.
    @Test
    public void parsesAgainstExplicitWebUrlBase() {
        WebURL base = WebURL.parse("https://example.org/x/y/");

        assertEquals("https://example.org/x/z", WebURL.parse("../z", base).href());
        assertEquals("https://example.org/x/z", base.resolve("../z").href());
        assertEquals("https://example.net/z", WebURL.parse("/z", "https://example.net/base").href());
    }

    /// Tests resolving URL inputs against an existing URL.
    @Test
    public void resolvesAgainstThisUrl() {
        WebURL base = WebURL.parse("https://example.com/a/b/?q=1#frag");

        assertEquals("https://example.com/a/c", base.resolve("../c").href());
        assertEquals("https://example.com/a/b/?next", base.resolve("?next").href());
        assertEquals("https://example.com/a/b/?q=1#top", base.resolve("#top").href());
        assertEquals("https://example.net/x", base.resolve("https://example.net/x").href());
        assertEquals(WebURLParseException.ErrorType.PORT_OUT_OF_RANGE,
                assertThrows(WebURLParseException.class,
                        () -> base.resolve("https://example.com:999999/")).getErrorType());
    }

    /// Tests reusable parser instances and strict validation behavior.
    @Test
    public void parsesWithReusableParsers() {
        assertEquals(Set.of(), WebURLParser.getDefault().getRejectedValidationErrors());
        for (WebURLParseException.ErrorType errorType : WebURLParseException.ErrorType.values()) {
            assertEquals(errorType.isRecoverable(),
                    WebURLParser.getStrict().getRejectedValidationErrors().contains(errorType),
                    errorType.getErrorName());
        }

        assertEquals(WebURL.parse("https://example.com/"), WebURLParser.getDefault().parse("https://example.com/"));
        assertEquals("http://user@example.com/", WebURLParser.getDefault().parse("http://user@example.com/").href());
        assertEquals("https://example.com/a", WebURLParser.getDefault().parse("a", "https://example.com/").href());
        assertEquals("https://example.com/a", WebURLParser.getDefault().parse("a", WebURL.parse("https://example.com/")).href());
        assertNotNull(WebURLParser.getDefault().tryParse("https://example.com/"));
        assertNotNull(WebURLParser.getDefault().tryParse("a", "https://example.com/"));
        assertNotNull(WebURLParser.getDefault().tryParse("a", WebURL.parse("https://example.com/")));
        assertEquals("https://example.com/", WebURLParser.getDefault().parseBrowserInput("example.com").href());
        assertNotNull(WebURLParser.getDefault().tryParseBrowserInput("example.com"));

        WebURLParseException exception = assertThrows(WebURLParseException.class,
                () -> WebURLParser.getStrict().parse("http://user@example.com/"));
        assertEquals(WebURLParseException.ErrorType.INVALID_CREDENTIALS, exception.getErrorType());
        assertEquals("http://user@example.com/", exception.getInput());
        assertEquals("http://user@example.com/".indexOf('@'), exception.getIndex());

        WebURLParseException whitespace = assertThrows(WebURLParseException.class,
                () -> WebURLParser.getStrict().parse(" https://example.com/"));
        assertEquals(WebURLParseException.ErrorType.INVALID_URL_UNIT, whitespace.getErrorType());
        assertEquals(" https://example.com/", whitespace.getInput());
        assertEquals(0, whitespace.getIndex());

        assertNull(WebURLParser.getStrict().tryParse("http://user@example.com/"));
        assertNull(WebURLParser.getStrict().tryParseBrowserInput("http://user@example.com/"));

        assertEquals(WebURLParseException.ErrorType.IPV4_EMPTY_PART,
                assertThrows(WebURLParseException.class,
                        () -> WebURLParser.getStrict().parse("http://127.0.0.1./")).getErrorType());
        assertEquals(WebURLParseException.ErrorType.IPV4_NON_DECIMAL_PART,
                assertThrows(WebURLParseException.class,
                        () -> WebURLParser.getStrict().parse("http://0x7f.0.0.1/")).getErrorType());
    }

    /// Tests that browser input leaves complete absolute URL strings to standard URL parsing.
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            https://example.com/a?b#c | https://example.com/a?b#c
            HTTP://EXAMPLE.COM:80/a | http://example.com/a
            https://dotlesshostname/ | https://dotlesshostname/
            data:text/plain,hi | data:text/plain,hi
            mailto:user@example.com | mailto:user@example.com
            file:///tmp/file.txt | file:///tmp/file.txt
            """)
    public void parsesCompleteUrlsWithBrowserInput(String input, String expected) {
        assertEquals(expected, WebURL.parseBrowserInput(input).href());
    }

    /// Tests browser-style public domain inputs that are completed with HTTPS.
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            example.com | https://example.com/
            EXAMPLE.COM | https://example.com/
            example.com/alpha | https://example.com/alpha
            example.com/a b | https://example.com/a%20b
            example.com?q=1#top | https://example.com/?q=1#top
            //example.com/path | https://example.com/path
            www.glavo.site/path?q=1#f | https://www.glavo.site/path?q=1#f
            例え.テスト | https://xn--r8jz45g.xn--zckzah/
            你好.世界 | https://xn--6qq79v.xn--rhqv96g/
            example。com | https://example.com/
            """)
    public void completesPublicDomainsWithHttps(String input, String expected) {
        assertEquals(expected, WebURL.parseBrowserInput(input).href());
    }

    /// Tests public domain inputs with explicit ports.
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            example.com:80 | http://example.com/
            example.com:080/path | http://example.com/path
            example.com:443 | http://example.com:443/
            example.com:8080 | http://example.com:8080/
            www.foo.com:81/path | http://www.foo.com:81/path
            //example.com:80/path | http://example.com/path
            //example.com:8080/path | http://example.com:8080/path
            """)
    public void keepsPublicDomainsWithExplicitPortsOnHttp(String input, String expected) {
        assertEquals(expected, WebURL.parseBrowserInput(input).href());
    }

    /// Tests browser-style local, single-label, and reserved host inputs that are completed with HTTP.
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            localhost | http://localhost/
            LOCALHOST:8080 | http://localhost:8080/
            foo.localhost | http://foo.localhost/
            test/path | http://test/path
            site.test | http://site.test/
            foo.example | http://foo.example/
            foo.invalid | http://foo.invalid/
            printer | http://printer/
            printer/path | http://printer/path
            dotlesshostname/ | http://dotlesshostname/
            foo:81 | http://foo:81/
            """)
    public void completesLocalAndReservedHostsWithHttp(String input, String expected) {
        assertEquals(expected, WebURL.parseBrowserInput(input).href());
    }

    /// Tests IP address inputs that are completed with HTTP.
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            127.0.0.1 | http://127.0.0.1/
            127.0.0.1:80 | http://127.0.0.1/
            127.0.0.1:8080 | http://127.0.0.1:8080/
            0.0.0.0 | http://0.0.0.0/
            0x7f.0.0.1 | http://127.0.0.1/
            [::1]:8080 | http://[::1]:8080/
            [2001:dB8::1] | http://[2001:db8::1]/
            //[::1]/ | http://[::1]/
            """)
    public void completesIpAddressesWithHttp(String input, String expected) {
        assertEquals(expected, WebURL.parseBrowserInput(input).href());
    }

    /// Tests local path inputs that are completed as file URLs.
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            C:\\Users\\Alice\\file.txt | file:///C:/Users/Alice/file.txt
            C:/Users/Alice/file.txt | file:///C:/Users/Alice/file.txt
            c:\\Program Files\\demo #1.txt | file:///c:/Program%20Files/demo%20%231.txt
            C:\\tmp\\100%\\a?b.txt | file:///C:/tmp/100%25/a%3Fb.txt
            /home/alice/file.txt | file:///home/alice/file.txt
            /tmp/a b#c?d | file:///tmp/a%20b%23c%3Fd
            /relative | file:///relative
            \\\\server\\share\\file.txt | file://server/share/file.txt
            \\\\files.example.com\\share name\\file.txt | file://files.example.com/share%20name/file.txt
            """)
    public void completesLocalPathsWithFileUrls(String input, String expected) {
        assertEquals(expected, WebURL.parseBrowserInput(input).href());
    }

    /// Tests browser-style input preprocessing before address completion.
    @Test
    public void preprocessesBrowserInputText() {
        assertEquals("https://example.com/path", WebURL.parseBrowserInput("  example.com/path  ").href());
        assertEquals("https://example.com/path", WebURL.parseBrowserInput("examp\tle.com/pa\nth").href());
        assertEquals("https://example.com/path", WebURL.parseBrowserInput("example.com\\path").href());
        assertEquals("file://example.com/path", WebURL.parseBrowserInput("\\\\example.com\\path").href());
    }

    /// Tests browser-style inputs that are neither complete URLs nor recognized URL-like addresses.
    @ParameterizedTest
    @CsvSource(textBlock = """
            not a url
            foo bar.com
            ?foo
            #fragment
            foo+bar
            foo+bar.com
            foo;bar
            foo._
            .internal
            example..com
            [2001:]
            [foo.com]
            """)
    public void rejectsNonUrlLikeBrowserInputs(String input) {
        assertNull(WebURL.tryParseBrowserInput(input));
        assertThrows(WebURLParseException.class, () -> WebURL.parseBrowserInput(input));
    }

    /// Tests browser-style input port failures.
    @Test
    public void reportsBrowserInputPortFailures() {
        assertNull(WebURL.tryParse("www.glavo.site"));
        assertNotNull(WebURL.tryParseBrowserInput("www.glavo.site"));
        assertNull(WebURL.tryParseBrowserInput("www.glavo.site:abc"));
        assertNull(WebURL.tryParseBrowserInput("example.com:123456"));
        assertEquals(WebURLParseException.ErrorType.PORT_INVALID,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parseBrowserInput("www.glavo.site:abc")).getErrorType());
        assertEquals(WebURLParseException.ErrorType.PORT_OUT_OF_RANGE,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parseBrowserInput("example.com:123456")).getErrorType());
    }

    /// Tests parse and tryParse failure handling.
    @Test
    public void reportsParsingFailures() {
        assertNull(WebURL.tryParse("https://example.com:999999/"));
        assertNull(WebURL.tryParse("/relative", "not a url"));
        assertEquals(WebURLParseException.ErrorType.PORT_OUT_OF_RANGE,
                assertThrows(WebURLParseException.class,
                        () -> WebURL.parse("https://example.com:999999/")).getErrorType());
    }

    /// Tests UTS #46 domain processing.
    @Test
    public void parsesUnicodeDomainsWithUts46() {
        assertEquals("https://xn--bcher-kva.example/", WebURL.parse("https://bücher.example/").href());
        assertEquals("https://xn--fa-hia.example/", WebURL.parse("https://faß.example/").href());
        assertNotNull(WebURL.tryParse("https://xn--bcher-kva.example/"));
    }
}
