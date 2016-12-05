package com.redhat.ipaas.runtime.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	protected List<ModelData> readDataFromFile(String fileName) throws JsonParseException, JsonMappingException, IOException {
		File apiClientDataFile = new File(fileName);
		boolean exists = apiClientDataFile.exists();
		if (!exists) throw new FileNotFoundException("Cannot find file " + fileName);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(apiClientDataFile, new TypeReference<List<ModelData>>(){});
	}

}
