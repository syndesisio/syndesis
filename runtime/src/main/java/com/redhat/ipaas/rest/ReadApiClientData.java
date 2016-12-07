package com.redhat.ipaas.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/redhat/ipaas/rest/deployment.json");
//		System.out.println(1 + " " + Thread.currentThread().getContextClassLoader().getResourceAsStream("deployment.json"));
//		System.out.println(1 + " " + Thread.currentThread().getContextClassLoader().getResourceAsStream("/deployment.json"));
//		System.out.println(2 + " " + Thread.currentThread().getContextClassLoader().getResourceAsStream("com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(3 + " " + Thread.currentThread().getContextClassLoader().getResourceAsStream("/com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(4 + " " + this.getClass().getResourceAsStream("deployment.json"));
//		System.out.println(4 + " " + this.getClass().getResourceAsStream("/deployment.json"));
//		System.out.println(5 + " " + this.getClass().getResourceAsStream("com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(6 + " " + this.getClass().getResourceAsStream("/com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(7 + " " + ClassLoader.getSystemClassLoader().getResourceAsStream("deployment.json"));
//		System.out.println(8 + " " + ClassLoader.getSystemClassLoader().getResourceAsStream("com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(9 + " " + ClassLoader.getSystemClassLoader().getResourceAsStream("/com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(10 + " " + this.getClass().getClassLoader().getResourceAsStream("deployment.json"));
//		System.out.println(10 + " " + this.getClass().getClassLoader().getResourceAsStream("/deployment.json"));
//		System.out.println(11 + " " + this.getClass().getClassLoader().getResourceAsStream("com/redhat/ipaas/rest/deployment.json"));
//		System.out.println(12 + " " + this.getClass().getClassLoader().getResourceAsStream("/com/redhat/ipaas/rest/deployment.json"));
		
		//InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName);
		if (is==null) throw new FileNotFoundException("Cannot find file " + fileName + " on classpath");
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(is, new TypeReference<List<ModelData>>(){});
	}

}
