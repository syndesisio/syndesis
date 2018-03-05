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
package io.syndesis.server.metrics.jsondb;


import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.common.util.Json;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public class JsonDBRawMetrics implements RawMetricsHandler {

    private static final String HISTORY = "HISTORY";
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDBRawMetrics.class);
    public static final TypeReference<Map<String, Boolean>> TYPE_REFERENCE = new TypeReference<Map<String, Boolean>>() {
    };
    public static final TypeReference<Map<String, RawMetrics>> VALUE_TYPE_REF = new TypeReference<Map<String, RawMetrics>>() {
    };
    private final JsonDB jsonDB;

    public JsonDBRawMetrics(JsonDB jsonDB) {
        this.jsonDB = jsonDB;
    }

    /**
     * Persists the latest metrics of a live pod to the database.
     */
    @Override
    public void persist(RawMetrics rawMetrics) {

        try {
            //persist the latest rawMetrics
            String path = path(rawMetrics.getIntegrationId(), rawMetrics.getPod());
            String json = Json.writer().writeValueAsString(rawMetrics);
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

    /**
     * Obtains all RawMetrics entries in the DB for the current integration
     *
     * @param integrationId - the integrationId for which we are obtaining the metrics
     * @return a Map containing all RawMetrics entries for the current integration,
     * the key is either HISTORY or the podName.
     * @throws IOException
     */
    @Override
    public Map<String,RawMetrics> getRawMetrics(String integrationId) throws IOException {
        //try to obtain all raw metrics in this integration
        Map<String,RawMetrics> metrics = new HashMap<>();
        String path = path(integrationId);
        String json = jsonDB.getAsString(path);
        if (json != null) {
            metrics = Json.reader().forType(VALUE_TYPE_REF).readValue(json);
        }
        return metrics;
    }

    /**
     * Adds the RawMetrics of dead pods to a special HISTORY bucket. Each
     * Integration should only have 1 HISTORY bucket and 1 bucket per live
     * pod.
     *
     * @param integrationId
     * @param metrics
     * @param livePodIds
     * @throws IOException
     */
    @Override
    public void curate(
            String integrationId,
            Map<String,RawMetrics> metrics,
            Set<String> livePodIds) throws IOException {

        for (Map.Entry<String, RawMetrics> entry : metrics.entrySet()) {
            String historyKey = HISTORY + entry.getValue().getVersion();
            if (! entry.getKey().contains(historyKey) && ! livePodIds.contains(entry.getKey())) { //dead pod check
                if (metrics.containsKey(historyKey)) {
                    //add to existing history
                    RawMetrics history = metrics.get(historyKey);
                    RawMetrics dead = entry.getValue();
                    Date lastProcessed = history.getLastProcessed().orElse(new Date(0)).after(dead.getLastProcessed().orElse(new Date(0)))
                            ? history.getLastProcessed().orElse(null) : dead.getLastProcessed().orElse(null);
                    RawMetrics updatedHistoryMetrics = new RawMetrics.Builder()
                            .integrationId(integrationId)
                            .version(dead.getVersion())
                            .pod(history.getIntegrationId() + ":" + dead.getPod())
                            .messages(history.getMessages() + dead.getMessages())
                            .errors(history.getErrors() + dead.getErrors())
                            .startDate(Optional.empty())
                            .resetDate(Optional.empty())
                            .lastProcessed(Optional.ofNullable(lastProcessed))
                            .build();
                    String json = Json.writer().writeValueAsString(updatedHistoryMetrics);
                    jsonDB.update(path(integrationId,historyKey), json);
                } else {
                    //create history bucket, first time we find a dead pod for this integration
                    String json = Json.writer().writeValueAsString(metrics.get(entry.getKey()));
                    jsonDB.set(path(integrationId,historyKey), json);
                }
                //delete the dead pod metrics since it has been added to the history
                jsonDB.delete(path(integrationId,entry.getKey()));
            }
        }
    }

    /**
     * If Integrations get deleted we should also delete their metrics
     *
     * @param activeIntegrationIds
     * @throws IOException
     * @throws JsonMappingException
     */
    @Override
    public void curate(Set<String> activeIntegrationIds) throws IOException, JsonMappingException {

        //1. Loop over all RawMetrics
        String json = jsonDB.getAsString(path(), new GetOptions().depth(1));
        if (json != null) {
            Map<String,Boolean> metricsMap = Json.reader().forType(TYPE_REFERENCE).readValue(json);
            Set<String> rawIntegrationIds = metricsMap.keySet();
            for (String rawIntId : rawIntegrationIds) {
                if (! activeIntegrationIds.contains(rawIntId)) {
                    jsonDB.delete(path(rawIntId));
                }
            }
        }
    }

    static String path(String integrationId,String podName) {
        return String.format("%s/integrations/%s/pods/%s", RawMetrics.class.getSimpleName(),
                integrationId, podName);
    }

    static String path(String integrationId) {
        return String.format("%s/integrations/%s/pods", RawMetrics.class.getSimpleName(),
                integrationId);
    }

    static String path() {
        return String.format("%s/integrations", RawMetrics.class.getSimpleName());
    }
}
