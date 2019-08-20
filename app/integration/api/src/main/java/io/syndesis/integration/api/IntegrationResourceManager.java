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
package io.syndesis.integration.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Dependency.Type;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionBase;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.Names;
import io.syndesis.common.util.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

public interface IntegrationResourceManager {

    /**
     * Load a connector from the underlying storage by id.
     */
    Optional<Connector> loadConnector(String id);

    /**
     * Load a connector from the give connection or from the underlying storage
     * if the connector is referenced by id.
     */
    default Optional<Connector> loadConnector(Connection connection) {
        final Optional<Connector> connector;

        if (connection.getConnector().isPresent()) {
            connector = connection.getConnector();
        } else {
            connector = loadConnector(connection.getConnectorId());
        }

        return connector;
    }

    /**
     * Load an extension from the underlying storage by id.
     */
    Optional<Extension> loadExtension(String id);

    /**
     * Load all extensions belonging to a specific tag.
     */
    List<Extension> loadExtensionsByTag(String tag);

    /**
     * Load an extension binary from the underlying storage by id.
     */
    Optional<InputStream> loadExtensionBLOB(String id);

    /**
     * Load an OpenApi definition from the underlying storage by id.
     */
    Optional<OpenApi> loadOpenApiDefinition(String id);

    /**
     * Decrypt a property.
     */
    String decrypt(String encrypted);

    /**
     * Collect dependencies.
     */
    default Collection<Dependency> collectDependencies(Integration integration) {
        return collectDependencies(integration.getFlows().stream().flatMap(flow -> flow.getSteps().stream()).collect(Collectors.toList()), true);
    }


    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    default Integration sanitize(Integration integration) {
        // Always sanitize the integration name
        String sanitizeIntegrationName = sanitize(integration.getName());

        if (integration.getFlows().isEmpty()) {
            return new Integration.Builder().createFrom(integration)
                .name(sanitizeIntegrationName).build();
        }

        final List<Flow> replacementFlows = new ArrayList<>(integration.getFlows());
        final ListIterator<Flow> flows = replacementFlows.listIterator();
        while (flows.hasNext()) {
            final Flow flow = flows.next();
            if (flow.getSteps().isEmpty()) {
                continue;
            }

            final List<Step> replacementSteps = new ArrayList<>(flow.getSteps());
            final ListIterator<Step> steps = replacementSteps.listIterator();

            while (steps.hasNext()) {
                final Step source = steps.next();

                Step replacement = source;
                if (source.getConnection().isPresent()) {
                    final Connection connection = source.getConnection().get();

                    // If connector is not set, fetch it from data source and update connection
                    if (!connection.getConnector().isPresent()) {
                        Connector connector = loadConnector(connection.getConnectorId()).orElseThrow(
                            () -> new IllegalArgumentException("Unable to fetch connector: " + connection.getConnectorId())
                        );
                        // Add missing connector to connection.
                        Connection newConnection = new Connection.Builder()
                            .createFrom(connection)
                            .connector(connector)
                            .build();
                        // Replace with the new 'sanitized' step
                        replacement =
                            new Step.Builder()
                                .createFrom(source)
                                .connection(newConnection)
                                .build();
                    }
                    // Prune Connector, nix actions. The action in use is on the Step
                    Connector prunedConnector = new Connector.Builder()
                        .createFrom(replacement.getConnection().get().getConnector().get())
                        .actions(new ArrayList<>())
                        .icon(null)
                        .build();

                    // Replace with the new 'pruned' connector
                    Connection prunedConnection = new Connection.Builder()
                        .createFrom(connection)
                        .connector(prunedConnector)
                        .icon(null)
                        .build();
                    // Replace with the new 'pruned' step
                    replacement =
                        new Step.Builder()
                            .createFrom(source)
                            .connection(prunedConnection)
                            .build();
                }

                //
                // If a template step then update it to ensure it
                // has the correct dependencies
                //
                steps.set(TemplateStepLanguage.updateStep(replacement));
            }

            final Flow.Builder replacementFlowBuilder = flow.builder().createFrom(flow).steps(replacementSteps);
            flows.set(replacementFlowBuilder.build());

            // Temporary implementation until https://github.com/syndesisio/syndesis/issues/736
            // is fully implemented and schedule options are set on integration.
            if (!flow.getScheduler().isPresent()) {
                final Step firstStep = replacementSteps.get(0);
                final Map<String, String> properties = new HashMap<>(firstStep.getConfiguredProperties());
                String type = properties.remove("schedulerType");
                final String expr = properties.remove("schedulerExpression");

                if (StringUtils.isNotEmpty(expr)) {
                    if (StringUtils.isEmpty(type)) {
                        type = "timer";
                    }

                    final Scheduler scheduler = new Scheduler.Builder().type(Scheduler.Type.valueOf(type)).expression(expr).build();
                    final Flow replacementFlow = replacementFlowBuilder.scheduler(scheduler).build();

                    flows.set(replacementFlow);
                }

                // Replace first step so underlying connector won't fail uri param
                // validation if schedule options were set.
                steps.set(
                    new Step.Builder()
                        .createFrom(firstStep)
                        .configuredProperties(properties)
                        .build()
                );
            }
        }

        return new Integration.Builder().createFrom(integration)
            .name(sanitizeIntegrationName)
            .flows(replacementFlows)
            .build();
    }

