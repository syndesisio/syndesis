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
package io.syndesis.common.model.connection;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ToJson;
import io.syndesis.common.model.WithDependencies;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithIdVersioned;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithProperties;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.WithVersion;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.WithActions;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Connector.Builder.class)
@SuppressWarnings("immutables")
public interface Connector extends WithId<Connector>, WithIdVersioned<Connector>, WithVersion.AutoUpdatable, WithActions<ConnectorAction>, WithTags, WithName, WithProperties, WithDependencies, WithMetadata, ToJson, Serializable {

    class Builder extends ImmutableConnector.Builder implements WithPropertiesBuilder<Builder> {
        public Builder putOrRemoveConfiguredPropertyTaggedWith(final String tag, final String value) {
            return putOrRemoveConfiguredPropertyTaggedWith(this, tag, value);
        }
    }

    default Builder builder() {
        return new Builder().createFrom(this);
    }

    @Override
    default Connector withVersion(int version) {
        return new Builder().createFrom(this).version(version).build();
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

    @Value.Default
    default List<String> getConnectorCustomizers() {
        return Collections.emptyList();
    }

    /**
     * Provides a summary of the connector's actions
     * <p>
     * Note:
     * Excluded from {@link #hashCode()} and {@link #equals(Object)}
     *
     * @return the summary of the connector's actions
     */
    @Value.Auxiliary
    Optional<ActionsSummary> getActionsSummary();

    /**
     * Provide number of connections using this connector
     * <p>
     * Note:
     * Excluded from {@link #hashCode()} and {@link #equals(Object)}
     *
     * @return count of integrations
     */
    @Value.Auxiliary
    OptionalInt getUses();

    default Optional<String> propertyTaggedWith(final String tag) {
        return propertyTaggedWith(getConfiguredProperties(), tag);
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
     * @param another a {@link Connector} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    default boolean equivalent(Connector another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        List<ConnectorAction> myActions = getActions();
        if (myActions == null) {
            if (another.getActions() != null) {
                return false;
            }
        } else {
            for (ConnectorAction myAction : myActions) {
                ConnectorAction anotherAction = another.findActionById(myAction.getId().get()).orElse(null);
                if (! myAction.equivalent(anotherAction)) {
                    return false;
                }
            }
        }

        return Objects.equals(getConnectorGroup(), another.getConnectorGroup())
                        && Objects.equals(getConnectorGroupId(), another.getConnectorGroupId())
                        && Objects.equals(getDescription(), another.getDescription())
                        && Objects.equals(getIcon(), another.getIcon())
                        && getKind().equals(another.getKind())
                        && Objects.equals(getComponentScheme(), another.getComponentScheme())
                        && Objects.equals(getConnectorFactory(), another.getConnectorFactory())
                        && getConnectorCustomizers().equals(another.getConnectorCustomizers())
                        && Objects.equals(getId(), another.getId())
                        && getTags().equals(another.getTags())
                        && Objects.equals(getName(), another.getName())
                        && getProperties().equals(another.getProperties())
                        && getConfiguredProperties().equals(another.getConfiguredProperties())
                        && getDependencies().equals(another.getDependencies())
                        && getMetadata().equals(another.getMetadata());
    }
}
