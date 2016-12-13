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
package com.redhat.ipaas.rest.util;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Generic comparator which sorts based on fields. Fields are retrieved by reflections
 *
 * @author roland
 * @since 13/12/16
 */
public class ReflectiveSorter<T> implements Function<List<T>, List<T>>, Comparator<T> {

    private Comparator<T> delegate;

    @Override
    public List<T> apply(List<T> list) {
        if (delegate != null) {
            // We are sorting inline when a delegate is given. Otherwise its a no-op
            list.sort(this);
        }
        return list;
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
        Field stringField = getFieldOfType(modelClass, fieldName, String.class);
        if (stringField != null) {
            return Comparator.comparing(k -> {
                try {
                    stringField.setAccessible(true);
                    return (String) stringField.get(k);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot extract String value of field " + stringField + " for object " + k, e);
                }
            });
        }
        return null;
    }

    private Comparator<T> getIntComparator(Class<T> modelClass, String fieldName) {
        Field intField = getFieldOfType(modelClass, fieldName, int.class, Integer.class);
        if (intField != null) {
            return Comparator.comparingInt(k -> {
                        try {
                            intField.setAccessible(true);
                            return (Integer) intField.get(k);
                        } catch (IllegalAccessException e) {
                            throw new IllegalArgumentException("Cannot extract int value of field " + intField + " for object " + k, e);
                        }
            });
        }
        return null;
    }


    private Field getFieldOfType(Class clazz, String fieldName, Class ... types) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Class fieldClass = field.getType();
            for (Class type : types) {
                if (fieldClass.isAssignableFrom(type)) {
                    return field;
                }
            }
            return null;
        } catch (NoSuchFieldException exp) {
            return null;
        }
    }

    @Override
    public int compare(T o1, T o2) {
        return delegate.compare(o1,o2);
    }

}
