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
package io.syndesis.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.core.json.StringTrimmingConverter;

@Value.Immutable
@JsonDeserialize(builder = DataShape.Builder.class)
// Immutables generates code that fails these checks
@SuppressWarnings({ "ArrayEquals", "ArrayHashCode", "ArrayToString" })
public interface DataShape extends Serializable, WithName, WithMetadata {

    @Override
    String getName();

    String getDescription();

    @JsonDeserialize(converter = StringTrimmingConverter.class)
    DataShapeKinds getKind();

    @JsonDeserialize(converter = StringTrimmingConverter.class)
    String getType();

    String getSpecification();

    Optional<byte[]> getExemplar();

    @Override
    Map<String, String> getMetadata();

    class Builder extends ImmutableDataShape.Builder {
    }
}
