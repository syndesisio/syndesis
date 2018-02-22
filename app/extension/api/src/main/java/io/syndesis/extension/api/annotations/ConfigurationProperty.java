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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(ConfigurationProperties.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ConfigurationProperty {
    /**
     * The name of the property.
     */
    String name();

    /**
     * The display name of the property.
     */
    String displayName();

    /**
     * The property description.
     */
    String description();

    /**
     * The default value of the property.
     */
    String defaultValue() default "";

    /**
     * Indicates if the property is a component property.
     */
    boolean componentProperty() default false;

    /**
     * Indicates if the property is deprecated.
     */
    boolean deprecated() default false;

    /**
     * The property group.
     */
    String group() default "";

    /**
     * The java type of the property.
     */
    String javaType() default "String";

    /**
     * The kind of property.
     */
    String kind() default "parameter";

    /**
     * The label associated with the property.
     */
    String label() default "";

    /**
     * Indicates if this property is required.
     */
    boolean required() default false;

    /**
     * Indicates if this property may contain sensitive data.
     */
    boolean secret() default false;

    /**
     * Indicates if this property should not be sanitized.
     */
    boolean raw() default false;

    /**
     * The type of property.
     */
    String type() default "string";

    /**
     * The tags associated with the property.
     */
    String[] tags() default {};

    /**
     * All enums associated with the property.
     */
    PropertyEnum[] enums() default {};

    /**
     * Represents a key-value enumerable element.
     */
    @interface PropertyEnum {
        /**
         * The label of the enum.
         */
        String label();

        /**
         * The value of the enum.
         */
        String value();
    }
}
