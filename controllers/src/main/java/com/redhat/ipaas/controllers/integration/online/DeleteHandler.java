/*
 *
 *  Copyright (C) 2016 Red Hat, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.redhat.ipaas.controllers.integration.online;

import com.redhat.ipaas.controllers.integration.StatusChangeHandlerProvider;
import com.redhat.ipaas.core.Tokens;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.openshift.OpenShiftDeployment;
import com.redhat.ipaas.openshift.OpenShiftService;

import java.util.*;

public class DeleteHandler implements StatusChangeHandlerProvider.StatusChangeHandler {

    private final OpenShiftService openShiftService;

    DeleteHandler(OpenShiftService openShiftService) {
        this.openShiftService = openShiftService;
    }

    public Set<Integration.Status> getTriggerStatuses() {
        return Collections.singleton(Integration.Status.Deleted);
    }

    @Override
    public StatusUpdate execute(Integration integration) {
        String token = integration.getToken().get();
        Tokens.setAuthenticationToken(token);

        OpenShiftDeployment deployment = OpenShiftDeployment
            .builder()
            .name(integration.getName())
            .token(token)
            .build();

        Integration.Status currentStatus = !openShiftService.exists(deployment)
            || openShiftService.delete(deployment)
            ? Integration.Status.Deleted
            : Integration.Status.Pending;

        return new StatusUpdate(currentStatus);
    }

}
