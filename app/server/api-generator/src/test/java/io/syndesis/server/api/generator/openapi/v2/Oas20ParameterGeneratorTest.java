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

package io.syndesis.server.api.generator.openapi.v2;

import java.io.IOException;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.syndesis.common.model.connection.ConfigurationProperty;
import org.junit.jupiter.api.Test;

import static io.syndesis.server.api.generator.openapi.TestHelper.resource;
import static org.assertj.core.api.Assertions.assertThat;

public class Oas20ParameterGeneratorTest {

    @Test
    public void shouldCreatePropertyParametersFromPetstoreSwagger() throws IOException {
        final String specification = resource("/openapi/v2/petstore.json");
        final Oas20Document openApiDoc = (Oas20Document) Library.readDocumentFromJSONString(specification);
        final Oas20Parameter petIdPathParameter = (Oas20Parameter) openApiDoc.paths.getPathItem("/pet/{petId}").get.getParameters().get(0);

        final ConfigurationProperty configurationProperty =
            Oas20ParameterGenerator.createPropertyFromParameter(petIdPathParameter, petIdPathParameter.type, Oas20ModelHelper.javaTypeFor(petIdPathParameter),
                                                                petIdPathParameter.default_, petIdPathParameter.enum_);

        final ConfigurationProperty expected = new ConfigurationProperty.Builder()//
            .componentProperty(false)//
            .deprecated(false)//
            .description("ID of pet to return")//
            .displayName("petId")//
            .group("producer")//
            .javaType(Long.class.getName())//
            .kind("property")//
            .required(true)//
            .secret(false)//
            .type("integer")//
            .build();

        assertThat(configurationProperty).isEqualTo(expected);
    }
}
