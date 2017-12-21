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
package io.syndesis.model.connection;

import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.WithDependencies;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import io.syndesis.model.WithProperties;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.WithActions;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Connector.Builder.class)
@SuppressWarnings("immutables")
@JsonIgnoreProperties(value = {"dependencies"}, allowGetters = true)
public interface Connector extends WithId<Connector>, WithActions<ConnectorAction>, WithName, WithProperties, WithDependencies, Serializable {

    class Builder extends ImmutableConnector.Builder implements WithPropertiesBuilder<Builder> {
        public Builder putOrRemoveConfiguredPropertyTaggedWith(final String tag, final String value) {
            return putOrRemoveConfiguredPropertyTaggedWith(this, tag, value);
        }
    }

    default Optional<ConnectorAction> actionById(final String id) {
        return getActions().stream().filter(a -> a.idEquals(id)).findFirst();
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }

    Optional<ConnectorGroup> getConnectorGroup();

    Optional<String> getConnectorGroupId();

    String getDescription();

    String getIcon();

    @Override
    @Value.Default
    default Kind getKind() {
        return Kind.Connector;
    }

    // This is set to optional for backward compatibility with camel style connectors
    Optional<String> getComponentScheme();

    Optional<String> getConnectorFactory();

    Optional<ConnectorSummary> getSummary();

    OptionalInt getUses();

    default Optional<String> propertyTaggedWith(final String tag) {
        return propertyTaggedWith(getConfiguredProperties(), tag);
    }
}
