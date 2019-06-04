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
package io.syndesis.server.monitoring;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import io.syndesis.common.model.WithId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.common.model.monitoring.LinkType;
import io.syndesis.common.util.cache.CacheManager;
import io.syndesis.common.util.cache.LRUCacheManager;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

import static io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState.ASSEMBLING;
import static io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState.BUILDING;
import static io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState.DEPLOYING;
import static io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState.STARTING;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Used to unit test the {@link io.syndesis.server.monitoring.DeploymentStateMonitor} implementation.
 */
@RunWith(Parameterized.class)
public class DeploymentStateMonitorControllerTest {

    private static final String INTEGRATION_ID = "test-integration";
    private static final int INTEGRATION_VERSION = 1;
    private static final String DEPLOYMENT_ID = IntegrationDeployment.compositeId(INTEGRATION_ID, INTEGRATION_VERSION);

    private static final String BUILD_POD_NAME = "build-pod";
    private static final String DEPLOYER_POD_NAME = "test-integration-1-deploy";
    private static final String DEPLOYMENT_POD_NAME = "deployment-pod";

    private static final PodStatus PENDING_STATUS = new PodStatusBuilder().withPhase("Pending").build();
    private static final PodStatus RUNNING_STATUS = new PodStatusBuilder().withPhase("Running").build();
    private static final PodStatus SUCCEEDED_STATUS = new PodStatusBuilder().withPhase("Succeeded").build();

    @Parameterized.Parameter(value = 0)
    public Pod deploymentPod;

    @Parameterized.Parameter(value = 1)
    public ReplicationController replicationController;

    @Parameterized.Parameter(value = 2)
    public Pod deployerPod;

    @Parameterized.Parameter(value = 3)
    public Build build;

    @Parameterized.Parameter(value = 4)
    public Pod buildPod;

    @Parameterized.Parameter(value = 5)
    public IntegrationDeploymentStateDetails expectedDetails;

    private DataManager dataManager;
    private static NamespacedOpenShiftClient client;

    private static final String TEST_NAMESPACE = "test-namespace";

