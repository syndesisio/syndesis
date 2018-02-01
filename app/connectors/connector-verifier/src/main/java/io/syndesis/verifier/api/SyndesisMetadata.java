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
package io.syndesis.verifier.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.syndesis.model.DataShape;

public final class SyndesisMetadata {

    public final DataShape inputShape;

    public final DataShape outputShape;

    /**
     * A Map keyed by action property name with a list of {@link PropertyPair}
     * values that are applicable to for that property.
     */
    public final Map<String, List<PropertyPair>> properties;

    public SyndesisMetadata(final Map<String, List<PropertyPair>> properties, final DataShape inputShape, final DataShape outputShape) {
        this.properties = properties;
        this.inputShape = inputShape;
        this.outputShape = outputShape;

        if (properties != null) {
            for (final List<PropertyPair> propertyPairs : properties.values()) {
                Collections.sort(propertyPairs, Comparator.comparing(PropertyPair::getDisplayValue));
            }
        }
    }
}
