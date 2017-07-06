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
package io.syndesis.integration.model.steps;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.syndesis.integration.model.StepKinds;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs a content based routing.
 * <p>
 * Finds and evaluates the first {@link Filter} or invokes the otherwise steps
 */
@JsonPropertyOrder({"filters", "otherwise"})
public class Choice extends Step {
    private List<Filter> filters = new ArrayList<>();
    private Otherwise otherwise;

    public Choice() {
        super(StepKinds.CHOICE);
    }

    public Choice(List<Filter> filters, Otherwise otherwise) {
        this.filters = filters;
        this.otherwise = otherwise;
    }

    @Override
    public String toString() {
        return "Choice: filters: " + filters + " otherwise: " + otherwise;
    }


    // DSL
    //-------------------------------------------------------------------------
    public Filter when(String expression) {
        Filter step = new Filter(expression);
        filters.add(step);
        return step;
    }

    public Otherwise otherwise() {
        if (otherwise == null) {
            otherwise = new Otherwise();
        }
        return otherwise;
    }

    // Properties
    //-------------------------------------------------------------------------

    public String getKind() {
        return StepKinds.CHOICE;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public Otherwise getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Otherwise otherwise) {
        this.otherwise = otherwise;
    }
}
