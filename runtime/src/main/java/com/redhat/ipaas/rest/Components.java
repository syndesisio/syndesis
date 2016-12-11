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

import com.redhat.ipaas.api.Component;
import com.redhat.ipaas.api.ComponentGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/components")
@Api(value = "components")
public class Components {

    @Inject
    private DataManager dataMgr;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List components")
    public Collection<Component> list() {
        return dataMgr.fetchAll(Component.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    @ApiOperation(value = "Get component by ID")
    public Component get(
        @ApiParam(value = "id of the Component", required = true) @PathParam("id") String id) {
        Component component = dataMgr.fetch(Component.class, id);
        if (component.getComponentGroupId() != null) {
            ComponentGroup cg = dataMgr.fetch(ComponentGroup.class, component.getComponentGroupId());
            component.setComponentGroup(cg);
        }
        return component;
    }

}
