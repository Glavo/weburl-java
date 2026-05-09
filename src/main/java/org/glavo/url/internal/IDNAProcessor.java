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

import org.glavo.url.IDNAProfile;
import org.glavo.url.internal.idna.UTS46;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.IDN;
import java.util.Locale;

/// Converts Unicode domain names to ASCII using the configured IDNA profile.
@NotNullByDefault
public final class IDNAProcessor {
    /// The JDK `java.net.IDN` processor.
    private static final Processor JDK_PROCESSOR = new JdkProcessor();

    /// Creates no instances.
    private IDNAProcessor() {
    }

    /// Returns whether a configured IDNA profile is available.
    public static boolean isAvailable(IDNAProfile profile) {
        return true;
    }

    /// Converts a domain name to ASCII, returning `null` on failure.
    static @Nullable String toAscii(String domain, boolean strict, IDNAProfile profile) {
        return switch (profile) {
            case UTS_46 -> {
                UTS46.Result result = UTS46.toAsciiForUrl(domain, strict);
                yield result.error() ? null : result.value();
            }
            case IDNA_2003 -> JDK_PROCESSOR.toAscii(domain, strict);
        };
    }

    /// Domain-to-ASCII processor.
    @NotNullByDefault
    private interface Processor {
        /// Converts a domain name to ASCII.
        @Nullable String toAscii(String domain, boolean strict);
    }

    /// JDK fallback processor.
    @NotNullByDefault
    private static final class JdkProcessor implements Processor {
        /// Converts a domain through `java.net.IDN`.
        @Override
        public @Nullable String toAscii(String domain, boolean strict) {
            try {
                int flags = strict ? IDN.USE_STD3_ASCII_RULES : 0;
                return IDN.toASCII(domain, flags).toLowerCase(Locale.ROOT);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }
}
