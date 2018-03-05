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
package io.syndesis.server.logging.jsondb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.util.SuppressFBWarnings;

import java.util.HashSet;
import java.util.Set;

/**
 * Static helpers for working with JsonNode objects
 */
public final class JsonNodeSupport {

    private JsonNodeSupport(){
        // utility class
    }

    public static String getString(JsonNode json, String field) {
        JsonNode value = json.get(field);
        if( value==null ) {
            return null;
        }
        if( value.isNull() ) {
            return null;
        }
        return value.asText();
    }

    public static String removeString(ObjectNode json, String field) {
        JsonNode value = json.remove(field);
        if( value==null ) {
            return null;
        }
        if( value.isNull() ) {
            return null;
        }
        return value.asText();
    }

    public static Long removeLong(ObjectNode json, String field) {
        JsonNode value = json.remove(field);
        if( value==null ) {
            return null;
        }
        if( value.isNull() ) {
            return null;
        }
        return value.asLong();
    }

    public static Long getLong(JsonNode json, String field) {
        JsonNode value = json.get(field);
        if( value==null ) {
            return null;
        }
        if( value.isNull() ) {
            return null;
        }
        return value.asLong();
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public static Boolean removeBoolean(ObjectNode json, String field) {
        JsonNode value = json.remove(field);
        if( value==null ) {
            return null;
        }
        if( value.isNull() ) {
            return null;
        }
        return value.asBoolean();
    }

    public static Set<String> fieldNames(JsonNode json) {
        Set<String> rc = new HashSet<>();
        json.fieldNames().forEachRemaining(rc::add);
        return rc;
    }

}
