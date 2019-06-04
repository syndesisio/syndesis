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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A special cache using soft references to hold data, to allow the garbage collector to reclaim space if the application
 * is suffering for high memory usage.
 */
public class LRUSoftCache<K, V> implements Cache<K, V> {

    private Cache<K, Reference<V>> cache;

    public LRUSoftCache(int maxElements) {
        this.cache = new LRUDefaultCache<>(maxElements);
    }

    @Override
    public V get(K key) {
        Reference<V> ref = cache.get(key);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    @Override
    public Set<K> keySet() {
        return cache.keySet()
            .stream()
            .filter(k -> {
                Reference<V> res = cache.get(k);
                V val = res != null ? res.get() : null;
                return val != null;
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        return cache.values()
            .stream()
            .map(Reference::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, new SoftReference<>(value));
    }

    @Override
    public V remove(K key) {
        Reference<V> value = cache.remove(key);
        return value != null ? value.get() : null;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public String toString() {
        return "LRUSoftCache{" +
            "cache=" + cache +
            '}';
    }
}
