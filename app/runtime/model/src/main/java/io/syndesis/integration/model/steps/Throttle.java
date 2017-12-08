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
package io.syndesis.integration.model.steps;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.service.AutoService;

/**
 * Throttles the flow
 */
@AutoService(Step.class)
@JsonPropertyOrder({"maximumRequests", "periodMillis"})
public class Throttle extends ChildSteps<Throttle> {
    public static final String KIND = "throttle";

    private long maximumRequests;
    private Long periodMillis;

    public Throttle() {
        super(KIND);
    }

    public Throttle(long maximumRequests) {
        super(KIND);
        this.maximumRequests = maximumRequests;
    }

    public Throttle(long maximumRequests, long periodMillis) {
        super(KIND);
        this.maximumRequests = maximumRequests;
        this.periodMillis = periodMillis;
    }

    @Override
    public String toString() {
        return "Throttle: " + maximumRequests;
    }

    public long getMaximumRequests() {
        return maximumRequests;
    }

    public void setMaximumRequests(long maximumRequests) {
        this.maximumRequests = maximumRequests;
    }

    public Long getPeriodMillis() {
        return periodMillis;
    }

    public void setPeriodMillis(Long periodMillis) {
        this.periodMillis = periodMillis;
    }
}
