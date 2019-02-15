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
package io.syndesis.server.metrics.prometheus;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Query result from Prometheus HTTP API.
 * @author dhirajb
 */
@Value.Immutable
@SuppressWarnings("immutables")
@JsonDeserialize(builder = QueryResult.Builder.class)
public interface QueryResult extends Serializable {

    Logger LOG = LoggerFactory.getLogger(QueryResult.class);

    static <T> Optional<T> getFirstValue(QueryResult response, Class<? extends T> clazz) {
        return response.getData().map(data -> {
            final List<Data.Result> result = data.getResult();
            return result.isEmpty() ? null : result.get(0).getTypedValue(clazz).orElse(null);
        });
    }

    static <T> Optional<T> getMergedValue(QueryResult response, Class<? extends T> clazz, BinaryOperator<T> mergeFunction) {
        return response.getData().flatMap(data -> data.getResult().stream()
            .<T>map(result -> result.getTypedValue(clazz).orElse(null))
            .reduce(mergeFunction)
        );
    }

    static <T> Map<String, ? extends T> getValueMap(final QueryResult response, final String label, final Class<? extends T> clazz) {
        return response.getData().map(data ->
                data.getResult().stream().collect(
                    Collectors.toMap(r -> r.getLabel(label), r -> r.getTypedValue(clazz).orElse(null))
                )
            ).orElse(Collections.emptyMap());
    }

    static <T> Map<String, T> getValueMap(final QueryResult response, final String label, final Class<? extends T> clazz,
                                          BinaryOperator<T> mergeFunction) {
        return response.getData().map(data ->
                data.getResult().stream().collect(
                    Collectors.toMap(r -> r.getLabel(label), r -> r.getTypedValue(clazz).orElse(null), mergeFunction)
                )
            ).orElse(Collections.emptyMap());
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
                // needs the quotes for types like Date
                return String.format("\"%s\"", getValue().get(1));
            }

            default <T> Optional<T> getTypedValue(Class<? extends T> clazz) {
                final String value = getStringValue();
                try {
                    return Optional.of(Utils.getObjectReader().forType(clazz).readValue(value));
                } catch (IOException e) {
                    LOG.warn("Returning empty value due to error parsing {} as {}: {}", value, clazz.getName(), e.getMessage());
                }
                return Optional.empty();
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
