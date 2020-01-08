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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link XmlSchemaExtractor}.
 */
public class XmlSchemaExtractorTypeTest extends AbstractXmlSchemaExtractorTest {

    @Parameterized.Parameter
    public XmlSchemaType type;

    @Parameterized.Parameter(1)
    public QName typeName;

    @Parameterized.Parameters(name = "Type {1}")
    public static List<Object[]> getTypes() throws XmlSchemaSerializer.XmlSchemaSerializerException {
        if (sourceSchemas == null) {
            AbstractXmlSchemaExtractorTest.setupClass();
        }
        // extract all top level types from source schema
        return Arrays.stream(sourceSchemas.getXmlSchemas())
            .flatMap(s -> s.getSchemaTypes().values().stream())
            .map(t -> new Object[]{t, t.getQName()})
            .collect(Collectors.toList());
    }

    @Test
    public void extractType() throws ParserException, XmlSchemaSerializer.XmlSchemaSerializerException, IOException, SAXException {
        // create an element of type 'type' in the namespace ""
        final XmlSchemaElement testElement = xmlSchemaExtractor.extract(new QName("", typeName.getLocalPart()), type);
        xmlSchemaExtractor.copyObjects();

        validateTargetSchemas(testElement);
    }
}
