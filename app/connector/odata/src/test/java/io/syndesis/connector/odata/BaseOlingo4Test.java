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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.olingo4.Olingo4AppEndpointConfiguration;
import org.apache.camel.component.olingo4.Olingo4Component;
import org.apache.camel.main.Main;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.core.ODataClientFactory;
import org.junit.Test;

public class BaseOlingo4Test extends AbstractODataTest {

    private static class MyRouteBuilder extends RouteBuilder {

        private String olingoURI;

        public MyRouteBuilder(String olingoURI) {
            this.olingoURI = olingoURI;
        }

        @Override
        public void configure() throws Exception {
            from(olingoURI)
                .to("mock:result");
        }
    }

    private static class MyMain extends Main {

        CamelContext context;

        public void setCamelContext(CamelContext context) {
            this.context = context;
        }

        @Override
        protected CamelContext createContext() {
            if (context == null) {
                return super.createContext();
            }

            return context;
        }
    }

    @Test
    public void testExpectations() throws Exception {
        URI httpURI = URI.create(defaultTestServer.servicePlainUri() + FORWARD_SLASH + defaultTestServer.resourcePath());
        String camelURI = "olingo4://read/" + defaultTestServer.resourcePath();

        //
        // Create own main class to allow for setting the context
        //
        MyMain main = new MyMain();

        //
        // Get a context we can play with
        //
        CamelContext context = main.getOrCreateCamelContext();

        //
        // Find the olingo4 component to configure
        //
        Olingo4Component component = (Olingo4Component) context.getComponent("olingo4");

        //
        // Create a configuration and apply the sevice url to
        // workaround the no serviceUri problem.
        //
        Olingo4AppEndpointConfiguration configuration = new Olingo4AppEndpointConfiguration();

        //
        // Override the ACCEPT header since it does not take account of the odata.metadata parameter
        //
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put(HttpHeaders.ACCEPT, "application/json;odata.metadata=full,application/xml,*/*");
        configuration.setHttpHeaders(httpHeaders);

        configuration.setServiceUri(defaultTestServer.servicePlainUri());

        //
        // Apply empty values to these properties so they are
        // not violated as missing
        //
        configuration.setQueryParams(new HashMap<>());
        configuration.setEndpointHttpHeaders(new HashMap<>());

        //
        // Apply the configurtion to the component
        //
        component.setConfiguration(configuration);

        //
        // Apply the component to the context
        //
        context.removeComponent("olingo4");
        context.addComponent("olingo4", component);

        //
        // Apply the context to main
        //
        main.setCamelContext(context);

        //
        // Apply the route and run
        //
        main.addRouteBuilder(new MyRouteBuilder(camelURI));

        try {
            ClientEntitySet olEntitySet = null;
            ODataRetrieveResponse<ClientEntitySet> response = null;
            try {
                ODataClient client = ODataClientFactory.getClient();
                response = client.getRetrieveRequestFactory().getEntitySetRequest(httpURI).execute();
                assertEquals(HttpStatus.SC_OK, response.getStatusCode());

                olEntitySet = response.getBody();
                assertNotNull(olEntitySet);
            } finally {
                if (response != null) {
                    response.close();
                }
            }

            main.start();

            /*
             * Note:
             * Annoyingly, cannot put olEntitySet in the expected body of
             * the mock:result. Although an EntitySet is returned with all the
             * correct properties and values, some of the individual entity
             * attributes are slightly different, such as names being null
             * rather than Edm.null. These different attributes do not make
             * the results wrong enough to fail the test.
             */

            MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
            result.setMinimumExpectedMessageCount(1);
            result.assertIsSatisfied();

            Object body = result.getExchanges().get(0).getIn().getBody();
            assertTrue(body instanceof ClientEntity);
            ClientEntity cmEntity = (ClientEntity) body;

            ClientEntity olEntity = olEntitySet.getEntities().get(0);
            assertEquals(olEntity.getProperties(), cmEntity.getProperties());

        } finally {
            main.stop();
        }
    }
}
