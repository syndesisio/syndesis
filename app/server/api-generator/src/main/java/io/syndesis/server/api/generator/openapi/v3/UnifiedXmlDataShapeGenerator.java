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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30ParameterDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.apicurio.datamodels.openapi.v3.models.Oas30SchemaDefinition;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;
import io.syndesis.server.api.generator.openapi.UnifiedXmlDataShapeSupport;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import io.syndesis.server.api.generator.openapi.util.XmlSchemaHelper;
import org.dom4j.Element;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

class UnifiedXmlDataShapeGenerator extends UnifiedXmlDataShapeSupport<Oas30Document, Oas30Operation, Oas30Response> implements DataShapeGenerator<Oas30Document, Oas30Operation> {

    @Override
    protected OasSchema dereference(OasSchema property, Oas30Document openApiDoc) {
        return Oas30ModelHelper.dereference(property, openApiDoc);
    }

    @Override
    protected String getName(OasSchema schema) {
        if (schema instanceof Oas30SchemaDefinition) {
            return ((Oas30SchemaDefinition) schema).getName();
        }

        return null;
    }

    @Override
    protected Class<Oas30Response> getResponseType() {
        return Oas30Response.class;
    }

    @Override
    protected Predicate<Oas30Response> hasSchema() {
        return response -> Oas30ModelHelper.getSchema(response, APPLICATION_XML) != null;
    }

    @Override
    protected OasSchema getSchema(Oas30Response response) {
        return Oas30ModelHelper.getSchema(response, APPLICATION_XML);
    }

    @Override
    public Optional<NameAndSchema> findBodySchema(Oas30Operation operation) {
        if (operation.requestBody == null) {
            return empty();
        }

        Oas30MediaType body = Oas30ModelHelper.getMediaType(operation.requestBody, APPLICATION_XML);
        if (body != null) {
            String name = ofNullable(body.getName()).orElse(operation.requestBody.description);
            return Optional.of(new NameAndSchema(name, body.schema));
        }

        return empty();
    }

    @Override
    protected OasSchema createSchemaDefinition(String name) {
        return new Oas30SchemaDefinition(name);
    }

    @Override
    protected Element createParametersSchema(final Oas30Document openApiDoc, final Oas30Operation operation) {
        final List<Oas30Parameter> operationParameters = Oas30ModelHelper.getParameters(operation);

        OasPathItem parent = ofNullable(operation.parent())
            .filter(OasPathItem.class::isInstance)
            .map(OasPathItem.class::cast)
            .orElse(null);
        final List<Oas30Parameter> pathParameters = Oas30ModelHelper.getParameters(parent);
        operationParameters.addAll(pathParameters);

        final Map<String, Oas30ParameterDefinition> globalParameters = Oas30ModelHelper.getParameters(openApiDoc);
        operationParameters.addAll(globalParameters.values());

        final List<Oas30Parameter> parameterList = operationParameters.stream()
            .filter(p -> p.schema instanceof Oas30Schema && ((Oas30Schema) p.schema).type != null)
            .filter(OasModelHelper::isSerializable)
            .collect(Collectors.toList());

        if (parameterList.isEmpty()) {
            return null;
        }

        final Element schema = XmlSchemaHelper.newXmlSchema(SYNDESIS_PARAMETERS_NS);

        final Element parameters = XmlSchemaHelper.addElement(schema, "element");
        parameters.addAttribute("name", "parameters");

        final Element complex = XmlSchemaHelper.addElement(parameters, "complexType");
        final Element sequence = XmlSchemaHelper.addElement(complex, "sequence");

        for (final Oas30Parameter parameter : parameterList) {
            final Oas30Schema parameterSchema = Oas30ModelHelper.getSchema(parameter);

            if (parameterSchema == null) {
                continue;
            }

            final String type = XmlSchemaHelper.toXsdType(parameterSchema.type);
            final String name = trimToNull(parameter.getName());

            if ("file".equals(type)) {
                // 'file' type is not allowed in JSON schema
                continue;
            }

            final Element element = XmlSchemaHelper.addElement(sequence, "element");
            element.addAttribute("name", name);
            element.addAttribute("type", type);

            final Object defaultValue = parameterSchema.default_;
            if (defaultValue != null) {
                element.addAttribute("default", String.valueOf(defaultValue));
            }

            addEnumsTo(element, parameterSchema);
        }

        return schema;
    }

    private static void addEnumsTo(final Element element, final Oas30Schema schema) {
        if (schema.items != null) {
            final Oas30Schema items = (Oas30Schema) schema.items;

            List<String> enums = ofNullable(items.enum_).orElse(Collections.emptyList());
            if (!enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        } else {
            final List<String> enums = schema.enum_;

            if (enums != null && !enums.isEmpty()) {
                addEnumerationsTo(element, enums);
            }
        }
    }
}
