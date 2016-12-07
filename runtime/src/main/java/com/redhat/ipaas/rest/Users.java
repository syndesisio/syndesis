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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import com.redhat.ipaas.api.IPaasEntity;
import com.redhat.ipaas.api.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.infinispan.Cache;

@Path("/users")
@Api(value = "users")
public class Users {

	@Inject
	Cache<String, Map<String,IPaasEntity>> cache;
	
	private DataManager dataMgr;
	
	@PostConstruct
	public void init() {
		dataMgr = new DataManager(cache);
		dataMgr.init();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<User> list() {
		return dataMgr.fetchAll(User.class);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value="/{id}")
	public User get(
			@ApiParam(value = "id of the User", required = true) @PathParam("id") String id) {
		User user = dataMgr.fetch(User.class,id);
		
		return user;
	}

}