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
package io.syndesis.server.runtime.swagger;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelConverterTest {

    final ModelConverter converter = new ModelConverter();

    interface Sample {

        @JsonIgnore(false)
        String notToBeIgnored();

        @JsonIgnore
        String shouldBeIgnored();

    }

    @Test
    public void shouldIgnoreMethodsAnnotatedWithJsonIgnore() throws NoSuchMethodException {
        final AnnotatedMethod method = method("shouldBeIgnored");

        assertThat(converter.ignore(method, null, "property", Collections.emptySet())).isTrue();
    }

    @Test
    public void shouldNotIgnoreMethodsAnnotatedWithJsonIgnoreValueOfFalse() throws NoSuchMethodException {
        final AnnotatedMethod method = method("notToBeIgnored");

        assertThat(converter.ignore(method, null, "property", Collections.emptySet())).isFalse();
    }

    private static AnnotatedMethod method(final String name) throws NoSuchMethodException {
        final Method shouldBeIgnored = Sample.class.getMethod(name);
        final AnnotationMap annotation = AnnotationMap.of(JsonIgnore.class, shouldBeIgnored.getAnnotation(JsonIgnore.class));
        final AnnotatedMethod method = new AnnotatedMethod(null, shouldBeIgnored, annotation, null);
        return method;
    }
}
