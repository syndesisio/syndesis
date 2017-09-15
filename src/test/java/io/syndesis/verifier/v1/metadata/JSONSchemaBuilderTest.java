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

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

public class JSONSchemaBuilderTest {

    @Test
    public void schemaTest()  throws JsonProcessingException {

        /* Create Schema using Jackson */
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        JsonSchema schema = schemaGen.generateSchema(SimpleInputBean.class);
        String desiredSchema = (mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
        System.out.println(desiredSchema);
        //Manually cleaned up version up of desiredSchema to more easily test equality
        desiredSchema = "{\n" + 
                "  \"type\" : \"object\",\n" + 
                "  \"$schema\" : \"http://json-schema.org/schema#\",\n" + 
                "  \"properties\" : {\n" + 
                "    \"a\" : {\n" + 
                "      \"type\" : \"integer\"\n" + 
                "    },\n" + 
                "    \"b\" : {\n" + 
                "      \"type\" : \"integer\"\n" + 
                "    }\n" + 
                "  }\n" + 
                "}";
        JsonSchema schemaFromBuilder = new JSONBeanSchemaBuilder()
                .addField("a", JDBCType.INTEGER)
                .addField("b", JDBCType.INTEGER).build();
        String actualSchema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaFromBuilder);
        System.out.println(actualSchema);
        Assert.assertEquals(desiredSchema, actualSchema);
    }

    @Test
    public void testNoSchema() {
        JsonSchema noSchema = new JSONBeanSchemaBuilder().build();
        Assert.assertNull(noSchema);
    }

    class SimpleInputBean {
        
        int a;
        int b;
        
        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

    }
    
}
