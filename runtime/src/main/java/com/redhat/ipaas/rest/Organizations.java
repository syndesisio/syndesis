package com.redhat.ipaas.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.redhat.ipaas.api.Organization;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Path("/organizations")
public class Organizations {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	
	public Set<Organization> doGet() {
		
		Set<Organization> orgs = new HashSet<Organization>();
		return orgs;
	}
}