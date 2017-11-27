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
import java.util.Optional;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import io.syndesis.core.Json;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorTemplate;
import io.syndesis.model.connection.ConnectorSettings;

import org.junit.Test;

import static io.syndesis.connector.generator.swagger.TestHelper.reformatJson;
import static io.syndesis.connector.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerConnectorGeneratorTest {

    @Test
    public void shouldCreatePropertyParametersFromPetstoreSwagger() throws IOException {
        final String specification = resource("/petstore.json");
        final Swagger swagger = new SwaggerParser().parse(specification);

        final Parameter petIdPathParameter = swagger.getPath("/pet/{petId}").getGet().getParameters().get(0);

        final Optional<ConfigurationProperty> maybeConfigurationProperty = SwaggerConnectorGenerator
            .createPropertyFromParameter(petIdPathParameter);

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

        assertThat(maybeConfigurationProperty).hasValue(expected);
    }

    @Test
    public void shouldCreateSecurityConfigurationFromReverbSwagger() throws IOException {
        final String specification = resource("/reverb.json");

        final ConnectorTemplate connectorTemplate = new ConnectorTemplate.Builder()//
            .id("swagger-connector-template")//
            .camelConnectorGAV("io.syndesis:swagger-connector:latest")//
            .camelConnectorPrefix("swagger-operation")//
            .putConnectorProperty("host",
                new ConfigurationProperty.Builder()//
                    .kind("property")//
                    .displayName("Host")//
                    .group("producer")//
                    .label("producer")//
                    .required(false)//
                    .type("string")//
                    .javaType("java.lang.String")//
                    .deprecated(false)//
                    .secret(false)//
                    .componentProperty(true)//
                    .description(
                        "Scheme hostname and port to direct the HTTP requests to in the form of https://hostname:port. Can be configured at the endpoint component or in the correspoding REST configuration in the Camel Context. If you give this component a name (e.g. petstore) that REST configuration is consulted first rest-swagger next and global configuration last. If set overrides any value found in the Swagger specification RestConfiguration. Can be overriden in endpoint configuration.")//
                    .build())
            .build();

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Reverb API")//
            .description("Invokes Reverb API")//
            .icon("fa-music")//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector sculpted = new SwaggerConnectorGenerator().generate(connectorTemplate, connectorSettings);

        assertThat(sculpted.getProperties()).containsKeys("accessToken", "accessTokenUrl", "clientId", "clientSecret");
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    public void shouldSculptForConcurQuickExpensesSwagger() throws IOException {
        final String specification = resource("/concur.QuickExpenses.swagger2.json");

        final ConnectorTemplate connectorTemplate = new ConnectorTemplate.Builder()//
            .id("swagger-connector-template")//
            .camelConnectorGAV("io.syndesis:swagger-connector:latest")//
            .camelConnectorPrefix("swagger-operation")//
            .putConnectorProperty("host",
                new ConfigurationProperty.Builder()//
                    .kind("property")//
                    .displayName("Host")//
                    .group("producer")//
                    .label("producer")//
                    .required(false)//
                    .type("string")//
                    .javaType("java.lang.String")//
                    .deprecated(false)//
                    .secret(false)//
                    .componentProperty(true)//
                    .description(
                        "Scheme hostname and port to direct the HTTP requests to in the form of https://hostname:port. Can be configured at the endpoint component or in the correspoding REST configuration in the Camel Context. If you give this component a name (e.g. petstore) that REST configuration is consulted first rest-swagger next and global configuration last. If set overrides any value found in the Swagger specification RestConfiguration. Can be overriden in endpoint configuration.")//
                    .build())
            .build();

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Concur Quick Expense API")//
            .description("Invokes Quick Expense API")//
            .icon("fa-link")//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector sculpted = new SwaggerConnectorGenerator().generate(connectorTemplate, connectorSettings);

        final Connector expected = Json.mapper().readValue(resource("/expected-quick-expenses-connector.json"),
            Connector.class);

        assertThat(sculpted.getConfiguredProperties()).containsKey("specification").hasSize(1);
        final String sculptedSpecification = sculpted.getConfiguredProperties().get("specification");
        final String expectedSpecification = expected.getConfiguredProperties().get("specification");
        assertThat(reformatJson(sculptedSpecification)).isEqualTo(reformatJson(expectedSpecification));

        assertThat(sculpted.getProperties()).isEqualTo(expected.getProperties());
        assertThat(sculpted).isEqualToIgnoringGivenFields(expected, "id", "configuredProperties", "actions");
        assertThat(sculpted.getActions()).hasSameSizeAs(expected.getActions());

        for (final ConnectorAction expectedAction : expected.getActions()) {
            final String actionId = expectedAction.getId().get().replace("_id_", sculpted.getId().get());
            final Optional<ConnectorAction> maybeSculptedAction = sculpted.actionById(actionId);
            assertThat(maybeSculptedAction).as("No action with id: " + actionId + " was sculpted").isPresent();

            final ConnectorAction sculptedAction = maybeSculptedAction.get();
            assertThat(sculptedAction).as("Difference found for action: " + actionId)
                .isEqualToIgnoringGivenFields(expectedAction, "id", "descriptor");

            assertThat(sculptedAction.getDescriptor().getPropertyDefinitionSteps())
                .as("Sculpted and expected action definition property definition steps for action with id: " + actionId
                    + " differ")
                .isEqualTo(expectedAction.getDescriptor().getPropertyDefinitionSteps());

            if (expectedAction.getDescriptor().getInputDataShape().isPresent()) {
                final DataShape sculptedInputDataShape = sculptedAction.getDescriptor().getInputDataShape().get();
                final DataShape expectedInputDataShape = expectedAction.getDescriptor().getInputDataShape().get();

                assertThat(sculptedInputDataShape).isEqualToIgnoringGivenFields(expectedInputDataShape,
                    "specification");

                assertThat(reformatJson(sculptedInputDataShape.getSpecification()))
                    .isEqualTo(reformatJson(expectedInputDataShape.getSpecification()));
            }

            if (expectedAction.getDescriptor().getOutputDataShape().isPresent()) {
                final DataShape sculptedOutputDataShape = sculptedAction.getDescriptor().getOutputDataShape().get();
                final DataShape expectedOutputDataShape = expectedAction.getDescriptor().getOutputDataShape().get();
                assertThat(sculptedOutputDataShape).isEqualToIgnoringGivenFields(expectedOutputDataShape,
                    "specification");

                assertThat(reformatJson(sculptedOutputDataShape.getSpecification()))
                    .isEqualTo(reformatJson(expectedOutputDataShape.getSpecification()));
            }
        }
    }

}
