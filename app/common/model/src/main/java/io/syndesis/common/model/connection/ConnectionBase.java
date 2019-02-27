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

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.ToJson;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.WithTags;
import io.syndesis.common.model.WithUsage;
import io.syndesis.common.model.environment.Organization;

public interface ConnectionBase
    extends WithResourceId, WithTags, WithName, WithConfiguredProperties, WithMetadata, ToJson, WithUsage {

    Optional<Organization> getOrganization();

    Optional<String> getOrganizationId();

    Optional<Connector> getConnector();

    String getConnectorId();

    /**
     * Actual options how this connection is configured
     *
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
     * Ostensibly used to mark the {@link #getConfiguredProperties()} being set
     * by the OAuth flow so that the UI can alternate between full edit and
     * reconnect OAuth views.
     */
    boolean isDerived();

}
