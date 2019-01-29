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
package io.syndesis.common.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

import io.syndesis.common.util.json.StringTrimmingConverter;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Identifies a resource managed by the system.
 */
@Value.Immutable
@JsonDeserialize(builder = ResourceIdentifier.Builder.class)
@SuppressWarnings("immutables")
public interface ResourceIdentifier extends WithId<ResourceIdentifier>, Serializable {

    Comparator<ResourceIdentifier> LATEST_VERSION = Comparator.comparing(r -> r.getVersion().orElse(0));

    /**
     * Refers to the "target" resource kind.
     */
    @JsonDeserialize(contentConverter = StringTrimmingConverter.class)
    @JsonIgnore(false)
    @Override
    Kind getKind();

    Optional<String> name();

    Optional<Integer> getVersion();

    @Override
    default ResourceIdentifier withId(String id) {
        return new ResourceIdentifier.Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableResourceIdentifier.Builder {
        // allow access to ImmutableResourceIdentifier.Builder
    }
}
