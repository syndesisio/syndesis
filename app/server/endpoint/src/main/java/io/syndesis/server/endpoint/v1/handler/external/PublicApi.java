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
package io.syndesis.server.endpoint.v1.handler.external;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.ContinuousDeliveryImportResults;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;

@Api("public-api")
@Path("/public")
public interface PublicApi {

    /**
     * List all available environments.
     */
    @GET
    @Path("environments")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getReleaseEnvironments();

    /**
     * Rename an environment across all integrations.
     */
    @PUT
    @Path("environments/{env}")
    @Consumes(MediaType.APPLICATION_JSON)
    void renameEnvironment(@NotNull @PathParam("env") @ApiParam(required = true) String environment, @NotNull @ApiParam(required = true) String newEnvironment);

    /**
     * List all tags associated with this integration.
     */
    @GET
    @Path("integrations/{id}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, ContinuousDeliveryEnvironment> getReleaseTags(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId);

    /**
     * Delete an environment tag associated with this integration.
     */
    @DELETE
    @Path("integrations/{id}/tags/{env}")
    void deleteReleaseTag(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId, @NotNull @PathParam("env") @ApiParam(required = true) String environment);

    /**
     * Tag an integration for release to target environments.
     */
    @POST
    @Path("integrations/{id}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Map<String, ContinuousDeliveryEnvironment> tagForRelease(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId,
                                                @NotNull @ApiParam(required = true) List<String> environments);

    /**
     * Export integrations to a target environment.
     */
    @GET
    @Path("integrations/{env}/export.zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    StreamingOutput exportResources(@NotNull @PathParam("env") @ApiParam(required = true) String environment,
                           @QueryParam("all") @ApiParam() boolean exportAll) throws IOException;

    /**
     * Import integrations into a target environment.
     */
    @POST
    @Path("integrations")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    ContinuousDeliveryImportResults importResources(@Context SecurityContext sec, @NotNull @ApiParam ImportFormDataInput formInput) throws IOException;

    /**
     * Configure a connection.
     */
    @POST
    @Path("connections/{id}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ConnectionOverview configureConnection(@Context SecurityContext sec, @NotNull @PathParam("id") @ApiParam String connectionId, @NotNull @ApiParam Map<String, String> properties) throws IOException;

    /**
     * Get Integration state.
     */
    @GET
    @Path("integrations/{id}/state")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    IntegrationDeploymentStateDetails getIntegrationState(@Context SecurityContext sec, @NotNull @PathParam("id") @ApiParam String integrationId) throws IOException;

    /**
     * Start Integration.
     */
    @POST
    @Path("integrations/{id}/deployments")
    @Produces(MediaType.APPLICATION_JSON)
    IntegrationDeployment publishIntegration(@Context final SecurityContext sec, @NotNull @PathParam("id") @ApiParam(required = true) final String integrationId);

    /**
     * Stop Integration.
     */
    @PUT
    @Path("integrations/{id}/deployments/stop")
    @Produces(MediaType.APPLICATION_JSON)
    void stopIntegration(@Context final SecurityContext sec, @NotNull @PathParam("id") @ApiParam(required = true) final String integrationId);

    /**
     * DTO for {@link org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput} for importResources.
     */
    class ImportFormDataInput {
        @FormParam("data")
        private InputStream data;

        @FormParam("properties")
        private InputStream properties;

        @FormParam("environment")
        private String environment;

        @FormParam("deploy")
        private Boolean deploy;

        public InputStream getData() {
            return data;
        }

        public void setData(InputStream data) {
            this.data = data;
        }

        public InputStream getProperties() {
            return properties;
        }

        public void setProperties(InputStream properties) {
            this.properties = properties;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public Boolean getDeploy() {
            return deploy;
        }

        public void setDeploy(Boolean deploy) {
            this.deploy = deploy;
        }
    }
}
