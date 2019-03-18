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
package io.syndesis.server.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.integration.IntegrationDeploymentError;
import io.syndesis.common.model.integration.IntegrationDeploymentState;

public class StateUpdate {

    private final IntegrationDeploymentState state;
    private final IntegrationDeploymentError error;
    private final String statusMessage;
    private final Map<String, String> stepsPerformed;

   //
   // Constructors
   //
   // Don't expose constructors without stepsPerformed, because this instance will be used to update the IntegrationDeployment and we'll end up loosing info.

    public StateUpdate(IntegrationDeploymentState state, Map<String, String> stepsPerformed) {
        this(state, stepsPerformed, null);
    }

    public StateUpdate(IntegrationDeploymentState state, Map<String, String> stepsPerformed, String statusMessage) {
        this(state, stepsPerformed, statusMessage, null);
    }

    public StateUpdate(IntegrationDeploymentState state, Map<String, String> stepsPerformed, String statusMessage, IntegrationDeploymentError error) {
        this.state = state;
        this.stepsPerformed = Optional.ofNullable(stepsPerformed).orElseGet(Collections::emptyMap);
        this.statusMessage = statusMessage;
        this.error = error;
    }

    public IntegrationDeploymentState getState() {
        return state;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getStepsPerformed() {
        return stepsPerformed;
    }

    public IntegrationDeploymentError getError() {
        return error;
    }
}
