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

import java.util.Arrays;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link XmlSchemaExtractor}.
 */
public class XmlSchemaExtractorElementTest extends AbstractXmlSchemaExtractorTest {

    static Stream<Arguments> parameters() throws XmlSchemaSerializer.XmlSchemaSerializerException {
        if (sourceSchemas == null) {
            AbstractXmlSchemaExtractorTest.setupClass();
        }
        // extract all top level elements from source schema
        return Arrays.stream(sourceSchemas.getXmlSchemas())
            .flatMap(s -> s.getElements().values().stream())
            .map(e -> Arguments.of(e, e.getQName()));
    }

    @ParameterizedTest(name = "Element {1}")
    @MethodSource("parameters")
    public void extractElement(final XmlSchemaElement element, QName name) throws Exception {
        final XmlSchemaElement testElement = xmlSchemaExtractor.extract(element);
        xmlSchemaExtractor.copyObjects();

        validateTargetSchemas(testElement);
    }
}
