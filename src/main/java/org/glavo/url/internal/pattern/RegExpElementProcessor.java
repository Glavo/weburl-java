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
package org.glavo.url.internal.pattern;

import org.glavo.url.pattern.WebURLPatternParser;
import org.glavo.url.pattern.WebURLPatternSyntaxException;
import org.jetbrains.annotations.NotNullByDefault;

/// Processes user-written URLPattern regular-expression elements.
@NotNullByDefault
interface RegExpElementProcessor {
    /// Processor that accepts the supported standard-compatible subset.
    RegExpElementProcessor SUPPORTED = JavaScriptRegExpProcessor::process;

    /// Processor that rejects every user-written regular-expression element.
    RegExpElementProcessor REJECT = regexp -> {
        throw new WebURLPatternSyntaxException("Custom URLPattern regular expressions are rejected by this parser");
    };

    /// Processor that accepts Java regular-expression syntax without additional capture groups.
    RegExpElementProcessor JAVA = regexp -> {
        validateNoCapturingGroups(regexp);
        return regexp;
    };

    /// Returns the processor for the given policy.
    ///
    /// @param policy the regular-expression element policy
    /// @return the processor
    static RegExpElementProcessor forPolicy(WebURLPatternParser.RegExpPolicy policy) {
        return switch (policy) {
            case SUPPORTED -> SUPPORTED;
            case REJECT -> REJECT;
            case JAVA -> JAVA;
        };
    }

    /// Processes one regular-expression element and returns Java-compatible source.
    ///
    /// @param regexp regular-expression element source
    /// @return Java-compatible regular-expression source
    String process(String regexp);

    /// Rejects Java regular expressions that would shift URLPattern capture group indexes.
    private static void validateNoCapturingGroups(String regexp) {
        boolean inClass = false;
        boolean escaped = false;

        for (int i = 0; i < regexp.length(); i++) {
            char c = regexp.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (inClass) {
                if (c == ']') {
                    inClass = false;
                }
                continue;
            }
            if (c == '[') {
                inClass = true;
                continue;
            }
            if (c != '(') {
                continue;
            }
            if (i + 1 >= regexp.length() || regexp.charAt(i + 1) != '?') {
                throw new WebURLPatternSyntaxException("URLPattern regular expressions cannot contain capture groups");
            }
            if (i + 2 < regexp.length() && regexp.charAt(i + 2) == '<') {
                if (i + 3 >= regexp.length() || regexp.charAt(i + 3) != '=' && regexp.charAt(i + 3) != '!') {
                    throw new WebURLPatternSyntaxException(
                            "URLPattern regular expressions cannot contain named capture groups");
                }
            }
        }
    }
}
