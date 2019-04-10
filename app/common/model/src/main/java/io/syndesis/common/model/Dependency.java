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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = Dependency.Builder.class)
public interface Dependency {

    enum Type {
        MAVEN,
        EXTENSION,
        EXTENSION_TAG,
        ICON
    }

    /**
     * The dependency type;
     */
    Type getType();

    /**
     * The dependency id such as a maven coordinate, and extension id, etc.
     */
    String getId();

    // *****************
    // Helpers
    // *****************

    @JsonIgnore
    default boolean isOfType(Type type) {
        return type.equals(getType());
    }

    @JsonIgnore
    default boolean isMaven() {
        return isOfType(Type.MAVEN);
    }

    @JsonIgnore
    default boolean isExtension() {
        return isOfType(Type.EXTENSION);
    }

    @JsonIgnore
    default boolean isExtensionTag() {
        return isOfType(Type.EXTENSION_TAG);
    }

    @JsonIgnore
    default boolean isIcon() {
        return isOfType(Type.ICON);
    }

    @JsonIgnore
    static Dependency from(Type type, String id) {
        return new Builder().type(type).id(id).build();
    }

    @JsonIgnore
    static Dependency maven(String id) {
        return from(Type.MAVEN, id);
    }

    @JsonIgnore
    static Dependency extension(String id) {
        return from(Type.EXTENSION, id);
    }

    @JsonIgnore
    static Dependency libraryTag(String id) {
        return from(Type.EXTENSION_TAG, id);
    }

    @JsonIgnore
    static Dependency icon(String id) {
        return from(Type.ICON, id);
    }

    // *****************
    // Builder
    // *****************

    class Builder extends ImmutableDependency.Builder {
    }
}
