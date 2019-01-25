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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.odata.AbstractODataRouteTest;
import io.syndesis.connector.odata.PropertyBuilder;
import io.syndesis.connector.odata.component.ODataComponentFactory;
import io.syndesis.connector.odata.customizer.ODataStartCustomizer;

public abstract class AbstractODataReadRouteTest extends AbstractODataRouteTest {

    protected static final int MOCK_TIMEOUT_MILLISECONDS = 60000;
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

    public AbstractODataReadRouteTest(boolean splitResult) {
        super();
        this.splitResult = splitResult;
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

    /**
     * Creates a camel context complete with a properties component that handles
     * lookups of secret values such as passwords. Fetches the values from external
     * properties file.
     *
     * @return CamelContext
     */
    protected CamelContext createCamelContext() {
        CamelContext ctx = new SpringCamelContext(applicationContext);
        PropertiesComponent pc = new PropertiesComponent("classpath:odata-test-options.properties");
        ctx.addComponent("properties", pc);
        return ctx;
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

    protected Step createMockStep() {
        Step mockStep = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("mock")
                                .putConfiguredProperty("name", "result")
                                .build())
                    .build())
            .build();
        return mockStep;
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

    @SuppressWarnings( "unchecked" )
    protected <T> T extractJsonFromExchgMsg(MockEndpoint result, int index, Class<T> bodyClass) {
        Object body = result.getExchanges().get(index).getIn().getBody();
        assertTrue(bodyClass.isInstance(body));
        T json = (T) body;
        return json;
    }

    protected String extractJsonFromExchgMsg(MockEndpoint result, int index) {
        return extractJsonFromExchgMsg(result, index, String.class);
    }

    protected void testResult(MockEndpoint result, int exchangeIdx, String testDataFile) throws Exception {
        String json = extractJsonFromExchgMsg(result, exchangeIdx);
        String expected = testData(testDataFile);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @SuppressWarnings( "unchecked" )
    protected void testListResult(MockEndpoint result, int exchangeIdx, String... testDataFiles) throws Exception {
        List<String> json = extractJsonFromExchgMsg(result, exchangeIdx, List.class);
        assertEquals(testDataFiles.length, json.size());
        for (int i = 0; i < testDataFiles.length; ++i) {
            String expected = testData(testDataFiles[i]);
            JSONAssert.assertEquals(expected, json.get(i), JSONCompareMode.LENIENT);
        }
    }
}
