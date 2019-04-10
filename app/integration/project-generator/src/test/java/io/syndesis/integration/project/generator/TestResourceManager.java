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
package io.syndesis.integration.project.generator;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.integration.api.IntegrationResourceManager;
import org.apache.commons.io.IOUtils;

final class TestResourceManager implements IntegrationResourceManager {
    private final ConcurrentMap<String, Object> resources;

    TestResourceManager() {
        resources = new ConcurrentHashMap<>();
    }

    public void put(String id, Object resource) {
        resources.put(id, resource);
    }

    @Override
    public Optional<Connector> loadConnector(String id) {
        return Optional.ofNullable(resources.get(id))
            .filter(Connector.class::isInstance)
            .map(Connector.class::cast);
    }

    @Override
    public Optional<Extension> loadExtension(String id) {
        return Optional.ofNullable(resources.get(id))
            .filter(Extension.class::isInstance)
            .map(Extension.class::cast);
    }

    @Override
    public List<Extension> loadExtensionsByTag(String tag) {
        return resources.values().stream()
            .filter(Extension.class::isInstance)
            .map(Extension.class::cast)
            .filter(extension ->  extension.getTags().contains(tag))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<InputStream> loadExtensionBLOB(String id) {
        final InputStream is = IOUtils.toInputStream(id, StandardCharsets.UTF_8);

        return Optional.of(is);
    }

    @Override
    public Optional<OpenApi> loadOpenApiDefinition(String id) {
        Object res = resources.get(id);
        if (res == null) {
            return Optional.empty();
        }

        if (res instanceof OpenApi) {
            return Optional.of((OpenApi) res);
        }

        return Optional.empty();
    }

    @Override
    public String decrypt(String encrypted) {
        return encrypted;
    }


    Integration newIntegration(Step... steps) {
        for (int i = 0; i < steps.length; i++) {
            steps[i].getConnection()
                    .filter(r -> r.getId().isPresent())
                    .ifPresent(r -> this.put(r.getId().get(), r));

            steps[i].getAction()
                    .filter(ConnectorAction.class::isInstance)
                    .map(ConnectorAction.class::cast)
                    .filter(r -> r.getId().isPresent())
                    .ifPresent(r -> this.put(r.getId().get(), r));

            steps[i].getExtension()
                    .filter(r -> r.getId().isPresent())
                    .ifPresent(r -> this.put(r.getId().get(), r)
            );

            steps[i] = new Step.Builder().createFrom(steps[i]).build();
        }

        return new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addFlow(new Flow.Builder()
                    .steps(Arrays.asList(steps))
                .build())
            .build();
    }
}
