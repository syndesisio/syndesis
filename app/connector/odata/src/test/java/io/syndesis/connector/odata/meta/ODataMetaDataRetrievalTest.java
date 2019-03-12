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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ContainerTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.odata.AbstractODataTest;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

public class ODataMetaDataRetrievalTest extends AbstractODataTest {

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

    private Map<String, JsonSchema> checkShape(DataShape dataShape, Class<? extends ContainerTypeSchema> expectedShapeClass) throws IOException, JsonParseException, JsonMappingException {
        assertNotNull(dataShape);

        assertEquals(DataShapeKinds.JSON_SCHEMA, dataShape.getKind());
        assertNotNull(dataShape.getSpecification());

        ContainerTypeSchema schema = Json.copyObjectMapperConfiguration().readValue(
                                            dataShape.getSpecification(), expectedShapeClass);

        Map<String, JsonSchema> propSchemaMap = null;
        if (schema instanceof ArraySchema) {
            propSchemaMap = ((ArraySchema) schema).getItems().asSingleItems().getSchema().asObjectSchema().getProperties();
        } else if (schema instanceof ObjectSchema) {
            propSchemaMap = ((ObjectSchema) schema).getProperties();
        }

        assertNotNull(propSchemaMap);
        return propSchemaMap;
    }

    private void checkTestServerSchemaMap(Map<String, JsonSchema> schemaMap) {
        JsonSchema descSchema = schemaMap.get("Description");
        assertNotNull(descSchema);
        assertNotNull(schemaMap.get("ID"));
        assertNotNull(schemaMap.get("Name"));

        JsonFormatTypes descType = descSchema.getType();
        assertNotNull(descType);
        assertEquals(JsonFormatTypes.STRING, descType);
        assertEquals(false, descSchema.getRequired());
    }

    @Test
    public void testReadMetaDataRetrieval() throws Exception {
        CamelContext context = new DefaultCamelContext();
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();

        String resourcePath = "Products";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_NAME, Methods.READ.id());
        parameters.put(SERVICE_URI, defaultTestServer.servicePlainUri());
        parameters.put(RESOURCE_PATH, resourcePath);

        String componentId = "odata";
        String actionId = "io.syndesis:" + Methods.READ.connectorId();

        SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
        assertNotNull(metadata);

        Map<String, List<PropertyPair>> properties = metadata.properties;
        assertFalse(properties.isEmpty());

        //
        // The method names are important for collecting prior
        // to the filling in of the integration step (values such as resource etc...)
        //
        List<PropertyPair> resourcePaths = properties.get(RESOURCE_PATH);
        assertNotNull(resourcePaths);
        assertFalse(resourcePaths.isEmpty());

        PropertyPair pair = resourcePaths.get(0);
        assertNotNull(pair);
        assertEquals(resourcePath, pair.getValue());

