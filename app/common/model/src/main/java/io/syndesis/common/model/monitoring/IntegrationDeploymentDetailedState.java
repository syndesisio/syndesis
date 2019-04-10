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
package io.syndesis.common.model.monitoring;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum to describe current Integration Deployment sub state
 * while transitioning (publishing/un-publishing) an integration's state.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum IntegrationDeploymentDetailedState {

    ASSEMBLING(1, Constants.TOTAL_PUBLISHING_STEPS),
    BUILDING(2, Constants.TOTAL_PUBLISHING_STEPS),
    DEPLOYING(3, Constants.TOTAL_PUBLISHING_STEPS),
    STARTING(4, Constants.TOTAL_PUBLISHING_STEPS);

    private final int currentStep;
    private final int totalSteps;

    @JsonProperty
    public int getCurrentStep() {
        return currentStep;
    }

    @JsonProperty
    public int getTotalSteps() {
        return totalSteps;
    }

    @JsonProperty
    public String getValue() {
        return this.name();
    }

    IntegrationDeploymentDetailedState(int currentStep, int totalSteps) {
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
    }

    @JsonCreator
    static IntegrationDeploymentDetailedState fromValue(@JsonProperty("value") String value,
                                                        @JsonProperty("currentStep") int currentStep,
                                                        @JsonProperty("totalSteps") int totalSteps) {
        return Arrays.stream(values())
                .filter(v -> v.getValue().equals(value)
                        && v.getCurrentStep() == currentStep
                        && v.getTotalSteps() == totalSteps)
                .findFirst()
                .get();
    }

    private static class Constants {
        static final int TOTAL_PUBLISHING_STEPS = 4;
    }
}
