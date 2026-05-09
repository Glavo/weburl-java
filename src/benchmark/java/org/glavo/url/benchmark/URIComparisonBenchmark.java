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
package org.glavo.url.benchmark;

import org.glavo.url.WebURL;
import org.jetbrains.annotations.NotNullByDefault;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/// JMH benchmarks comparing `WebURL` with JDK `URI`.
///
/// The inputs are valid for both parsers so the parsing benchmark compares successful parses only. Component
/// benchmarks use already parsed objects and measure the steady-state cost of reading the closest public
/// component views exposed by each API.
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
@NotNullByDefault
public class URIComparisonBenchmark {
    /// Default benchmark input used before JMH parameter injection.
    private static final String DEFAULT_INPUT = "https://user:pass@example.com:8443/a/b/c?x=1&y=2#fragment";

    /// Parameterized URL text for parse benchmarks.
    @State(Scope.Benchmark)
    @NotNullByDefault
    public static class URLInput {
        /// URL text parsed by the benchmark method.
        @Param({
                "https://user:pass@example.com:8443/a/b/c?x=1&y=2#fragment",
                "https://xn--bcher-kva.example/%E8%B7%AF%E5%BE%84?q=%E5%80%BC#%E7%89%87",
                "http://[2001:db8::1]:8080/api/v1/items?sort=name#top",
                "file:///C:/Users/Glavo/project/file.txt",
                "data:text/plain,hello%20world"
        })
        public String input = DEFAULT_INPUT;
    }

    /// Parameterized parsed URL state for accessor benchmarks.
    @State(Scope.Thread)
    @NotNullByDefault
    public static class ParsedURL {
        /// URL text parsed during setup.
        @Param({
                "https://user:pass@example.com:8443/a/b/c?x=1&y=2#fragment",
                "https://xn--bcher-kva.example/%E8%B7%AF%E5%BE%84?q=%E5%80%BC#%E7%89%87",
                "http://[2001:db8::1]:8080/api/v1/items?sort=name#top",
                "file:///C:/Users/Glavo/project/file.txt",
                "data:text/plain,hello%20world"
        })
        public String input = DEFAULT_INPUT;

        /// Parsed `WebURL` instance used by accessor benchmarks.
        private WebURL webURL = WebURL.parseURL(DEFAULT_INPUT);

        /// Parsed JDK `URI` instance used by accessor benchmarks.
        private URI javaURI = URI.create(DEFAULT_INPUT);

        /// Parses the current parameter value before benchmark invocation.
        @Setup
        public void parse() throws URISyntaxException {
            webURL = WebURL.parseURL(input);
            javaURI = new URI(input);
        }
    }

    /// Parses a URL with `WebURL`.
    @Benchmark
    public WebURL parseWebURL(URLInput input) {
        return WebURL.parseURL(input.input);
    }

    /// Parses a URL with JDK `URI`.
    @Benchmark
    public URI parseJavaURI(URLInput input) throws URISyntaxException {
        return new URI(input.input);
    }

    /// Serializes an already parsed `WebURL`.
    @Benchmark
    public String serializeWebURL(ParsedURL url) {
        return url.webURL.href();
    }

    /// Serializes an already parsed JDK `URI`.
    @Benchmark
    public String serializeJavaURI(ParsedURL url) {
        return url.javaURI.toString();
    }

    /// Converts an already parsed `WebURL` to Java's RFC 2396 URI string form.
    @Benchmark
    public String serializeWebURLAsRFC2396(ParsedURL url) {
        return url.webURL.toRFC2396String();
    }

    /// Serializes an already parsed JDK `URI` to ASCII form.
    @Benchmark
    public String serializeJavaURIAsASCII(ParsedURL url) {
        return url.javaURI.toASCIIString();
    }

    /// Reads public components from an already parsed `WebURL`.
    @Benchmark
    public void readWebURLComponents(ParsedURL url, Blackhole blackhole) {
        WebURL webURL = url.webURL;
        blackhole.consume(webURL.href());
        blackhole.consume(webURL.origin());
        blackhole.consume(webURL.getScheme());
        blackhole.consume(webURL.getScheme());
        blackhole.consume(webURL.getUsername());
        blackhole.consume(webURL.getUsernameOrEmpty());
        blackhole.consume(webURL.getPassword());
        blackhole.consume(webURL.getPasswordOrEmpty());
        blackhole.consume(webURL.getPort());
        blackhole.consume(webURL.getRawPath());
        blackhole.consume(webURL.getRawPathOrEmpty());
        blackhole.consume(webURL.getRawQuery());
        blackhole.consume(webURL.getRawQueryOrEmpty());
        blackhole.consume(webURL.getRawFragment());
        blackhole.consume(webURL.getRawFragmentOrEmpty());
    }

    /// Reads raw public components from an already parsed JDK `URI`.
    @Benchmark
    public void readJavaURIComponents(ParsedURL url, Blackhole blackhole) {
        URI javaURI = url.javaURI;
        blackhole.consume(javaURI.toString());
        blackhole.consume(javaURI.getScheme());
        blackhole.consume(javaURI.getRawSchemeSpecificPart());
        blackhole.consume(javaURI.getRawAuthority());
        blackhole.consume(javaURI.getRawUserInfo());
        blackhole.consume(javaURI.getHost());
        blackhole.consume(javaURI.getPort());
        blackhole.consume(javaURI.getRawPath());
        blackhole.consume(javaURI.getRawQuery());
        blackhole.consume(javaURI.getRawFragment());
    }
}
