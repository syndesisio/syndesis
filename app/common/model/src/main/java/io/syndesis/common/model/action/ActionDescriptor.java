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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;
import io.syndesis.common.util.immutable.ImmutablesStyle;
import org.immutables.value.Value;

@SuppressWarnings("immutables")
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

    @Value.Default
    default List<ActionDescriptorStep> getPropertyDefinitionSteps() {
        return Collections.emptyList();
    }

    @JsonIgnore
    default Map<String, ActionDescriptorStep> getPropertyDefinitionStepsAsMap() {
        return getPropertyDefinitionSteps().stream().collect(Collectors.toMap(WithName::getName, p -> p));
    }
}
