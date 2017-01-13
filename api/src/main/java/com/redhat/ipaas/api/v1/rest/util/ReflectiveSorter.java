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
package com.redhat.ipaas.api.v1.rest.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        Method stringGetMethod = getGetMethodOfType(modelClass, fieldName, String.class);
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
        Method intGetMethod = getGetMethodOfType(modelClass, fieldName, int.class, Integer.class);
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


    private Method getGetMethodOfType(Class clazz, String fieldName, Class ... types) {
        // Check direct:
        Method ret = extractMethod(clazz, fieldName, types);
        if (ret != null) {
            return ret;
        }

        // Check interfaces :
        for (Class intf : clazz.getInterfaces()) {
            ret = extractMethod(intf, fieldName, types);
            if (ret != null) {
                return ret;
            }
        }

        // Check parent:
        Class superClass = clazz.getSuperclass();
        return superClass != null ? getGetMethodOfType(superClass, fieldName, types) : null;
    }

    private Method extractMethod(Class clazz, String fieldName, Class[] types) {
        try {
            Method method = clazz.getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            Class returnType = method.getReturnType();
            for (Class type : types) {
                if (returnType.isAssignableFrom(type)) {
                    return method;
                }
            }
            return null;
        } catch (NoSuchMethodException exp) {
            return null;
        }
    }

    @Override
    public int compare(T o1, T o2) {
        return delegate.compare(o1,o2);
    }

}
