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
import java.util.Iterator;
import java.util.Map;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.http.HttpStatus;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.domain.ClientCollectionValueImpl;
import org.apache.olingo.client.core.domain.ClientComplexValueImpl;
import org.apache.olingo.client.core.domain.ClientEntityImpl;
import org.apache.olingo.client.core.domain.ClientEnumValueImpl;
import org.apache.olingo.client.core.domain.ClientPrimitiveValueImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.syndesis.common.model.DataShape;
import io.syndesis.connector.odata.customizer.json.ClientCollectionValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientComplexValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientEntitySerializer;
import io.syndesis.connector.odata.customizer.json.ClientEnumValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientPrimitiveValueSerializer;
import io.syndesis.connector.odata.meta.ODataMetaDataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataSerializerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@TestExecutionListeners(
    listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
    }
)
public class ODataSerializerTest extends AbstractODataTest {

    private static final String TEST_COLLECTION = "test-collection.json";
    private static final String TEST_COMPLEX = "test-complex.json";
    private static final String TEST_ENUM = "test-enum.json";

    @BeforeClass
    public static void setupClass() {
        SimpleModule module = new SimpleModule(ClientEntitySet.class.getSimpleName(),
                                                       new Version(1, 0, 0, null, null, null));
        module
            .addSerializer(new ClientEntitySerializer())
            .addSerializer(new ClientPrimitiveValueSerializer())
            .addSerializer(new ClientEnumValueSerializer())
            .addSerializer(new ClientCollectionValueSerializer())
            .addSerializer(new ClientComplexValueSerializer());
        OBJECT_MAPPER.registerModule(module);
    }

    private void checkEntity(ClientEntity entity, String testDataFile) throws Exception {
        String json = OBJECT_MAPPER.writeValueAsString(entity);
        String expected = testData(testDataFile);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    public void testSerializerCollection() throws Exception {
        ClientEntity entity = new ClientEntityImpl(null);
        ClientPrimitiveValue test1 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test1");
        ClientPrimitiveValue test2 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test2");
        ClientPrimitiveValue test3 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test3");

        ClientCollectionValueImpl<ClientPrimitiveValue> collValue = new ClientCollectionValueImpl<ClientPrimitiveValue>("testCollection");
        collValue.add(test1);
        collValue.add(test2);
        collValue.add(test3);

        ClientProperty prop = new ClientPropertyImpl("testCollection", collValue);
        entity.getProperties().add(prop);

        checkEntity(entity, TEST_COLLECTION);
    }

    @Test
    public void testSerializerComplex() throws Exception {
        ClientEntity entity = new ClientEntityImpl(null);
        ClientPrimitiveValue test1 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test1");
        ClientPrimitiveValue test2 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test2");
        ClientPrimitiveValue test3 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test3");

        ClientComplexValueImpl complexValue = new ClientComplexValueImpl("testComplex");
        complexValue.add(new ClientPropertyImpl("test1", test1));
        complexValue.add(new ClientPropertyImpl("test2", test2));
        complexValue.add(new ClientPropertyImpl("test3", test3));
        entity.getProperties().add(new ClientPropertyImpl("testComplex", complexValue));

        checkEntity(entity, TEST_COMPLEX);
    }

    @Test
    public void testSerializerEnum() throws Exception {
        ClientEntity entity = new ClientEntityImpl(
                                                   new FullQualifiedName("Microsoft.OData.Service.Sample.TrippinInMemory.Models.Person"));
        ClientPropertyImpl enumProperty = new ClientPropertyImpl("Gender",
                                                                 new ClientEnumValueImpl(
                                                                                         "Microsoft.OData.Service.Sample.TrippinInMemory.Models.PersonGender",
                                                                                         "Male"));
        entity.getProperties().add(enumProperty);
        checkEntity(entity, TEST_ENUM);
    }

    private String fetchReferenceEntity(String uri) throws Exception {
        URI httpURI = URI.create(uri);
        ClientEntity olEntity = null;
        ODataRetrieveResponse<ClientEntity> response = null;
        try {
            ODataClient client = ODataClientFactory.getClient();
            response = client.getRetrieveRequestFactory().getEntityRequest(httpURI).execute();
            assertEquals(HttpStatus.SC_OK, response.getStatusCode());

            olEntity = response.getBody();
            assertNotNull(olEntity);
            return OBJECT_MAPPER.writeValueAsString(olEntity);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String getMetadataSchema(String serviceURI, String resourcePath, String keyPredicate) throws Exception {
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_NAME, Methods.READ.id());
        parameters.put(SERVICE_URI, serviceURI);
        parameters.put(RESOURCE_PATH, resourcePath);
        parameters.put(KEY_PREDICATE, keyPredicate);

        String componentId = "odata";
        String actionId = "io.syndesis:" + Methods.READ.actionIdentifierRoot() + HYPHEN + FROM;

        SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
        assertNotNull(metadata);
        DataShape outputShape = metadata.outputShape;
        assertNotNull(outputShape);
        return outputShape.getSpecification();
    }

    private void validateResultAgainstSchema(JsonNode jsonResultNode, JsonNode schemaNode) throws ProcessingException {
        String schemaURI = "http://json-schema.org/schema#";
        LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(schemaURI, schemaNode)
                .freeze();
        JsonSchema jsonSchema = JsonSchemaFactory.newBuilder()
            .setLoadingConfiguration(loadingConfiguration)
            .freeze()
            .getJsonSchema(schemaURI);

        ProcessingReport report = jsonSchema.validate(jsonResultNode);
        Iterator<ProcessingMessage> msgIter = report.iterator();
        assertTrue(report.isSuccess());
    }

    @Test
    public void testSchemaAlignmentToResult() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();

        try {
            String serviceURI = defaultTestServer.servicePlainUri();
            String resourcePath = defaultTestServer.resourcePath();
            String keyPredicate = "1";

            // Fetch the actual json as will be returned by the camel context
            String jsonResult = fetchReferenceEntity(
                                                     serviceURI + FORWARD_SLASH +
                                                     resourcePath + OPEN_BRACKET +
                                                     keyPredicate + CLOSE_BRACKET);
            JsonNode jsonResultNode = OBJECT_MAPPER.readTree(jsonResult);

            // Fetch the json schema as created by metadata retrieval
            String schema = getMetadataSchema(serviceURI, resourcePath, keyPredicate);
            JsonNode schemaNode = OBJECT_MAPPER.readTree(schema);

            // Validate the result against the schema
            validateResultAgainstSchema(jsonResultNode, schemaNode);

        } finally {
            if (context != null) {
                context.stop();
                context = null;
            }
        }
    }
}
