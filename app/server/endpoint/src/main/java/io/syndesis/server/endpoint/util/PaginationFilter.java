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
package io.syndesis.server.endpoint.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.syndesis.common.model.ListResult;

/**
 * Filters the list with the provided pagination options.
 *
 * @param <T> The type of the elements in the filtered list.
 */
public class PaginationFilter<T> implements Function<ListResult<T>, ListResult<T>> {

    private final int startIndex;
    private final int endIndex;

    /**
     * Creates the filter with the specified pagination options.
     *
     * @param options The pagination options. Note that page and per_page must both be greater than 0.
     * @throws IllegalArgumentException If page or perPage are less than 1.
     */
    public PaginationFilter(PaginationOptions options) {
        if (options.getPage() < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (options.getPerPage() < 1) {
            throw new IllegalArgumentException("Per page must be greater than 0");
        }
        this.startIndex = (options.getPage() - 1) * options.getPerPage();
        this.endIndex = options.getPage() * options.getPerPage();
    }

    /**
     * Applies the filter to the provided list.
     *
     * @param result The result to filter.
     * @return The relevant page of the provided list. Returns an empty list if the requested page would be outside of the provided list range.
     */
    @Override
    public ListResult<T> apply(ListResult<T> result) {
        List<T> list = result.getItems();
        if (startIndex >= list.size()) {
            list = Collections.emptyList();
        } else {
            list = list.subList(startIndex, Math.min(list.size(), endIndex));
        }
        return new ListResult.Builder<T>().createFrom(result).items(list).build();
    }

}
