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
package io.syndesis.extension.converter;

import java.util.OptionalInt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.syndesis.core.Json;
import io.syndesis.model.action.Action;
import io.syndesis.model.action.ExtensionAction;
import io.syndesis.model.action.ExtensionDescriptor;
import io.syndesis.model.extension.Extension;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class ExtensionSchemaValidationTest {

    @Test
    @Ignore("Used to generate the initial extension definition")
    public void generateBaseExtensionDefinition() throws Exception {
        ObjectMapper mapper = Json.mapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaGen.generateSchema(Extension.class);

        System.out.println(mapper.writeValueAsString(schema));
    }

    @Test
    public void validateExtensionTest() throws ProcessingException {
        String syndesisExtensionSchema = "/syndesis/syndesis-extension-definition-schema.json";
        JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + syndesisExtensionSchema);
        ExtensionConverter converter = new DefaultExtensionConverter();

        Extension extension = new Extension.Builder()
            .extensionId("my-extension")
            .name("Name")
            .description("Description")
            .uses(OptionalInt.empty())
            .version("1.0.0")
            .schemaVersion(ExtensionConverter.getCurrentSchemaVersion())
            .addAction(new ExtensionAction.Builder()
                .id("action-1")
                .name("action-1-name")
                .description("Action 1 Description")
                .actionType("extension")
                .pattern(Action.Pattern.From)
                .descriptor(new ExtensionDescriptor.Builder()
                    .entrypoint("direct:hello")
                    .kind(ExtensionAction.Kind.ENDPOINT)
                    .build())
                .build())
            .build();

        JsonNode tree = converter.toPublicExtension(extension);
        ProcessingReport report = schema.validate(tree);
        assertFalse(report.toString(), report.iterator().hasNext());

        Extension extensionClone = converter.toInternalExtension(tree);
        assertEquals(extensionClone, extension);
    }

    @Test
    public void upgradePublicModelExtensionTest() throws ProcessingException {
        String syndesisExtensionSchema = "/syndesis/syndesis-extension-definition-schema.json";
        JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + syndesisExtensionSchema);
        ExtensionConverter converter = new DefaultExtensionConverter();

        Extension extension = new Extension.Builder()
                .extensionId("my-extension")
                .name("Name")
                .description("Description")
                .version("1.0.0")
                .schemaVersion("old-V0.1")
                .build();

        JsonNode tree = converter.toPublicExtension(extension);
        ProcessingReport report = schema.validate(tree);
        assertFalse(report.toString(), report.iterator().hasNext());

        Extension extensionClone = converter.toInternalExtension(tree);
        assertNotEquals(extensionClone, extension);
        assertEquals(ExtensionConverter.getCurrentSchemaVersion(), extensionClone.getSchemaVersion());
    }

    @Test
    public void addSchemaVersionInPublicModelExtensionTest() throws ProcessingException {
        String syndesisExtensionSchema = "/syndesis/syndesis-extension-definition-schema.json";
        JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + syndesisExtensionSchema);
        ExtensionConverter converter = new DefaultExtensionConverter();

        ObjectNode tree = Json.mapper().createObjectNode()
                .put("extensionId", "my-extension")
                .put("name", "Name")
                .put("description", "Description")
                .put("version", "1.0.0");

        ProcessingReport report = schema.validate(tree);
        assertFalse(report.toString(), report.iterator().hasNext());

        Extension extension = converter.toInternalExtension(tree);
        assertEquals(ExtensionConverter.getCurrentSchemaVersion(), extension.getSchemaVersion());
    }

}
