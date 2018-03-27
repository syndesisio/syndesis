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
package io.syndesis.common.model.bulletin;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.WithMetadata;
import org.immutables.value.Value;

import static io.syndesis.common.model.bulletin.LeveledMessage.Level.INFO;

/**
 * A message with an associated level.
 */
@Value.Immutable
@JsonDeserialize(builder = LeveledMessage.Builder.class)
@SuppressWarnings("immutables")
public interface LeveledMessage extends WithMetadata {

    enum Code {
        SYNDESIS000, // Generic message
        SYNDESIS001 // There are parameter updates for this connection
    }

    enum Level {
        INFO,
        WARN,
        ERROR
    }

    @Value.Default
    default Level getLevel() {
        return INFO;
    }

    @Value.Default
    default Code getCode() {
        return Code.SYNDESIS000;
    }

    Optional<String> getMessage();

    class Builder extends ImmutableLeveledMessage.Builder {
        // allow access to the immutable builder
    }

    static LeveledMessage of(Level level, String message) {
        return new Builder().level(level).message(message).build();
    }

}
