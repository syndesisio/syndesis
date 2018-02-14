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
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = DataShape.Builder.class)
// Immutables generates code that fails these checks
@SuppressWarnings({ "ArrayEquals", "ArrayHashCode", "ArrayToString" })
public interface DataShape extends Serializable, WithName {

    @Override
    String getName();

    String getDescription();

    DataShapeKinds getKind();

    String getType();

    String getSpecification();

    Optional<byte[]> getExemplar();

    class Builder extends ImmutableDataShape.Builder {
    }
}
