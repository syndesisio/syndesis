/*
`	 * Copyright (C) 2016 Red Hat, Inc.
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
/**
 * ComponentGroups are labels in Camel. 
 * 
 * https://github.com/apache/camel/blob/master/platforms/catalog/src/main/java/org/apache/camel/catalog/CamelCatalog.java#L233
 * 
 * @author kstam
 *
 */
public class ComponentGroup implements Serializable {

    private static final long serialVersionUID = -7751366211175725297L;
    String id;
    String name;
    
	public ComponentGroup(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public ComponentGroup() {
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
    
    
}
