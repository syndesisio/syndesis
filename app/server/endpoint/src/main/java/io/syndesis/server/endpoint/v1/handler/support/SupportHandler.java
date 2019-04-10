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
package io.syndesis.server.endpoint.v1.handler.support;

import io.swagger.annotations.Api;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;



@Path("/support")
@Api(value = "support")
@Component
@ConditionalOnBean(SupportUtil.class)
public class SupportHandler extends BaseHandler {

    private final SupportUtil util;

    private static final Logger LOG = LoggerFactory.getLogger(SupportHandler.class);

    public SupportHandler(final DataManager dataMgr, SupportUtil util) {
        super(dataMgr);
        this.util = util;
    }

    @POST
    @Produces("application/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value = "/downloadSupportZip")
    public Response downloadSupportZip(Map<String, Boolean> configurationMap, @Context UriInfo uriInfo) {
        LOG.info("Received Support file request: {}", configurationMap);
        File zipFile = util.createSupportZipFile(configurationMap, uriInfo);

        return Response.ok()
            .header("Content-Disposition",
                        "attachment; filename=\"syndesis.zip\"")
            .entity(new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                try {
                    FileUtils.copyFile(zipFile, output);
                } finally {
                    if(zipFile!=null && !zipFile.delete()) {
                        zipFile.deleteOnExit();
                    }
                }
            }
        }).build();
    }

}
