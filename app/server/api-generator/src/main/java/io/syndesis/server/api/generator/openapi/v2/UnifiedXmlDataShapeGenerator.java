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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Items;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.UnifiedXmlDataShapeSupport;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import io.syndesis.server.api.generator.openapi.util.XmlSchemaHelper;
import org.dom4j.Element;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

class UnifiedXmlDataShapeGenerator extends UnifiedXmlDataShapeSupport<Oas20Document, Oas20Operation, Oas20Response> implements DataShapeGenerator<Oas20Document, Oas20Operation> {

    @Override
    protected OasSchema dereference(OasSchema property, Oas20Document openApiDoc) {
        return Oas20ModelHelper.dereference(property, openApiDoc);
    }

    @Override
    protected String getName(OasSchema schema) {
        if (schema instanceof Oas20SchemaDefinition) {
            return ((Oas20SchemaDefinition) schema).getName();
        }

        return null;
    }

    @Override
    protected Class<Oas20Response> getResponseType() {
        return Oas20Response.class;
    }

    @Override
    protected Predicate<Oas20Response> hasSchema() {
        return response -> response.schema != null;
    }

    @Override
    protected Oas20Schema getSchema(Oas20Response response) {
        return response.schema;
    }

    @Override
    public List<OasResponse> resolveResponses(Oas20Document openApiDoc, List<OasResponse> operationResponses) {
        return Oas20DataShapeGeneratorHelper.resolveResponses(openApiDoc, operationResponses);
    }

    @Override
    public Optional<NameAndSchema> findBodySchema(Oas20Document openApiDoc, Oas20Operation operation) {
        return Oas20DataShapeGeneratorHelper.findBodySchema(operation);
    }

    @Override
    protected OasSchema createSchemaDefinition(String name) {
        return new Oas20SchemaDefinition(name);
    }

    @Override
    protected Element createParametersSchema(final Oas20Document openApiDoc, final Oas20Operation operation) {
        final List<Oas20Parameter> operationParameters = Oas20DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);

        return createSchemaFor(operationParameters.stream()
            .filter(p -> p.type != null)
            .filter(OasModelHelper::isSerializable)
            .collect(Collectors.toList()));
    }

    private static Element createSchemaFor(final List<Oas20Parameter> parameterList) {
        if (parameterList.isEmpty()) {
            return null;
        }
        final Element schema = XmlSchemaHelper.newXmlSchema(SYNDESIS_PARAMETERS_NS);

        final Element parameters = XmlSchemaHelper.addElement(schema, "element");
        parameters.addAttribute("name", "parameters");

        final Element complex = XmlSchemaHelper.addElement(parameters, "complexType");
        final Element sequence = XmlSchemaHelper.addElement(complex, "sequence");

        for (final Oas20Parameter parameter : parameterList) {
            final String type = XmlSchemaHelper.toXsdType(parameter.type);
            final String name = trimToNull(parameter.getName());

            if ("file".equals(type)) {
                // 'file' type is not allowed in JSON schema
                continue;
            }

            final Element element = XmlSchemaHelper.addElement(sequence, "element");
            element.addAttribute("name", name);
            element.addAttribute("type", type);

            final Object defaultValue = parameter.default_;
            if (defaultValue != null) {
                element.addAttribute("default", String.valueOf(defaultValue));
            }

            addEnumsTo(element, parameter);
        }

        return schema;
    }

    private static void addEnumsTo(final Element element, final Oas20Parameter parameter) {
        if (parameter.items != null) {
            final Oas20Items items = parameter.items;

            List<String> enums = ofNullable(items.enum_).orElse(Collections.emptyList());
            if (!enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        } else {
            final List<String> enums = parameter.enum_;

            if (enums != null && !enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        }
    }
}