    /**
     * Sanitize an integration name according a specific set of characters
     *
     * @param name the name to sanitize
     * @return a sanitized name replacing illegal characters
     */
    default String sanitize(String name) {
        return name == null ? null : Names.sanitize(name);
    }

    /**
     * Collect dependencies.
     */
    default Collection<Dependency> collectDependencies(Collection<? extends Step> steps, boolean resolveExtensionTags) {
        final List<Dependency> dependencies = new ArrayList<>();

        for (Step step : steps) {
            dependencies.addAll(step.getDependencies());

            step.getAction()
                .filter(WithDependencies.class::isInstance)
                .map(WithDependencies.class::cast)
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            List<Dependency> connectorDependencies = step.getConnection()
                .flatMap(Connection::getConnector)
                .map(WithDependencies::getDependencies)
                .orElse(Collections.emptyList());
            dependencies.addAll(connectorDependencies);

            List<Dependency> lookedUpConnectorDependencies = step.getConnection()
                .filter(c -> !c.getConnector().isPresent())
                .map(Connection::getConnectorId)
                .flatMap(this::loadConnector)
                .map(WithDependencies::getDependencies)
                .orElse(Collections.emptyList());
            dependencies.addAll(lookedUpConnectorDependencies);

            // Custom Icon
            step.getConnection().
                flatMap(ConnectionBase::getConnector).
                flatMap(ctr -> Optional.ofNullable(ctr.getIcon())).
                filter(icon -> icon.startsWith("db:")).
                ifPresent(icon -> dependencies.add(Dependency.from(Type.ICON, icon)));

            // Connector extension
            Stream.concat(connectorDependencies.stream(), lookedUpConnectorDependencies.stream())
                .filter(Dependency::isExtension)
                .map(Dependency::getId)
                .map(this::loadExtension)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Extension::getDependencies)
                .forEach(dependencies::addAll);

            // Step extension
            step.getExtension()
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            step.getExtension()
                .map(Extension::getExtensionId)
                .map(Dependency::extension)
                .ifPresent(dependencies::add);
        }

        if (resolveExtensionTags) {
            return dependencies.stream()
                .flatMap(dep -> {
                    if (dep.isExtensionTag()) {
                        List<Extension> extensions = this.loadExtensionsByTag(dep.getId());

                        Stream<Dependency> extensionDependency = extensions.stream().map(ext -> Dependency.extension(ext.getExtensionId()));
                        Stream<Dependency> transitive = extensions.stream().map(Extension::getDependencies).flatMap(Collection::stream);
                        return Stream.concat(extensionDependency, transitive);
                    } else {
                        return Stream.of(dep);
                    }
                }).collect(Collectors.toCollection(ArrayList::new));
        } else {
            return dependencies;
        }
    }

}
