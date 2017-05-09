/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dao.init;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.core.Json;
import io.syndesis.model.Kind;
import io.syndesis.model.ToJson;

import java.io.IOException;

/**
 * Used to read the deployment.json file from the client GUI project
 *
 */
public class ModelData implements ToJson {

    private Kind kind;
    private Object data;
    private String json;

    public ModelData() {
    }

    public ModelData(Kind kind, Object data) {
        super();
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
            json = Json.mapper().writeValueAsString(data);
        }
        return json;
    }

    @JsonRawValue
    @JsonProperty("data")
    public void setDataFromJson(JsonNode json) throws JsonProcessingException {
        this.data = null;
        this.json = Json.mapper().writeValueAsString(json);
    }

    @JsonIgnore
    public Object getData() throws IOException {
        if (data == null && kind != null && json != null) {
            data = Json.mapper().readValue(json, kind.getModelClass());
        }
        return data;
    }

    @JsonIgnore
    public void setData(Object data) {
        this.data = data;
        this.json = null;
    }

}
