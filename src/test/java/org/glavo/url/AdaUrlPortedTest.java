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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/// Tests ported from `ada-url/ada` at commit `d53b80614100a4f7ac40ae0ec3c1644185bb2f6d`.
@NotNullByDefault
public final class AdaUrlPortedTest {
    /// Tests Ada's additional URL parser data.
    ///
    /// Source:
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt/ada_extra_urltestdata.json#L1-L289
    @ParameterizedTest
    @MethodSource
    public void parsesAdaExtraUrlTestData(UrlCase testCase) {
        WebURL url = parse(testCase.input(), testCase.base());
        assertUrlCase(url, testCase);
    }

    /// Returns Ada's additional URL parser cases that should parse successfully.
    private static Stream<UrlCase> parsesAdaExtraUrlTestData() {
        return Stream.of(
                urlCase("#x", "about:blank", "about:blank#x", null,
                        "", "", "", "", "", "blank", "", "#x"),
                urlCase("https://lemire.me/école", "about:blank", "https://lemire.me/%C3%A9cole",
                        "https://lemire.me", "", "", "lemire.me", "lemire.me", "", "/%C3%A9cole", "", ""),
                urlCase("http://./", "about:blank", "http://./", "http://.",
                        "", "", ".", ".", "", "/", "", ""),
                urlCase("http://Yağız.com", "about:blank", "http://xn--yaz-isa4g.com/",
                        "http://xn--yaz-isa4g.com", "", "", "xn--yaz-isa4g.com", "xn--yaz-isa4g.com",
                        "", "/", "", ""),
                urlCase("fs:/hello.eth", "about:blank", "fs:/hello.eth", "null",
                        "", "", "", "", "", "/hello.eth", "", ""),
                urlCase("http://///\\'", "about:blank", "http://'/", "http://'",
                        "", "", "'", "'", "", "/", "", ""),
                urlCase("./foo", "http://www.example.org", "http://www.example.org/foo", null,
                        "", "", "www.example.org", "www.example.org", "", "/foo", "", ""),
                urlCase("mailto:a@b.com", "about:blank", "mailto:a@b.com", "null",
                        "", "", "", "", "", "a@b.com", "", ""),
                urlCase("scheme:example.com", "about:blank", "scheme:example.com", "null",
                        "", "", "", "", "", "example.com", "", ""),
                urlCase("scheme:example.com/path", "about:blank", "scheme:example.com/path", "null",
                        "", "", "", "", "", "example.com/path", "", ""),
                urlCase("scheme://username@@@@example.com", "about:blank",
                        "scheme://username%40%40%40@example.com", "null",
                        "username%40%40%40", "", "example.com", "example.com", "", "", "", ""),
                urlCase("file://localhost/path/to/file.txt", "about:blank", "file:///path/to/file.txt",
                        "null", "", "", "", "", "", "/path/to/file.txt", "", ""),
                urlCase("data:space    ?test#test", "about:blank", "data:space   %20?test#test", "null",
                        "", "", "", "", "", "space   %20", "?test", "#test"),
                urlCase("https://example.com/\"quoted\"", "about:blank",
                        "https://example.com/%22quoted%22", "https://example.com",
                        "", "", "example.com", "example.com", "", "/%22quoted%22", "", ""),
                urlCase("a:b#", null, "a:b#", null,
                        "", "", "", "", "", "b", "", "")
        );
    }

    /// Tests Ada's additional URL parser failures.
    ///
    /// Source:
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt/ada_extra_urltestdata.json#L3-L242
    @ParameterizedTest
    @MethodSource
    public void rejectsAdaExtraUrlTestData(String input, @Nullable String base) {
        assertNull(tryParse(input, base));
    }

    /// Returns Ada's additional URL parser cases that should fail.
    private static Stream<Arguments> rejectsAdaExtraUrlTestData() {
        return Stream.of(
                Arguments.of("https://www.google.com", "http://[0:0:0:0:0:0:0:0"),
                Arguments.of("example.com`x.example.com", null),
                Arguments.of("http://[0:0:0:0:0:0:0:0", "about:blank"),
                Arguments.of("http://0:0:0:0:0:0:0:0]", "about:blank"),
                Arguments.of("scheme://example.com:-1", "about:blank"),
                Arguments.of("scheme://example.com:+1", "about:blank"),
                Arguments.of("schéme://example.com", "about:blank"),
                Arguments.of("file://localhost:8098/path/to/file.txt", "about:blank"),
                Arguments.of("", "about:blank")
        );
    }

