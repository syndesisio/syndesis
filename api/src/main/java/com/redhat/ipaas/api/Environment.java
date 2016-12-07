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

public class Environment implements Serializable, IPaasEntity {

    private static final long serialVersionUID = -4311560785106816407L;
    String id;
    String name;
    EnvironmentType environmentType;
    Set<Organization> organization;
    
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
	public EnvironmentType getEnvironmentType() {
		return environmentType;
	}
	public void setEnvironmentType(EnvironmentType environmentType) {
		this.environmentType = environmentType;
	}
	public Set<Organization> getOrganization() {
		return organization;
	}
	public void setOrganization(Set<Organization> organization) {
		this.organization = organization;
	}
    
}
