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
package io.syndesis.server.endpoint.v1.handler.user;

import io.swagger.annotations.Api;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.user.Quota;
import io.syndesis.common.model.user.User;
import io.syndesis.common.util.Labels;
import io.syndesis.server.openshift.OpenShiftService;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Path("/users")
@Api(value = "users")
@Component
public class UserHandler extends BaseHandler implements Lister<User>, Getter<User> {

    private final OpenShiftService openShiftService;
    private final UserConfigurationProperties properties;

    @Autowired
    public UserHandler(DataManager dataMgr, OpenShiftService openShiftService, UserConfigurationProperties properties) {
        super(dataMgr);
        this.openShiftService = openShiftService;
        this.properties = properties;
    }

    @Override
    public Kind resourceKind() {
        return Kind.User;
    }

    @Path("~")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User whoAmI(@Context SecurityContext sec) {
        String username = sec.getUserPrincipal().getName();
        io.fabric8.openshift.api.model.User openShiftUser = this.openShiftService.whoAmI(username);
        Assert.notNull(openShiftUser, "A valid user is required");
        return new User.Builder().username(openShiftUser.getMetadata().getName())
                .fullName(Optional.ofNullable(openShiftUser.getFullName()))
                .name(Optional.ofNullable(openShiftUser.getFullName())).build();
    }

    @Path("~/quota")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Quota quota(@Context SecurityContext sec) {
        String username = sec.getUserPrincipal().getName();
        io.fabric8.openshift.api.model.User openShiftUser = this.openShiftService.whoAmI(username);
        Assert.notNull(openShiftUser, "A valid user is required");
        return new Quota.Builder()
                .maxDeploymentsPerUser(properties.getMaxDeploymentsPerUser())
                .maxIntegrationsPerUser(properties.getMaxIntegrationsPerUser())
                .usedDeploymentsPerUser(countDeployments(username))
                .usedIntegrationsPerUser(countActiveIntegrations(username))
                .build();
    }

    /**
     * Counts active integrations (in DB) of the owner of the specified integration.
     *
     * @param deployment The specified IntegrationDeployment.
     * @return The number of integrations (excluding the current).
     */
    private int countActiveIntegrations(String username) {

        return (int) getDataManager().fetchAll(IntegrationDeployment.class).getItems()
            .stream()
            .filter(i -> IntegrationDeploymentState.Published == i.getCurrentState())
            .filter(i -> i.getUserId().map(username::equals).orElse(Boolean.FALSE))
            .count();
    }

    /**
     * Count the deployments of the owner of the specified integration.
     *
     * @param username The specified user
     * @return The number of deployed integrations.
     */
    private int countDeployments(String username) {

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username));

        return (int) this.openShiftService.getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> d.getSpec().getReplicas() > 0)
            .count();
    }
}
