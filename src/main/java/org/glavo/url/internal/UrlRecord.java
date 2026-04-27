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

/// Mutable internal URL record used by the parser and public URL facade.
@NotNullByDefault
public final class UrlRecord {
    /// URL scheme without the trailing colon.
    public String scheme = "";
    /// Percent-encoded username.
    public String username = "";
    /// Percent-encoded password.
    public String password = "";
    /// URL host, or `null` when absent.
    public @Nullable UrlHost host;
    /// URL port, or `null` when absent or defaulted.
    public @Nullable Integer port;
    /// Non-opaque path segments.
    public List<String> path = new ArrayList<>();
    /// Opaque path, or `null` when the URL has a path segment list.
    public @Nullable String opaquePath;
    /// Percent-encoded query, or `null` when absent.
    public @Nullable String query;
    /// Percent-encoded fragment, or `null` when absent.
    public @Nullable String fragment;

    /// Creates an empty URL record.
    public UrlRecord() {
    }

    /// Returns whether this URL record has an opaque path.
    public boolean hasOpaquePath() {
        return opaquePath != null;
    }

    /// Copies this record.
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public UrlRecord clone() {
        UrlRecord newRecord = new UrlRecord();
        newRecord.scheme = scheme;
        newRecord.username = username;
        newRecord.password = password;
        newRecord.host = host;
        newRecord.port = port;
        newRecord.path = new ArrayList<>(path);
        newRecord.opaquePath = opaquePath;
        newRecord.query = query;
        newRecord.fragment = fragment;
        return newRecord;
    }
}
