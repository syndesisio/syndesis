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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.integration.Step;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(builder = LogStep.Builder.class)
@SuppressWarnings("immutables")
public interface LogStep extends Step {

    String STEP_KIND = "log";

    @JsonProperty("propertyNames")
    List<String> getPropertyNames();

    @JsonProperty("inHeaderNames")
    List<String> getInHeaderNames();

    @JsonProperty("outHeaderNames")
    List<String> getOutHeaderNames();

    @JsonProperty("bodyLoggingEnabled")
    boolean isBodyLoggingEnabled();

    @JsonProperty("expression")
    String getExpression();

    class Builder extends ImmutableLogStep.Builder { }
}
