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
import org.glavo.url.WebURLSearchParams;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/// Internal immutable implementation of `WebURL`.
@NotNullByDefault
public final class WebURLImpl implements WebURL {
    /// The serialized URL.
    final String href;
    /// Index of the colon after the scheme.
    final int schemeEnd;
    /// Start index of the username, or `-1` when credentials are absent.
    private final int usernameStart;
    /// End index of the username, or `-1` when credentials are absent.
    private final int usernameEnd;
    /// Start index of the password, or `-1` when absent.
    private final int passwordStart;
    /// End index of the password, or `-1` when absent.
    private final int passwordEnd;
    /// Start index of the host, or `-1` when absent.
    private final int hostStart;
    /// End index of the host, or `-1` when absent.
    private final int hostEnd;
    /// Start index of the port, or `-1` when absent.
    private final int portStart;
    /// End index of the port, or `-1` when absent.
    private final int portEnd;
    /// Start index of the logical path.
    private final int pathStart;
    /// End index of the logical path.
    private final int pathEnd;
    /// Start index of the query, or `-1` when absent.
    private final int queryStart;
    /// End index of the query, or `-1` when absent.
    private final int queryEnd;
    /// Start index of the fragment, or `-1` when absent.
    private final int fragmentStart;
    /// Whether the path is opaque.
    private final boolean opaquePath;
    /// Whether href contains the extra `/.` prefix before the logical path.
    private final boolean pathPrefix;
    /// Cached immutable query parameter object, or `null` until requested.
    private volatile @Nullable WebURLSearchParams searchParams;

    /// Creates an immutable URL from parsed components.
    WebURLImpl(
            String scheme,
            String username,
            String password,
            @Nullable UrlHost host,
            int port,
            List<String> path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        StringBuilder output = new StringBuilder();
        output.append(scheme);
        int schemeEndValue = output.length();
        output.append(':');

        int usernameStartValue = -1;
        int usernameEndValue = -1;
        int passwordStartValue = -1;
        int passwordEndValue = -1;
        int hostStartValue = -1;
        int hostEndValue = -1;
        int portStartValue = -1;
        int portEndValue = -1;

        if (host != null) {
            output.append("//");
            if (!username.isEmpty() || !password.isEmpty()) {
                usernameStartValue = output.length();
                output.append(username);
                usernameEndValue = output.length();
                if (!password.isEmpty()) {
                    output.append(':');
                    passwordStartValue = output.length();
                    output.append(password);
                    passwordEndValue = output.length();
                }
                output.append('@');
            }

            hostStartValue = output.length();
            output.append(UrlParser.serializeHost(host));
            hostEndValue = output.length();
            if (port != -1) {
                output.append(':');
                portStartValue = output.length();
                output.append(port);
                portEndValue = output.length();
            }
        }

        boolean opaquePathValue = opaquePath != null;
        boolean pathPrefixValue = false;
        int pathStartValue = output.length();
        if (opaquePathValue) {
            output.append(opaquePath);
        } else {
            if (host == null && path.size() > 1 && path.get(0).isEmpty()) {
                output.append("/.");
                pathPrefixValue = true;
                pathStartValue = output.length();
            }
            for (String segment : path) {
                output.append('/').append(segment);
            }
        }
        int pathEndValue = output.length();

        int queryStartValue = -1;
        int queryEndValue = -1;
        if (query != null) {
            output.append('?');
            queryStartValue = output.length();
            output.append(query);
            queryEndValue = output.length();
        }

        int fragmentStartValue = -1;
        if (fragment != null) {
            output.append('#');
            fragmentStartValue = output.length();
            output.append(fragment);
        }

        this.href = output.toString();
        this.schemeEnd = schemeEndValue;
        this.usernameStart = usernameStartValue;
        this.usernameEnd = usernameEndValue;
        this.passwordStart = passwordStartValue;
        this.passwordEnd = passwordEndValue;
        this.hostStart = hostStartValue;
        this.hostEnd = hostEndValue;
        this.portStart = portStartValue;
        this.portEnd = portEndValue;
        this.pathStart = pathStartValue;
        this.pathEnd = pathEndValue;
        this.queryStart = queryStartValue;
        this.queryEnd = queryEndValue;
        this.fragmentStart = fragmentStartValue;
        this.opaquePath = opaquePathValue;
        this.pathPrefix = pathPrefixValue;
    }

    /// Returns whether this URL has an opaque path.
    boolean hasOpaquePath() {
        return opaquePath;
    }

    /// Returns whether this URL has a host.
    boolean hasHost() {
        return hostStart >= 0;
    }

