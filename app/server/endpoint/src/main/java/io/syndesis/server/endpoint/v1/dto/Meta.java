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
package io.syndesis.server.endpoint.v1.dto;

import java.util.Collections;
import java.util.Optional;

import io.syndesis.server.endpoint.v1.dto.Meta.Data.Type;
import io.syndesis.server.endpoint.v1.handler.exception.Errors;

import org.immutables.value.Value;

import static io.syndesis.server.endpoint.v1.dto.ImmutableData.builder;

public final class Meta<T> extends Mixed {

    private final Data data;

    private final T value;

    @Value.Immutable
    public interface Data {

        enum Type {
            DANGER, INFO, SUCCESS, WARNING
        }

        Optional<String> getMessage();

        Optional<Type> getType();
    }

    public Meta(final T value) {
        this(value, builder().build());
    }

    private Meta(final T value, final Data data) {
        super(value, Collections.singletonMap("_meta", data));

        this.value = value;
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public T getValue() {
        return value;
    }

    public static <V> Meta<V> verbatim(final V value) {
        return new Meta<>(value);
    }

    public static <V> Meta<V> withError(final V value, final Throwable throwable) {
        final String message = Errors.userMessageFrom(throwable);

        return new Meta<>(value, builder().type(Type.DANGER).message(message).build());
    }

}
