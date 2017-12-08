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
package io.syndesis.connector.generator.swagger;

import java.io.IOException;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.parser.SwaggerParser;
import io.syndesis.model.DataShape;

import org.junit.Test;

import static io.syndesis.connector.generator.swagger.TestHelper.reformatJson;
import static io.syndesis.connector.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

public class DataShapeHelperTest {

    @Test
    public void shouldExtractJsonSchemaFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");
        final Swagger swagger = new SwaggerParser().parse(specification);

        final BodyParameter body = (BodyParameter) swagger.getPath("/pet").getPost().getParameters().get(0);

        final DataShape dataShape = DataShapeHelper.createShapeFromModel(specification, body.getSchema());

        final String jsonSchema = dataShape.getSpecification();

        assertThat(reformatJson(jsonSchema)).isEqualTo(reformatJson(resource("/swagger/expected-pet-schema.json")));
    }

}