    /// Returns whether this URL has a host that serializes to an empty string.
    boolean hasEmptyHost() {
        return hostStart >= 0 && hostStart == hostEnd;
    }

    /// Returns the serialized URL without its fragment.
    String hrefWithoutFragment() {
        return fragmentStart < 0 ? href : href.substring(0, fragmentStart - 1);
    }

    /// Returns whether the scheme equals the supplied lower-case ASCII value.
    boolean schemeEquals(String value) {
        return schemeEnd == value.length() && href.regionMatches(0, value, 0, schemeEnd);
    }

    /// Returns the port value, or `-1` when absent.
    int portValue() {
        return portStart < 0 ? -1 : Integer.parseInt(href.substring(portStart, portEnd));
    }

    /// Returns the host reconstructed for parser state.
    @Nullable UrlHost hostValue() {
        if (hostStart < 0) {
            return null;
        }
        return UrlParser.parseSerializedHost(hostname(), !UrlParser.isSpecialScheme(scheme()));
    }

    /// Returns a mutable copy of the non-opaque path segments.
    List<String> pathSegments() {
        ArrayList<String> result = new ArrayList<>();
        if (opaquePath || pathStart == pathEnd) {
            return result;
        }

        int index = pathStart;
        while (index < pathEnd) {
            int next = href.indexOf('/', index + 1);
            if (next < 0 || next > pathEnd) {
                result.add(href.substring(index + 1, pathEnd));
                break;
            }
            result.add(href.substring(index + 1, next));
            index = next;
        }
        return result;
    }

    /// Returns the first path segment, or `null` when absent.
    @Nullable String firstPathSegment() {
        if (opaquePath || pathStart == pathEnd) {
            return null;
        }
        int next = href.indexOf('/', pathStart + 1);
        int end = next < 0 || next > pathEnd ? pathEnd : next;
        return href.substring(pathStart + 1, end);
    }

    /// Returns the opaque path value, or `null` for a non-opaque path.
    @Nullable String opaquePathValue() {
        return opaquePath ? pathname() : null;
    }

    /// Returns the query value, or `null` when absent.
    @Nullable String queryValue() {
        return queryStart < 0 ? null : href.substring(queryStart, queryEnd);
    }

    /// Returns the fragment value, or `null` when absent.
    @Nullable String fragmentValue() {
        return fragmentStart < 0 ? null : href.substring(fragmentStart);
    }

    /// Returns the serialized URL.
    @Override
    public String href() {
        return href;
    }

    /// Returns the serialized origin.
    @Override
    public String origin() {
        switch (scheme()) {
            case "blob":
                WebURLImpl pathUrl = UrlParser.parseUrl(pathname());
                if (pathUrl == null || (!pathUrl.schemeEquals("http") && !pathUrl.schemeEquals("https"))) {
                    return "null";
                }
                return pathUrl.origin();
            case "ftp":
            case "http":
            case "https":
            case "ws":
            case "wss":
                if (!hasHost()) {
                    return "null";
                }
                return scheme() + "://" + host();
            case "file":
            default:
                return "null";
        }
    }

    /// Returns the scheme.
    @Override
    public String scheme() {
        return href.substring(0, schemeEnd);
    }

    /// Returns a URL with the scheme updated when the URL Standard permits the change.
    @Override
    public WebURL withScheme(String value) {
        return withSchemeOverride(value);
    }

    /// Returns the protocol, including the trailing colon.
    @Override
    public String protocol() {
        return href.substring(0, schemeEnd + 1);
    }

    /// Returns a URL with the protocol updated when the URL Standard permits the change.
    @Override
    public WebURL withProtocol(String value) {
        return withSchemeOverride(value);
    }

    /// Returns the username.
    @Override
    public String username() {
        return usernameStart < 0 ? "" : href.substring(usernameStart, usernameEnd);
    }

    /// Returns a URL with the username updated when the URL can have credentials.
    @Override
    public WebURL withUsername(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        return copy(UrlParser.percentEncodeUserInfo(value), password(), hostValue(), portValue(),
                pathSegments(), opaquePathValue(), queryValue(), fragmentValue());
    }

    /// Returns the password.
    @Override
    public String password() {
        return passwordStart < 0 ? "" : href.substring(passwordStart, passwordEnd);
    }

    /// Returns a URL with the password updated when the URL can have credentials.
    @Override
    public WebURL withPassword(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        return copy(username(), UrlParser.percentEncodeUserInfo(value), hostValue(), portValue(),
                pathSegments(), opaquePathValue(), queryValue(), fragmentValue());
    }

