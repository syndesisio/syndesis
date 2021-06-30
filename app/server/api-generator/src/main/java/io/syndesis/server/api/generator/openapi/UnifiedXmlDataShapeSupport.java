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

package io.syndesis.server.api.generator.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.models.OasXML;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import io.syndesis.server.api.generator.openapi.util.XmlSchemaHelper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@SuppressWarnings("PMD.GodClass")
public abstract class UnifiedXmlDataShapeSupport<T extends OasDocument, O extends OasOperation, R extends OasResponse> implements DataShapeGenerator<T, O> {

    protected static final String SCHEMA_SET_NS = "http://atlasmap.io/xml/schemaset/v2";

    protected static final String SYNDESIS_PARAMETERS_NS = "http://syndesis.io/v1/swagger-connector-template/parameters";

    protected static final String SYNDESIS_REQUEST_NS = "http://syndesis.io/v1/swagger-connector-template/request";

    public static class SchemaPrefixAndElement {
        public final String prefix;

        public final Element schema;

        SchemaPrefixAndElement(final String prefix, final Element schema) {
            this.prefix = prefix;
            this.schema = schema;
        }
    }

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final T openApiDoc, final O operation) {
        final Document document = DocumentHelper.createDocument();

        final Element schemaSet = document.addElement("d:SchemaSet", SCHEMA_SET_NS);
        schemaSet.addNamespace(XmlSchemaHelper.XML_SCHEMA_PREFIX, XmlSchemaHelper.XML_SCHEMA_NS);

        final Element schema = XmlSchemaHelper.addElement(schemaSet, "schema");
        schema.addAttribute("targetNamespace", SYNDESIS_REQUEST_NS);
        schema.addAttribute("elementFormDefault", "qualified");

        final Element parametersSchema = createParametersSchema(openApiDoc, operation);

        final Map<String, SchemaPrefixAndElement> moreSchemas = new HashMap<>();

        final Element bodySchema = createRequestBodySchema(openApiDoc, operation, moreSchemas);

        if (bodySchema == null && parametersSchema == null) {
            return DATA_SHAPE_NONE;
        }

        final Element element = XmlSchemaHelper.addElement(schema, "element");
        element.addAttribute("name", "request");
        final Element sequence = XmlSchemaHelper.addElement(element, "complexType", "sequence");

        final Element additionalSchemas = schemaSet.addElement("d:AdditionalSchemas");

        if (parametersSchema != null) {
            final Element parameters = XmlSchemaHelper.addElement(sequence, "element");
            parameters.addNamespace("p", SYNDESIS_PARAMETERS_NS);
            parameters.addAttribute("ref", "p:parameters");

            additionalSchemas.add(parametersSchema.detach());
        }

        if (bodySchema != null) {
            final Element bodyElement = XmlSchemaHelper.addElement(sequence, "element");
            bodyElement.addAttribute("name", "body");

            final Element body = XmlSchemaHelper.addElement(bodyElement, "complexType", "sequence", "element");
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

        final String xmlSchemaSet = XmlSchemaHelper.serialize(document);

        return new DataShape.Builder()//
            .kind(DataShapeKinds.XML_SCHEMA)//
            .name("Request")//
            .description("API request payload")//
            .specification(xmlSchemaSet)//
            .putMetadata(DataShapeMetaData.UNIFIED, "true")
            .build();
    }

    @Override
    public DataShape createShapeFromResponse(final ObjectNode json, final T openApiDoc, final O operation) {
        final Optional<R> maybeResponse = findResponse(openApiDoc, operation, hasSchema(), getResponseType());

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Document document = DocumentHelper.createDocument();

        final Element schemaSet = document.addElement("d:SchemaSet", SCHEMA_SET_NS);
        schemaSet.addNamespace(XmlSchemaHelper.XML_SCHEMA_PREFIX, XmlSchemaHelper.XML_SCHEMA_NS);

        final Map<String, SchemaPrefixAndElement> moreSchemas = new HashMap<>();

        final Element bodySchema = createResponseBodySchema(openApiDoc, getSchema(maybeResponse.get()), moreSchemas);
        if (bodySchema == null) {
            return DATA_SHAPE_NONE;
        }

        schemaSet.add(bodySchema.detach());

        if (!moreSchemas.isEmpty()) {
            final Element additionalSchemas = schemaSet.addElement("d:AdditionalSchemas");
            moreSchemas.values().forEach(e -> additionalSchemas.add(e.schema.detach()));
        }

        final String xmlSchemaSet = XmlSchemaHelper.serialize(document);

        return new DataShape.Builder()//
            .name("Response")//
            .description("API response payload")//
            .kind(DataShapeKinds.XML_SCHEMA)//
            .specification(xmlSchemaSet)//
            .putMetadata(DataShapeMetaData.UNIFIED, "true")
            .build();
    }

    private Element createRequestBodySchema(final T openApiDoc, final O operation,
                                              final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Optional<NameAndSchema> maybeBodySchema = findBodySchema(openApiDoc, operation);

        if (!maybeBodySchema.isPresent()) {
            return null;
        }

        final OasSchema bodySchema = maybeBodySchema.get().schema;

        final OasSchema bodySchemaToUse;
        if (OasModelHelper.isReferenceType(bodySchema)) {
            bodySchemaToUse = dereference(bodySchema, openApiDoc);
        } else if (OasModelHelper.isArrayType(bodySchema)) {
            final OasSchema items = (OasSchema) bodySchema.items;

            if (OasModelHelper.isReferenceType(items)) {
                bodySchemaToUse = dereference(items, openApiDoc);
            } else {
                final String name = XmlSchemaHelper.nameOrDefault(items, "array");
                bodySchemaToUse = createSchemaDefinition(name);
                bodySchemaToUse.addProperty(name, items);

            }
        } else {
            bodySchemaToUse = bodySchema;
        }

        final String targetNamespace = XmlSchemaHelper.xmlTargetNamespaceOrNull(bodySchemaToUse);

        final Element schema = XmlSchemaHelper.newXmlSchema(targetNamespace);

        final Element bodyElement = XmlSchemaHelper.addElement(schema, "element");
        bodyElement.addAttribute("name", XmlSchemaHelper.xmlNameOrDefault(bodySchemaToUse.xml, getName(bodySchemaToUse)));

        final Element complexBody = XmlSchemaHelper.addElement(bodyElement, "complexType");
        final Element bodySequence = XmlSchemaHelper.addElement(complexBody, "sequence");

        defineElementPropertiesOf(bodySequence, bodySchemaToUse, openApiDoc, moreSchemas);

        defineAttributePropertiesOf(complexBody, bodySchemaToUse);

        return schema;
    }

    protected abstract Class<R> getResponseType();

    protected abstract Predicate<R> hasSchema();

    protected abstract OasSchema getSchema(R response);

    protected abstract OasSchema createSchemaDefinition(String name);

    protected abstract Element createParametersSchema(T openApiDoc, O operation);

    protected abstract OasSchema dereference(OasSchema property, T openApiDoc);

    protected abstract String getName(OasSchema schema);

    public void defineArrayElement(final OasSchema property, final String propertyName, final Element parent, final T openApiDoc,
                                   final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Element sequence;
        final OasXML arrayXml = property.xml;
        if (arrayXml != null && Boolean.TRUE.equals(arrayXml.wrapped)) {
            final String arrayElementName = determineArrayElementName(propertyName, property);

            final Element arrayElement = XmlSchemaHelper.addElement(parent, "element");
            arrayElement.addAttribute("name", requireNonNull(arrayElementName, "missing array property name"));

            final Element arrayComplex = XmlSchemaHelper.addElement(arrayElement, "complexType");
            sequence = XmlSchemaHelper.addElement(arrayComplex, "sequence");
        } else {
            sequence = parent;
        }

        final OasSchema items = (OasSchema) property.items;

        final Element itemsElement;
        if (OasModelHelper.isReferenceType(items)) {
            itemsElement = defineComplexElement(items, sequence, openApiDoc, moreSchemas);
        } else {
            itemsElement = XmlSchemaHelper.addElement(sequence, "element");
            itemsElement.addAttribute("name", determineArrayItemName(propertyName, property));
            itemsElement.addAttribute("type", XmlSchemaHelper.toXsdType(items.type));
        }

        if (property.maxItems == null) {
            itemsElement.addAttribute("maxOccurs", "unbounded");
        } else {
            itemsElement.addAttribute("maxOccurs", String.valueOf(property.maxItems));
        }

        if (property.minItems == null) {
            itemsElement.addAttribute("minOccurs", "0");
        } else {
            itemsElement.addAttribute("minOccurs", String.valueOf(property.minItems));
        }
    }

    public static String determineArrayElementName(final String propertyName, final OasSchema array) {
        final OasXML xml = array.xml;

        if (xml == null || xml.wrapped == null || Boolean.FALSE.equals(xml.wrapped)) {
            return null;
        }

        final String xmlName = xml.name;
        if (xmlName != null) {
            return xmlName;
        }

        return propertyName;
    }

    public static String determineArrayItemName(final String propertyName, final OasSchema array) {
        final OasSchema items = (OasSchema) array.items;

        final OasXML itemXml = items.xml;
        if (itemXml != null && itemXml.name != null) {
            return itemXml.name;
        }

        final OasXML xml = array.xml;
        if (xml != null && xml.name != null) {
            return xml.name;
        }

        return propertyName;
    }

    protected static void addEnumerationsTo(final Element element, final List<?> enums) {
        final Element simpleType = XmlSchemaHelper.addElement(element, "simpleType");
        final Element restriction = XmlSchemaHelper.addElement(simpleType, "restriction");
        restriction.addAttribute("base", element.attributeValue("type"));

        for (final Object enumValue : enums) {
            final Element enumeration = XmlSchemaHelper.addElement(restriction, "enumeration");
            enumeration.addAttribute("value", String.valueOf(enumValue));
        }
    }

    protected Element createResponseBodySchema(final T openApiDoc, final OasSchema bodySchema,
                                                    final Map<String, SchemaPrefixAndElement> moreSchemas) {
        if (OasModelHelper.isReferenceType(bodySchema)) {
            return defineComplexElement(bodySchema, null, openApiDoc, moreSchemas);
        } else if (OasModelHelper.isArrayType(bodySchema)) {
            final String targetNamespace = XmlSchemaHelper.xmlTargetNamespaceOrNull(bodySchema);
            final Element schema = XmlSchemaHelper.newXmlSchema(targetNamespace);

            OasSchema propertySchema = ofNullable(bodySchema.items).map(OasSchema.class::cast).orElse(null);
            defineElementProperty(OasModelHelper.getPropertyName(propertySchema, "array"), bodySchema, schema, openApiDoc, moreSchemas);

            return schema;
        } else {
            throw new IllegalArgumentException("Unsupported response schema type: " + bodySchema);
        }
    }

    protected static void defineAttributePropertiesOf(final Element parent, final OasSchema model) {
        final Map<String, OasSchema> properties = model.properties;

        for (final Map.Entry<String, OasSchema> propertyEntry : properties.entrySet()) {
            final String propertyName = propertyEntry.getKey();
            final OasSchema property = propertyEntry.getValue();

            if (XmlSchemaHelper.isAttribute(property)) {
                defineAttributeProperty(propertyName, property, parent);
            }
        }
    }

    private static void defineAttributeProperty(final String propertyName, final OasSchema property, final Element parent) {
        final String type = property.type;

        final Element propertyElement = XmlSchemaHelper.addElement(parent, "attribute");
        propertyElement.addAttribute("name", requireNonNull(propertyName, "missing property name"));

        propertyElement.addAttribute("type", XmlSchemaHelper.toXsdType(type));
    }

    private Element defineComplexElement(final OasSchema property, final Element parent, final T openApiDoc,
                                                final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final OasSchema model = dereference(property, openApiDoc);

        Element ret;
        Element elementToDeclareIn;

        final String namespace = XmlSchemaHelper.xmlTargetNamespaceOrNull(model);
        final String name = XmlSchemaHelper.xmlNameOrDefault(model.xml, getName(model));

        if (namespace != null && parent != null) {
            // this is element that could be in a (possibly) previously unknown
            // namespace
            final SchemaPrefixAndElement schemaPrefixAndElement = moreSchemas.computeIfAbsent(namespace, n -> {
                return new SchemaPrefixAndElement("p" + moreSchemas.size(), XmlSchemaHelper.newXmlSchema(n));
            });

            elementToDeclareIn = XmlSchemaHelper.addElement(schemaPrefixAndElement.schema, "element");
            elementToDeclareIn.addAttribute("name", name);

            ret = XmlSchemaHelper.addElement(parent, "element");
            ret.addAttribute("ref", schemaPrefixAndElement.prefix + ":" + name);
            ret.addNamespace(schemaPrefixAndElement.prefix, namespace);
        } else {
            if (parent == null) {
                // this is the top level element (in a new namespace)
                ret = XmlSchemaHelper.newXmlSchema(namespace);
                elementToDeclareIn = XmlSchemaHelper.addElement(ret, "element");
                elementToDeclareIn.addAttribute("name", name);
            } else {
                // this is a nested element in the same namespace
                ret = XmlSchemaHelper.addElement(parent, "element");
                ret.addAttribute("name", name);

                elementToDeclareIn = ret;
            }
        }

        final Element complex = XmlSchemaHelper.addElement(elementToDeclareIn, "complexType");
        final Element sequence = XmlSchemaHelper.addElement(complex, "sequence");

        defineElementPropertiesOf(sequence, model, openApiDoc, moreSchemas);

        defineAttributePropertiesOf(complex, model);

        return ret;
    }

    protected void defineElementPropertiesOf(final Element parent, final OasSchema model, final T openApiDoc,
                                                  final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Map<String, OasSchema> properties = model.properties;

        for (final Map.Entry<String, OasSchema> propertyEntry : properties.entrySet()) {
            final String propertyName = propertyEntry.getKey();
            final OasSchema property = propertyEntry.getValue();

            if (XmlSchemaHelper.isElement(property)) {
                defineElementProperty(propertyName, property, parent, openApiDoc, moreSchemas);
            }
        }
    }

    private void defineElementProperty(final String propertyName, final OasSchema property, final Element parent,
                                              final T openApiDoc, final Map<String, SchemaPrefixAndElement> moreSchemas) {
        if (OasModelHelper.isReferenceType(property)) {
            defineComplexElement(property, parent, openApiDoc, moreSchemas);
        } else if (OasModelHelper.isArrayType(property)) {
            defineArrayElement(property, propertyName, parent, openApiDoc, moreSchemas);
        } else {
            final Element propertyElement = XmlSchemaHelper.addElement(parent, "element");
            propertyElement.addAttribute("name", requireNonNull(propertyName, "missing property name"));
            propertyElement.addAttribute("type", XmlSchemaHelper.toXsdType(property.type));
        }
    }

}
