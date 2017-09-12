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
package io.syndesis.connector;

import java.util.Iterator;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility to help with parsing the data from the simple
 * serialized java bean. The resulting property map can 
 * then be used by the SqlStoredComponentConnector.
 * 
 * @author kstam
 *
 */
public class JSONBeanUtil {

    /** 
     * Convenience method to parse the properties from a simple BeanJSON.
     * Properties can be read by Camel.
     * 
     * @param json simple JSON represenation of a Java Bean used
     * as input Data for the SqlStoredConnector
     * @return Properties representation of the simple JSON bean
     */
    public static Properties parsePropertiesFromJSONBean(String json) throws JSONException {
        Properties properties = new Properties();
        JSONObject obj = new JSONObject(json);
        @SuppressWarnings("unchecked")
        Iterator<String> iterator = obj.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = String.valueOf(obj.get(key));
            properties.setProperty(key, value);
        }
        return properties;
    }
}
