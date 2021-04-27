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
package io.syndesis.server.api.generator.soap.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.camel.StringSource;
import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Helper class for XMLSchema testing.
 */
public final class XmlSchemaTestHelper {

    static final String NAME_ATTRIBUTE = "name";
    private static SchemaFactory schemaFactory;
    private static Validator validator;
    private static Validator schemaSetValidator;

    private XmlSchemaTestHelper() {
        // helper
    }

    public static void validateSchema(String schema) throws SAXException, IOException {
        getSchemaValidator().validate(new StringSource(schema));
    }

    private static Validator getSchemaValidator() throws SAXException {
        if (validator == null) {
            final InputStream inputStream = XmlSchemaTestHelper.class.getResourceAsStream("/xsd/2001/XMLSchema.xsd");
            final SchemaFactory schemaFactory = getSchemaFactory();
            schemaFactory.setResourceResolver((type, namespaceURI, publicId, systemId, baseURI) -> {
                final InputStream resourceStream = XmlSchemaTestHelper.class.getResourceAsStream("/xsd/" + systemId);
                if (resourceStream != null) {
                    return new LSInputImpl(publicId, systemId, resourceStream);
                }
                return null;
            });
            final Schema xsdSchema = schemaFactory.newSchema(new StreamSource(inputStream));
            validator = xsdSchema.newValidator();
        }
        return validator;
    }

    public static void validateSchemaSet(String schemaSet) throws SAXException, IOException {
        getSchemaSetValidator().validate(new StringSource(schemaSet));
    }

    private static Validator getSchemaSetValidator() throws SAXException, IOException {
        if (schemaSetValidator == null) {
            try (InputStream is = XmlSchemaTestHelper.class.getResourceAsStream("/openapi/v3/atlas-xml-schemaset-model-v2.xsd")) {
                final Schema xsdSchema = getSchemaFactory().newSchema(new StreamSource(is));
                schemaSetValidator = xsdSchema.newValidator();
            }
        }
        return schemaSetValidator;
    }

    private static SchemaFactory getSchemaFactory() {
        if (schemaFactory == null) {
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        }
        return schemaFactory;
    }

    public static void checkSubsetSchema(Document targetSchema, List<Element> sourceChildNodes) {
        final NodeList targetChildNodes = targetSchema.getDocumentElement().getChildNodes();
        for (int i = 0; i < targetChildNodes.getLength(); i++) {
            final Node node = targetChildNodes.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                compareMatchingElement(sourceChildNodes, element);
            }
        }
    }

    private static void compareMatchingElement(List<Element> sourceChildNodes, Element element) {
        final String type = element.getLocalName();
        final String name = element.getAttribute(NAME_ATTRIBUTE);
        boolean matchFound = false;
        for (Element sourceElement : sourceChildNodes) {
            if (type.equals(sourceElement.getLocalName()) &&
                name.equals(sourceElement.getAttribute(NAME_ATTRIBUTE))) {
                // compare matching element
                Diff diff = DiffBuilder.compare(Input.fromNode(sourceElement))
                    .withTest(Input.fromNode(sourceElement))
                    .ignoreComments()
                    .ignoreWhitespace()
                    .checkForIdentical()
                    .build();
                assertThat(diff.hasDifferences())
                    .overridingErrorMessage("Schema differences " + diff.toString())
                    .isFalse();
                matchFound = true;
            }
        }

        if (!matchFound) {
            fail(String.format("Missing source element %s[name=%s]", type, name));
        }
    }

}
