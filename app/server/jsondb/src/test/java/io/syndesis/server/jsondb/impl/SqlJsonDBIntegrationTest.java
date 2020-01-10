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
package io.syndesis.server.jsondb.impl;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.SyndesisServerException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.DBI;

public class SqlJsonDBIntegrationTest {

    private static final String JSON = "{\"a\": \"b\"}";

    private EventBus bus;

    private SqlJsonDB jsonDB;

    @BeforeAll
    public void prepare() {
        bus = mock(EventBus.class);

        final DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:", "sa", "password");
        final DBI dbi = new DBI(dataSource);
        jsonDB = new SqlJsonDB(dbi, bus);
        jsonDB.createTables();
    }

    @Test
    public void shouldCommitTransactionForExecutionsWithoutIssues() {
        jsonDB.withGlobalTransaction(checkpointed -> {
            checkpointed.set("initial", JSON);
        });

        assertThat(jsonDB.getAsString("initial")).isNotNull();
        verify(bus).broadcast("jsondb-updated", "/initial");
        verifyNoMoreInteractions(bus);
    }

    @Test
    public void shouldNotCommitWhenIssuesArise() {
        assertThatExceptionOfType(SyndesisServerException.class).isThrownBy(() -> jsonDB.withGlobalTransaction(checkpointed -> {
            checkpointed.set("failed", JSON);

            assertThat(checkpointed.getAsString("failed")).isNotNull();

            throw new SyndesisServerException("expected");
        }))
            .withMessage("expected");

        assertThat(jsonDB.getAsString("failed")).isNull();
        verifyNoMoreInteractions(bus);
    }
}
