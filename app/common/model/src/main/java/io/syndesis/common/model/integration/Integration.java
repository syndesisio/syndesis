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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.validation.UniquenessRequired;
import io.syndesis.common.model.validation.integration.NoDuplicateIntegration;

@Value.Immutable
@JsonDeserialize(builder = Integration.Builder.class)
@NoDuplicateIntegration(groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
public interface Integration extends WithId<Integration>, IntegrationBase {

    @Override
    default Kind getKind() {
        return Kind.Integration;
    }

    class Builder extends ImmutableIntegration.Builder {
        // allow access to ImmutableIntegration.Builder
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
     *<p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Integration} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    default boolean equivalent(Integration another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        List<Connection> myConnections = getConnections();
        if (myConnections == null) {
            if (another.getConnections() != null) {
                return false;
            }
        } else {
            for (Connection myConnection : myConnections) {
                Connection anotherConnection = another.findConnectionById(myConnection.getId().get()).orElse(null);
                if (! myConnection.equivalent(anotherConnection)) {
                    return false;
                }
            }
        }

        List<Step> mySteps = getSteps();
        if (mySteps == null) {
            if (another.getSteps() != null) {
                return false;
            }
        } else {
            for (Step myStep : mySteps) {
                Step anotherStep = another.findStepById(myStep.getId().get()).orElse(null);
                if (! myStep.equivalent(anotherStep)) {
                    return false;
                }
            }
        }

        return Objects.equals(getId(), another.getId())
                            && isDeleted() == another.isDeleted()
                            && getResources().equals(another.getResources())
                            && Objects.equals(getScheduler(), another.getScheduler())
                            && Objects.equals(getDescription(), another.getDescription())
                            && getTags().equals(another.getTags())
                            && Objects.equals(getName(), another.getName());
    }
}
