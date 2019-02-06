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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = DataShape.Builder.class)
// Immutables generates code that fails these checks
@SuppressWarnings({ "ArrayEquals", "ArrayHashCode", "ArrayToString" })
public interface DataShape extends Serializable, WithName, WithMetadata {

    @Override
    String getName();

    String getDescription();

    DataShapeKinds getKind();

    String getType();

    /**
     * The collection type that should be inspected.
     */
    Optional<String> getCollectionType();

    /**
     * The class name that should be inspected.
     */
    Optional<String> getCollectionClassName();

    String getSpecification();

    Optional<byte[]> getExemplar();

    @Override
    Map<String, String> getMetadata();

    /**
     * Holds the variants available for this data shape.
     *
     * A variant could be the single element inspection of collection
     */
    @Value.Default
    default List<DataShape> getVariants() {
        return Collections.emptyList();
    }

    default Optional<DataShape> findVariant(Predicate<DataShape> predicate) {
        if (predicate.test(this)) {
            return Optional.of(this);
        }

        return getVariants().stream().filter(predicate).findFirst();
    }

    default Optional<DataShape> findVariantByMeta(String key, String val) {
        return findVariant(ds -> {
            if (key == null || val == null) {
                return false;
            }

            return Objects.equals(
                ds.getMetadata().get(key),
                val
            );
        });
    }

    class Builder extends ImmutableDataShape.Builder {
    }
}
