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

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// Fast parser for already-normalized ASCII absolute URL serializations.
///
/// This parser intentionally accepts only a conservative subset. Inputs outside that subset return `null` and
/// are handled by the complete WHATWG state machine.
@NotNullByDefault
final class UrlFastParser {
    /// Scheme marker for non-special schemes.
    private static final int NON_SPECIAL = -2;
    /// Scheme marker for unrecognized or unsupported fast-path schemes.
    private static final int UNSUPPORTED = -3;

    /// Prevents instantiation.
    private UrlFastParser() {
    }

    /// Parses an already-normalized absolute URL serialization, or returns `null`.
    static @Nullable WebURLImpl parse(String input) {
        if (input.isEmpty() || !isAsciiWithoutPreprocessing(input)) {
            return null;
        }

        int schemeEnd = schemeEnd(input);
        if (schemeEnd < 0) {
            return null;
        }

        int defaultPort = defaultPort(input, schemeEnd);
        if (defaultPort == UNSUPPORTED) {
            return null;
        }

        return defaultPort == NON_SPECIAL
                ? parseOpaque(input, schemeEnd)
                : defaultPort == 0
                ? parseFile(input, schemeEnd)
                : parseSpecial(input, schemeEnd, defaultPort);
    }

    /// Parses a normalized special URL serialization.
    private static @Nullable WebURLImpl parseSpecial(String input, int schemeEnd, int defaultPort) {
        int length = input.length();
        int authorityStart = schemeEnd + 3;
        if (authorityStart > length
                || input.charAt(schemeEnd + 1) != '/'
                || input.charAt(schemeEnd + 2) != '/') {
            return null;
        }

        int pathStart = firstSpecialPathStart(input, authorityStart);
        if (pathStart < 0) {
            return null;
        }
        if (authorityStart == pathStart) {
            return null;
        }

        int at = lastIndexOf(input, '@', authorityStart, pathStart);
        int usernameStart = -1;
        int usernameEnd = -1;
        int passwordStart = -1;
        int passwordEnd = -1;
        int hostStart = authorityStart;
        String username = "";
        String password = "";
        if (at >= 0) {
            if (at == authorityStart) {
                return null;
            }
            int colon = indexOf(input, ':', authorityStart, at);
            usernameStart = authorityStart;
            usernameEnd = colon < 0 ? at : colon;
            if (containsInvalidUserInfo(input, usernameStart, usernameEnd)) {
                return null;
            }
            username = input.substring(usernameStart, usernameEnd);
            if (colon >= 0) {
                passwordStart = colon + 1;
                passwordEnd = at;
                if (containsInvalidUserInfo(input, passwordStart, passwordEnd)) {
                    return null;
                }
                password = input.substring(passwordStart, passwordEnd);
            }
            hostStart = at + 1;
        }

        HostAndPort hostAndPort = parseHostAndPort(input, hostStart, pathStart, defaultPort);
        if (hostAndPort == null) {
            return null;
        }

        PathAndSuffix pathAndSuffix = parsePathAndSuffix(input, pathStart);
        if (pathAndSuffix == null) {
            return null;
        }

        UrlRecord record = new UrlRecord();
        record.scheme = scheme(input, schemeEnd);
        record.username = username;
        record.password = password;
        record.host = hostAndPort.host;
        record.port = hostAndPort.port;
        record.path = pathAndSuffix.path;
        record.query = pathAndSuffix.query;
        record.fragment = pathAndSuffix.fragment;

        return new WebURLImpl(record, input, schemeEnd, usernameStart, usernameEnd, passwordStart, passwordEnd,
                hostAndPort.hostStart, hostAndPort.hostEnd, hostAndPort.portStart, hostAndPort.portEnd,
                pathAndSuffix.pathStart, pathAndSuffix.pathEnd, pathAndSuffix.queryStart, pathAndSuffix.queryEnd,
                pathAndSuffix.fragmentStart, false);
    }

