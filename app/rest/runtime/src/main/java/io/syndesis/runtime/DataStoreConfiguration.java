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
package io.syndesis.runtime;

import io.syndesis.core.IndexedProperty;
import io.syndesis.jsondb.impl.Index;
import io.syndesis.jsondb.impl.SqlJsonDB;
import io.syndesis.model.Kind;
import io.syndesis.model.validation.UniqueProperty;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * Creates and configures the main datastore
 */
@Configuration
public class DataStoreConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaCheck.class);

    @Bean
    @Autowired
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public SqlJsonDB realTimeDB(DBI dbi) {

        ArrayList<Index> indexes = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            UniqueProperty uniqueProperty = kind.getModelClass().getAnnotation(UniqueProperty.class);
            if (uniqueProperty != null) {
                indexes.add(new Index("/" + kind.getModelName() + "s", uniqueProperty.value()));
            }

            IndexedProperty indexedProperty = kind.getModelClass().getAnnotation(IndexedProperty.class);
            if (indexedProperty != null) {
                indexes.add(new Index("/" + kind.getModelName() + "s", indexedProperty.value()));
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

}
