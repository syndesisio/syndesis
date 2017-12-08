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
package io.syndesis.integration.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.syndesis.integration.model.steps.Choice;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Filter;
import io.syndesis.integration.model.steps.Function;
import io.syndesis.integration.model.steps.Log;
import io.syndesis.integration.model.steps.SetBody;
import io.syndesis.integration.model.steps.SetHeaders;
import io.syndesis.integration.model.steps.Split;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.model.steps.Throttle;
import io.syndesis.integration.support.Strings;

public class Flow extends DtoSupport {
    private String name;
    private Boolean trace;
    private Boolean logResult;
    private Boolean singleMessageMode;
    private List<Step> steps = new ArrayList<>();

    public Flow addStep(Step step) {
        steps.add(step);
        return this;
    }

    public Flow name(String value) {
        setName(value);
        return this;
    }

    public Flow logResult(boolean value) {
        setLogResult(value);
        return this;
    }

    public Flow trace(boolean value) {
        setTrace(value);
        return this;
    }

    public Flow singleMessageMode(boolean value) {
        setSingleMessageMode(value);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Flow ");
        if (!Strings.isEmpty(name)) {
            builder.append(name);
            builder.append(": ");
        }
        if (steps != null) {
            boolean first = true;
            for (Step step : steps) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" => ");
                }
                builder.append(step);
            }
        }
        if (isTraceEnabled()) {
            builder.append(" (tracing) ");
        }
        return builder.toString();
    }

    // DSL
    //-------------------------------------------------------------------------
    public Flow endpoint(String uri) {
        return addStep(new Endpoint(uri));
    }

    public Flow function(String name) {
        return addStep(new Function(name));
    }

    public Flow setBody(String body) {
        return addStep(new SetBody(body));
    }

    public Flow setHeaders(Map<String, Object> headers) {
        return addStep(new SetHeaders(headers));
    }

    public Split split(String expression) {
        Split step = new Split(expression);
        addStep(step);
        return step;
    }

    public Filter filter(String expression) {
        Filter step = new Filter(expression);
        addStep(step);
        return step;
    }

    public Choice choice() {
        Choice step = new Choice();
        addStep(step);
        return step;
    }

    public Throttle throttle(long maximumRequests) {
        Throttle step = new Throttle(maximumRequests);
        addStep(step);
        return step;
    }

    public Throttle throttle(long maximumRequests, long periodMillis) {
        Throttle step = new Throttle(maximumRequests, periodMillis);
        addStep(step);
        return step;
    }

    public Log log(String message, String loggingLevel, String logger, String marker) {
        Log step = new Log(message, loggingLevel, logger, marker);
        addStep(step);
        return step;
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Boolean getTrace() {
        return trace;
    }

    public void setTrace(Boolean trace) {
        this.trace = trace;
    }


    public Boolean getLogResult() {
        return logResult;
    }

    public void setLogResult(Boolean logResult) {
        this.logResult = logResult;
    }

    public Boolean getSingleMessageMode() {
        return singleMessageMode;
    }

    public void setSingleMessageMode(Boolean singleMessageMode) {
        this.singleMessageMode = singleMessageMode;
    }

    @JsonIgnore
    public boolean isTraceEnabled() {
        return trace != null && trace.booleanValue();
    }

    @JsonIgnore
    public boolean isLogResultEnabled() {
        return logResult != null && logResult.booleanValue();
    }

    @JsonIgnore
    public boolean isSingleMessageModeEnabled() {
        return singleMessageMode != null && singleMessageMode.booleanValue();
    }
}
