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
package io.syndesis.server.update.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.bulletin.BulletinBoard;
import io.syndesis.server.dao.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public abstract class AbstractResourceUpdateHandler<T extends BulletinBoard<T>> implements ResourceUpdateHandler {
    private final Logger logger;
    private final AtomicBoolean running;
    private final DataManager dataManager;

    protected AbstractResourceUpdateHandler(DataManager dataManager) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.running = new AtomicBoolean(false);
        this.dataManager = dataManager;
    }

    @Override
    public void process(ChangeEvent event) {
        if (running.compareAndSet(false, true)) {
            try {
                compute(event).forEach(dataManager::set);
            } catch (@SuppressWarnings("PMD.AvoidCatchingThrowable") Throwable e) {
                logger.warn("Error handling update event {}", event, e);

                throw e;
            } finally {
                running.lazySet(false);
            }
        }
    }

    protected DataManager getDataManager() {
        return this.dataManager;
    }

    /**
     * Compute the bulletin boards for the given change.
     *
     * @param event the event.
     * @return a list of boards or an empty collection.
     */
    protected abstract List<T> compute(ChangeEvent event);
}
