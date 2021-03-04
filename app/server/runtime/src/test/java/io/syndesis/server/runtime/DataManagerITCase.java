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
package io.syndesis.server.runtime;

import javax.persistence.EntityExistsException;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DataManagerITCase extends BaseITCase {

    @Test
    public void createDuplicateConnection() {
        final ListResult<Connection> connections = dataManager.fetchAll(Connection.class);
        final Connection c = connections.getItems().get(0);
        final Connection connection = new Connection.Builder()
            .id(c.getId())
            .name(c.getName())
            .build();

        assertThatExceptionOfType(EntityExistsException.class).isThrownBy(() -> dataManager.create(connection))
            .withMessage("There already exists a connection with id 5");
    }

    @Test
    public void createDuplicateName() {
        final ListResult<Connection> connections = dataManager.fetchAll(Connection.class);
        final Connection c = connections.getItems().get(0);
        final Connection connection = new Connection.Builder()
            .name(c.getName())
            .build();

        assertThatExceptionOfType(EntityExistsException.class).isThrownBy(() -> dataManager.create(connection))
            .withMessage("There already exists a Connection with name PostgresDB");
    }

}
