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
package io.syndesis.runtime;

import io.syndesis.jsondb.impl.SqlJsonDB;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and configures the main datastore
 */
@Configuration
public class DataStoreConfiguration {

    @Bean
    @Autowired
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public SqlJsonDB realTimeDB(DBI dbi) {
        SqlJsonDB jsondb = new SqlJsonDB(dbi, null);
        try {
            jsondb.createTables();
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ignore) {
        }
        return jsondb;
    }

}
