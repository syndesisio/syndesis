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

public class LRUSoftCacheTest {

    /**
     * This test requires a cap of 200MB on the Java heap to work (it's set on surefire).
     * Run it with: "-Xmx200m"
     */
    @Test
    public void testCacheEmptiedOnHeapFull() {
        int max = 150;
        int bufferSize = 4000000;
        // max * bufferSize should be way more than the heap size (~ 600MB)
        CacheManager manager = new LRUCacheManager(max);
        Cache<String, byte[]> cache = manager.getCache("cache", true);

        for (int i = 0; i < max; i++) {
            cache.put("" + i, new byte[bufferSize]);
        }
        Assertions.assertThat(cache.size()).isLessThan(max); // auto-purge before going oom
    }

}
