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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;

import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for {@link XmlSchemaExtractor}.
 */
public abstract class AbstractXmlSchemaExtractorTest {

    public static final String TEST_SCHEMA = "/soap/parser/test-schema.xsd";
    protected static SchemaCollection sourceSchemas;
    protected static Map<String, List<Element>> sourceSchemaNodes;

    protected XmlSchemaExtractor xmlSchemaExtractor;

    @BeforeAll
    public static void setupClass() throws XmlSchemaSerializer.XmlSchemaSerializerException {
        if (sourceSchemas == null) {
            final InputSource inputSource = new InputSource(AbstractXmlSchemaExtractorTest.class
                .getResourceAsStream(TEST_SCHEMA));
            final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
            schemaCollection.read(inputSource);
            sourceSchemas = new SchemaCollection(schemaCollection);

            sourceSchemaNodes = new HashMap<>();
            for (XmlSchema schema : sourceSchemas.getXmlSchemas()) {
                final NodeList childNodes = schema.getSchemaDocument().getDocumentElement().getChildNodes();
                List<Element> elements = new ArrayList<>();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    final Node node = childNodes.item(i);
                    if (node instanceof Element) {
                        elements.add((Element) node);
                    }
                }
                sourceSchemaNodes.put(schema.getTargetNamespace(), elements);
            }
        }
    }

    @BeforeEach
    public void setup() {
        final SchemaCollection targetSchemas = new SchemaCollection();
        xmlSchemaExtractor = new XmlSchemaExtractor(targetSchemas, sourceSchemas);
    }

    protected void validateTargetSchemas(XmlSchemaElement testElement) throws XmlSchemaSerializer.XmlSchemaSerializerException, IOException, SAXException {

        final String testNamespace = testElement.getQName().getNamespaceURI();
        final XmlSchema[] schemas = xmlSchemaExtractor.getTargetSchemas().getXmlSchemas();

        for (XmlSchema targetSchema : schemas) {
            final String targetNamespace = targetSchema.getTargetNamespace();
            // skip XSD and XML namespaces, which break in StaxUtils.toString below
            if (targetNamespace.equals(XMLConstants.XML_NS_URI) || targetNamespace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                continue;
            }
            final Document schemaDocument = targetSchema.getSchemaDocument();
            if (targetNamespace.isEmpty()) {
                // remove invalid xmlns:tns="" attribute or it breaks StaxUtils.toString below
                schemaDocument.getDocumentElement().removeAttribute("xmlns:tns");
            }
            final String schemaString = StaxUtils.toString(schemaDocument);

            // validate against schema XSD
            XmlSchemaTestHelper.validateSchema(schemaString);

            if (testNamespace.equals(targetNamespace)) {
                // must have element in test namespace
                assertThat(targetSchema.getElements()).isNotEmpty();
            }

            // all schemas must not be empty
            assertThat(targetSchema.getItems()).isNotEmpty();

            // check that all targetSchema types and elements are present in sourceSchema
            final List<Element> sourceNodes = sourceSchemaNodes.get(targetNamespace);
            if (sourceNodes == null) {
                if (!targetNamespace.equals(testElement.getQName().getNamespaceURI())) {
                    fail("Unexpected missing source schema " + targetNamespace);
                }
            } else {
                XmlSchemaTestHelper.checkSubsetSchema(schemaDocument, sourceNodes);
            }
        }
    }
}
