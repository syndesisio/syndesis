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
package io.syndesis.camel.component.proxy.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.syndesis.integration.model.steps.FromStep;
import io.syndesis.integration.model.steps.Step;

public final class Connector extends Step implements FromStep {
    public static final String KIND = "connector";

    private String componentId;
    private String componentScheme;
    private Map<String, Object> properties;
    private List<String> customizers;
    private String factory;

    public Connector() {
        this(null, null, null, null, null);
    }

    public Connector(String componentId, String componentScheme) {
        this(componentId, componentScheme, null, null, null);
    }

    public Connector(String componentId, String componentScheme, Map<String, Object> properties) {
        this(componentId, componentScheme, properties, null, null);
    }

    public Connector(String componentId, String componentScheme, Map<String, Object> properties, String factory, List<String> customizers) {
        super(KIND);

        setComponentId(componentId);
        setComponentScheme(componentScheme);
        setProperties(properties);
        setFactory(factory);
        setCustomizers(customizers);
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentScheme() {
        return componentScheme;
    }

    public void setComponentScheme(String componentScheme) {
        this.componentScheme = componentScheme;
    }

    public Map<String, Object> getProperties() {
        return clone(this.properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = clone(properties);
    }

    public List<String> getCustomizers() {
        return clone(customizers);
    }

    public void setCustomizers(List<String> customizers) {
        this.customizers = clone(customizers);
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    @Override
    public String toString() {
        return "Connector{" +
            "componentId='" + componentId + '\'' +
            ", componentScheme='" + componentScheme + '\'' +
            ", properties=" + properties +
            ", customizers=" + customizers +
            ", factory=" + factory +
            '}';
    }

    private static final Map<String, Object> clone(Map<String, Object> map) {
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return new TreeMap<>(map);
        }
    }

    private static final List<String> clone(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(list);
        }
    }
}
