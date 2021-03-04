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

import java.util.HashMap;
import java.util.Map;

import io.syndesis.connector.support.verifier.api.sample.SampleConnectorMetaDataRetrieval;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorMetadataTest {

    @Test
    public void verifyMetadataPropertyEchoed() {
        SampleConnectorMetaDataRetrieval metaDataRetrieval = new SampleConnectorMetaDataRetrieval();
        Map<String, Object> properties = new HashMap<>();
        properties.put("brokers", null);
        properties.put("anotherProperty", null);
        SyndesisMetadataProperties dynamicProperties = metaDataRetrieval
            .fetchProperties(null, "test", properties);

        assertThat(dynamicProperties.getProperties().get("brokers"))
            .containsOnly(new PropertyPair("brokers", "BROKERS"));
        assertThat(dynamicProperties.getProperties().get("anotherProperty"))
            .containsOnly(new PropertyPair("anotherProperty", "ANOTHERPROPERTY"));
    }

}
