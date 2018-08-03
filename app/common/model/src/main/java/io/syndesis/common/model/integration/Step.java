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
import java.util.Objects;
import java.util.Optional;
import org.immutables.value.Value;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.extension.Extension;

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

    Optional<Connection> getConnection();

    Optional<Extension> getExtension();

    StepKind getStepKind();

    String getName();

    class Builder extends ImmutableStep.Builder {
        // allow access to ImmutableStep.Builder
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Step} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    default boolean equivalent(Step another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        Connection myConnection = getConnection().orElse(null);
        Connection anotherConnection = another.getConnection().orElse(null);
        if (myConnection == null) {
            if (anotherConnection != null) {
                return false;
            }
        } else if (! myConnection.equivalent(anotherConnection)) {
            return false;
        }

        Extension myExtension = getExtension().orElse(null);
        Extension anotherExtension = another.getExtension().orElse(null);
        if (myExtension == null) {
            if (anotherExtension != null) {
                return false;
            }
        } else if (! myExtension.equivalent(anotherExtension)) {
            return false;
        }

        Action myAction = getAction().orElse(null);
        Action anotherAction = another.getAction().orElse(null);
        if (myAction == null) {
            if (anotherAction != null) {
                return false;
            }
        } else if (! myAction.equivalent(anotherAction)) {
            return false;
        }

        return Objects.equals(getStepKind(), another.getStepKind())
            && Objects.equals(getName(), another.getName())
            && Objects.equals(getId(), another.getId())
            && getConfiguredProperties().equals(another.getConfiguredProperties())
            && getDependencies().equals(another.getDependencies())
            && getMetadata().equals(another.getMetadata());
    }
}
