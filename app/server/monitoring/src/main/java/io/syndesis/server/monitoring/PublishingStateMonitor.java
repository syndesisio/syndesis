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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.common.model.monitoring.LinkType;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;

import static io.syndesis.common.model.integration.IntegrationDeploymentState.Pending;
import static io.syndesis.common.model.integration.IntegrationDeploymentState.Published;
import static io.syndesis.common.model.monitoring.LinkType.EVENTS;
import static io.syndesis.common.model.monitoring.LinkType.LOGS;

/**
 * Monitor Integrations based on their deployment state.
 * Used for PUBLISHING status updates. Can be utilized in the future for other purposes.
 * @author dhirajsb
 */
@Service
@ConditionalOnProperty(value = "features.monitoring.enabled", havingValue = "true")
public class PublishingStateMonitor implements StateHandler {

    static final String DEPLOYER_POD_NAME_ANNOTATION = "openshift.io/deployer-pod.name";
    static final String BUILD_POD_NAME_ANNOTATION = "openshift.io/build.pod-name";

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingStateMonitor.class);

    private final NamespacedOpenShiftClient client;
    private final DataManager dataManager;

    @Autowired
    public PublishingStateMonitor(DeploymentStateMonitor monitor, NamespacedOpenShiftClient client, DataManager dataManager) {
        this.client = client;
        this.dataManager = dataManager;

        monitor.register(Pending, this);
    }

    @Override
    public String getDescription() {
        return "Publishing state details";
    }

    @Override
    @SuppressWarnings("PMD.NPathComplexity")
    public void accept(IntegrationDeployment integrationDeployment) {

        final String integrationId = integrationDeployment.getIntegrationId().get();
        final String version = String.valueOf(integrationDeployment.getVersion());
        final String compositeId = IntegrationDeployment.compositeId(integrationId, integrationDeployment.getVersion());
        final IntegrationDeploymentState targetState = integrationDeployment.getTargetState();

        // is it being published?
        if (targetState.equals(Published)) {

            IntegrationDeploymentDetailedState detailedState = null;
            String podName = null;
            LinkType linkType = null;

            // work backwards, in reverse order: deployed pod, deployer pod, build pod, default to assembling
            // 1. look for deployed pod
            final PodList podList = getDeploymentPodList(integrationId, version);
            if (!podList.getItems().isEmpty()) {

                // TODO: handle pod scaling in the future
                final Pod pod = podList.getItems().get(0);
                podName = pod.getMetadata().getName();
                // check if deployment is ready
                if (Readiness.isPodReady(pod)) {
                    // no details needed once deployed successfully!!!
                    // NOTE that this won't happen always, the state polling window may miss this
                    // TODO need a cleanup handler to remove successfully published state details??
                    deleteStateDetails(compositeId);
                } else {
                    linkType = getPodUrls(pod);
                    // pending deployment pod
                    detailedState = EVENTS == linkType ? IntegrationDeploymentDetailedState.DEPLOYING : IntegrationDeploymentDetailedState.STARTING;
                }

            } else {
                // 2. look for deployer pod
                final Optional<ReplicationController> replicationController = getReplicationController(integrationId,
                        version);
                if (replicationController.isPresent()) {
                    podName = replicationController.get().getMetadata().getAnnotations().get(DEPLOYER_POD_NAME_ANNOTATION);
                    if (podName != null) {
                        final Pod deployerPod = getPod(podName);
                        if (deployerPod != null) {
                            // with a deployer pod, detailed state is always deploying
                            linkType = getPodUrls(deployerPod);
                            detailedState = IntegrationDeploymentDetailedState.DEPLOYING;
                        } else {
                            // clear podName, since it doesn't exist yet
                            podName = null;
                        }
                    }
                }

                // 3. look for build pod, if deployer pod not found
                if (podName == null) {
                    final Optional<Build> build = getBuild(integrationId, version);
                    if (build.isPresent()) {

                        podName = build.get().getMetadata().getAnnotations().get(BUILD_POD_NAME_ANNOTATION);
                        if (podName != null) {
                            final Pod pod = getPod(podName);
                            if (pod != null) {
                                linkType = getPodUrls(pod);
                                // pending deployment pod
                                detailedState = EVENTS == linkType ? IntegrationDeploymentDetailedState.ASSEMBLING : IntegrationDeploymentDetailedState.BUILDING;
                            } else {
                                // clear podName, since it doesn't exist yet
                                podName = null;
                            }
                        }
                    }
                }

                if (detailedState == null) {
                    // 4. default initial state, with no event or log urls!!!
                    detailedState = IntegrationDeploymentDetailedState.ASSEMBLING;
                }
            }

            if (detailedState != null) {
                IntegrationDeploymentStateDetails stateDetails = getStateDetails(integrationId, version, compositeId,
                        detailedState, podName, linkType);
                if (dataManager.fetch(IntegrationDeploymentStateDetails.class, compositeId) != null) {
                    dataManager.update(stateDetails);
                } else {
                    dataManager.create(stateDetails);
                }
            }
        }

    }

    private IntegrationDeploymentStateDetails getStateDetails(String integrationId, String version, String
            compositeId, IntegrationDeploymentDetailedState detailedState, String podName, LinkType linkType) {
        return new IntegrationDeploymentStateDetails.Builder()
            .id(compositeId)
            .integrationId(integrationId)
            .deploymentVersion(Integer.parseInt(version))
            .detailedState(detailedState)
            .namespace(client.getNamespace())
            .podName(Optional.ofNullable(podName))
            .linkType(Optional.ofNullable(linkType))
            .build();
    }

    protected Optional<ReplicationController> getReplicationController(String integrationId, String version) {
        return client.replicationControllers()
                .withLabel(OpenShiftService.INTEGRATION_ID_LABEL, integrationId)
                .withLabel(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
                .list().getItems().stream().findFirst();
    }

    protected Pod getPod(String podName) {
        return client.pods().withName(podName).get();
    }

    protected Optional<Build> getBuild(String integrationId, String version) {
        return client.builds()
                            .withLabel(OpenShiftService.INTEGRATION_ID_LABEL, integrationId)
                            .withLabel(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
                            .list()
                .getItems().stream().findFirst();
    }

    protected PodList getDeploymentPodList(String integrationId, String version) {
        return client.pods()
                        .withLabel(OpenShiftService.COMPONENT_LABEL, "integration")
                        .withLabel(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
                        .withLabel(OpenShiftService.INTEGRATION_ID_LABEL, integrationId).list();
    }

    protected LinkType getPodUrls(Pod pod) {
        LinkType linkType;

        final PodStatus status = pod.getStatus();
        switch (status.getPhase()) {
        case "Pending":
        case "Unknown":
            // get pod events url
            linkType = EVENTS;
            break;
        default:
            // Running, Succeeded or Failed
            // get pod logs url
            linkType = LOGS;
            break;
        }
        return linkType;
    }

    private void deleteStateDetails(String id) {
        if (dataManager.delete(IntegrationDeploymentStateDetails.class, id)) {
            LOGGER.debug("Removed detailed state for {}", id);
        }
    }
}
