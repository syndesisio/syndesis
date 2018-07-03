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
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentDetailedState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;

import static io.syndesis.common.model.integration.IntegrationDeploymentState.Pending;
import static io.syndesis.common.model.integration.IntegrationDeploymentState.Published;

/**
 * Monitor Integrations based on their deployment state.
 * Used for PUBLISHING status updates. Can be utilized in the future for other purposes.
 * @author dhirajsb
 */
@Service
@ConditionalOnProperty(value = "features.monitoring.enabled", havingValue = "true")
public class PublishingStateMonitor implements StateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingStateMonitor.class);
    private static final String POD_LOG_URL = "%sapi/v1/namespaces/%s/pods/%s/logs";
    private static final String POD_EVENT_URL = "%sapi/v1/namespaces/%s/pods/%s/events";

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
        return "Publishing state details handler";
    }

    @Override
    public void accept(IntegrationDeployment integrationDeployment) {

        final String integrationId = integrationDeployment.getIntegrationId().get();
        final IntegrationDeploymentState targetState = integrationDeployment.getTargetState();

        // is it being published?
        if (targetState.equals(Published)) {

            IntegrationDeploymentDetailedState detailedState = null;
            String[] podUrls = new String[] { null, null };

            // work backwards, in order: deployed pod, build pod, default to assembling
            // 1. look for deployed pod
            final PodList podList = client.pods().withLabel(OpenShiftService.COMPONENT_LABEL, "integration")
                    .withLabel(OpenShiftService.INTEGRATION_ID_LABEL, integrationId).list();
            if (!podList.getItems().isEmpty()) {
                // check if deployment is ready
                // TODO: handle pod scaling in the future
                final Pod pod = podList.getItems().get(0);
                if (Readiness.isPodReady(pod)) {
                    // no details needed once deployed successfully!!!
                    // NOTE that this won't happen always, the state polling window may miss this
                    // TODO need a cleanup handler to remove successfully published state details??
                    deleteStateDetails(integrationId);
                } else {
                    podUrls = getPodUrls(pod);
                    // pending deployment pod
                    detailedState = null != podUrls[0] ? IntegrationDeploymentDetailedState.DEPLOYING :
                            IntegrationDeploymentDetailedState.STARTING;
                }

            } else {
                // 2. look for build pod
                // TODO: fix build pod labels once UI is working again!!!
                final PodList buildPodList = client.pods().withLabel(OpenShiftService.COMPONENT_LABEL, "integration")
                        .withLabel(OpenShiftService.INTEGRATION_ID_LABEL, integrationId).list();
                if (!buildPodList.getItems().isEmpty()) {
                    final Pod pod = buildPodList.getItems().get(0);
                    podUrls = getPodUrls(pod);
                    // pending deployment pod
                    detailedState = (null != podUrls[0]) ? IntegrationDeploymentDetailedState.ASSEMBLING :
                            IntegrationDeploymentDetailedState.BUILDING;
                } else {
                    // 3. default state, with no event or log urls!!!
                    detailedState = IntegrationDeploymentDetailedState.ASSEMBLING;
                }
            }

            if (detailedState != null) {
                IntegrationDeploymentStateDetails stateDetails = new IntegrationDeploymentStateDetails.Builder()
                    .id(integrationId)
                    .detailedState(detailedState)
                    .eventsUrl(Optional.ofNullable(podUrls[0]))
                    .podLogUrl(Optional.ofNullable(podUrls[1]))
                    .build();
                if (dataManager.fetch(IntegrationDeploymentStateDetails.class, integrationId) != null) {
                    dataManager.update(stateDetails);
                } else {
                    dataManager.create(stateDetails);
                }
            }
        }

    }

    private String[] getPodUrls(Pod pod) {
        String eventUrl = null;
        String logUrl = null;
        if (!Readiness.isPodReady(pod)) {
            final PodStatus status = pod.getStatus();
            switch (status.getPhase()) {
            case "Pending":
                // get pod event url
                eventUrl = String.format(POD_EVENT_URL, client.getMasterUrl(), client.getNamespace(), pod.getMetadata().getName());
                break;
            case "Running":
                // get pod log url
                logUrl = String.format(POD_LOG_URL, client.getMasterUrl(), client.getNamespace(), pod.getMetadata().getName());
                break;
            default:
                // ignore
            }
        }
        return new String[]{ eventUrl, logUrl };
    }

    private void deleteStateDetails(String id) {
        if (dataManager.delete(IntegrationDeploymentStateDetails.class, id)) {
            LOGGER.debug("Removed detailed state for {}", id);
        }
    }
}
