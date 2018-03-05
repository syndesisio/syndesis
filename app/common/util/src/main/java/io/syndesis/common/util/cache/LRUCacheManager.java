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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LRUCacheManager implements CacheManager {
    private final int maxElements;
    private final ConcurrentMap<String, Map<?, ?>> maps;

    public LRUCacheManager(int maxElements) {
        this.maxElements = maxElements;
        this.maps = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> getCache(String name) {
        return Map.class.cast(maps.computeIfAbsent(name, this::newCache));
    }

    private <K, V> Map<K, V> newCache(@SuppressWarnings("PMD.UnusedFormalParameter") String name) {
        return Collections.synchronizedMap(new LinkedHashMap<K, V>() {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
                return size() > maxElements;
            }
        });
    }
}
