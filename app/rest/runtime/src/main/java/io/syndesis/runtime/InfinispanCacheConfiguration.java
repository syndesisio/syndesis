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

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfinispanCacheConfiguration {

    private static CacheManager cacheManager;
    private static EmbeddedCacheManager embeddedCacheManager;

    @Value("${cache.max.entries}")
    private int maxEntries;

    @Bean
    public EmbeddedCacheManager embeddedCacheManager() {
        if (embeddedCacheManager == null) {
            embeddedCacheManager = new DefaultCacheManager(
                    new GlobalConfigurationBuilder().nonClusteredDefault().globalJmxStatistics().enable()
                            .defaultCacheName("syndesis-cache").build(),
                    new ConfigurationBuilder().simpleCache(true).memory().evictionType(EvictionType.COUNT)
                            .size(maxEntries).jmxStatistics().enable().build());
        }
        return embeddedCacheManager;
    }

    @Bean
    public CacheManager cacheManager(final EmbeddedCacheManager nativeCacheManager) {
        if (cacheManager == null) {
            cacheManager = new SpringEmbeddedCacheManager(nativeCacheManager);
        }
        return cacheManager;
    }
}
