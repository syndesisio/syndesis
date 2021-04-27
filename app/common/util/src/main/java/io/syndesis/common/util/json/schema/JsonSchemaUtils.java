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

package io.syndesis.common.util.json.schema;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

public final class JsonSchemaUtils {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaUtils.class);

    private JsonSchemaUtils() {
        // Prevent instantiation for utility class.
    }

    /**
     * This method creates a copy of the default ObjectMapper configuration and adds special Json schema compatibility handlers
     * for supporting draft-03, draft-04 and draft-06 level at the same time.
     * Auto converts "$id" to "id" property for draft-04 compatibility.
     * In case the provided schema specification to read uses draft-04 and draft-06 specific features such as "examples" or a list of "required"
     * properties as array these information is more or less lost and auto converted to draft-03 compatible defaults. This way we can
     * read the specification to draft-03 compatible objects and use those.
     */
    public static ObjectReader reader() {
        return JsonUtils.copyObjectMapperConfiguration()
                .addHandler(new DeserializationProblemHandler() {
                    @Override
                    public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType, JsonToken t,
                                                        JsonParser p, String failureMsg) throws IOException {
                        if (t == JsonToken.START_ARRAY && targetType.getRawClass().equals(Boolean.class)) {
                            // handle Json schema draft-04 array type for required field and resolve to default value (required=true).
                            String[] requiredProps = new StringArrayDeserializer().deserialize(p, ctxt);
                            LOG.warn("Auto convert Json schema draft-04 \"required\" array value '{}' to default \"required=false\" value for draft-03 parser compatibility reasons", Arrays.toString(requiredProps));
                            return null;
                        }

                        return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg);
                    }
                })
                .addMixIn(JsonSchema.class, MixIn.Draft6.class)
                .reader()
                .forType(JsonSchema.class);
    }

    /**
     * Jackson mixins creating compatibility with other schema draft versions.
     */
    interface MixIn {

        /**
         * Adds compatibility for draft-06 specific properties.
         */
        interface Draft6 {
            @JsonAlias("$id") String getId();
        }
    }
}
