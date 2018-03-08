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
package io.syndesis.connector.salesforce;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.component.salesforce.api.utils.JsonUtils;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SalesforceMetadataRetrievalTest {

    private static final CamelContext CONTEXT = new DefaultCamelContext();

    private static final Map<String, Object> NOT_USED = null;

    private final SalesforceMetadataRetrieval adapter = new SalesforceMetadataRetrieval();

    private final ObjectSchema payload;

    public SalesforceMetadataRetrievalTest() {
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

        payload = new ObjectSchema();
        payload.setOneOf(Collections.singleton(objectSchema));
    }

    @Test
    public void shouldAdaptObjectMetadataForProperties() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("sObjectName", "SimpleObject");
        properties.put("sObjectIdName", null);

        final SyndesisMetadata metadata = adapter.adapt(null, null, null, properties,
            MetaDataBuilder.on(CONTEXT).withAttribute("scope", "object").withPayload(payload).build());

        assertThat(metadata.properties).containsKey("sObjectIdName");

        final List<PropertyPair> values = metadata.properties.get("sObjectIdName");

        assertThat(values).containsOnly(new PropertyPair("uniqueProperty1", "Unique property 1"),
            new PropertyPair("uniqueProperty2", "Unique property 2"));
    }

    @Test
    public void shouldAdaptObjectMetadataForSchema() throws IOException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("sObjectName", "SimpleObject");

        final SyndesisMetadata metadata = adapter.adapt(null, null, null, properties,
            MetaDataBuilder.on(CONTEXT).withAttribute("scope", "object").withPayload(payload).build());

        assertThat(metadata.inputShape).isSameAs(metadata.inputShape);
        final Object oneOf = payload.getOneOf().iterator().next();
        final ObjectSchema inSchema = Json.reader().forType(ObjectSchema.class).readValue(metadata.inputShape.getSpecification()    );

        assertThat(inSchema).isEqualTo(oneOf);
        assertThat(inSchema.get$schema()).isEqualTo(JsonUtils.SCHEMA4);
    }

    @Test
    public void shouldAdaptObjectTypesMetadataForProperties() {
        final ObjectSchema globalObjectsPayload = new ObjectSchema();
        final HashSet<Object> oneOf = new HashSet<>();
        oneOf.add(simpleObjectSchema("Object1", "Object1 Label"));
        oneOf.add(simpleObjectSchema("Object2", "Object2 Label"));
        globalObjectsPayload.setOneOf(oneOf);

        final SyndesisMetadata metadata = adapter.adapt(null, null, null, NOT_USED,
            MetaDataBuilder.on(CONTEXT).withPayload(globalObjectsPayload).build());

        assertThat(metadata.properties).containsKey("sObjectName");

        final List<PropertyPair> values = metadata.properties.get("sObjectName");

        assertThat(values).containsOnly(new PropertyPair("Object1", "Object1 Label"),
            new PropertyPair("Object2", "Object2 Label"));
    }

    @Test
    public void shouldAdaptObjectTypesMetadataForPropertiesLegacy() throws IOException {
        final JsonNode payload = new ObjectMapper().readTree(
            "[{\"name\":\"Object1\",\"label\":\"Object1 Label\"},{\"name\":\"Object2\",\"label\":\"Object2 Label\"}]");

        final SyndesisMetadata metadata = adapter.adapt(null, null, null, NOT_USED,
            MetaDataBuilder.on(CONTEXT).withAttribute("scope", "object_types").withPayload(payload).build());

        assertThat(metadata.properties).containsKey("sObjectName");

        final List<PropertyPair> values = metadata.properties.get("sObjectName");

        assertThat(values).containsOnly(new PropertyPair("Object1", "Object1 Label"),
            new PropertyPair("Object2", "Object2 Label"));
    }

    ObjectSchema simpleObjectSchema(final String name, final String label) {
        final ObjectSchema objectSchema = new ObjectSchema();
        objectSchema.setId(JsonUtils.DEFAULT_ID_PREFIX + ":" + name);
        objectSchema.setTitle(label);

        return objectSchema;
    }
}
