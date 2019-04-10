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
package io.syndesis.server.logging.jsondb.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import io.syndesis.common.util.Json;
import io.syndesis.server.endpoint.v1.handler.activity.Activity;
import io.syndesis.server.endpoint.v1.handler.activity.ActivityStep;
import io.syndesis.server.endpoint.v1.handler.activity.ActivityTrackingService;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.logging.jsondb.controller.ActivityTrackingController;

/**
 * Implements a dblogging service for the Activity JAXRS service.
 */
@Component
@ConditionalOnProperty(value = "endpoints.dblogging.enabled", havingValue = "true", matchIfMissing = true)
public class DBActivityTrackingService implements ActivityTrackingService {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityTrackingController.class);
    private final JsonDB jsondb;

    public DBActivityTrackingService(final JsonDB jsondb) {
        this.jsondb = jsondb;
    }

    @Override
    public List<Activity> getActivities(String integrationId, String from, Integer requestedLimit) throws IOException {

        String path = "/activity/exchanges/" + integrationId;

        int limit = 10;
        if( requestedLimit != null ) {
            limit = requestedLimit;
        }
        if( limit > 1000 ) {
            limit = 1000; // max out to 1000 per request.
        }

        GetOptions options = new GetOptions()
            .order(GetOptions.Order.DESC) // reverse the order since we want most recent exchanges first.
            .startAfter(from).limitToFirst(limit); // allow paging

        byte[] data = jsondb.getAsByteArray(path, options);
        if( data == null )  {
            return new ArrayList<>();
        }

        JsonNode map = Json.reader().readTree(new ByteArrayInputStream(data));
        List<Activity> rc = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> i = map.fields();
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> entry = i.next();
            try {
                String value = entry.getValue().textValue();
                Activity activity = Json.reader().forType(Activity.class).readValue(value);
                if (activity.getSteps() == null){
                    activity.setSteps(new ArrayList<ActivityStep>());
                }
                rc.add(activity);
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException ignored) {
                // We could get stuff like class cast exceptions..
                LOG.debug("Could convert entry: {}", entry, ignored);
            }
        }
        return rc;
    }

}
