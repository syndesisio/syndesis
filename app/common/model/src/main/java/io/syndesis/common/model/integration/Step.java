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
package io.syndesis.common.model.integration;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Dependency.Type;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.extension.Extension;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Step.Builder.class)
@SuppressWarnings("immutables")
public interface Step extends WithId<Step>, WithConfiguredProperties, WithDependencies, WithMetadata, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Step;
    }

    Optional<Action> getAction();

    @JsonIgnore
    default <T extends Action> Optional<T> getActionAs(Class<T> type) {
        return getAction().filter(type::isInstance).map(type::cast);
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }

    Optional<Connection> getConnection();

    @JsonIgnore
    default Optional<String> getConnectionId() {
        return getConnection().flatMap(Connection::getId);
    }

    Optional<Extension> getExtension();

    StepKind getStepKind();

    String getName();

    class Builder extends ImmutableStep.Builder {
        // allow access to ImmutableStep.Builder
    }

    default Optional<DataShape> inputDataShape() {
        return getAction().flatMap(Action::getInputDataShape);
    }

    default Optional<DataShape> outputDataShape() {
        return getAction().flatMap(Action::getOutputDataShape);
    }

    default Step updateInputDataShape(final Optional<DataShape> inputDataShape) {
        return getAction().map(a -> builder().action(a.withInputDataShape(inputDataShape))).map(ImmutableStep.Builder::build)
            .orElseThrow(() -> new IllegalStateException("Unable to update input data shape of non existing action"));
    }

    default Step updateOutputDataShape(final Optional<DataShape> outputDataShape) {
        return getAction().map(a -> builder().action(a.withOutputDataShape(outputDataShape))).map(ImmutableStep.Builder::build)
            .orElseThrow(() -> new IllegalStateException("Unable to update output data shape of non existing action"));
    }

    default boolean hasInputDataShape() {
        Optional<DataShape> maybeShape = inputDataShape();
        return maybeShape.isPresent() && maybeShape.get().getKind() != DataShapeKinds.NONE;
    }

    default boolean hasOutputDataShape() {
        Optional<DataShape> maybeShape = outputDataShape();
        return maybeShape.isPresent() && maybeShape.get().getKind() != DataShapeKinds.NONE;
    }

    /**
     * Evaluates if step output shape is a Json schema that is marked to be a unified specification.
     * @return boolean flag marking that output shape is of unified json nature
     */
    default boolean hasUnifiedJsonSchemaOutputShape() {
        Optional<StepAction> stepAction = getActionAs(StepAction.class);
        if (stepAction.isPresent()) {
            Optional<DataShape> outputShape = stepAction.get().getOutputDataShape();
            if (outputShape.isPresent() && outputShape.get().getKind() == DataShapeKinds.JSON_SCHEMA) {
                return outputShape.get().getMetadata()
                        .entrySet()
                        .stream()
                        .anyMatch(entry -> entry.getKey().equals(DataShapeMetaData.UNIFIED));
            }
        }

        return false;
    }

    @JsonIgnore
    default Set<String> getExtensionIds() {
        final Set<String> collected = getDependencies().stream()
            .filter(d -> d.getType() == Type.EXTENSION)
            .map(Dependency::getId)
            .collect(Collectors.toSet());

        getExtension().map(Extension::getExtensionId).map(collected::add);

        return collected;
    }
}
