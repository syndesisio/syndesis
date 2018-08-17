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

import io.syndesis.common.util.KeyGenerator;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Offset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for KeyGenerator
 */
public class KeyGeneratorTest {

    @Before
    public void resetToDefaults() {
        KeyGenerator.clock = KeyGenerator.DEFAULT_CLOCK;
        KeyGenerator.randomnessByte = (byte) ThreadLocalRandom.current().nextInt();
    }

    /**
     * Generate keys in a tight loop and verify that we don't generate a dup key
     * since keys have a timestamp component to them.
     */
    @Test
    public void testCreateKey() {
        // Check to make sure we don't generate dup keys and that they are ordered properly
        String last = KeyGenerator.createKey();
        for (int i = 0; i < 1000000; i++) {
            final String lastKey = last;
            final String key = KeyGenerator.createKey();
            Assertions.assertThat(key).is(new Condition<>((other) -> lastKey.compareTo(other) < 0, "greater than " + lastKey));
            last = key;
        }
    }

    @Test
    public void testCreateKeyMultithreaded() {
        final int count = 100000;

        final Collection<Callable<String>> tasks = IntStream.range(0, count).boxed()
            .map(i -> (Callable<String>) () -> KeyGenerator.createKey()).collect(Collectors.toList());

        final ForkJoinPool pool = ForkJoinPool.commonPool();

        final List<Future<String>> results = pool.invokeAll(tasks);

        final Set<String> keys = results.stream().map(t -> {
            try {
                return t.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }).collect(Collectors.toSet());

        Assert.assertEquals("If " + count + " key generations are performed in parallel, it should yield " + count
            + " of distinct keys", count, keys.size());
    }


    @Test
    public void testGetKeyTimeMillis() throws IOException {
        long t1 = System.currentTimeMillis();
        String key = KeyGenerator.createKey();
        long t2 = KeyGenerator.getKeyTimeMillis(key);
        assertThat(t2).isCloseTo(t1, Offset.offset(500L));
    }

    @Test
    public void shouldGenerateKeysInChronologicalOrder() {
        for (int rnd = Byte.MIN_VALUE; rnd <= Byte.MAX_VALUE; rnd++) {
            KeyGenerator.randomnessByte = (byte) rnd;

            // 2018-08-17 12:38:48
            KeyGenerator.clock = () -> 1534509528000L;
            final String newer = KeyGenerator.createKey();

            // 2018-08-14 11:22:06
            KeyGenerator.clock = () -> 1534245726000L;
            final String older = KeyGenerator.createKey();

            assertThat(newer.compareTo(older)).isGreaterThan(0);
        }
    }
}
