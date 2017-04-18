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

import com.redhat.ipaas.model.integration.Integration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConditionalOnProperty(value = "controllers.integration.enabled", havingValue = "false")
public class DemoActivateHandler implements WorkflowHandler {

    public static final Set<Integration.Status> DESIRED_STATE_TRIGGERS =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Integration.Status.Activated)));

    public Set<Integration.Status> getTriggerStatuses() {
        return DESIRED_STATE_TRIGGERS;
    }

    @Override
    public Integration execute(Integration integration) {

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            return integration;
        }

        Integration updatedIntegration = new Integration.Builder()
            .createFrom(integration)
            .currentStatus(Integration.Status.Activated)
            .lastUpdated(new Date())
            .build();
        return updatedIntegration;
    }

}


