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
package io.syndesis.server.endpoint.v1.handler.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentOverview;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.IntegrationEndpoint;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.operators.IdPrefixFilter;
import io.syndesis.server.dao.manager.operators.ReverseFilter;
import io.syndesis.server.endpoint.v1.util.DataManagerSupport;
import io.syndesis.server.openshift.ExposureHelper;
import org.springframework.stereotype.Component;

@Component
class IntegrationOverviewHelper {

    private final DataManager dataManager;
    private final ExposureHelper exposureHelper;

    IntegrationOverviewHelper(DataManager dataManager, ExposureHelper exposureHelper) {
        this.dataManager = dataManager;
        this.exposureHelper = exposureHelper;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    IntegrationOverview toCurrentIntegrationOverview(final Integration integration) {
        final String id = integration.getId().get();
        final IntegrationOverview.Builder builder = new IntegrationOverview.Builder().createFrom(integration);

        // add board
        DataManagerSupport.fetchBoard(dataManager, IntegrationBulletinBoard.class, id).ifPresent(builder::board);

        // Defaults
        builder.isDraft(true);
        builder.currentState(IntegrationDeploymentState.Unpublished);
        builder.targetState(IntegrationDeploymentState.Unpublished);

        if (!integration.getFlows().isEmpty() && !integration.getSteps().isEmpty()) {
            throw new IllegalStateException(String.format("Integration has inconsistent state: flows=%d, steps=%d",
                integration.getFlows().size(), integration.getSteps().size()));
        }

        if (integration.getFlows().isEmpty() && !integration.getSteps().isEmpty()) {
            // this means that the integration is an old integration that
            // does not have the concept of flows so let's create a flow
            // which wraps the configured steps.
            //
            // We do generate a stable flowId based on integration id the
            // endpoint
            // is invoked multiple times, the flow id does not change.
            //
            builder.addFlow(new Flow.Builder().id(String.format("%s:flows:fromSteps", integration.getId().get()))
                .name(integration.getName()).description(integration.getDescription()).steps(integration.getSteps())
                .build());

            // and remove the steps
            builder.steps(Collections.emptyList());

            // when the integration is saved, it is automatically migrated to
            // the new style.
        } else {
            // get the latest flows, connections and steps
            builder.flows(integration.getFlows().stream().map(f -> UpdatesHelper.toCurrentFlow(f, dataManager))
                .collect(Collectors.toList()));
        }

        final ListResult<IntegrationDeployment> deployments = dataManager.fetchAll(IntegrationDeployment.class,
                new IdPrefixFilter<>(id + ":"), ReverseFilter.getInstance());

        final List<IntegrationDeployment> activeDeployments = new ArrayList<>();
        for (final IntegrationDeployment deployment : deployments) {
            builder.addDeployment(IntegrationDeploymentOverview.of(deployment));

            if (deployment.getCurrentState() == IntegrationDeploymentState.Pending ||
                deployment.getCurrentState() == IntegrationDeploymentState.Published) {
                activeDeployments.add(deployment);
            }
        }

        Optional<IntegrationDeployment> maybePublished = activeDeployments.stream()
                  .filter(deployment -> deployment.getCurrentState() == IntegrationDeploymentState.Published)
                  .findFirst();

        if (integration.isExposable()) {
            builder.exposureMeans(exposureHelper.getExposureMeans());
        }

        IntegrationDeployment exposedDeployment = null;
        if (maybePublished.isPresent()) {
            IntegrationDeployment published = maybePublished.get();
            builder.isDraft(computeDraft(integration, published.getSpec()));
            builder.version(published.getVersion());
            exposedDeployment = published;
        } else if (activeDeployments.size() == 1) {
            exposedDeployment = activeDeployments.get(0);
        }

        // Set the URL of the integration deployment if present
        if (exposedDeployment != null) {
            builder.deploymentVersion(exposedDeployment.getVersion());
            builder.targetState(exposedDeployment.getTargetState());
            builder.currentState(exposedDeployment.getCurrentState());

            if (exposedDeployment.getId().isPresent()) {
                builder.managementUrl(Optional.ofNullable(exposureHelper.getManagementUrl(integration.getExposure())));

                final IntegrationEndpoint endpoint = dataManager.fetch(IntegrationEndpoint.class,
                    exposedDeployment.getId().get());
                if (endpoint != null) {
                    builder.url(endpoint.getUrl());
                }
            }
        } else if (!deployments.getItems().isEmpty()) {
            // set overview status to latest deployment because there is no active deployment
            builder.targetState(deployments.getItems().get(0).getTargetState());
            builder.currentState(deployments.getItems().get(0).getCurrentState());
        }

        return builder.build();
    }

    private boolean computeDraft(final Integration current, final Integration deployed) {
        final List<Flow> currentFlows = current.getFlows();
        final List<Flow> deployedFlows = deployed.getFlows();

        if (currentFlows.size() != deployedFlows.size()) {
            return true;
        }

        final Iterator<Flow> currentFlowIterator = currentFlows.iterator();
        final Iterator<Flow> deployedFlowIterator = deployedFlows.iterator();
        while (currentFlowIterator.hasNext()) {
            // makes the assumption that flows are ordered
            final Flow currentFlow = currentFlowIterator.next();
            final Flow deployedFlow = deployedFlowIterator.next();

            final List<Step> currentSteps = currentFlow.getSteps();
            final List<Step> deployedSteps = deployedFlow.getSteps();
            if (currentSteps.size() != deployedSteps.size()) {
                return true;
            }

            final Iterator<Step> currentStepsIterator = currentSteps.iterator();
            final Iterator<Step> deployedStepsIterator = deployedSteps.iterator();

            while (currentStepsIterator.hasNext()) {
                final Step currentStep = currentStepsIterator.next();
                final Step deployedStep = deployedStepsIterator.next();

                if (currentStep.getStepKind() != deployedStep.getStepKind()) {
                    return true;
                }

                if (!currentStep.getConfiguredProperties().equals(deployedStep.getConfiguredProperties())) {
                    return true;
                }
            }
        }

        return false;
    }

}
