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
package io.syndesis.server.cli.command.migrate;

import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.cache.CacheManager;
import io.syndesis.common.util.cache.LRUCacheManager;
import io.syndesis.server.dao.manager.DaoConfiguration;
import io.syndesis.server.jsondb.dao.JsonDbDao;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.runtime.DataSourceConfiguration;
import io.syndesis.server.runtime.DataStoreConfiguration;
import io.syndesis.server.runtime.DefaultMigrator;
import io.syndesis.server.runtime.SimpleEventBus;
import io.syndesis.server.runtime.StoredSettings;

import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Import({DataSourceAutoConfiguration.class, DaoConfiguration.class, DataStoreConfiguration.class, DataSourceConfiguration.class})
@Configuration
@ComponentScan(basePackageClasses = JsonDbDao.class)
public class MigrationConfiguration {

    @Value("${encrypt.key:}")
    private String encryptKey;

    @Bean
    public CacheManager cacheManager() {
        return new LRUCacheManager(0); // perform no caching
    }

    @Bean
    public EventBus eventBus() {
        return new SimpleEventBus();
    }

    @Bean
    public TextEncryptor getTextEncryptor() {
        return Encryptors.text(encryptKey, "deadbeef");
    }

    @Bean
    public Migrator migrator(final ResourceLoader resourceLoader) {
        return new DefaultMigrator(resourceLoader);
    }

    @Bean
    public StoredSettings storedSettings(final DBI dbi) {
        return new StoredSettings(dbi);
    }
}
