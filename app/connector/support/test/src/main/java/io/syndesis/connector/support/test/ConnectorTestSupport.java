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
package io.syndesis.connector.support.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.ProjectGenerator;
import io.syndesis.integration.project.generator.ProjectGeneratorConfiguration;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;

public abstract class ConnectorTestSupport extends CamelTestSupport {
    private final ResourceManager resourceManager;

    protected ConnectorTestSupport() {
        this.resourceManager = new ResourceManager();
    }

    protected ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    protected abstract List<Step> createSteps();

    // ******************************
    // Configure camel
    // ******************************

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        context.disableJMX();

        return context;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new IntegrationRouteBuilder("", Resources.loadServices(IntegrationStepHandler.class)) {
            @Override
            protected Integration loadIntegration() throws IOException {
                return newIntegration();
            }
        };
    }

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        try {
            ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
            ProjectGenerator projectGenerator = new ProjectGenerator(configuration, new ResourceManager(), new MavenProperties());

            return projectGenerator.generateApplicationProperties(newIntegration());
        } catch (IOException e) {
            Assertions.fail("Unable to generate integration properties", e);
        }

        return null;
    }

    // ******************************
    // Helpers
    // ******************************

    protected final Step newSimpleEndpointStep(String scheme, Consumer<ConnectorDescriptor.Builder> consumer) {
        ConnectorDescriptor.Builder builder = new ConnectorDescriptor.Builder().componentScheme(scheme);
        consumer.accept(builder);

        return new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .descriptor(builder.build())
                .build())
            .build();
    }

    protected final Step newEndpointStep(String connectorId, String actionId, Consumer<Connection.Builder> connectionConsumer, Consumer<Step.Builder> stepConsumer) {
        Connector connector = resourceManager.mandatoryLoadConnector(connectorId);
        ConnectorAction action = resourceManager.mandatoryLookupAction(connector, actionId);

        Connection.Builder connectionBuilder = new Connection.Builder().connector(connector);
        connectionConsumer.accept(connectionBuilder);

        Step.Builder stepBuilder = new Step.Builder().stepKind(StepKind.endpoint).action(action).connection(connectionBuilder.build());
        stepConsumer.accept(stepBuilder);

        return stepBuilder.build();
    }


    protected final Integration newIntegration() {
        return new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addFlow(new Flow.Builder().steps(createSteps()).build())
            .build();
    }

    // ******************************
    //
    // ******************************

    protected static class ResourceManager implements IntegrationResourceManager {
        @Override
        public Optional<Connector> loadConnector(String id) {
            Connector connector = null;

            try (InputStream is = ConnectorTestSupport.class.getClassLoader().getResourceAsStream("META-INF/syndesis/connector/" + id + ".json")) {
                connector = JsonUtils.reader().forType(Connector.class).readValue(is);
            } catch (IOException e) {
                Assertions.fail("Unable to load connector: " + id, e);
            }

            return Optional.ofNullable(connector);
        }

        @Override
        public Optional<Extension> loadExtension(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<InputStream> loadExtensionBLOB(String id) {
            return Optional.empty();
        }

        @Override
        public List<Extension> loadExtensionsByTag(String tag) {
            return Collections.emptyList();
        }

        @Override
        public Optional<OpenApi> loadOpenApiDefinition(String s) {
            return Optional.empty();
        }

        @Override
        public String decrypt(String encrypted) {
            return encrypted;
        }

        public final Connector mandatoryLoadConnector(String connectorId) {
            return loadConnector(connectorId)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find connector: " + connectorId));
        }

        public final ConnectorAction mandatoryLookupAction(Connector connector, String actionId) {
            for (ConnectorAction action : connector.getActions()) {
                if (action.getId().isPresent() && action.getId().get().equals(actionId)) {
                    return action;
                }
            }

            throw new IllegalArgumentException("Unable to find action: " + actionId);
        }

        public final ConnectorAction mandatoryLookupAction(String connectorId, String actionId) {
            Connector connector = mandatoryLoadConnector(connectorId);
            return mandatoryLookupAction(connector, actionId);
        }
    }

    protected static <T> Consumer<T> nop(final Class<T> type) {
        return x -> {
            // no op
        };
    }
}
