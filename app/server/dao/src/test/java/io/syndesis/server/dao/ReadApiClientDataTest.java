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
package io.syndesis.server.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ModelData;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectorGroup;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.Json;
import io.syndesis.server.dao.init.ReadApiClientData;
import org.junit.Assert;
import org.junit.Test;


public class ReadApiClientDataTest {

    @Test
    public void deserializeModelDataTest() throws IOException {

        Integration integrationIn = new Integration.Builder()
                    .tags(new TreeSet<>(Arrays.asList("tag1", "tag2")))
                    .createdAt(System.currentTimeMillis())
                    .build();
        String integrationJson = Json.writer().writeValueAsString(integrationIn);
        Integration integrationOut = Json.reader().forType(Integration.class).readValue(integrationJson);

        //serialize
        ConnectorGroup cg = new ConnectorGroup.Builder().id("label").name("label").build();
        ModelData<ConnectorGroup> mdIn = new ModelData<>(Kind.ConnectorGroup, cg);
        Assert.assertEquals("{\"id\":\"label\",\"name\":\"label\"}", mdIn.getDataAsJson());

        //deserialize
        String json = Json.writer().writeValueAsString(mdIn);
        ModelData<?> mdOut = Json.reader().forType(ModelData.class).readValue(json);
        Assert.assertEquals("{\"id\":\"label\",\"name\":\"label\"}", mdOut.getDataAsJson());
    }

    @Test
    public void loadApiClientDataTest() throws IOException {
        List<ModelData<?>> modelDataList = new ReadApiClientData().readDataFromFile("io/syndesis/server/dao/deployment.json");
        Assert.assertTrue("We should find some ModelData", 0 < modelDataList.size());

        List<Object> items = new ArrayList<>();
        for (ModelData<?> md : modelDataList) {
            if (md.getKind() == Kind.ConnectorTemplate) {
                items.add(md.getData());
            }
        }

        Assert.assertFalse("We should find some Connectors", items.isEmpty());
    }

    @Test
    public void tokenReplacementDataTest() throws IOException {
        final String fileName = "io/syndesis/server/dao/test-data.json";
        final Map<String,String> env = new HashMap<>();
        env.put("POSTGRESQL_SAMPLEDB_PASSWORD", "password123");
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            ReadApiClientData readApiClientData =  new ReadApiClientData();
            String jsonText = readApiClientData.from(is);
            jsonText = readApiClientData.findAndReplaceTokens(jsonText, env);
            Assert.assertTrue(jsonText.contains("@SECRET_NOT_IN_ENV@"));
            Assert.assertFalse(jsonText.contains("@POSTGRESQL_SAMPLEDB_PASSWORD@"));

            //passing in the updated String with replaced tokens
            List<ModelData<?>> modelDataList = readApiClientData.readDataFromString(jsonText);
            Assert.assertTrue("We should find some ModelData", 0 < modelDataList.size());

            //the second item is the sampledb-connection
            Connection connection = (Connection) modelDataList.get(1).getData();
            String pw = connection.getConfiguredProperties().get("password");
            Assert.assertEquals("password123", pw);
        }
    }
}
