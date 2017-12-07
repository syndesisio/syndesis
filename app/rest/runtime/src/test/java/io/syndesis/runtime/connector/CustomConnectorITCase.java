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

import io.syndesis.connector.generator.ActionsSummary;
import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.connector.generator.ConnectorSummary;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorGroup;
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

    private static final String SECOND_TEMPLATE_ID = "second-connector-template";

    private static final String TEMPLATE_ID = "connector-template";

    private final Connector connector1 = new Connector.Builder().id("connector-from-template-1")
        .connectorGroup(new ConnectorGroup.Builder().id(TEMPLATE_ID).name("connector-template-group").build()).build();

    private final Connector connector2 = new Connector.Builder().id("connector-from-template-2")
        .connectorGroup(new ConnectorGroup.Builder().id(TEMPLATE_ID).name("connector-template-group").build()).build();

    private final Connector connector3 = new Connector.Builder().id("connector-from-second-template")
        .connectorGroup(new ConnectorGroup.Builder().id(SECOND_TEMPLATE_ID).name("second-connector-template-group").build()).build();

    private final ConnectorTemplate template = createConnectorTemplate(TEMPLATE_ID, "connector template");

    private String firstConnectorId;

    public static class ConnectorResultList {
        public List<Connector> items;

        public int totalCount;
    }

    @Configuration
    public static class TestConfiguration {
        private static final ActionsSummary ACTIONS_SUMMARY = new ActionsSummary.Builder().totalActions(5).putActionCountByTag("foo", 3)
            .putActionCountByTag("bar", 2).build();

        private static final ConfigurationProperty PROPERTY_1 = new ConfigurationProperty.Builder().displayName("Property 1").build();

        @Bean(TEMPLATE_ID)
        public static final ConnectorGenerator testGenerator() {
            return new ConnectorGenerator() {
                @Override
                public Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return generateTestConnector(connectorTemplate, connectorSettings);
                }

                @Override
                public ConnectorSummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    final Connector base = generateTestConnector(connectorTemplate, connectorSettings);

                    return new ConnectorSummary.Builder().createFrom(base).actionsSummary(ACTIONS_SUMMARY).build();
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

    @Before
    public void createConnectorTemplates() {
        dataManager.create(template);
        final ConnectorTemplate secondTemplate = createConnectorTemplate(SECOND_TEMPLATE_ID, "connector template");

        dataManager.create(secondTemplate);

        firstConnectorId = dataManager.create(connector1).getId().get();
        dataManager.create(connector2);
        dataManager.create(connector3);
    }

    @Test
    public void shouldCreateNewCustomConnectors() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().connectorTemplateId(TEMPLATE_ID).build();

        final ResponseEntity<Connector> response = post("/api/v1/connectors/custom", connectorSettings, Connector.class);

        final Connector created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getDescription()).isEqualTo("test-description");
        assertThat(dataManager.fetch(Connector.class, response.getBody().getId().get())).isNotNull();
    }

    @Test
    public void shouldFetchCustomConnectorsById() {
        final ResponseEntity<Connector> response = get("/api/v1/connectors/custom/" + firstConnectorId, Connector.class);

        assertThat(response.getBody()).isEqualTo(connector1);
    }

    @Test
    public void shouldListCustomConnectorsGeneratedFromFirstTemplate() {
        final ResponseEntity<ConnectorResultList> response = get("/api/v1/connectors/custom?templateId=" + TEMPLATE_ID,
            ConnectorResultList.class);

        assertThat(response.getBody().items).containsOnly(connector1, connector2);
    }

    @Test
    public void shouldListCustomConnectorsGeneratedFromSecondTemplate() {
        final ResponseEntity<ConnectorResultList> response = get("/api/v1/connectors/custom?templateId=" + SECOND_TEMPLATE_ID,
            ConnectorResultList.class);

        assertThat(response.getBody().items).containsOnly(connector3);
    }

    @Test
    public void shouldOfferCustomConnectorInfo() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().connectorTemplateId(TEMPLATE_ID).build();

        final ResponseEntity<ConnectorSummary> response = post("/api/v1/connectors/custom/info", connectorSettings, ConnectorSummary.class);

        final ConnectorSummary expected = new ConnectorSummary.Builder()// \
            .name("test-name")//
            .description("test-description")//
            .icon("test-icon")//
            .putProperty("property1", TestConfiguration.PROPERTY_1)//
            .actionsSummary(TestConfiguration.ACTIONS_SUMMARY)//
            .build();
        assertThat(response.getBody()).isEqualTo(expected);
    }

    private static ConnectorTemplate createConnectorTemplate(final String id, final String name) {
        return new ConnectorTemplate.Builder()//
            .id(id)//
            .name(name)//
            .build();
    }
}
