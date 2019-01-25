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
package io.syndesis.connector.odata;

import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.domain.ClientCollectionValueImpl;
import org.apache.olingo.client.core.domain.ClientComplexValueImpl;
import org.apache.olingo.client.core.domain.ClientEntityImpl;
import org.apache.olingo.client.core.domain.ClientPrimitiveValueImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.syndesis.connector.odata.customizer.json.ClientCollectionValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientComplexValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientEntitySerializer;
import io.syndesis.connector.odata.customizer.json.ClientEnumValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientPrimitiveValueSerializer;
import io.syndesis.connector.odata.customizer.json.ClientPropertySerializer;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        ODataSerializerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@TestExecutionListeners(
    listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
    }
)
public class ODataSerializerTest extends AbstractODataRouteTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEST_COLLECTION = "test-collection.json";
    private static final String TEST_COMPLEX = "test-complex.json";

    @BeforeClass
    public static void setupClass() {
        SimpleModule module = new SimpleModule(ClientEntitySet.class.getSimpleName(),
                                                       new Version(1, 0, 0, null, null, null));
        module
            .addSerializer(new ClientEntitySerializer())
            .addSerializer(new ClientPropertySerializer())
            .addSerializer(new ClientPrimitiveValueSerializer())
            .addSerializer(new ClientEnumValueSerializer())
            .addSerializer(new ClientCollectionValueSerializer())
            .addSerializer(new ClientComplexValueSerializer(OBJECT_MAPPER));
        OBJECT_MAPPER.registerModule(module);
    }

    private void checkEntity(ClientEntity entity, String testDataFile) throws Exception {
        String json = OBJECT_MAPPER.writeValueAsString(entity);
        String expected = testData(testDataFile);
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    public void testSerializerCollection() throws Exception {
        ClientEntity entity = new ClientEntityImpl(null);
        ClientPrimitiveValue test1 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test1");
        ClientPrimitiveValue test2 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test2");
        ClientPrimitiveValue test3 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test3");

        ClientCollectionValueImpl<ClientPrimitiveValue> collValue = new ClientCollectionValueImpl<ClientPrimitiveValue>("testCollection");
        collValue.add(test1);
        collValue.add(test2);
        collValue.add(test3);

        ClientProperty prop = new ClientPropertyImpl("testCollection", collValue);
        entity.getProperties().add(prop);

        checkEntity(entity, TEST_COLLECTION);
    }

    @Test
    public void testSerializerComplex() throws Exception {
        ClientEntity entity = new ClientEntityImpl(null);
        ClientPrimitiveValue test1 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test1");
        ClientPrimitiveValue test2 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test2");
        ClientPrimitiveValue test3 = new ClientPrimitiveValueImpl.BuilderImpl().buildString("test3");

        ClientComplexValueImpl complexValue = new ClientComplexValueImpl("testComplex");
        complexValue.add(new ClientPropertyImpl("test1", test1));
        complexValue.add(new ClientPropertyImpl("test2", test2));
        complexValue.add(new ClientPropertyImpl("test3", test3));
        entity.getProperties().add(new ClientPropertyImpl("testComplex", complexValue));

        checkEntity(entity, TEST_COMPLEX);
    }
}
