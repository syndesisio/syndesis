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

import org.immutables.value.Value;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionUtilsTest {

    @Test
    public void shouldGetAllClassMethods() {
        Method intMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "anInt", Integer.class);
        Method booleanMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "aBoolean", boolean.class);
        Method doubleMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "aDouble", double.class);
        Method stringMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "aString", String.class);
        Method objectMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "anObject", Object.class);
        Method classMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "someClass", SomeClass.class);
        // Check inheritance
        Method classToSomeOtherClassMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "someClass", SomeOtherClass.class);
        // Check fallback to Object
        Method classToObjectMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "someClass", Object.class);
        // Check inheritance with Enum supertype
        Method enumMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "enumType", Enum.class);
        // Check fallback to Object
        Method enumToObjectMethod = ReflectionUtils.getGetMethodOfType(JustForTest.class, "enumType", Object.class);

        assertThat(intMethod).isNotNull();
        assertThat(booleanMethod).isNotNull();
        assertThat(doubleMethod).isNotNull();
        assertThat(stringMethod).isNotNull();
        assertThat(objectMethod).isNotNull();
        assertThat(classMethod).isNotNull();
        assertThat(classToSomeOtherClassMethod).isNotNull();
        assertThat(classToObjectMethod).isNotNull();
        assertThat(enumMethod).isNotNull();
        assertThat(enumToObjectMethod).isNotNull();
    }

    @Value.Immutable
    interface JustForTest{

        enum Type {
            TypeA,
            TypeB
        }

        Integer getAnInt();
        double getADouble();
        boolean isABoolean();
        String getAString();
        Object getAnObject();
        SomeClass getSomeClass();
        Type getEnumType();
    }

    interface SomeClass extends SomeOtherClass {
        Integer getAnotherInt();
    }

    interface SomeOtherClass{

    }

}


