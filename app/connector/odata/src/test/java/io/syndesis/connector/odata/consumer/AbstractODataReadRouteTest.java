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
package io.syndesis.connector.odata.consumer;

import org.junit.After;
import org.junit.Before;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.odata.AbstractODataTest;
import io.syndesis.connector.odata.PropertyBuilder;
import io.syndesis.connector.odata.component.ODataComponentFactory;
import io.syndesis.connector.odata.customizer.ODataStartCustomizer;

public abstract class AbstractODataReadRouteTest extends AbstractODataTest {

    protected static final String TEST_SERVER_DATA_1 = "test-server-data-1.json";
    protected static final String TEST_SERVER_DATA_2 = "test-server-data-2.json";
    protected static final String TEST_SERVER_DATA_3 = "test-server-data-3.json";
    protected static final String TEST_SERVER_DATA_1_WITH_COUNT = "test-server-data-1-with-count.json";
    protected static final String TEST_SERVER_DATA_2_WITH_COUNT = "test-server-data-2-with-count.json";
    protected static final String TEST_SERVER_DATA_3_WITH_COUNT = "test-server-data-3-with-count.json";
    protected static final String REF_SERVER_PEOPLE_DATA_1 = "ref-server-people-data-1.json";
    protected static final String REF_SERVER_PEOPLE_DATA_2 = "ref-server-people-data-2.json";
    protected static final String REF_SERVER_PEOPLE_DATA_3 = "ref-server-people-data-3.json";
    protected static final String TEST_SERVER_DATA_EMPTY = "test-server-data-empty.json";

    private final boolean splitResult;

    protected final ConnectorAction readAction;

    protected final Step mockStep;

    public AbstractODataReadRouteTest(boolean splitResult) throws Exception {
        super();
        this.splitResult = splitResult;
        readAction = createReadAction();
        mockStep = createMockStep();
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

    protected boolean isSplitResult() {
        return splitResult;
    }

    @Override
    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder) {
        configurePropBuilder.property(SPLIT_RESULT, Boolean.toString(isSplitResult()));
        return super.createODataConnector(configurePropBuilder);
    }

    @Override
    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder,
                                             PropertyBuilder<ConfigurationProperty> propBuilder) {
        configurePropBuilder.property(SPLIT_RESULT, Boolean.toString(isSplitResult()));
        return super.createODataConnector(configurePropBuilder, propBuilder);
    }

    protected ConnectorAction createReadAction() throws Exception {
        ConnectorAction odataAction = new ConnectorAction.Builder()
            .description("Read a resource from the server")
             .id("io.syndesis:odata-read-connector")
             .name("Read")
             .descriptor(new ConnectorDescriptor.Builder()
                        .componentScheme("olingo4")
                        .putConfiguredProperty("apiName", "read")
                        .addConnectorCustomizer(ODataStartCustomizer.class.getName())
                        .connectorFactory(ODataComponentFactory.class.getName())
                        .outputDataShape(new DataShape.Builder()
                                         .kind(DataShapeKinds.JSON_INSTANCE)
                                         .build())
                        .build())
            .build();
        return odataAction;
    }

    protected Step.Builder odataStepBuilder(Connector odataConnector) {
        Step.Builder odataStepBuilder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(readAction)
            .connection(
                            new Connection.Builder()
                                .connector(odataConnector)
                                .build());
        return odataStepBuilder;
    }

    protected Step createODataStep(Connector odataConnector, String methodName) {
        return odataStepBuilder(odataConnector)
                                        .putConfiguredProperty(METHOD_NAME, methodName)
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
}
