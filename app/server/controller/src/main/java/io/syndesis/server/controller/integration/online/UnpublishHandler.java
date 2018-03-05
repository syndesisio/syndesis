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

import java.util.Collections;
import java.util.Set;

import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.openshift.OpenShiftService;

public class UnpublishHandler extends BaseHandler implements StateChangeHandler {

    private final DataManager dataManager;

    UnpublishHandler(OpenShiftService openShiftService, DataManager dataManager) {
        super(openShiftService);
        this.dataManager = dataManager;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Unpublished);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        IntegrationDeploymentState currentState = !openShiftService().exists(integrationDeployment.getSpec().getName())
            || openShiftService().delete(integrationDeployment.getSpec().getName())
            ? IntegrationDeploymentState.Unpublished
            : IntegrationDeploymentState.Pending;

        if (currentState == IntegrationDeploymentState.Unpublished) {
            logInfo(integrationDeployment,"Deleted");
            IntegrationDeployment updated = new IntegrationDeployment.Builder().createFrom(integrationDeployment).addAllStepsDone(Collections.emptyList()).build();
            dataManager.update(updated);
        }
        return new StateUpdate(currentState);
    }

}
