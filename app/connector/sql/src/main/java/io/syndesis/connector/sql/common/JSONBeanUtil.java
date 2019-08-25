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
package io.syndesis.connector.sql.common;

import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.springframework.jdbc.core.SqlParameterValue;

/**
 * Utility to help with parsing the data from the simple serialized java bean.
 * The resulting property map can then be used by the
 * SqlStoredComponentConnector.
 *
 * @author kstam
 *
 */
public final class JSONBeanUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, String>> STRING_STRING_MAP = new TypeReference<Map<String, String>>() {
        // type token pattern
    };

    private JSONBeanUtil() {
        // utility class
    }

    /**
     * Convenience method to parse the properties from a simple BeanJSON.
     * Properties can be read by Camel.
     *
     * @param json simple JSON representation of a Java Bean used as input Data
     *            for the SqlStoredConnector
     * @return Properties representation of the simple JSON bean
     */
    public static Properties parsePropertiesFromJSONBean(final String json) {
        final Map<String, String> parsed;
        try {
            parsed = MAPPER.readerFor(STRING_STRING_MAP).readValue(json);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to parse given JSON", e);
        }

        final Properties ret = new Properties();
        if (parsed != null) {
            parsed.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null)
                    .forEach(e -> ret.put(e.getKey(), e.getValue()));
        }
        return ret;
    }

    public static Map<String, SqlParameterValue> parseSqlParametersFromJSONBean(final String json, final Map<String, Integer> jdbcTypeMap) {
        if (!isValidJSONBean(json)) {
            return Collections.emptyMap(); // json is empty so no need to parse
        }

        final Map<String, String> parsed;
        try {
            parsed = MAPPER.readerFor(STRING_STRING_MAP).readValue(json);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to parse given JSON", e);
        }

        final Map<String,SqlParameterValue> ret = new HashMap<>();
        for (Map.Entry<String, String> entry : parsed.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Integer jdbcType = null;
            if (jdbcTypeMap != null) {
                jdbcType = jdbcTypeMap.get(key);
            }

            if (jdbcType == null) {
                jdbcType = Types.VARCHAR;
            }

            SqlParameterValue sqlParam = new SqlParameterValue(jdbcType, value);
            ret.put(key, sqlParam);
        }
        return ret;
    }

    /**
     * Validate JSON bean for being a proper parameter information object.
     * @param json
     * @return
     */
    private static boolean isValidJSONBean(String json) {
        return json != null && !json.isEmpty() && json.trim().startsWith("{") && json.trim().endsWith("}");
    }

    /**
     * Convenience method to convert a Camel Map output to a JSON Bean String.
     *
     * @param list
     * @return JSON bean String
     */
    @SuppressWarnings("unchecked")
    public static String toJSONBean(final List<Object> list) {
        String json = null;
        if (list.size() == 1) {
            final Map<String, Object> map = (Map<String, Object>) list.get(0);
            json = JSONBeanUtil.toJSONBean(map);
        } else if (list.size() > 1) {
            final StringBuilder stringBuilder = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                final Map<String, Object> map = (Map<String, Object>) list.get(i);
                stringBuilder.append(JSONBeanUtil.toJSONBean(map));
                if (i < list.size() - 1) {
                    stringBuilder.append(',');
                }
            }
            stringBuilder.append(']');
            json = stringBuilder.toString();
        }
        return json;
    }

    /**
     * Convenience method to convert a Camel Map output to a JSON Bean String.
     *
     * @param map
     * @return JSON bean String
     */
    public static String toJSONBean(final Map<String, Object> map) {
        final Map<String, Object> data = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (key.charAt(0) != '#') { // don't include Camel stats
                data.put(key, value);
            }
        }

        try {
            return MAPPER.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize to JSON", e);
        }
    }

    /**
     * Converts Camel Map output representing DB result set to a list of JSON Bean Strings.
     *
     * @param in
     * @return
     */
    public static List<String> toJSONBeans(Message in) {
        final List<String> jsonBeans = new ArrayList<>();

        if (in.getBody() instanceof List) {
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> maps = in.getBody(List.class);

            if (ObjectHelper.isNotEmpty(maps)) {
                for (Map<String, Object> map : maps) {
                    final String bean = JSONBeanUtil.toJSONBean(map);
                    jsonBeans.add(bean);
                }
            }
            return jsonBeans;
        } else if (in.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> singleMap = in.getBody(Map.class);

            if (singleMap != null) {
                final String bean = JSONBeanUtil.toJSONBean(singleMap);
                jsonBeans.add(bean);
            }
            return jsonBeans;
        } else {
            return null;
        }

    }

    /**
     * Converts Camel Generated Key output to a list of JSON Bean Strings.
     *
     * @param in
     * @param autoIncrementColumnName the name of the auto increment column name
     * @return List of JSON beans.
     */
    public static List<String> toJSONBeansFromHeader(Message in, String autoIncrementColumnName) {
        final List<String> jsonBeans = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> generatedKeys = in.getHeader(CamelSqlConstants.SQL_GENERATED_KEYS_DATA, List.class);
        for (Map<String, Object> generatedKey : generatedKeys) {
            final Map<String, Object> map = new HashMap<>();
            map.put(autoIncrementColumnName, generatedKey.values().iterator().next());
            final String bean = JSONBeanUtil.toJSONBean(map);
            jsonBeans.add(bean);
        }
        return jsonBeans;
    }
}
