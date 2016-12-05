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

import com.redhat.ipaas.api.Connection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Path("/connections")
@Api(value = "connections")
public class Connections {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Connection> list() {
		
		Set<Connection> connections = new HashSet<Connection>();
		return connections;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Connection get(@ApiParam String id) {
		
		Connection connection = new Connection();
		return connection;
	}
	
	
}