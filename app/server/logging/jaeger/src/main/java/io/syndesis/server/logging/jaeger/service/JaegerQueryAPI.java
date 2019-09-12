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
package io.syndesis.server.logging.jaeger.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.SuppressFBWarnings;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Interface to Jaeger's Query API
 */
public class JaegerQueryAPI {

    private final WebTarget api;

    @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    static class JsonBase {
        @Override
        public String toString() {
            try {
                return Json.writer().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e); // NOPMD
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Traces extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public ArrayList<Trace> data;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Trace extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String traceID;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public ArrayList<Span> spans;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public HashMap<String, JaegerProcess> processes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Span extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String traceID;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String spanID;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public byte flags;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String operationName;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public ArrayList<Reference> references;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public long startTime;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public long duration;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public ArrayList<Tag> tags;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        @SuppressWarnings("PMD.ProperLogger")
        public ArrayList<Log> logs;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String processID;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public Object warnings;

        public <T> T findTag(String key, Class<T> type) {
            return JaegerQueryAPI.findTag(tags, key, type);
        }

        public <T> List<T> findTags(String key, Class<T> type) {
            return JaegerQueryAPI.findTags(tags, key, type);
        }

        public List<String> findLogs(String key) {
            return logs.stream()
                .flatMap(log -> log.findFields(key, String.class).stream())
                .collect(Collectors.toList());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Reference extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String refType;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String traceID;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String spanID;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Tag extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String key;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String type;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public Object value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Log extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public long timestamp;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public ArrayList<Tag> fields;

        public <T> T findField(String key, Class<T> type) {
            return JaegerQueryAPI.findTag(fields, key, type);
        }

        public <T> List<T> findFields(String key, Class<T> type) {
            return JaegerQueryAPI.findTags(fields, key, type);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class JaegerProcess extends JsonBase {
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public String serviceName;
        @SuppressFBWarnings("NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
        public ArrayList<Tag> tags;

        public <T> T findTag(String key, Class<T> type) {
            return JaegerQueryAPI.findTag(tags, key, type);
        }

        public <T> List<T> findTags(String key, Class<T> type) {
            return JaegerQueryAPI.findTags(tags, key, type);
        }
    }

    public JaegerQueryAPI(String jaegerApiURL) {
        Client client = ClientBuilder.newClient();
        api = client.target(jaegerApiURL);
    }

    private static <T> T findTag(ArrayList<Tag> tags, String key, Class<T> type) {
        return tags.stream().filter(x -> x.key.equals(key)).map(x -> type.cast(x.value)).findFirst().orElse(null);
    }

    private static <T> List<T> findTags(ArrayList<Tag> tags, String key, Class<T> type) {
        return tags.stream().filter(x -> x.key.equals(key)).map(x -> type.cast(x.value)).collect(Collectors.toList());
    }


    public ArrayList<Trace> tracesForService(String service, int lookbackDays, int limit) {

        long now = System.currentTimeMillis();
        long start = now - TimeUnit.DAYS.toMillis(lookbackDays);
        Response response = api.path("traces")
            .queryParam("end", now * 1009) // in ns
            .queryParam("start", start * 1000) // in ns
            .queryParam("limit", limit)
            .queryParam("lookback", lookbackDays + "d")
            .queryParam("service", service)
            .request(MediaType.APPLICATION_JSON)
            .get();

        Response.StatusType status = response.getStatusInfo();
        if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new WebApplicationException("HTTP  " + status.getStatusCode() + ": " + response.readEntity(String.class), response);
        }

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || !contentType.startsWith(MediaType.APPLICATION_JSON)) {
                throw new WebApplicationException("Got unexpected content type: " + contentType + " with body: " + response.readEntity(String.class));
            }
            return response.readEntity(Traces.class).data;
// Use the following commented code instead if you want to peek at what the query API is returning...
//            byte[] bytes = response.readEntity(byte[].class);
//            String data = new String(bytes);
//            System.out.println(data);
//            Traces o = null;
//            try {
//                o = Json.reader().forType(Traces.class).readValue(data);
//            } catch (IOException e) {
//                throw new WebApplicationException(e);
//            }
//            return o.data;
        }
        return new ArrayList<>();
    }

}
