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

import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `WebURLSearchParams`.
@NotNullByDefault
public final class WebURLSearchParamsTest {
    /// Tests parsing and serialization.
    @Test
    public void parsesAndSerializes() {
        WebURLSearchParams params = new WebURLSearchParams("?a=1&a=2&b=x+y&empty");

        assertEquals(4, params.size());
        assertEquals("1", params.get("a"));
        assertEquals("x y", params.get("b"));
        assertEquals("", params.get("empty"));
        assertEquals("a=1&a=2&b=x+y&empty=", params.toString());
    }

    /// Tests mutation operations.
    @Test
    public void mutatesTuples() {
        WebURLSearchParams params = new WebURLSearchParams("z=1&a=2&a=3");

        assertTrue(params.has("a"));
        assertTrue(params.has("a", "2"));
        params.delete("a", "2");
        assertFalse(params.has("a", "2"));

        params.set("z", "9");
        params.append("b", "space value");
        params.sort();

        assertEquals("a=3&b=space+value&z=9", params.toString());
    }

    /// Tests iteration.
    @Test
    public void iteratesEntries() {
        WebURLSearchParams params = new WebURLSearchParams("a=1&b=2");
        Iterator<Map.Entry<String, String>> iterator = params.iterator();

        Map.Entry<String, String> first = iterator.next();
        Map.Entry<String, String> second = iterator.next();

        assertEquals("a", first.getKey());
        assertEquals("1", first.getValue());
        assertEquals("b", second.getKey());
        assertEquals("2", second.getValue());
        assertFalse(iterator.hasNext());
    }
}
