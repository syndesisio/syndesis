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

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.SqlParameterValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JSONBeanUtilTest {

    static class SimpleInputBean {

        int a;
        int b;

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public void setA(final int a) {
            this.a = a;
        }

        public void setB(final int b) {
            this.b = b;
        }
    }

    static class SimpleOutputBean {
        int c;

        public int getC() {
            return c;
        }

        public void setC(final int c) {
            this.c = c;
        }
    }

    @Test
    public void mapToJsonBeanTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleOutputBean bean = new SimpleOutputBean();
        bean.setC(50);
        final String jsonBeanExpected = mapper.writeValueAsString(bean);

        final Map<String, Object> map = new HashMap<>();
        map.put("c", 50);
        map.put("#update-count-1", 0);
        final String jsonBeanActual = JSONBeanUtil.toJSONBean(map);
        Assert.assertEquals(jsonBeanExpected, jsonBeanActual);
    }

    @Test
    public void parsePropertiesFromJSONBeanTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleInputBean bean = new SimpleInputBean();
        bean.setA(20);
        bean.setB(30);
        final String jsonBean = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bean);

        final Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(jsonBean);
        Assert.assertTrue(properties.containsKey("a"));
        Assert.assertEquals("20", properties.get("a"));
        Assert.assertTrue(properties.containsKey("b"));
        Assert.assertEquals("30", properties.get("b"));
    }

    /**
     * Address issue when sql is parsed but contains no sql parameters
     * @see https://github.com/syndesisio/syndesis/issues/2706
     *
     * @throws JsonProcessingException
     */
    @Test
    public void parseSqlParametersFromJSONBeanWhenNoParameters() throws JsonProcessingException {
        try {
            String[] jsonParamValues = { null, "", "{}", "[]", "something else" };
            for (int i = 0; i < jsonParamValues.length; ++i) {
                Map<String, SqlParameterValue> retMap = JSONBeanUtil.parseSqlParametersFromJSONBean(jsonParamValues[i], new HashMap<>());
                assertThat(retMap).isEmpty();
            }
        } catch (Exception ex) {
            throw new AssertionError("Should not throw exception when json parameter is empty", ex);
        }

        //
        // Test type map
        //
        try {
            String jsonParamValue = "{ \"arg1\": \"blah\" }";
            Map<String, SqlParameterValue> retMap = JSONBeanUtil.parseSqlParametersFromJSONBean(jsonParamValue, null);
            SqlParameterValue paramValue = retMap.get("arg1");
            assertNotNull(paramValue);
            assertEquals(Types.VARCHAR, paramValue.getSqlType());
        }  catch (Exception ex) {
            throw new AssertionError("Should not throw exception when json parameter is empty", ex);
        }
    }
}
