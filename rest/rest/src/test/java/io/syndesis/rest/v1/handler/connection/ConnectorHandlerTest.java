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
package io.syndesis.rest.v1.handler.connection;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.credential.Credentials;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.inspector.Inspectors;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorGroup;
import io.syndesis.model.connection.ConnectorTemplate;
import io.syndesis.rest.v1.state.ClientSideState;
import io.syndesis.verifier.Verifier;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectorHandlerTest {

    private static final Credentials NO_CREDENTIALS = null;

    private static final EncryptionComponent NO_ENCRYPTION_COMPONENT = null;

    private static final Inspectors NO_INSPECTORS = null;

    private static final ClientSideState NO_STATE = null;

    private static final Verifier NO_VERIFIER = null;

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final DataManager dataManager = mock(DataManager.class);

    private final ConnectorHandler handler = new ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS,
        NO_INSPECTORS, NO_STATE, NO_ENCRYPTION_COMPONENT, applicationContext);

    @Test
    public void shouldCreateNewConnectorsBasedOnConnectorTemplates() {
        final Map<String, ConfigurationProperty> properties = new HashMap<>();
        properties.put("prop1", new ConfigurationProperty.Builder().build());

        final Map<String, ConfigurationProperty> connectorProperties = new HashMap<>();
        connectorProperties.put("prop2", new ConfigurationProperty.Builder().build());
        connectorProperties.put("prop3", new ConfigurationProperty.Builder().build());

        final ConnectorGroup group = new ConnectorGroup.Builder().name("connector template group").build();

        final ConnectorTemplate connectorTemplate = new ConnectorTemplate.Builder()//
            .name("connector template")//
            .properties(properties).connectorProperties(connectorProperties)//
            .connectorGroup(group)//
            .build();

        final ConnectorAction action = new ConnectorAction.Builder().name("action").build();

        when(dataManager.fetch(ConnectorTemplate.class, "connector-template-id")).thenReturn(connectorTemplate);
        when(dataManager.create(any(Connector.class)))
            .thenAnswer(invocation -> invocation.getArgumentAt(0, Connector.class));

        when(applicationContext.getBean("connector-template-id", ConnectorGenerator.class))
            .thenReturn(new ConnectorGenerator() {
                @Override
                public Connector generate(final ConnectorTemplate connectorTemplate, final Connector template) {
                    return new Connector.Builder().createFrom(baseConnectorFrom(connectorTemplate, template))
                        .addAction(action).build();
                }
            });

        final Connector created = handler.create("connector-template-id",
            new Connector.Builder()//
                .name("new connector")//
                .description("new connector description")//
                .icon("new connector icon")//
                .putConfiguredProperty("prop1", "value1")//
                .putConfiguredProperty("unknown-prop", "unknown-value")//
                .build());

        final Connector expected = new Connector.Builder()//
            .id("connector-template:new-connector")//
            .name("new connector")//
            .description("new connector description")//
            .icon("new connector icon")//
            .connectorGroup(group)//
            .properties(connectorProperties)//
            .putConfiguredProperty("prop1", "value1")//
            .addAction(action)//
            .build();

        assertThat(created).isEqualTo(expected);
    }

    @Test
    public void shouldThrowEntityNotFoundIfNoConnectorTemplateExists() {
        assertThatThrownBy(() -> handler.create("non-existant", new Connector.Builder().build()))
            .isInstanceOf(EntityNotFoundException.class).hasMessage("Connector template: non-existant");
    }
}
