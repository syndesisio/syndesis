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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.core.Json;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static io.syndesis.connector.generator.swagger.TestHelper.reformatJson;
import static io.syndesis.connector.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class SwaggerConnectorGeneratorExampleTests extends SwaggerConnectorGeneratorBaseTest {

    private final Connector expected;

    private final String specification;

    public SwaggerConnectorGeneratorExampleTests(final String name) throws IOException {
        specification = resource("/swagger/" + name + ".swagger.json");
        expected = Json.mapper().readValue(resource("/swagger/" + name + ".connector.json"), Connector.class);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    public void shouldGenerateAsExpected() throws IOException {

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector generated = new SwaggerConnectorGenerator().generate(SWAGGER_TEMPLATE, connectorSettings);

        final Map<String, String> generatedConfiguredProperties = generated.getConfiguredProperties();
        final String generatedSpecification = generatedConfiguredProperties.get("specification");

        final Map<String, String> expectedConfiguredProperties = expected.getConfiguredProperties();
        final String expectedSpecification = expectedConfiguredProperties.get("specification");
        assertThat(reformatJson(generatedSpecification)).isEqualTo(reformatJson(expectedSpecification));

        assertThat(without(generatedConfiguredProperties, "specification"))
            .containsAllEntriesOf(without(expectedConfiguredProperties, "specification"));

        assertThat(generated.getProperties().keySet()).as("Expecting the same properties to be generated")
            .containsOnlyElementsOf(expected.getProperties().keySet());
        assertThat(generated.getProperties()).containsAllEntriesOf(expected.getProperties());
        assertThat(generated).isEqualToIgnoringGivenFields(expected, "id", "properties", "configuredProperties", "actions");
        assertThat(generated.getActions()).hasSameSizeAs(expected.getActions());

        for (final ConnectorAction expectedAction : expected.getActions()) {
            final String actionId = expectedAction.getId().get().replace("_id_", generated.getId().get());
            final Optional<ConnectorAction> maybeGeneratedAction = generated.actionById(actionId);
            assertThat(maybeGeneratedAction).as("No action with id: " + actionId + " was generated").isPresent();

            final ConnectorAction generatedAction = maybeGeneratedAction.get();
            assertThat(generatedAction).as("Difference found for action: " + actionId).isEqualToIgnoringGivenFields(expectedAction, "id",
                "descriptor");

            assertThat(generatedAction.getDescriptor().getPropertyDefinitionSteps())
                .as("Generated and expected action definition property definition steps for action with id: " + actionId + " differ")
                .isEqualTo(expectedAction.getDescriptor().getPropertyDefinitionSteps());

            if (expectedAction.getDescriptor().getInputDataShape().isPresent()) {
                final DataShape generatedInputDataShape = generatedAction.getDescriptor().getInputDataShape().get();
                final DataShape expectedInputDataShape = expectedAction.getDescriptor().getInputDataShape().get();

                assertThat(generatedInputDataShape).isEqualToIgnoringGivenFields(expectedInputDataShape, "specification");

                assertThat(reformatJson(generatedInputDataShape.getSpecification()))
                    .isEqualTo(reformatJson(expectedInputDataShape.getSpecification()));
            }

            if (expectedAction.getDescriptor().getOutputDataShape().isPresent()) {
                final DataShape generatedOutputDataShape = generatedAction.getDescriptor().getOutputDataShape().get();
                final DataShape expectedOutputDataShape = expectedAction.getDescriptor().getOutputDataShape().get();
                assertThat(generatedOutputDataShape).isEqualToIgnoringGivenFields(expectedOutputDataShape, "specification");

                assertThat(reformatJson(generatedOutputDataShape.getSpecification()))
                    .isEqualTo(reformatJson(expectedOutputDataShape.getSpecification()));
            }
        }
    }

    @Parameters(name = "{0}")
    public static Iterable<String> parameters() {
        return Arrays.asList("concur", "petstore", "reverb");
    }

    private static Map<String, String> without(final Map<String, String> map, final String key) {
        final Map<String, String> ret = new HashMap<>(map);

        ret.remove(key);

        return ret;
    }

}
