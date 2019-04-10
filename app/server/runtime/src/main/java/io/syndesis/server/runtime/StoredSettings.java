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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringColumnMapper;
import org.springframework.stereotype.Component;

@Component
public class StoredSettings {

    private final DBI dbi;

    public StoredSettings(DBI dbi) {
        this.dbi = dbi;
    }

    @PostConstruct
    public void createTables() {
        withTransaction(dbi -> {
            dbi.update("CREATE TABLE IF NOT EXISTS config (name VARCHAR PRIMARY KEY, value VARCHAR)");
        });
    }

    public void dropTables() {
        withTransaction(dbi -> {
            dbi.update("DROP TABLE config;");
        });
    }

    public String get(String name) {
        AtomicReference<String> rc = new AtomicReference<>();
        withTransaction(dbi -> {
            final String query = "SELECT value FROM config WHERE name = ?";
            rc.set(dbi.createQuery(query)
                .bind(0, name)
                .map(StringColumnMapper.INSTANCE).first());
        });
        return rc.get();
    }

    public void set(String name, String value) {
        withTransaction(dbi -> {
            dbi.update("DELETE FROM config WHERE name = ?", name);
            dbi.update("INSERT INTO config (name, value) values (?, ?)", name, value);
        });
    }

    private void withTransaction(Consumer<Handle> cb) {
        try (Handle h = dbi.open()) {
            try {
                h.begin();
                cb.accept(h);
                h.commit();
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException")RuntimeException e) {
                h.rollback();
                throw e;
            }
        }
    }
}
