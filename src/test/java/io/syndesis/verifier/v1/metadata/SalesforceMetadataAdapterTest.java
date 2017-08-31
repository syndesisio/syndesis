/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.verifier.v1.metadata;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SalesforceMetadataAdapterTest {

    private static final CamelContext CONTEXT = new DefaultCamelContext();

    private static final Map<String, Object> NOT_USED = null;

    private final SalesforceMetadataAdapter adapter = new SalesforceMetadataAdapter();

    @Test
    public void shouldAdaptObjectMetadata() throws IOException {
        final Map<String, JsonSchema> objectProperties = new HashMap<>();
        objectProperties.put("simpleProperty", new StringSchema());
        objectProperties.put("anotherProperty", new NumberSchema());

        final StringSchema uniqueProperty1 = new StringSchema();
        uniqueProperty1.setDescription("idLookup,autoNumber");
        uniqueProperty1.setTitle("Unique property 1");

        final StringSchema uniqueProperty2 = new StringSchema();
        uniqueProperty2.setDescription("calculated,idLookup");
        uniqueProperty2.setTitle("Unique property 2");

        objectProperties.put("uniqueProperty1", uniqueProperty1);
        objectProperties.put("uniqueProperty2", uniqueProperty2);

        final ObjectSchema objectSchema = new ObjectSchema();
        objectSchema.setId("urn:jsonschema:org:apache:camel:component:salesforce:dto:SimpleObject");
        objectSchema.setProperties(objectProperties);

        final ObjectSchema payload = new ObjectSchema();
        payload.setOneOf(Collections.singleton(objectSchema));

        final Map<String, Object> properties = new HashMap<>();
        properties.put("sObjectName", "SimpleObject");
        properties.put("sObjectIdName", null);

        final Map<String, List<PropertyPair>> adapted = adapter.apply(properties,
            MetaDataBuilder.on(CONTEXT).withAttribute("scope", "object").withPayload(payload).build());

        assertThat(adapted).containsKey("sObjectIdName");

        final List<PropertyPair> values = adapted.get("sObjectIdName");

        assertThat(values).containsOnly(new PropertyPair("uniqueProperty1", "Unique property 1"),
            new PropertyPair("uniqueProperty2", "Unique property 2"));
    }

    @Test
    public void shouldAdaptObjectTypesMetadata() throws IOException {
        final JsonNode payload = new ObjectMapper().readTree(
            "[{\"name\":\"Object1\",\"label\":\"Object1 Label\"},{\"name\":\"Object2\",\"label\":\"Object2 Label\"}]");

        final Map<String, List<PropertyPair>> adapted = adapter.apply(NOT_USED,
            MetaDataBuilder.on(CONTEXT).withAttribute("scope", "object_types").withPayload(payload).build());

        assertThat(adapted).containsKey("sObjectName");

        final List<PropertyPair> values = adapted.get("sObjectName");

        assertThat(values).containsOnly(new PropertyPair("Object1", "Object1 Label"),
            new PropertyPair("Object2", "Object2 Label"));
    }
}
