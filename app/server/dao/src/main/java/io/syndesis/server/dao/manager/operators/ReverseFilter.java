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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithResourceId;

/**
 * Reverse the elements in the list.
 *
 * @param <T> The type of the elements in the filtered list.
 */
public final class ReverseFilter<T extends WithResourceId> implements Function<ListResult<T>, ListResult<T>> {

    @SuppressWarnings({"rawtypes"})
    public static final ReverseFilter INSTANCE = new ReverseFilter();

    /**
     * Applies the filter to the provided list.
     */
    @Override
    public ListResult<T> apply(ListResult<T> result) {
        List<T> list = new ArrayList<>(result.getItems());
        Collections.reverse(list);
        return ListResult.of(list);
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends WithResourceId> ReverseFilter<T>getInstance() {
        return (ReverseFilter<T>)INSTANCE;
    }
}
