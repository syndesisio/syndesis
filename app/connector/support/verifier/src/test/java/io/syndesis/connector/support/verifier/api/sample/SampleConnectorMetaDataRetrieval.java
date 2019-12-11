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
package io.syndesis.connector.support.verifier.api.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import io.syndesis.connector.support.verifier.api.SyndesisMetadataProperties;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;

public class SampleConnectorMetaDataRetrieval extends ComponentMetadataRetrieval {

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        return null;
    }

    @Override
    public SyndesisMetadataProperties fetchProperties(CamelContext context, String componentId, Map<String, Object> properties) {
        Map<String, List<PropertyPair>> echoProperties = SampleConnectorMetaDataRetrieval.echoProperties(properties);
        return new SyndesisMetadataProperties(echoProperties);
    }

    private static Map<String, List<PropertyPair>> echoProperties(Map<String, Object> properties) {
        Map<String, List<PropertyPair>> echoProperties = new HashMap<>();
        for(Map.Entry<String, Object> x : properties.entrySet()){
            List<PropertyPair> propertyPairs = new ArrayList<>();
            propertyPairs.add(new PropertyPair(x.getKey(), x.getKey().toUpperCase()));
            echoProperties.put(x.getKey(),propertyPairs);
        }
        return echoProperties;
    }
}
