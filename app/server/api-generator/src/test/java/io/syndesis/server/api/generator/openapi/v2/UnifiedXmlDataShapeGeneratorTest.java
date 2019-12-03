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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.UnifiedXmlDataShapeSupport;
import io.syndesis.server.api.generator.openapi.util.XmlSchemaHelper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class UnifiedXmlDataShapeGeneratorTest {

    private static final Map<String, UnifiedXmlDataShapeSupport.SchemaPrefixAndElement> NO_MORE_SCHEMAS = null;

    private static final Oas20Document NO_OPEN_API_DOC = null;

    @Parameter(3)
    public String arrayElementName;

    @Parameter(2)
    public String arrayItemName;

    @Parameter(0)
    public String jsonSchemaSnippet;

    @Parameter(1)
    public String xmlSchemaSnippet;

    @Test
    public void shouldCreateArrayFromExamples() throws IOException {
        final Map<String, Oas20Schema> namedPropertyMap = propertyFrom(jsonSchemaSnippet);
        final Entry<String, Oas20Schema> namedProperty = namedPropertyMap.entrySet().iterator().next();

        final String propertyName = namedProperty.getKey();
        final Oas20Schema array = namedProperty.getValue();

        final Document document = DocumentHelper.createDocument();
        final Element parent = document.addElement("xsd:sequence", XmlSchemaHelper.XML_SCHEMA_NS);


        assertThat(UnifiedXmlDataShapeSupport.determineArrayItemName(propertyName, array)).isEqualTo(arrayItemName);
        assertThat(UnifiedXmlDataShapeSupport.determineArrayElementName(propertyName, array)).isEqualTo(arrayElementName);

        UnifiedXmlDataShapeGenerator unifiedXmlDataShapeGenerator = new UnifiedXmlDataShapeGenerator();
        unifiedXmlDataShapeGenerator.defineArrayElement(array, propertyName, parent, NO_OPEN_API_DOC, NO_MORE_SCHEMAS);
        assertThat(XmlSchemaHelper.serialize(document)).isXmlEqualTo(schema(xmlSchemaSnippet));
    }

    @Parameters
    public static Iterable<Object[]> examples() {
        return Arrays.<Object[]>asList(
            new Object[] {
                "{\n" + //
                    "\"photoUrls\": {\n" + //
                    "          \"type\": \"array\",\n" + //
                    "          \"xml\": {\n" + //
                    "            \"name\": \"photoUrl\",\n" + //
                    "            \"wrapped\": true\n" + //
                    "          },\n" + //
                    "          \"items\": {\n" + //
                    "            \"type\": \"string\"\n" + //
                    "          }\n" + //
                    "        }" + //
                    "}",
                "<xsd:element name=\"photoUrl\"><xsd:complexType><xsd:sequence><xsd:element name=\"photoUrl\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element>",
                "photoUrl", "photoUrl"},
            new Object[] {
                "{\n" + //
                    "  \"animals\": {\n" + //
                    "    \"type\": \"array\",\n" + //
                    "    \"items\": {\n" + //
                    "      \"type\": \"string\",\n" + //
                    "      \"xml\": {\n" + //
                    "        \"name\": \"animal\"\n" + //
                    "      }\n" + //
                    "    }\n" + //
                    "  }\n" + //
                    "}",
                "<xsd:element name=\"animal\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"xsd:string\"/>", "animal",
                null},
            new Object[] {"{\n" + //
                "  \"animals\": {\n" + //
                "    \"type\": \"array\",\n" + //
                "    \"items\": {\n" + //
                "      \"type\": \"string\",\n" + //
                "      \"xml\": {\n" + //
                "        \"name\": \"animal\"\n" + //
                "      }\n" + //
                "    },\n" + //
                "    \"xml\": {\n" + //
                "      \"name\": \"aliens\"\n" + //
                "    }\n" + //
                "  }\n" + //
                "}", "<xsd:element name=\"animal\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"xsd:string\"/>", "animal", null},
            new Object[] {
                "{\n" + //
                    "  \"animals\": {\n" + //
                    "    \"type\": \"array\",\n" + //
                    "    \"items\": {\n" + //
                    "      \"type\": \"string\"\n" + //
                    "    },\n" + //
                    "    \"xml\": {\n" + //
                    "      \"wrapped\": true\n" + //
                    "    }\n" + //
                    "  }\n" + //
                    "}",
                "<xsd:element name=\"animals\"><xsd:complexType><xsd:sequence><xsd:element name=\"animals\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element>",
                "animals", "animals"});
    }

    private static Map<String, Oas20Schema> propertyFrom(final String json) throws IOException {
        final ObjectNode object = (ObjectNode) JsonUtils.reader().readTree(json);

        final String propertyName = object.fieldNames().next();

        final JsonNode node = object.elements().next();

        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.paths = openApiDoc.createPaths();
        Oas20PathItem pathItem = (Oas20PathItem) openApiDoc.paths.createPathItem("/foo");

        final Oas20Schema schema = (Oas20Schema) Library.readNode(node, pathItem.createParameter().createSchema());
        return Collections.singletonMap(propertyName, schema);
    }

    private static String schema(final String xml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsd:sequence xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + xml
            + "</xsd:sequence>";
    }
}
