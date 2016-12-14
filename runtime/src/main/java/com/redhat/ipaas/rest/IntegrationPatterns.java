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
package com.redhat.ipaas.rest;

import com.redhat.ipaas.api.v1.model.IntegrationPattern;
import com.redhat.ipaas.api.v1.model.IntegrationPatternGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/integrationpatterns")
@Api(value = "integrationpatterns")
public class IntegrationPatterns {

    @Inject
    private DataManager dataMgr;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List integration patterns")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = IntegrationPattern.class)})
    public Collection<IntegrationPattern> list() {
        return dataMgr.fetchAll(IntegrationPattern.KIND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    @ApiOperation(value = "Get an integration patten by ID")
    public IntegrationPattern get(
        @ApiParam(value = "id of the IntegrationPattern", required = true) @PathParam("id") String id) {
        IntegrationPattern ip = dataMgr.fetch(IntegrationPattern.KIND, id);
        if (ip.getIntegrationPatternGroupId().isPresent()) {
            IntegrationPatternGroup ipg = dataMgr.fetch(IntegrationPatternGroup.KIND, ip.getIntegrationPatternGroupId().get());
            ip = new IntegrationPattern.Builder().createFrom(ip).integrationPatternGroup(ipg).build();
        }
        return ip;
    }

}
