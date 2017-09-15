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
package io.syndesis.verifier.v1.metadata;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

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
    List<String> properties = new ArrayList<>();

    public JSONBeanSchemaBuilder addField(String name, JDBCType jdbcType) {
        properties.add(String.format("    \"%s\" : {\"type\" : %s}", name, toJSONType(jdbcType)));
        return this;
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public JsonSchema build()  {
        try {
            if (hasProperties()) {
                String propertiesString = "";
                int count = 0;
                for (String property : properties) {
                    propertiesString += property;
                    if ( properties.size()!=++count ) propertiesString += ",\n";
                }
                return new ObjectMapper().readValue(String.format(SCHEMA, propertiesString), JsonSchema.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** convert JDBC Datatype to JSON Datatype **/
    protected String toJSONType(JDBCType jdbcType) {

        String jsonType = "\"unmapped\"";

        switch (jdbcType) {
        case ARRAY:
            jsonType = "\"array\"";
            break;
        case BIGINT:
            jsonType = "\"integer\"";
            break;
        case BINARY:
            jsonType = "{\n" + 
                    "      \"type\" : \"array\",\n" + 
                    "      \"items\" : {\n" + 
                    "        \"type\" : \"integer\"\n" + 
                    "      }";
            break;
        case BIT:
            jsonType = "\"boolean\"";
            break;
        case BLOB:
            jsonType = "{\n" + 
                    "      \"type\" : \"array\",\n" + 
                    "      \"items\" : {\n" + 
                    "        \"type\" : \"integer\"\n" + 
                    "      }";
            break;
        case BOOLEAN:
            jsonType = "\"boolean\"";
            break;
        case CHAR:
            jsonType = "\"string\"";
            break;
        case CLOB:
            jsonType = "\"string\"";
            break;
        case DATALINK:
            jsonType = "\"string\"";
            break;
        case DATE:
            jsonType = "{\n" + 
                    "      \"type\" : \"string\",\n" + 
                    "      \"format\" : \"date-time\"\n" + 
                    "    }";
            break;
        case DECIMAL:
            jsonType = "\"number\"";
            break;
        case DISTINCT:
            break;
        case DOUBLE:
            jsonType = "\"number\"";
            break;
        case FLOAT:
            jsonType = "\"number\"";
            break;
        case INTEGER:
            jsonType = "\"integer\"";
            break;
        case JAVA_OBJECT:
            break;
        case LONGNVARCHAR:
            jsonType = "\"string\"";
            break;
        case LONGVARBINARY:
            jsonType = "{\n" + 
                    "      \"type\" : \"array\",\n" + 
                    "      \"items\" : {\n" + 
                    "        \"type\" : \"integer\"\n" + 
                    "      }";
            break;
        case LONGVARCHAR:
            jsonType = "\"string\"";
            break;
        case NCHAR:
            jsonType = "\"string\"";
            break;
        case NCLOB:
            jsonType = "\"string\"";
            break;
        case NULL:
            jsonType = "\"null\"";
            break;
        case NUMERIC:
            jsonType = "\"number\"";
            break;
        case NVARCHAR:
            jsonType = "\"string\"";
            break;
        case OTHER:
            break;
        case REAL:
            jsonType = "\"number\"";
            break;
        case REF:
            break;
        case REF_CURSOR:
            break;
        case ROWID:
            jsonType = "\"string\"";
            break;
        case SMALLINT:
            jsonType = "\"integer\"";
            break;
        case SQLXML:
            jsonType = "\"string\"";
            break;
        case STRUCT:
            break;
        case TIME:
            jsonType = "{\n" + 
                    "      \"type\" : \"string\",\n" + 
                    "      \"format\" : \"date-time\"\n" + 
                    "    }";
            break;
        case TIMESTAMP:
            jsonType = "{\n" + 
                    "      \"type\" : \"string\",\n" + 
                    "      \"format\" : \"date-time\"\n" + 
                    "    }";
            break;
        case TIMESTAMP_WITH_TIMEZONE:
            jsonType = "{\n" + 
                    "      \"type\" : \"string\",\n" + 
                    "      \"format\" : \"date-time\"\n" + 
                    "    }";
            break;
        case TIME_WITH_TIMEZONE:
            jsonType = "{\n" + 
                    "      \"type\" : \"string\",\n" + 
                    "      \"format\" : \"date-time\"\n" + 
                    "    }";
            break;
        case TINYINT:
            jsonType = "\"integer\"";
            break;
        case VARBINARY:
            jsonType = "{\n" + 
                    "      \"type\" : \"array\",\n" + 
                    "      \"items\" : {\n" + 
                    "        \"type\" : \"integer\"\n" + 
                    "      }";
            break;
        case VARCHAR:
            jsonType = "\"string\"";
            break;
        }
        return jsonType;
    }
}
