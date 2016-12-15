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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.funktion.model.StepKinds;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets headers on the payload
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class SetHeaders extends Step {
    private Map<String, Object> headers;

    public SetHeaders() {
        super(StepKinds.SET_HEADERS);
        headers = new HashMap<>();
    }

    public SetHeaders(Map<String, Object> headers) {
        super("setHeaders");
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "SetHeaders: " + headers;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