    /// Tests Ada's long IDNA host parser data.
    ///
    /// Source:
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/wpt/ada_long_urltestdata.json#L1-L19
    @ParameterizedTest
    @MethodSource
    public void parsesAdaLongUrlTestData(UrlCase testCase) {
        WebURL url = parse(testCase.input(), testCase.base());
        assertUrlCase(url, testCase);
    }

    /// Returns Ada's long URL parser cases.
    private static Stream<UrlCase> parsesAdaLongUrlTestData() {
        String host = "xn--teaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa78h"
                + "babbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.com";
        return Stream.of(urlCase(
                "http://ğığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığığı.com",
                "about:blank",
                "http://" + host + "/",
                "http://" + host,
                "", "", host, host, "", "/", "", ""
        ));
    }

    /// Tests selected Ada parser regressions from `basic_tests.cpp`.
    ///
    /// Sources:
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/basic_tests.cpp#L587-L664
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/basic_tests.cpp#L702-L745
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/basic_tests.cpp#L839-L956
    @ParameterizedTest
    @MethodSource
    public void parsesAdaBasicRegressionCases(UrlCase testCase) {
        WebURL url = parse(testCase.input(), testCase.base());
        assertUrlCase(url, testCase);
    }

    /// Returns selected Ada parser regression cases that should parse successfully.
    private static Stream<UrlCase> parsesAdaBasicRegressionCases() {
        return Stream.of(
                urlCase("localhost:80", null, "localhost:80", "null",
                        "", "", "", "", "", "80", "", ""),
                urlCase("http://\u200b123.123.123.123", null, "http://123.123.123.123/",
                        "http://123.123.123.123", "", "", "123.123.123.123", "123.123.123.123",
                        "", "/", "", ""),
                urlCase("/안녕", "https://non-ascii-location-header.sys.workers.dev/redirect",
                        "https://non-ascii-location-header.sys.workers.dev/%EC%95%88%EB%85%95",
                        null, "", "", "non-ascii-location-header.sys.workers.dev",
                        "non-ascii-location-header.sys.workers.dev", "", "/%EC%95%88%EB%85%95", "", ""),
                urlCase("file:///foo/.bar/../baz.js", null, "file:///foo/baz.js", null,
                        "", "", "", "", "", "/foo/baz.js", "", ""),
                urlCase("file:///foo/bar/baz.js", null, "file:///foo/bar/baz.js", null,
                        "", "", "", "", "", "/foo/bar/baz.js", "", ""),
                urlCase("http://foo/bar^baz", null, "http://foo/bar%5Ebaz", null,
                        "", "", "foo", "foo", "", "/bar%5Ebaz", "", ""),
                urlCase("https://example.sub.com/??", null, "https://example.sub.com/??", null,
                        "", "", "example.sub.com", "example.sub.com", "", "/", "??", ""),
                urlCase("https://example.sub.com/???", null, "https://example.sub.com/???", null,
                        "", "", "example.sub.com", "example.sub.com", "", "/", "???", ""),
                urlCase("https://example.sub.com/????", null, "https://example.sub.com/????", null,
                        "", "", "example.sub.com", "example.sub.com", "", "/", "????", ""),
                urlCase("", "file:///path?", "file:///path?", null,
                        "", "", "", "", "", "/path", "", ""),
                urlCase("", "file://e//.U./UU.//&eSe?", "file://e//.U./UU.//&eSe?", null,
                        "", "", "e", "e", "", "//.U./UU.//&eSe", "", ""),
                urlCase("", "https://example.com/path?", "https://example.com/path?", null,
                        "", "", "example.com", "example.com", "", "/path", "", ""),
                urlCase("#hash", "foo:bar?", "foo:bar?#hash", null,
                        "", "", "", "", "", "bar", "", "#hash")
        );
    }

    /// Tests selected Ada parser regression failures from `basic_tests.cpp`.
    ///
    /// Source:
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/basic_tests.cpp#L587-L648
    @ParameterizedTest
    @MethodSource
    public void rejectsAdaBasicRegressionCases(String input, @Nullable String base) {
        assertNull(tryParse(input, base));
    }

    /// Returns selected Ada parser regression cases that should fail.
    private static Stream<Arguments> rejectsAdaBasicRegressionCases() {
        return Stream.of(
                Arguments.of("", "localhost:80"),
                Arguments.of("http://1.1.1.256", null),
                Arguments.of("https://0.0.0.0x100/", null)
        );
    }

