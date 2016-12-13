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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A connection is basically a Camel endpoint configuration (parameters) 
 * and some metadata describing which parameters are available to configure.
 */
public class Connection implements Serializable, IPaasEntity {

	private static final long serialVersionUID = -1860337496976921351L;
	private String id;
	private String name;
	private Organization organization;
	private Component component;
	private String componentId;
	private String configuredProperties;
	private String icon;
	private String description;
	private String position;
	private Set<Tag> tags;
	private String userId;
	private String organizationId;
    
    public Connection() {
 		super();
 	}
    
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
	public Organization getOrganization() {
		return organization;
	}
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	public Component getComponent() {
		return component;
	}
	public void setComponent(Component component) {
		this.component = component;
	}
	@JsonRawValue
	public String getConfiguredProperties() {
		return configuredProperties;
	}
	public void setConfiguredProperties(String configuredProperties) {
		this.configuredProperties = configuredProperties;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public Set<Tag> getTags() {
		return tags;
	}
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}
    
    
}
