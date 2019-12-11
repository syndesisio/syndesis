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
package io.syndesis.connector.support.verifier.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SyndesisMetadataProperties {

    public static final SyndesisMetadataProperties EMPTY = new SyndesisMetadataProperties(Collections.emptyMap());

    /**
     * A Map keyed by action property name with a list of {@link PropertyPair}
     * values that are applicable to for that property.
     */
    protected final Map<String, List<PropertyPair>> properties;

    public SyndesisMetadataProperties(Map<String, List<PropertyPair>> properties) {
        this.properties = properties;

        if (properties != null && !properties.isEmpty()) {
            for (final List<PropertyPair> propertyPairs : properties.values()) {
                Collections.sort(propertyPairs, Comparator.comparing(PropertyPair::getDisplayValue));
            }
        }
    }

    public Map<String, List<PropertyPair>> getProperties() {
        return properties;
    }
}