        //
        // The out data shape is defined after the integration step has
        // been populated and should be a dynamic json-schema based
        // on the contents of the OData Edm metadata object.
        //
        checkTestServerSchemaMap(checkShape(metadata.outputShape, ArraySchema.class));
    }

    @Test
    public void testReadMetaDataRetrievalWithSplit() throws Exception {
        CamelContext context = new DefaultCamelContext();
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();

        String resourcePath = "Products";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_NAME, Methods.READ.id());
        parameters.put(SERVICE_URI, defaultTestServer.servicePlainUri());
        parameters.put(RESOURCE_PATH, resourcePath);
        parameters.put(SPLIT_RESULT, true);

        String componentId = "odata";
        String actionId = "io.syndesis:" + Methods.READ.connectorId();

        SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
        assertNotNull(metadata);

        Map<String, List<PropertyPair>> properties = metadata.properties;
        assertFalse(properties.isEmpty());

        //
        // The method names are important for collecting prior
        // to the filling in of the integration step (values such as resource etc...)
        //
        List<PropertyPair> resourcePaths = properties.get(RESOURCE_PATH);
        assertNotNull(resourcePaths);
        assertFalse(resourcePaths.isEmpty());

        PropertyPair pair = resourcePaths.get(0);
        assertNotNull(pair);
        assertEquals(resourcePath, pair.getValue());

        //
        // The out data shape is defined after the integration step has
        // been populated and should be a dynamic json-schema based
        // on the contents of the OData Edm metadata object.
        //
        // Split causes it to be an ObjectSchema rather than an ArraySchema
        //
        checkTestServerSchemaMap(checkShape(metadata.outputShape, ObjectSchema.class));
    }

    @Test
    public void testCreateMetaDataRetrieval() throws Exception {
        CamelContext context = new DefaultCamelContext();
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();

        String resourcePath = "Products";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_NAME, Methods.CREATE.id());
        parameters.put(SERVICE_URI, defaultTestServer.servicePlainUri());
        parameters.put(RESOURCE_PATH, resourcePath);

        String componentId = "odata";
        String actionId = "io.syndesis:" + Methods.CREATE.connectorId();

        SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
        assertNotNull(metadata);

        Map<String, List<PropertyPair>> properties = metadata.properties;
        assertFalse(properties.isEmpty());

        //
        // The method names are important for collecting prior
        // to the filling in of the integration step (values such as resource etc...)
        //
        List<PropertyPair> resourcePaths = properties.get(RESOURCE_PATH);
        assertNotNull(resourcePaths);
        assertFalse(resourcePaths.isEmpty());

        PropertyPair pair = resourcePaths.get(0);
        assertNotNull(pair);
        assertEquals(resourcePath, pair.getValue());

        //
        // Both data shapes are defined after the integration step has
        // been populated and should be dynamic json-schema based
        // on the contents of the OData Edm metadata object.
        //
        checkTestServerSchemaMap(checkShape(metadata.inputShape, ObjectSchema.class));
        checkTestServerSchemaMap(checkShape(metadata.outputShape, ObjectSchema.class));
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testReadMetaDataRetrievalSSL() throws Exception {
        CamelContext context = new DefaultCamelContext();
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();

        String resourcePath = "Products";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_NAME, Methods.READ.id());

        parameters.put(SERVICE_URI, sslTestServer.serviceSSLUri());
        parameters.put(RESOURCE_PATH, resourcePath);
        // Provide the server's SSL certificate to allow client handshake
        parameters.put(SERVER_CERTIFICATE, ODataTestServer.serverCertificate());

        String componentId = "odata";
        String actionId = "io.syndesis:" + Methods.READ.connectorId();

        SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
        assertNotNull(metadata);

        Map<String, List<PropertyPair>> properties = metadata.properties;
        assertFalse(properties.isEmpty());

        //
        // The method names are important for collecting prior
        // to the filling in of the integration step (values such as resource etc...)
        //
        List<PropertyPair> resourcePaths = properties.get(RESOURCE_PATH);
        assertNotNull(resourcePaths);
        assertFalse(resourcePaths.isEmpty());

        PropertyPair pair = resourcePaths.get(0);
        assertNotNull(pair);
        assertEquals(resourcePath, pair.getValue());

        //
        // The out data shape is defined after the integration step has
        // been populated and should be a dynamic json-schema based
        // on the contents of the OData Edm metadata object.
        //
        Map<String, JsonSchema> schemaMap = checkShape(metadata.outputShape, ArraySchema.class);
        checkTestServerSchemaMap(schemaMap);
    }

    @Test
        public void testReadMetaDataRetrievalReferenceServerSSL() throws Exception {
            CamelContext context = new DefaultCamelContext();
            ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();

            String resourcePath = "Airlines";

            Map<String, Object> parameters = new HashMap<>();
            parameters.put(METHOD_NAME, Methods.READ.id());
            parameters.put(SERVICE_URI, REF_SERVICE_URI);
            parameters.put(RESOURCE_PATH, resourcePath);

            String componentId = "odata";
            String actionId = "io.syndesis:" + Methods.READ.connectorId();

            SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
            assertNotNull(metadata);

            Map<String, List<PropertyPair>> properties = metadata.properties;
            assertFalse(properties.isEmpty());

            //
            // The method names are important for collecting prior
            // to the filling in of the integration step (values such as resource etc...)
            //
            List<PropertyPair> resourcePaths = properties.get(RESOURCE_PATH);
            assertNotNull(resourcePaths);
            assertFalse(resourcePaths.isEmpty());

            PropertyPair pair = resourcePaths.get(0);
            assertNotNull(pair);
            assertEquals(resourcePath, pair.getValue());

            //
            // The out data shape is defined after the integration step has
            // been populated and should be a dynamic json-schema based
            // on the contents of the OData Edm metadata object.
            //
            Map<String, JsonSchema> schemaMap = checkShape(metadata.outputShape, ArraySchema.class);
            assertNotNull(schemaMap.get("Name"));
            assertNotNull(schemaMap.get("AirlineCode"));
        }
}
