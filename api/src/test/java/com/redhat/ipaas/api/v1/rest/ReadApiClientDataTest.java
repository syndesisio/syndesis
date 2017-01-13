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
package com.redhat.ipaas.api.v1.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.redhat.ipaas.api.v1.model.ComponentGroup;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ReadApiClientDataTest {

    private final static ObjectMapper mapper = getObjectMapper();

	private static ObjectMapper getObjectMapper() {
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.registerModule(new Jdk8Module());
	    return objectMapper;
    }

	@Test
	public void deserializeModelDataTest() throws IOException {

		//serialize
		ComponentGroup cg = new ComponentGroup.Builder().id("label").name("label").build();
		ModelData mdIn = new ModelData(ComponentGroup.KIND,  mapper.writeValueAsString(cg));
		assertEquals("{\"id\":\"label\",\"name\":\"label\"}", mdIn.getData());

		//deserialize
		String json = mapper.writeValueAsString(mdIn);
		ModelData mdOut = mapper.readValue(json, ModelData.class);
		assertEquals("{\"id\":\"label\",\"name\":\"label\"}", mdOut.getData());
	}

	@Test
	public void loadApiClientDataTest() throws IOException {
		List<ModelData> modelDataList = new ReadApiClientData().readDataFromFile("com/redhat/ipaas/api/v1/deployment.json");
		System.out.println("Found " + modelDataList.size() + " entities.");
		assertTrue("We should find some ModelData", 0 < modelDataList.size());
		List<ComponentGroup> componentGroupList = new ArrayList<ComponentGroup>();
		for (ModelData md : modelDataList) {
			if (md.getModel().equalsIgnoreCase("componentgroup")) {
				ComponentGroup cg = mapper.readValue(md.getData(), ComponentGroup.class);
				componentGroupList.add(cg);
			}
		}
		System.out.println("Found " + componentGroupList.size() + " ComponentGroups");
		assertTrue("We should find some ComponentGroups", 0 < componentGroupList.size());
	}

}
