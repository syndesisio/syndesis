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

import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.model.connection.ConnectorTemplate;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectorSettingsHandlerTest {

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final DataManager dataManager = mock(DataManager.class);

    @Test
    public void shouldProvideInfoAboutAppliedConnectorSettings() {
        final ConnectorSettingsHandler handler = new ConnectorSettingsHandler("connector-template", dataManager, applicationContext);
        final ConnectorGenerator connectorGenerator = mock(ConnectorGenerator.class);

        final ConnectorTemplate template = new ConnectorTemplate.Builder().build();
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().build();
        final Connector connector = new Connector.Builder().build();

        when(dataManager.fetch(ConnectorTemplate.class, "connector-template")).thenReturn(template);
        when(applicationContext.getBean("connector-template", ConnectorGenerator.class)).thenReturn(connectorGenerator);
        when(connectorGenerator.info(same(template), same(connectorSettings))).thenReturn(connector);

        final Connector connectorInfo = handler.info(connectorSettings);

        assertThat(connectorInfo).isSameAs(connector);
    }

}
