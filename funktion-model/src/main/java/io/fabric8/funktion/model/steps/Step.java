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
package io.fabric8.funktion.model.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.fabric8.funktion.model.StepKinds;

/**
 * Defines the a step in a funktion flow
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Choice.class, name = StepKinds.CHOICE),
        @JsonSubTypes.Type(value = Endpoint.class, name = StepKinds.ENDPOINT),
        @JsonSubTypes.Type(value = Filter.class, name = StepKinds.FILTER),
        @JsonSubTypes.Type(value = Function.class, name = StepKinds.FUNCTION),
        @JsonSubTypes.Type(value = Otherwise.class, name = StepKinds.OTHERWISE),
        @JsonSubTypes.Type(value = SetBody.class, name = StepKinds.SET_BODY),
        @JsonSubTypes.Type(value = SetHeaders.class, name = StepKinds.SET_HEADERS),
        @JsonSubTypes.Type(value = Split.class, name = StepKinds.SPLIT),
        @JsonSubTypes.Type(value = Throttle.class, name = StepKinds.THROTTLE)}
)
public abstract class Step {
    private String kind;

    public Step() {
    }

    public Step(String kind) {
        this.kind = kind;
    }

    @JsonIgnore
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

}
