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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.WithId;
import java.util.Objects;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = StepAction.Builder.class)
@SuppressWarnings("immutables")
public interface StepAction extends Action, WithId<StepAction> {

    @Override
    @Value.Default
    default String getActionType() {
        return Action.TYPE_STEP;
    }

    @Override
    default StepAction withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    @Override
    StepDescriptor getDescriptor();

    class Builder extends ImmutableStepAction.Builder {
    }


    enum Kind {
        STEP,
        BEAN,
        ENDPOINT
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
     * @param another a {@link StepAction} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @Override
    default boolean equivalent(Action another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        if (! (another instanceof StepAction)) {
            return false;
        }

        StepAction stepAction = (StepAction) another;

        StepDescriptor myDescriptor = getDescriptor();
        StepDescriptor anotherDescriptor = stepAction.getDescriptor();
        if (myDescriptor == null) {
            if (anotherDescriptor != null) {
                return false;
            }
        } else if (! myDescriptor.equivalent(anotherDescriptor)) {
            return false;
        }

        return getActionType().equals(another.getActionType())
                            && Objects.equals(getDescription(), another.getDescription())
                            && Objects.equals(getPattern(), another.getPattern())
                            && Objects.equals(getId(), another.getId())
                            && Objects.equals(getName(), another.getName())
                            && getTags().equals(another.getTags())
                            && getMetadata().equals(another.getMetadata());
    }
}
