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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.util.Json;
import io.syndesis.connector.aws.ddb.AWSDDBConfiguration;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.impl.DefaultCamelContext;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

public class AWSDDBMetadataAdapterTest {

    @Test
    public void adaptTest() throws JsonProcessingException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        AWSDDBConnectorMetaDataExtension ext = new AWSDDBConnectorMetaDataExtension(camelContext);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("element", "{\"clave\" : \":#KEY\"}");
        parameters.put("attributes", "clave, atributo");
        parameters.put(AWSDDBConfiguration.REGION, AWSDDBConfiguration.REGION_VALUE);
        parameters.put(AWSDDBConfiguration.SECRETKEY, AWSDDBConfiguration.SECRETKEY_VALUE);
        parameters.put(AWSDDBConfiguration.ACCESSKEY, AWSDDBConfiguration.ACCESSKEY_VALUE);
        parameters.put(AWSDDBConfiguration.TABLENAME, AWSDDBConfiguration.TABLENAME_VALUE);
        Optional<MetaData> metadata = ext.meta(parameters);
        AWSDDBMetadataRetrieval adapter = new AWSDDBMetadataRetrieval() {
            @Override
            DescribeTableResult fetchTableDescription(Map<String, Object> properties) {
                DescribeTableResult tableDescriptionResult = new DescribeTableResult();
                TableDescription tableDescription = new TableDescription();
                tableDescription.setAttributeDefinitions(Collections.singletonList(new AttributeDefinition("clave", ScalarAttributeType.S)));

                tableDescriptionResult.setTable(tableDescription);

                return tableDescriptionResult;
            }
        };

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
            "\\\"properties\\\":{\\\":#KEY\\\":{\\\"type\\\":\\\"string\\\"}}}\",\n" +
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
            "  },\n" +
            "  \"properties\" : {\n" +
            "    \"attributes\" : [ {\n" +
            "      \"displayValue\" : \"clave\",\n" +
            "      \"value\" : \"clave\"\n" +
            "    } ],\n" +
            "    \"element\" : [ {\n" +
            "      \"displayValue\" : \"{\\\"clave\\\" : \\\"S\\\"}\",\n" +
            "      \"value\" : \"{\\\"clave\\\" : \\\"S\\\"}\"\n" +
            "    } ]\n" +
            "  }\n" +
            "}";

        JSONAssert.assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);
    }


    @Test
    public void adaptRemovalTest() throws JsonProcessingException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        AWSDDBConnectorMetaDataExtension ext = new AWSDDBConnectorMetaDataExtension(camelContext);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("element", "{\"clave\" : \":#KEY\"}");
        parameters.put("region", "EU_WEST_3");
        parameters.put("secretKey", "invalidKey");
        parameters.put("accessKey", "invalidKey");
        parameters.put("tableName", "TestTable");
        Optional<MetaData> metadata = ext.meta(parameters);
        AWSDDBMetadataRetrieval adapter = new AWSDDBMetadataRetrieval() {
            @Override
            DescribeTableResult fetchTableDescription(Map<String, Object> properties) {
                DescribeTableResult tableDescriptionResult = new DescribeTableResult();
                TableDescription tableDescription = new TableDescription();
                tableDescription.setAttributeDefinitions(Collections.emptyList());

                tableDescriptionResult.setTable(tableDescription);

                return tableDescriptionResult;
            }
        };

        ObjectWriter writer = Json.writer();
        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext,
            "aws-ddb", "io.syndesis:aws-ddb-removeitem-to-connector", parameters, metadata.get());
        String actualMetadata = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);

        String expectedMetadata = "{\n" +
            "  \"inputShape\" : {\n" +
            "    \"name\" : \"Parameters\",\n" +
            "    \"description\" : \"Query parameters.\",\n" +
            "    \"kind\" : \"json-schema\",\n" +
            "    \"type\" : \"Parameters\",\n" +
            "    \"specification\" : \"{\\\"type\\\":\\\"object\\\",\\\"title\\\":\\\"Parameters\\\"," +
            "\\\"properties\\\":{\\\":#KEY\\\":{\\\"type\\\":\\\"string\\\"}}}\",\n" +
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
            "\\\"properties\\\":{\\\"clave\\\":{\\\"type\\\":\\\"string\\\"}}}\",\n" +
            "    \"metadata\" : {\n" +
            "      \"variant\" : \"collection\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"properties\" : {\n" +
            "    \"attributes\" : [ { } ],\n" +
            "    \"element\" : [ {\n" +
            "      \"displayValue\" : \"{}\",\n" +
            "      \"value\" : \"{}\"\n" +
            "    } ]\n" +
            "  }\n" +
            "}\n";

        JSONAssert.assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);
    }

}
