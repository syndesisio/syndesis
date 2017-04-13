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
package com.redhat.ipaas.rest.v1.handler.integration;

import com.redhat.ipaas.core.EventBus;
import com.redhat.ipaas.core.IPaasServerException;
import com.redhat.ipaas.core.Json;
import com.redhat.ipaas.core.Tokens;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.ChangeEvent;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.rest.v1.handler.BaseHandler;
import com.redhat.ipaas.rest.v1.operations.Creator;
import com.redhat.ipaas.rest.v1.operations.Deleter;
import com.redhat.ipaas.rest.v1.operations.Getter;
import com.redhat.ipaas.rest.v1.operations.Lister;
import com.redhat.ipaas.rest.v1.operations.Updater;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.annotations.Api;

@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler implements Lister<Integration>, Getter<Integration>, Creator<Integration>, Deleter<Integration>, Updater<Integration> {

    private static final String INTEGRATION_CONTROLLER_SUBSCRIBER_ID = "integration-controller";

    private final EventBus eventBus;

    @Value("${openshift.apiBaseUrl}")
    private String openshiftApiBaseUrl;

    @Value("${openshift.namespace}")
    private String namespace;

    public IntegrationHandler(DataManager dataMgr, EventBus eventBus) {
        super(dataMgr);
        this.eventBus = eventBus;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Integration;
    }


    @PUT
    @Path(value = "/{id}/activate")
    @Consumes("application/json")
    public void activate(@PathParam("id") String id)
    {
        update(id, new Integration.Builder().createFrom(get(id)).desiredStatus(Integration.Status.Activated).build());
    }

    @PUT
    @Path(value = "/{id}/deactivate")
    @Consumes("application/json")
    public void deactivate(@PathParam("id") String id)
    {
        update(id, new Integration.Builder().createFrom(get(id)).desiredStatus(Integration.Status.Activated).build());
    }

    @Override
    public Integration get(String id) {
        Integration integration = Getter.super.get(id);

        //fudging the timesUsed for now
        if (integration.getCurrentStatus().equals(Integration.Status.Activated)) {
            Integration updatedIntegration = integration = new Integration.Builder()
                    .createFrom(integration)
                    .timesUsed(BigInteger.valueOf(new Date().getTime()/1000000))
                    .build();
            return updatedIntegration;
        } else {
            return integration;
        }
    }

    @Override
    public Integration create(Integration integration) {
        try {
            Date rightNow = new Date();
            Integration updatedIntegration = new Integration.Builder()
                .createFrom(integration)
                .token(Tokens.getAuthenticationToken())
                .currentStatus(Integration.Status.Draft)
                .desiredStatus(Integration.Status.Activated)
                .lastUpdated(rightNow)
                .createdDate(rightNow)
                .build();

            return Creator.super.create(updatedIntegration);
        } finally {
            eventBus.send(INTEGRATION_CONTROLLER_SUBSCRIBER_ID, "change-event", newEvent(integration.getId(), "created"));
        }
    }


    @Override
    public void update(String id, Integration integration) {
        try {
            Integration updatedIntegration = new Integration.Builder()
                .token(Tokens.getAuthenticationToken())
                .createFrom(integration)
                .lastUpdated(new Date())
                .build();

            Updater.super.update(id, updatedIntegration);
        }  finally {
            eventBus.send(INTEGRATION_CONTROLLER_SUBSCRIBER_ID, "change-event", newEvent(integration.getId(), "updated"));
        }
    }

    @Override
    public void delete(@PathParam("id") String id) {
        try {
            Deleter.super.delete(id);
        } finally {
            eventBus.send(INTEGRATION_CONTROLLER_SUBSCRIBER_ID, "change-event", newEvent(Optional.of(id), "deleted"));
        }
    }

    private static final String newEvent(Optional<String> id, String action) {
        try {
            return Json.mapper().writeValueAsString(new ChangeEvent.Builder()
                .kind(Kind.Integration.getModelName())
                .id(id)
                .action(action).build());
        } catch (Throwable t) {
            throw IPaasServerException.launderThrowable(t);
        }
    }
}
