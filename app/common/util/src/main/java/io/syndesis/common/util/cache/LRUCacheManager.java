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
package io.syndesis.common.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LRUCacheManager implements CacheManager {
    private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);

    private final ConcurrentMap<String, Cache<?, ?>> caches;
    private final int maxElements;

    public LRUCacheManager(final int maxElements) {
        this.maxElements = maxElements;
        caches = new ConcurrentHashMap<>();
    }

    @Override
    public void evictAll() {
        caches.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Cache<K, V> getCache(final String name, boolean soft) {
        Cache<K, V> cache = (Cache<K, V>) caches.computeIfAbsent(name, n -> this.newCache(n, soft));
        if ((soft && !(cache instanceof GuavaSoftCache)) || (!soft && (cache instanceof GuavaSoftCache))) {
            LOG.warn("Cache {} is being used in mixed 'soft' and 'hard' mode", name);
        }
        return cache;
    }

    private <K, V> Cache<K, V> newCache(@SuppressWarnings("PMD.UnusedFormalParameter") final String name, boolean soft) {
        if (soft) {
            return new GuavaSoftCache<>(maxElements);
        }
        return new LRUDefaultCache<>(maxElements);
    }
}
