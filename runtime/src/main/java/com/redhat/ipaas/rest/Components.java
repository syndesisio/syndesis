package com.redhat.ipaas.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.redhat.ipaas.api.Component;

import io.swagger.annotations.Api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Path("/components")
@Api(value = "components")
public class Components {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Component> doGet() {
		//return all Camel components
		Set<Component> components = new HashSet<Component>();
		return components;
	}
}