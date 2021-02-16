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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30PathItem;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.LocalResolver;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xmlunit.validation.JAXPValidator;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationProblem;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

public class UnifiedXmlDataShapeGeneratorShapeValidityTest {

    private static final Source CACHED_SCHEMA = new Source() {
        @Override
        public String getSystemId() {
            return "cached:schema";
        }

        @Override
        public void setSystemId(final String systemId) {
            // nop
        }
    };

    private static final SchemaFactory SCHEMA_FACTORY;

    private static final Validator VALIDATOR;

    static {
        SCHEMA_FACTORY = SchemaFactory.newInstance(Languages.W3C_XML_SCHEMA_NS_URI);
        SCHEMA_FACTORY.setResourceResolver(new LocalResolver());
        VALIDATOR = createValidator();
    }

    private static final UnifiedXmlDataShapeGenerator generator = new UnifiedXmlDataShapeGenerator();

    private static final class FixedSchemaValidator extends SchemaFactory {
        private final Schema schema;

        public FixedSchemaValidator(final Schema schema) {
            this.schema = schema;
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public LSResourceResolver getResourceResolver() {
            return new LocalResolver();
        }

        @Override
        public boolean isSchemaLanguageSupported(final String schemaLanguage) {
            return false;
        }

        @Override
        public Schema newSchema() throws SAXException {
            return schema;
        }

        @Override
        public Schema newSchema(final Source[] schemas) throws SAXException {
            return schema;
        }

        @Override
        public void setErrorHandler(final ErrorHandler errorHandler) {
            // nop
        }

        @Override
        public void setResourceResolver(final LSResourceResolver resourceResolver) {
            // nop
        }
    }

    @ParameterizedTest
    @MethodSource("specifications")
    public void shouldGenerateValidInputSchemasets(final ObjectNode json, final Oas30Document openApiDoc, final Oas30Operation operation,
        final String specification) {
        final DataShape input = generator.createShapeFromRequest(json, openApiDoc, operation);

        if (input.getKind() != DataShapeKinds.XML_SCHEMA) {
            return;
        }

        final String inputSpecification = input.getSpecification();
        final ValidationResult result = VALIDATOR.validateInstance(source(inputSpecification));
        assertThat(result.isValid())//
            .as("Non valid input XML schemaset was generated for specification: %s, operation: %s, errors: %s", specification,
                operation.operationId,
                StreamSupport.stream(result.getProblems().spliterator(), false).map(ValidationProblem::toString)//
                    .collect(Collectors.joining("\n")))//
            .isTrue();
    }

    @ParameterizedTest
    @MethodSource("specifications")
    public void shouldGenerateValidOutputSchemasets(final ObjectNode json, final Oas30Document openApiDoc, final Oas30Operation operation,
        final String specification) throws IOException {
        final DataShape output = generator.createShapeFromResponse(json, openApiDoc, operation);

        if (output.getKind() != DataShapeKinds.XML_SCHEMA) {
            return;
        }

        final Validator validator = createValidator();
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/openapi/v3/atlas-xml-schemaset-model-v2.xsd")) {
            validator.setSchemaSource(new StreamSource(in));
            final String outputSpecification = output.getSpecification();
            final ValidationResult result = validator.validateInstance(source(outputSpecification));
            assertThat(result.isValid())//
                .as("Non valid output XML schemaset was generated for specification: %s, operation: %s, errors: %s", specification,
                    operation.operationId,
                    StreamSupport.stream(result.getProblems().spliterator(), false).map(ValidationProblem::toString)//
                        .collect(Collectors.joining("\n")))//
                .isTrue();
        }
    }

    public static Validator createValidator() {

        final Schema schema;
        try {
            schema = SCHEMA_FACTORY.newSchema(UnifiedXmlDataShapeGenerator.class.getResource("/openapi/v3/atlas-xml-schemaset-model-v2.xsd"));
        } catch (final SAXException e) {
            throw new ExceptionInInitializerError(e);
        }

        final Validator validator = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI, new FixedSchemaValidator(schema));

        validator.setSchemaSource(CACHED_SCHEMA);

        return validator;
    }

    static Stream<Arguments> specifications() {
        final String specification;
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/openapi/v3/petstore.json")) {
            specification = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new AssertionError("Unable to load swagger specification", e);
        }

        final ObjectNode json;
        try {
            json = (ObjectNode) JsonUtils.reader().readTree(specification);
        } catch (final IOException e) {
            throw new AssertionError("Unable to parse swagger specification", e);
        }
        final Oas30Document openApiDoc = (Oas30Document) Library.readDocumentFromJSONString(specification);

        return openApiDoc.paths.getPathItems()
            .stream()
            .flatMap(pathItem -> fromPathItem(openApiDoc, json, specification, (Oas30PathItem) pathItem));
    }

    static Stream<Arguments> fromPathItem(final Oas30Document openApiDoc, final ObjectNode json, String specification, final Oas30PathItem pathItem) {
        return Oas30ModelHelper.getOperationMap(pathItem).values().stream().map(operation -> {
            final Optional<DataShapeGenerator.NameAndSchema> bodySchema = generator.findBodySchema(openApiDoc, operation);

            return bodySchema.map(x -> Arguments.of(json, openApiDoc, operation, specification));
        })
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    private static StreamSource source(final String xml) {
        return new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
