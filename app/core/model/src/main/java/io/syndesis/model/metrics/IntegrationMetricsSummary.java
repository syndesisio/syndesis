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
package io.syndesis.model.metrics;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.Kind;
import io.syndesis.model.WithId;

@Value.Immutable
@JsonDeserialize(builder = IntegrationMetricsSummary.Builder.class)
@SuppressWarnings("immutables")
public interface IntegrationMetricsSummary extends WithId<IntegrationMetricsSummary>, Serializable{

    @Override
    default Kind getKind() {
        return Kind.IntegrationMetricsSummary;
    }

    Optional<Long> getMessages();

    Optional<Long> getErrors();

    Optional<Date> getLastProcessed();

    Optional<Long> getUptime();

    class Builder extends ImmutableIntegrationMetricsSummary.Builder {
        // allow access to ImmutablIntegrationeMetricsSummary.Builder
    }
}
