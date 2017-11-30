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
package io.syndesis.runtime.connector;

import java.util.List;

import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.model.connection.ConnectorTemplate;
import io.syndesis.runtime.BaseITCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration
public class CustomConnectorITCase extends BaseITCase {

    private final ConnectorTemplate template;

    public static class ConnectorTemplateResultList {
        public List<ConnectorTemplate> items;

        public int totalCount;
    }

    @Configuration
    public static class TestConfiguration {
        @Bean("connector-template")
        public static final ConnectorGenerator testGenerator() {
            return new ConnectorGenerator() {
                @Override
                public Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return baseConnectorFrom(connectorTemplate, connectorSettings);
                }

                @Override
                public Connector info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return baseConnectorFrom(connectorTemplate, connectorSettings);
                }
            };

        }
    }

    public CustomConnectorITCase() {
        template = createConnectorTemplate();
    }

    @Before
    public void createConnectorTemplates() {
        dataManager.create(template);
    }

    @Test
    public void shouldCreateNewCustomConnectors() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().build();

        final ResponseEntity<Connector> response = post("/api/v1/custom/connectors/connector-template", connectorSettings, Connector.class);

        final Connector created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(dataManager.fetch(Connector.class, response.getBody().getId().get())).isNotNull();
    }

    @Test
    public void shouldOfferConnectorTemplates() {
        final ResponseEntity<ConnectorTemplateResultList> response = get("/api/v1/custom/connectors", ConnectorTemplateResultList.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items).contains(template);
    }

    @Test
    public void shouldOfferCustomConnectorInfo() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().build();

        final ResponseEntity<Connector> response = post("/api/v1/custom/connectors/connector-template/info", connectorSettings,
            Connector.class);

        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void shouldOfferSingleConnectorTemplateById() {
        final ResponseEntity<ConnectorTemplate> response = get("/api/v1/custom/connectors/connector-template", ConnectorTemplate.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(template);
    }

    private static ConnectorTemplate createConnectorTemplate() {
        return new ConnectorTemplate.Builder()//
            .id("connector-template")//
            .name("connector template")//
            .build();
    }
}
