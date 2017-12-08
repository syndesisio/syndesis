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
package io.syndesis.inspector;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.rules.ExternalResource;

public class InfinispanCache extends ExternalResource {

    private CacheContainer caches;

    @Override
    protected void before() throws Throwable {
        EmbeddedCacheManager manager = new DefaultCacheManager(
            new GlobalConfigurationBuilder().nonClusteredDefault().build(),
            new ConfigurationBuilder()
            .memory().evictionType(EvictionType.COUNT).size(100)
            .build()
        );
        caches = manager;
    }

    @Override
    protected void after() {
        caches.stop();
    }

    public CacheContainer getCaches() {
        return caches;
    }
}
