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
package io.syndesis.common.model.buletin;

import static io.syndesis.common.model.buletin.LeveledMessage.Level.INFO;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A message with an associated level.
 */
@Value.Immutable
@JsonDeserialize(builder = LeveledMessage.Builder.class)
@SuppressWarnings("immutables")
public interface LeveledMessage {

    enum Level {
        INFO,
        WARN,
        ERROR
    }

    @Value.Default
    default Level getLevel() {
        return INFO;
    }

    String getMessage();

    class Builder extends ImmutableLeveledMessage.Builder {
        // allow access to the immutable builder
    }

    static LeveledMessage of(Level level, String message) {
        return new Builder().level(level).message(message).build();
    }

}
