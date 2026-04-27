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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// Parser and serializer for `application/x-www-form-urlencoded` data.
@NotNullByDefault
public final class UrlEncoded {
    /// Creates no instances.
    private UrlEncoded() {
    }

    /// Parses a form-urlencoded string into name-value tuples.
    public static List<Map.Entry<String, String>> parseUrlencodedString(String input) {
        return parseUrlencoded(Encoding.utf8Encode(input));
    }

    /// Serializes name-value tuples as a form-urlencoded string.
    public static String serializeUrlencoded(List<Map.Entry<String, String>> tuples) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < tuples.size(); i++) {
            Map.Entry<String, String> tuple = tuples.get(i);
            if (i != 0) {
                output.append('&');
            }
            output.append(PercentEncoding.utf8PercentEncodeString(
                    tuple.getKey(), PercentEncoding::isUrlEncodedPercentEncode, true));
            output.append('=');
            output.append(PercentEncoding.utf8PercentEncodeString(
                    tuple.getValue(), PercentEncoding::isUrlEncodedPercentEncode, true));
        }
        return output.toString();
    }

    /// Parses form-urlencoded bytes into name-value tuples.
    private static List<Map.Entry<String, String>> parseUrlencoded(byte[] input) {
        List<Map.Entry<String, String>> output = new ArrayList<>();
        int start = 0;
        while (start <= input.length) {
            int end = indexOf(input, (byte) '&', start);
            if (end < 0) {
                end = input.length;
            }

            if (end > start) {
                int equals = indexOf(input, (byte) '=', start, end);
                byte[] name = slice(input, start, equals >= 0 ? equals : end);
                byte[] value = equals >= 0 ? slice(input, equals + 1, end) : new byte[0];
                replace(name, (byte) '+', (byte) ' ');
                replace(value, (byte) '+', (byte) ' ');
                output.add(Map.entry(
                        Encoding.utf8DecodeWithoutBom(PercentEncoding.percentDecodeBytes(name)),
                        Encoding.utf8DecodeWithoutBom(PercentEncoding.percentDecodeBytes(value))));
            }

            if (end == input.length) {
                break;
            }
            start = end + 1;
        }
        return output;
    }

    /// Finds a byte in an array.
    private static int indexOf(byte[] input, byte value, int from) {
        return indexOf(input, value, from, input.length);
    }

    /// Finds a byte in an array before the given exclusive end.
    private static int indexOf(byte[] input, byte value, int from, int end) {
        for (int i = from; i < end; i++) {
            if (input[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /// Copies a byte slice.
    private static byte[] slice(byte[] input, int start, int end) {
        byte[] output = new byte[end - start];
        System.arraycopy(input, start, output, 0, output.length);
        return output;
    }

    /// Replaces all matching bytes in an array.
    private static void replace(byte[] input, byte from, byte to) {
        for (int i = 0; i < input.length; i++) {
            if (input[i] == from) {
                input[i] = to;
            }
        }
    }
}
