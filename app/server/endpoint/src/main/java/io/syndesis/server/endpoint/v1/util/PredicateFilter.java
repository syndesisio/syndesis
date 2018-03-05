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
package io.syndesis.server.endpoint.v1.util;


import io.syndesis.common.model.ListResult;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic filter that removes items that do not match predicate
 */
public class PredicateFilter<T> implements Function<ListResult<T>, ListResult<T>> {

    private final Predicate<T> filter;

    @Override
    public ListResult<T> apply(ListResult<T> result) {
        List<T> list = result.getItems().stream().filter(filter).collect(Collectors.toList());

        return new ListResult.Builder<T>().createFrom(result).items(list).totalCount(list.size()).build();
    }

    public PredicateFilter(Predicate<T> filter) {
        this.filter = filter;
    }

}
