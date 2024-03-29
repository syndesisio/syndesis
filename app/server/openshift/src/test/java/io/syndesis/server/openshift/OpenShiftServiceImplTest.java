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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.DeleteOptionsBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ReplicationControllerListBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.ServerRequest;
import io.fabric8.mockwebserver.ServerResponse;
import io.fabric8.mockwebserver.internal.MockDispatcher;
import io.fabric8.openshift.api.model.BuildBuilder;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.ImageStreamTagBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static io.fabric8.kubernetes.client.utils.Serialization.asJson;
import static io.syndesis.server.openshift.OpenShiftServiceImpl.openshiftName;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;

public class OpenShiftServiceImplTest {

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


    @BeforeAll
    public static void setupLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @BeforeEach
    public void setUp() {
        final HashMap<ServerRequest, Queue<ServerResponse>> responses = new HashMap<ServerRequest, Queue<ServerResponse>>();
        server = new OpenShiftMockServer(new Context(), new MockWebServer(), responses, new MockDispatcher(responses) {
            @Override
            public MockResponse peek() {
                return new MockResponse().setSocketPolicy(SocketPolicy.EXPECT_CONTINUE);
            }
        }, true);
        client = server.createOpenShiftClient();

        config = new OpenShiftConfigurationProperties();
        config.setPollingInterval(10);
        service = new OpenShiftServiceImpl(client, config);
    }

