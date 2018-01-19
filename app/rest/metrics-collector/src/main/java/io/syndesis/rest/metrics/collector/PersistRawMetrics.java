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
package io.syndesis.rest.metrics.collector;


import io.syndesis.jsondb.JsonDB;
import io.syndesis.core.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PersistRawMetrics implements RawMetricsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistRawMetrics.class);
    private final JsonDB jsonDB;

    public PersistRawMetrics(JsonDB jsonDB) {
        this.jsonDB = jsonDB;
    }

    /**
     * Persists the latest metrics of a live pod to the database.
     */
    @Override
    public void handle(RawMetrics rawMetrics) {

        try {
            //persist the latest rawMetrics
            String path = String.format("%s/integrations/%s/pods/%s",
                RawMetrics.class.getSimpleName(),rawMetrics.getIntegration(), rawMetrics.getPod());
            String json = Json.mapper().writeValueAsString(rawMetrics);
            if (jsonDB.exists(path)) {
                //only update if not the same (don't cause unnecessary and expensive writes)
                if (! jsonDB.getAsString(path).equals(json)) {
                    jsonDB.update(path, json);
                }
            } else {
                jsonDB.set(path, json);
            }

        } catch (JsonProcessingException e) {
            LOGGER.error("Error persisting metrics!", e);
        }
    }
}
