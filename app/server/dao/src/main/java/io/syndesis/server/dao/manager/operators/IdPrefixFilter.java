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
package io.syndesis.server.dao.manager.operators;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithResourceId;

/**
 * Filters the by id prefix.
 *
 * @param <T> The type of the elements in the filtered list.
 */
public final class IdPrefixFilter<T extends WithResourceId> implements Function<ListResult<T>, ListResult<T>> {

    private final String prefix;

    /**
     * Creates the filter with the specified prefix filter.
     *
     * @param prefix the prefix to match
     */
    public IdPrefixFilter(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Applies the filter to the provided list.
     *
     * @param result The result to filter.
     * @return  all entries with ids that start with specified prefix
     */
    @Override
    public ListResult<T> apply(ListResult<T> result) {
        List<T> list = result.getItems().stream()
            .filter(x ->
                x.getId().filter(y->y.startsWith(prefix)).isPresent()
            )
            .collect(Collectors.toList());
        return new ListResult.Builder<T>().createFrom(result).items(list).build();
    }

    public String getPrefix() {
        return prefix;
    }
}
