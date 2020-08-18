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
import java.util.List;
import java.util.Set;

import io.syndesis.connector.odata2.meta.ODataMetadata.PropertyMetadata;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmElement;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdmTypeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(EdmTypeConverter.class);

    private String name = "<Unknown>";

    private boolean optional = true;

    private boolean isCollection;

    private PropertyMetadata property(Class<?> klazz) {
        PropertyMetadata metadata = new PropertyMetadata(this.name, klazz);
        metadata.setArray(isCollection);
        metadata.setRequired(!optional);
        return metadata;
    }

    private Set<PropertyMetadata> properties(EdmStructuralType structuredType) throws EdmException {
        Set<PropertyMetadata> properties = new HashSet<>();
        List<String> propertyNames = structuredType.getPropertyNames();
        for (String propertyName : propertyNames) {
            EdmTyped property = structuredType.getProperty(propertyName);
            this.name = propertyName;
            properties.add(visit(property));
        }
        return properties;
    }

    public PropertyMetadata visit(EdmSimpleType type) throws EdmException {
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
            case "DateTime":
            case "DateTimeOffset":
                return property(Number.class);
            case "String":
            case "Binary":
            case "Date":
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

    public PropertyMetadata visit(EdmComplexType type) throws EdmException {
        PropertyMetadata property = property(Object.class);
        Set<PropertyMetadata> childProperties = properties(type);
        property.setChildProperties(childProperties);
        return property;
    }

    public Set<PropertyMetadata> visit(EdmEntityType entityType) throws EdmException {
        return properties(entityType);
    }

    public PropertyMetadata visit(EdmType type) throws EdmException {
        if (type instanceof EdmSimpleType) {
            return visit((EdmSimpleType) type);
        } else if (type instanceof EdmComplexType) {
            return visit((EdmComplexType) type);
        }

        return property(String.class);
    }

    public PropertyMetadata visit(EdmTyped type) throws EdmException {
        if (type instanceof EdmProperty) {
            return visit((EdmProperty) type);
        } else if (type instanceof EdmParameter) {
            return visit((EdmParameter) type);
        } else if (type instanceof EdmElement) {
            return visit((EdmElement) type);
        }

        return property(String.class);
    }

    public PropertyMetadata visit(EdmNavigationProperty property) throws EdmException {
        this.optional = property.getMultiplicity().equals(EdmMultiplicity.ZERO_TO_ONE);
        this.isCollection = property.getMultiplicity().equals(EdmMultiplicity.MANY);
        PropertyMetadata metaProperty = property(Object.class);
        if (property.getType() instanceof EdmStructuralType) {
            metaProperty.setChildProperties(properties((EdmStructuralType) property.getType()));
        }

        return metaProperty;
    }

    public PropertyMetadata visit(EdmParameter property) throws EdmException {
        this.optional = property.getMultiplicity().equals(EdmMultiplicity.ZERO_TO_ONE);
        this.isCollection = property.getMultiplicity().equals(EdmMultiplicity.MANY);
        return visit(property.getType());
    }

    public PropertyMetadata visit(EdmProperty property) throws EdmException {
        this.optional = property.getMultiplicity().equals(EdmMultiplicity.ZERO_TO_ONE);
        this.isCollection = property.getMultiplicity().equals(EdmMultiplicity.MANY);
        return visit(property.getType());
    }

    public PropertyMetadata visit(EdmElement property) throws EdmException {
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
