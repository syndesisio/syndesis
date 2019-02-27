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
package io.syndesis.connector.odata;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.After;
import org.junit.Before;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;

public abstract class AbstractODataRouteTest extends AbstractODataTest {

    protected final Step mockStep;

    protected final ConnectorAction connectorAction;

    public AbstractODataRouteTest() throws Exception {
        super();
        this.mockStep = createMockStep();
        this.connectorAction = createConnectorAction();
    }

    @Before
    public void setup() throws Exception {
        context = createCamelContext();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    protected abstract ConnectorAction createConnectorAction() throws Exception;

    protected Step.Builder odataStepBuilder(Connector odataConnector) {
        Step.Builder odataStepBuilder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(connectorAction)
            .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build());
        return odataStepBuilder;
    }

    protected Step createODataStep(Connector odataConnector, String resourcePath) {
        return odataStepBuilder(odataConnector)
                                        .putConfiguredProperty(RESOURCE_PATH, resourcePath)
                                        .build();
    }

    /**
     * Generates a {@link ConfigurationProperty} for the basic password
     * mimicking the secret operations conducted for real openshift passwords.
     * The actual password is fetched from the camel context's properties component.
     * The defaultValue is just a placeholder as it is checked for non-nullability.
     */
    protected ConfigurationProperty basicPasswordProperty() {
        return new ConfigurationProperty.Builder()
              .secret(Boolean.TRUE)
              .defaultValue(BASIC_PASSWORD)
              .build();
    }

    protected String createResponseJson(HttpStatusCode statusCode) {
        return OPEN_BRACE +
            "\"Response\"" + COLON + statusCode.getStatusCode() + COMMA +
            "\"Information\"" + COLON + SPEECH_MARK + statusCode.getInfo() + SPEECH_MARK +
        CLOSE_BRACE;
    }

}
