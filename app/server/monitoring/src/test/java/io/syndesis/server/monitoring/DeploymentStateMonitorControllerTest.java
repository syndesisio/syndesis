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
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildBuilder;
import io.fabric8.openshift.api.model.BuildList;
import io.fabric8.openshift.api.model.BuildListBuilder;
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

import static io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState.*;
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
    private static final String DEPLOYMENT_POD_NAME = "deployment-pod";

    private static final PodStatus PENDING_STATUS = new PodStatusBuilder().withPhase("Pending").build();
    private static final PodStatus RUNNING_STATUS = new PodStatusBuilder().withPhase("Running").build();
    private static final PodStatus SUCCEEDED_STATUS = new PodStatusBuilder().withPhase("Succeeded").build();

    @Parameterized.Parameter(value = 0)
    public Pod deploymentPod;

    @Parameterized.Parameter(value = 1)
    public Build build;

    @Parameterized.Parameter(value = 2)
    public Pod buildPod;

    @Parameterized.Parameter(value = 3)
    public IntegrationDeploymentStateDetails expectedDetails;

    private DataManager dataManager;
    private static NamespacedOpenShiftClient client;

    public static final String TEST_NAMESPACE = "test-namespace";

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
    public void before() throws Exception {
        CacheManager cacheManager = new LRUCacheManager(100);
        EncryptionComponent encryptionComponent = new EncryptionComponent(null);
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        // create test Data Manager and integration deployment
        dataManager = new DataManager(cacheManager, Collections.emptyList(), null, encryptionComponent, resourceLoader);
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
                protected Pod getBuildPod(String podName) {
                    return buildPod;
                }

                @Override
                protected BuildList getBuildList(String integrationId, String version) {
                    final BuildListBuilder builder = new BuildListBuilder();
                    return (build == null) ? builder.build() : builder.addToItems(build).build();
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

        final Build build = new BuildBuilder()
                .withNewMetadata()
                .addToAnnotations(PublishingStateMonitor.BUILD_POD_NAME_ANNOTATION, BUILD_POD_NAME)
                .endMetadata()
                .build();

        // deploymentPod, build, buildPod, and expectedDetails
        Pod pod;
        return Arrays.asList(
                // no deployment or build pod yet
                new Object[]{null, null, null,
                        getDetails(ASSEMBLING, TEST_NAMESPACE, null, null, null, null)},
                // build without build pod
                new Object[] {null, build, null,
                        getDetails(ASSEMBLING, TEST_NAMESPACE, null, null, null, null)},
                // build pod with Pending status
                new Object[] {null, build, pod = getPod(PENDING_STATUS, BUILD_POD_NAME),
                        getDetails(ASSEMBLING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.EVENTS, getEventsUrl(pod), null)},
                // build pod with Running status
                new Object[] {null, build, pod = getPod(RUNNING_STATUS, BUILD_POD_NAME),
                        getDetails(BUILDING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.LOGS, null, getLogsUrl(pod))},
                // build pod with Succeeded status, no deployment pod yet
                new Object[] {null, build, pod = getPod(SUCCEEDED_STATUS, BUILD_POD_NAME),
                        getDetails(BUILDING, TEST_NAMESPACE, BUILD_POD_NAME, LinkType.LOGS, null, getLogsUrl(pod))},
                // deployment pod with Pending status
                new Object[] { pod = getPod(PENDING_STATUS, DEPLOYMENT_POD_NAME), null, null,
                        getDetails(DEPLOYING, TEST_NAMESPACE, DEPLOYMENT_POD_NAME, LinkType.EVENTS, getEventsUrl(pod), null)},
                // deployment pod with Running status
                new Object[] { pod = getPod(RUNNING_STATUS, DEPLOYMENT_POD_NAME), null, null,
                        getDetails(STARTING, TEST_NAMESPACE, DEPLOYMENT_POD_NAME, LinkType.LOGS, null, getLogsUrl(pod))}
                );
    }

    private static IntegrationDeploymentStateDetails getDetails(IntegrationDeploymentDetailedState state, String namespace, String podName, LinkType linkType, String eventsUrl, String logsUrl) {
        return new IntegrationDeploymentStateDetails.Builder()
                .id(DEPLOYMENT_ID)
                .integrationId(INTEGRATION_ID)
                .deploymentVersion(INTEGRATION_VERSION)
                .detailedState(state)
                .namespace(Optional.ofNullable(namespace))
                .podName(Optional.ofNullable(podName))
                .linkType(Optional.ofNullable(linkType))
                .eventsUrl(Optional.ofNullable(eventsUrl))
                .logsUrl(Optional.ofNullable(logsUrl))
                .build();
    }

    private static String getEventsUrl(Pod pod) {
        return PublishingStateMonitor.getEventsUrl(client, pod);
    }

    private static String getLogsUrl(Pod pod) {
        return PublishingStateMonitor.getLogsUrl(client, pod);
    }

    private static Pod getPod(PodStatus status, String name) {
        return new PodBuilder().withNewStatusLike(status).endStatus().withNewMetadata().withName(name).endMetadata().build();
    }
}
