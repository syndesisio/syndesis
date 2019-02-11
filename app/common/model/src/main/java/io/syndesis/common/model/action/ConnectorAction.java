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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.WithId;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ConnectorAction.Builder.class)
@SuppressWarnings("immutables")
public interface ConnectorAction extends Action, WithId<ConnectorAction>, WithDependencies {

    @Override
    @Value.Default
    default String getActionType() {
        return Action.TYPE_CONNECTOR;
    }

    @Override
    default ConnectorAction withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    @Override
    ConnectorDescriptor getDescriptor();

    @Override
    default List<Dependency> getDependencies() {
        final String gav = getDescriptor().getCamelConnectorGAV();

        return StringUtils.isEmpty(gav) ? Collections.emptyList() : Collections.singletonList(Dependency.maven(gav));
    }

    class Builder extends ImmutableConnectorAction.Builder {
    }

    default ConnectorAction.Builder builder() {
        return new Builder().createFrom(this);
    }

    @Override
    default ConnectorAction withInputDataShape(final Optional<DataShape> inputDataShape) {
        return builder().descriptor(getDescriptor().builder().withInputDataShape(inputDataShape).build()).build();
    }

    @Override
    default ConnectorAction withOutputDataShape(final Optional<DataShape> outputDataShape) {
        return builder().descriptor(getDescriptor().builder().withOutputDataShape(outputDataShape).build()).build();
    }
}
