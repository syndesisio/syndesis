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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class LRUCacheManagerTest {

    private boolean soft;


    public LRUCacheManagerTest(boolean soft) {
        this.soft = soft;
    }

    @Parameterized.Parameters(name = "LRUCacheManagerTest(soft={0})")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {Boolean.FALSE},
            {Boolean.TRUE}
        });
    }

    @Test
    public void testEviction() {
        CacheManager manager = new LRUCacheManager(2);
        Cache<String, Object> cache = manager.getCache("cache", this.soft);

        String one = "1";
        String two = "2";
        String three = "3";

        cache.put(one, one);
        cache.put(two, two);
        cache.put(three, three);

        Assertions.assertThat(cache.size()).isEqualTo(2);
        Assertions.assertThat(cache.get(one)).isNull();
        Assertions.assertThat(cache.get(two)).isNotNull();
        Assertions.assertThat(cache.get(three)).isNotNull();
    }

    @Test
    public void testIdentity() {
        CacheManager manager = new LRUCacheManager(2);
        Cache<String, String> cache1 = manager.getCache("cache", this.soft);
        Cache<String, String> cache2 = manager.getCache("cache", this.soft);
        // same cache, but warning printed
        Cache<String, String> cache3 = manager.getCache("cache", !this.soft);

        Assertions.assertThat(cache1).isEqualTo(cache2);
        Assertions.assertThat(cache1).isEqualTo(cache3);
    }
}
