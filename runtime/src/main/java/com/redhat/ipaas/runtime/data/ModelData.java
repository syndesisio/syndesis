package com.redhat.ipaas.runtime.data;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Used to read the deployment.json file from the client GUI project
 *
 */
public class ModelData {

	private String model;
	private Object data;
	
	
	public ModelData(String model, Object data) {
		super();
		this.model = model;
		this.data = data;
	}


	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@JsonRawValue
	public String getData() {
		return data == null ? null : data.toString();
	}

	public void setData(JsonNode data) {
		this.data = data;
	}

	public ModelData() {
		super();
	}
	
}
