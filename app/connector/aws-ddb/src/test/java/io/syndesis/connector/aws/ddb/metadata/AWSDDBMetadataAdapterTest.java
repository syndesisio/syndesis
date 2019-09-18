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
package io.syndesis.connector.aws.ddb.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.models.Swagger;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.openapi.OpenApiHelper;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.impl.DefaultCamelContext;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class AWSDDBMetadataAdapterTest {


    @Test
    public void adaptTest() throws JsonProcessingException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        AWSDDBConnectorMetaDataExtension ext = new AWSDDBConnectorMetaDataExtension(camelContext);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("element", "{\"clave\" : \":#KEY\"}");
        parameters.put("attributes", "clave, atributo");
        parameters.put("region", "EU_WEST_3");
        parameters.put("secretKey", "invalidKey");
        parameters.put("accessKey", "invalidKey");
        parameters.put("tableName", "TestTable");
        Optional<MetaData> metadata = ext.meta(parameters);
        AWSDDBMetadataRetrieval adapter = new AWSDDBMetadataRetrieval();

        ObjectWriter writer = Json.writer();
        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext,
        "aws-ddb", "io.syndesis:aws-ddb-query-connector", parameters, metadata.get());
        String actualMetadata = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);

        String expectedMetadata = "{\n" +
            "  \"inputShape\" : {\n" +
            "    \"name\" : \"Parameters\",\n" +
            "    \"description\" : \"Query parameters.\",\n" +
            "    \"kind\" : \"json-schema\",\n" +
            "    \"type\" : \"Parameters\",\n" +
            "    \"specification\" : \"{\\\"type\\\":\\\"object\\\",\\\"title\\\":\\\"Parameters\\\"," +
            "\\\"properties\\\":{\\\"{S: :#KEY,}\\\":{\\\"type\\\":\\\"string\\\"}}}\",\n" +
            "    \"metadata\" : {\n" +
            "      \"variant\" : \"element\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"outputShape\" : {\n" +
            "    \"name\" : \"Result\",\n" +
            "    \"description\" : \"Attributes on the result.\",\n" +
            "    \"kind\" : \"json-schema\",\n" +
            "    \"type\" : \"Result\",\n" +
            "    \"specification\" : \"{\\\"type\\\":\\\"object\\\",\\\"title\\\":\\\"Result\\\"," +
            "\\\"properties\\\":{\\\"clave\\\":{\\\"type\\\":\\\"string\\\"}," +
            "\\\"atributo\\\":{\\\"type\\\":\\\"string\\\"}}}\",\n" +
            "    \"metadata\" : {\n" +
            "      \"variant\" : \"collection\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        JSONAssert.assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);


    }

}
