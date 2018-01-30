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
package io.syndesis.extension.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SyndesisExtensionAction {
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
     * The action input data shape name;
     */
    String inputDataShapeName() default "";

    /**
     * The action input data shape description;
     */
    String inputDataShapeDescription() default "";

    /**
     * The action input data shape;
     */
    String inputDataShape() default "any";

    /**
     * The action output data shape name;
     */
    String outputDataShapeName() default "";

    /**
     * The action output data shape description;
     */
    String outputDataShapeDescription() default "";

    /**
     * The action output data shape;
     */
    String outputDataShape() default "any";

    /**
     * The entrypoint;
     */
    String entrypoint() default "";
}
