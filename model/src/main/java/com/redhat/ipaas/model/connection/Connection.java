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
package com.redhat.ipaas.model.connection;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.WithId;
import com.redhat.ipaas.model.WithName;
import com.redhat.ipaas.model.environment.Organization;
import com.redhat.ipaas.model.integration.Tag;
import org.immutables.value.Value;

/**
 * A connection is basically a Camel endpoint configuration (parameters)
 * and some metadata describing which parameters are available to configure.
 */
@Value.Immutable
@JsonDeserialize(builder = Connection.Builder.class)
public interface Connection extends WithId<Connection>, WithName, Serializable {

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

    String getDescription();

    String getPosition();

    Optional<List<Tag>> getTags();

    Optional<String> getUserId();

    @Override
    default Connection withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableConnection.Builder {
    }

}
