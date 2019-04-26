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
package io.syndesis.connector.odata.meta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.odata.meta.ODataMetadata.PropertyMetadata;

public class EdmTypeConvertor {

    private static final Logger LOG = LoggerFactory.getLogger(EdmTypeConvertor.class);

    private String name = "<Unknown>";

    private boolean nullable = true;

    private boolean isCollection;

    private PropertyMetadata property(Class<?> klazz) {
        PropertyMetadata metadata = new PropertyMetadata(this.name, klazz);
        metadata.setArray(isCollection);
        metadata.setRequired(! nullable);
        return metadata;
    }

    private Set<PropertyMetadata> properties(EdmStructuredType structuredType) {
        Set<PropertyMetadata> properties = new HashSet<>();
        List<String> propertyNames = structuredType.getPropertyNames();
        for (String propertyName : propertyNames) {
            EdmElement property = structuredType.getProperty(propertyName);
            properties.add(visit(property));
        }
        return properties;
    }

    public PropertyMetadata visit(EdmEnumType type) {
        /*
         * Need to synchronize with what the customize generates from enum values
         * see {@link io.syndesis.connector.odata.customizer.json.ClientEnumValueSerializer#serialize}
         */
        return property(String.class);
    }

    public PropertyMetadata visit(EdmPrimitiveType type) {
        switch (type.getName()) {
            case "Boolean":
                return property(Boolean.class);
            case "Byte":
            case "SByte":
            case "Int16":
            case "Int32":
            case "Int64":
            case "Single":
            case "Double":
            case "Decimal":
                return property(Number.class);
            case "String":
            case "Binary":
            case "Date":
            case "DateTimeOffset":
            case "Duration":
            case "Guid":
            case "TimeOfDay":
                return property(String.class);
            default:
                if (LOG.isWarnEnabled()) {
                    LOG.warn("The primitive edm type '{}' is not supported. Returning as string", type.getName());
                }
                return property(String.class);
        }
    }

    public PropertyMetadata visit(EdmComplexType type) {
        PropertyMetadata property = property(Object.class);
        Set<PropertyMetadata> childProperties = properties(type);
        property.setChildProperties(childProperties);
        return property;
    }

    public Set<PropertyMetadata> visit(EdmEntityType entityType) {
        return properties(entityType);
    }

    public PropertyMetadata visit(EdmType type) {
        if (type instanceof EdmEnumType) {
            return visit((EdmEnumType) type);
        } else if (type instanceof EdmPrimitiveType) {
            return visit((EdmPrimitiveType) type);
        } else if (type instanceof EdmComplexType) {
            return visit((EdmComplexType) type);
        }

        return property(String.class);
    }

    public PropertyMetadata visit(EdmNavigationProperty property) {
        this.nullable = property.isNullable();
        this.isCollection = property.isCollection();
        PropertyMetadata metaProperty = property(Object.class);
        metaProperty.setChildProperties(visit(property.getType()));
        return metaProperty;
    }

    public PropertyMetadata visit(EdmParameter property) {
        this.nullable = property.isNullable();
        this.isCollection = property.isCollection();
        return visit(property.getType());
    }

    public PropertyMetadata visit(EdmProperty property) {
        this.nullable = property.isNullable();
        this.isCollection = property.isCollection();
        return visit(property.getType());
    }

    public PropertyMetadata visit(EdmElement property) {
        this.name = property.getName();

        if (property instanceof EdmNavigationProperty) {
            return visit((EdmNavigationProperty) property);
        } else if (property instanceof EdmParameter) {
            return visit((EdmParameter) property);
        } else if (property instanceof EdmProperty) {
            return visit((EdmProperty) property);
        }

        throw new UnsupportedOperationException();
    }
}
