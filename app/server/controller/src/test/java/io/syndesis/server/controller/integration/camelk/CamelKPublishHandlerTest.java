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
package io.syndesis.server.controller.integration.camelk;

import java.util.Properties;

import com.jcabi.manifests.Manifests;
import io.fabric8.kubernetes.api.model.Secret;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.integration.project.generator.ProjectGenerator;
import io.syndesis.integration.project.generator.ProjectGeneratorConfiguration;
import io.syndesis.server.endpoint.v1.VersionService;
import io.syndesis.server.openshift.OpenShiftServiceNoOp;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CamelKPublishHandlerTest {

    @BeforeClass
    public static void testInit(){

    }

    // ****************************
    //
    // Resources
    //
    // ****************************

    private final static ConnectorAction HTTP_ACTION = new ConnectorAction.Builder()
        .id(KeyGenerator.createKey())
        .descriptor(new ConnectorDescriptor.Builder()
            .connectorId("http-connector")
            .build())
        .build();

    private final static Connector HTTP_CONNECTOR = new Connector.Builder()
        .id("http-connector")
        .addAction(HTTP_ACTION)
        .componentScheme("http4")
        .putProperty("username",
            new ConfigurationProperty.Builder()
                .secret(true)
                .build())
        .putProperty("password",
            new ConfigurationProperty.Builder()
                .secret(true)
                .build())
        .putProperty("token",
            new ConfigurationProperty.Builder()
                .secret(true)
                .build())
        .build();

    private final static Connection HTTP_CONNECTION = new Connection.Builder()
        .connector(HTTP_CONNECTOR)
        .build();

    // ****************************
    //
    // Tests
    //
    // ****************************

    @Test
    public void testCamelkIntegrationSecret() throws Exception {
        TestResourceManager manager = new TestResourceManager();

        Integration integration = manager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("username", "admin")
                .action(HTTP_ACTION)
                .connection(HTTP_CONNECTION)
                .build()
        );

        ProjectGenerator generator = new ProjectGenerator(new ProjectGeneratorConfiguration(), manager, new MavenProperties());
        IntegrationDeployment deployment = new IntegrationDeployment.Builder().spec(integration).build();

        CamelKPublishHandler handler = new CamelKPublishHandler(
            new OpenShiftServiceNoOp(),
            null,
            null,
            generator,
            null,
            manager,
            new VersionService());

        Secret secret = handler.createIntegrationSecret(deployment);

        assertThat(secret.getMetadata()).hasFieldOrPropertyWithValue("name", integration.getId().get());
        assertThat(secret.getStringData()).containsKeys("application.properties");

        Properties actual = CamelKSupport.secretToProperties(secret);
        Properties expected = generator.generateApplicationProperties(integration);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual).containsEntry("flow-0.http4-0.username", "admin");
    }


    @Test
    public void testCamelkIntegration() throws Exception {
        TestResourceManager manager = new TestResourceManager();

        Integration integration = manager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .putConfiguredProperty("username", "admin")
                .action(HTTP_ACTION)
                .connection(HTTP_CONNECTION)
                .build(),
            new Step.Builder()
                .stepKind(StepKind.mapper)
                .putConfiguredProperty("atlasmapping", "some data")
                .build()
        );

        ProjectGenerator generator = new ProjectGenerator(new ProjectGeneratorConfiguration(), manager, new MavenProperties());
        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .spec(integration)
            .build();

        CamelKPublishHandler handler = new CamelKPublishHandler(
            new OpenShiftServiceNoOp(),
            null,
            null,
            generator,
            null,
            manager,
            new VersionService());


        io.syndesis.server.controller.integration.camelk.crd.Integration i = handler.createIntegrationCR(deployment);

        assertThat(i.getSpec().getSources()).isNotEmpty();
        assertThat(i.getSpec().getDependencies()).isNotEmpty();
        assertThat(i.getSpec().getResources()).isNotEmpty();
    }
}
