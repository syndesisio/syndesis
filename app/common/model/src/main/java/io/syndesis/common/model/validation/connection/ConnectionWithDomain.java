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
package io.syndesis.common.model.validation.connection;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.environment.Organization;
import io.syndesis.common.model.validation.TargetWithDomain;

public class ConnectionWithDomain extends TargetWithDomain<Connection> implements Connection {

    public ConnectionWithDomain(Connection target, Collection<Connection> domain) {
        super(target, domain);
    }

    @Override
    public Optional<Organization> getOrganization() {
        return getTarget().getOrganization();
    }

    @Override
    public Optional<String> getOrganizationId() {
        return getTarget().getOrganizationId();
    }

    @Override
    public Optional<Connector> getConnector() {
        return getTarget().getConnector();
    }

    @Override
    public String getConnectorId() {
        return getTarget().getConnectorId();
    }

    @Override
    public Map<String, String> getOptions() {
        return getTarget().getOptions();
    }

    @Override
    public String getIcon() {
        return getTarget().getIcon();
    }

    @Override
    public Optional<String> getDescription() {
        return getTarget().getDescription();
    }

    @Override
    public Optional<String> getUserId() {
        return getTarget().getUserId();
    }

    @Override
    public Optional<Date> getLastUpdated() {
        return getTarget().getLastUpdated();
    }

    @Override
    public Optional<Date> getCreatedDate() {
        return getTarget().getCreatedDate();
    }

    @Override
    public boolean isDerived() {
        return getTarget().isDerived();
    }

    @Override
    public int getUses() {
        return getTarget().getUses();
    }

    @Override
    public SortedSet<String> getTags() {
        return getTarget().getTags();
    }

    @Override
    public String getName() {
        return getTarget().getName();
    }

}
