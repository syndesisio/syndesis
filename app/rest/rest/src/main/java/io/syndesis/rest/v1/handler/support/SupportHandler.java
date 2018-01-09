/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.handler.support;

import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.swagger.annotations.Api;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.openshift.OpenShiftConfigurationProperties;
import io.syndesis.rest.v1.handler.BaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.Map;



@Path("/support")
@Api(value = "support")
@Component
@ConditionalOnBean(SupportUtil.class)
public class SupportHandler extends BaseHandler {

    private final NamespacedOpenShiftClient client;
    private final OpenShiftConfigurationProperties config;
    private final SupportUtil util;

    private Logger LOG = LoggerFactory.getLogger(SupportHandler.class);

    public SupportHandler(final DataManager dataMgr, NamespacedOpenShiftClient openShiftClient, OpenShiftConfigurationProperties config, SupportUtil util) {
        super(dataMgr);
        this.client = openShiftClient;
        this.config = config;
        this.util = util;
    }

    @POST
    @Produces("application/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value = "/downloadSupportZip")
    public Response downloadSupportZip(Map<String, Boolean> configurationMap, @Context UriInfo uriInfo) {
        LOG.info("Received Support file request: {}", configurationMap);
        File zipFile = util.createSupportZipFile(configurationMap, uriInfo);
        return Response.ok(zipFile)
                .header("Content-Disposition",
                        "attachment; filename=\"syndesis.zip\"").build();

    }

}
