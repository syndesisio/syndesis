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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

class CollectionsUtilsTest {

    @Test
    void testAggregate() {
        Map<Integer, String> map1 = new HashMap<>();
        Map<Integer, String> map2 = new HashMap<>();

        map1.put(1, "one");
        map2.put(2, "two");
        map1.put(3, "three");

        Map<Integer, String> map3 = CollectionsUtils.aggregate(map1, map2);
        Assertions.assertThat(map3).containsAllEntriesOf(map1);
        Assertions.assertThat(map3).containsAllEntriesOf(map2);
    }

    @Test
    void testRemoveNullValues() {
        Map<Integer, String> map1 = new HashMap<>();

        map1.put(1, "one");
        map1.put(2, "two");
        map1.put(3, "three");
        map1.put(0, null);

        Map<Integer, String> res = CollectionsUtils.removeNullValues(map1);
        Assertions.assertThat(res).noneSatisfy(
            (i, v) -> Assertions.assertThat(v).isNull());
    }

    @Test
    void filter() {
        Map<Integer, String> map1 = new HashMap<>();

        map1.put(1, "one");
        map1.put(2, "two");
        map1.put(3, "three");
        map1.put(0, null);

        Predicate<Map.Entry<Integer, String>> predicate = integerStringEntry -> integerStringEntry.getKey() > 1;

        Stream<Map.Entry<Integer, String>> res = CollectionsUtils.filter(map1, predicate);
        Assertions.assertThat(res).allMatch(predicate);
    }

    @Test
    void testImmutableMapOf() {
        Map<Integer, String> res = CollectionsUtils.immutableMapOf(1, "one", 2, "two", 3, "three");
        Assertions.assertThat(res).hasSize(3);
    }
}
