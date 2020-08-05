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
package io.syndesis.connector.odata2.meta;

import java.util.HashSet;
import java.util.Set;
import io.syndesis.connector.odata2.ODataConstants;

public class ODataMetadata implements ODataConstants {

    private Set<String> entityNames;

    private Set<PropertyMetadata> entityProperties;

    public static class PropertyMetadata {

        public enum TypeClass {
            STRING,
            NUMBER,
            BOOLEAN,
            OBJECT,
            OTHER;

            public static TypeClass valueOf(Class<?> klazz) {
                for (TypeClass typeClass : values()) {
                    if (typeClass.name().equalsIgnoreCase(klazz.getSimpleName())) {
                        return typeClass;
                    }
                }

                return OTHER;
            }
        }

        private final String name;
        private final TypeClass type;
        private boolean array;
        private boolean required;
        private Set<PropertyMetadata> childProperties;

        public PropertyMetadata(String name, Class<?> type) {
            this.name = name;
            this.type = TypeClass.valueOf(type);
        }

        public String getName() {
            return name;
        }

        public TypeClass getType() {
            return type;
        }

        public boolean isArray() {
            return array;
        }

        public void setArray(boolean array) {
            this.array = array;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Set<PropertyMetadata> getChildProperties() {
            return childProperties;
        }

        public void setChildProperties(Set<PropertyMetadata> childProperties) {
            this.childProperties = childProperties;
        }
    }

    public boolean hasEntityProperties() {
        return entityProperties != null && ! entityProperties.isEmpty();
    }

    public Set<PropertyMetadata> getEntityProperties() {
        return entityProperties;
    }

    public void addEntityProperty(PropertyMetadata property) {
        if (entityProperties == null) {
            entityProperties = new HashSet<>();
        }

        entityProperties.add(property);
    }

    public void setEntityProperties(Set<PropertyMetadata> properties) {
        entityProperties = properties;
    }

    public boolean hasEntityNames() {
        return entityNames != null && ! entityNames.isEmpty();
    }

    public Set<String> getEntityNames() {
        return entityNames;
    }

    public void setEntityNames(Set<String> names) {
        this.entityNames = names;
    }
}
