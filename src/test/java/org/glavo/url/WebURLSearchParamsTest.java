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
import java.util.LinkedHashMap;
import java.util.List;
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

    /// Tests immutable update operations.
    @Test
    public void updatesTuplesImmutably() {
        WebURLSearchParams params = new WebURLSearchParams("z=1&a=2&a=3");

        assertTrue(params.has("a"));
        assertTrue(params.has("a", "2"));
        WebURLSearchParams deleted = params.delete("a", "2");
        assertTrue(params.has("a", "2"));
        assertFalse(deleted.has("a", "2"));

        WebURLSearchParams updated = deleted
                .set("z", "9")
                .append("b", "space value")
                .sort();

        assertEquals("z=1&a=2&a=3", params.toString());
        assertEquals("a=3&b=space+value&z=9", updated.toString());
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

    /// Tests entry, key, value, and callback helpers.
    @Test
    public void exposesOrderedViews() {
        WebURLSearchParams params = new WebURLSearchParams("a=1&b=2&a=3");
        StringBuilder callbacks = new StringBuilder();

        params.forEach((value, name) -> callbacks.append(name).append('=').append(value).append(';'));

        assertEquals(List.of("a", "b", "a"), params.keys());
        assertEquals(List.of("1", "2", "3"), params.values());
        assertEquals("a=1;b=2;a=3;", callbacks.toString());
        assertEquals(params.iterator().next(), params.entries().iterator().next());
    }

    /// Tests constructing from ordered map entries.
    @Test
    public void constructsFromMapEntries() {
        LinkedHashMap<String, String> input = new LinkedHashMap<>();
        input.put("b", "2");
        input.put("a", "1");

        WebURLSearchParams params = new WebURLSearchParams(input);

        assertEquals("b=2&a=1", params.toString());
    }

    /// Tests that copying URL search parameters creates a detached list.
    @Test
    public void copiesUrlParamsAsDetachedParams() {
        WebURL url = WebURL.of("https://example.test/?a=1");
        WebURLSearchParams copy = new WebURLSearchParams(url.searchParams());

        WebURLSearchParams updated = copy.set("a", "2");

        assertEquals("https://example.test/?a=1", url.href());
        assertEquals("a=1", copy.toString());
        assertEquals("a=2", updated.toString());
    }
}
