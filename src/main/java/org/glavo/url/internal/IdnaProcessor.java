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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.IDN;
import java.util.Locale;

/// Converts Unicode domain names to ASCII using ICU when present and JDK IDN otherwise.
@NotNullByDefault
final class IdnaProcessor {
    /// The selected processor.
    private static final Processor PROCESSOR = selectProcessor();

    /// Creates no instances.
    private IdnaProcessor() {
    }

    /// Converts a domain name to ASCII, returning `null` on failure.
    static @Nullable String toAscii(String domain, boolean strict) {
        return PROCESSOR.toAscii(domain, strict);
    }

    /// Selects the best available processor.
    private static Processor selectProcessor() {
        Processor icu = IcuProcessor.tryCreate();
        return icu != null ? icu : new JdkProcessor();
    }

    /// Domain-to-ASCII processor.
    @NotNullByDefault
    private interface Processor {
        /// Converts a domain name to ASCII.
        @Nullable String toAscii(String domain, boolean strict);
    }

    /// ICU4J reflection-based processor.
    @NotNullByDefault
    private static final class IcuProcessor implements Processor {
        /// ICU IDNA instance.
        private final Object idna;
        /// ICU Info constructor.
        private final Constructor<?> infoConstructor;
        /// ICU `toASCII` method.
        private final Method toAscii;
        /// ICU `Info.hasErrors` method.
        private final Method hasErrors;

        /// Creates a processor from reflected ICU members.
        private IcuProcessor(Object idna, Constructor<?> infoConstructor, Method toAscii, Method hasErrors) {
            this.idna = idna;
            this.infoConstructor = infoConstructor;
            this.toAscii = toAscii;
            this.hasErrors = hasErrors;
        }

        /// Attempts to create the ICU processor.
        static @Nullable Processor tryCreate() {
            try {
                Class<?> idnaClass = Class.forName("com.ibm.icu.text.IDNA");
                Class<?> infoClass = Class.forName("com.ibm.icu.text.IDNA$Info");
                int options = intField(idnaClass, "CHECK_BIDI")
                        | intField(idnaClass, "CHECK_CONTEXTJ")
                        | intField(idnaClass, "NONTRANSITIONAL_TO_ASCII");
                Object idna = idnaClass.getMethod("getUTS46Instance", int.class).invoke(null, options);
                Constructor<?> infoConstructor = infoClass.getConstructor();
                Method toAscii = idnaClass.getMethod("nameToASCII", CharSequence.class, StringBuilder.class, infoClass);
                Method hasErrors = infoClass.getMethod("hasErrors");
                return new IcuProcessor(idna, infoConstructor, toAscii, hasErrors);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return null;
            }
        }

        /// Converts a domain through ICU.
        @Override
        public @Nullable String toAscii(String domain, boolean strict) {
            try {
                Object info = infoConstructor.newInstance();
                StringBuilder output = new StringBuilder();
                toAscii.invoke(idna, domain, output, info);
                if ((Boolean) hasErrors.invoke(info)) {
                    return null;
                }
                return output.toString().toLowerCase(Locale.ROOT);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return null;
            }
        }

        /// Reads an ICU integer option field.
        private static int intField(Class<?> idnaClass, String name) throws ReflectiveOperationException {
            Field field = idnaClass.getField(name);
            return field.getInt(null);
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
