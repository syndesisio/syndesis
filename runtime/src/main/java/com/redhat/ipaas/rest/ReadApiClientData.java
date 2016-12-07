package com.redhat.ipaas.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReadApiClientData {
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<ModelData> readDataFromFile(String fileName) throws JsonParseException, JsonMappingException, IOException {
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
		if (is==null) throw new FileNotFoundException("Cannot find file " + fileName + " on classpath");
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(is, new TypeReference<List<ModelData>>(){});
	}

}
