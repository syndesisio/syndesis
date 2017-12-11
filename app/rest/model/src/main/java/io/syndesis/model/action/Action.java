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
package io.syndesis.model.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.syndesis.model.DataShape;
import io.syndesis.model.Kind;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.WithKind;
import io.syndesis.model.WithName;
import io.syndesis.model.WithTags;
import io.syndesis.model.connection.ConfigurationProperty;

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
        value = ImmutableExtensionAction.class,
        name  = Action.TYPE_EXTENSION)
})
@JsonPropertyOrder({ "id", "name", "description", "descriptor", "tags", "actionType" })
@JsonIgnoreProperties(value = {"properties", "inputDataShape", "outputDataShape"}, allowGetters = true)
public interface Action<D extends ActionDescriptor> extends WithKind, WithName, WithTags, WithConfigurationProperties, Serializable {
    String TYPE_CONNECTOR = "connector";
    String TYPE_EXTENSION = "extension";

    enum Pattern { From, To }

    @Override
    default Kind getKind() {
        return Kind.Action;
    }

    String getActionType();

    String getDescription();

    D getDescriptor();

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
}
