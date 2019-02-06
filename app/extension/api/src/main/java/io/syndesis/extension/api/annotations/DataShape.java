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
public @interface DataShape {

    /**
     * The data shape name.
     */
    String name() default "";

    /**
     * The data shape description.
     */
    String description() default "";

    /**
     * The data shape kind.
     */
    String kind() default "none";

    /**
     * The data shape type.
     */
    String type() default "";

    /**
     * The collection   class name that should be inspected.
     */
    String collectionClassName() default "";

    /**
     * The collection type that should be inspected.
     */
    String collectionType() default "";

    /**
     * The data shape specification.
     */
    String specification() default "";

    /**
     * Meta data associated with the data shape.
     */
    Meta[] metadata() default {};

    /**
     * Variants for the data shape.
     */
    Variant[] variants() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @interface Variant {

        /**
         * The data shape name.
         */
        String name() default "";

        /**
         * The data shape description.
         */
        String description() default "";

        /**
         * The data shape kind.
         */
        String kind() default "none";

        /**
         * The data shape type.
         */
        String type() default "";

        /**
         * The collection   class name that should be inspected.
         */
        String collectionClassName() default "";

        /**
         * The collection type that should be inspected.
         */
        String collectionType() default "";

        /**
         * The data shape specification.
         */
        String specification() default "";

        /**
         * Meta data associated with the variant.
         */
        Meta[] metadata() default {};
    }

    /**
     * Represents a key-value pair.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @interface Meta {
        /**
         * The key.
         */
        String key();

        /**
         * The value.
         */
        String value();
    }

}
