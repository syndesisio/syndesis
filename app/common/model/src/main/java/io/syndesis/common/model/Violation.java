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
package io.syndesis.common.model;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.common.util.immutable.ImmutablesStyle;
import io.syndesis.common.util.json.StringTrimmingConverter;

import org.immutables.value.Value;

@ImmutablesStyle
@Value.Immutable
@JsonDeserialize(builder = Violation.Builder.class)
public interface Violation {

    @SuppressWarnings("PMD.UseUtilityClass")
    final class Builder extends ImmutableViolation.Builder {

        public static Violation fromConstraintViolation(final ConstraintViolation<?> constraintViolation) {
            final String property = constraintViolation.getPropertyPath().toString();
            final ConstraintDescriptor<?> constraintDescriptor = constraintViolation.getConstraintDescriptor();
            final Annotation annotation = constraintDescriptor.getAnnotation();
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            final String error = annotationType.getSimpleName();
            final String message = constraintViolation.getMessage();

            return new Builder().property(property).error(error).message(message).build();
        }
    }

    @JsonDeserialize(converter = StringTrimmingConverter.class)
    String error();

    @JsonDeserialize(converter = StringTrimmingConverter.class)
    String message();

    @JsonDeserialize(converter = StringTrimmingConverter.class)
    String property();
}
