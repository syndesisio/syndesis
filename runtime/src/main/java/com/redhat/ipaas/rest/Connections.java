package com.redhat.ipaas.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.redhat.ipaas.api.Connection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
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