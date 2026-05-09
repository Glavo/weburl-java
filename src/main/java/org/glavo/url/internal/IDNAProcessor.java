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

import com.ibm.icu.text.IDNA;
import org.glavo.url.IDNAProfile;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.IDN;
import java.util.Locale;

/// Converts Unicode domain names to ASCII using the configured IDNA profile.
@NotNullByDefault
public final class IDNAProcessor {
    /// The JDK `java.net.IDN` processor.
    private static final Processor JDK_PROCESSOR = new JdkProcessor();
    /// The ICU4J processor, or `null` when ICU4J is not available.
    private static final @Nullable Processor ICU_PROCESSOR = createIcuProcessor();

    /// Creates no instances.
    private IDNAProcessor() {
    }

    /// Attempts to create the ICU processor.
    private static @Nullable Processor createIcuProcessor() {
        try {
            return IcuProcessor.create();
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    /// Returns whether a configured IDNA profile is available.
    public static boolean isAvailable(IDNAProfile profile) {
        return switch (profile) {
            case UTS_46 -> ICU_PROCESSOR != null;
            case IDNA_2003 -> true;
        };
    }

    /// Converts a domain name to ASCII, returning `null` on failure.
    static @Nullable String toAscii(String domain, boolean strict, IDNAProfile profile) {
        Processor processor = processor(profile);
        if (processor == null) {
            throw new IllegalStateException("UTS #46 IDNA processing is not available");
        }
        return processor.toAscii(domain, strict);
    }

    /// Selects the processor for a configured profile.
    private static @Nullable Processor processor(IDNAProfile profile) {
        return switch (profile) {
            case UTS_46 -> ICU_PROCESSOR;
            case IDNA_2003 -> JDK_PROCESSOR;
        };
    }

    /// Domain-to-ASCII processor.
    @NotNullByDefault
    private interface Processor {
        /// Converts a domain name to ASCII.
        @Nullable String toAscii(String domain, boolean strict);
    }

    /// ICU4J processor.
    @NotNullByDefault
    private static final class IcuProcessor implements Processor {
        /// ICU IDNA instance.
        private final IDNA idna;

        /// Creates a processor from an ICU IDNA instance.
        private IcuProcessor(IDNA idna) {
            this.idna = idna;
        }

        /// Creates the ICU processor.
        static Processor create() {
            int options = IDNA.CHECK_BIDI
                    | IDNA.CHECK_CONTEXTJ
                    | IDNA.NONTRANSITIONAL_TO_ASCII;
            return new IcuProcessor(IDNA.getUTS46Instance(options));
        }

        /// Converts a domain through ICU.
        @Override
        public @Nullable String toAscii(String domain, boolean strict) {
            try {
                IDNA.Info info = new IDNA.Info();
                StringBuilder output = new StringBuilder();
                idna.nameToASCII(domain, output, info);
                if (info.hasErrors()) {
                    return null;
                }
                return output.toString().toLowerCase(Locale.ROOT);
            } catch (RuntimeException | LinkageError ignored) {
                return null;
            }
        }
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
