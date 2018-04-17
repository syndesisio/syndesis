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
package io.syndesis.server.connector.generator.swagger;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Xml;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper;

import org.apache.commons.lang3.ClassUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.syndesis.server.connector.generator.swagger.util.SwaggerHelper.dereference;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.addElement;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.isAttribute;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.isElement;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.nameOf;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.nameOrDefault;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.newXmlSchema;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.serialize;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.toXsdType;
import static io.syndesis.server.connector.generator.swagger.util.XmlSchemaHelper.xmlTargetNamespaceOrNull;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("PMD.GodClass")
public class UnifiedXmlDataShapeGenerator extends BaseDataShapeGenerator {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<AbstractSerializableParameter<?>> PARAM_CLASS = (Class) AbstractSerializableParameter.class;

    private static final String SCHEMA_SET_NS = "http://atlasmap.io/xml/schemaset/v2";

    private static final String SYNDESIS_PARAMETERS_NS = "http://syndesis.io/v1/swagger-connector-template/parameters";

    private static final String SYNDESIS_REQUEST_NS = "http://syndesis.io/v1/swagger-connector-template/request";

    static class SchemaPrefixAndElement {
        private final String prefix;

        private final Element schema;

        public SchemaPrefixAndElement(final String prefix, final Element schema) {
            this.prefix = prefix;
            this.schema = schema;
        }
    }

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Swagger swagger, final Operation operation) {
        final Document document = DocumentHelper.createDocument();

        final Element schemaSet = document.addElement("d:SchemaSet", SCHEMA_SET_NS);
        schemaSet.addNamespace(XmlSchemaHelper.XML_SCHEMA_PREFIX, XmlSchemaHelper.XML_SCHEMA_NS);

        final Element schema = addElement(schemaSet, "schema");
        schema.addAttribute("targetNamespace", SYNDESIS_REQUEST_NS);
        schema.addAttribute("elementFormDefault", "qualified");

        final Element parametersSchema = createParametersSchema(operation);

        final Map<String, SchemaPrefixAndElement> moreSchemas = new HashMap<>();

        final Element bodySchema = createRequestBodySchema(swagger, operation, moreSchemas);

        if (bodySchema == null && parametersSchema == null) {
            return DATA_SHAPE_NONE;
        }

        final Element element = addElement(schema, "element");
        element.addAttribute("name", "request");
        final Element sequence = addElement(element, "complexType", "sequence");

        final Element additionalSchemas = schemaSet.addElement("d:AdditionalSchemas");

        if (parametersSchema != null) {
            final Element parameters = addElement(sequence, "element");
            parameters.addNamespace("p", SYNDESIS_PARAMETERS_NS);
            parameters.addAttribute("ref", "p:parameters");

            additionalSchemas.add(parametersSchema.detach());
        }

        if (bodySchema != null) {
            final Element bodyElement = addElement(sequence, "element");
            bodyElement.addAttribute("name", "body");

            final Element body = addElement(bodyElement, "complexType", "sequence", "element");
            final String bodyTargetNamespace = bodySchema.attributeValue("targetNamespace");

            final String bodyElementName = bodySchema.element("element").attributeValue("name");
            if (bodyTargetNamespace != null) {
                body.addNamespace("b", bodyTargetNamespace);
                body.addAttribute("ref", "b:" + bodyElementName);
            } else {
                body.addAttribute("ref", bodyElementName);
            }

            additionalSchemas.add(bodySchema.detach());
        }

        moreSchemas.values().forEach(e -> additionalSchemas.add(e.schema.detach()));

        final String xmlSchemaSet = serialize(document);

        return new DataShape.Builder()//
            .kind(DataShapeKinds.XML_SCHEMA)//
            .name("Request")//
            .description("API request payload")//
            .specification(xmlSchemaSet)//
            .build();
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final Swagger swagger, final Operation operation) {
        final Optional<Response> maybeResponse = findResponse(operation);

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Document document = DocumentHelper.createDocument();

        final Element schemaSet = document.addElement("d:SchemaSet", SCHEMA_SET_NS);
        schemaSet.addNamespace(XmlSchemaHelper.XML_SCHEMA_PREFIX, XmlSchemaHelper.XML_SCHEMA_NS);

        final Map<String, SchemaPrefixAndElement> moreSchemas = new HashMap<>();

        final Element bodySchema = createResponseBodySchema(swagger, operation, moreSchemas);
        if (bodySchema == null) {
            return DATA_SHAPE_NONE;
        }

        schemaSet.add(bodySchema.detach());

        if (!moreSchemas.isEmpty()) {
            final Element additionalSchemas = schemaSet.addElement("d:AdditionalSchemas");
            moreSchemas.values().forEach(e -> additionalSchemas.add(e.schema.detach()));
        }

        final String xmlSchemaSet = serialize(document);

        return new DataShape.Builder()//
            .name("Response")//
            .description("API response payload")//
            .kind(DataShapeKinds.XML_SCHEMA)//
            .specification(xmlSchemaSet)//
            .build();
    }

    static void defineArrayElement(final Property property, final String propertyName, final Element parent, final Swagger swagger,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final ArrayProperty array = (ArrayProperty) property;

        final Element sequence;
        final Xml arrayXml = array.getXml();
        if (arrayXml != null && Boolean.TRUE.equals(arrayXml.getWrapped())) {
            final String arrayElementName = determineArrayElementName(propertyName, array);

            final Element arrayElement = addElement(parent, "element");
            arrayElement.addAttribute("name", requireNonNull(arrayElementName, "missing array property name"));

            final Element arrayComplex = addElement(arrayElement, "complexType");
            sequence = addElement(arrayComplex, "sequence");
        } else {
            sequence = parent;
        }

        final Property items = array.getItems();

        final Element itemsElement;
        final String arrayItemsType = items.getType();
        if ("ref".equals(arrayItemsType)) {
            itemsElement = defineComplexElement((RefProperty) items, sequence, swagger, moreSchemas);
        } else {
            itemsElement = addElement(sequence, "element");
            itemsElement.addAttribute("name", determineArrayItemName(propertyName, array));
            itemsElement.addAttribute("type", toXsdType(arrayItemsType));
        }

        if (array.getMaxItems() == null) {
            itemsElement.addAttribute("maxOccurs", "unbounded");
        } else {
            itemsElement.addAttribute("maxOccurs", String.valueOf(array.getMaxItems()));
        }

        if (array.getMinItems() == null) {
            itemsElement.addAttribute("minOccurs", "0");
        } else {
            itemsElement.addAttribute("minOccurs", String.valueOf(array.getMinItems()));
        }
    }

    static String determineArrayElementName(final String propertyName, final ArrayProperty array) {
        final Xml xml = array.getXml();

        if (xml == null || xml.getWrapped() == null || Boolean.FALSE.equals(xml.getWrapped())) {
            return null;
        }

        final String xmlName = xml.getName();
        if (xmlName != null) {
            return xmlName;
        }

        return propertyName;
    }

    static String determineArrayItemName(final String propertyName, final ArrayProperty array) {
        final Property items = array.getItems();

        final Xml itemXml = items.getXml();
        if (itemXml != null && itemXml.getName() != null) {
            return itemXml.getName();
        }

        final Xml xml = array.getXml();
        if (xml != null && xml.getName() != null) {
            return xml.getName();
        }

        return propertyName;
    }

    private static void addEnumerationsTo(final Element element, final List<?> enums) {
        final Element simpleType = addElement(element, "simpleType");
        final Element restriction = addElement(simpleType, "restriction");
        restriction.addAttribute("base", element.attributeValue("type"));

        for (final Object enumValue : enums) {
            final Element enumeration = addElement(restriction, "enumeration");
            enumeration.addAttribute("value", String.valueOf(enumValue));
        }
    }

    private static void addEnumsTo(final Element element, final AbstractSerializableParameter<?> serializableParameter) {
        if (serializableParameter.getItems() != null) {
            final Property items = serializableParameter.getItems();

            List<?> enums;
            try {
                final Method method = ClassUtils.getPublicMethod(items.getClass(), "getEnum");
                final List<?> tmp = (List<?>) method.invoke(items);

                enums = tmp;
            } catch (@SuppressWarnings("unused") final ReflectiveOperationException ignored) {
                enums = Collections.emptyList();
            }

            if (enums != null && !enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        } else {
            final List<String> enums = serializableParameter.getEnum();

            if (enums != null && !enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        }
    }

    private static Element createParametersSchema(final Operation operation) {
        final List<Parameter> operationParameters = operation.getParameters();

        final List<AbstractSerializableParameter<?>> serializableParameters = operationParameters.stream()//
            .filter(PARAM_CLASS::isInstance)//
            .map(PARAM_CLASS::cast)//
            .collect(Collectors.toList());

        if (serializableParameters.isEmpty()) {
            return null;
        }

        final Element schema = newXmlSchema(SYNDESIS_PARAMETERS_NS);

        final Element parameters = addElement(schema, "element");
        parameters.addAttribute("name", "parameters");

        final Element complex = addElement(parameters, "complexType");
        final Element sequence = addElement(complex, "sequence");

        for (final AbstractSerializableParameter<?> serializableParameter : serializableParameters) {
            final String type = toXsdType(serializableParameter.getType());
            final String name = trimToNull(serializableParameter.getName());

            if ("file".equals(type)) {
                // 'file' type is not allowed in JSON schema
                continue;
            }

            final Element element = addElement(sequence, "element");
            element.addAttribute("name", name);
            if (type != null) {
                element.addAttribute("type", type);
            }

            final Object defaultValue = serializableParameter.getDefault();
            if (defaultValue != null) {
                element.addAttribute("default", String.valueOf(defaultValue));
            }

            addEnumsTo(element, serializableParameter);
        }

        return schema;
    }

    private static Element createRequestBodySchema(final Swagger swagger, final Operation operation,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Optional<BodyParameter> bodyParameter = findBodyParameter(operation);

        if (!bodyParameter.isPresent()) {
            return null;
        }

        final BodyParameter body = bodyParameter.get();

        final Model bodySchema = body.getSchema();

        final ModelImpl bodySchemaToUse;
        if (bodySchema instanceof RefModel) {
            bodySchemaToUse = dereference((RefModel) bodySchema, swagger);
        } else if (bodySchema instanceof ArrayModel) {
            final Property items = ((ArrayModel) bodySchema).getItems();

            if (items instanceof RefProperty) {
                bodySchemaToUse = dereference((RefProperty) items, swagger);
            } else {
                bodySchemaToUse = new ModelImpl();
                final String name = nameOrDefault(items, "array");
                bodySchemaToUse.name(name);
                bodySchemaToUse.addProperty(name, items);

            }
        } else {
            bodySchemaToUse = (ModelImpl) bodySchema;
        }

        final String targetNamespace = xmlTargetNamespaceOrNull(bodySchemaToUse);

        final Element schema = newXmlSchema(targetNamespace);

        final Element bodyElement = addElement(schema, "element");
        bodyElement.addAttribute("name", nameOf(bodySchemaToUse));

        final Element complexBody = addElement(bodyElement, "complexType");
        final Element bodySequence = addElement(complexBody, "sequence");

        defineElementPropertiesOf(bodySequence, bodySchemaToUse, swagger, moreSchemas);

        defineAttributePropertiesOf(complexBody, bodySchemaToUse);

        return schema;
    }

    private static Element createResponseBodySchema(final Swagger swagger, final Operation operation,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Optional<Response> maybeResponse = findResponse(operation);

        if (!maybeResponse.isPresent()) {
            return null;
        }

        final Response body = maybeResponse.get();

        final Property bodySchema = body.getSchema();
        if (bodySchema instanceof RefProperty) {
            return defineComplexElement((RefProperty) bodySchema, null, swagger, moreSchemas);
        } else if (bodySchema instanceof ArrayProperty) {
            final ArrayProperty array = (ArrayProperty) bodySchema;

            final String targetNamespace = xmlTargetNamespaceOrNull(array);
            final Element schema = newXmlSchema(targetNamespace);

            defineElementProperty(ofNullable(array.getName()).orElse("array"), array, schema, swagger, moreSchemas);

            return schema;
        } else {
            throw new IllegalArgumentException("Unsupported response schema type: " + bodySchema);
        }
    }

    private static void defineAttributePropertiesOf(final Element parent, final ModelImpl model) {
        final Map<String, Property> properties = model.getProperties();

        for (final Entry<String, Property> propertyEntry : properties.entrySet()) {
            final String propertyName = propertyEntry.getKey();
            final Property property = propertyEntry.getValue();

            if (isAttribute(property)) {
                defineAttributeProperty(propertyName, property, parent);
            }
        }
    }

    private static void defineAttributeProperty(final String propertyName, final Property property, final Element parent) {
        final String type = property.getType();

        final Element propertyElement = addElement(parent, "attribute");
        propertyElement.addAttribute("name", requireNonNull(propertyName, "missing property name"));

        propertyElement.addAttribute("type", toXsdType(type));
    }

    private static Element defineComplexElement(final RefProperty property, final Element parent, final Swagger swagger,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final ModelImpl model = dereference(property, swagger);

        Element ret;
        Element elementToDeclareIn;

        final String namespace = xmlTargetNamespaceOrNull(model);
        final String name = nameOf(model);

        if (namespace != null && parent != null) {
            // this is element that could be in a (possibly) previously unknown
            // namespace
            final SchemaPrefixAndElement schemaPrefixAndElement = moreSchemas.computeIfAbsent(namespace, n -> {
                return new SchemaPrefixAndElement("p" + moreSchemas.size(), newXmlSchema(n));
            });

            elementToDeclareIn = addElement(schemaPrefixAndElement.schema, "element");
            elementToDeclareIn.addAttribute("name", name);

            ret = addElement(parent, "element");
            ret.addAttribute("ref", schemaPrefixAndElement.prefix + ":" + name);
            ret.addNamespace(schemaPrefixAndElement.prefix, namespace);
        } else {
            if (parent == null) {
                // this is the top level element (in a new namespace)
                ret = newXmlSchema(namespace);
                elementToDeclareIn = addElement(ret, "element");
                elementToDeclareIn.addAttribute("name", name);
            } else {
                // this is a nested element in the same namespace
                ret = addElement(parent, "element");
                ret.addAttribute("name", name);

                elementToDeclareIn = ret;
            }
        }

        final Element complex = addElement(elementToDeclareIn, "complexType");
        final Element sequence = addElement(complex, "sequence");

        defineElementPropertiesOf(sequence, model, swagger, moreSchemas);

        defineAttributePropertiesOf(complex, model);

        return ret;
    }

    private static void defineElementPropertiesOf(final Element parent, final Model model, final Swagger swagger,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Map<String, Property> properties = model.getProperties();

        for (final Entry<String, Property> propertyEntry : properties.entrySet()) {
            final String propertyName = propertyEntry.getKey();
            final Property property = propertyEntry.getValue();

            if (isElement(property)) {
                defineElementProperty(propertyName, property, parent, swagger, moreSchemas);
            }
        }
    }

    private static void defineElementProperty(final String propertyName, final Property property, final Element parent,
        final Swagger swagger, final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final String type = property.getType();

        switch (type) {
        case "ref":
            defineComplexElement((RefProperty) property, parent, swagger, moreSchemas);
            break;
        case "array":
            defineArrayElement(property, propertyName, parent, swagger, moreSchemas);
            break;
        default:
            final Element propertyElement = addElement(parent, "element");
            propertyElement.addAttribute("name", requireNonNull(propertyName, "missing property name"));

            propertyElement.addAttribute("type", toXsdType(type));
            break;
        }
    }

}
