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
package io.syndesis.server.endpoint.v1.handler.connection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netflix.hystrix.HystrixExecutable;
import io.syndesis.common.model.connection.DynamicConnectionPropertiesMetadata;
import io.syndesis.common.model.connection.WithDynamicProperties;
import io.syndesis.server.verifier.MetadataConfigurationProperties;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectorPropertiesHandlerTest {

    @Test
    public void shouldSendRequestsToMeta() {
        final MetadataConfigurationProperties config = new MetadataConfigurationProperties();
        config.setService("syndesis-meta");

        final Client client = mock(Client.class);

        final ConnectorPropertiesHandler handler = new ConnectorPropertiesHandler(config) {
            @Override
            protected HystrixExecutable<DynamicConnectionPropertiesMetadata> createMetadataConnectionPropertiesCommand(final String connectorId) {
                return new MetadataConnectionPropertiesCommand(config, connectorId, Collections.emptyMap()){
                    @Override
                    protected Client createClient() {
                        return client;
                    }
                };
            }
        };

        final ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);

        final WebTarget target = mock(WebTarget.class);
        when(client.target(url.capture())).thenReturn(target);
        final Invocation.Builder builder = mock(Invocation.Builder.class);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        final Map<String, Object> properties = Collections.emptyMap();
        final Map<String, List<WithDynamicProperties.ActionPropertySuggestion>> dynamicProperties = buildProperties();
        final DynamicConnectionPropertiesMetadata dynamicConnectionPropertiesMetadata =
            new DynamicConnectionPropertiesMetadata.Builder()
                .properties(dynamicProperties)
                .build();
        when(builder.post(Entity.entity(properties, MediaType.APPLICATION_JSON_TYPE),DynamicConnectionPropertiesMetadata.class))
            .thenReturn(dynamicConnectionPropertiesMetadata);
        final DynamicConnectionPropertiesMetadata received = handler.dynamicConnectionProperties("connectorId");

        assertThat(received).isSameAs(dynamicConnectionPropertiesMetadata);
        assertThat(url.getValue()).isEqualTo("http://syndesis-meta/api/v1/connectors/connectorId/properties/meta");
    }

    private static Map<String, List<WithDynamicProperties.ActionPropertySuggestion>> buildProperties() {
        HashMap<String, List<WithDynamicProperties.ActionPropertySuggestion>> properties = new HashMap<>();
        List<WithDynamicProperties.ActionPropertySuggestion> values = new ArrayList<>();
        values.add(new WithDynamicProperties.ActionPropertySuggestion.Builder()
            .value("valueTest")
            .displayValue("displayValueTest")
            .build()
        );
        properties.put("someProperty", values);
        return properties;
    }
}
