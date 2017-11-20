/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

/**
 * Identifies a resource managed by the system.
 */
@Value.Immutable
@JsonDeserialize(builder = ResourceIdentifier.Builder.class)
@SuppressWarnings("immutables")
public interface ResourceIdentifier extends WithId<ResourceIdentifier>, WithKind, Serializable {

    Optional<String> name();

    @Override
    default ResourceIdentifier withId(String id) {
        return new ResourceIdentifier.Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableResourceIdentifier.Builder {
        // allow access to ImmutableResourceIdentifier.Builder
    }
}
