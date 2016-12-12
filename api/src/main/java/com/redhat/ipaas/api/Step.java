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
package com.redhat.ipaas.api;

import java.io.Serializable;

public class Step implements Serializable, IPaasEntity {
   
    private static final long serialVersionUID = 1588083078080824197L;
    String id;
    IntegrationPattern integrationPattern;
    String configuredProperties;
    
    @Override
	public String getId() {
		return id;
	}
    @Override
	public void setId(String id) {
		this.id = id;
	}
	public IntegrationPattern getIntegrationPattern() {
		return integrationPattern;
	}
	public void setIntegrationPattern(IntegrationPattern integrationPattern) {
		this.integrationPattern = integrationPattern;
	}
	public String getConfiguredProperties() {
		return configuredProperties;
	}
	public void setConfiguredProperties(String configuredProperties) {
		this.configuredProperties = configuredProperties;
	}
    
}
