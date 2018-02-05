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
import java.util.stream.Stream;

import io.syndesis.model.Dependency;
import io.syndesis.model.WithDependencies;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;

public interface IntegrationResourceManager {

    Optional<Connector> loadConnector(String id);

    Optional<Extension> loadExtension(String id);

    Optional<InputStream> loadExtensionBLOB(String id);

    String decrypt(String encrypted);

    default Collection<Dependency> collectDependencies(IntegrationDeployment deployment) {
        return collectDependencies(deployment.getSpec().getSteps());
    }

    default Collection<Dependency> collectDependencies(Collection<? extends Step> steps) {
        final List<Dependency> dependencies = new ArrayList<>();

        for (Step step : steps) {
            step.getAction()
                .filter(WithDependencies.class::isInstance)
                .map(WithDependencies.class::cast)
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            List<Dependency> connectorDependecies = step.getConnection()
                .flatMap(Connection::getConnector)
                .map(WithDependencies::getDependencies)
                .orElse(Collections.emptyList());
            dependencies.addAll(connectorDependecies);

            List<Dependency> lookedUpConnectorDependecies = step.getConnection()
                .filter(c -> !c.getConnector().isPresent())
                .flatMap(Connection::getConnectorId)
                .flatMap(this::loadConnector)
                .map(WithDependencies::getDependencies)
                .orElse(Collections.emptyList());
            dependencies.addAll(lookedUpConnectorDependecies);

            // Connector extension
            Stream.concat(connectorDependecies.stream(), lookedUpConnectorDependecies.stream())
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

        return dependencies;
    }
}
