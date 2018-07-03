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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum to describe current Integration Deployment sub state
 * while transitioning (publishing/un-publishing) an integration's state.
 */
public enum IntegrationDeploymentDetailedState {

    ASSEMBLING(1, Constants.TOTAL_PUBLISHING_STEPS),
    BUILDING(2, Constants.TOTAL_PUBLISHING_STEPS),
    DEPLOYING(3, Constants.TOTAL_PUBLISHING_STEPS),
    STARTING(4, Constants.TOTAL_PUBLISHING_STEPS);

    private static final Pattern VALUE_PATTERN = Pattern.compile("(\\w+)\\(\\d+/\\d+\\)");
    private final int currentStep;
    private final int totalSteps;

    @JsonValue
    public int getCurrentStep() {
        return currentStep;
    }

    @JsonValue
    public int getTotalSteps() {
        return totalSteps;
    }

    IntegrationDeploymentDetailedState(int currentStep, int totalSteps) {
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
    }

    @JsonValue
    @Override
    public String toString() {
        return String.format("%s(%d/%d)", super.name(), currentStep, totalSteps);
    }

    @JsonCreator
    public static IntegrationDeploymentDetailedState fromString(String value) {
        final Matcher matcher = VALUE_PATTERN.matcher(value);
        if (matcher.matches()) {
            final String name = matcher.group(1);
            for (IntegrationDeploymentDetailedState state : values()) {
                if (state.name().equals(name)) {
                    return state; // ignore current step and totalsteps from value!
                }
            }
        }
        throw new IllegalArgumentException("Illegal value " + value);
    }

    private static class Constants {
        public static final int TOTAL_PUBLISHING_STEPS = 4;
    }
}
