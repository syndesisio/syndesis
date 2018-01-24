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
package io.syndesis.rest.dblogging.jaxrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.core.Json;
import io.syndesis.core.KeyGenerator;
import io.syndesis.jsondb.GetOptions;
import io.syndesis.jsondb.JsonDB;
import io.syndesis.rest.dblogging.controller.LogsController;
import io.syndesis.rest.dblogging.jaxrs.model.Exchange;
import io.syndesis.rest.dblogging.jaxrs.model.ExchangeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.syndesis.rest.dblogging.jaxrs.JsonNodeSupport.fieldNames;
import static io.syndesis.rest.dblogging.jaxrs.JsonNodeSupport.getLong;
import static io.syndesis.rest.dblogging.jaxrs.JsonNodeSupport.getString;
import static io.syndesis.rest.dblogging.jaxrs.JsonNodeSupport.removeBoolean;
import static io.syndesis.rest.dblogging.jaxrs.JsonNodeSupport.removeLong;
import static io.syndesis.rest.dblogging.jaxrs.JsonNodeSupport.removeString;

/**
 * Provides a JAXR interface to query stored integration logging from the DB.
 */
@Component
public class LogResource {

    private static final Logger LOG = LoggerFactory.getLogger(LogsController.class);
    private static final Set<String> EVENT_FIELDS_SKIP_LIST = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("id", "at", "logts")));

    private final JsonDB jsondb;


    public LogResource(final JsonDB jsondb) {
        this.jsondb = jsondb;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{integrationId}")
    public List<Exchange> getLogs(
        @PathParam("integrationId") String integrationId,
        @QueryParam("from") String from,
        @QueryParam("limit") Integer requestedLimit
    ) throws IOException {

        String path = "/logs/exchanges/" + integrationId;

        int limit = 10;
        if( requestedLimit != null ) {
            limit = requestedLimit;
        }
        if( limit > 1000 ) {
            limit = 1000; // max out to 1000 per request.
        }

        GetOptions options = new GetOptions()
            .order(GetOptions.Order.DESC) // reverse the order since we want most recent exchanges first.
            .after(from).limit(limit); // allow paging

        byte[] data = jsondb.getAsByteArray(path, options);
        if( data == null )  {
            return new ArrayList<>();
        }

        return toAPIAPITxLogEntryList(Json.mapper().readTree(data));
    }

    private List<Exchange> toAPIAPITxLogEntryList(JsonNode from) {
        return toList(from, j-> {

            Exchange rc = new Exchange();

            rc.setId(removeString(j, "id"));
            rc.setAt(removeLong(j, "at"));
            rc.setFailed(removeBoolean(j, "failed"));
            rc.setPod(removeString(j, "pod"));
            rc.setStatus(removeString(j, "status"));
            rc.setVer(removeString(j, "ver"));
            rc.setLogts(removeString(j, "logts"));

            ObjectNode fromSteps = (ObjectNode) j.remove("steps");
            List<ExchangeStep> steps = toList(fromSteps, fromStepEvents -> {

                ExchangeStep toStep = new ExchangeStep();
                toStep.setId(removeString(fromStepEvents, "id"));
                fromStepEvents.remove("at");

                toStep.setEvents(toList(fromStepEvents, fromStepEvent -> {

                    Long at = getLong(fromStepEvent, "at");
                    if (at != null && toStep.getAt() == null) {
                        toStep.setAt(at);
                    }
                    Long duration = removeLong(fromStepEvent, "duration");
                    if (duration != null) {
                        toStep.setDuration(duration);
                    }
                    String failure = removeString(fromStepEvent, "failure");
                    if (failure != null) {
                        toStep.setFailure(failure);
                    }

                    String message = getString(fromStepEvent, "message");
                    if (message != null) {
                        toStep.addMessage(message);
                    }

                    // Should we skip adding this event?
                    if (EVENT_FIELDS_SKIP_LIST.equals(fieldNames(fromStepEvent))) {
                        return null;
                    }
                    return fromStepEvent;

                }));

                if( toStep.getMessages()!=null ) {
                    Collections.reverse(toStep.getMessages());
                }
                return toStep;
            });

            if( steps!=null ) {
                Collections.reverse(steps);
                rc.setSteps(steps);
            }

            if( j.size() > 0 ) {
                rc.setMetadata(j);
            }
            return rc;
        });
    }

    private static <T> List<T> toList(JsonNode map, Function<ObjectNode, T> converter) {
        if ( map == null ) {
            return null;
        }
        List<T> rc = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> i = map.fields();
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> entry = i.next();
            try {
                ObjectNode to = (ObjectNode) entry.getValue();
                to.put("id", entry.getKey());
                try {
                    to.put("at", KeyGenerator.getKeyTimeMillis(entry.getKey()));
                } catch (IOException e) {
                    // looks like bad id format, skip over it.
                }
                T apply = converter.apply(to);
                if( apply !=null ) {
                    rc.add(apply);
                }
            } catch (RuntimeException ignore) {
                // We could get stuff like class cast exceptions..
                LOG.debug("Could convert entry: {}", entry, ignore);
            }
        }
        return rc;
    }

}
