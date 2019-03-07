/*
 * Copyright (C) 2016 Red Hat, Inc.
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

package io.syndesis.server.endpoint.v1.handler.meta;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.StepKind;

/**
 * @author Christoph Deppisch
 */
interface StepMetadataHandler {

    String VARIANT_METADATA_KEY = "variant";
    String VARIANT_ELEMENT = "element";
    String VARIANT_COLLECTION = "collection";

    /**
     * Adapt dynamic meta data for given step descriptor;
     * @param metadata
     * @return
     */
    DynamicActionMetadata handle(DynamicActionMetadata metadata);

    /**
     * Determine if this handler can handle the specific step kind.
     * @param kind
     */
    default boolean canHandle(StepKind kind) {
        return false;
    }

    /**
     * Extracts variants from given original data shape excluding the given variant. Includes the original data shape itself as variant to the list of extracted variants.
     * In case given original data shape and exclude variant happen to be equal just return original data shape itself as exclusive variant and set given variant meta data.
     * @param original the original data shape providing variants
     * @param variant variant to exclude from the original variants
     * @param variantMeta optional new meta data variant value in case original and exclude happen to be equal
     * @return
     */
    default List<DataShape> extractVariants(DataShape original, DataShape variant, String variantMeta) {
        if (original.equals(variant)) {
            return Collections.singletonList(new DataShape.Builder()
                    .createFrom(original)
                    .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, variantMeta)
                    .variants(Collections.emptyList())
                    .build());
        } else {
            List<DataShape> variants = original.getVariants()
                    .stream()
                    .filter(shape -> !shape.equals(variant))
                    .collect(Collectors.toList());

            variants.add(new DataShape.Builder()
                    .createFrom(original)
                    .variants(Collections.emptyList())
                    .build());

            return variants;
        }
    }
}
