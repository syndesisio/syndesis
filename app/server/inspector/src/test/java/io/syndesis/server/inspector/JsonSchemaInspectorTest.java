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
package io.syndesis.server.inspector;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.util.IOStreams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaInspectorTest {

    private final String JSON_SCHEMA_KIND = "json-schema";
    private final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";
    private JsonSchemaInspector inspector = new JsonSchemaInspector();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCollectPathsFromJsonSchema() throws IOException {
        final List<String> paths = inspector.getPaths(JSON_SCHEMA_KIND, "", IOStreams.readText(getSalesForceContactSchema()), Optional.empty());
        assertSalesforceContactProperties(paths);
        assertThat(paths).doesNotContainAnyElementsOf(JsonSchemaInspector.COLLECTION_PATHS);
    }

    @Test
    public void shouldCollectPathsFromJsonArraysSchema() throws IOException {
        final List<String> paths = inspector.getPaths(JSON_SCHEMA_KIND, "", getSalesForceContactArraySchema(), Optional.empty());
        assertSalesforceContactArrayProperties(paths);
        assertThat(paths).containsAll(JsonSchemaInspector.COLLECTION_PATHS);
    }

    @Test
    public void shouldFetchPathsFromJsonSchema() throws IOException {
        final ObjectSchema schema = mapper.readValue(getSalesForceContactSchema(), ObjectSchema.class);

        final List<String> paths = new ArrayList<>();
        JsonSchemaInspector.fetchPaths(null, paths, schema.getProperties());
        assertSalesforceContactProperties(paths);
    }

    @Test
    public void shouldFetchPathsWithNestedArraySchema() throws IOException {
        final ObjectSchema schema = mapper.readValue(getSchemaWithNestedArray(), ObjectSchema.class);

        final List<String> paths = new ArrayList<>();
        JsonSchemaInspector.fetchPaths(null, paths, schema.getProperties());

        assertThat(paths).hasSize(4);
        assertThat(paths).containsAll(Arrays.asList("Id", "PhoneNumbers.size()", "PhoneNumbers[].Name", "PhoneNumbers[].Number"));
    }

    private InputStream getSalesForceContactSchema() {
        return JsonSchemaInspectorTest.class.getResourceAsStream("/salesforce.Contact.jsonschema");
    }

    private String getSalesForceContactArraySchema() throws IOException {
        return "{\"type\":\"array\",\"$schema\":\"" + JSON_SCHEMA_ORG_SCHEMA + "\",\"items\":" + IOStreams.readText(getSalesForceContactSchema()) + "}";
    }

    private String getSchemaWithNestedArray() {
        return "{" +
                    "\"type\":\"object\"," +
                "\"$schema\":\"" + JSON_SCHEMA_ORG_SCHEMA + "\", " +
                    "\"properties\": { " +
                            "\"Id\": { \"type\": \"string\",\"required\": true }, " +
                            "\"PhoneNumbers\": {" +
                                    "\"type\": \"array\"," +
                                    "\"items\": {" +
                                        "\"type\":\"object\", " +
                                        "\"properties\": { " +
                                                "\"Name\": { \"type\": \"string\",\"required\": true }, " +
                                                "\"Number\": { \"type\": \"string\",\"required\": true } " +
                                        "}" +
                                    "}" +
                            "}" +
                    "}" +
                "}";
    }

    private void assertSalesforceContactProperties(List<String> paths) {
        assertSalesforceContactProperties(paths, null);
    }

    private void assertSalesforceContactArrayProperties(List<String> paths) {
        assertSalesforceContactProperties(paths, "[]");
    }

    private void assertSalesforceContactProperties(List<String> paths, String context) {
        List<String> expectedPaths = Arrays.asList("Id",
                "IsDeleted", "MasterRecordId", "AccountId", "LastName",
                "FirstName", "OtherAddress.latitude", "MailingAddress.city");

        assertThat(paths).containsAll(expectedPaths.stream()
                                                    .map(item -> Optional.ofNullable(context).map(path -> path + ".").orElse("") + item)
                                                    .collect(Collectors.toList()));
    }
}
