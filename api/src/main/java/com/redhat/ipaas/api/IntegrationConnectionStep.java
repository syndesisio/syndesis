/*
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
 *
 */
package com.redhat.ipaas.api;

import java.io.Serializable;

public class IntegrationConnectionStep implements Serializable, IPaasEntity {

    private static final long serialVersionUID = 2637202281003772023L;
    String id;
    Integration integration;
    Connection connection;
    Step step;
    Step previousStep;
    Step nextStep;
    String type;
    
    @Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		this.id = id;
	}
	public Integration getIntegration() {
		return integration;
	}
	public void setIntegration(Integration integration) {
		this.integration = integration;
	}
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public Step getStep() {
		return step;
	}
	public void setStep(Step step) {
		this.step = step;
	}
	public Step getPreviousStep() {
		return previousStep;
	}
	public void setPreviousStep(Step previousStep) {
		this.previousStep = previousStep;
	}
	public Step getNextStep() {
		return nextStep;
	}
	public void setNextStep(Step nextStep) {
		this.nextStep = nextStep;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
    
    
}
