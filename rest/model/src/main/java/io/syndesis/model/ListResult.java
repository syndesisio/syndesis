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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

/**
 * Holds the result of a list, including the total count of elements. This is used by the client for working out paging
 * when viewing a large list.
 *
 * @param <T> The type of the elements in the returned list.
 */
@Value.Immutable
@JsonDeserialize(builder = ListResult.Builder.class)
@SuppressWarnings({"rawtypes", "varargs"})
public interface ListResult<T> {

    /**
     *
     * @return The total count of entities available.
     */
    int getTotalCount();

    /**
     *
     * @return The filtered list of items. Depending on operations, this will contain at most getTotalCount elements.
     */
    List<T> getItems();

    class Builder<T> extends ImmutableListResult.Builder<T> {
    }

    static <T> ListResult<T> of(Collection<T> items) {
        return new Builder<T>().items(items).totalCount(items.size()).build();
    }

    static <T> Collector<T, Builder<T>, Builder<T>> collector() {
        return Collector.of(Builder::new, Builder::addItem, (b1, b2) -> b1.addAllItems(b2.build().getItems()));
    }

}
