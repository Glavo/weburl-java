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

package org.glavo.url.website;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.teavm.classlib.ResourceSupplier;
import org.teavm.classlib.ResourceSupplierContext;

/// Supplies classpath resources that the website TeaVM build must embed into the generated module.
@NotNullByDefault
public final class WebURLResourceSupplier implements ResourceSupplier {
    /// IDNA data resource used by the main WebURL parser.
    private static final String IDNA_DATA_RESOURCE = "org/glavo/url/internal/idna/IdnaData.bin";

    /// Creates a resource supplier.
    public WebURLResourceSupplier() {
    }

    /// Returns the resources required by the WebURL parser at runtime.
    ///
    /// @param context TeaVM resource supplier context
    /// @return resource paths to embed
    @Override
    public String @Unmodifiable [] supplyResources(ResourceSupplierContext context) {
        return new String[]{IDNA_DATA_RESOURCE};
    }
}
