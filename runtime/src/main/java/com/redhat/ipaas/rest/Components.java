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
package com.redhat.ipaas.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.redhat.ipaas.api.Component;

import io.swagger.annotations.Api;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.infinispan.Cache;

@Path("/components")
@Api(value = "components")
public class Components {

	@Inject
	Cache<String, String> cache;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Component> list() {
		// return all Camel components
		cache.keySet();
		System.out.println("*******************" + cache);
		Set<Component> components = new HashSet<Component>();
		return components;
	}

}