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
package io.syndesis.server.controller.integration.online;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Labels;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.openshift.OpenShiftService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UnpublishHandler extends BaseHandler implements StateChangeHandler {

    UnpublishHandler(OpenShiftService openShiftService) {
        super(openShiftService);
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Unpublished);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        Map<String, String> stepsDone = new HashMap<>(integrationDeployment.getStepsDone());
        stepsDone.remove("deploy"); //we are literally undoing this step.

        IntegrationDeploymentState currentState = IntegrationDeploymentState.Pending;

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationDeployment.getIntegrationId().get()));
        labels.put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, String.valueOf(integrationDeployment.getVersion()));

        if (!openShiftService().getDeploymentsByLabel(labels).isEmpty()) {
            try {
                openShiftService().scale(integrationDeployment.getSpec().getName(), labels, 0, 1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new StateUpdate(currentState, stepsDone);
            }
        } else  {
            currentState = IntegrationDeploymentState.Unpublished;
        }

        return new StateUpdate(currentState, stepsDone);
    }

}
