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
import java.util.Set;

public class Integration implements Serializable, IPaasEntity{
   
    private static final long serialVersionUID = -1557934137547343303L;
    String id;
    String name;
    String configuration;
    IntegrationTemplate integrationTemplate;
    Set<User> users;
    Set<Tag> tags;
    
    @Override
	public String getId() {
		return id;
	}
    @Override
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getConfiguration() {
		return configuration;
	}
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	public IntegrationTemplate getIntegrationTemplate() {
		return integrationTemplate;
	}
	public void setIntegrationTemplate(IntegrationTemplate integrationTemplate) {
		this.integrationTemplate = integrationTemplate;
	}
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	public Set<Tag> getTags() {
		return tags;
	}
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
}
