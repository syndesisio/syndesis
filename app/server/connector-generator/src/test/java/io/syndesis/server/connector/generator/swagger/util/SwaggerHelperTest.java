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
package io.syndesis.server.connector.generator.swagger.util;

import java.io.IOException;

import io.syndesis.server.connector.generator.swagger.AbstractSwaggerConnectorTest;
import io.syndesis.server.connector.generator.swagger.SwaggerModelInfo;

import org.junit.Test;

import static io.syndesis.server.connector.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerHelperTest extends AbstractSwaggerConnectorTest {

    @Test
    public void testThatAllSwaggerFilesAreValid() throws IOException {
        final String[] specifications = {"/swagger/concur.swagger.json", "/swagger/petstore.swagger.json", "/swagger/todo.swagger.yaml"};

        for (final String specificationFile : specifications) {
            final String specification = resource(specificationFile);
            final SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

            assertThat(info.getErrors()).withFailMessage("Specification " + specificationFile + " has errors: " + info.getErrors())
                .isEmpty();
        }
    }

    @Test
    public void testThatInvalidFieldPetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-field.petstore.swagger.json");
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("object instance has properties which are not allowed by the schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet/put");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatInvalidSchemePetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-scheme.petstore.swagger.json");
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).hasSize(1);
        assertThat(info.getErrors().get(0).message()).startsWith("instance value (\"httpz\") not found in enum");
        assertThat(info.getErrors().get(0).property()).contains("/schemes/0");
        assertThat(info.getErrors().get(0).error()).contains("validation");
        assertThat(info.getWarnings().get(0).message()).startsWith("Unable to determine the scheme");
        assertThat(info.getWarnings().get(0).property()).contains("/schemes");
        assertThat(info.getWarnings().get(0).error()).contains("missing-schemes");
    }

    @Test
    public void testThatInvalidTypePetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-type.petstore.swagger.json");
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("instance failed to match exactly one schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet~1{petId}/post/parameters/2");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatWarningPetstoreSwaggerContainsWarnings() throws IOException {
        final String specification = resource("/swagger/invalid/warning-petstore.swagger.json");
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).isEmpty();
        assertThat(info.getWarnings()).hasSize(3);
    }

}
