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

import java.util.Properties;

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
                "  \"$schema\": \"http://json-schema.org/schema#\",\n" + 
                "  \"type\" : \"object\",\n" + 
                "  \"properties\" : {\n" + 
                "    \"a\" : {\"type\" : \"integer\"},\n" + 
                "    \"b\" : {\"type\" : \"integer\"}\n" + 
                "   } \n" + 
                "}";
        String schemaFromBuilder = new JSONBeanSchemaBuilder()
                .addField("a", "integer")
                .addField("b", "integer").build();
        System.out.println(schemaFromBuilder);
        Assert.assertEquals(desiredSchema, schemaFromBuilder);
    }

    @Test
    public void parsePropertiesFromJSONBeanTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleInputBean bean = new SimpleInputBean();
        bean.setA(20);
        bean.setB(30);
        String jsonBean = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bean);
        
        Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(jsonBean);
        Assert.assertTrue(properties.containsKey("a"));
        Assert.assertEquals("20", properties.get("a"));
        Assert.assertTrue(properties.containsKey("b"));
        Assert.assertEquals("30", properties.get("b"));
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
