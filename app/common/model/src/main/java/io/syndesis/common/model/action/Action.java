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
package io.syndesis.common.model.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithConfigurationProperties;
import io.syndesis.common.model.WithKind;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.connection.ConfigurationProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use      = JsonTypeInfo.Id.NAME,
    include  = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "actionType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(
        value = ImmutableConnectorAction.class,
        name  = Action.TYPE_CONNECTOR),
    @JsonSubTypes.Type(
        value = ImmutableStepAction.class,
        name  = Action.TYPE_STEP)
})
@JsonPropertyOrder({ "id", "name", "description", "descriptor", "tags", "actionType" })
@JsonIgnoreProperties(value = {"properties", "inputDataShape", "outputDataShape"}, allowGetters = true)
public interface Action extends WithResourceId, WithKind, WithName, WithTags, WithConfigurationProperties, WithMetadata, Serializable {
    String TYPE_CONNECTOR = "connector";
    String TYPE_STEP = "step";

    enum Pattern { From, Pipe, To }

    @Override
    default Kind getKind() {
        return Kind.Action;
    }

    String getActionType();

    String getDescription();

    ActionDescriptor getDescriptor();

    Pattern getPattern();

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default Optional<DataShape> getInputDataShape() {
        ActionDescriptor descriptor = getDescriptor();

        return descriptor != null
            ? descriptor.getInputDataShape()
            : Optional.empty();
    }

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default Optional<DataShape> getOutputDataShape() {
        ActionDescriptor descriptor = getDescriptor();

        return descriptor != null
            ? descriptor.getOutputDataShape()
            : Optional.empty();
    }

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    default Map<String, ConfigurationProperty> getProperties() {
        ActionDescriptor descriptor = getDescriptor();

        return descriptor != null
            ? descriptor.getPropertyDefinitionSteps().stream()
                .flatMap(step -> step.getProperties().entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
            : Collections.emptyMap();
    }

    Action withInputDataShape(Optional<DataShape> inputDataShape);

    Action withOutputDataShape(Optional<DataShape> outputDataShape);
}
