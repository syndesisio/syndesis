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
package io.syndesis.connector.debezium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.connector.debezium.metadata.DebeziumDatashapeStrategy;
import io.syndesis.connector.debezium.metadata.DebeziumMySQLDatashapeStrategy;
import io.syndesis.connector.kafka.KafkaMetaDataRetrieval;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;

public class DebeziumMetaDataRetrieval extends KafkaMetaDataRetrieval {

    private static final DataShape ANY = new DataShape.Builder().kind(DataShapeKinds.ANY).build();
    private final DebeziumDatashapeStrategy datashapeStrategy = new DebeziumMySQLDatashapeStrategy();

    @Override
    protected SyndesisMetadata adapt(final CamelContext context, final String componentId, final String actionId, final Map<String, Object> properties,
                                     final MetaDataExtension.MetaData metadata) {
        // Retrieve the list of topics
        @SuppressWarnings("unchecked") final Set<String> topicsNames = (Set<String>) metadata.getPayload();
        final List<PropertyPair> topicsResult = new ArrayList<>();
        topicsNames.stream().forEach(
            t -> topicsResult.add(new PropertyPair(t, t)));
        // Retrieve dynamically from list of topics provided by Kafka
        final Map<String, List<PropertyPair>> dynamicProperties = new HashMap<>();
        dynamicProperties.put("topic", topicsResult);
        dynamicProperties.put("schemaChange", topicsResult);

        final String selectedTopic = ConnectorOptions.extractOption(properties, "topic");

        final DataShape outputDataShape =  selectedTopic != null ? datashapeStrategy.getDatashape(properties) : ANY;
        return new SyndesisMetadata(
            dynamicProperties,
            null,
            outputDataShape);
    }
}
