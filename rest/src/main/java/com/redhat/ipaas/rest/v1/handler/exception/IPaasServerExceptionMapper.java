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
package com.redhat.ipaas.rest.v1.handler.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Provider
public class IPaasServerExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

	private static final Logger LOG = LoggerFactory.getLogger(IPaasServerExceptionMapper.class);

	@Override
	public Response toResponse(Exception e) {
		LOG.error(e.getMessage(),e);
		RestError error = new RestError("Internal Server Exception. " + e.getMessage(), "Please contact the administrator and file a bug report", 500);
		return Response.status(error.errorCode).entity(error).build();
	}

}
