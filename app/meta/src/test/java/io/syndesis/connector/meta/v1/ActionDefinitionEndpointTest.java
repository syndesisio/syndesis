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
package io.syndesis.connector.meta.v1;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionDefinitionEndpointTest {
    private static final DataShape INPUT = new DataShape.Builder().build();

    private static final DataShape OUTPUT = new DataShape.Builder().build();;

    private static final Map<String, String> PAYLOAD = Collections.singletonMap("this", "is playload");

    private static final Map<String, List<PropertyPair>> PROPERTIES = Collections.singletonMap("property",
        Arrays.asList(new PropertyPair("value1", "First Value"), new PropertyPair("value2", "Second Value")));


    @Test
    public void shouldMetadata() throws Exception {
        final CamelContext context = new DefaultCamelContext();
        context.addComponent("petstore", new PetstoreComponent(PAYLOAD));

        try {
            context.start();

            final PetstoreMetadataRetrieval adapter = new PetstoreMetadataRetrieval(PAYLOAD, PROPERTIES, INPUT, OUTPUT);
            final SyndesisMetadata metadata = adapter.fetch(context, "petstore", "dog-food", Collections.emptyMap());

            assertThat(metadata.properties).isSameAs(PROPERTIES);
            assertThat(metadata.inputShape).isSameAs(INPUT);
            assertThat(metadata.outputShape).isSameAs(OUTPUT);
        } finally {
            context.stop();
        }
    }
}
