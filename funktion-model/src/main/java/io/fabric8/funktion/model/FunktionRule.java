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
import io.fabric8.funktion.support.Strings;

import java.util.ArrayList;
import java.util.List;

public class FunktionRule extends DtoSupport {
    private String name;
    private String trigger;
    private Boolean trace;
    private List<FunktionAction> actions = new ArrayList<>();

    public FunktionAction addAction(FunktionAction action) {
        actions.add(action);
        return action;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("FUNKTION ");
        if (!Strings.isEmpty(name)) {
            builder.append(name);
            builder.append(": ");
        }
        if (!Strings.isEmpty(trigger)) {
            builder.append(trigger);
        }
        if (actions != null) {
            for (FunktionAction action : actions) {
                builder.append(" => ");
                builder.append(action);
            }
        }
        if (isTracing()) {
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

    public List<FunktionAction> getActions() {
        return actions;
    }

    public void setActions(List<FunktionAction> actions) {
        this.actions = actions;
    }


    @JsonIgnore
    public boolean isTracing() {
        return trace != null && trace.booleanValue();
    }

    public Boolean getTrace() {
        return trace;
    }

    public void setTrace(Boolean trace) {
        this.trace = trace;
    }
}
