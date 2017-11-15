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
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.core.SyndesisServerException;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.model.WithConfigurationProperties;
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

        try {
            return createEndpoint(
                visitorContext,
                generatorContext.getRequest(),
                step,
                step.getConnection().orElseThrow(() -> new IllegalStateException("Action is not present")),
                step.getAction().orElseThrow(() -> new IllegalStateException("Action is not present"))
            );
        } catch (IOException | URISyntaxException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private Endpoint createEndpoint(StepVisitorContext visitorContext, GenerateProjectRequest request, Step step, Connection connection, Action action) throws URISyntaxException, IOException {
        String connectorId = step.getConnection().get().getConnectorId().orElse(action.getConnectorId());
        if (!request.getConnectors().containsKey(connectorId)) {
            throw new IllegalStateException("Connector:[" + connectorId + "] not found.");
        }

        final String camelConnectorPrefix = action.getCamelConnectorPrefix();
        final Connector connector = request.getConnectors().get(connectorId);
        final Map<String, String> configuredProperties = aggregate(connector.getConfiguredProperties(), connection.getConfiguredProperties(), step.getConfiguredProperties());
        final Map<String, String> properties = aggregate(connector.filterProperties(configuredProperties, connector::isEndpointProperty), action.filterProperties(configuredProperties, action::isEndpointProperty));
        final boolean hasComponentOptions = hasComponentProperties(configuredProperties, connector, action);

        // connector prefix is suffixed with the computed id if needed (i.e. if
        // multiple instances of the same connector are defined). In such case
        // the prefix is something like:
        //
        //     twitter-search-connector-1
        //
        // otherwise it fallback to
        //
        //     twitter-search-connector
        //
        final String connectorPrefix = visitorContext.getConnectorIdSupplier().apply(step).map(id -> camelConnectorPrefix + "-" + id).orElse(camelConnectorPrefix);

        // connector prefix is suffixed with the computed id if needed (i.e. if
        // multiple instances of the same connector are defined) and if the
        // connector has component options to match the code in ActivateHandler
        // (see ActivateHandler#extractApplicationProperties) and in such case
        // the scheme generated scheme is something like:
        //
        //      twitter-search-connector-1
        //
        // otherwise it fallback to
        //
        //     twitter-search-connector
        //
        final String connectorScheme = hasComponentOptions ? visitorContext.getConnectorIdSupplier().apply(step).map(id -> camelConnectorPrefix + "-" + id).orElse(camelConnectorPrefix) : camelConnectorPrefix;

        // if the option is marked as secret use property placeholder as the
        // value is added to the integration secret.
        if (generatorContext.getGeneratorProperties().isSecretMaskingEnabled()) {
            properties.entrySet()
                .stream()
                .filter(or(connector::isSecret, action::isSecret))
                .forEach(e -> e.setValue(String.format("{{%s.%s}}", connectorPrefix, e.getKey())));
        }

        // TODO Remove this hack... when we can read endpointValues from connector schema then we should use those as initial properties.
        if ("periodic-timer".equals(camelConnectorPrefix)) {
            properties.put("timerName", "every");
        }

        return createEndpoint(camelConnectorPrefix, connectorScheme, properties);
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

    private static <T> Predicate<T> or(Predicate<T>... predicates) {
        Predicate<T> predicate = predicates[0];

        for (int i = 1; i < predicates.length; i++) {
            predicate = predicate.or(predicates[i]);
        }

        return predicate;
    }

    private static boolean hasComponentProperties(Map<String, String> properties, WithConfigurationProperties... withConfigurationProperties) {
        for (WithConfigurationProperties wcp : withConfigurationProperties) {
            if (properties.entrySet().stream().anyMatch(wcp::isComponentProperty)) {
                return true;
            }
        }

        return false;
    }
}
