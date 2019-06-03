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

    // max * bufferSize should be way more than the heap size (~ 600MB)
    private static final int MAX_ELEMENTS = 150;
    private static final int BUFFER_SIZE = 4000000;

    @Test
    public void testDefaultCacheEmptiedOnHeapFull() {
        CacheManager manager = new LRUCacheManager(MAX_ELEMENTS);
        Cache<String, byte[]> cache = manager.getCache("cache", true);
        doTest(cache);
    }

    @Test
    public void testSyndesisSoftCacheEmptiedOnHeapFull() {
        Cache<String, byte[]> cache = new LRUSoftCache<>(MAX_ELEMENTS);
        doTest(cache);
    }

    @Test
    public void testGuavaSoftCacheEmptiedOnHeapFull() {
        Cache<String, byte[]> cache = new GuavaSoftCache<>(MAX_ELEMENTS);
        doTest(cache);
    }

    /**
     * This test requires a cap of 200MB on the Java heap to work (it's set on surefire).
     * Run it with: "-Xmx200m"
     */
    private void doTest(Cache<String, byte[]> cache) {
        // Initial tests on the cache
        byte[] payload = new byte[]{1, 2, 3, 4, 5};
        cache.put("key", payload);
        Assertions.assertThat(cache.get("key")).isEqualTo(payload);
        Assertions.assertThat(cache.size()).isEqualTo(1);
        cache.remove("key");
        Assertions.assertThat(cache.get("key")).isNull();
        Assertions.assertThat(cache.size()).isEqualTo(0);

        // Test OOM behavior
        for (int i = 0; i < MAX_ELEMENTS; i++) {
            cache.put("" + i, new byte[BUFFER_SIZE]);
        }
        Assertions.assertThat(cache.size()).isLessThan(MAX_ELEMENTS); // auto-purge before going oom
    }

}
