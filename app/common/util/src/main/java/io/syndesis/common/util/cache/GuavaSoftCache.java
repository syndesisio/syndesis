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

import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.Set;

/**
 * A Guava based implementation of a soft cache, to allow the garbage collector to reclaim space if the application
 * is suffering for high memory usage.
 */
public class GuavaSoftCache<K, V> implements Cache<K, V> {

    private com.google.common.cache.Cache<K, V> cache;

    public GuavaSoftCache(int maxElements) {
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(maxElements)
            .softValues()
            .build();
    }

    @Override
    public V get(K key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public Set<K> keySet() {
        return this.cache.asMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return this.cache.asMap().values();
    }

    @Override
    public void put(K key, V value) {
        this.cache.put(key, value);
    }

    @Override
    public V remove(K key) {
        V old = this.cache.getIfPresent(key);
        this.cache.invalidate(key);
        return old;
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    @Override
    public int size() {
        return (int) this.cache.size();
    }

    @Override
    public String toString() {
        return "GuavaSoftCache{" +
            "cache=" + cache +
            '}';
    }
}
