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
package io.syndesis.rest.metrics.prometheus;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.syndesis.core.Json;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Query result from Prometheus HTTP API.
 * @author dhirajb
 */
@Value.Immutable
@JsonDeserialize(builder = QueryResult.Builder.class)
public interface QueryResult extends Serializable {

    static <T> Optional<T> getFirstValue(QueryResult response, Class<? extends T> clazz) {
        return response.getData().map(data -> {
            try {
                final List<Data.Result> result = data.getResult();
                return result.isEmpty() ? null : result.get(0).getTypedValue(clazz);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error parsing metric value " + e.getMessage());
            }
        });
    }

    static <T> Optional<Map<String, T>> getValueMap(final QueryResult response, final String label, final Class<? extends T> clazz) {
        return response.getData().map(data ->
                data.getResult().stream().collect(
                    Collectors.toMap(
                        r -> r.getLabel(label), r -> {
                            try {
                                return r.getTypedValue(clazz);
                            } catch (IOException e) {
                                throw new IllegalArgumentException("Error parsing metric value " + e.getMessage());
                            }
                        }
                    )
                )
            );
    }

    class Builder extends ImmutableQueryResult.Builder {
        // make ImmutableQueryResult.Builder accessible
    }

    @Value.Immutable
    @JsonDeserialize(builder = Data.Builder.class)
    interface Data {

        class Builder extends ImmutableData.Builder {
            // make ImmutableData.Builder accessible
        }

        @Value.Immutable
        @JsonDeserialize(builder = Result.Builder.class)
        interface Result {

            class Builder extends ImmutableResult.Builder {
                // make ImmutableResult.Builder accessible
            }

            Map<String, String> getMetric();

            List<Object> getValue();

            default String getLabel(String label) {
                return getMetric().get(label);
            }

            default String getStringValue() {
                return getValue().get(1).toString();
            }

            default <T> T getTypedValue(Class<? extends T> clazz) throws IOException {
                return Json.reader().forType(clazz).readValue(getStringValue());
            }
        }

        String getResultType();

        List<Result> getResult();
    }

    String getStatus();

    Optional<String> getErrorType();

    Optional<String> getError();

    Optional<Data> getData();

    @JsonIgnore
    default boolean isError() {
        return "error".equals(getStatus());
    }
}
