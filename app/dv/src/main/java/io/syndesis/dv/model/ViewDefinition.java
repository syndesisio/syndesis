/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicUpdate;
import io.syndesis.dv.repository.JpaConverterJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Represents the configuration of a view editor state
 */
@Entity
@JsonSerialize(as = ViewDefinition.class)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@DynamicUpdate
public class ViewDefinition extends BaseEntity {

    public static class State {
        private List<String> sourcePaths = new ArrayList<>(1);

        public List<String> getSourcePaths() {
            return sourcePaths;
        }
        public void setSourcePaths(List<String> sourcePaths) {
            this.sourcePaths = sourcePaths;
        }

        @Override
        public int hashCode() {
            return sourcePaths.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof State)) {
                return false;
            }
            return Objects.equals(sourcePaths, ((State)obj).sourcePaths);
        }
    }

    public static class ViewDefinitionStateConvertor extends JpaConverterJson<State> {
        @Override
        public Class<State> targetClass() {
            return State.class;
        }
    }

    private String ddl;
    @Column(name = "dv_name")
    private String dataVirtualizationName;
    private String description;
    private boolean complete;
    private boolean userDefined;
    @JsonIgnore
    private boolean parsable;

    @JsonIgnore //for non-Entity serialization, the getters/setters will be used
    @Convert(converter = ViewDefinitionStateConvertor.class)
    private State state = new State();

    protected ViewDefinition() {
    }

    public ViewDefinition(String dataVirtualizationName, String name) {
        setName(name);
        this.dataVirtualizationName = dataVirtualizationName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDdl() {
        return this.ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public void setUserDefined(boolean userDefined) {
        this.userDefined = userDefined;
    }

    public boolean isUserDefined() {
        return this.userDefined;
    }

    public List<String> getSourcePaths() {
        return state.sourcePaths;
    }

    public void addSourcePath(String sourcePath) {
        this.getSourcePaths().add(sourcePath);
    }

    public void setSourcePaths(List<String> sourcePaths) {
        state.sourcePaths = sourcePaths;
    }

    public String getDataVirtualizationName() {
        return dataVirtualizationName;
    }

    public void setDataVirtualizationName(String dataVirtualizationName) {
        this.dataVirtualizationName = dataVirtualizationName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void clearState() {
        this.state = new State();
    }

    public boolean isParsable() {
        return parsable;
    }

    public void setParsable(boolean parsable) {
        this.parsable = parsable;
    }

}
