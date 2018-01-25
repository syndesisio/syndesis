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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.syndesis.model.Dependency;
import io.syndesis.model.action.StepAction;
import io.syndesis.model.action.StepDescriptor;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.SimpleStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.ExcessiveImports")
@RunWith(Parameterized.class)
public class ProjectGeneratorTest extends ProjectGeneratorTestSupport {
    private final String basePath;
    private final List<ProjectGeneratorConfiguration.Templates.Resource> additionalResources;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                "",
                Collections.emptyList()
            },
            {
                "redhat",
                Arrays.asList(
                    new ProjectGeneratorConfiguration.Templates.Resource("deployment.yml", "src/main/fabric8/deployment.yml"),
                    new ProjectGeneratorConfiguration.Templates.Resource("settings.xml", "configuration/settings.xml")
                )
            }
        });
    }

    public ProjectGeneratorTest(String basePath, List<ProjectGeneratorConfiguration.Templates.Resource> additionalResources) {
        this.basePath = basePath;
        this.additionalResources = additionalResources;
    }

    // ***************************
    // Tests
    // ***************************

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    @Test
    public void testGenerate() throws Exception {
        IntegrationDeployment deployment = newIntegration(
            new SimpleStep.Builder()
                .stepKind("endpoint")
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connector(TIMER_CONNECTOR)
                    .build())
                .putConfiguredProperty("period", "5000")
                .action(PERIODIC_TIMER_ACTION)
                .build(),
            new SimpleStep.Builder()
                .stepKind("mapper")
                .putConfiguredProperty("atlasmapping", "{}")
                .build(),
            new RuleFilterStep.Builder()
                .putConfiguredProperty("predicate", FilterPredicate.AND.toString())
                .putConfiguredProperty("rules", "[{ \"path\": \"in.header.counter\", \"op\": \">\", \"value\": \"10\" }]")
                .build(),
            new SimpleStep.Builder()
                .stepKind("extension")
                .extension(new Extension.Builder()
                    .id("my-extension-1")
                    .extensionId("my-extension-1")
                    .addDependency(Dependency.maven("org.slf4j:slf4j-api:1.7.11"))
                    .addDependency(Dependency.maven("org.slf4j:slf4j-simple:1.7.11"))
                    .addDependency(Dependency.maven("org.apache.camel:camel-spring-boot-starter:2.10.0"))
                    .build())
                .putConfiguredProperty("key-1", "val-1")
                .putConfiguredProperty("key-2", "val-2")
                .action(new StepAction.Builder()
                    .id("my-extension-1-action-1")
                    .descriptor(new StepDescriptor.Builder()
                        .kind(StepAction.Kind.ENDPOINT)
                        .entrypoint("direct:extension")
                        .build()
                    ).build())
                .build(),
            new SimpleStep.Builder()
                .stepKind("extension")
                .extension(new Extension.Builder()
                    .id("my-extension-2")
                    .extensionId("my-extension-2")
                    .build())
                .putConfiguredProperty("key-1", "val-1")
                .putConfiguredProperty("key-2", "val-2")
                .action(new StepAction.Builder()
                    .id("my-extension-1-action-1")
                    .descriptor(new StepDescriptor.Builder()
                        .kind(StepAction.Kind.BEAN)
                        .entrypoint("com.example.MyExtension::action")
                        .build()
                    ).build())
                .build(),
            new SimpleStep.Builder()
                .stepKind("extension")
                .extension(new Extension.Builder()
                    .id("my-extension-3")
                    .extensionId("my-extension-3")
                    .build())
                .putConfiguredProperty("key-1", "val-1")
                .putConfiguredProperty("key-2", "val-2")
                .action(new StepAction.Builder()
                    .id("my-extension-2-action-1")
                    .descriptor(new StepDescriptor.Builder()
                        .kind(StepAction.Kind.STEP)
                        .entrypoint("com.example.MyStep")
                        .build()
                    ).build())
                .build(),
            new SimpleStep.Builder()
                .stepKind("endpoint")
                .connection(new Connection.Builder()
                    .id("http-connection")
                    .connector(HTTP_CONNECTOR)
                    .build())
                .putConfiguredProperty("httpUri", "http://localhost:8080/hello")
                .putConfiguredProperty("username", "admin")
                .putConfiguredProperty("password", "admin")
                .putConfiguredProperty("token", "mytoken")
                .action(HTTP_GET_ACTION)
                .build()
        );

        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        configuration.getMavenProperties().addRepository("maven.central", "https://repo1.maven.org/maven2");
        configuration.getMavenProperties().addRepository("redhat.ga", "https://maven.repository.redhat.com/ga");
        configuration.getMavenProperties().addRepository("jboss.ea", "https://repository.jboss.org/nexus/content/groups/ea");

        configuration.getTemplates().setOverridePath(this.basePath);
        configuration.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        configuration.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(deployment, configuration);

        assertFileContents(configuration, runtimeDir.resolve("src/main/resources/application.properties"), "application.properties");
        assertFileContents(configuration, runtimeDir.resolve("src/main/resources/loader.properties"), "loader.properties");
        assertFileContents(configuration, runtimeDir.resolve("src/main/resources/syndesis/integration/integration.json"), "integration.json");
        assertFileContents(configuration, runtimeDir.resolve("pom.xml"), "pom.xml");
        assertFileContents(configuration, runtimeDir.resolve(".s2i/bin/assemble"), "assemble");

        assertThat(runtimeDir.resolve("extensions/my-extension-1.jar")).exists();
        assertThat(runtimeDir.resolve("extensions/my-extension-2.jar")).exists();
        assertThat(runtimeDir.resolve("extensions/my-extension-3.jar")).exists();
        assertThat(runtimeDir.resolve("src/main/resources/mapping-step-2.json")).exists();
    }
}
