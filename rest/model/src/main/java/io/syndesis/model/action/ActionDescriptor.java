/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.model.action;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.core.immutable.ImmutablesStyle;
import io.syndesis.model.DataShape;
import io.syndesis.model.WithName;
import io.syndesis.model.WithProperties;
import org.immutables.value.Value;

public interface ActionDescriptor {
    @Value.Immutable
    @JsonDeserialize(builder = ActionDescriptor.ActionDescriptorStep.Builder.class)
    @ImmutablesStyle
    interface ActionDescriptorStep extends WithName, WithProperties, Serializable {

        final class Builder extends ImmutableActionDescriptorStep.Builder {
            // make ImmutableActionDefinitionStep.Builder accessible
        }

        String getDescription();
    }

    Optional<DataShape> getInputDataShape();

    Optional<DataShape> getOutputDataShape();

    List<ActionDescriptorStep> getPropertyDefinitionSteps();
}