    /// Tests that Ada's mixed IDNA regression is stable after serialization.
    ///
    /// Source:
    /// https://github.com/ada-url/ada/blob/d53b80614100a4f7ac40ae0ec3c1644185bb2f6d/tests/basic_tests.cpp#L944-L956
    @ParameterizedTest
    @MethodSource
    public void reparsesAdaIdnaRegressionHref(String input) {
        WebURL url = WebURL.parse(input);
        WebURL reparsed = WebURL.parse(url.href());
        assertEquals(url.href(), reparsed.href());
    }

    /// Returns Ada IDNA idempotency regression inputs.
    private static Stream<String> reparsesAdaIdnaRegressionHref() {
        return Stream.of("http://\u33ff\u33fdxn--./");
    }

    /// Parses a URL with an optional base.
    private static WebURL parse(String input, @Nullable String base) {
        return base == null ? WebURL.parse(input) : WebURL.parse(input, base);
    }

    /// Tries to parse a URL with an optional base.
    private static @Nullable WebURL tryParse(String input, @Nullable String base) {
        return base == null ? WebURL.tryParse(input) : WebURL.tryParse(input, base);
    }

    /// Asserts all expected URL fields for a ported Ada URL case.
    private static void assertUrlCase(WebURL url, UrlCase expected) {
        assertEquals(expected.href(), url.href(), "href");
        if (expected.origin() != null) {
            assertEquals(expected.origin(), url.origin(), "origin");
        }
        assertEquals(expected.protocol(), url.getScheme() + ":", "protocol");
        assertEquals(expected.username(), url.getRawUsernameOrEmpty(), "username");
        assertEquals(expected.password(), url.getRawPasswordOrEmpty(), "password");
        assertEquals(expected.host(), host(url), "host");
        assertEquals(expected.hostname(), hostname(url), "hostname");
        assertEquals(expected.port(), port(url), "port");
        assertEquals(expected.pathname(), url.getRawPath(), "pathname");
        assertEquals(expected.search(), search(url), "search");
        assertEquals(expected.hash(), hash(url), "hash");
    }

    /// Returns the WHATWG host field from public URI-style URL components.
    private static String host(WebURL url) {
        @Nullable String host = url.getHost();
        if (host == null) {
            return "";
        }

        @Nullable String port = url.getRawPort();
        return port == null ? host : host + ":" + port;
    }

    /// Returns the WHATWG hostname field from public URI-style URL components.
    private static String hostname(WebURL url) {
        @Nullable String host = url.getHost();
        return host == null ? "" : host;
    }

    /// Returns the WHATWG port field from public URI-style URL components.
    private static String port(WebURL url) {
        @Nullable String port = url.getRawPort();
        return port == null ? "" : port;
    }

    /// Returns the WHATWG search field from public URI-style URL components.
    private static String search(WebURL url) {
        @Nullable String query = url.getRawQuery();
        return query == null || query.isEmpty() ? "" : "?" + query;
    }

    /// Returns the WHATWG hash field from public URI-style URL components.
    private static String hash(WebURL url) {
        @Nullable String fragment = url.getRawFragment();
        return fragment == null || fragment.isEmpty() ? "" : "#" + fragment;
    }

    /// Creates a URL case with a protocol inferred from the expected href.
    private static UrlCase urlCase(
            String input,
            @Nullable String base,
            String href,
            @Nullable String origin,
            String username,
            String password,
            String host,
            String hostname,
            String port,
            String pathname,
            String search,
            String hash
    ) {
        return new UrlCase(input, base, href, origin, protocol(href), username, password, host, hostname, port,
                pathname, search, hash);
    }

    /// Returns the protocol field from a serialized URL.
    private static String protocol(String href) {
        int schemeEnd = href.indexOf(':');
        if (schemeEnd < 0) {
            throw new AssertionError("Missing scheme delimiter in expected href: " + href);
        }
        return href.substring(0, schemeEnd + 1);
    }

    /// One expected URL parser case ported from Ada.
    private record UrlCase(
            String input,
            @Nullable String base,
            String href,
            @Nullable String origin,
            String protocol,
            String username,
            String password,
            String host,
            String hostname,
            String port,
            String pathname,
            String search,
            String hash
    ) {
    }
}
