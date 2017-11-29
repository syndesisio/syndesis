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
package io.syndesis.integration.model.steps;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.google.auto.service.AutoService;

/**
 * Invokes an endpoint URI (typically HTTP or HTTPS) with the current payload
 */
@AutoService(Step.class)
public final class Endpoint extends Step implements FromStep {
    public static final String KIND = "endpoint";

    private String uri;
    private Map<String, Object> properties;

    public Endpoint() {
        super(KIND);
    }

    public Endpoint(String uri) {
        this(uri, null);
    }

    public Endpoint(String uri, Map<String, Object> properties) {
        super(KIND);
        setUri(uri);
        setProperties(properties);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, Object> getProperties() {
        return clone(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = clone(properties);
    }

    @Override
    public String toString() {
        return "Endpoint{"
            + "uri='" + uri + '\''
            + ", properties=" + properties
            + '}';
    }

    private static final Map<String, Object> clone(Map<String, Object> properties) {
        if (properties == null) {
            return Collections.emptyMap();
        } else {
            return new TreeMap<>(properties);
        }
    }
}
