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
package io.syndesis.controllers.integration.online;

import java.util.Collections;
import java.util.Set;

import io.syndesis.controllers.StateChangeHandler;
import io.syndesis.controllers.StateUpdate;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;
import io.syndesis.openshift.OpenShiftService;

public class DeleteHandler extends BaseHandler implements StateChangeHandler {
    DeleteHandler(OpenShiftService openShiftService) {
        super(openShiftService);
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Undeployed);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        IntegrationDeploymentState currentState = !openShiftService().exists(integrationDeployment.getSpec().getName())
            || openShiftService().delete(integrationDeployment.getSpec().getName())
            ? IntegrationDeploymentState.Undeployed
            : IntegrationDeploymentState.Pending;
        logInfo(integrationDeployment,"Deleted");

        return new StateUpdate(currentState);
    }

}