    @Test
    public void testDeploy() {
        final String name = "test-deployment";

        final DeploymentData deploymentData = new DeploymentData.Builder()
            .addAnnotation("testName", "testDeploy")
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
        assertThat(issuedRequests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
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
        assertThat(issuedRequests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/apis/route.openshift.io/v1/namespaces/test/routes"));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/api/v1/namespaces/test/services"));
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
        assertThat(issuedRequests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/apis/route.openshift.io/v1/namespaces/test/routes"));
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
                .withAnnotations(Collections.emptyMap())
                .withLabels(Collections.emptyMap())
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
        assertThat(issuedRequests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).doesNotContain(Request.with("POST", "/apis/route.openshift.io/v1/namespaces/test/routes"));
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
                .withAnnotations(Collections.emptyMap())
                .withLabels(Collections.emptyMap())
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
            .withPath("/apis/route.openshift.io/v1/namespaces/test/routes")
            .andReturn(200, expectedRoute)
            .always();

        service.deploy(name, deploymentData);
        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
        assertThat(issuedRequests).contains(Request.with("POST", "/api/v1/namespaces/test/services", expectedService));
        assertThat(issuedRequests).contains(Request.with("POST", "/apis/route.openshift.io/v1/namespaces/test/routes"));
    }

    @Test
    public void shouldRemoveExposureViaRouteIfChangedTo3scale() {
        final String name = "switching-from-service-and-route-to-service-only";

        final DeploymentData existingDeploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.of(Exposure.SERVICE, Exposure.ROUTE))
            .build();

        final DeploymentConfig existingDeploymentConfig = baseDeploymentFor(name, existingDeploymentData)
            .build();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-via-service-and-route")
            .andReturn(200, existingDeploymentConfig)
            .once();

        final DeploymentData deploymentData = new DeploymentData.Builder()
            .withExposure(EnumSet.of(Exposure.SERVICE, Exposure._3SCALE))
            .build();


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
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-via-service-and-route")
            .andReturn(200, expectedDeploymentConfig)
            .always();
        
        service.deploy(name, deploymentData);
        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("DELETE", "/apis/route.openshift.io/v1/namespaces/test/routes/i-switching-from-service-and-route-to-service-only", new DeleteOptionsBuilder().withPropagationPolicy("Background").build()));
    }

    @Test
    public void shouldDeleteBasedOnIntegrationName() {
        server.expect()
            .delete()
            .withPath("/apis/route.openshift.io/v1/namespaces/test/routes/i-integration")
            .andReturn(202, null)
            .once();

        server.expect()
            .delete()
            .withPath("/api/v1/namespaces/test/services/i-integration")
            .andReturn(202, null)
            .once();

        server.expect()
            .delete()
            .withPath("/apis/image.openshift.io/v1/namespaces/test/imagestreams/i-integration")
            .andReturn(202, null)
            .once();

        final DeploymentConfig deploymentConfig = new DeploymentConfigBuilder()
            .withNewMetadata()
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, deploymentConfig)
            .once();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, deploymentConfig)
            .times(2);

        server.expect()
            .delete()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(202, null)
            .once();

        server.expect()
            .get()
            .withPath("/api/v1/namespaces/test/replicationcontrollers?labelSelector=openshift.io%2Fdeployment-config.name")
            .andReturn(200, new ReplicationControllerListBuilder()
                .addNewItem()
                .endItem()
                .build());

        server.expect()
            .delete()
            .withPath("/api/v1/namespaces/test/secrets/i-integration")
            .andReturn(202, null)
            .once();

        server.expect()
            .delete()
            .withPath("/apis/build.openshift.io/v1/namespaces/test/buildconfigs/i-integration")
            .andReturn(202, null)
            .once();

        boolean delete = service.delete("integration");

        assertThat(delete).isTrue();

        final List<Request> issuedRequests = gatherRequests();
        assertThat(issuedRequests).contains(Request.with("DELETE", "/apis/route.openshift.io/v1/namespaces/test/routes/i-integration"));
        assertThat(issuedRequests).contains(Request.with("DELETE", "/api/v1/namespaces/test/services/i-integration"));
        assertThat(issuedRequests).contains(Request.with("DELETE", "/apis/image.openshift.io/v1/namespaces/test/imagestreams/i-integration"));
        assertThat(issuedRequests).contains(Request.with("DELETE", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration"));
        assertThat(issuedRequests).contains(Request.with("DELETE", "/api/v1/namespaces/test/secrets/i-integration"));
        assertThat(issuedRequests).contains(Request.with("DELETE", "/apis/build.openshift.io/v1/namespaces/test/buildconfigs/i-integration"));
    }

    @Test
    public void buildShouldCreateImageStreamAndBuildConfig() throws InterruptedException {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .build();

        server.expect()
            .post()
            .withPath("/apis/image.openshift.io/v1/namespaces/test/imagestreams")
            .andReturn(201, new ImageStreamBuilder()
                .build())
            .once();

        server.expect()
            .post()
            .withPath("/apis/build.openshift.io/v1/namespaces/test/buildconfigs")
            .andReturn(201, new BuildConfigBuilder()
                .build())
            .once();

        server.expect()
            .post()
            .withPath("/apis/build.openshift.io/v1/namespaces/test/buildconfigs/i-integration/instantiatebinary?name=i-integration&namespace=test")
            .andReturn(200, new BuildBuilder()
                .withNewMetadata()
                .withNamespace("test")
                .withName("i-integration-1")
                .endMetadata()
                .build())
            .once();
        
        server.expect()
            .get()
            .withPath("/apis/build.openshift.io/v1/namespaces/test/builds/i-integration-1")
            .andReturn(200, new BuildBuilder()
                .withNewMetadata()
                    .withNamespace("test")
                    .withName("i-integration-1")
                .endMetadata()
                .withNewStatus()
                    .withPhase("Complete")
                    .withOutputDockerImageReference("registry/test/i-integration:1")
                .endStatus()
                .build())
            .once();

        server.expect()
            .get()
            .withPath("/apis/image.openshift.io/v1/namespaces/test/imagestreamtags/i-integration:1")
            .andReturn(200, new ImageStreamTagBuilder()
                .withNewImage()
                    .withDockerImageReference("registry/test/i-integration@sha256:hash")
                .endImage()
                .build())
            .once();
        
        final String reference = service.build("integration", deploymentData, new ByteArrayInputStream(new byte[0]));
        
        assertThat(reference).isEqualTo("registry/test/i-integration@sha256:hash");

        final List<Request> requests = gatherRequests();
        assertThat(requests).contains(Request.with("POST", "/apis/image.openshift.io/v1/namespaces/test/imagestreams", new ImageStreamBuilder()
            .withNewMetadata()
                .withName("i-integration")
                .addToLabels(OpenShiftServiceImpl.defaultLabels())
            .endMetadata()
            .build()));
        assertThat(requests).contains(Request.with("POST", "/apis/build.openshift.io/v1/namespaces/test/buildconfigs", new BuildConfigBuilder()
            .withNewMetadata()
                .withName("i-integration")
                .withAnnotations(Collections.emptyMap())
                .addToLabels(OpenShiftServiceImpl.defaultLabels())
            .endMetadata()
            .withNewSpec()
                .withNewOutput()
                    .withNewTo()
                        .withKind("ImageStreamTag")
                        .withName("i-integration:0")
                    .endTo()
                .endOutput()
                .withNewRunPolicy("SerialLatestOnly")
                .withNewSource()
                    .withType("Binary")
                .endSource()
                .withNewStrategy()
                    .withType("Source")
                    .withNewSourceStrategy()
                        .addToEnv(new EnvVar("MAVEN_OPTS", "-XX:+UseG1GC -XX:+UseStringDeduplication -Xmx300m", null))
                        .addToEnv(new EnvVar("MAVEN_ARGS_APPEND", "--strict-checksums", null))
                        .addToEnv(new EnvVar("BUILD_LOGLEVEL", "1", null))
                        .addToEnv(new EnvVar("SCRIPT_DEBUG", "false", null))
                        .withNewFrom()
                            .withKind("ImageStreamTag")
                            .withName("syndesis-s2i:latest")
                        .endFrom()
                        .withIncremental(false)
                    .endSourceStrategy()
                .endStrategy()
            .endSpec()
            .build()));
    }

    @Test
    public void stopShouldScaleDownAndRememberReplicas() {
        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withNewResourceVersion("123")
                .endMetadata()
                .withNewSpec()
                    .withReplicas(3)
                .endSpec()
                .build())
            .times(3);

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .build())
            .always();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withGeneration(1L)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(0)
                .endSpec()
                .withNewStatus()
                    .withReplicas(0)
                    .withObservedGeneration(1L)
                .endStatus()
                .build())
            .always();

        final boolean stopped = service.stop("integration");
        assertThat(stopped).isTrue();

        final List<Request> requests = gatherRequests();
        final Map<String, Object> scaleDownPatch = new HashMap<>();
        scaleDownPatch.put("op", "replace");
        scaleDownPatch.put("path", "/spec/replicas");
        scaleDownPatch.put("value", 0);
        assertThat(requests).contains(Request.with("PATCH", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration", Collections.singletonList(scaleDownPatch)));

        final Map<String, Object> setAnnotationPatch = new HashMap<>();
        setAnnotationPatch.put("op", "add");
        setAnnotationPatch.put("path", "/metadata/annotations");
        setAnnotationPatch.put("value", Collections.singletonMap("syndesis.io/deploy-replicas", "3"));
        assertThat(requests).contains(Request.with("PATCH", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration", Collections.singletonList(setAnnotationPatch)));
    }

    @Test
    public void redeployingPreservesCustomEnvironment() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .addEnvironmentVariable("ADDED", "value")
            .build();

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor("integration", deploymentData)
            .editSpec()
                .editTemplate()
                    .editSpec()
                        .editContainer(0)
                            .addToEnv(new EnvVar("ADDED", "value", null))
                            .addToEnv(new EnvVar("EXISTING", "something", null))
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withAnnotations(Collections.emptyMap())
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withNewTemplate()
                        .withNewSpec()
                            .withContainers(new ContainerBuilder()
                                .withName("i-integration")
                                .withEnv(new EnvVar("EXISTING", "something", null))
                                .build())
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build())
            .times(1);

        server.expect()
            .post()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .build())
            .always();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withGeneration(1L)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(0)
                .endSpec()
                .withNewStatus()
                    .withReplicas(0)
                    .withObservedGeneration(1L)
                .endStatus()
                .build())
            .always();

        service.deploy("integration", deploymentData);

        final List<Request> requests = gatherRequests();
        assertThat(requests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
    }

    @Test
    public void deployShouldApplyEnvironmentChanges() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .addEnvironmentVariable("CHANGED", "new value")
            .removeEnvironmentVariable("REMOVED")
            .removeEnvironmentVariable("EXISTING_REMOVED")
            .build();

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor("integration", deploymentData)
            .editSpec()
                .editTemplate()
                    .editSpec()
                        .editContainer(0)
                            .addToEnv(new EnvVar("CHANGED", "new value", null))
                            .addToEnv(new EnvVar("EXISTING", "something1", null))
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withAnnotations(Collections.emptyMap())
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withNewTemplate()
                        .withNewSpec()
                            .withContainers(new ContainerBuilder()
                                .withName("i-integration")
                                .addToEnv(new EnvVar("CHANGED", "old value", null))
                                .addToEnv(new EnvVar("REMOVED", "value", null))
                                .addToEnv(new EnvVar("EXISTING", "something1", null))
                                .addToEnv(new EnvVar("EXISTING_REMOVED", "something2", null))
                                .build())
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build())
            .times(1);

        server.expect()
            .post()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .build())
            .always();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withGeneration(1L)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(0)
                .endSpec()
                .withNewStatus()
                    .withReplicas(0)
                    .withObservedGeneration(1L)
                .endStatus()
                .build())
            .always();

        service.deploy("integration", deploymentData);

        final List<Request> requests = gatherRequests();
        assertThat(requests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
    }

    @Test
    public void redeployingPreservesCustomReplicas() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .build();

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor("integration", deploymentData)
            .editMetadata()
                .addToAnnotations(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION, "3")
            .endMetadata()
            .build();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .addToAnnotations(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION, "3")
                .endMetadata()
                .withNewSpec()
                    .withReplicas(3)
                    .withNewTemplate()
                        .withNewSpec()
                            .withContainers(new ContainerBuilder()
                                .withName("i-integration")
                                .build())
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build())
            .times(1);

        server.expect()
            .post()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .build())
            .always();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withGeneration(1L)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(0)
                .endSpec()
                .withNewStatus()
                    .withReplicas(0)
                    .withObservedGeneration(1L)
                .endStatus()
                .build())
            .always();

        service.deploy("integration", deploymentData);

        final List<Request> requests = gatherRequests();
        assertThat(requests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
    }

    @Test
    public void redeployingPreservesCustomStrategy() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .build();

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor("integration", deploymentData)
            .editSpec()
                .withNewStrategy()
                    .withType("Rolling")
                    .withNewRollingParams()
                        .withUpdatePeriodSeconds(1L)
                        .withIntervalSeconds(1L)
                        .withTimeoutSeconds(120L)
                        .withNewMaxSurge("20%")
                        .withNewMaxUnavailable("10%")
                    .endRollingParams()
                    .withNewResources()
                       .addToLimits("memory", new Quantity("2Gi"))
                       .addToRequests("memory", new Quantity("2Gi"))
                    .endResources()
                .endStrategy()
            .endSpec()
            .build();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, expectedDeploymentConfig)
            .times(1);

        server.expect()
            .post()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .build())
            .always();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withGeneration(1L)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(0)
                .endSpec()
                .withNewStatus()
                    .withReplicas(0)
                    .withObservedGeneration(1L)
                .endStatus()
                .build())
            .always();

        service.deploy("integration", deploymentData);

        final List<Request> requests = gatherRequests();
        assertThat(requests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
    }

    @Test
    public void redeployingPreservesCustomVolumes() {
        final DeploymentData deploymentData = new DeploymentData.Builder()
            .build();

        final DeploymentConfig expectedDeploymentConfig = baseDeploymentFor("integration", deploymentData)
            .editSpec()
                .editTemplate()
                    .editSpec()
                        .editContainer(0)
                            .addNewVolumeMount()
                                .withMountPath("/custom")
                                .withName("custom-config")
                            .endVolumeMount()
                        .endContainer()
                        .addNewVolume()
                            .withNewConfigMap()
                                .withName("custom-config")
                            .endConfigMap()
                        .endVolume()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, expectedDeploymentConfig)
            .times(1);

        server.expect()
            .post()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs")
            .andReturn(200, expectedDeploymentConfig)
            .always();

        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .build())
            .always();

        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/i-integration")
            .andReturn(200, new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withGeneration(1L)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(0)
                .endSpec()
                .withNewStatus()
                    .withReplicas(0)
                    .withObservedGeneration(1L)
                .endStatus()
                .build())
            .always();

        service.deploy("integration", deploymentData);

        final List<Request> requests = gatherRequests();
        assertThat(requests).contains(Request.with("POST", "/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs", expectedDeploymentConfig));
    }

    DeploymentConfigBuilder baseDeploymentFor(final String name, final DeploymentData deploymentData) {
        return new DeploymentConfigBuilder()
            .withNewMetadata()
                .withName(openshiftName(name))
                .addToAnnotations(deploymentData.getAnnotations())
                .addToAnnotations(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION, "1")
                .addToLabels(deploymentData.getLabels())
                .addToLabels(OpenShiftServiceImpl.defaultLabels())
            .endMetadata()
            .withNewSpec()
                .withReplicas(0)
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
                            .addToEnv(new EnvVarBuilder().withName("AB_PROMETHEUS_ENABLE").withValue("true").build())
                            .addToEnv(new EnvVarBuilder().withName("AB_PROMETHEUS_JMX_EXPORTER_PORT").withValue("9779").build())
                            .addToEnv(new EnvVarBuilder().withName("AB_PROMETHEUS_JMX_EXPORTER_CONFIG").withValue("/deployments/data/prometheus-config.yml").build())
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
                                    .withPath("/actuator/health")
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
            .withNewMetadata()
                .withGeneration(1L)
            .endMetadata()
            .withNewSpec()
                .withReplicas(1)
            .endSpec()
            .withNewStatus()
                .withLatestVersion(1L)
                .withReplicas(1)
                .withReadyReplicas(1)
                .withObservedGeneration(1L)
            .endStatus()
            .build();
        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/" + openshiftName(name))
            .andReturn(404, new StatusBuilder().withCode(404).build())
            .times(1);
        server.expect()
            .get()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/" + openshiftName(name))
            .andReturn(200, deployed)
            .always();
        server.expect()
            .patch()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs/" + openshiftName(name))
            .andReturn(200, deployed)
            .always();
        server.expect()
            .post()
            .withPath("/apis/apps.openshift.io/v1/namespaces/test/deploymentconfigs")
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
