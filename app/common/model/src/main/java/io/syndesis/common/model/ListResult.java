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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public interface ListResult<T> extends Iterable<T> {

    /**
     *
     * @return The total count of entities available.
     */
    int getTotalCount();

    /**
     *
     * @return The filtered list of items. Depending on operations, this will contain at most getTotalCount elements.
     */
    @Value.Default
    default List<T> getItems() {
        return Collections.emptyList();
    }

    @JsonIgnore
    @Override
    default void forEach(Consumer<? super T> action) {
        getItems().forEach(action);
    }

    @JsonIgnore
    @Override
    default Iterator<T> iterator() {
        return getItems().iterator();
    }

    class Builder<T> extends ImmutableListResult.Builder<T> {
    }

    static <T> ListResult<T> of(Collection<T> items) {
        return new Builder<T>().items(items).totalCount(items.size()).build();
    }

    @SafeVarargs
    static <T> ListResult<T> of(T... items) {
        return new Builder<T>().addItems(items).totalCount(items.length).build();
    }

    static <T> Collector<T, Builder<T>, Builder<T>> collector() {
        return Collector.of(Builder::new, Builder::addItem, (b1, b2) -> b1.addAllItems(b2.build().getItems()));
    }

}
