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
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaInspectorTest {

    @Test
    public void shouldCollectPathsFromJsonSchema() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectSchema schema = mapper.readValue(
            JsonSchemaInspectorTest.class.getResourceAsStream("/salesforce.Contact.jsonschema"), ObjectSchema.class);

        final ArrayList<String> paths = new ArrayList<>();
        JsonSchemaInspector.fetchPaths(null, paths, schema.getProperties());

        assertThat(paths).contains("Id", "IsDeleted", "MasterRecordId", "AccountId", "LastName", "FirstName",
            "OtherAddress.latitude", "MailingAddress.city");
    }

}
