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

import io.syndesis.common.model.ListResult;
import io.syndesis.server.endpoint.v1.util.PredicateFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ReflectiveFilterer<T> implements Function<ListResult<T>, ListResult<T>> {

    private final List<PredicateFilter<T>> predicateFilters;

    public ReflectiveFilterer(Class<T> modelClass, List<FilterOptionsParser.Filter> filters) {
        predicateFilters = new ArrayList<>(filters.size());
        for (FilterOptionsParser.Filter f : filters) {
            String op = f.getOperation().orElseThrow(
                () -> new IllegalArgumentException("Missing filter operation")
            );

            if ("=".equals(op)) {
                String value = f.getValue().orElseThrow(
                    () -> new IllegalArgumentException("Missing value in equality filter")
                );
                predicateFilters.add(equalityFilter(modelClass, f.getProperty(), value));
            } else {
                throw new IllegalArgumentException(String.format("Unknown filter operation %s", op));
            }
        }
    }

    @Override
    public ListResult<T> apply(ListResult<T> result) {
        ListResult<T> intermediate = result;
        for (PredicateFilter<T> pf : predicateFilters) {
            intermediate = pf.apply(intermediate);
        }
        return intermediate;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private PredicateFilter<T> equalityFilter(final Class<T> modelClass, final String property, final String value) {
        Method stringGetMethod = ReflectionUtils.getGetMethodOfType(modelClass, property, String.class);
        if (stringGetMethod != null) {
            return new PredicateFilter<>(o -> {
                try {
                    return value.equals(stringGetMethod.invoke(o));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot extract String value from " + stringGetMethod + " for object " + o, e);
                }
            });
        }
        Method optionalStringGetMethod = ReflectionUtils.getGetMethodOfType(modelClass, property, Optional.class);
        if (optionalStringGetMethod != null) {
            return new PredicateFilter<>(o -> {
                try {
                    return Optional.ofNullable(value).equals(((Optional<?>) optionalStringGetMethod.invoke(o)).map(Object::toString));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot extract String value from " + optionalStringGetMethod + " for object " + o, e);
                }
            });
        }
        Method objectGetMethod = ReflectionUtils.getGetMethodOfType(modelClass, property, Object.class);
        if (objectGetMethod != null) {
            return new PredicateFilter<>(o -> {
                try {
                    return value.equals(Optional.ofNullable(objectGetMethod.invoke(o)).map(Object::toString).orElse(null));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot extract Object value from " + objectGetMethod + " for object " + o, e);
                }
            });
        }
        throw new IllegalArgumentException(String.format("Cannot find field %s in %s as field", property, modelClass.getName()));
    }
}
