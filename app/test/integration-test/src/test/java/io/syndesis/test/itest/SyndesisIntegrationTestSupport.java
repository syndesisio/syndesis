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

import javax.sql.DataSource;

import com.consol.citrus.dsl.junit.JUnit4CitrusTest;
import io.syndesis.test.container.db.SyndesisDbContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SyndesisIntegrationTestSupport.EndpointConfig.class)
public abstract class SyndesisIntegrationTestSupport extends JUnit4CitrusTest {

    private static SyndesisDbContainer syndesisDb;

    static {
        syndesisDb = new SyndesisDbContainer();
        syndesisDb.start();
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public DataSource sampleDb() {
            return new SingleConnectionDataSource(syndesisDb.getJdbcUrl(),
                                                    syndesisDb.getUsername(),
                                                    syndesisDb.getPassword(), true);
        }

        @Bean
        public DataSource syndesisDb() {
            return new SingleConnectionDataSource(syndesisDb.getJdbcUrl(),
                                                    "syndesis",
                                                    syndesisDb.getPassword(), true);
        }
    }

    protected static SyndesisDbContainer getSyndesisDb() {
        return syndesisDb;
    }
}
