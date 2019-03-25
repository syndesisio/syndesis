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

package io.syndesis.common.util.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.util.Json;

/**
 * Utilities for working with Json.
 *
 * @author Christoph Deppisch
 */
public final class JsonUtils {

    /**
     * Prevent instantiation for utility class.
     */
    private JsonUtils() {
        super();
    }

    /**
     * Checks given value to be a Json array of object representation.
     * @param value
     * @return
     */
    public static boolean isJson(String value) {
        if (value == null) {
            return false;
        }

        return isJsonObject(value) || isJsonArray(value);
    }

    /**
     * Checks given value to be a proper Json object string.
     * @param value
     * @return
     */
    public static boolean isJsonObject(String value) {
        if (value == null) {
            return false;
        }

        return value.trim().startsWith("{") && value.trim().endsWith("}");
    }

    /**
     * Checks given value to be a proper Json array string.
     * @param value
     * @return
     */
    public static boolean isJsonArray(String value) {
        if (value == null) {
            return false;
        }

        return value.trim().startsWith("[") && value.trim().endsWith("]");
    }

    /**
     * Converts array json node to a list of json object strings. Used when splitting a
     * json array with split EIP.
     * @param json
     * @return
     * @throws JsonProcessingException
     */
    public static List<String> arrayToJsonBeans(JsonNode json) throws JsonProcessingException {
        List<String> jsonBeans = new ArrayList<>();

        if (json.isArray()) {
            Iterator<JsonNode> it = json.elements();
            while (it.hasNext()) {
                jsonBeans.add(Json.writer().writeValueAsString(it.next()));
            }

            return jsonBeans;
        }

        return jsonBeans;
    }

    /**
     * Converts list of json object strings to a json array string.
     * @param jsonBeans
     * @return
     */
    public static String jsonBeansToArray(List<?> jsonBeans) {
        final StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Object jsonBean : jsonBeans) {
            joiner.add(String.valueOf(jsonBean));
        }
        return joiner.toString();
    }
}