    /// Parses a normalized file URL serialization with an empty host.
    private static @Nullable WebURLImpl parseFile(String input, int schemeEnd) {
        int authorityStart = schemeEnd + 3;
        if (authorityStart > input.length()
                || input.charAt(schemeEnd + 1) != '/'
                || input.charAt(schemeEnd + 2) != '/') {
            return null;
        }
        if (authorityStart >= input.length() || input.charAt(authorityStart) != '/') {
            return null;
        }

        PathAndSuffix pathAndSuffix = parsePathAndSuffix(input, authorityStart);
        if (pathAndSuffix == null) {
            return null;
        }
        if (!pathAndSuffix.path.isEmpty() && isWindowsDriveLetterWithPipe(pathAndSuffix.path.get(0))) {
            return null;
        }

        UrlRecord record = new UrlRecord();
        record.scheme = "file";
        record.host = UrlHost.EMPTY_DOMAIN;
        record.path = pathAndSuffix.path;
        record.query = pathAndSuffix.query;
        record.fragment = pathAndSuffix.fragment;

        return new WebURLImpl(record, input, schemeEnd, -1, -1, -1, -1,
                authorityStart, authorityStart, -1, -1,
                pathAndSuffix.pathStart, pathAndSuffix.pathEnd, pathAndSuffix.queryStart, pathAndSuffix.queryEnd,
                pathAndSuffix.fragmentStart, false);
    }

    /// Parses a normalized non-special opaque URL serialization.
    private static @Nullable WebURLImpl parseOpaque(String input, int schemeEnd) {
        if (schemeEnd + 1 < input.length() && input.charAt(schemeEnd + 1) == '/') {
            return null;
        }
        if (schemeEnd + 2 < input.length() && input.charAt(schemeEnd + 1) == '/' && input.charAt(schemeEnd + 2) == '/') {
            return null;
        }

        int pathStart = schemeEnd + 1;
        int queryMarker = indexOf(input, '?', pathStart, input.length());
        int fragmentMarker = indexOf(input, '#', pathStart, input.length());
        if (fragmentMarker >= 0 && queryMarker > fragmentMarker) {
            queryMarker = -1;
        }

        int pathEnd = queryMarker >= 0 ? queryMarker : fragmentMarker >= 0 ? fragmentMarker : input.length();
        if (!isNormalizedOpaquePath(input, pathStart, pathEnd)) {
            return null;
        }

        int queryStart = -1;
        int queryEnd = -1;
        @Nullable String query = null;
        if (queryMarker >= 0) {
            queryStart = queryMarker + 1;
            queryEnd = fragmentMarker >= 0 ? fragmentMarker : input.length();
            if (containsInvalidQuery(input, queryStart, queryEnd, false)) {
                return null;
            }
            query = input.substring(queryStart, queryEnd);
        }

        int fragmentStart = -1;
        @Nullable String fragment = null;
        if (fragmentMarker >= 0) {
            fragmentStart = fragmentMarker + 1;
            if (containsInvalidFragment(input, fragmentStart, input.length())) {
                return null;
            }
            fragment = input.substring(fragmentStart);
        }

        UrlRecord record = new UrlRecord();
        record.scheme = scheme(input, schemeEnd);
        record.path.clear();
        record.opaquePath = input.substring(pathStart, pathEnd);
        record.query = query;
        record.fragment = fragment;

        return new WebURLImpl(record, input, schemeEnd, -1, -1, -1, -1, -1, -1, -1, -1,
                pathStart, pathEnd, queryStart, queryEnd, fragmentStart, false);
    }

    /// Parses a special URL host and port.
    private static @Nullable HostAndPort parseHostAndPort(String input, int start, int end, int defaultPort) {
        if (start == end) {
            return null;
        }

        int hostEnd;
        int portStart = -1;
        int portEnd = -1;
        int port = -1;
        if (input.charAt(start) == '[') {
            int close = indexOf(input, ']', start + 1, end);
            if (close < 0) {
                return null;
            }
            hostEnd = close + 1;
            if (hostEnd < end) {
                if (input.charAt(hostEnd) != ':') {
                    return null;
                }
                portStart = hostEnd + 1;
                portEnd = end;
                port = parseNormalizedPort(input, portStart, portEnd, defaultPort);
                if (port < -1) {
                    return null;
                }
            }
        } else {
            int colon = indexOf(input, ':', start, end);
            hostEnd = colon < 0 ? end : colon;
            if (colon >= 0) {
                portStart = colon + 1;
                portEnd = end;
                port = parseNormalizedPort(input, portStart, portEnd, defaultPort);
                if (port < -1) {
                    return null;
                }
            }
        }

        @Nullable UrlHost host = parseNormalizedHost(input, start, hostEnd);
        if (host == null) {
            return null;
        }
        return new HostAndPort(host, start, hostEnd, portStart, portEnd, port);
    }

