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
package io.syndesis.server.runtime.connector;

import java.util.List;

import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.runtime.BaseITCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration
public class ConnectorTemplateITCase extends BaseITCase {

    private final ConnectorTemplate template;

    public static class ConnectorTemplateResultList {
        public List<ConnectorTemplate> items;

        public int totalCount;
    }

    @Configuration
    public static class TestConfiguration {
        private static final ActionsSummary ACTIONS_SUMMARY = new ActionsSummary.Builder().totalActions(5).putActionCountByTag("foo", 3)
            .putActionCountByTag("bar", 2).build();

        private static final ConfigurationProperty PROPERTY_1 = new ConfigurationProperty.Builder().displayName("Property 1").build();

        @Bean("connector-template")
        public static final ConnectorGenerator testGenerator() {
            return new ConnectorGenerator(new Connector.Builder()
                .addTags("from-connector")
                .build()) {
                @Override
                public Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return generateTestConnector(connectorTemplate, connectorSettings);
                }

                @Override
                public APISummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    final Connector base = generateTestConnector(connectorTemplate, connectorSettings);

                    return new APISummary.Builder().createFrom(base).actionsSummary(ACTIONS_SUMMARY).build();
                }

                Connector generateTestConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return new Connector.Builder().createFrom(baseConnectorFrom(connectorTemplate, connectorSettings))//
                        .icon("test-icon")//
                        .putProperty("property1", PROPERTY_1)//
                        .build();
                }

                @Override
                protected String determineConnectorDescription(final ConnectorTemplate connectorTemplate,
                    final ConnectorSettings connectorSettings) {
                    return "test-description";
                }

                @Override
                protected String determineConnectorName(final ConnectorTemplate connectorTemplate,
                    final ConnectorSettings connectorSettings) {
                    return "test-name";
                }
            };

        }
    }

    public ConnectorTemplateITCase() {
        template = createConnectorTemplate();
    }

    @Before
    public void createConnectorTemplates() {
        dataManager.create(template);
    }

    @Test
    public void shouldOfferConnectorTemplates() {
        final ResponseEntity<ConnectorTemplateResultList> response = get("/api/v1/connector-templates", ConnectorTemplateResultList.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items).contains(template);
    }

    @Test
    public void shouldOfferSingleConnectorTemplateById() {
        final ResponseEntity<ConnectorTemplate> response = get("/api/v1/connector-templates/connector-template", ConnectorTemplate.class);

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
