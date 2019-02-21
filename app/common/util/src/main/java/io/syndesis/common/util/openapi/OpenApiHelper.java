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
package io.syndesis.common.util.openapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.PropertyDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * All interaction with SwaggerParser should be done through this class because
 * it performs additional setup of the Jackson ObjectMapper used by the
 * SwaggerParser in order to make sure that the parssing and serialization is
 * done in accordance to the specification.
 */
public final class OpenApiHelper {

    static final SwaggerParser SWAGGER_PARSER = new SwaggerParser();

    static {
        final ObjectMapper mapper = io.swagger.util.Json.mapper();

        // make sure that Jackson is using our own BaseIntegerProperty which
        // supports `enum` property
        mapper.addMixIn(Property.class, BaseIntegerProperty.class);

        // we don't need empty arrays
        mapper.configOverride(List.class).setInclude(JsonInclude.Value.construct(Include.NON_EMPTY, null));
    }

    @JsonDeserialize(using = BaseIntegerProperty.Serializer.class)
    public static class BaseIntegerProperty extends io.swagger.models.properties.BaseIntegerProperty {

        protected List<Integer> enumValues;

        public static final class Serializer extends JsonDeserializer<Property> {

            private final JsonDeserializer<Property> defaultDeserializer = new PropertyDeserializer();

            @Override
            public Property deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
                final CapturingCodec capturingCodec = new CapturingCodec(parser.getCodec());

                final Property property = defaultDeserializer.deserialize(new JsonParserDelegate(parser) {
                    @Override
                    public ObjectCodec getCodec() {
                        return capturingCodec;
                    }
                }, context);

                if (!(property instanceof io.swagger.models.properties.BaseIntegerProperty)) {
                    return property;
                }

                final JsonNode captured = (JsonNode) capturingCodec.captured();

                final JsonNode enumNode = captured.get("enum");
                if (enumNode == null || enumNode.isMissingNode() || enumNode.isNull() || !enumNode.isArray()) {
                    return property;
                }

                final BaseIntegerProperty integerProperty = new BaseIntegerProperty((io.swagger.models.properties.BaseIntegerProperty) property);
                integerProperty.enumValues = new ArrayList<>();
                ((ArrayNode) enumNode).elements().forEachRemaining(value -> {
                    if (value.isNumber()) {
                        integerProperty.enumValues.add(value.intValue());
                    }
                });

                return integerProperty;
            }

        }

        public BaseIntegerProperty(final io.swagger.models.properties.BaseIntegerProperty property) {
            access = property.getAccess();
            allowEmptyValue = property.getAllowEmptyValue();
            description = property.getDescription();
            example = property.getExample();
            exclusiveMaximum = property.getExclusiveMaximum();
            exclusiveMinimum = property.getExclusiveMinimum();
            format = property.getFormat();
            maximum = property.getMaximum();
            minimum = property.getMinimum();
            multipleOf = property.getMultipleOf();
            name = property.getName();
            position = property.getPosition();
            readOnly = property.getReadOnly();
            required = property.getRequired();
            title = property.getTitle();
            type = property.getType();
            vendorExtensions = property.getVendorExtensions();
            xml = property.getXml();
        }

        public List<Integer> getEnum() {
            return enumValues;
        }

        public void setEnum(final List<Integer> enumValues) {
            this.enumValues = enumValues;
        }
    }

    private OpenApiHelper() {
        // helper class
    }

    public static ObjectMapper mapper() {
        return io.swagger.util.Json.mapper();
    }

    public static Swagger parse(final String specification) {
        return SWAGGER_PARSER.parse(specification);
    }

    public static SwaggerDeserializationResult parseWithResult(final String specification) {
        return SWAGGER_PARSER.readWithInfo(specification);
    }

    public static String serialize(final Swagger swagger) {
        try {
            return io.swagger.util.Json.mapper().writeValueAsString(swagger);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize OpenAPI document", e);
        }
    }
}
