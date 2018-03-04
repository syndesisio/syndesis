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
package io.syndesis.common.model.integration;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Scheduler.Builder.class)
@SuppressWarnings("immutables")
public interface Scheduler {

    enum Type {
        timer,
        cron
    }

    @Value.Default
    default Type getType() {
        return Type.timer;
    }

    /**
     * The schedule expression according to the type, i.e.
     *  - timer: "1s"
     *  - cron: "0 0/5 12-18 ? * MON-FRI"
     */
    String getExpression();

    @JsonIgnore
    default boolean isTimer() {
        return Type.timer == getType();
    }

    @JsonIgnore
    default boolean isCron() {
        return Type.cron == getType();
    }

    class Builder extends ImmutableScheduler.Builder {
        // allow access to ImmutableIntegration.Builder
    }
}
