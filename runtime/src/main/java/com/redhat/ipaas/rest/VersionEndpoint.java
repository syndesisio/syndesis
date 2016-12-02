package com.redhat.ipaas.rest;

import com.redhat.ipaas.api.Version;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Path("/version")
public class VersionEndpoint {

	@GET
	@Produces("text/plain")
	public Response doGet() {
		return Response.ok(Version.getVersion()).build();
	}
}