    /// Returns the host, including the port when present.
    @Override
    public String host() {
        if (hostStart < 0) {
            return "";
        }
        return portStart < 0 ? href.substring(hostStart, hostEnd) : href.substring(hostStart, portEnd);
    }

    /// Returns a URL with the host updated when the URL has a non-opaque path.
    @Override
    public WebURL withHost(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        return withStateOverride(value, UrlParser.State.HOST);
    }

    /// Returns the hostname.
    @Override
    public String hostname() {
        return hostStart < 0 ? "" : href.substring(hostStart, hostEnd);
    }

    /// Returns a URL with the hostname updated when the URL has a non-opaque path.
    @Override
    public WebURL withHostname(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        return withStateOverride(value, UrlParser.State.HOSTNAME);
    }

    /// Returns the port as a string.
    @Override
    public String port() {
        return portStart < 0 ? "" : href.substring(portStart, portEnd);
    }

    /// Returns a URL with the port updated when the URL can have a port.
    @Override
    public WebURL withPort(String value) {
        if (UrlParser.cannotHaveAUsernamePasswordPort(this)) {
            return this;
        }
        if (value.isEmpty()) {
            return copy(username(), password(), hostValue(), -1, pathSegments(),
                    opaquePathValue(), queryValue(), fragmentValue());
        }
        return withStateOverride(value, UrlParser.State.PORT);
    }

    /// Returns the serialized pathname.
    @Override
    public String pathname() {
        return href.substring(pathStart, pathEnd);
    }

    /// Returns a URL with the pathname updated when the URL has a non-opaque path.
    @Override
    public WebURL withPathname(String value) {
        if (hasOpaquePath()) {
            return this;
        }
        WebURLImpl copy = copy(username(), password(), hostValue(), portValue(),
                List.of(), null, queryValue(), fragmentValue());
        return parseIntoCopyOrThis(value, copy, UrlParser.State.PATH_START);
    }

    /// Returns the search string, including the leading question mark when non-empty.
    @Override
    public String search() {
        return queryStart < 0 || queryStart == queryEnd ? "" : href.substring(queryStart - 1, queryEnd);
    }

    /// Returns a URL with the search string updated.
    @Override
    public WebURL withSearch(String value) {
        if (value.isEmpty()) {
            return copy(username(), password(), hostValue(), portValue(),
                    pathSegments(), opaquePathValue(), null, fragmentValue());
        }

        String input = value.charAt(0) == '?' ? value.substring(1) : value;
        WebURLImpl copy = copy(username(), password(), hostValue(), portValue(),
                pathSegments(), opaquePathValue(), "", fragmentValue());
        return parseIntoCopyOrThis(input, copy, UrlParser.State.QUERY);
    }

    /// Returns immutable search parameters parsed from the current query.
    @Override
    public WebURLSearchParams searchParams() {
        WebURLSearchParams params = searchParams;
        if (params == null) {
            params = WebURLSearchParamsImpl.fromQueryInternal(queryStart < 0 ? "" : href.substring(queryStart, queryEnd));
            searchParams = params;
        }
        return params;
    }

    /// Returns a URL with the query replaced by serialized search parameters.
    @Override
    public WebURL withSearchParams(WebURLSearchParams value) {
        String serializedQuery = value.toString();
        return copy(username(), password(), hostValue(), portValue(), pathSegments(), opaquePathValue(),
                serializedQuery.isEmpty() ? null : serializedQuery, fragmentValue());
    }

    /// Returns the hash string, including the leading number sign when non-empty.
    @Override
    public String hash() {
        return fragmentStart < 0 || fragmentStart == href.length() ? "" : href.substring(fragmentStart - 1);
    }

    /// Returns a URL with the hash string updated.
    @Override
    public WebURL withHash(String value) {
        if (value.isEmpty()) {
            return copy(username(), password(), hostValue(), portValue(),
                    pathSegments(), opaquePathValue(), queryValue(), null);
        }

        String input = value.charAt(0) == '#' ? value.substring(1) : value;
        WebURLImpl copy = copy(username(), password(), hostValue(), portValue(),
                pathSegments(), opaquePathValue(), queryValue(), "");
        return parseIntoCopyOrThis(input, copy, UrlParser.State.FRAGMENT);
    }

    /// Returns the serialized URL as a Java `URI`.
    @Override
    public URI toURI() {
        try {
            return new URI(toRFC2396String());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("This URL cannot be represented as an RFC 2396 URI", exception);
        }
    }

    /// Returns the serialized URL as a Java `URL`.
    @Override
    public URL toURL() throws MalformedURLException {
        return toURI().toURL();
    }

