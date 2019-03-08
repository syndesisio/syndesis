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
package io.syndesis.server.api.generator.swagger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.openapi.OpenApiHelper;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
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

@RunWith(Parameterized.class)
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

    @Parameter(0)
    public ObjectNode json;

    @Parameter(2)
    public Operation operation;

    @Parameter(3)
    public String specification;

    @Parameter(1)
    public Swagger swagger;

    private final UnifiedXmlDataShapeGenerator generator = new UnifiedXmlDataShapeGenerator();

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

    @Test
    public void shouldGenerateValidInputSchemasets() {
        final DataShape input = generator.createShapeFromRequest(json, swagger, operation);

        if (input.getKind() != DataShapeKinds.XML_SCHEMA) {
            return;
        }

        final String inputSpecification = input.getSpecification();
        final ValidationResult result = VALIDATOR.validateInstance(source(inputSpecification));
        assertThat(result.isValid())//
            .as("Non valid input XML schemaset was generated for specification: %s, operation: %s, errors: %s", specification,
                operation.getOperationId(),
                StreamSupport.stream(result.getProblems().spliterator(), false).map(ValidationProblem::toString)//
                    .collect(Collectors.joining("\n")))//
            .isTrue();
    }

    @Test
    public void shouldGenerateValidOutputSchemasets() throws IOException {
        final DataShape output = generator.createShapeFromResponse(json, swagger, operation);

        if (output.getKind() != DataShapeKinds.XML_SCHEMA) {
            return;
        }

        final Validator validator = createValidator();
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/swagger/atlas-xml-schemaset-model-v2.xsd")) {
            validator.setSchemaSource(new StreamSource(in));
            final String outputSpecification = output.getSpecification();
            final ValidationResult result = validator.validateInstance(source(outputSpecification));
            assertThat(result.isValid())//
                .as("Non valid output XML schemaset was generated for specification: %s, operation: %s, errors: %s", specification,
                    operation.getOperationId(),
                    StreamSupport.stream(result.getProblems().spliterator(), false).map(ValidationProblem::toString)//
                        .collect(Collectors.joining("\n")))//
                .isTrue();
        }
    }

    public static Validator createValidator() {

        final Schema schema;
        try {
            schema = SCHEMA_FACTORY.newSchema(UnifiedXmlDataShapeGenerator.class.getResource("/swagger/atlas-xml-schemaset-model-v2.xsd"));
        } catch (final SAXException e) {
            throw new ExceptionInInitializerError(e);
        }

        final Validator validator = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI, new FixedSchemaValidator(schema));

        validator.setSchemaSource(CACHED_SCHEMA);

        return validator;
    }

    @Parameters
    public static Iterable<Object[]> specifications() {
        final List<String> specifications = Collections.singletonList("/swagger/petstore.swagger.json");

        final List<Object[]> parameters = new ArrayList<>();

        specifications.forEach(specification -> {
            final String specificationContent;
            try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream(specification)) {
                specificationContent = IOUtils.toString(in, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new AssertionError("Unable to load swagger specification in path: " + specification, e);
            }

            ObjectNode json;
            try {
                json = (ObjectNode) Json.reader().readTree(specificationContent);
            } catch (final IOException e) {
                throw new AssertionError("Unable to parse swagger specification in path as JSON: " + specification, e);
            }
            final Swagger swagger = OpenApiHelper.parse(specificationContent);

            swagger.getPaths().forEach((path, operations) -> {
                operations.getOperationMap().forEach((method, operation) -> {
                    final Optional<BodyParameter> bodyParameter = BaseDataShapeGenerator.findBodyParameter(operation);
                    if (!bodyParameter.isPresent()) {
                        // by default we resort to JSON for payloads without
                        // body, i.e.
                        // only parameters
                        return;
                    }

                    parameters.add(new Object[] {json, swagger, operation, specification});
                });
            });
        });

        return parameters;
    }

    private static StreamSource source(final String xml) {
        return new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
