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

import java.io.IOException;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.extension.Extension;

import io.syndesis.common.util.json.JsonUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ExtensionSchemaValidationTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.copyObjectMapperConfiguration();

    @Test
    public void validateStepExtensionTest() throws ProcessingException, IOException {
        String syndesisExtensionSchema = "/syndesis/syndesis-extension-definition-schema.json";
        JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + syndesisExtensionSchema);
        ExtensionConverter converter = new DefaultExtensionConverter();

        Extension extension = new Extension.Builder()
            .extensionId("my-extension")
            .name("Name")
            .description("Description")
            .version("1.0.0")
            .schemaVersion(ExtensionConverter.getCurrentSchemaVersion())
            .addAction(new StepAction.Builder()
                .id("action-1")
                .name("action-1-name")
                .description("Action 1 Description")
                .pattern(Action.Pattern.From)
                .descriptor(new StepDescriptor.Builder()
                    .entrypoint("direct:hello")
                    .kind(StepAction.Kind.ENDPOINT)
                    .build())
                .build())
            .build();

        JsonNode tree = converter.toPublicExtension(extension);
        ProcessingReport report = schema.validate(tree);
        assertFalse(report.iterator().hasNext(), report.toString());

        Extension extensionClone = converter.toInternalExtension(tree);
        assertEquals(extensionClone, extension);
    }

    @Test
    public void upgradePublicModelExtensionTest() throws ProcessingException, IOException {
        String syndesisExtensionSchema = "/syndesis/syndesis-extension-definition-schema.json";
        JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + syndesisExtensionSchema);
        ExtensionConverter converter = new DefaultExtensionConverter();

        Extension extension = new Extension.Builder()
                .extensionId("my-extension")
                .name("Name")
                .description("Description")
                .version("1.0.0")
                .schemaVersion("old-V0.1")
                .extensionType(Extension.Type.Steps)
                .build();

        JsonNode tree = converter.toPublicExtension(extension);
        ProcessingReport report = schema.validate(tree);
        assertFalse(report.iterator().hasNext(), report.toString());

        Extension extensionClone = converter.toInternalExtension(tree);
        assertNotEquals(extensionClone, extension);
        assertEquals(ExtensionConverter.getCurrentSchemaVersion(), extensionClone.getSchemaVersion());
    }

    @Test
    public void addSchemaVersionInPublicModelExtensionTest() throws ProcessingException, IOException {
        String syndesisExtensionSchema = "/syndesis/syndesis-extension-definition-schema.json";
        JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema("resource:" + syndesisExtensionSchema);
        ExtensionConverter converter = new DefaultExtensionConverter();

        ObjectNode tree = OBJECT_MAPPER.createObjectNode()
                .put("extensionId", "my-extension")
                .put("name", "Name")
                .put("description", "Description")
                .put("version", "1.0.0");

        ProcessingReport report = schema.validate(tree);
        assertFalse(report.iterator().hasNext(), report.toString());

        Extension extension = converter.toInternalExtension(tree);
        assertEquals(ExtensionConverter.getCurrentSchemaVersion(), extension.getSchemaVersion());
    }

}
