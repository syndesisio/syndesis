/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.funktion.model.steps.Endpoint;
import io.fabric8.funktion.model.steps.Function;
import io.fabric8.funktion.model.steps.SetBody;
import io.fabric8.funktion.model.steps.SetHeaders;
import io.fabric8.funktion.model.steps.Step;
import io.fabric8.funktion.support.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Flow extends DtoSupport {
    private String name;
    private String trigger;
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

    public Flow trigger(String value) {
        setTrigger(value);
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

    // Steps
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

    public Flow setHeaders(Map<String,Object> headers) {
        return addStep(new SetHeaders(headers));
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Flow ");
        if (!Strings.isEmpty(name)) {
            builder.append(name);
            builder.append(": ");
        }
        if (!Strings.isEmpty(trigger)) {
            builder.append(trigger);
        }
        if (steps != null) {
            for (Step step : steps) {
                builder.append(" => ");
                builder.append(step);
            }
        }
        if (isTraceEnabled()) {
            builder.append(" (tracing) ");
        }
        return builder.toString();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
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
