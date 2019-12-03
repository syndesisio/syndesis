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
package io.syndesis.server.api.generator.openapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.openapi.util.OpenApiModelParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class OpenApiConnectorGeneratorExampleTest extends BaseOpenApiGeneratorExampleTest {

    public OpenApiConnectorGeneratorExampleTest(final String name) throws IOException {
        super("unified", name);
    }

    @Test
    @Override
    public void shouldGenerateAsExpected() throws IOException {
        super.shouldGenerateAsExpected();
    }

    @Test
    public void specificationsShouldNotContainErrors() {
        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).isEmpty();
    }

    @Override
    ConnectorGenerator generator() {
        try (InputStream stream = OpenApiConnectorGeneratorExampleTest.class.getResourceAsStream("/META-INF/syndesis/connector/rest-swagger.json")) {
            final Connector restSwagger = JsonUtils.readFromStream(stream, Connector.class);

            final AtomicInteger cnt = new AtomicInteger();
            return new OpenApiConnectorGenerator(restSwagger, () -> "operation-" + cnt.getAndIncrement());
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    @Parameters(name = "{0}")
    public static Iterable<String> parameters() {
        return Arrays.asList("reverb", "petstore", "petstore_xml", "basic_auth", "todo", "complex_xml", "kie-server", "machine_history", "damage_service");
    }

}
