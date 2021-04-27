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
package io.syndesis.connector.odata2;

import java.io.IOException;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractODataRouteTest extends AbstractODataTest {

    protected static final String TEST_SERVER_DATA_1 = "test-server-data-1.json";
    protected static final String TEST_SERVER_DATA_1_ADDRESS = "test-server-data-1-address.json";
    protected static final String TEST_SERVER_DATA_2 = "test-server-data-2.json";
    protected static final String TEST_SERVER_DATA_1_WITH_COUNT = "test-server-data-1-with-count.json";
    protected static final String TEST_SERVER_DATA_2_WITH_COUNT = "test-server-data-2-with-count.json";
    protected static final String TEST_SERVER_DATA_EMPTY = "test-server-data-empty.json";
    protected static final String REF_SERVER_PRODUCT_DATA_1 = "ref-server-product-data-1.json";
    protected static final String REF_SERVER_PRODUCT_DATA_1_WITH_COUNT = "ref-server-product-data-1-with-count.json";
    protected static final String REF_SERVER_PRODUCT_DATA_2 = "ref-server-product-data-2.json";
    protected static final String REF_SERVER_PRODUCT_DATA_2_WITH_COUNT = "ref-server-product-data-2-with-count.json";
    protected static final String REF_SERVER_SUPPLIER_DATA_1 = "ref-server-supplier-data-1.json";
    protected static final String REF_SERVER_SUPPLIER_DATA_2 = "ref-server-supplier-data-2.json";
    protected static final String REF_SERVER_SUPPLIER_DATA_1_EXPANDED = "ref-server-supplier-data-1-expanded.json";
    protected static final String REF_SERVER_SUPPLIER_DATA_1_ADDRESS = "ref-server-supplier-data-1-address.json";
    protected static final String REF_SERVER_SUPPLIER_DATA_1_ADDRESS_STREET = "ref-server-supplier-data-1-address-street.json";

    protected final Step mockStep;

    protected final ConnectorAction connectorAction;

    protected AbstractODataRouteTest() throws Exception {
        this.mockStep = createMockStep();
        this.connectorAction = createConnectorAction();
    }

    @BeforeEach
    public void setup() {
        context = createCamelContext();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    protected abstract ConnectorAction createConnectorAction();

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

    protected String createResponseJson(HttpStatusCodes statusCode) {
        return OPEN_BRACE +
            "\"Response\"" + COLON + statusCode.getStatusCode() + COMMA +
            "\"Information\"" + COLON + SPEECH_MARK + statusCode.getInfo() + SPEECH_MARK +
        CLOSE_BRACE;
    }

    /*
     * Taken with appreciation from camel-olingo4 code found at
     * https://github.com/apache/camel/blob/master/components/camel-olingo4/camel-olingo4-component/src/test/java/org/apache/camel/component/olingo4/AbstractOlingo4TestSupport.java#L77
     * Every request to the demo OData 4.0
     * (http://services.odata.org/TripPinRESTierService) generates unique
     * service URL with postfix like (S(tuivu3up5ygvjzo5fszvnwfv)) for each
     * session This method makes request to the base URL and return URL with
     * generated postfix
     */
    protected String getRealRefServiceUrl(String baseUrl) throws ClientProtocolException, IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(baseUrl);
        HttpContext httpContext = new BasicHttpContext();
        httpclient.execute(httpGet, httpContext);
        HttpUriRequest currentReq = (HttpUriRequest)httpContext.getAttribute(HttpCoreContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost)httpContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);

        return currentReq.getURI().isAbsolute() ? currentReq.getURI().toString() : (currentHost.toURI() + currentReq.getURI());
    }
}
