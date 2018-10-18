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
package io.syndesis.server.api.generator;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.util.Optionals;

import java.util.Objects;
import java.util.Optional;

/**
 * Collects template data needed by the API generator for provided APIs.
 */
public final class ProvidedApiTemplate {

    private final Connection connection;

    private final String startActionId;

    private final String endActionId;

    public ProvidedApiTemplate(final Connection connection, final String startActionId, final String endActionId) {
        this.connection = Objects.requireNonNull(connection, "connection cannot be null");
        this.startActionId = Objects.requireNonNull(startActionId, "startActionId cannot be null");
        this.endActionId = Objects.requireNonNull(endActionId, "endActionId cannot be null");
    }

    public Connection getConnection() {
        return connection;
    }

    public String getStartActionId() {
        return startActionId;
    }

    public Optional<? extends Action> getStartAction() {
        return getAction(this.startActionId);
    }

    public String getEndActionId() {
        return endActionId;
    }

    public Optional<? extends Action> getEndAction() {
        return getAction(this.endActionId);
    }

    Optional<? extends Action> getAction(final String id) {
        return Optionals.asStream(this.connection.getConnector())
            .flatMap(c -> c.getActions().stream())
            .filter(a -> a.getId().equals(Optional.of(id)))
            .findFirst();
    }
}
