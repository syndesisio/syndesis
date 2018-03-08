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

import io.syndesis.server.endpoint.v1.dto.MetaData.Type;
import io.syndesis.server.endpoint.v1.handler.exception.Errors;

import com.netflix.hystrix.HystrixInvokableInfo;

public final class Meta<T> extends Mixed {

    private final MetaData data;

    private final T value;

    public Meta(final T value) {
        this(value, ImmutableMetaData.builder().build());
    }

    private Meta(final T value, final MetaData data) {
        super(value, Collections.singletonMap("_meta", data));

        this.value = value;
        this.data = data;
    }

    public MetaData getData() {
        return data;
    }

    public T getValue() {
        return value;
    }

    public static <V> Meta<V> from(final V value, final HystrixInvokableInfo<V> metaInfo) {
        if (metaInfo.isFailedExecution()) {
            final Throwable executionException = metaInfo.getFailedExecutionException();
            return Meta.withError(value, executionException);
        } else if (metaInfo.isResponseTimedOut()) {
            final double timeout = metaInfo.getProperties().executionTimeoutInMilliseconds().get() / 1000.0;
            return Meta.withWarning(value, "The query could not be completed in " + timeout + " seconds.");
        } else if (metaInfo.isSuccessfulExecution()) {
            return Meta.verbatim(value);
        } else {
            return Meta.withWarning(value, "The query did not succeed");
        }
    }

    public static <V> Meta<V> verbatim(final V value) {
        return new Meta<>(value);
    }

    public static <V> Meta<V> withError(final V value, final Throwable throwable) {
        final String message = Errors.userMessageFrom(throwable);

        return new Meta<>(value, ImmutableMetaData.builder().type(Type.DANGER).message(message).build());
    }

    public static <V> Meta<V> withWarning(final V value, final String message) {
        return new Meta<>(value, ImmutableMetaData.builder().type(Type.WARNING).message(message).build());
    }

}
