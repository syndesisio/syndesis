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
package io.syndesis.model.log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.integration.Step;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonDeserialize(builder = LogStep.Builder.class)
@SuppressWarnings("immutables")
public interface LogStep extends Step {

    String STEP_KIND = "log";

    default List<String> getPropertyNames() {
        return asList("propertyNames", getConfiguredProperties());
    }


    default List<String> getInHeaderNames() {
        return asList("inHeaderNames", getConfiguredProperties());
    }


    default List<String> getOutHeaderNames() {
        return asList("outHeaderNames", getConfiguredProperties());
    }

    default Boolean isBodyLoggingEnabled() {
        final Map<String, String> props = getConfiguredProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(props.getOrDefault("bodyLoggingEnabled", "false"));
    }

    default String getExpression() {
        final Map<String, String> props = getConfiguredProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }
        return props.get("expression");
    }


    static List<String> asList(String propertyName, Map<String, String> props) {
        if (props == null || props.isEmpty()) {
            return null;
        }
        String names = props.get(propertyName);
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(names.split("[ \\n,]+"));
    }

    class Builder extends ImmutableLogStep.Builder { }
}
