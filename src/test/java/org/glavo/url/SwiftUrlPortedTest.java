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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests ported from `karwa/swift-url` at commit `9306a962396a50d7d88e924afcd7ec67226763db`.
@NotNullByDefault
public final class SwiftUrlPortedTest {
    /// Tests legacy IPv4 host parsing and serialization.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/IPv4AddressTests.swift#L22-L171
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            3237937669 | http://192.255.2.5/
            0xC0.077601005 | http://192.255.2.5/
            192.0xff.01005 | http://192.255.2.5/
            192.255.2.5 | http://192.255.2.5/
            0xc0.0xff.0x02.0x05 | http://192.255.2.5/
            1.2.3.4. | http://1.2.3.4/
            234 | http://0.0.0.234/
            234.0 | http://234.0.0.0/
            234.011 | http://234.0.0.9/
            234.011.0 | http://234.9.0.0/
            0xFFFFFFFF | http://255.255.255.255/
            """)
    public void parsesSwiftUrlIpv4AddressCases(String host, String expectedHref) {
        WebURL url = WebURL.parse("http://" + host + "/");
        assertEquals(expectedHref, url.href());
    }

    /// Tests selected invalid legacy IPv4 inputs that are also rejected as URL hosts.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/IPv4AddressTests.swift#L51-L238
    @ParameterizedTest
    @CsvSource(textBlock = """
            1.2.3.4.5
            234.011.0.0.0
            0xFFFFFFFF1
            1.0xFFFFFFF
            1.1.0xFFFFF
            1.1.1.0xFFF
            """)
    public void rejectsSwiftUrlInvalidIpv4AddressCases(String host) {
        assertNull(WebURL.tryParse("http://" + host + "/"));
    }

    /// Tests IPv6 host parsing, compression, and IPv4-in-IPv6 syntax.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/IPv6AddressTests.swift#L22-L127
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            2001:0db8:85a3:0000:0000:8a2e:0370:7334 | http://[2001:db8:85a3::8a2e:370:7334]/
            2001::ce49:7601:e866:efff:62c3:fffe | http://[2001:0:ce49:7601:e866:efff:62c3:fffe]/
            2608::3:5 | http://[2608::3:5]/
            :: | http://[::]/
            ::ffff:192.168.0.1 | http://[::ffff:c0a8:1]/
            ::1234:F088 | http://[::1234:f088]/
            0:0::0:192.168.0.2 | http://[::c0a8:2]/
            1212:F0F0::3434:D0D0 | http://[1212:f0f0::3434:d0d0]/
            1234:F088:: | http://[1234:f088::]/
            """)
    public void parsesSwiftUrlIpv6AddressCases(String host, String expectedHref) {
        WebURL url = WebURL.parse("http://[" + host + "]/");
        assertEquals(expectedHref, url.href());
    }

    /// Tests invalid IPv6 host inputs.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/IPv6AddressTests.swift#L129-L214
    @ParameterizedTest
    @CsvSource(textBlock = """
            12345::
            FG::
            :
            :::
            F:
            42:
            ::ffff:555.168.0.1
            ::ffff:192.168.0.1.8
            ::ffff:192.168.a.1
            ::ffff:192.168.0.01
            ::ffff:192.168.0xf.1
            ::ffff:192.168.0.1.
            ::ffff:.168.0.1
            0001:0002:0003:0004:0005
            0001:0002:0003:0004:0005:0006:0007:0008:0009
            ::helo
            ::.
            1234k:12k::1234
            1234:12k4::1234
            1234:12k::1234k
            """)
    public void rejectsSwiftUrlInvalidIpv6AddressCases(String host) {
        assertNull(WebURL.tryParse("http://[" + host + "]/"));
    }

    /// Tests host parsing cases involving IDNA, percent decoding, special URLs, and opaque hosts.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/HostTests.swift#L454-L628
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/WebURLTests.swift#L1273-L1601
    @ParameterizedTest
    @CsvSource(delimiter = '|', emptyValue = "", textBlock = """
            http://💩.com/foo | xn--ls8h.com | http://xn--ls8h.com/foo
            http://www.foo。bar.com/foo | www.foo.bar.com | http://www.foo.bar.com/foo
            http://0x𝟕f.1/foo | 127.0.0.1 | http://127.0.0.1/foo
            http://www.foo%E3%80%82bar.com/foo | www.foo.bar.com | http://www.foo.bar.com/foo
            http://%F0%9F%92%A9.com/foo | xn--ls8h.com | http://xn--ls8h.com/foo
            http://0x%F0%9D%9F%95f.1/foo | 127.0.0.1 | http://127.0.0.1/foo
            foo://💩.com/foo | %F0%9F%92%A9.com | foo://%F0%9F%92%A9.com/foo
            foo://www.foo。bar.com/foo | www.foo%E3%80%82bar.com | foo://www.foo%E3%80%82bar.com/foo
            foo://0x𝟕f.1/foo | 0x%F0%9D%9F%95f.1 | foo://0x%F0%9D%9F%95f.1/foo
            file://loCAlhost/foo | '' | file:///foo
            file://loc%61lhost/foo | '' | file:///foo
            http://loc%61lhost/foo | localhost | http://localhost/foo
            """)
    public void parsesSwiftUrlHostCases(String input, String expectedHost, String expectedHref) {
        WebURL url = WebURL.parse(input);
        assertEquals(expectedHost, hostOrEmpty(url));
        assertEquals(expectedHref, url.href());
    }

    /// Tests host parsing cases that Swift URL expects to fail.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/HostTests.swift#L454-L628
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/WebURLTests.swift#L1273-L1601
    @ParameterizedTest
    @CsvSource(textBlock = """
            http://xn--cafe-dma.com/foo
            http://xn--cafe-yvc.com/foo
            http://has a space.com/foo
            http://11.173.240.13.4/foo
            http://[blahblahblah]/foo
            foo://[blahblahblah]/foo
            file://\u00AD/bar
            http://\u00AD/bar
            """)
    public void rejectsSwiftUrlHostCases(String input) {
        assertNull(WebURL.tryParse(input));
    }

    /// Tests selected IDNA cases from Swift URL.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/WebURLTests.swift#L1683-L1734
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/IDNATests/SimpleToUnicodeTests.swift#L49-L94
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            faß.api.你好你好.com | xn--fa-hia.api.xn--6qqa088eba.com
            faß.ExAmPlE | xn--fa-hia.example
            ０Ｘｃ０．０２５０．０１ | 192.168.0.1
            ₓn--fa-hia.example | xn--fa-hia.example
            ☃ | xn--n3h
            你好你好 | xn--6qqa088eba
            a.أهلا.com | a.xn--igbi0gl.com
            a.هذهالكلمة.com | a.xn--mgbet1febhkb.com
            %58%6E-%2Df%61-hi%61 | xn--fa-hia
            %E2%82%93n%2D%2dn3h | xn--n3h
            """)
    public void parsesSwiftUrlIdnaCases(String host, String expectedHost) {
        WebURL url = WebURL.parse("http://" + host + "/");
        assertEquals(expectedHost, url.getHost());
    }

    /// Tests selected IDNA cases from Swift URL that should fail.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/WebURLTests.swift#L1683-L1734
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/IDNATests/SimpleToUnicodeTests.swift#L49-L94
    @ParameterizedTest
    @CsvSource(textBlock = """
            GOO 　goo.com
            xn--cafe-yvc.fr
            a.b.c.xn--pokxncvks
            %78n--
            %58n--
            x%6e--
            """)
    public void rejectsSwiftUrlIdnaCases(String host) {
        assertNull(WebURL.tryParse("http://" + host + "/"));
    }

    /// Tests percent-decoded component behavior for incomplete and invalid percent triplets.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Tests/WebURLTests/PercentEncodingTests.swift#L250-L336
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            /hello%2C%20world! | /hello, world!
            /%2Fusr%2Fbin%2Fswift | //usr/bin/swift
            /%F0%9F%98%8E | /😎
            /%5 | /%5
            /%0%61 | /%0a
            /%0%6172 | /%0a72
            /%0z%61 | /%0za
            /%0z%25%7C1 | '/%0z%|1'
            /%%%%%% | /%%%%%%
            /king+of+the+%F0%9F%A6%86s | /king+of+the+🦆s
            """)
    public void decodesSwiftUrlPercentEncodingCases(String rawPath, String expectedPath) {
        WebURL url = WebURL.parse("https://example.com" + rawPath);
        assertEquals(expectedPath, url.getPath());
    }

    /// Tests additional constructor cases from Swift URL.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/additional_constructor_tests.json#L1-L703
    @ParameterizedTest
    @CsvSource(delimiter = '|', nullValues = "null", textBlock = """
            file:/a/./.. | about:blank | file:///
            file:/a/./././.. | about:blank | file:///
            . | file:///a/b/ | file:///a/b/
            .. | file:///a/b/c | file:///a/
            ... | file:///a/b/... | file:///a/b/...
            ./. | file:///a/b/ | file:///a/b/
            ../.. | file:///a/b/c | file:///
            """)
    public void parsesSwiftUrlAdditionalConstructorCases(String input, @Nullable String base, String expectedHref) {
        WebURL url = base == null ? WebURL.parse(input) : WebURL.parse(input, base);
        assertEquals(expectedHref, url.href());
    }

    /// Tests Swift URL cases that are accepted by the default parser but rejected by the strict parser.
    ///
    /// Source:
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/urltestdata.json#L1-L109
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/urltestdata.json#L1227-L1239
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/urltestdata.json#L1484-L1510
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/urltestdata.json#L2186-L2198
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/urltestdata.json#L2417-L2429
    /// https://github.com/karwa/swift-url/blob/9306a962396a50d7d88e924afcd7ec67226763db/Sources/WebURLTestSupport/TestFilesData/urltestdata.json#L6620-L6687
    @ParameterizedTest
    @MethodSource("swiftUrlStrictRecoverableCases")
    public void rejectsSwiftUrlRecoverableValidationErrorsInStrictMode(
            String input,
            @Nullable String base,
            String expectedHref,
            WebURLParseException.ErrorType expectedErrorType
    ) {
        WebURL defaultUrl = parseDefault(input, base);
        assertEquals(expectedHref, defaultUrl.href());

        WebURLParser strict = WebURLParser.getStrict();
        assertNull(parseStrictNullable(strict, input, base));

        WebURLParseException exception = assertThrows(WebURLParseException.class,
                () -> parseStrict(strict, input, base));
        assertEquals(expectedErrorType, exception.getErrorType());
        assertEquals(input, exception.getInput());
    }

    /// Returns Swift URL cases that exercise strict parser rejection of recoverable validation errors.
    private static Stream<Arguments> swiftUrlStrictRecoverableCases() {
        return Stream.of(
                Arguments.of(
                        "http://example\t.\norg",
                        "http://example.org/foo/bar",
                        "http://example.org/",
                        WebURLParseException.ErrorType.INVALID_URL_UNIT
                ),
                Arguments.of(
                        "https://test:@test",
                        null,
                        "https://test@test/",
                        WebURLParseException.ErrorType.INVALID_CREDENTIALS
                ),
                Arguments.of(
                        "non-special://test:@test/x",
                        null,
                        "non-special://test@test/x",
                        WebURLParseException.ErrorType.INVALID_CREDENTIALS
                ),
                Arguments.of(
                        "http:foo.com",
                        "http://example.org/foo/bar",
                        "http://example.org/foo/foo.com",
                        WebURLParseException.ErrorType.SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS
                ),
                Arguments.of(
                        "https:example.com/",
                        null,
                        "https://example.com/",
                        WebURLParseException.ErrorType.SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS
                ),
                Arguments.of(
                        "file:c:\\foo\\bar.html",
                        null,
                        "file:///c:/foo/bar.html",
                        WebURLParseException.ErrorType.SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS
                ),
                Arguments.of(
                        "http://example.com\\\\foo\\\\bar",
                        null,
                        "http://example.com//foo//bar",
                        WebURLParseException.ErrorType.INVALID_REVERSE_SOLIDUS
                ),
                Arguments.of(
                        "http:\\\\www.google.com\\foo",
                        null,
                        "http://www.google.com/foo",
                        WebURLParseException.ErrorType.SPECIAL_SCHEME_MISSING_FOLLOWING_SOLIDUS
                ),
                Arguments.of(
                        "file://C:/",
                        null,
                        "file:///C:/",
                        WebURLParseException.ErrorType.FILE_INVALID_WINDOWS_DRIVE_LETTER_HOST
                ),
                Arguments.of(
                        "file://C|/",
                        null,
                        "file:///C:/",
                        WebURLParseException.ErrorType.FILE_INVALID_WINDOWS_DRIVE_LETTER_HOST
                )
        );
    }

    /// Parses an input with the default parser.
    private static WebURL parseDefault(String input, @Nullable String base) {
        return base == null ? WebURL.parse(input) : WebURL.parse(input, base);
    }

    /// Parses an input with a strict parser.
    private static WebURL parseStrict(WebURLParser strict, String input, @Nullable String base) {
        return base == null ? strict.parse(input) : strict.parse(input, base);
    }

    /// Parses an input with a strict parser, returning `null` on failure.
    private static @Nullable WebURL parseStrictNullable(WebURLParser strict, String input, @Nullable String base) {
        return base == null ? strict.tryParse(input) : strict.tryParse(input, base);
    }

    /// Returns the host, or the empty string when absent.
    private static String hostOrEmpty(WebURL url) {
        @Nullable String host = url.getHost();
        return host == null ? "" : host;
    }
}
