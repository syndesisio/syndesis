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
package io.syndesis.server.api.generator.openapi.v3;

import java.io.IOException;
import java.util.stream.Stream;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.BaseOpenApiGeneratorExampleTest;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.util.OpenApiModelParser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiConnectorGeneratorExampleTest extends BaseOpenApiGeneratorExampleTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("parameters")
    public void shouldGenerateAsExpected(final String name, final String specification, final Connector connector) throws IOException {
        shouldGenerateAsExpected(specification, connector);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parameters")
    public void specificationsShouldNotContainErrors(final String name, final String specification) {
        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).isEmpty();
    }

    public static Stream<Arguments> parameters() throws IOException {
        return Stream.of(
            argument("reverb"),
            argument("petstore"),
            argument("petstore_xml"),
            argument("basic_auth"),
            argument("apikey_auth"),
            argument("todo"),
            argument("complex_xml"),
            argument("kie-server"),
            argument("machine_history"),
            argument("damage_service"),
            argument("doubleclick_reporting"),
            argument("issue_9638"));
    }

    static Arguments argument(String name) throws IOException {
        return Arguments.of(name, loadSpecification("v3", name), loadConnector("v3", name, "unified"));
    }
}
