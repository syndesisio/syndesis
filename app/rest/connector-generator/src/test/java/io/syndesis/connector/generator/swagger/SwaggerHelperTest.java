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
package io.syndesis.connector.generator.swagger;

import org.junit.Test;

import java.io.IOException;

import static io.syndesis.connector.generator.swagger.TestHelper.resource;
import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerHelperTest extends SwaggerConnectorGeneratorBaseTest {

    @Test
    public void testThatAllSwaggerFilesAreValid() throws IOException {
        String[] specifications = {
            "/swagger/concur.swagger.json",
            "/swagger/petstore.swagger.json",
            // "/swagger/reverb.swagger.json" // reverb is invalid
        };

        for (String specificationFile : specifications) {
            String specification = resource(specificationFile);
            SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

            assertThat(info.getErrors())
                .withFailMessage("Specification " + specificationFile + " has errors: " + info.getErrors())
                .isEmpty();

            assertThat(info.getWarnings())
                .withFailMessage("Specification " + specificationFile + " has warnings: " + info.getWarnings())
                .isEmpty();
        }
    }

    @Test
    public void testThatInvalidSchemePetstoreSwaggerIsInvalid() throws IOException {
        String specification = resource("/swagger/invalid/invalid-scheme.petstore.swagger.json");
        SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("instance value (\"httpz\") not found in enum");
        assertThat(info.getErrors().get(0).property()).contains("/schemes/0");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatInvalidFieldPetstoreSwaggerIsInvalid() throws IOException {
        String specification = resource("/swagger/invalid/invalid-field.petstore.swagger.json");
        SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("object instance has properties which are not allowed by the schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet/put");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatInvalidTypePetstoreSwaggerIsInvalid() throws IOException {
        String specification = resource("/swagger/invalid/invalid-type.petstore.swagger.json");
        SwaggerModelInfo info = SwaggerHelper.parse(specification, true);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("instance failed to match exactly one schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet~1{petId}/post/parameters/2");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

}
