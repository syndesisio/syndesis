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
package io.syndesis.server.endpoint.v1.handler.environment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.environment.Environment;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;

@Path("/environments")
@Component
public class EnvironmentHandler extends BaseHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentHandler.class);
    private static final Pattern UNSAFE_CHARS = Pattern.compile("[<>\"#%{}|\\\\^~\\[\\]`;/?:@=&]");
    private static final String NAME_PROPERTY = "name";

    public EnvironmentHandler(DataManager dataMgr) {
        super(dataMgr);
    }

    @SuppressWarnings("unchecked")
    public List<String> getReleaseEnvironments() {
        return (List<String>) getReleaseEnvironments(false).getEntity();
    }

    /**
     * List all available environments.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReleaseEnvironments(@QueryParam("withUses") @ApiParam boolean withUses) {

        final Response response;
        final List<Environment> environments = getDataManager().fetchAll(Environment.class).getItems();

        if (!withUses) {
            response = Response.ok(environments.stream()
                    .map(Environment::getName)
                    .collect(Collectors.toList()))
                    .build();
        } else {
            final Map<String, Long> idCountMap = getDataManager().fetchAll(Integration.class).getItems().stream()
                    .filter(i -> !i.isDeleted())
                    .flatMap(i -> i.getContinuousDeliveryState().keySet().stream())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            final List<EnvironmentWithUses> result = new ArrayList<>();
            for (Environment env : environments) {
                result.add(new EnvironmentWithUses(env.getName(),
                        idCountMap.computeIfAbsent(env.getId().get(), k -> 0L )));
            }
            response = Response.ok(result).build();
        }

        return response;
    }

    /**
     * Add new unused environment.
     */
    @POST
    @Path("{env}")
    public void addNewEnvironment(@NotNull @PathParam("env") @ApiParam(required = true) String environment) {
        validateEnvironment("environment", environment);

        // look for duplicate environment name
        if (fetchEnvironment(environment).isPresent()) {
            throw new ClientErrorException("Duplicate environment " + environment, Response.Status.BAD_REQUEST);
        }

        getDataManager().create(new Environment.Builder().name(environment).build());
    }

    /**
     * Delete an environment across all integrations.
     */
    @DELETE
    @Path("{env}")
    public void deleteEnvironment(@NotNull @PathParam("env") @ApiParam(required = true) String environment) {

        validateEnvironment("environment", environment);
        final DataManager dataManager = getDataManager();
        final String envId = getEnvironment(environment).getId().orElse(null);

        // get and update list of integrations with this environment
        final List<Integration> integrations = dataManager.fetchAll(Integration.class).getItems().stream()
                .filter(i -> i.getContinuousDeliveryState().containsKey(envId))
                .map(i -> {
                    final Map<String, ContinuousDeliveryEnvironment> state = new HashMap<>(i.getContinuousDeliveryState());
                    // untag
                    state.remove(envId);

                    return i.builder().continuousDeliveryState(state).build();
                }).collect(Collectors.toList());

        // update integrations using this environment name
        integrations.forEach(dataManager::update);

        // delete the environment
        dataManager.delete(Environment.class, envId);
    }

    /**
     * Rename an environment across all integrations.
     */
    @PUT
    @Path("{env}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void renameEnvironment(@NotNull @PathParam("env") @ApiParam(required = true) String environment, @NotNull @ApiParam(required = true) String newEnvironment) {

        validateEnvironment("environment", environment);
        validateEnvironment("newEnvironment", newEnvironment);

        // ignore request if names are the same
        if (environment.equals(newEnvironment)) {
            return;
        }

        // check if the new environment name is in use
        if (fetchEnvironment(newEnvironment).isPresent()) {
            throw new ClientErrorException("Duplicate environment " + newEnvironment, Response.Status.BAD_REQUEST);
        }

        // find existing environment
        final Environment env = getEnvironment(environment);

        // update environment name
        getDataManager().update(new Environment.Builder().createFrom(env).name(newEnvironment).build());

    }

    /**
     * List all tags associated with this integration.
     */
    @GET
    @Path("integrations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, ContinuousDeliveryEnvironment> getReleaseTags(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId) {
        final Map<String, ContinuousDeliveryEnvironment> deliveryState =
                getIntegration(integrationId).getContinuousDeliveryState();
        return getNamedDeliveryState(deliveryState);
    }

    /**
     * Delete an environment tag associated with this integration.
     */
    @DELETE
    @Path("integrations/{id}/{env}")
    public void deleteReleaseTag(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId, @NotNull @PathParam("env") @ApiParam(required = true) String environment) {

        final Integration integration = getIntegration(integrationId);
        validateEnvironment("environment", environment);

        final Environment env = getEnvironment(environment);

        final Map<String, ContinuousDeliveryEnvironment> deliveryState = new HashMap<>(integration.getContinuousDeliveryState());
        if (null == deliveryState.remove(env.getId().get())) {
            throw new ClientErrorException("Missing environment tag " + environment, Response.Status.NOT_FOUND);
        }

        // update json db
        getDataManager().update(integration.builder().continuousDeliveryState(deliveryState).build());
    }

    /**
     * Set tags on an integration for release to target environments. Also deletes other tags.
     */
    @PUT
    @Path("integrations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, ContinuousDeliveryEnvironment> putTagsForRelease(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId, @NotNull @ApiParam(required = true) List<String> environments) {
        return tagForRelease(integrationId, environments, true);
    }

    /**
     * Add tags to an integration for release to target environments.
     */
    @PATCH
    @Path("integrations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, ContinuousDeliveryEnvironment> patchTagsForRelease(@NotNull @PathParam("id") @ApiParam(required = true) String integrationId, @NotNull @ApiParam(required = true) List<String> environments) {
        return tagForRelease(integrationId, environments, false);
    }

    public static ContinuousDeliveryEnvironment createOrUpdateTag(Map<String, ContinuousDeliveryEnvironment> deliveryState,
                                                                  String environmentId, Date lastTaggedAt) {

        ContinuousDeliveryEnvironment result = deliveryState.get(environmentId);
        if (result == null) {
            result = ContinuousDeliveryEnvironment.Builder.createFrom(environmentId, lastTaggedAt);
        } else {
            result = ContinuousDeliveryEnvironment.Builder.createFrom(result, lastTaggedAt);
        }
        deliveryState.put(environmentId, result);
        return result;
    }

    public static void validateEnvironment(String name, String value) {
        validateParam(name, value);

        // make sure it's a valid HTTP url path key
        if (UNSAFE_CHARS.matcher(value).find()) {
            throw new ClientErrorException(String.format("Invalid parameter %s:%s", name, value), Response.Status.NOT_FOUND);
        }
    }

    public Environment getEnvironment(@ApiParam(required = true) @PathParam("env") @NotNull String environment) throws ClientErrorException {
        return fetchEnvironment(environment)
                .orElseThrow(() -> new ClientErrorException("Missing environment " + environment, Response.Status.NOT_FOUND));
    }

    public void updateCDEnvironments(List<Integration> integrations, String environmentId, Date taggedAt,
                                     Function<ContinuousDeliveryEnvironment.Builder, ContinuousDeliveryEnvironment.Builder> operator) {
        integrations.forEach(i -> {
            final Map<String, ContinuousDeliveryEnvironment> map = new HashMap<>(i.getContinuousDeliveryState());
            map.put(environmentId, operator.apply(map.getOrDefault(environmentId,
                    ContinuousDeliveryEnvironment.Builder.createFrom(environmentId, taggedAt)).builder()).build());
            getDataManager().update(i.builder().continuousDeliveryState(map).build());
        });
    }

    private Map<String, ContinuousDeliveryEnvironment> tagForRelease(String integrationId, List<String> environments,
                                                                     boolean deleteOtherTags) {

        if (environments == null) {
            throw new ClientErrorException("Missing parameter environments", Response.Status.BAD_REQUEST);
        }

        // validate individual environment names
        final List<String> ids = environments.stream().
                map(e -> getEnvironment(e).getId().get())
                .collect(Collectors.toList());

        // fetch integration
        final Integration integration = getIntegration(integrationId);
        final HashMap<String, ContinuousDeliveryEnvironment> deliveryState = new HashMap<>(integration.getContinuousDeliveryState());

        Date lastTaggedAt = new Date();
        for (String envId : ids) {
            // create or update tag
            deliveryState.put(envId, createOrUpdateTag(deliveryState, envId, lastTaggedAt));
        }

        // delete tags not in the environments list?
        final Set<String> keySet = deliveryState.keySet();
        if (deleteOtherTags) {
            keySet.retainAll(ids);
        }

        // update json db
        getDataManager().update(integration.builder().continuousDeliveryState(deliveryState).build());

        LOG.debug("Tagged integration {} for environments {} at {}", integrationId, environments, lastTaggedAt);

        return getNamedDeliveryState(deliveryState);
    }

    private Map<String, ContinuousDeliveryEnvironment> getNamedDeliveryState(Map<String, ContinuousDeliveryEnvironment> deliveryState) {
        return deliveryState.values().stream()
                .filter(v -> getDataManager().fetch(Environment.class, v.getEnvironmentId()) != null) // avoid NPE
                .collect(Collectors.toMap(v -> getDataManager().fetch(Environment.class, v.getEnvironmentId()).getName(), v -> v));
    }

    private Integration getIntegration(String integrationId) {
        validateParam("integrationId", integrationId);

        // try fetching by name first, then by id
        final Integration resource = getDataManager().fetchAllByPropertyValue(Integration.class, NAME_PROPERTY, integrationId)
                .filter((Predicate<? super Integration>) i -> !i.isDeleted())
                .findFirst()
                .orElse(getDataManager().fetch(Integration.class, integrationId));
        if (resource == null) {
            throw new ClientErrorException(
                    String.format("Missing Integration with name/id %s", integrationId),
                    Response.Status.NOT_FOUND);
        }
        return resource;
    }

    private Optional<Environment> fetchEnvironment(@ApiParam(required = true) @PathParam("env") @NotNull String environment) {
        return getDataManager().fetchByPropertyValue(Environment.class, NAME_PROPERTY, environment);
    }

    private static void validateParam(String name, String param) {
        if (param == null || param.isEmpty()) {
            throw new ClientErrorException("Missing parameter " + name, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Response for {@link #getReleaseEnvironments(boolean)}.
     */
    public static class EnvironmentWithUses {

        private final String name;
        private final Long uses;

        public EnvironmentWithUses(String environment, Long uses) {
            this.name = environment;
            this.uses = uses;
        }

        public String getName() {
            return name;
        }

        public Long getUses() {
            return uses;
        }
    }

}