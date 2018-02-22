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
package io.syndesis.model.buletin;

import java.util.Collections;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import io.syndesis.model.WithModificationTimestamps;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.validation.UniqueProperty;
import io.syndesis.model.validation.UniquenessRequired;

/**
 * A IntegrationBulletinBoard holds any notifications that should be displayed to the user
 * for a given integration.
 */
@Value.Immutable
@JsonDeserialize(builder = Integration.Builder.class)
@UniqueProperty(value = "name", groups = UniquenessRequired.class)
@SuppressWarnings("immutables")
public interface IntegrationBulletinBoard extends WithId<IntegrationBulletinBoard>, WithModificationTimestamps {

    @Override
    default Kind getKind() {
        return Kind.IntegrationBulletinBoard;
    }

    @Value.Default
    default List<LeveledMessage> getMessages() {
        return Collections.emptyList();
    }

    static IntegrationBulletinBoard of(String id, List<LeveledMessage> messages) {
        return new Builder().id(id).messages(messages).build();
    }

    class Builder extends ImmutableIntegrationBulletinBoard.Builder {
        // allow access to ImmutableIntegration.Builder
    }


}
