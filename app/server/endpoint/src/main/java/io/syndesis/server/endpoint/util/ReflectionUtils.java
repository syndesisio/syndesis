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

import java.lang.reflect.Method;
import java.util.Locale;

public final class ReflectionUtils {
    private ReflectionUtils() {
        // utility class
    }

    static Method getGetMethodOfType(Class<?> clazz, String fieldName, Class<?>... types) {
        // Check direct:
        Method ret = extractMethod(clazz, fieldName, types);
        if (ret != null) {
            return ret;
        }

        // Check interfaces :
        for (Class<?> intf : clazz.getInterfaces()) {
            ret = extractMethod(intf, fieldName, types);
            if (ret != null) {
                return ret;
            }
        }

        // Check parent:
        Class<?> superClass = clazz.getSuperclass();
        return superClass != null ? getGetMethodOfType(superClass, fieldName, types) : null;
    }

    static Method extractMethod(Class<?> clazz, String fieldName, Class<?>... types) {
        try {
            Method method = clazz.getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1));
            Class<?> returnType = method.getReturnType();
            for (Class<?> type : types) {
                if (returnType.isAssignableFrom(type)) {
                    return method;
                }
            }
            return null;
        } catch (NoSuchMethodException exp) {
            return null;
        }
    }
}
