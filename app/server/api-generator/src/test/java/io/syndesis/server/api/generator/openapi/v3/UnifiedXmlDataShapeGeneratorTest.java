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
package io.syndesis.server.api.generator.openapi.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30PathItem;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.UnifiedXmlDataShapeSupport;
import io.syndesis.server.api.generator.openapi.util.XmlSchemaHelper;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UnifiedXmlDataShapeGeneratorTest {

    private static final Map<String, UnifiedXmlDataShapeSupport.SchemaPrefixAndElement> NO_MORE_SCHEMAS = null;

    private static final Oas30Document NO_OPEN_API_DOC = null;

    @ParameterizedTest
    @MethodSource("examples")
    public void shouldCreateArrayFromExamples(final String jsonSchemaSnippet, final String xmlSchemaSnippet, final String arrayItemName,
        final String arrayElementName) throws IOException {
        final Map<String, Oas30Schema> namedPropertyMap = propertyFrom(jsonSchemaSnippet);
        final Map.Entry<String, Oas30Schema> namedProperty = namedPropertyMap.entrySet().iterator().next();

        final String propertyName = namedProperty.getKey();
        final Oas30Schema array = namedProperty.getValue();

        final Document document = DocumentHelper.createDocument();
        final Element parent = document.addElement("xsd:sequence", XmlSchemaHelper.XML_SCHEMA_NS);

        assertThat(UnifiedXmlDataShapeSupport.determineArrayItemName(propertyName, array)).isEqualTo(arrayItemName);
        assertThat(UnifiedXmlDataShapeSupport.determineArrayElementName(propertyName, array)).isEqualTo(arrayElementName);

        UnifiedXmlDataShapeGenerator unifiedXmlDataShapeGenerator = new UnifiedXmlDataShapeGenerator();
        unifiedXmlDataShapeGenerator.defineArrayElement(array, propertyName, parent, NO_OPEN_API_DOC, NO_MORE_SCHEMAS);
        assertThat(XmlSchemaHelper.serialize(document)).isXmlEqualTo(schema(xmlSchemaSnippet));
    }

    static Stream<Arguments> examples() {
        return Stream.of(
            Arguments.of(
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
                "photoUrl", "photoUrl"),
            Arguments.of(
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
                null),
            Arguments.of("{\n" + //
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
                "}", "<xsd:element name=\"animal\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"xsd:string\"/>", "animal", null),
            Arguments.of(
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
                "animals", "animals"));
    }

    private static Map<String, Oas30Schema> propertyFrom(final String json) throws IOException {
        final ObjectNode object = (ObjectNode) JsonUtils.reader().readTree(json);

        final String propertyName = object.fieldNames().next();

        final JsonNode node = object.elements().next();

        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/foo");

        final Oas30Schema schema = (Oas30Schema) Library.readNode(node, pathItem.createParameter().createSchema());
        return Collections.singletonMap(propertyName, schema);
    }

    private static String schema(final String xml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsd:sequence xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + xml
            + "</xsd:sequence>";
    }
}
