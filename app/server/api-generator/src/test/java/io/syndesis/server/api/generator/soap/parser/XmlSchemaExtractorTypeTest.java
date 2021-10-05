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
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link XmlSchemaExtractor}.
 */
public class XmlSchemaExtractorTypeTest extends AbstractXmlSchemaExtractorTest {

    static Stream<Arguments> extractTypeParameters() throws XmlSchemaSerializer.XmlSchemaSerializerException, IOException {
        if (sourceSchemas == null) {
            AbstractXmlSchemaExtractorTest.setupClass();
        }
        // extract all top level types from source schema
        return Arrays.stream(sourceSchemas.getXmlSchemas())
            .flatMap(s -> s.getSchemaTypes().values().stream())
            .map(t -> Arguments.of(t, t.getQName()));
    }

    @ParameterizedTest(name = "Type {1}")
    @MethodSource("extractTypeParameters")
    public void extractType(final XmlSchemaType type, final QName typeName) throws Exception {
        // create an element of type 'type' in the namespace ""
        final XmlSchemaElement testElement = xmlSchemaExtractor.extract(new QName("", typeName.getLocalPart()), type);
        xmlSchemaExtractor.copyObjects();

        validateTargetSchemas(testElement);
    }
}
