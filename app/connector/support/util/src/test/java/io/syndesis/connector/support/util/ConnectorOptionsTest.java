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
package io.syndesis.connector.support.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;

public class ConnectorOptionsTest {

    private String key = "myKey";
    private String missingKey = "missingKey";
    private String value = "myValue";

    private enum Colours {
        RED,
        BLUE,
        GREEN,
        YELLOW;
    }

    private Map<String, Object> parameters;

    @Before
    public void setup() {
        parameters = new HashMap<String, Object>();
    }

    private void put(String key, Object value) {
        parameters.put(key, value);
    }

    private Long convertLong(Object someObject) {
        return value.equals(someObject) ? 1L : 0L;
    }

    public boolean isEmpty(Object value) {
        return !isNotEmpty(value);
    }

    public boolean isNotEmpty(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof String) {
            String text = (String) value;
            return text.trim().length() > 0;
        } else if (value instanceof Collection) {
            return !((Collection<?>)value).isEmpty();
        } else if (value instanceof Map) {
            return !((Map<?, ?>)value).isEmpty();
        } else {
            return true;
        }
    }

    @Test
    public void testNoMap() throws Exception {
        String defaultString = "defaultString";
        Long defaultLong = 2L;
        parameters = null;

        assertNull(ConnectorOptions.extractOption(parameters, key));
        assertEquals(defaultString, ConnectorOptions.extractOption(parameters, key, defaultString));
        assertNull(ConnectorOptions.extractOptionAndMap(parameters, key, this::convertLong));
        assertEquals(defaultLong, ConnectorOptions.extractOptionAndMap(parameters, key, this::convertLong, defaultLong));
        assertNull(ConnectorOptions.extractOptionAsType(parameters, key, String.class));
        assertEquals(defaultString, ConnectorOptions.extractOptionAsType(parameters, key, String.class, defaultString));
        assertNull(ConnectorOptions.popOption(parameters, key));

        Consumer<String> c = (String v) -> fail("This should never be executed");
        ConnectorOptions.extractOptionAndConsume(parameters, key, c);
    }

    @Test
    public void testStringValue() {
        put(key, value);

        assertEquals(value, ConnectorOptions.extractOption(parameters, key));
        assertNull(ConnectorOptions.extractOption(parameters, missingKey));
    }

    @Test
    public void testStringValueWithDefault() {
        String defaultValue = "defaultValue";
        put(key, value);

        assertEquals(value, ConnectorOptions.extractOption(parameters, key, defaultValue));
        assertEquals(defaultValue, ConnectorOptions.extractOption(parameters, missingKey, defaultValue));
    }

    @Test
    public void testStringValueAndMap() throws Exception {
        put(key, value);

        assertEquals((Long) 1L, ConnectorOptions.extractOptionAndMap(parameters, key, this::convertLong));
        assertNull(ConnectorOptions.extractOptionAndMap(parameters, missingKey, this::convertLong));
    }

    @Test
    public void testStringValueAndMapWithDefault() {
        Long defaultValue = 2L;
        put(key, value);

        assertEquals((Long) 1L, ConnectorOptions.extractOptionAndMap(parameters, key, this::convertLong, defaultValue));
        assertEquals(defaultValue, ConnectorOptions.extractOptionAndMap(parameters, missingKey, this::convertLong, defaultValue));
    }

    @Test
    public void testStringValueAndMapToArrayWithDefault() {
        String vToSplit = "hello,bonjour,salve,hiya";

        put(key, vToSplit);

        String[] exp = {"hello", "bonjour", "salve", "hiya"};
        String[] defaultValue = new String[]{};
        String[] result = ConnectorOptions.extractOptionAndMap(parameters, key,
                                                                     names -> names.split(","), defaultValue);
        assertArrayEquals(exp, result);
        assertArrayEquals(defaultValue, ConnectorOptions.extractOptionAndMap(parameters, missingKey,
                                                                    names -> names.split(","), defaultValue));

        String[] columns = ConnectorOptions.extractOption(parameters, key).split(",", -1);
        assertArrayEquals(columns, result);
    }

    @Test
    public void testStringValueAndMapToBooleanWithDefault() {
        put(key, Boolean.toString(true));
        assertEquals(Boolean.TRUE, ConnectorOptions.extractOptionAndMap(parameters, key, Boolean::valueOf, false));
        assertEquals(Boolean.FALSE, ConnectorOptions.extractOptionAndMap(parameters, missingKey, Boolean::valueOf, false));

        put(key, "NotABoolean");
        assertEquals(Boolean.FALSE, ConnectorOptions.extractOptionAndMap(parameters, key, Boolean::valueOf, false));
    }

    @Test
    public void testStringValueAndMapToIntWithDefault() throws Exception {
        put(key, 2);
        assertTrue(2 == ConnectorOptions.extractOptionAndMap(parameters, key, Integer::parseInt, 3));
        assertTrue(3 == ConnectorOptions.extractOptionAndMap(parameters, missingKey, Integer::parseInt, 3));

        put(key, "NotAnInt");
        assertTrue(-1 == ConnectorOptions.extractOptionAndMap(parameters, key, Integer::parseInt, -1));
    }

    @Test
    public void testValueAndConsumer() {
        put(key, value);

        final int[] cRun = new int[1];
        final int[] success = new int[1];
        Consumer<String> c = (String v) -> {
            cRun[0] = 1;
            if (v.equals(value)) {
                success[0] = 1;
            }
        };

        cRun[0] = 0;
        success[0] = 0;
        ConnectorOptions.extractOptionAndConsume(parameters, key, c);
        assertEquals(1, cRun[0]);
        assertEquals(1, success[0]);

        //
        // Consumer not executed if key not present
        //
        cRun[0] = 0;
        success[0] = 0;
        ConnectorOptions.extractOptionAndConsume(parameters, missingKey, c);
        assertEquals(0, cRun[0]);
        assertEquals(0, success[0]);
    }

    @Test
    public void testTypedValue() {
        Colours redValue = Colours.RED;
        put(key, redValue);

        assertEquals(redValue, ConnectorOptions.extractOptionAsType(parameters, key, Colours.class));
        assertNull(ConnectorOptions.extractOptionAsType(parameters, missingKey, Colours.class));
    }
}
