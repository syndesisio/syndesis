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

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Schema builder for the simple schema's the SQL Stored Connector uses.
 * 
 * The schema is created so it can be passed on to the dataMapper. An example 
 * schema looks like
 * 
 * <pre>
 * {@code
 * "$schema": "http://json-schema.org/schema#",
 * "type" : "object",
 * "properties" : {
 *   "a" : {"type" : "integer"},
 *   "b" : {"type" : "integer"}
 *  } 
 * }
 * </pre>
 * 
 * @author kstam
 * @since 09/11/2017
 *
 */
public class JSONBeanSchemaBuilder {

    final static String SCHEMA = "{\n" + 
            "  \"$schema\": \"http://json-schema.org/schema#\",\n" +
            "  \"type\" : \"object\",\n" + 
            "  \"properties\" : {\n%s\n   } \n}";
    List<String> fields = new ArrayList<String>();
    
    public JSONBeanSchemaBuilder addField(String name, String type) {
        fields.add(String.format("    \"%s\" : {\"type\" : \"%s\"}", name, type));
        return this;
    }
    
    public String build() {
       String fieldString = "";
       int count = 0;
       for (String field : fields) {
           fieldString += field;
           if ( fields.size()!=++count ) fieldString += ",\n";
       }
       return String.format(SCHEMA, fieldString);
    }
}
