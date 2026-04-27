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

import java.nio.charset.StandardCharsets;

/// UTF-8 encoding helpers used by URL parsing.
@NotNullByDefault
final class Encoding {
    /// Creates no instances.
    private Encoding() {
    }

    /// Encodes the string as UTF-8 bytes.
    static byte[] utf8Encode(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    /// Decodes UTF-8 bytes, ignoring an initial byte order mark when present.
    static String utf8DecodeWithoutBom(byte[] bytes) {
        int offset = bytes.length >= 3
                && (bytes[0] & 0xff) == 0xef
                && (bytes[1] & 0xff) == 0xbb
                && (bytes[2] & 0xff) == 0xbf ? 3 : 0;
        return new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8);
    }
}
