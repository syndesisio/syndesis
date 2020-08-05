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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.olingo2.Olingo2AppEndpointConfiguration;
import org.apache.camel.component.olingo2.Olingo2Component;
import org.apache.camel.main.Main;
import org.apache.http.entity.ContentType;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseOlingo2Test extends AbstractODataTest {

    private static class MyMain extends Main {

        @Override
        public CamelContext getCamelContext() {
            if (camelContext == null) {
                camelContext = createCamelContext();
            }

            return camelContext;
        }

        @Override
        protected CamelContext createCamelContext() {
            if (camelContext == null) {
                return super.createCamelContext();
            }

            return camelContext;
        }
    }

    @Test
    public void testExpectations() throws Exception {
        MyMain main = new MyMain();

        //
        // Get a context we can play with
        //
        CamelContext context = main.getCamelContext();

        //
        // Find the olingo2 component to configure
        //
        Olingo2Component component = (Olingo2Component) context.getComponent("olingo2");

        //
        // Create a configuration and apply the service url to
        // workaround the no serviceUri problem.
        //
        Olingo2AppEndpointConfiguration configuration = new Olingo2AppEndpointConfiguration();

        configuration.setContentType(ContentType.APPLICATION_XML.getMimeType());
        configuration.setServiceUri(odataTestServer.getServiceUri());

        //
        // Apply empty values to these properties so they are
        // not violated as missing
        //
        configuration.setQueryParams(new HashMap<>());
        configuration.setEndpointHttpHeaders(new HashMap<>());

        //
        // Apply the configuration to the component
        //
        component.setConfiguration(configuration);

        //
        // Apply the component to the context
        //
        context.removeComponent("olingo2");
        context.addComponent("olingo2", component);

        //
        // Apply the route and run
        //
        main.configure().addRoutesBuilder(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("olingo2://read/" + MANUFACTURERS)
                    .to("mock:result");
            }
        });

        main.start();

        /*
         * Note:
         * Annoyingly, cannot put oDataFeed in the expected body of
         * the mock:result. Although an EntitySet is returned with all the
         * correct properties and values, some of the individual entity
         * attributes are slightly different, such as names being null
         * rather than Edm.null. These different attributes do not make
         * the results wrong enough to fail the test.
         */

        MockEndpoint result = context.getEndpoint("mock:result", MockEndpoint.class);
        result.setMinimumExpectedMessageCount(1);
        result.assertIsSatisfied();

        //
        // Split is true by default hence the return of a client entity rather than an entity set
        //
        Object body = result.getExchanges().get(0).getIn().getBody();
        assertTrue(body instanceof ODataEntry);
        ODataEntry cmEntity = (ODataEntry) body;

        Map<String, Object> options = Collections.emptyMap();
        Edm edm = ODataUtil.readEdm(odataTestServer.getServiceUri(), options);
        ODataFeed oDataFeed = ODataUtil.readFeed(edm, MANUFACTURERS,odataTestServer.getServiceUri() + FORWARD_SLASH + MANUFACTURERS, options);
        ODataEntry entry = oDataFeed.getEntries().get(0);
        assertEquals(entry.getProperties(), cmEntity.getProperties());

        main.stop();
    }
}
