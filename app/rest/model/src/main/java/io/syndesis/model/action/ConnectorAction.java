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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.Dependency;
import io.syndesis.model.WithDependencies;
import io.syndesis.model.WithId;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = ConnectorAction.Builder.class)
@SuppressWarnings("immutables")
@JsonIgnoreProperties(value = {"dependencies"}, allowGetters = true)
public interface ConnectorAction extends Action<ConnectorDescriptor>, WithId<ConnectorAction>, WithDependencies {

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
    default List<Dependency> getDependencies() {
        final String gav = getDescriptor().getCamelConnectorGAV();

        return Strings.isEmpty(gav)
            ? Collections.emptyList()
            : Collections.singletonList(Dependency.maven(gav));
    }

    class Builder extends ImmutableConnectorAction.Builder {
    }
}
