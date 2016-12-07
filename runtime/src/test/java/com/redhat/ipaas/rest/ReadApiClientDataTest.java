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
