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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import io.syndesis.common.model.ListResult;

/**
 * Generic comparator which sorts based on fields. Fields are retrieved by reflections
 */
public class ReflectiveSorter<T> implements Function<ListResult<T>, ListResult<T>>, Comparator<T> {

    private Comparator<T> delegate;

    @Override
    public ListResult<T> apply(ListResult<T> result) {
        List<T> list = new ArrayList<>(result.getItems());
        if (delegate != null) {
            // We are sorting inline when a delegate is given. Otherwise its a no-op
            list.sort(this);
        }
        return new ListResult.Builder<T>().createFrom(result).items(list).build();
    }

    public ReflectiveSorter(Class<T> modelClass, SortOptions options) {
        String sortField = options.getSortField();
        if (sortField == null) {
            // No sorting
            delegate = null;
        } else {
            delegate = createDelegateComparator(modelClass, sortField);

            if (options.getSortDirection() == SortOptions.SortDirection.DESC) {
                delegate = delegate.reversed();
            }
        }
    }

    private Comparator<T> createDelegateComparator(Class<T> modelClass, String fieldName) {
        Comparator<T> delegate = getIntComparator(modelClass, fieldName);
        if (delegate != null) {
            return delegate;
        }

        delegate = getStringComparator(modelClass, fieldName);
        if (delegate != null) {
            return delegate;
        }

        throw new IllegalArgumentException(String.format("Cannot find field %s in %s as int or String field",fieldName, modelClass.getName()));
    }

    private Comparator<T> getStringComparator(Class<T> modelClass, String fieldName) {
        Method stringGetMethod = ReflectionUtils.getGetMethodOfType(modelClass, fieldName, String.class);
        if (stringGetMethod != null) {
            return Comparator.comparing(k -> {
                try {
                    return (String) stringGetMethod.invoke(k);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot extract String value from " + stringGetMethod + " for object " + k, e);
                }
            });
        }
        return null;
    }

    private Comparator<T> getIntComparator(Class<T> modelClass, String fieldName) {
        Method intGetMethod = ReflectionUtils.getGetMethodOfType(modelClass, fieldName, int.class, Integer.class);
        if (intGetMethod != null) {
            return Comparator.comparingInt(k -> {
                        try {
                            return (Integer) intGetMethod.invoke(k);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new IllegalArgumentException("Cannot extract int value from " + intGetMethod + " for object " + k, e);
                        }
            });
        }
        return null;
    }


    @Override
    public int compare(T o1, T o2) {
        return delegate.compare(o1,o2);
    }

}
