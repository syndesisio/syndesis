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
import java.util.Properties;

public class Component implements Serializable {

	private static final long serialVersionUID = -4372417241895695792L;
    private String id;
    private String name;
    private String icon;
    private Properties properties;
    private ComponentGroup componentGroup;
    
    public Component() {
		super();
	}
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public ComponentGroup getComponentGroup() {
		return componentGroup;
	}
	public void setComponentGroup(ComponentGroup componentGroup) {
		this.componentGroup = componentGroup;
	}
    
    
}
