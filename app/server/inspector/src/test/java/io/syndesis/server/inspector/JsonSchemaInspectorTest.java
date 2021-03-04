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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.util.IOStreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaInspectorTest {

    private static final String JSON_SCHEMA_KIND = "json-schema";
    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";
    private final JsonSchemaInspector inspector = new JsonSchemaInspector();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCollectPathsFromJsonSchema() throws IOException {
        final List<String> paths = inspector.getPaths(JSON_SCHEMA_KIND, "", IOStreams.readText(getSalesForceContactSchema()), Optional.empty());
        assertSalesforceContactProperties(paths);
        assertThat(paths).doesNotContainAnyElementsOf(JsonSchemaInspector.COLLECTION_PATHS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/petstore-unified-schema.json",
                 "/petstore-unified-schema-draft-4.json",
                 "/petstore-unified-schema-draft-6.json"})
    public void shouldCollectPathsFromUnifiedJsonSchema(final String schemaPath) throws IOException {
        final List<String> paths = inspector.getPaths(JSON_SCHEMA_KIND, "", IOStreams.readText(getPetstoreUnifiedSchema(schemaPath)), Optional.empty());
        assertPetstoreProperties(paths);
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

    private static InputStream getPetstoreUnifiedSchema(String schemaPath) {
        return JsonSchemaInspectorTest.class.getResourceAsStream(schemaPath);
    }

    private static InputStream getSalesForceContactSchema() {
        return JsonSchemaInspectorTest.class.getResourceAsStream("/salesforce.Contact.jsonschema");
    }

    private static String getSalesForceContactArraySchema() throws IOException {
        return "{\"type\":\"array\",\"$schema\":\"" + JSON_SCHEMA_ORG_SCHEMA + "\",\"items\":" + IOStreams.readText(getSalesForceContactSchema()) + "}";
    }

    private static String getSchemaWithNestedArray() {
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

    private static void assertSalesforceContactProperties(List<String> paths) {
        assertSalesforceContactProperties(paths, null);
    }

    private static void assertSalesforceContactArrayProperties(List<String> paths) {
        assertSalesforceContactProperties(paths, "[]");
    }

    private static void assertSalesforceContactProperties(List<String> paths, String context) {
        List<String> expectedPaths = Arrays.asList("Id",
                "IsDeleted", "MasterRecordId", "AccountId", "LastName",
                "FirstName", "OtherAddress.latitude", "MailingAddress.city");

        assertThat(paths).containsAll(expectedPaths.stream()
                                                    .map(item -> Optional.ofNullable(context).map(path -> path + ".").orElse("") + item)
                                                    .collect(Collectors.toList()));
    }

    private static void assertPetstoreProperties(List<String> paths) {
        List<String> expectedParameters = Collections.singletonList("version");
        List<String> expectedBodyPaths = Arrays.asList("id", "name", "category.id", "category.name", "photoUrls.size()",
                "tags[].id", "tags[].name", "tags.size()", "status");

        assertThat(paths).containsAll(expectedBodyPaths.stream()
                .map(item -> "body." + item)
                .collect(Collectors.toList()));

        assertThat(paths).containsAll(expectedParameters.stream()
                .map(item -> "parameters." + item)
                .collect(Collectors.toList()));
    }
}