    static {
        client = Mockito.mock(NamespacedOpenShiftClient.class);
        try {
            Mockito.when(client.getOpenshiftUrl()).thenReturn(new URL("https://test-cluster"));
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }
        Mockito.when(client.getNamespace()).thenReturn(TEST_NAMESPACE);
    }

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        CacheManager cacheManager = new LRUCacheManager(100);
        EncryptionComponent encryptionComponent = new EncryptionComponent(null);
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        // create test Data Manager and integration deployment
        dataManager = new DataManager(cacheManager, Collections.emptyList(), null, encryptionComponent, resourceLoader, null) {
            @Override
            public <K extends WithId<K>> Stream<K> fetchAllByPropertyValue(Class<K> type, String property, String value) {
                if (type.equals(IntegrationDeployment.class) && property.equals("currentState")) {
                    return (Stream<K>) fetchAll(IntegrationDeployment.class)
                        .getItems()
                        .stream()
                        .filter(d -> d.getCurrentState().name().equals(value));
                }
                return super.fetchAllByPropertyValue(type, property, value);
            }
        };
        dataManager.create(new IntegrationDeployment.Builder()
                .id(DEPLOYMENT_ID)
                .integrationId(java.util.Optional.of(INTEGRATION_ID))
                .version(1)
                .currentState(IntegrationDeploymentState.Pending)
                .targetState(IntegrationDeploymentState.Published)
                .build());

    }

    @Test
    public void testMonitoringController() throws IOException {

        final MonitoringConfiguration configuration = new MonitoringConfiguration();
        configuration.setInitialDelay(5);
        configuration.setPeriod(5);

        try (DeploymentStateMonitorController controller = new DeploymentStateMonitorController(configuration, dataManager)) {

            new PublishingStateMonitor(controller, client, dataManager) {
                @Override
                protected Pod getPod(String podName) {
                    final Pod pod;
                    switch (podName) {
                    case BUILD_POD_NAME:
                        pod = buildPod;
                        break;
                    case DEPLOYER_POD_NAME:
                        pod = deployerPod;
                        break;
                    default:
                        pod = null;
                    }
                    return pod;
                }

                @Override
                protected Optional<Build> getBuild(String integrationId, String version) {
                    return Optional.ofNullable(build);
                }

                @Override
                protected Optional<ReplicationController> getReplicationController(String integrationId, String version) {
                    return Optional.ofNullable(replicationController);
                }

                @Override
                protected PodList getDeploymentPodList(String integrationId, String version) {
                    final PodListBuilder builder = new PodListBuilder();
                    return deploymentPod == null ? builder.build() : builder.addToItems(deploymentPod).build();
                }
            };

            controller.open();

            // Eventually all the log data should make it into the dataManager
            given().await()
                .atMost(20, SECONDS)
                .pollInterval(5, SECONDS)
                .untilAsserted(() -> assertEquals(expectedDetails, dataManager.fetch(IntegrationDeploymentStateDetails.class, DEPLOYMENT_ID)));
        }

    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {

        final ReplicationController replicationController = new ReplicationControllerBuilder()
                .withNewMetadata()
                .addToAnnotations(PublishingStateMonitor.DEPLOYER_POD_NAME_ANNOTATION, DEPLOYER_POD_NAME)
                .endMetadata()
                .build();
        final Build build = new BuildBuilder()
                .withNewMetadata()
                .addToAnnotations(PublishingStateMonitor.BUILD_POD_NAME_ANNOTATION, BUILD_POD_NAME)
                .endMetadata()
                .build();

        // successful build and deployer pod
        final Pod buildPod = getPod(SUCCEEDED_STATUS, BUILD_POD_NAME);
        final Pod deployerPod = getPod(SUCCEEDED_STATUS, DEPLOYER_POD_NAME);

        // deploymentPod, rc, deployerPod, build, buildPod, and expectedDetails
        return Arrays.asList(
                // no deployment, rc, deployer, or build pod yet
                new Object[]{null, null, null, null, null,
                        getDetails(ASSEMBLING, TEST_NAMESPACE, null, null)},
                // build pod with Pending status
                new Object[] {null, null, null, build, getPod(PENDING_STATUS, BUILD_POD_NAME),
                        getDetails(ASSEMBLING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.EVENTS)},
                // build pod with Running status
                new Object[] {null, null, null, build, getPod(RUNNING_STATUS, BUILD_POD_NAME),
                        getDetails(BUILDING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.LOGS)},
                // build pod with Succeeded status, no deployment pod yet
                new Object[] {null, null, null, build, buildPod,
                        getDetails(BUILDING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.LOGS)},
                // rc without deployer pod
                new Object[] {null, replicationController, null, build, buildPod,
                        getDetails(BUILDING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.LOGS)},
                // rc with pending deployer pod
                new Object[] {null, replicationController, getPod(PENDING_STATUS, DEPLOYER_POD_NAME), build, buildPod,
                        getDetails(DEPLOYING, TEST_NAMESPACE, DEPLOYER_POD_NAME, LinkType.EVENTS)},
                // rc with running deployer pod
                new Object[] {null, replicationController, getPod(RUNNING_STATUS, DEPLOYER_POD_NAME), build, buildPod,
                        getDetails(DEPLOYING, TEST_NAMESPACE, DEPLOYER_POD_NAME, LinkType.LOGS)},
                // rc with succeeded deployer pod
                new Object[] {null, replicationController, deployerPod, build, buildPod,
                        getDetails(DEPLOYING, TEST_NAMESPACE, DEPLOYER_POD_NAME, LinkType.LOGS)},
                // deployment pod with Pending status
                new Object[] {getPod(PENDING_STATUS, DEPLOYMENT_POD_NAME), replicationController, deployerPod, build, buildPod,
                        getDetails(DEPLOYING, TEST_NAMESPACE, DEPLOYMENT_POD_NAME, LinkType.EVENTS)},
                // deployment pod with Running status
                new Object[] {getPod(RUNNING_STATUS,DEPLOYMENT_POD_NAME), replicationController, deployerPod, build, buildPod,
                        getDetails(STARTING, TEST_NAMESPACE, DEPLOYMENT_POD_NAME, LinkType.LOGS)}
                );
    }

    private static IntegrationDeploymentStateDetails getDetails(IntegrationDeploymentDetailedState state, String namespace, String podName, LinkType linkType) {
        return new IntegrationDeploymentStateDetails.Builder()
                .id(DEPLOYMENT_ID)
                .integrationId(INTEGRATION_ID)
                .deploymentVersion(INTEGRATION_VERSION)
                .detailedState(state)
                .namespace(Optional.ofNullable(namespace))
                .podName(Optional.ofNullable(podName))
                .linkType(Optional.ofNullable(linkType))
                .build();
    }

    private static Pod getPod(PodStatus status, String name) {
        return new PodBuilder().withNewStatusLike(status).endStatus().withNewMetadata().withName(name).endMetadata().build();
    }
}
