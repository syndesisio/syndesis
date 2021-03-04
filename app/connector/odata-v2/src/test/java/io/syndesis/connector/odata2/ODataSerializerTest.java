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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.syndesis.common.model.DataShape;
import io.syndesis.connector.odata2.customizer.json.ODataEntrySerializer;
import io.syndesis.connector.odata2.meta.ODataMetaDataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@ExtendWith(SpringExtension.class)
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

    @BeforeAll
    public static void setupClass() {
        SimpleModule module = new SimpleModule(ODataEntry.class.getSimpleName(),
                                                       new Version(1, 0, 0, null, null, null));
        module.addSerializer(new ODataEntrySerializer());
        OBJECT_MAPPER.registerModule(module);
    }

    private String getMetadataSchema(String serviceURI, String resourcePath, String keyPredicate) throws Exception {
        ODataMetaDataRetrieval retrieval = new ODataMetaDataRetrieval();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(METHOD_NAME, Methods.READ.id());
        parameters.put(SERVICE_URI, serviceURI);
        parameters.put(RESOURCE_PATH, resourcePath);
        parameters.put(KEY_PREDICATE, keyPredicate);

        String componentId = "odata-v2";
        String actionId = "io.syndesis:" + Methods.READ.actionIdentifierRoot() + HYPHEN + FROM;

        SyndesisMetadata metadata = retrieval.fetch(context, componentId, actionId, parameters);
        assertNotNull(metadata);
        DataShape outputShape = metadata.outputShape;
        assertNotNull(outputShape);
        return outputShape.getSpecification();
    }

    private static void validateResultAgainstSchema(JsonNode jsonResultNode, JsonNode schemaNode) throws ProcessingException {
        String schemaURI = "http://json-schema.org/schema#";
        LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(schemaURI, schemaNode)
                .freeze();
        JsonSchema jsonSchema = JsonSchemaFactory.newBuilder()
            .setLoadingConfiguration(loadingConfiguration)
            .freeze()
            .getJsonSchema(schemaURI);

        ProcessingReport report = jsonSchema.validate(jsonResultNode);
        assertTrue(report.isSuccess());
    }

    @Test
    public void testSchemaAlignmentToResult() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();

        try {
            String serviceUrl = odataTestServer.getServiceUri();
            String resourcePath = MANUFACTURERS;
            String keyPredicate = "'1'";

            // Fetch the actual json as will be returned by the camel context
            Map<String, Object> options = Collections.emptyMap();
            Edm edm = ODataUtil.readEdm(serviceUrl, options);
            ODataEntry entry = ODataUtil.readEntry(edm, resourcePath, serviceUrl + FORWARD_SLASH +
                                                     resourcePath + OPEN_BRACKET + keyPredicate + CLOSE_BRACKET, options);

            assertNotNull(entry);
            String jsonResult = OBJECT_MAPPER.writeValueAsString(entry);
            JsonNode jsonResultNode = OBJECT_MAPPER.readTree(jsonResult);

            // Fetch the json schema as created by metadata retrieval
            String schema = getMetadataSchema(serviceUrl, resourcePath, keyPredicate);
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