    /// Parses the path, query, and fragment suffix of a normalized special URL.
    private static @Nullable PathAndSuffix parsePathAndSuffix(String input, int pathStart) {
        int queryMarker = indexOf(input, '?', pathStart, input.length());
        int fragmentMarker = indexOf(input, '#', pathStart, input.length());
        if (fragmentMarker >= 0 && queryMarker > fragmentMarker) {
            queryMarker = -1;
        }

        int pathEnd = queryMarker >= 0 ? queryMarker : fragmentMarker >= 0 ? fragmentMarker : input.length();
        @Nullable List<String> path = parseNormalizedPath(input, pathStart, pathEnd);
        if (path == null) {
            return null;
        }

        int queryStart = -1;
        int queryEnd = -1;
        @Nullable String query = null;
        if (queryMarker >= 0) {
            queryStart = queryMarker + 1;
            queryEnd = fragmentMarker >= 0 ? fragmentMarker : input.length();
            if (containsInvalidQuery(input, queryStart, queryEnd, true)) {
                return null;
            }
            query = input.substring(queryStart, queryEnd);
        }

        int fragmentStart = -1;
        @Nullable String fragment = null;
        if (fragmentMarker >= 0) {
            fragmentStart = fragmentMarker + 1;
            if (containsInvalidFragment(input, fragmentStart, input.length())) {
                return null;
            }
            fragment = input.substring(fragmentStart);
        }

        return new PathAndSuffix(path, pathStart, pathEnd, queryStart, queryEnd, fragmentStart, query, fragment);
    }

    /// Parses a normalized host.
    private static @Nullable UrlHost parseNormalizedHost(String input, int start, int end) {
        if (start == end) {
            return null;
        }
        if (input.charAt(start) == '[') {
            String hostText = input.substring(start, end);
            UrlHost host;
            try {
                host = UrlParser.parseSerializedHost(hostText, false);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
            return host.matchSerialized(input, start) == end ? host : null;
        }
        if (!isNormalizedDomainOrIpv4Host(input, start, end)) {
            return null;
        }
        if (endsInANumber(input, start, end)) {
            String hostText = input.substring(start, end);
            UrlHost host;
            try {
                host = UrlParser.parseSerializedHost(hostText, false);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
            return host.matchSerialized(input, start) == end ? host : null;
        }
        return UrlHost.domain(input.substring(start, end));
    }

    /// Parses normalized path segments.
    private static @Nullable List<String> parseNormalizedPath(String input, int start, int end) {
        if (start >= end || input.charAt(start) != '/') {
            return null;
        }

        ArrayList<String> path = new ArrayList<>();
        int segmentStart = start + 1;
        while (true) {
            int segmentEnd = indexOf(input, '/', segmentStart, end);
            if (segmentEnd < 0) {
                segmentEnd = end;
            }
            if (!isNormalizedPathSegment(input, segmentStart, segmentEnd)) {
                return null;
            }
            path.add(input.substring(segmentStart, segmentEnd));
            if (segmentEnd == end) {
                return path;
            }
            segmentStart = segmentEnd + 1;
        }
    }

    /// Returns whether the input needs no preprocessing before parsing.
    private static boolean isAsciiWithoutPreprocessing(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c <= 0x20 || c > 0x7e) {
                return false;
            }
        }
        return true;
    }

