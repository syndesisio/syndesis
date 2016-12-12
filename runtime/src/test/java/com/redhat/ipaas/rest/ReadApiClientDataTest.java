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
package com.redhat.ipaas.rest;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.api.ComponentGroup;
import com.redhat.ipaas.rest.ModelData;
import com.redhat.ipaas.rest.ReadApiClientData;


public class ReadApiClientDataTest {

	private static ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void deserializeModelDateTest() throws JsonParseException, JsonMappingException, IOException {
		
		//serialize
		ComponentGroup cg = new ComponentGroup("label", "label");
		ModelData mdIn = new ModelData("ComponentGroup",  mapper.writeValueAsString(cg));
		assertTrue("{\"id\":\"label\",\"name\":\"label\"}".equals(mdIn.getData()));
		
		//deserialize
		String json = mapper.writeValueAsString(mdIn);
		ModelData mdOut = mapper.readValue(json, ModelData.class);
		assertTrue("{\"id\":\"label\",\"name\":\"label\"}".equals(mdOut.getData()));
	}
	
	@Test
	public void loadApiClientDataTest() throws JsonParseException, JsonMappingException, IOException {
		List<ModelData> modelDataList = new ReadApiClientData().readDataFromFile("com/redhat/ipaas/rest/deployment.json");
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
