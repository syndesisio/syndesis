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
package io.syndesis.server.openshift;

import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class OpenShiftServiceImplTest {
    @Rule
    public TestName testName = new TestName();

    private OpenShiftMockServer server;
    private NamespacedOpenShiftClient client;

    @BeforeClass
    public static void setUpBeforeClass() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void setUp() {
        this.server = new OpenShiftMockServer();
        this.client = server.createOpenShiftClient();
    }

    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.JUnitTestsShouldIncludeAssert"})
    @Test
    public void testDeploy() {
        String name = "test-deployment";
        OpenShiftConfigurationProperties config = new OpenShiftConfigurationProperties();
        OpenShiftServiceImpl service = new OpenShiftServiceImpl(client, config);

        DeploymentData deploymentData = new DeploymentData.Builder()
            .addAnnotation("testName", testName.getMethodName())
            .addLabel("type", "test")
            .addSecretEntry("secret-key", "secret-val")
            .withImage("testimage")
            .build();

        ImageStream expectedImageStream = new ImageStreamBuilder()
            .withNewMetadata()
                .withName(name)
            .endMetadata()
            .build();

        Secret expectedSecret = new SecretBuilder()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withStringData(deploymentData.getSecret())
            .build();

        DeploymentConfig expectedDeploymentConfig = new DeploymentConfigBuilder()
            .withNewMetadata()
                .withName(OpenShiftServiceImpl.openshiftName(name))
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withNewSpec()
                .withReplicas(1)
                .addToSelector("integration", name)
                .withNewStrategy()
                    .withType("Recreate")
                    .withNewResources()
                       .addToLimits("memory", new Quantity(config.getDeploymentMemoryLimitMi()  + "Mi"))
                       .addToRequests("memory", new Quantity(config.getDeploymentMemoryRequestMi() +  "Mi"))
                    .endResources()
                .endStrategy()
                .withRevisionHistoryLimit(0)
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("integration", name)
                        .addToLabels(OpenShiftServiceImpl.COMPONENT_LABEL, "integration")
                        .addToLabels(deploymentData.getLabels())
                        .addToAnnotations(deploymentData.getAnnotations())
                        .addToAnnotations("prometheus.io/scrape", "true")
                        .addToAnnotations("prometheus.io/port", "9779")
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withImage(deploymentData.getImage())
                            .withImagePullPolicy("Always")
                            .withName(name)
                            .addToEnv(new EnvVarBuilder().withName("LOADER_HOME").withValue(config.getIntegrationDataPath()).build())
                            .addToEnv(new EnvVarBuilder().withName("AB_JMX_EXPORTER_CONFIG").withValue("/tmp/src/prometheus-config.yml").build())
                            .addNewPort()
                                .withName("jolokia")
                                .withContainerPort(8778)
                            .endPort()
                            .addNewVolumeMount()
                                .withName("secret-volume")
                                .withMountPath("/deployments/config")
                                .withReadOnly(false)
                            .endVolumeMount()
                        .endContainer()
                        .addNewVolume()
                            .withName("secret-volume")
                            .withNewSecret()
                                .withSecretName(expectedSecret.getMetadata().getName())
                            .endSecret()
                        .endVolume()
                    .endSpec()
                .endTemplate()
                .addNewTrigger()
                    .withType("ConfigChange")
                .endTrigger()
            .endSpec()
            .withNewStatus().withLatestVersion(1L).endStatus()
            .build();

        server.expect()
            .withPath("/oapi/v1/namespaces/test/imagestreams")
            .andReturn(200, expectedImageStream)
            .always();
        server.expect()
            .withPath("/api/v1/namespaces/test/secrets")
            .andReturn(200, expectedSecret)
            .always();
        server.expect()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();
        server.expect()
                .withPath("/oapi/v1/namespaces/test/deploymentconfigs/i-test-deployment")
                .andReturn(200, expectedDeploymentConfig)
                .always();
        server.expect()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs/test-deployment")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        service.deploy(name, deploymentData);
    }
}
