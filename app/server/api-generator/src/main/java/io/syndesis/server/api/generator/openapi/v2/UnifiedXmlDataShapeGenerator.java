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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.models.OasXML;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Items;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinition;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
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
import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("PMD.GodClass")
class UnifiedXmlDataShapeGenerator implements Oas20DataShapeGenerator {

    private static final String SCHEMA_SET_NS = "http://atlasmap.io/xml/schemaset/v2";

    private static final String SYNDESIS_PARAMETERS_NS = "http://syndesis.io/v1/swagger-connector-template/parameters";

    private static final String SYNDESIS_REQUEST_NS = "http://syndesis.io/v1/swagger-connector-template/request";

    private static final Predicate<Oas20Response> RESPONSE_HAS_SCHEMA = response -> response.schema != null;

    static class SchemaPrefixAndElement {
        private final String prefix;

        private final Element schema;

        SchemaPrefixAndElement(final String prefix, final Element schema) {
            this.prefix = prefix;
            this.schema = schema;
        }
    }

    @Override
    public DataShape createShapeFromRequest(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
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
    public DataShape createShapeFromResponse(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        final Optional<Oas20Response> maybeResponse = OasModelHelper.findResponse(operation, RESPONSE_HAS_SCHEMA, Oas20Response.class);

        if (!maybeResponse.isPresent()) {
            return DATA_SHAPE_NONE;
        }

        final Document document = DocumentHelper.createDocument();

        final Element schemaSet = document.addElement("d:SchemaSet", SCHEMA_SET_NS);
        schemaSet.addNamespace(XmlSchemaHelper.XML_SCHEMA_PREFIX, XmlSchemaHelper.XML_SCHEMA_NS);

        final Map<String, SchemaPrefixAndElement> moreSchemas = new HashMap<>();

        final Element bodySchema = createResponseBodySchema(openApiDoc, maybeResponse.get().schema, moreSchemas);
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

    static void defineArrayElement(final Oas20Schema property, final String propertyName, final Element parent, final Oas20Document openApiDoc,
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

    static String determineArrayElementName(final String propertyName, final Oas20Schema array) {
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

    static String determineArrayItemName(final String propertyName, final Oas20Schema array) {
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

    private static void addEnumerationsTo(final Element element, final List<?> enums) {
        final Element simpleType = XmlSchemaHelper.addElement(element, "simpleType");
        final Element restriction = XmlSchemaHelper.addElement(simpleType, "restriction");
        restriction.addAttribute("base", element.attributeValue("type"));

        for (final Object enumValue : enums) {
            final Element enumeration = XmlSchemaHelper.addElement(restriction, "enumeration");
            enumeration.addAttribute("value", String.valueOf(enumValue));
        }
    }

    private static void addEnumsTo(final Element element, final Oas20Parameter serializableParameter) {
        if (serializableParameter.items != null) {
            final Oas20Items items = serializableParameter.items;

            List<String> enums = ofNullable(items.enum_).orElse(Collections.emptyList());
            if (!enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        } else {
            final List<String> enums = serializableParameter.enum_;

            if (enums != null && !enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        }
    }

    private static Element createParametersSchema(final Oas20Document openApiDoc, final Oas20Operation operation) {
        final List<Oas20Parameter> operationParameters = OasModelHelper.getParameters(operation, Oas20Parameter.class);

        OasPathItem parent = Optional.of(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas20Parameter> pathParameters = OasModelHelper.getParameters(parent, Oas20Parameter.class);
        operationParameters.addAll(pathParameters);

        final List<Oas20ParameterDefinition> globalParameters = ofNullable(openApiDoc.parameters)
            .map(Oas20ParameterDefinitions::getItems)
            .orElse(Collections.emptyList());
        operationParameters.addAll(globalParameters);

        final List<Oas20Parameter> serializableParameters = operationParameters.stream()
            .filter(p -> p.type != null)
            .filter(OasModelHelper::isSerializable)
            .collect(Collectors.toList());

        if (serializableParameters.isEmpty()) {
            return null;
        }

        final Element schema = XmlSchemaHelper.newXmlSchema(SYNDESIS_PARAMETERS_NS);

        final Element parameters = XmlSchemaHelper.addElement(schema, "element");
        parameters.addAttribute("name", "parameters");

        final Element complex = XmlSchemaHelper.addElement(parameters, "complexType");
        final Element sequence = XmlSchemaHelper.addElement(complex, "sequence");

        for (final Oas20Parameter serializableParameter : serializableParameters) {
            final String type = XmlSchemaHelper.toXsdType(serializableParameter.type);
            final String name = trimToNull(serializableParameter.getName());

            if ("file".equals(type)) {
                // 'file' type is not allowed in JSON schema
                continue;
            }

            final Element element = XmlSchemaHelper.addElement(sequence, "element");
            element.addAttribute("name", name);
            element.addAttribute("type", type);

            final Object defaultValue = serializableParameter.default_;
            if (defaultValue != null) {
                element.addAttribute("default", String.valueOf(defaultValue));
            }

            addEnumsTo(element, serializableParameter);
        }

        return schema;
    }

    private static Element createRequestBodySchema(final Oas20Document openApiDoc, final Oas20Operation operation,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Optional<OasParameter> bodyParameter = OasModelHelper.findBodyParameter(operation);

        if (!bodyParameter.isPresent()) {
            return null;
        }

        final OasParameter body = bodyParameter.get();

        final Oas20Schema bodySchema = (Oas20Schema) body.schema;

        final Oas20SchemaDefinition bodySchemaToUse;
        if (OasModelHelper.isReferenceType(bodySchema)) {
            bodySchemaToUse = Oas20ModelHelper.dereference(bodySchema, openApiDoc);
        } else if (OasModelHelper.isArrayType(bodySchema)) {
            final Oas20Schema items = (Oas20Schema) bodySchema.items;

            if (OasModelHelper.isReferenceType(items)) {
                bodySchemaToUse = Oas20ModelHelper.dereference(items, openApiDoc);
            } else {
                final String name = XmlSchemaHelper.nameOrDefault(items, "array");
                bodySchemaToUse = new Oas20SchemaDefinition(name);
                bodySchemaToUse.addProperty(name, items);

            }
        } else {
            bodySchemaToUse = (Oas20SchemaDefinition) bodySchema;
        }

        final String targetNamespace = XmlSchemaHelper.xmlTargetNamespaceOrNull(bodySchemaToUse);

        final Element schema = XmlSchemaHelper.newXmlSchema(targetNamespace);

        final Element bodyElement = XmlSchemaHelper.addElement(schema, "element");
        bodyElement.addAttribute("name", XmlSchemaHelper.xmlNameOrDefault(bodySchemaToUse.xml, bodySchemaToUse.getName()));

        final Element complexBody = XmlSchemaHelper.addElement(bodyElement, "complexType");
        final Element bodySequence = XmlSchemaHelper.addElement(complexBody, "sequence");

        defineElementPropertiesOf(bodySequence, bodySchemaToUse, openApiDoc, moreSchemas);

        defineAttributePropertiesOf(complexBody, bodySchemaToUse);

        return schema;
    }

    private static Element createResponseBodySchema(final Oas20Document openApiDoc, final Oas20Schema bodySchema,
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

    private static void defineAttributePropertiesOf(final Element parent, final Oas20SchemaDefinition model) {
        final Map<String, OasSchema> properties = model.properties;

        for (final Entry<String, OasSchema> propertyEntry : properties.entrySet()) {
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

    private static Element defineComplexElement(final OasSchema property, final Element parent, final Oas20Document openApiDoc,
        final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Oas20SchemaDefinition model = Oas20ModelHelper.dereference(property, openApiDoc);

        Element ret;
        Element elementToDeclareIn;

        final String namespace = XmlSchemaHelper.xmlTargetNamespaceOrNull(model);
        final String name = XmlSchemaHelper.xmlNameOrDefault(model.xml, model.getName());

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

    private static void defineElementPropertiesOf(final Element parent, final Oas20Schema model, final Oas20Document openApiDoc,
                                                  final Map<String, SchemaPrefixAndElement> moreSchemas) {
        final Map<String, OasSchema> properties = model.properties;

        for (final Entry<String, OasSchema> propertyEntry : properties.entrySet()) {
            final String propertyName = propertyEntry.getKey();
            final Oas20Schema property = (Oas20Schema) propertyEntry.getValue();

            if (XmlSchemaHelper.isElement(property)) {
                defineElementProperty(propertyName, property, parent, openApiDoc, moreSchemas);
            }
        }
    }

    private static void defineElementProperty(final String propertyName, final Oas20Schema property, final Element parent,
                                              final Oas20Document openApiDoc, final Map<String, SchemaPrefixAndElement> moreSchemas) {
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
