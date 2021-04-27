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

package io.syndesis.test.itest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import io.syndesis.test.container.db.SyndesisDbContainer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.dsl.junit.jupiter.CitrusExtension;
import com.zaxxer.hikari.HikariDataSource;

@ExtendWith(CitrusExtension.class)
@Testcontainers
public abstract class SyndesisIntegrationTestSupport {

    private static final List<DisposableBean> DESTRUCTION_LIST = new CopyOnWriteArrayList<>();

    private static final ConcurrentMap<String, DataSource> SYNDESIS_DB_POOLS = new ConcurrentHashMap<>();

    @Container
    private static final SyndesisDbContainer syndesisDb = new SyndesisDbContainer();

    @BeforeEach
    void truncateSampleDbTables() throws SQLException {
        try (Connection connection = sampleDb().getConnection();
            ResultSet tables = connection.getMetaData().getTables(null, null, null, null);
            Statement statement = connection.createStatement()) {

            while (tables.next()) {
                if ("TABLE".equals(tables.getString("TABLE_TYPE"))) {
                    statement.execute("TRUNCATE TABLE " + tables.getString("TABLE_NAME"));
                }
            }
        }
    }

    @AfterAll
    public static void shutdown() {
        for (final DisposableBean toDestroy : DESTRUCTION_LIST) {
            try {
                toDestroy.destroy();
            } catch (final Exception ignore) {
            }
        }
    }

    private static DataSource syndesisDbDataSourceFor(final String username) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(syndesisDb.getJdbcUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(syndesisDb.getPassword());

        return dataSource;
    }

    protected static SyndesisDbContainer getSyndesisDb() {
        return syndesisDb;
    }

    protected static DataSource sampleDb() {
        return SYNDESIS_DB_POOLS.computeIfAbsent(syndesisDb.getUsername(), SyndesisIntegrationTestSupport::syndesisDbDataSourceFor);
    }

    protected static <T extends InitializingBean & DisposableBean> T startup(final T service) {
        try {
            service.afterPropertiesSet();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        DESTRUCTION_LIST.add(service);

        return service;
    }

    protected static DataSource syndesisDb() {
        return SYNDESIS_DB_POOLS.computeIfAbsent("syndesis", SyndesisIntegrationTestSupport::syndesisDbDataSourceFor);
    }
}
