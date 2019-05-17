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
package io.syndesis.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.util.Json;

import java.io.IOException;

/**
 * Used to read the deployment.json file from the client GUI project
 *
 */
public class ModelData<T extends WithId<T>> implements ToJson {

    private Kind kind;
    private T data;
    private String json;
    private String condition;

    public ModelData() {
        // makes it easier to handle as a Java bean
    }

    public ModelData(Kind kind, T data) {
        this.kind = kind;
        this.data = data;
    }


    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @JsonRawValue
    @JsonProperty("data")
    public String getDataAsJson() throws JsonProcessingException {
        if (json == null) {
            json = Json.writer().writeValueAsString(data);
        }
        return json;
    }

    @JsonRawValue
    @JsonProperty("data")
    public void setDataFromJson(JsonNode json) throws JsonProcessingException {
        this.data = null;
        this.json = Json.writer().writeValueAsString(json);
    }

    @JsonIgnore
    public T getData() throws IOException {
        if (data == null && kind != null && json != null) {
            @SuppressWarnings("unchecked")
            final Class<T> modelClass = (Class<T>) kind.getModelClass();
            data = Json.reader().forType(modelClass).readValue(json);
        }
        return data;
    }

    @JsonIgnore
    public void setData(T data) {
        this.data = data;
        this.json = null;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}
