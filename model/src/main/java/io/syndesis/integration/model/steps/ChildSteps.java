/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.model.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reuable base class for steps which contain nested steps
 */
public abstract class ChildSteps<T extends ChildSteps> extends Step {
    protected List<Step> steps = new ArrayList<>();

    public ChildSteps(String kind) {
        super(kind);
    }

    public ChildSteps(String kind, List<Step> steps) {
        super(kind);
        this.steps = steps;
    }

    public T addStep(Step step) {
        steps.add(step);
        return (T) this;
    }

    // DSL
    //-------------------------------------------------------------------------
    public T endpoint(String uri) {
        return addStep(new Endpoint(uri));
    }

    public T function(String name) {
        return addStep(new Function(name));
    }

    public T setBody(String body) {
        return addStep(new SetBody(body));
    }

    public T setHeaders(Map<String, Object> headers) {
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

    // Properties
    //-------------------------------------------------------------------------

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}
