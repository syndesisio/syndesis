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
package io.syndesis.connector.odata.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

public class ODataMetaDataRetrievalTest implements ODataConstants {

    private CamelContext context;

    @Before
    public void setup() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    @Test
    public void testMetaDataRetrieval() throws Exception {
        CamelContext context = new DefaultCamelContext();
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();

        final ODataTestServer server = new ODataTestServer();
        server.start();

        String methodValue = "Products";

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(API_NAME, "read");
            parameters.put(SERVICE_URI, server.serviceUrl());
            parameters.put(METHOD_NAME, methodValue);

            String componentId = "odata";
            String actionId = "io.syndesis:odata-read-connector";

            SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
            assertNotNull(metadata);

            Map<String, List<PropertyPair>> properties = metadata.properties;
            assertFalse(properties.isEmpty());

            //
            // The method names are important for collecting prior
            // to the filling in of the integration step (values such as resource etc...)
            //
            List<PropertyPair> methodNames = properties.get(METHOD_NAME);
            assertNotNull(methodNames);
            assertFalse(methodNames.isEmpty());

            PropertyPair pair = methodNames.get(0);
            assertNotNull(pair);
            assertEquals(methodValue, pair.getValue());

            //
            // The out data shape is defined after the integration step has
            // been populated and should be a dynamic json-schema based
            // on the contents of the OData Edm metadata object.
            //
            DataShape outputShape = metadata.outputShape;
            assertNotNull(outputShape);

            assertEquals(DataShapeKinds.JSON_SCHEMA, outputShape.getKind());
            assertNotNull(outputShape.getSpecification());

            ObjectSchema schema = Json.copyObjectMapperConfiguration().readValue(
                                                                             outputShape.getSpecification(), ObjectSchema.class);
            Map<String, JsonSchema> propSchemaMap = schema.getProperties();
            assertNotNull(propSchemaMap);

            JsonSchema descSchema = propSchemaMap.get("Description");
            assertNotNull(descSchema);
            assertNotNull(propSchemaMap.get("ID"));
            assertNotNull(propSchemaMap.get("Name"));

            JsonFormatTypes descType = descSchema.getType();
            assertNotNull(descType);
            assertEquals(JsonFormatTypes.STRING, descType);
            assertEquals(false, descSchema.getRequired());
        } finally {
            server.stop();
        }
    }
}
