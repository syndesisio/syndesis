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

import io.swagger.models.ModelImpl;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

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

    public static boolean isAttribute(final Property property) {
        final Xml xml = property.getXml();
        if (xml == null) {
            return false;
        }

        final Boolean attribute = xml.getAttribute();

        return Boolean.TRUE.equals(attribute);
    }

    public static boolean isElement(final Property property) {
        final Xml xml = property.getXml();
        if (xml == null) {
            return true;
        }

        final Boolean attribute = xml.getAttribute();

        return attribute == null || Boolean.FALSE.equals(attribute);
    }

    public static String nameOf(final ModelImpl model) {
        return xmlNameOrDefault(model.getXml(), model.getName());
    }

    public static String nameOf(final Property property) {
        if (property instanceof RefProperty) {
            throw new IllegalArgumentException("Make sure that you dereference property, given: " + property);
        }

        return xmlNameOrDefault(property.getXml(), property.getName());
    }

    public static String nameOrDefault(final Property property, final String name) {
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

    public static boolean xmlIsWrapped(final ArrayProperty array) {
        if (array == null || array.getXml() == null) {
            return false;
        }

        return Boolean.TRUE.equals(array.getXml().getWrapped());
    }

    public static String xmlNameOrDefault(final Xml xml, final String defaultName) {
        if (xml == null || xml.getName() == null) {
            return defaultName;
        }

        return xml.getName();
    }

    public static String xmlTargetNamespaceOrNull(final ModelImpl model) {
        if (model == null || model.getXml() == null) {
            return null;
        }

        return model.getXml().getNamespace();
    }

    public static String xmlTargetNamespaceOrNull(final Property property) {
        if (property == null || property.getXml() == null) {
            return null;
        }

        return property.getXml().getNamespace();
    }

}
