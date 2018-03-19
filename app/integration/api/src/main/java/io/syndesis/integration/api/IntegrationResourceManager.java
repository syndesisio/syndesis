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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;

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
     * Decrypt a property.
     */
    String decrypt(String encrypted);

    /**
     * Collect dependencies.
     */
    default Collection<Dependency> collectDependencies(Integration integration) {
        return collectDependencies(integration.getSteps(), true);
    }

    /**
     * Collect dependencies.
     */
    default Collection<Dependency> collectDependencies(Collection<? extends Step> steps, boolean resolveExtensionTags) {
        final List<Dependency> dependencies = new ArrayList<>();

        for (Step step : steps) {
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

            List<Dependency> lookedUpConnectorDependecies = step.getConnection()
                .filter(c -> !c.getConnector().isPresent())
                .map(Connection::getConnectorId)
                .flatMap(this::loadConnector)
                .map(WithDependencies::getDependencies)
                .orElse(Collections.emptyList());
            dependencies.addAll(lookedUpConnectorDependecies);

            // Connector extension
            Stream.concat(connectorDependencies.stream(), lookedUpConnectorDependecies.stream())
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
