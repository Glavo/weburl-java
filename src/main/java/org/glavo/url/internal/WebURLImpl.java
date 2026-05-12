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
package org.glavo.url.internal;

import org.glavo.url.WebURL;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/// Internal immutable implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
    /// Serialization identifier for this implementation type.
    @Serial
    private static final long serialVersionUID = 1L;

    /// URL scheme without the trailing colon.
    private final String scheme;
    /// URL host, or `null` when absent.
    private final @Nullable UrlHost urlHost;
    /// URL port, or `-1` when absent or defaulted.
    private final int port;
    /// Immutable non-opaque path segments.
    private final @Unmodifiable List<String> pathSegments;
    /// Opaque path, or `null` when the URL has a path segment list.
    private final @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    private final @Nullable String rawQueryValue;
    /// Percent-encoded fragment, or `null` when absent.
    private final @Nullable String rawFragmentValue;
    /// Serialized WHATWG URL.
    private final String href;
    /// Index of the colon after the scheme.
    private final int schemeEnd;
    /// Range of the username in `href`, or `IndexRanges.ABSENT` when credentials are absent.
    private final @IndexRange("href") long usernameRange;
    /// Range of the password in `href`, or `IndexRanges.ABSENT` when absent.
    private final @IndexRange("href") long passwordRange;
    /// Range of the host in `href`, or `IndexRanges.ABSENT` when absent.
    private final @IndexRange("href") long hostRange;
    /// Range of the port in `href`, or `IndexRanges.ABSENT` when absent.
    private final @IndexRange("href") long portRange;
    /// Range of the logical path in `href`.
    private final @IndexRange("href") long pathRange;
    /// Range of the query in `href`, or `IndexRanges.ABSENT` when absent.
    private final @IndexRange("href") long queryRange;
    /// Range of the fragment in `href`, or `IndexRanges.ABSENT` when absent.
    private final @IndexRange("href") long fragmentRange;
    /// Whether href contains the extra `/.` prefix before the logical path.
    private final boolean pathPrefix;
    /// Cached origin string, or `null` until requested.
    private @Nullable String origin;
    /// Cached RFC 2396 URI string, or `null` until requested.
    private @Nullable String rfc2396String;
    /// Cached display string, or `null` until requested.
    private @Nullable String displayString;
    /// Cached Java URI object, or `null` until requested.
    private @Nullable URI uri;

    /// Creates an immutable URL from a completed URL record and serialized URL indexes.
    WebURLImpl(
            UrlRecord record,
            String href,
            int schemeEnd,
            @IndexRange("href") long usernameRange,
            @IndexRange("href") long passwordRange,
            @IndexRange("href") long hostRange,
            @IndexRange("href") long portRange,
            @IndexRange("href") long pathRange,
            @IndexRange("href") long queryRange,
            @IndexRange("href") long fragmentRange,
            boolean pathPrefix
    ) {
        this.scheme = record.scheme;
        this.urlHost = record.host;
        this.port = record.port;
        this.pathSegments = List.copyOf(record.path);
        this.opaquePath = record.opaquePath;
        this.rawQueryValue = record.query;
        this.rawFragmentValue = record.fragment;
        this.href = href;
        this.schemeEnd = schemeEnd;
        this.usernameRange = usernameRange;
        this.passwordRange = passwordRange;
        this.hostRange = hostRange;
        this.portRange = portRange;
        this.pathRange = pathRange;
        this.queryRange = queryRange;
        this.fragmentRange = fragmentRange;
        this.pathPrefix = pathPrefix;
    }

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return opaquePath != null;
    }

    /// Returns whether this URL has a host.
    boolean hasHost() {
        return urlHost != null;
    }

    /// Returns the serialized host plus port when a port is present.
    private String hostAndPort() {
        String href = href();
        if (IndexRanges.isAbsent(hostRange)) {
            return "";
        }
        return IndexRanges.isAbsent(portRange)
                ? IndexRanges.substring(href, hostRange)
                : href.substring(IndexRanges.start(hostRange), IndexRanges.end(portRange));
    }

    /// Returns the serialized URL without its fragment.
    String hrefWithoutFragment() {
        String href = href();
        return IndexRanges.isAbsent(fragmentRange) ? href : href.substring(0, IndexRanges.start(fragmentRange) - 1);
    }

    /// Returns whether the scheme equals the supplied lower-case ASCII value.
    boolean schemeEquals(String value) {
        return scheme.equals(value);
    }

    /// Returns the port value, or `-1` when absent.
    int portValue() {
        return port;
    }

    /// Returns the host value for parser state.
    @Nullable UrlHost hostValue() {
        return urlHost;
    }

    /// Returns a mutable copy of the non-opaque path segments.
    List<String> pathSegments() {
        return new ArrayList<>(pathSegments);
    }

    /// Returns the first path segment, or `null` when absent.
    @Nullable String firstPathSegment() {
        return opaquePath != null || pathSegments.isEmpty() ? null : pathSegments.get(0);
    }

    /// Returns the opaque path value, or `null` for a non-opaque path.
    @Nullable String opaquePathValue() {
        return opaquePath;
    }

    /// Returns the query value, or `null` when absent.
    @Nullable String queryValue() {
        return rawQueryValue;
    }

    /// Returns a mutable copy of this URL's logical record.
    UrlRecord toRecord() {
        UrlRecord record = new UrlRecord();
        record.scheme = scheme;
        record.username = getRawUsernameOrEmpty();
        record.password = getRawPasswordOrEmpty();
        record.host = urlHost;
        record.port = port;
        record.path = new ArrayList<>(pathSegments);
        record.opaquePath = opaquePath;
        record.query = rawQueryValue;
        record.fragment = rawFragmentValue;
        return record;
    }

    /// Returns the serialized URL.
    @Override
    public String href() {
        return href;
    }

    /// Returns a human-readable display string.
    @Override
    public String toDisplayString() {
        @Nullable String cached = displayString;
        if (cached != null) {
            return cached;
        }

        String href = href();
        @Nullable String displayUsername = displayDecode(href, usernameRange);
        @Nullable String displayPassword = displayDecode(href, passwordRange);
        @Nullable String displayHost = urlHost == null ? null : urlHost.displayString();
        @Nullable String displayPath = displayDecode(href, pathRange);
        @Nullable String displayQuery = displayDecode(href, queryRange);
        @Nullable String displayFragment = displayDecode(href, fragmentRange);

        if (displayUsername == null
                && displayPassword == null
                && displayHost == null
                && displayPath == null
                && displayQuery == null
                && displayFragment == null) {
            displayString = href;
            return href;
        }

        StringBuilder output = new StringBuilder(href.length());
        output.append(href, 0, schemeEnd + 1);

        if (hasOpaquePath()) {
            appendDisplayComponent(output, href, pathRange, displayPath);
        } else {
            if (hasHost()) {
                output.append("//");
                if (IndexRanges.isPresent(usernameRange)) {
                    appendDisplayComponent(output, href, usernameRange, displayUsername);
                    if (IndexRanges.isPresent(passwordRange)) {
                        output.append(':');
                        appendDisplayComponent(output, href, passwordRange, displayPassword);
                    }
                    output.append('@');
                }
                appendDisplayComponent(output, href, hostRange, displayHost);
                if (IndexRanges.isPresent(portRange)) {
                    output.append(':').append(href, IndexRanges.start(portRange), IndexRanges.end(portRange));
                }
            } else if (pathPrefix) {
                output.append("/.");
            }
            appendDisplayComponent(output, href, pathRange, displayPath);
        }

        if (IndexRanges.isPresent(queryRange)) {
            output.append('?');
            appendDisplayComponent(output, href, queryRange, displayQuery);
        }
        if (IndexRanges.isPresent(fragmentRange)) {
            output.append('#');
            appendDisplayComponent(output, href, fragmentRange, displayFragment);
        }

        String value = output.toString();
        displayString = value;
        return value;
    }

    /// Returns the serialized origin.
    @Override
    public String origin() {
        @Nullable String cached = origin;
        if (cached != null) {
            return cached;
        }

        String value;
        switch (scheme) {
            case "blob":
                @Nullable WebURLImpl pathUrl = UrlParser.parseUrl(getRawPath());
                if (pathUrl == null || (!pathUrl.schemeEquals("http") && !pathUrl.schemeEquals("https"))) {
                    value = "null";
                } else {
                    value = pathUrl.origin();
                }
                break;
            case "ftp":
            case "http":
            case "https":
            case "ws":
            case "wss":
                if (!hasHost()) {
                    value = "null";
                } else {
                    value = scheme + "://" + hostAndPort();
                }
                break;
            case "file":
            default:
                value = "null";
                break;
        }
        origin = value;
        return value;
    }

    /// Returns the scheme.
    @Override
    public String getScheme() {
        return scheme;
    }

    /// Returns the decoded username, or `null` when absent.
    @Override
    public @Nullable String getUsername() {
        @Nullable String rawValue = getRawUsername();
        return rawValue == null ? null : PercentEncoding.percentDecodeUtf8(rawValue);
    }

    /// Returns the raw username, or the empty string when absent.
    @Override
    public String getRawUsernameOrEmpty() {
        return IndexRanges.isAbsent(usernameRange) ? "" : IndexRanges.substring(href(), usernameRange);
    }

    /// Returns the raw username, or `null` when absent.
    @Override
    public @Nullable String getRawUsername() {
        return IndexRanges.isAbsent(usernameRange) ? null : getRawUsernameOrEmpty();
    }

    /// Returns the decoded password, or `null` when absent.
    @Override
    public @Nullable String getPassword() {
        @Nullable String rawValue = getRawPassword();
        return rawValue == null ? null : PercentEncoding.percentDecodeUtf8(rawValue);
    }

    /// Returns the raw password, or the empty string when absent.
    @Override
    public String getRawPasswordOrEmpty() {
        return IndexRanges.isAbsent(passwordRange) ? "" : IndexRanges.substring(href(), passwordRange);
    }

    /// Returns the raw password, or `null` when absent.
    @Override
    public @Nullable String getRawPassword() {
        return IndexRanges.isAbsent(passwordRange) ? null : getRawPasswordOrEmpty();
    }

    /// Returns the decoded user-info, or `null` when absent.
    @Override
    public @Nullable String getUserInfo() {
        @Nullable String rawValue = getRawUserInfo();
        return rawValue == null ? null : PercentEncoding.percentDecodeUtf8(rawValue);
    }

    /// Returns the raw user-info, or `null` when absent.
    @Override
    public @Nullable String getRawUserInfo() {
        if (IndexRanges.isAbsent(usernameRange)) {
            return null;
        }

        return href().substring(IndexRanges.start(usernameRange), IndexRanges.start(hostRange) - 1);
    }

    /// Returns the decoded authority, or `null` when absent.
    @Override
    public @Nullable String getAuthority() {
        @Nullable String rawValue = getRawAuthority();
        return rawValue == null ? null : PercentEncoding.percentDecodeUtf8(rawValue);
    }

    /// Returns the raw authority, or `null` when absent.
    @Override
    public @Nullable String getRawAuthority() {
        if (IndexRanges.isAbsent(hostRange)) {
            return null;
        }

        return href().substring(schemeEnd + 3,
                IndexRanges.isAbsent(portRange) ? IndexRanges.end(hostRange) : IndexRanges.end(portRange));
    }

    /// Returns the host, or `null` when absent.
    @Override
    public @Nullable String getHost() {
        if (IndexRanges.isAbsent(hostRange)) {
            return null;
        }

        return IndexRanges.substring(href(), hostRange);
    }

    /// Returns the effective port value, or `-1` when absent and no default is known.
    @Override
    public int getPort() {
        return port >= 0 ? port : UrlParser.defaultPort(scheme);
    }

    /// Returns the raw port, or `null` when absent.
    @Override
    public @Nullable String getRawPort() {
        if (IndexRanges.isAbsent(portRange)) {
            return null;
        }

        return IndexRanges.substring(href(), portRange);
    }

    /// Returns the decoded path.
    @Override
    public String getPath() {
        return PercentEncoding.percentDecodeUtf8(getRawPath());
    }

    /// Returns the raw path.
    @Override
    public String getRawPath() {
        return IndexRanges.substring(href(), pathRange);
    }

    /// Returns the decoded query, or `null` when absent.
    @Override
    public @Nullable String getQuery() {
        if (rawQueryValue == null) {
            return null;
        }

        return PercentEncoding.percentDecodeUtf8(rawQueryValue);
    }

    /// Returns the raw query, or `null` when absent.
    @Override
    public @Nullable String getRawQuery() {
        return rawQueryValue;
    }

    /// Returns the raw query, or the empty string when absent.
    @Override
    public String getRawQueryOrEmpty() {
        @Nullable String value = rawQueryValue;
        return value == null ? "" : value;
    }

    /// Returns the decoded fragment, or `null` when absent.
    @Override
    public @Nullable String getFragment() {
        if (rawFragmentValue == null) {
            return null;
        }

        return PercentEncoding.percentDecodeUtf8(rawFragmentValue);
    }

    /// Returns the raw fragment, or `null` when absent.
    @Override
    public @Nullable String getRawFragment() {
        return rawFragmentValue;
    }

    /// Returns the raw fragment, or the empty string when absent.
    @Override
    public String getRawFragmentOrEmpty() {
        @Nullable String value = rawFragmentValue;
        return value == null ? "" : value;
    }

    /// Returns the serialized URL as a Java `URI`.
    @Override
    public URI toURI() throws URISyntaxException {
        @Nullable URI cached = uri;
        if (cached != null) {
            return cached;
        }

        URI value = new URI(toRFC2396String());
        uri = value;
        return value;
    }

    /// Returns the serialized URL as a Java `URL`.
    @Override
    public URL toURL() throws MalformedURLException {
        try {
            return toURI().toURL();
        } catch (URISyntaxException exception) {
            MalformedURLException malformed = new MalformedURLException(exception.getMessage());
            malformed.initCause(exception);
            throw malformed;
        }
    }

    /// Compares this URL with another URL by serialized URL string.
    @Override
    public int compareTo(WebURL other) {
        return href().compareTo(other.href());
    }

    /// Compares this URL with another object for serialized URL equality.
    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof WebURL other && href().equals(other.href());
    }

    /// Returns the serialized URL hash code.
    @Override
    public int hashCode() {
        return href().hashCode();
    }

    /// Returns the serialized URL.
    @Override
    public String toString() {
        return href();
    }

    /// Returns the serialization proxy for this URL.
    @Serial
    private Object writeReplace() {
        return new WebURLImpl.SerializationProxy(href());
    }

    /// Rejects direct deserialization because the serialized form is defined by `SerializationProxy`.
    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    /// Rejects direct deserialization because the serialized form is defined by `SerializationProxy`.
    @Serial
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    /// Serialization proxy that stores only the canonical WHATWG URL serialization.
    @SuppressWarnings("ClassCanBeRecord")
    @NotNullByDefault
    private static final class SerializationProxy implements Serializable {
        /// Serialization identifier for the proxy type.
        @Serial
        private static final long serialVersionUID = 1L;

        /// Canonical WHATWG URL serialization.
        private final String href;

        /// Creates a serialization proxy with the canonical WHATWG URL serialization.
        private SerializationProxy(String href) {
            this.href = href;
        }

        /// Recreates a `WebURL` from the canonical WHATWG URL serialization.
        @Serial
        private Object readResolve() throws ObjectStreamException {
            if (href == null) {
                throw new InvalidObjectException("href cannot be null");
            }

            try {
                return WebURL.parse(href);
            } catch (RuntimeException exception) {
                InvalidObjectException invalidObject =
                        new InvalidObjectException("href is not a valid serialized URL");
                invalidObject.initCause(exception);
                throw invalidObject;
            }
        }
    }

    /// Appends either a decoded display component or the original serialized component.
    private static void appendDisplayComponent(
            StringBuilder output,
            String value,
            @IndexRange("value") long range,
            @Nullable String displayValue
    ) {
        if (displayValue == null) {
            output.append(value, IndexRanges.start(range), IndexRanges.end(range));
        } else {
            output.append(displayValue);
        }
    }

    /// Decodes percent-encoded non-ASCII UTF-8 sequences for display, or returns `null` if unchanged.
    private static @Nullable String displayDecode(String value, @IndexRange("value") long range) {
        if (IndexRanges.isAbsent(range)) {
            return null;
        }

        int start = IndexRanges.start(range);
        int end = IndexRanges.end(range);
        @Nullable StringBuilder output = null;
        for (int index = start; index < end; ) {
            char c = value.charAt(index);
            int next = index + 1;
            if (PercentEncoding.isValidPercentTriplet(value, index, end)) {
                int decoded = displayDecodeUtf8CodePoint(value, index, end);
                if (decoded >= 0) {
                    int decodedLength = displayUtf8Length(PercentEncoding.percentEncodedByte(value, index));
                    int decodedEnd = index + decodedLength * 3;
                    if (output == null) {
                        output = new StringBuilder(end - start);
                        output.append(value, start, index);
                    }
                    output.appendCodePoint(decoded);
                    index = decodedEnd;
                    continue;
                }
                next = index + 3;
            }

            if (output != null) {
                output.append(value, index, next);
            }
            index = next;
        }
        return output == null ? null : output.toString();
    }

    /// Decodes one display-safe UTF-8 code point from percent escapes, or returns `-1`.
    private static int displayDecodeUtf8CodePoint(String value, int start, int end) {
        int b0 = PercentEncoding.percentEncodedByte(value, start);
        int codePoint;
        int length;
        if (b0 < 0x80) {
            return -1;
        } else if (b0 >= 0xc2 && b0 <= 0xdf) {
            length = 2;
            int b1 = continuationByte(value, start + 3, end);
            if (b1 < 0) {
                return -1;
            }
            codePoint = ((b0 & 0x1f) << 6) | (b1 & 0x3f);
        } else if (b0 >= 0xe0 && b0 <= 0xef) {
            length = 3;
            int b1 = continuationByte(value, start + 3, end);
            int b2 = continuationByte(value, start + 6, end);
            if (b1 < 0 || b2 < 0 || (b0 == 0xe0 && b1 < 0xa0) || (b0 == 0xed && b1 >= 0xa0)) {
                return -1;
            }
            codePoint = ((b0 & 0x0f) << 12) | ((b1 & 0x3f) << 6) | (b2 & 0x3f);
        } else if (b0 >= 0xf0 && b0 <= 0xf4) {
            length = 4;
            int b1 = continuationByte(value, start + 3, end);
            int b2 = continuationByte(value, start + 6, end);
            int b3 = continuationByte(value, start + 9, end);
            if (b1 < 0 || b2 < 0 || b3 < 0 || (b0 == 0xf0 && b1 < 0x90) || (b0 == 0xf4 && b1 >= 0x90)) {
                return -1;
            }
            codePoint = ((b0 & 0x07) << 18) | ((b1 & 0x3f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f);
        } else {
            return -1;
        }

        return start + length * 3 <= end && isDisplayCodePoint(codePoint) ? codePoint : -1;
    }

    /// Returns the UTF-8 sequence length implied by the first escaped byte.
    private static int displayUtf8Length(int firstByte) {
        if (firstByte < 0x80) {
            return 1;
        }
        if (firstByte < 0xe0) {
            return 2;
        }
        return firstByte < 0xf0 ? 3 : 4;
    }

    /// Returns a continuation byte from a percent escape, or `-1`.
    private static int continuationByte(String value, int start, int end) {
        if (!PercentEncoding.isValidPercentTriplet(value, start, end)) {
            return -1;
        }
        int b = PercentEncoding.percentEncodedByte(value, start);
        return b >= 0x80 && b <= 0xbf ? b : -1;
    }

    /// Returns whether a code point may be decoded for display.
    private static boolean isDisplayCodePoint(int codePoint) {
        int type = Character.getType(codePoint);
        return codePoint >= 0x80
                && !Character.isISOControl(codePoint)
                && type != Character.CONTROL
                && type != Character.FORMAT
                && type != Character.PRIVATE_USE
                && type != Character.SURROGATE
                && type != Character.UNASSIGNED;
    }

    /// Returns the serialized URL converted to Java's RFC 2396 URI syntax.
    @Override
    public String toRFC2396String() {
        @Nullable String cached = rfc2396String;
        if (cached != null) {
            return cached;
        }

        String href = href();
        if (isRfc2396String(href)) {
            rfc2396String = href;
            return href;
        }

        StringBuilder output = new StringBuilder(href.length());
        output.append(href, 0, schemeEnd + 1);

        if (hasOpaquePath()) {
            appendRfc2396Encoded(output, href, pathRange, WebURLImpl::isRfc2396Uric);
        } else {
            if (hasHost()) {
                output.append("//");
                if (IndexRanges.isPresent(usernameRange)) {
                    appendRfc2396Encoded(output, href, usernameRange, WebURLImpl::isRfc2396UserInfo);
                    if (IndexRanges.isPresent(passwordRange)) {
                        output.append(':');
                        appendRfc2396Encoded(output, href, passwordRange, WebURLImpl::isRfc2396UserInfo);
                    }
                    output.append('@');
                }
                appendRfc2396Encoded(output, href, hostRange, WebURLImpl::isRfc2396Host);
                if (IndexRanges.isPresent(portRange)) {
                    output.append(':').append(href, IndexRanges.start(portRange), IndexRanges.end(portRange));
                }
            } else if (pathPrefix) {
                output.append("/.");
            }
            appendRfc2396Encoded(output, href, pathRange, WebURLImpl::isRfc2396Path);
        }

        if (IndexRanges.isPresent(queryRange)) {
            output.append('?');
            appendRfc2396Encoded(output, href, queryRange, WebURLImpl::isRfc2396Uric);
        }
        if (IndexRanges.isPresent(fragmentRange)) {
            output.append('#');
            appendRfc2396Encoded(output, href, fragmentRange, WebURLImpl::isRfc2396Uric);
        }
        String value = output.toString();
        rfc2396String = value;
        return value;
    }

    /// Returns whether the serialized URL can be used directly as an RFC 2396 URI string.
    private boolean isRfc2396String(String value) {
        if (hasOpaquePath()) {
            if (!isRfc2396Encoded(value, pathRange, WebURLImpl::isRfc2396Uric)) {
                return false;
            }
        } else {
            if (hasHost()) {
                if (IndexRanges.isPresent(usernameRange)) {
                    if (!isRfc2396Encoded(value, usernameRange, WebURLImpl::isRfc2396UserInfo)) {
                        return false;
                    }
                    if (IndexRanges.isPresent(passwordRange)
                            && !isRfc2396Encoded(value, passwordRange, WebURLImpl::isRfc2396UserInfo)) {
                        return false;
                    }
                }
                if (!isRfc2396Encoded(value, hostRange, WebURLImpl::isRfc2396Host)) {
                    return false;
                }
            }
            if (!isRfc2396Encoded(value, pathRange, WebURLImpl::isRfc2396Path)) {
                return false;
            }
        }

        if (IndexRanges.isPresent(queryRange)
                && !isRfc2396Encoded(value, queryRange, WebURLImpl::isRfc2396Uric)) {
            return false;
        }
        return IndexRanges.isAbsent(fragmentRange)
                || isRfc2396Encoded(value, fragmentRange, WebURLImpl::isRfc2396Uric);
    }

    /// Returns whether a component is already encoded for Java's RFC 2396 URI parser.
    private static boolean isRfc2396Encoded(
            String value,
            @IndexRange("value") long range,
            Rfc2396CharPredicate allowed
    ) {
        int start = IndexRanges.start(range);
        int end = IndexRanges.end(range);
        for (int index = start; index < end; ) {
            int c = value.codePointAt(index);
            if (PercentEncoding.isValidPercentTriplet(value, index, end)) {
                index += 3;
                continue;
            }
            if (c > 0x7f || !allowed.test(c)) {
                return false;
            }
            index += Character.charCount(c);
        }
        return true;
    }

    /// Appends a component encoded for Java's RFC 2396 URI parser.
    private static void appendRfc2396Encoded(
            StringBuilder output,
            String value,
            @IndexRange("value") long range,
            Rfc2396CharPredicate allowed
    ) {
        int start = IndexRanges.start(range);
        int end = IndexRanges.end(range);
        for (int index = start; index < end; ) {
            int c = value.codePointAt(index);
            if (PercentEncoding.isValidPercentTriplet(value, index, end)) {
                output.append('%').append(value.charAt(index + 1)).append(value.charAt(index + 2));
                index += 3;
                continue;
            }

            if (c <= 0x7f && allowed.test(c)) {
                output.append((char) c);
            } else {
                PercentEncoding.appendUtf8PercentEncodedCodePoint(output, c);
            }
            index += Character.charCount(c);
        }
    }

    /// Returns whether a character is an RFC 2396 path character.
    private static boolean isRfc2396Path(int c) {
        return isRfc2396Unreserved(c) || c == '/' || c == ';' || c == ':' || c == '@'
                || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is an RFC 2396 URI character.
    private static boolean isRfc2396Uric(int c) {
        return isRfc2396Unreserved(c) || c == ';' || c == '/' || c == '?' || c == ':'
                || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is allowed in an RFC 2396 user-info component.
    private static boolean isRfc2396UserInfo(int c) {
        return isRfc2396Unreserved(c) || c == ';' || c == ':' || c == '&' || c == '='
                || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is allowed in an RFC 2396 host component.
    private static boolean isRfc2396Host(int c) {
        return isRfc2396Unreserved(c) || c == '[' || c == ']' || c == ':' || c == ';'
                || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    /// Returns whether a character is unreserved under RFC 2396.
    private static boolean isRfc2396Unreserved(int c) {
        return StringUtils.isAsciiAlpha(c) || StringUtils.isAsciiDigit(c) || c == '-' || c == '_'
                || c == '.' || c == '!' || c == '~' || c == '*' || c == '\'' || c == '(' || c == ')';
    }

    /// Predicate over RFC 2396 ASCII characters.
    @FunctionalInterface
    @NotNullByDefault
    private interface Rfc2396CharPredicate {
        /// Returns whether the character may appear without escaping.
        boolean test(int c);
    }

}
