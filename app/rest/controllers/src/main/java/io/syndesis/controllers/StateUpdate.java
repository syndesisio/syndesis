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
package io.syndesis.controllers;

import io.syndesis.model.integration.IntegrationDeploymentState;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StateUpdate {

    private final IntegrationDeploymentState state;
    private final String statusMessage;
    private final List<String> stepsPerformed;


    public StateUpdate(IntegrationDeploymentState state, String statusMessage) {
        this(state, Collections.emptyList(), statusMessage);
    }

    public StateUpdate(IntegrationDeploymentState state) {
        this(state, Collections.emptyList(), null);
    }

    public StateUpdate(IntegrationDeploymentState state, List<String> stepsPerformed) {
        this(state, stepsPerformed, null);
    }

    public StateUpdate(IntegrationDeploymentState state, List<String> stepsPerformed, String statusMessage) {
        this.state = state;
        this.stepsPerformed = Optional.ofNullable(stepsPerformed).orElseGet(Collections::emptyList);
        this.statusMessage = statusMessage;
    }

    public IntegrationDeploymentState getState() {
        return state;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

     public List<String> getStepsPerformed() {
        return stepsPerformed;
    }
}
