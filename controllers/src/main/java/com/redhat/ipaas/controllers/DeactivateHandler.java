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
package com.redhat.ipaas.controllers;

import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.openshift.OpenShiftService;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DeactivateHandler implements WorkflowHandler {

    public static final Set<Integration.Status> DESIRED_STATE_TRIGGERS =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Integration.Status.Deactivated, Integration.Status.Draft
        )));


    private final DataManager dataManager;
    private final OpenShiftService openShiftService;

    public DeactivateHandler(DataManager dataManager, OpenShiftService openShiftService) {
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
    }

    public Set<Integration.Status> getTriggerStatuses() {
        return DESIRED_STATE_TRIGGERS;
    }

    @Override
    public Optional<Integration.Status> execute(Integration integration) throws Exception {
        Integration.Status currentStatus = openShiftService.isDeploymentConfigScaled(integration.getName(), 0)
            ? Integration.Status.Deactivated
            : Integration.Status.Pending;

        openShiftService.scaleDeploymentConfig(integration.getName(), 0);

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(integration)
            .currentStatus(currentStatus)
            .lastUpdated(new Date())
            .build();

        dataManager.update(updatedIntegration);
        return integration.getDesiredStatus();
    }

}
