/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import io.syndesis.controllers.integration.StatusChangeHandlerProvider;
import io.syndesis.core.Tokens;
import io.syndesis.model.integration.Integration;
import io.syndesis.openshift.OpenShiftDeployment;
import io.syndesis.openshift.OpenShiftService;

public class DeleteHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    private final OpenShiftService openShiftService;

    DeleteHandler(OpenShiftService openShiftService) {
        this.openShiftService = openShiftService;
    }

    @Override
    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Deleted);
    }

    @Override
    public StatusUpdate execute(Integration integration) {
        String username = integration.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        String token = integration.getToken().get();
        Tokens.setAuthenticationToken(token);

        OpenShiftDeployment deployment = OpenShiftDeployment
            .builder()
            .name(integration.getName())
            .username(username)
            .revisionId(integration.getDeployedRevisionId().orElse(1))
            .token(token)
            .build();

        Integration.Status currentStatus = !openShiftService.exists(deployment)
            || openShiftService.delete(deployment)
            ? Integration.Status.Deleted
            : Integration.Status.Pending;

        return new StatusUpdate(currentStatus);
    }

}
