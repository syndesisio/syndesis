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
import java.util.Collection;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link XmlSchemaExtractor}.
 */
public class XmlSchemaExtractorElementTest extends AbstractXmlSchemaExtractorTest {

    @Parameterized.Parameter
    public XmlSchemaElement element;

    @Parameterized.Parameter(1)
    public QName elementName;

    @Parameterized.Parameters(name = "Element {1}")
    public static Collection<Object[]> getElements() throws XmlSchemaSerializer.XmlSchemaSerializerException {
        if (sourceSchemas == null) {
            AbstractXmlSchemaExtractorTest.setupClass();
        }
        // extract all top level elements from source schema
        return Arrays.stream(sourceSchemas.getXmlSchemas())
            .flatMap(s -> s.getElements().values().stream())
            .map(e -> new Object[] {e, e.getQName()})
            .collect(Collectors.toList());
    }

    @Test
    public void extractElement() throws ParserException, XmlSchemaSerializer.XmlSchemaSerializerException, IOException, SAXException {
        final XmlSchemaElement testElement = xmlSchemaExtractor.extract(element);
        xmlSchemaExtractor.copyObjects();

        validateTargetSchemas(testElement);
    }
}
