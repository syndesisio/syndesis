/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.verifier.v1;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;
import io.syndesis.verifier.v1.metadata.PropertyPair;

import org.apache.camel.Endpoint;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultComponent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionPropertiesEndpointTest {

    private static final Map<String, String> PAYLOAD = Collections.singletonMap("this", "is playload");

    private static final Map<String, List<PropertyPair>> PROPERTIES = Collections.singletonMap("property",
        Arrays.asList(new PropertyPair("value1", "First Value"), new PropertyPair("value2", "Second Value")));
    private final ActionPropertiesEndpoint endpoint = new ActionPropertiesEndpoint(
        Collections.singletonMap("petstore-adapter", new PetstoreAdapter())) {
        @Override
        protected DefaultCamelContext camelContext() {
            final DefaultCamelContext camelContext = new DefaultCamelContext();
            camelContext.addComponent("petstore", new PetstoreComponent());

            return camelContext;
        }
    };

    public static class PetstoreAdapter implements MetadataAdapter {

        @Override
        public Map<String, List<PropertyPair>> apply(final Map<String, Object> properties, final MetaData metadata) {
            @SuppressWarnings("unchecked")
            final Map<String, String> payload = metadata.getPayload(Map.class);

            assertThat(payload).isSameAs(PAYLOAD);

            return PROPERTIES;
        }

    }

    private static class PetstoreComponent extends DefaultComponent {

        public PetstoreComponent() {
            registerExtension((MetaDataExtension) parameters -> Optional
                .of(MetaDataBuilder.on(getCamelContext()).withPayload(PAYLOAD).build()));
        }

        @Override
        protected Endpoint createEndpoint(final String uri, final String remaining,
            final Map<String, Object> parameters) throws Exception {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void shouldProvideActionPropertiesBasedOnMetadata() throws Exception {
        final Object properties = endpoint.properties("petstore", Collections.emptyMap());

        assertThat(properties).isSameAs(PROPERTIES);
    }
}
