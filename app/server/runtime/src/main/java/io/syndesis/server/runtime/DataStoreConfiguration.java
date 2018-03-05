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

import io.syndesis.common.util.IndexedProperty;
import io.syndesis.server.jsondb.impl.Index;
import io.syndesis.server.jsondb.impl.SqlJsonDB;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.validation.UniqueProperty;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Creates and configures the main datastore
 */
@Configuration
public class DataStoreConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(Migrations.class);

    @Bean
    @Autowired
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public SqlJsonDB jsonDB(DBI dbi, Optional<List<Index>> beanIndexes) {

        ArrayList<Index> indexes = new ArrayList<>();
        if(beanIndexes.isPresent()) {
            indexes.addAll(beanIndexes.get());
        }

        for (Kind kind : Kind.values()) {
            addIndex(indexes, kind, kind.getModelClass().getAnnotation(UniqueProperty.class));
            UniqueProperty.Multiple ump = kind.getModelClass().getAnnotation(UniqueProperty.Multiple.class);
            if (ump != null) {
                for (UniqueProperty p : ump.value()) {
                    addIndex(indexes, kind, p);
                }
            }

            addIndex(indexes, kind, kind.getModelClass().getAnnotation(IndexedProperty.class));
            IndexedProperty.Multiple imp = kind.getModelClass().getAnnotation(IndexedProperty.Multiple.class);
            if (imp != null) {
                for (IndexedProperty p : imp.value()) {
                    addIndex(indexes, kind, p);
                }
            }
        }

        SqlJsonDB jsondb = new SqlJsonDB(dbi, null, indexes);
        try {
            jsondb.createTables();
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ignore) {
            LOG.debug("Could not create tables", ignore);
        }
        return jsondb;
    }

    private void addIndex(List<Index> indexes, Kind kind, IndexedProperty indexedProperty) {
        if (indexedProperty != null) {
            indexes.add(new Index("/" + kind.getModelName() + "s", indexedProperty.value()));
        }
    }

    private void addIndex(List<Index> indexes, Kind kind, UniqueProperty p) {
        if (p != null) {
            indexes.add(new Index("/" + kind.getModelName() + "s", p.value()));
        }
    }

}
