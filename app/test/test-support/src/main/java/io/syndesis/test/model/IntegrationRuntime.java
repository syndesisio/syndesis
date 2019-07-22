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

package io.syndesis.test.model;

import java.util.stream.Stream;

import io.syndesis.test.integration.project.CamelKProjectBuilder;
import io.syndesis.test.integration.project.ProjectBuilder;
import io.syndesis.test.integration.project.SpringBootProjectBuilder;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

/**
 * Integration runtime provides runtime specific settings and configuration.
 */
public class IntegrationRuntime {
    public static final IntegrationRuntime SPRING_BOOT = new IntegrationRuntime("spring-boot",
            "spring-boot:run",
            Wait.forLogMessage(".*Started Application.*\\s", 1),
            SpringBootProjectBuilder::new);


    public static final IntegrationRuntime CAMEL_K = new IntegrationRuntime("camel-k",
            "process-resources exec:java",
            Wait.forLogMessage(".*Apache Camel .* started.*\\s", 1),
            CamelKProjectBuilder::new);

    private final String id;
    private final String command;
    private final WaitStrategy readinessProbe;
    private final ProjectBuilderSupplier projectBuilderSupplier;

    interface ProjectBuilderSupplier {
        ProjectBuilder get(String name, String syndesisVersion);
    }

    private IntegrationRuntime(String id, String command, WaitStrategy readinessProbe, ProjectBuilderSupplier projectBuilderSupplier) {
        this.id = id;
        this.command = command;
        this.readinessProbe = readinessProbe;
        this.projectBuilderSupplier = projectBuilderSupplier;
    }

    public String getCommand() {
        return command;
    }

    public String getId() {
        return id;
    }

    public WaitStrategy getReadinessProbe() {
        return readinessProbe;
    }

    public ProjectBuilder getProjectBuilder(String name, String syndesisVersion) {
        return projectBuilderSupplier.get(name, syndesisVersion);
    }

    public static IntegrationRuntime fromId(String id) {
        return Stream.of(CAMEL_K, SPRING_BOOT)
                .filter(runtime -> runtime.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported integration runtime '%s'", id)));
    }
}
