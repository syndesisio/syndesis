/*
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

package io.syndesis.project.converter.visitor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.core.SyndesisServerException;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Step;
import io.syndesis.project.converter.GenerateProjectRequest;

public class EndpointStepVisitor implements StepVisitor {
    private final GeneratorContext generatorContext;

    public static class Factory implements StepVisitorFactory<EndpointStepVisitor> {

        @Override
        public String getStepKind() {
            return Endpoint.KIND;
        }

        @Override
        public EndpointStepVisitor create(GeneratorContext generatorContext) {
            return new EndpointStepVisitor(generatorContext);
        }
    }

    public EndpointStepVisitor(GeneratorContext generatorContext) {
        this.generatorContext = generatorContext;
    }

    @Override
    public io.syndesis.integration.model.steps.Step visit(StepVisitorContext visitorContext) {
        Step step = visitorContext.getStep();
        if (!step.getAction().isPresent() || !step.getConnection().isPresent()) {
            return null;
        }

        Action action = step.getAction().orElseThrow(() -> new IllegalStateException("Action is not present"));
        Connection connection = step.getConnection().orElseThrow(() -> new IllegalStateException("Action is not present"));

        GenerateProjectRequest request = generatorContext.getRequest();
        try {
            String connectorId = step.getConnection().get().getConnectorId().orElse(action.getConnectorId());
            if (!request.getConnectors().containsKey(connectorId)) {
                throw new IllegalStateException("Connector:[" + connectorId + "] not found.");
            }

            return createEndpointStep(
                connection,
                request.getConnectors().get(connectorId),
                action.getCamelConnectorPrefix(),
                connection.getConfiguredProperties(),
                step.getConfiguredProperties().orElseGet(Collections::emptyMap));
        } catch (IOException | URISyntaxException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private io.syndesis.integration.model.steps.Step createEndpointStep(
            Connection connection, Connector connector, String camelConnectorPrefix, Map<String, String> connectionConfiguredProperties, Map<String, String> stepConfiguredProperties) throws IOException, URISyntaxException {

        final Map<String, String> aggregates = aggregate(connectionConfiguredProperties, stepConfiguredProperties);
        final Map<String, String> properties = connector.filterProperties(aggregates, connector::isEndpointProperty);
        final boolean hasComponentOptions = aggregates.entrySet().stream().anyMatch(connector::isComponentProperty);

        // connector prefix is suffixed with the connection id if present and in
        // such case the prefix is something like:
        //
        //     twitter-search-1
        //
        // otherwise it fallback to
        //
        //     twitter-search
        //
        final String connectorPrefix = connection.getId().map(id -> camelConnectorPrefix + "-" + id).orElse(camelConnectorPrefix);

        // connector scheme is suffixed with the connection id if present and if
        // the connector has component options to match the code in ActivateHandler
        // (see ActivateHandler#extractApplicationProperties) and in such case
        // the scheme generated scheme is something like:
        //
        //     twitter-search-1
        //
        // otherwise it fallback to
        //
        //     twitter-search
        //
        final String connectorScheme = hasComponentOptions ? connection.getId().map(id -> camelConnectorPrefix + "-" + id).orElse(camelConnectorPrefix) : camelConnectorPrefix;

        Map<String, String> secrets = connector.filterProperties(
            properties,
            connector.isSecret(),
            e -> e.getKey(),
            e -> String.format("{{%s.%s}}", connectorPrefix, e.getKey()));

        // TODO Remove this hack... when we can read endpointValues from connector schema then we should use those as initial properties.
        if ("periodic-timer".equals(camelConnectorPrefix)) {
            properties.put("timerName", "every");
        }

        return createEndpoint(
            camelConnectorPrefix,
            connectorScheme,
            generatorContext.getGeneratorProperties().isSecretMaskingEnabled()
                ? aggregate(properties, secrets)
                : properties
        );
    }

    private Endpoint createEndpoint(String camelConnectorPrefix, String connectorScheme, Map<String, String> endpointOptions) throws URISyntaxException {
        String endpointUri = generatorContext.getConnectorCatalog().buildEndpointUri(camelConnectorPrefix, endpointOptions);

        if (endpointUri.startsWith(camelConnectorPrefix) && !camelConnectorPrefix.equals(connectorScheme)) {
            String remaining = endpointUri.substring(camelConnectorPrefix.length());

            if (!remaining.isEmpty()) {
                endpointUri = connectorScheme + remaining;
            } else {
                endpointUri = connectorScheme;
            }
        }

        return new Endpoint(endpointUri);
    }

    private static Map<String, String> aggregate(Map<String, String> ... maps) throws IOException {
        return Stream.of(maps).flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
    }
}
