/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.core.Json;
import io.syndesis.dao.init.ModelData;
import io.syndesis.dao.init.ReadApiClientData;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorGroup;
import io.syndesis.model.integration.Integration;

import org.junit.Assert;
import org.junit.Test;


public class ReadApiClientDataTest {

    private final static ObjectMapper mapper = Json.mapper();

    @Test
    public void deserializeModelDataTest() throws IOException {

        Integration integrationIn = new Integration.Builder()
                    .desiredStatus(Integration.Status.Activated)
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

}
