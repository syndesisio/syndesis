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
package io.syndesis.server.api.generator.swagger.util;

import java.io.IOException;
import java.io.StringWriter;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.models.OasXML;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public final class XmlSchemaHelper {

    public static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

    public static final String XML_SCHEMA_PREFIX = "xsd";

    private XmlSchemaHelper() {
        // utility class
    }

    public static Element addElement(final Element parent, final String name) {
        return parent.addElement(XML_SCHEMA_PREFIX + ":" + name, XML_SCHEMA_NS);
    }

    public static Element addElement(final Element parent, final String... names) {
        Element current = parent;
        for (final String name : names) {
            current = current.addElement(XML_SCHEMA_PREFIX + ":" + name, XML_SCHEMA_NS);
        }

        return current;
    }

    public static boolean isAttribute(final OasSchema property) {
        final OasXML xml = property.xml;
        if (xml == null) {
            return false;
        }

        final Boolean attribute = xml.attribute;

        return Boolean.TRUE.equals(attribute);
    }

    public static boolean isElement(final OasSchema property) {
        final OasXML xml = property.xml;
        if (xml == null) {
            return true;
        }

        final Boolean attribute = xml.attribute;

        return attribute == null || Boolean.FALSE.equals(attribute);
    }

    public static String nameOf(final Oas20SchemaDefinition model) {
        return xmlNameOrDefault(model.xml, model.getName());
    }

    public static String nameOf(final OasSchema property) {
        if (Oas20ModelHelper.isReferenceType(property)) {
            throw new IllegalArgumentException("Make sure that you dereference property, given: " + property);
        }

        return xmlNameOrDefault(property.xml, Oas20ModelHelper.getPropertyName(property));
    }

    public static String nameOrDefault(final OasSchema property, final String name) {
        final String determined = nameOf(property);
        if (determined == null) {
            return name;
        }

        return determined;
    }

    public static Element newXmlSchema(final String targetNamespace) {
        final Document document = DocumentHelper.createDocument();
        final Element schema = document.addElement(XML_SCHEMA_PREFIX + ":schema", XML_SCHEMA_NS);

        if (!StringUtils.isEmpty(targetNamespace)) {
            schema.addAttribute("targetNamespace", targetNamespace);
        }

        return schema;
    }

    public static String serialize(final Document document) {
        try (StringWriter out = new StringWriter()) {
            final OutputFormat format = new OutputFormat(null, false, "UTF-8");
            format.setExpandEmptyElements(false);
            format.setIndent(false);

            final XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            writer.flush();

            return out.toString();
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to serialize given document to XML", e);
        }
    }

    public static String toXsdType(final String type) {
        switch (type) {
        case "boolean":
            return XML_SCHEMA_PREFIX + ":boolean";
        case "number":
            return XML_SCHEMA_PREFIX + ":decimal";
        case "string":
            return XML_SCHEMA_PREFIX + ":string";
        case "integer":
            return XML_SCHEMA_PREFIX + ":integer";
        default:
            throw new IllegalArgumentException("Unexpected type `" + type + "` given to convert to XSD type");
        }
    }

    public static boolean xmlIsWrapped(final OasSchema array) {
        if (array == null || array.xml == null) {
            return false;
        }

        return Boolean.TRUE.equals(array.xml.wrapped);
    }

    public static String xmlNameOrDefault(final OasXML xml, final String defaultName) {
        if (xml == null || xml.name == null) {
            return defaultName;
        }

        return xml.name;
    }

    public static String xmlTargetNamespaceOrNull(final Oas20SchemaDefinition model) {
        if (model == null || model.xml == null) {
            return null;
        }

        return model.xml.namespace;
    }

    public static String xmlTargetNamespaceOrNull(final OasSchema property) {
        if (property == null || property.xml == null) {
            return null;
        }

        return property.xml.namespace;
    }

}