    /// Returns the scheme delimiter index, or `-1`.
    private static int schemeEnd(String input) {
        if (!Infra.isAsciiAlpha(input.charAt(0))) {
            return -1;
        }
        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ':') {
                return i;
            }
            if (!Infra.isAsciiAlphanumeric(c) && c != '+' && c != '-' && c != '.') {
                return -1;
            }
            if (c >= 'A' && c <= 'Z') {
                return -1;
            }
        }
        return -1;
    }

    /// Returns the default port for a fast-path scheme marker.
    private static int defaultPort(String input, int schemeEnd) {
        if (matches(input, schemeEnd, "ftp")) {
            return 21;
        }
        if (matches(input, schemeEnd, "http")) {
            return 80;
        }
        if (matches(input, schemeEnd, "https")) {
            return 443;
        }
        if (matches(input, schemeEnd, "ws")) {
            return 80;
        }
        if (matches(input, schemeEnd, "wss")) {
            return 443;
        }
        if (matches(input, schemeEnd, "file")) {
            return 0;
        }
        return NON_SPECIAL;
    }

    /// Returns a canonical scheme string.
    private static String scheme(String input, int schemeEnd) {
        if (matches(input, schemeEnd, "ftp")) {
            return "ftp";
        }
        if (matches(input, schemeEnd, "http")) {
            return "http";
        }
        if (matches(input, schemeEnd, "https")) {
            return "https";
        }
        if (matches(input, schemeEnd, "ws")) {
            return "ws";
        }
        if (matches(input, schemeEnd, "wss")) {
            return "wss";
        }
        return input.substring(0, schemeEnd);
    }

    /// Returns the first path slash after a special URL authority.
    private static int firstSpecialPathStart(String input, int start) {
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '/') {
                return i;
            }
            if (c == '?' || c == '#') {
                return -1;
            }
            if (c == '\\') {
                return -1;
            }
        }
        return -1;
    }

    /// Parses a normalized non-default port.
    private static int parseNormalizedPort(String input, int start, int end, int defaultPort) {
        if (start == end || end - start > 1 && input.charAt(start) == '0') {
            return -2;
        }
        int value = 0;
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (!Infra.isAsciiDigit(c)) {
                return -2;
            }
            value = value * 10 + (c - '0');
            if (value > 65535) {
                return -2;
            }
        }
        return value == defaultPort ? -2 : value;
    }

    /// Returns whether a host slice is a normalized domain or IPv4 host candidate.
    private static boolean isNormalizedDomainOrIpv4Host(String input, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c >= 'A' && c <= 'Z' || c == '%' || c <= 0x20 || c == '#'
                    || c == '/' || c == ':' || c == '<' || c == '>' || c == '?' || c == '@'
                    || c == '[' || c == '\\' || c == ']' || c == '^' || c == '|') {
                return false;
            }
        }
        return !containsPunycodeLabel(input, start, end);
    }

    /// Returns whether the host slice ends in a numeric label.
    private static boolean endsInANumber(String input, int start, int end) {
        if (end > start + 1 && input.charAt(end - 1) == '.') {
            end--;
        }
        int lastDot = lastIndexOf(input, '.', start, end);
        int labelStart = lastDot < 0 ? start : lastDot + 1;
        if (parseIpv4Number(input, labelStart, end) != null) {
            return true;
        }
        for (int i = labelStart; i < end; i++) {
            if (!Infra.isAsciiDigit(input.charAt(i))) {
                return false;
            }
        }
        return labelStart < end;
    }

    /// Returns whether a domain slice contains a punycode label.
    private static boolean containsPunycodeLabel(String input, int start, int end) {
        int labelStart = start;
        while (labelStart <= end) {
            if (labelStart + 4 <= end && input.regionMatches(true, labelStart, "xn--", 0, 4)) {
                return true;
            }
            int dot = indexOf(input, '.', labelStart, end);
            if (dot < 0) {
                return false;
            }
            labelStart = dot + 1;
        }
        return false;
    }

    /// Parses an IPv4 number from a string slice.
    private static @Nullable Long parseIpv4Number(String input, int start, int end) {
        if (start == end) {
            return null;
        }

        int radix = 10;
        int valueStart = start;
        if (end - valueStart >= 2
                && input.charAt(valueStart) == '0'
                && (input.charAt(valueStart + 1) == 'x' || input.charAt(valueStart + 1) == 'X')) {
            valueStart += 2;
            radix = 16;
        } else if (end - valueStart >= 2 && input.charAt(valueStart) == '0') {
            valueStart++;
            radix = 8;
        }

        if (valueStart == end) {
            return 0L;
        }

        long value = 0;
        for (int i = valueStart; i < end; i++) {
            int c = input.charAt(i);
            boolean ok = radix == 10 ? Infra.isAsciiDigit(c)
                    : radix == 16 ? Infra.isAsciiHex(c)
                    : c >= '0' && c <= '7';
            if (!ok) {
                return null;
            }
            int digit = Character.digit(c, radix);
            if (value > (Long.MAX_VALUE - digit) / radix) {
                return Long.MAX_VALUE;
            }
            value = value * radix + digit;
        }
        return value;
    }

    /// Returns whether a path segment is normalized.
    private static boolean isNormalizedPathSegment(String input, int start, int end) {
        if (isSingleDotPathSegment(input, start, end) || isDoubleDotPathSegment(input, start, end)) {
            return false;
        }
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c == '%' && isInvalidPercentTriplet(input, i, end)) {
                return false;
            }
            if (c != '%' && PercentEncoding.isPathPercentEncode(c)) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether an opaque path slice is normalized.
    private static boolean isNormalizedOpaquePath(String input, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c == '%' && isInvalidPercentTriplet(input, i, end)) {
                return false;
            }
            if (c == ' ') {
                return false;
            }
        }
        return true;
    }

    /// Returns whether a query slice contains an invalid or unnormalized character.
    private static boolean containsInvalidQuery(String input, int start, int end, boolean special) {
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c == '%' && isInvalidPercentTriplet(input, i, end)) {
                return true;
            }
            if (c != '%' && (special
                    ? PercentEncoding.isSpecialQueryPercentEncode(c)
                    : PercentEncoding.isQueryPercentEncode(c))) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a fragment slice contains an invalid or unnormalized character.
    private static boolean containsInvalidFragment(String input, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c == '%' && isInvalidPercentTriplet(input, i, end)) {
                return true;
            }
            if (c != '%' && PercentEncoding.isFragmentPercentEncode(c)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a userinfo slice contains an invalid or unnormalized character.
    private static boolean containsInvalidUserInfo(String input, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c == '%' && isInvalidPercentTriplet(input, i, end)) {
                return true;
            }
            if (c != '%' && PercentEncoding.isUserinfoPercentEncode(c)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether the percent sign starts an invalid percent triplet within the slice.
    private static boolean isInvalidPercentTriplet(String input, int index, int end) {
        return index + 2 >= end
                || !Infra.isAsciiHex(input.charAt(index + 1))
                || !Infra.isAsciiHex(input.charAt(index + 2));
    }

    /// Returns whether the segment is a single-dot path segment.
    private static boolean isSingleDotPathSegment(String input, int start, int end) {
        return end - start == 1 && input.charAt(start) == '.'
                || end - start == 3
                && input.charAt(start) == '%'
                && input.charAt(start + 1) == '2'
                && (input.charAt(start + 2) == 'e' || input.charAt(start + 2) == 'E');
    }

    /// Returns whether the segment is a double-dot path segment.
    private static boolean isDoubleDotPathSegment(String input, int start, int end) {
        if (end - start == 2) {
            return input.charAt(start) == '.' && input.charAt(start + 1) == '.';
        }
        if (end - start == 4) {
            return isPercentEncodedDot(input, start) && input.charAt(start + 3) == '.'
                    || input.charAt(start) == '.' && isPercentEncodedDot(input, start + 1);
        }
        return end - start == 6 && isPercentEncodedDot(input, start) && isPercentEncodedDot(input, start + 3);
    }

    /// Returns whether the slice starts with percent-encoded dot.
    private static boolean isPercentEncodedDot(String input, int start) {
        return input.charAt(start) == '%'
                && input.charAt(start + 1) == '2'
                && (input.charAt(start + 2) == 'e' || input.charAt(start + 2) == 'E');
    }

    /// Returns whether a path segment is a Windows drive letter using pipe syntax.
    private static boolean isWindowsDriveLetterWithPipe(String value) {
        return value.length() == 2 && Infra.isAsciiAlpha(value.charAt(0)) && value.charAt(1) == '|';
    }

    /// Returns whether a slice equals a string.
    private static boolean matches(String input, int end, String value) {
        return end == value.length() && input.regionMatches(0, value, 0, value.length());
    }

    /// Returns the first index of a character in a slice, or `-1`.
    private static int indexOf(String input, char value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (input.charAt(i) == value) {
                return i;
            }
        }
        return -1;
    }

    /// Returns the last index of a character in a slice, or `-1`.
    private static int lastIndexOf(String input, char value, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (input.charAt(i) == value) {
                return i;
            }
        }
        return -1;
    }

    /// Parsed host and port indexes.
    private record HostAndPort(
            UrlHost host,
            int hostStart,
            int hostEnd,
            int portStart,
            int portEnd,
            int port
    ) {
    }

    /// Parsed path, query, and fragment data.
    private record PathAndSuffix(
            List<String> path,
            int pathStart,
            int pathEnd,
            int queryStart,
            int queryEnd,
            int fragmentStart,
            @Nullable String query,
            @Nullable String fragment
    ) {
    }
}
