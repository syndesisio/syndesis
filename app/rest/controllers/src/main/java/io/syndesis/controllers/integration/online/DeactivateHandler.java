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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.syndesis.controllers.StateChangeHandler;
import io.syndesis.controllers.StateUpdate;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationRevision;
import io.syndesis.model.integration.IntegrationRevisionState;
import io.syndesis.openshift.OpenShiftService;

public class DeactivateHandler extends BaseHandler implements StateChangeHandler {
    DeactivateHandler(OpenShiftService openShiftService) {
        super(openShiftService);
    }

    @Override
    public Set<IntegrationRevisionState> getTriggerStates() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            IntegrationRevisionState.Inactive, IntegrationRevisionState.Draft)));
    }

    @Override
    public StateUpdate execute(IntegrationRevision integrationRevision) {
        try {
            openShiftService().scale(integrationRevision.getName(), 0);
            logInfo(integrationRevision,"Deactivated");
        } catch (KubernetesClientException e) {
            // Ignore 404 errors, means the deployment does not exist for us
            // to scale down
            if( e.getCode() != 404 ) {
                throw e;
            }
        }

        IntegrationRevisionState currentState = openShiftService().isScaled(integrationRevision.getName(), 0)
            ? IntegrationRevisionState.Inactive
                : IntegrationRevisionState.Pending;

        return new StateUpdate(currentState);
    }

}
