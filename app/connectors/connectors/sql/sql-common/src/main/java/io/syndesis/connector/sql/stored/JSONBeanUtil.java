/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.sql.stored;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility to help with parsing the data from the simple serialized java bean.
 * The resulting property map can then be used by the
 * SqlStoredComponentConnector.
 *
 * @author kstam
 *
 */
public class JSONBeanUtil {

    /**
     * Convenience method to parse the properties from a simple BeanJSON.
     * Properties can be read by Camel.
     *
     * @param json simple JSON represenation of a Java Bean used as input Data
     *            for the SqlStoredConnector
     * @return Properties representation of the simple JSON bean
     */
    public static Properties parsePropertiesFromJSONBean(final String json) throws JSONException {
        final Properties properties = new Properties();
        final JSONObject obj = new JSONObject(json);
        @SuppressWarnings("unchecked")
        final Iterator<String> iterator = obj.keys();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            final String value = String.valueOf(obj.get(key));
            properties.setProperty(key, value);
        }
        return properties;
    }
    /**
     * Convenience method to convert a Camel Map output to a JSON Bean String.
     * 
     * @param map
     * @return JSON bean String
     */
    public static String toJSONBean(final Map<String,Object> map) {
        final JSONObject obj = new JSONObject();
        for (String key : map.keySet()) {
            if (! key.startsWith("#")) {  //don't include Camel stats
                obj.put(key, map.get(key));
            }
        }
        return obj.toString();
    }
    /**
     * Convenience method to convert a Camel Map output to a JSON Bean String.
     * 
     * @param map
     * @return JSON bean String
     */
    @SuppressWarnings("unchecked")
    public static String toJSONBean(final List<Object> list) {
        String json = null;
        if (list.size()==1) {
            Map<String,Object> map = (Map<String,Object>) list.get(0);
            json = JSONBeanUtil.toJSONBean(map);
        } else if (list.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder("[");
            for (int i=0; i<list.size(); i++) {
                Map<String,Object> map = (Map<String,Object>) list.get(i);
                stringBuilder.append(JSONBeanUtil.toJSONBean(map));
                if ( i < (list.size()-1 )) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append("]");
            json = stringBuilder.toString();
        }
        return json;
    }
}
