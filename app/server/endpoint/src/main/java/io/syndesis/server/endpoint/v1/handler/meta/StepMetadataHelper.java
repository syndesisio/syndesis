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
import java.util.Optional;
import java.util.stream.Collectors;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.model.integration.Step;

/**
 * @author Christoph Deppisch
 */
final class StepMetadataHelper {

    static final DataShape ANY_SHAPE = new DataShape.Builder().kind(DataShapeKinds.ANY).name("Any shape").build();
    static final DataShape NO_SHAPE = new DataShape.Builder().kind(DataShapeKinds.NONE).name("No shape").build();

    /**
     * Prevent instantiation of utility class.
     */
    private StepMetadataHelper() {
        super();
    }

    /**
     * Finds first step in list that has a proper input data shape.
     * @param steps list of steps to evaluate
     * @return optional matching step
     */
    static Optional<Step> getFirstWithInputShape(List<Step> steps) {
        return steps.stream()
                .filter(Step::hasInputDataShape)
                .findFirst();
    }

    /**
     * Finds last step in list that has a proper output data shape.
     * @param steps list of steps to evaluate
     * @return optional matching step
     */
    static Optional<Step> getLastWithOutputShape(List<Step> steps) {
        return steps.stream()
                .filter(Step::hasOutputDataShape)
                .reduce((first, second) -> second);
    }

    /**
     * Extracts variants from given original data shape excluding the given variant. Includes the original data shape itself as variant to the list of extracted variants.
     * In case given original data shape and exclude variant happen to be equal just return original data shape itself as exclusive variant and set given variant meta data.
     * @param original the original data shape providing variants
     * @param variant variant to exclude from the original variants
     * @param variantMeta optional new meta data variant value in case original and exclude happen to be equal
     * @return list of data shape variants
     */
    static List<DataShape> extractVariants(DataShape original, DataShape variant, String variantMeta) {
        if (original.equals(variant)) {
            return Collections.singletonList(new DataShape.Builder()
                    .createFrom(original)
                    .putMetadata(DataShapeMetaData.VARIANT, variantMeta)
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

    /**
     * Checks if given shape is a unified Json schema shape.
     * @param dataShape
     * @return
     */
    static boolean isUnifiedJsonSchemaShape(DataShape dataShape) {
        if (dataShape.getKind() == DataShapeKinds.JSON_SCHEMA) {
            return dataShape.getMetadata()
                    .entrySet()
                    .stream()
                    .anyMatch(entry -> entry.getKey().equals(DataShapeMetaData.UNIFIED));
        }

        return false;
    }
}
