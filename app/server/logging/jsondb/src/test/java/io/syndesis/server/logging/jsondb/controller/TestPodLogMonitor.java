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
package io.syndesis.server.logging.jsondb.controller;

import java.io.IOException;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric8.kubernetes.api.model.Pod;

public class TestPodLogMonitor extends PodLogMonitor {
    private final AtomicInteger counter;
    private final Phaser phaser;

    public TestPodLogMonitor(final ActivityTrackingController logsController, final Pod pod, final AtomicInteger counter, final Phaser phaser) {
        super(logsController, pod);
        this.counter = counter;
        this.phaser = phaser;
        state = new PodLogState();
    }

    @Override
    void processLine(String line) throws IOException {
        final int number = Integer.parseInt(line.trim());
        counter.set(number);
        if (number > 20) {
            phaser.arrive();
        } else if (number > 5) {
            phaser.arrive();
        }
    }

}
