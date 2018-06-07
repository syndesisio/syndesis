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
package io.syndesis.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CollectionsUtils {
    private CollectionsUtils() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <K, V> Map<K, V> aggregate(Map<K, V>... maps) {
        return Stream.of(maps)
            .flatMap(map -> map.entrySet().stream())
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K, V> Map<K, V> removeNullValues(Map<K, V> source) {
        return removeNullValues(source, HashMap::new);
    }

    public static <K, V> Map<K, V> removeNullValues(Map<K, V> source, Supplier<Map<K, V>> supplier) {
        final Map<K, V> answer = supplier.get();
        answer.putAll(source);
        answer.values().removeIf(Objects::isNull);

        return answer;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <C extends Collection<T>, T> C aggregate(Supplier<C> collectionFactory, Collection<T>... collections) {
        final C result = collectionFactory.get();

        for (Collection<T> collection: collections) {
            result.addAll(collection);
        }

        return result;
    }

    public static <K, V> Stream<Map.Entry<K, V>> filter(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        return map.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            .filter(predicate);
    }


    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Supplier<Map<K, V>> creator, K key, V value, Object... keyVals) {
        Map<K, V> map = creator.get();
        map.put(key, value);

        for (int i = 0; i < keyVals.length; i += 2) {
            map.put(
                (K) keyVals[i],
                (V) keyVals[i + 1]
            );
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> immutableMapOf(Supplier<Map<K, V>> creator, K key, V value, Object... keyVals) {
        return Collections.unmodifiableMap(
            mapOf(creator, key, value, keyVals)
        );
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(K key, V value, Object... keyVals) {
        return mapOf(HashMap::new, key, value, keyVals);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> immutableMapOf(K key, V value, Object... keyVals) {
        return Collections.unmodifiableMap(
            mapOf(HashMap::new, key, value, keyVals)
        );
    }
}
