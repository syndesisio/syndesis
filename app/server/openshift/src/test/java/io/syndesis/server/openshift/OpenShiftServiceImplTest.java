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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.bridge.SLF4JBridgeHandler;

import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import com.fasterxml.jackson.core.JsonProcessingException;

import okhttp3.mockwebserver.RecordedRequest;

import static io.fabric8.kubernetes.client.utils.Serialization.asJson;
import static io.syndesis.server.openshift.OpenShiftServiceImpl.openshiftName;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenShiftServiceImplTest {
    @Rule
    public TestName testName = new TestName();

    private OpenShiftMockServer server;
    private NamespacedOpenShiftClient client;
    private OpenShiftServiceImpl service;
    private OpenShiftConfigurationProperties config;

    private final static class Request {

        private final String method;
        private final String path;
        private final String body;

        private Request(String method, String path, String body) {
            this.method = method;
            this.path = path;
            this.body = body;
        }

        private static Request createFrom(RecordedRequest req) {
            return new Request(req.getMethod(), req.getPath(), req.getBody().readUtf8());
        }

        public static Request with(String method, String path, Object body) {
            return new Request(method, path, asJson(body));
        }

        public static Request with(String method, String path) {
            return new Request(method, path, "*");
        }

        @Override
        public String toString() {
            return method + " " + path + "\n" + body;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof Request)) {
                return false;
            }

            final Request other = (Request) obj;

            return Objects.equals(method, other.method)
                && Objects.equals(path, other.path)
                && (Objects.equals(body, other.body) || "*".equals(body) || "*".equals(other.body));
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, path);
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void setUp() {
        server = new OpenShiftMockServer();
        client = server.createOpenShiftClient();

        config = new OpenShiftConfigurationProperties();
        service = new OpenShiftServiceImpl(client, config);
    }

    @Test
    public void testDeploy() throws InterruptedException, JsonProcessingException {
        final String name = "test-deployment";

        final DeploymentData deploymentData = new DeploymentData.Builder()
            .addAnnotation("testName", testName.getMethodName())
            .addLabel("type", "test")
            .addSecretEntry("secret-key", "secret-val")
            .withImage("testimage")
            .build();

        final Secret expectedSecret = secretFor(name, deploymentData);

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor(name, deploymentData).build();

        server.expect()
            .post()
            .withPath("/api/v1/namespaces/test/secrets")
            .andReturn(200, expectedSecret)
            .always();

        expectDeploymentOf(name, expectedDeploymentConfig);

        service.deploy(name, deploymentData);

        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("POST", "/oapi/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/secrets", expectedSecret));
    }

    @Test
    public void shouldNotExposeUnexposedDeployments() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.noneOf(Exposure.class))
            .build();

        final String name = "unexposed";

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor(name, deploymentData)
            .build();

        expectDeploymentOf(name, expectedDeploymentConfig);

        service.deploy(name, deploymentData);

        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("POST", "/oapi/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/oapi/v1/namespaces/test/routes"));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/oapi/v1/namespaces/test/services"));
    }

    @Test
    public void shouldExposeDeploymentsVia3ScaleServiceAnnotations() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.of(Exposure.SERVICE, Exposure._3SCALE))
            .build();

        final String name = "via service and 3scale";

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor(name, deploymentData)
            .build();

        expectDeploymentOf(name, expectedDeploymentConfig);

        final Service expectedService = new ServiceBuilder()
            .withNewMetadata()
                .withName(openshiftName(name))
                .addToLabels("discovery.3scale.net", "true")
                .addToAnnotations("discovery.3scale.net/scheme", "http")
                .addToAnnotations("discovery.3scale.net/port", "8080")
                .addToAnnotations("discovery.3scale.net/description-path", "/openapi.json")
            .endMetadata()
            .withNewSpec()
                .addNewPort()
                    .withName("http")
                    .withPort(8080)
                    .withProtocol("TCP")
                    .withTargetPort(new IntOrString(8080))
                .endPort()
                .addToSelector("syndesis.io/integration", openshiftName(name))
            .endSpec()
            .build();

        server.expect()
            .post()
            .withPath("/api/v1/namespaces/test/services")
            .andReturn(200, expectedService)
            .always();

        service.deploy(name, deploymentData);
        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("POST", "/oapi/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/oapi/v1/namespaces/test/routes"));
    }

    @Test
    public void shouldExposeDeploymentsViaServices() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.of(Exposure.SERVICE))
            .build();

        final String name = "via-service";

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor(name, deploymentData)
            .build();

        expectDeploymentOf(name, expectedDeploymentConfig);

        final Service expectedService = new ServiceBuilder()
            .withNewMetadata()
                .withName(openshiftName(name))
            .endMetadata()
            .withNewSpec()
                .addNewPort()
                    .withName("http")
                    .withPort(8080)
                    .withProtocol("TCP")
                    .withTargetPort(new IntOrString(8080))
                .endPort()
                .addToSelector("syndesis.io/integration", openshiftName(name))
            .endSpec()
            .build();

        server.expect()
            .post()
            .withPath("/api/v1/namespaces/test/services")
            .andReturn(200, expectedService)
            .always();

        service.deploy(name, deploymentData);
        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("POST", "/oapi/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/oapi/v1/namespaces/test/routes"));
    }

    @Test
    public void shouldExposeDeploymentsViaServicesAndRoutes() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.of(Exposure.SERVICE, Exposure.ROUTE))
            .build();

        final String name = "via-service-and-route";

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor(name, deploymentData)
            .build();

        expectDeploymentOf(name, expectedDeploymentConfig);

        final Service expectedService = new ServiceBuilder()
            .withNewMetadata()
                .withName(openshiftName(name))
            .endMetadata()
            .withNewSpec()
                .addNewPort()
                    .withName("http")
                    .withPort(8080)
                    .withProtocol("TCP")
                    .withTargetPort(new IntOrString(8080))
                .endPort()
                .addToSelector("syndesis.io/integration", openshiftName(name))
            .endSpec()
            .build();

        server.expect()
            .post()
            .withPath("/api/v1/namespaces/test/services")
            .andReturn(200, expectedService)
            .always();

        final Route expectedRoute = new RouteBuilder()
            .withApiVersion("route.openshift.io/v1")
            .withKind("Route")
            .withNewMetadata()
                .withName(openshiftName(name))
            .endMetadata()
            .withNewSpec()
                .withNewTls()
                    .withTermination("edge")
                .endTls()
                .withNewTo()
                    .withKind("Service")
                    .withName(openshiftName(name))
                .endTo()
            .endSpec()
            .build();

        server.expect()
            .post()
            .withPath("/oapi/v1/namespaces/test/routes")
            .andReturn(200, expectedRoute)
            .always();

        service.deploy(name, deploymentData);
        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("POST", "/oapi/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).contains(Request.with("POST", "/oapi/v1/namespaces/test/routes"));
    }

    @Test
    public void shouldRemoveExposureViaRouteIfChangedTo3scale() {
        shouldExposeDeploymentsViaServicesAndRoutes();

        final DeploymentData deploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.of(Exposure.SERVICE, Exposure._3SCALE))
            .build();

        final String name = "via-service-and-route";

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor(name, deploymentData)
            .build();

        expectDeploymentOf(name, expectedDeploymentConfig);

        final Service expectedService = new ServiceBuilder()
            .withNewMetadata()
            .withName(openshiftName(name))
            .addToLabels("discovery.3scale.net", "true")
            .addToAnnotations("discovery.3scale.net/scheme", "http")
            .addToAnnotations("discovery.3scale.net/port", "8080")
            .addToAnnotations("discovery.3scale.net/description-path", "/openapi.json")
            .endMetadata()
            .withNewSpec()
            .addNewPort()
            .withName("http")
            .withPort(8080)
            .withProtocol("TCP")
            .withTargetPort(new IntOrString(8080))
            .endPort()
            .addToSelector("syndesis.io/integration", openshiftName(name))
            .endSpec()
            .build();

        server.expect()
            .post()
            .withPath("/api/v1/namespaces/test/services")
            .andReturn(200, expectedService)
            .always();

        server.expect()
            .patch()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs/i-via-service-and-route")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        service.deploy(name, deploymentData);
        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("PATCH", "/oapi/v1/namespaces/test/deploymentconfigs/i-via-service-and-route", Collections.EMPTY_LIST));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).contains(Request.with("DELETE", "/oapi/v1/namespaces/test/routes/i-via-service-and-route"));
    }

    DeploymentConfigBuilder baseDeploymentFor(final String name, final DeploymentData deploymentData) {
        return new DeploymentConfigBuilder()
            .withNewMetadata()
                .withName(openshiftName(name))
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
                .addToLabels(OpenShiftServiceImpl.defaultLabels())
            .endMetadata()
            .withNewSpec()
                .withReplicas(1)
                .addToSelector("syndesis.io/integration", openshiftName(name))
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
                        .addToLabels("syndesis.io/integration", openshiftName(name))
                        .addToLabels(OpenShiftServiceImpl.COMPONENT_LABEL, "integration")
                        .addToLabels(OpenShiftServiceImpl.defaultLabels())
                        .addToLabels(deploymentData.getLabels())
                        .addToAnnotations(deploymentData.getAnnotations())
                        .addToAnnotations("prometheus.io/scrape", "true")
                        .addToAnnotations("prometheus.io/port", "9779")
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withImage(deploymentData.getImage())
                            .withImagePullPolicy("Always")
                            .withName(openshiftName(name))
                            .addToEnv(new EnvVarBuilder().withName("LOADER_HOME").withValue(config.getIntegrationDataPath()).build())
                            .addToEnv(new EnvVarBuilder().withName("AB_JMX_EXPORTER_CONFIG").withValue("/tmp/src/prometheus-config.yml").build())
                            .addToEnv(new EnvVarBuilder().withName("JAEGER_ENDPOINT").withValue("http://syndesis-jaeger-collector:14268/api/traces").build())
                            .addToEnv(new EnvVarBuilder().withName("JAEGER_TAGS").withValue("integration.version="+deploymentData.getVersion()).build())
                            .addToEnv(new EnvVarBuilder().withName("JAEGER_SAMPLER_TYPE").withValue("const").build())
                            .addToEnv(new EnvVarBuilder().withName("JAEGER_SAMPLER_PARAM").withValue("1").build())
                            .addNewPort()
                                .withName("jolokia")
                                .withContainerPort(8778)
                            .endPort()
                            .addNewPort()
                                .withName("metrics")
                                .withContainerPort(9779)
                            .endPort()
                            .addNewPort()
                                .withName("management")
                                .withContainerPort(8081)
                            .endPort()
                            .addNewVolumeMount()
                                .withName("secret-volume")
                                .withMountPath("/deployments/config")
                                .withReadOnly(false)
                            .endVolumeMount()
                            .withLivenessProbe(new ProbeBuilder()
                                .withInitialDelaySeconds(config.getIntegrationLivenessProbeInitialDelaySeconds())
                                .withNewHttpGet()
                                    .withPath("/health")
                                    .withNewPort(8081)
                                .endHttpGet()
                                .build())
                        .endContainer()
                        .addNewVolume()
                            .withName("secret-volume")
                            .withNewSecret()
                                .withSecretName(openshiftName(name))
                            .endSecret()
                        .endVolume()
                    .endSpec()
                .endTemplate()
                .addNewTrigger()
                    .withNewImageChangeParams()
                        .withAutomatic(true)
                        .withContainerNames(openshiftName(name))
                        .withNewFrom()
                            .withKind("ImageStreamTag")
                            .withName(openshiftName(name) + ":0")
                        .endFrom()
                    .endImageChangeParams()
                    .withType("ImageChange")
                .endTrigger()
                .addNewTrigger()
                    .withType("ConfigChange")
                .endTrigger()
            .endSpec();
    }


    void expectDeploymentOf(final String name, final DeploymentConfig expectedDeploymentConfig) {
        final DeploymentConfig deployed = new DeploymentConfigBuilder(expectedDeploymentConfig)
            .withNewStatus()
                .withLatestVersion(1L)
            .endStatus()
            .build();
        server.expect()
            .get()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs/" + openshiftName(name))
            .andReturn(404, new StatusBuilder().withCode(404).build())
            .times(1);
        server.expect()
            .get()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs/" + openshiftName(name))
            .andReturn(200, deployed)
            .always();
        server.expect()
            .patch()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs/" + openshiftName(name))
            .andReturn(200, deployed)
            .always();
        server.expect()
            .post()
            .withPath("/oapi/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();
    }

    List<Request> gatherRequests() {
        final List<Request> issued = new ArrayList<>();
        RecordedRequest taken;
        try {
            while ((taken = server.takeRequest(5, TimeUnit.MILLISECONDS)) != null) {
                final Request request = Request.createFrom(taken);
                issued.add(request);
            }
        } catch (InterruptedException e) {
            return issued;
        }

        return issued;
    }

    static Secret secretFor(final String name, final DeploymentData deploymentData) {
        final Secret expectedSecret = new SecretBuilder()
            .withNewMetadata()
                .withName(openshiftName(name))
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withStringData(deploymentData.getSecret())
            .build();
        return expectedSecret;
    }
}
