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
package io.syndesis.model.connection;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import io.syndesis.model.WithTags;
import io.syndesis.model.environment.Organization;
import io.syndesis.model.validation.UniqueProperty;
import io.syndesis.model.validation.UniquenessRequired;

import org.immutables.value.Value;

/**
 * A connection is basically a Camel endpoint configuration (parameters) and
 * some metadata describing which parameters are available to configure.
 */
@Value.Immutable
@JsonDeserialize(builder = Connection.Builder.class)
@UniqueProperty(value = "name", groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
public interface Connection extends WithId<Connection>, WithTags, WithName, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Connection;
    }

    Optional<Organization> getOrganization();

    Optional<String> getOrganizationId();

    Optional<Connector> getConnector();

    Optional<String> getConnectorId();

    Map<String, String> getConfiguredProperties();

    /**
     * Actual options how this connection is configured
     * @return list of options
     */
    Map<String, String> getOptions();

    String getIcon();

    Optional<String> getDescription();

    Optional<String> getUserId();

    Optional<Date> getLastUpdated();

    Optional<Date> getCreatedDate();

    /**
     * A flag denoting that the some of connection properties were derived.
     * Ostensibly used to mark the {@link #getConfiguredProperties()} being
     * set by the OAuth flow so that the UI can alternate between full edit
     * and reconnect OAuth views.
     */
    boolean isDerived();

    class Builder extends ImmutableConnection.Builder {
    }

}
