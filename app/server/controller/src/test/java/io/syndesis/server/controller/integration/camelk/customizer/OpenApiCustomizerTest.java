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
package io.syndesis.server.controller.integration.camelk.customizer;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.integration.camelk.TestResourceManager;
import io.syndesis.server.openshift.Exposure;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiCustomizerTest {
    @Test
    public void testOpenApiCustomizer() throws Exception {
        URL location = getClass().getResource("/petstore.json");
        byte[] content = Files.readAllBytes(Paths.get(location.toURI()));

        TestResourceManager manager = new TestResourceManager();
        manager.put("petstore", new OpenApi.Builder().document(content).id("petstore").build());

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addResource(new ResourceIdentifier.Builder()
                .kind(Kind.OpenApi)
                .id("petstore")
                .build())
            .build();

        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new OpenApiCustomizer(new ControllersConfigurationProperties(), manager);

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.SERVICE)
        );

        assertThat(i.getSpec().getConfiguration()).hasSize(2);
        assertThat(i.getSpec().getConfiguration()).anyMatch(
            c -> Objects.equals("customizer.servletregistration.enabled=true", c.getValue())
                    && Objects.equals("property", c.getType())
        );
        assertThat(i.getSpec().getConfiguration()).anyMatch(
            c -> Objects.equals("customizer.servletregistration.path=/*", c.getValue())
                && Objects.equals("property", c.getType())
        );
        assertThat(i.getSpec().getSources()).anyMatch(
            s -> Objects.equals("openapi-routes", s.getDataSpec().getName()) && Objects.equals("xml", s.getLanguage())
                && !s.getDataSpec().getCompression().booleanValue()
        );
        assertThat(i.getSpec().getSources()).anyMatch(
            s -> Objects.equals("openapi-endpoint", s.getDataSpec().getName()) && Objects.equals("xml", s.getLanguage())
                && !s.getDataSpec().getCompression().booleanValue()
        );
        assertThat(i.getSpec().getResources()).anyMatch(
            s -> Objects.equals("openapi.json", s.getDataSpec().getName()) && Objects.equals("data", s.getType())
                && s.getDataSpec().getCompression().booleanValue() && (s.getDataSpec().getContent().getBytes(UTF_8).length <= content.length)
        );
    }
}