    /// Returns the serialized URL.
    @Override
    public String toString() {
        return href;
    }

    /// Creates a URL copy with replacement components.
    private WebURLImpl copy(
            String username,
            String password,
            @Nullable UrlHost host,
            int port,
            List<String> path,
            @Nullable String opaquePath,
            @Nullable String query,
            @Nullable String fragment
    ) {
        return new WebURLImpl(scheme(), username, password, host, port, path, opaquePath, query, fragment);
    }

    /// Returns the implementation object for a `WebURL`.
    private static WebURLImpl implementation(WebURL url) {
        return (WebURLImpl) url;
    }

    /// Returns the serialized URL converted to Java's RFC 2396 URI syntax.
    @Override
    public String toRFC2396String() {
        StringBuilder output = new StringBuilder();
        output.append(href, 0, schemeEnd + 1);

        if (hasOpaquePath()) {
            appendRfc2396Encoded(output, pathname(), WebURLImpl::isRfc2396Uric);
        } else {
            if (hasHost()) {
                output.append("//");
                if (usernameStart >= 0) {
                    appendRfc2396Encoded(output, username(), WebURLImpl::isRfc2396UserInfo);
                    if (passwordStart >= 0) {
                        output.append(':');
                        appendRfc2396Encoded(output, password(), WebURLImpl::isRfc2396UserInfo);
                    }
                    output.append('@');
                }
                appendRfc2396Encoded(output, hostname(), WebURLImpl::isRfc2396Host);
                if (portStart >= 0) {
                    output.append(':').append(href, portStart, portEnd);
                }
            } else if (pathPrefix) {
                output.append("/.");
            }
            appendRfc2396Encoded(output, pathname(), WebURLImpl::isRfc2396Path);
        }

        if (queryStart >= 0) {
            output.append('?');
            appendRfc2396Encoded(output, href.substring(queryStart, queryEnd), WebURLImpl::isRfc2396Uric);
        }
        if (fragmentStart >= 0) {
            output.append('#');
            appendRfc2396Encoded(output, href.substring(fragmentStart), WebURLImpl::isRfc2396Uric);
        }
        return output.toString();
    }

    /// Appends a component encoded for Java's RFC 2396 URI parser.
    private static void appendRfc2396Encoded(
            StringBuilder output,
            String value,
            Rfc2396CharPredicate allowed
    ) {
        for (int index = 0; index < value.length(); ) {
            int c = value.codePointAt(index);
            if (c == '%' && index + 2 < value.length()
                    && Infra.isAsciiHex(value.charAt(index + 1))
                    && Infra.isAsciiHex(value.charAt(index + 2))) {
                output.append('%').append(value.charAt(index + 1)).append(value.charAt(index + 2));
                index += 3;
                continue;
            }

            if (c <= 0x7f && allowed.test(c)) {
                output.append((char) c);
            } else {
                for (byte b : Encoding.utf8Encode(new String(Character.toChars(c)))) {
                    appendRfc2396Escape(output, b & 0xff);
                }
            }
            index += Character.charCount(c);
        }
    }

    /// Appends one RFC 2396 percent escape.
    private static void appendRfc2396Escape(StringBuilder output, int value) {
        output.append('%');
        output.append(Character.toUpperCase(Character.forDigit((value >>> 4) & 0xf, 16)));
        output.append(Character.toUpperCase(Character.forDigit(value & 0xf, 16)));
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
        return Infra.isAsciiAlpha(c) || Infra.isAsciiDigit(c) || c == '-' || c == '_'
                || c == '.' || c == '!' || c == '~' || c == '*' || c == '\'' || c == '(' || c == ')';
    }

    /// Predicate over RFC 2396 ASCII characters.
    @FunctionalInterface
    @NotNullByDefault
    private interface Rfc2396CharPredicate {
        /// Returns whether the character may appear without escaping.
        boolean test(int c);
    }

    /// Runs a state override on a copy of this URL.
    private WebURL withStateOverride(String input, UrlParser.State state) {
        return parseIntoCopyOrThis(input, this, state);
    }

    /// Runs the scheme-state override with an input that has the colon delimiter required by the parser.
    private WebURL withSchemeOverride(String value) {
        return withStateOverride(value.endsWith(":") ? value : value + ":", UrlParser.State.SCHEME_START);
    }

    /// Parses into a copied URL and returns a new URL, or this URL on parser failure.
    private WebURL parseIntoCopyOrThis(String input, WebURLImpl copy, UrlParser.State state) {
        WebURLImpl parsed = UrlParser.basicParse(input, null, copy, state);
        return parsed == null ? this : parsed;
    }
}
