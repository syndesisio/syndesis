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
package io.syndesis.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.core.Json;
import io.syndesis.model.ModelData;
import io.syndesis.dao.init.ReadApiClientData;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorGroup;
import io.syndesis.model.integration.Integration;

import io.syndesis.model.integration.IntegrationDeploymentState;
import org.junit.Assert;
import org.junit.Test;


public class ReadApiClientDataTest {

    private final static ObjectMapper mapper = Json.mapper();

    @Test
    public void deserializeModelDataTest() throws IOException {

        Integration integrationIn = new Integration.Builder()
                    .desiredStatus(IntegrationDeploymentState.Active)
                    .tags(new TreeSet<>(Arrays.asList("tag1", "tag2")))
                    .createdDate(new Date())
                    .build();
        String integrationJson = mapper.writeValueAsString(integrationIn);
        System.out.println(integrationJson);
        Integration integrationOut = mapper.readValue(integrationJson, Integration.class);
        Assert.assertEquals(integrationIn.getDesiredStatus(), integrationOut.getDesiredStatus());

        //serialize
        ConnectorGroup cg = new ConnectorGroup.Builder().id("label").name("label").build();
        ModelData<ConnectorGroup> mdIn = new ModelData<>(Kind.ConnectorGroup, cg);
        Assert.assertEquals("{\"id\":\"label\",\"name\":\"label\"}", mdIn.getDataAsJson());

        //deserialize
        String json = mapper.writeValueAsString(mdIn);
        ModelData<?> mdOut = mapper.readValue(json, ModelData.class);
        Assert.assertEquals("{\"id\":\"label\",\"name\":\"label\"}", mdOut.getDataAsJson());
    }

    @Test
    public void loadApiClientDataTest() throws IOException {
        List<ModelData<?>> modelDataList = new ReadApiClientData().readDataFromFile("io/syndesis/dao/deployment.json");
        System.out.println("Found " + modelDataList.size() + " entities.");
        Assert.assertTrue("We should find some ModelData", 0 < modelDataList.size());
        List<Connector> connectorList = new ArrayList<>();
        for (ModelData<?> md : modelDataList) {
            if (md.getKind() == Kind.Connector) {
                Connector cg = (Connector) md.getData();
                connectorList.add(cg);
            }
        }
        System.out.println("Found " + connectorList.size() + " Connectors");
        Assert.assertTrue("We should find some Connectors", 0 < connectorList.size());
    }

    @Test
    public void tokenReplacementDataTest() throws IOException {
        final String fileName = "io/syndesis/dao/test-data.json";
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
            System.out.println("Found " + modelDataList.size() + " entities.");
            Assert.assertTrue("We should find some ModelData", 0 < modelDataList.size());

            //the second item is the sampledb-connection
            Connection connection = (Connection) modelDataList.get(1).getData();
            String pw = connection.getConfiguredProperties().get("password");
            Assert.assertEquals("password123", pw);
        }
    }
}
