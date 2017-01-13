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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
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
