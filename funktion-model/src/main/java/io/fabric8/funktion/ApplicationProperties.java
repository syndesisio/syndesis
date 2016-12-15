/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 */
public class ApplicationProperties {
    /**
     * Returns the string representation of the Spring Boot <code>application.properties</code> file
     */
    public static String toPropertiesString(Map<String, String> applicationProperties, String comments) throws IOException {
        Properties properties = toProperties(applicationProperties);
        StringWriter writer = new StringWriter();
        properties.store(writer, comments);
        return writer.toString();
    }

    protected static Properties toProperties(Map<String, String> applicationProperties) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : applicationProperties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            properties.setProperty(key, value);
        }
        return properties;
    }
}
