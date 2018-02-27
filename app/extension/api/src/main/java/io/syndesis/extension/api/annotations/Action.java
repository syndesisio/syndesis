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
package io.syndesis.extension.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Action {
    /**
     * The action id;
     */
    String id();

    /**
     * The action name.
     */
    String name();

    /**
     * The action description.
     */
    String description();

    /**
     * The action tags;
     */
    String[] tags() default {};

    /**
     * The action input data shape;
     */
    DataShape inputDataShape() default @DataShape();

    /**
     * The action output data shape;
     */
    DataShape outputDataShape() default @DataShape();

    /**
     * The action entry point;
     */
    String entrypoint() default "";
}
