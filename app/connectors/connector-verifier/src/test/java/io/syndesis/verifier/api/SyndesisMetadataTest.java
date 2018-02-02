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
package io.syndesis.verifier.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

public class SyndesisMetadataTest {

    @Test
    public void shouldSerializeWithObjectSchema() throws JsonProcessingException, JSONException {
        final Map<String, List<PropertyPair>> properties = new HashMap<>();
        properties.put("aProperty", Collections.singletonList(new PropertyPair("value", "label")));

        final ObjectSchema inputSchema = new ObjectSchema();
        inputSchema.setTitle("input");

        final ObjectSchema outputSchema = new ObjectSchema();
        outputSchema.setTitle("output");

        final SyndesisMetadata<ObjectSchema> syndesisMetadata = new SyndesisMetadata<>(properties, inputSchema, outputSchema);

        final ObjectMapper mapper = new ObjectMapper();

        final String json = mapper.writeValueAsString(syndesisMetadata);

        JSONAssert.assertEquals(
            "{\"outputSchema\":{\"type\":\"object\",\"title\":\"output\"},\"inputSchema\":{\"type\":\"object\",\"title\":\"input\"},\"properties\":{\"aProperty\":[{\"displayValue\":\"label\",\"value\":\"value\"}]}}",
            json, JSONCompareMode.STRICT);
    }
}
