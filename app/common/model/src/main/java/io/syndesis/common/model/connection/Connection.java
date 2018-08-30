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

import java.util.Objects;
import java.util.Optional;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.UniqueProperty;
import io.syndesis.common.model.validation.UniquenessRequired;
import io.syndesis.common.util.IndexedProperty;
import org.immutables.value.Value;

/**
 * A connection is basically a Camel endpoint configuration (parameters) and
 * some metadata describing which parameters are available to configure.
 */
@Value.Immutable
@JsonDeserialize(builder = Connection.Builder.class)
@UniqueProperty(value = "name", groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
@IndexedProperty("connectorId")
public interface Connection extends WithId<Connection>, ConnectionBase {

    @Override
    default Kind getKind() {
        return Kind.Connection;
    }

    class Builder extends ImmutableConnection.Builder {
    }

    default Builder builder() {
        return new Builder().createFrom(this);
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
     * @param another a {@link Connection} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    default boolean equivalent(Connection another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        Connector myConnector = getConnector().orElse(null);
        Connector anotherConnector = another.getConnector().orElse(null);

        if (myConnector == null) {
            if (anotherConnector != null) {
                return false;
            }
        } else if (! myConnector.equivalent(anotherConnector)) {
            return false;
        }

        return Objects.equals(getId(), another.getId())
            && Objects.equals(getOrganization(), another.getOrganization())
            && Objects.equals(getOrganizationId(), another.getOrganizationId())
            && Objects.equals(getConnectorId(), another.getConnectorId())
            && getOptions().equals(another.getOptions())
            && Objects.equals(getIcon(), another.getIcon())
            && Objects.equals(getDescription(), another.getDescription())
            && Objects.equals(getUserId(), another.getUserId())
            && isDerived() == another.isDerived()
            && getTags().equals(another.getTags())
            && Objects.equals(getName(), another.getName())
            && getConfiguredProperties().equals(another.getConfiguredProperties());
    }
}
