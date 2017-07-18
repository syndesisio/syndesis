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

import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets headers on the payload
 */
@AutoService(Step.class)
public class SetHeaders extends Step {
    public static final String KIND = "setHeaders";

    private Map<String, Object> headers;

    public SetHeaders() {
        this(new HashMap<>());
    }

    public SetHeaders(Map<String, Object> headers) {
        super(KIND);
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
