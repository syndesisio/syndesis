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
package io.syndesis.server.endpoint.v1.handler.continuousdelivery;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.ContinuousDeliveryImportResults;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.monitoring.MonitoringProvider;
import io.syndesis.server.endpoint.v1.handler.connection.ConnectionHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationDeploymentHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;

@Component
@ConditionalOnProperty(value = "continuousdelivery.enabled", havingValue = "true")
public class ContinuousDeliveryHandlerImpl implements ContinuousDeliveryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ContinuousDeliveryHandlerImpl.class);

    private final DataManager dataMgr;
    private final IntegrationSupportHandler handler;
    private final EncryptionComponent encryptionComponent;
    private final IntegrationDeploymentHandler deploymentHandler;
    private final ConnectionHandler connectionHandler;
    private final MonitoringProvider monitoringProvider;
    private final Set<String> environments;

    protected ContinuousDeliveryHandlerImpl(DataManager dataMgr, IntegrationSupportHandler handler, EncryptionComponent encryptionComponent, IntegrationDeploymentHandler deploymentHandler, ConnectionHandler connectionHandler, MonitoringProvider monitoringProvider) {
        this.dataMgr = dataMgr;
        this.handler = handler;
        this.encryptionComponent = encryptionComponent;
        this.deploymentHandler = deploymentHandler;
        this.connectionHandler = connectionHandler;
        this.monitoringProvider = monitoringProvider;

        // read all existing environment names in a cache
        this.environments = new CopyOnWriteArraySet<>();
        dataMgr.fetchAll(Integration.class)
                .getItems()
                .forEach(i -> environments.addAll(i.getContinuousDeliveryState().keySet()));
    }

    @Override
    public List<String> getReleaseEnvironments() {
        return Arrays.asList(environments.toArray(new String[0]));
    }

    @Override
    public void renameEnvironment(String environment, String newEnvironment) {

        validateParam("environment", environment);
        validateParam("newEnvironment", newEnvironment);

        if (this.environments.contains(environment)) {

            // get and update list of integrations with this environment
            final List<Integration> integrations = dataMgr.fetchAll(Integration.class)
                    .getItems()
                    .stream()
                    .filter(i -> i.getContinuousDeliveryState().containsKey(environment))
                    .map(i -> {
                        final Map<String, ContinuousDeliveryEnvironment> state = new HashMap<>(i.getContinuousDeliveryState());

                        state.put(newEnvironment, state.remove(environment)
                                .builder()
                                .name(newEnvironment)
                                .build());

                        return i.builder().continuousDeliveryState(state).build();
                    })
                    .collect(Collectors.toList());

            // update environment names
            integrations.forEach(dataMgr::update);

            // update cache
            environments.add(newEnvironment);
            environments.remove(environment);

        } else {
            throw new ClientErrorException("Missing environment " + environment, Response.Status.NOT_FOUND);
        }
    }

    @Override
    public Map<String, ContinuousDeliveryEnvironment> getReleaseTags(String integrationId) {
        final Map<String, ContinuousDeliveryEnvironment> result = new HashMap<>(
                getIntegration(integrationId).getContinuousDeliveryState());
        getReleaseEnvironments().forEach(e -> result.putIfAbsent(e, null));
        return result;
    }

    @Override
    public void deleteReleaseTag(String integrationId, String environment) {

        final Integration integration = getIntegration(integrationId);
        validateParam("environment", environment);
        final Map<String, ContinuousDeliveryEnvironment> deliveryState = new HashMap<>(integration.getContinuousDeliveryState());
        if (null == deliveryState.remove(environment)) {
            throw new ClientErrorException("Missing environment tag " + environment, Response.Status.NOT_FOUND);
        }

        // update json db
        dataMgr.update(integration.builder().continuousDeliveryState(deliveryState).build());

        // update cache
        boolean found = false;
        final ListResult<Integration> integrations = dataMgr.fetchAll(Integration.class);
        for (Integration intg : integrations) {
            if (intg.getContinuousDeliveryState().get(environment) != null) {
                found = true;
                break;
            }
        }
        if (!found) {
            environments.remove(environment);
        }
    }

    @Override
    public Map<String, ContinuousDeliveryEnvironment> tagForRelease(String integrationId, List<String> environments) {

        if (environments == null || environments.isEmpty()) {
            throw new ClientErrorException("Missing parameter environments", Response.Status.BAD_REQUEST);
        }

        // fetch integration
        final Integration integration = getIntegration(integrationId);
        final HashMap<String, ContinuousDeliveryEnvironment> deliveryState = new HashMap<>(integration.getContinuousDeliveryState());

        Map<String, ContinuousDeliveryEnvironment> result = new HashMap<>();
        Date lastTaggedAt = new Date();
        for (String environment : environments) {
            // create or update tag
            result.put(environment, createOrUpdateTag(deliveryState, environment, lastTaggedAt));

            // update cache
            if (!this.environments.contains(environment)) {
                this.environments.add(environment);
            }
        }

        // update json db
        dataMgr.update(integration.builder().continuousDeliveryState(deliveryState).build());

        LOG.debug("Tagged integration {} for environments {} at {}", integrationId, environments, lastTaggedAt);

        return result;
    }

    private ContinuousDeliveryEnvironment createOrUpdateTag(Map<String,
            ContinuousDeliveryEnvironment> deliveryState, String environment, Date lastTaggedAt) {

        ContinuousDeliveryEnvironment result = deliveryState.get(environment);
        if (result == null) {
            result = ContinuousDeliveryEnvironment.Builder.createFrom(environment, lastTaggedAt);
        } else {
            result = ContinuousDeliveryEnvironment.Builder.createFrom(result, lastTaggedAt);
        }
        deliveryState.put(environment, result);
        return result;
    }

    @Override
    public StreamingOutput exportResources(String environment, boolean exportAll) throws IOException {

        // validate environment
        validateParam("environment", environment);

        // lookup integrations to export for this environment
        final ListResult<Integration> integrations;

        // export all integrations ignoring any missing/existing tags?
        if (exportAll) {

            integrations = dataMgr.fetchAll(Integration.class);

            // tag all integrations for export
            Date taggedAt = new Date();
            integrations.getItems().forEach(i -> {
                createOrUpdateTag(i.getContinuousDeliveryState(), environment, taggedAt);
                dataMgr.update(i);
            });

            LOG.debug("Exporting ALL ({}) integrations for environment {} at {} ...", integrations.getItems().size(), environment, taggedAt);
        } else {

            // export integrations freshly tagged or tagged after last export
            integrations = dataMgr.fetchAll(Integration.class, listResult -> listResult.getItems().stream().filter(i -> {

                boolean result = false;
                final Map<String, ContinuousDeliveryEnvironment> map = i.getContinuousDeliveryState();
                if (map.containsKey(environment)) {
                    final Date taggedAt = map.get(environment).getLastTaggedAt();
                    final Date exportedAt = map.get(environment).getLastExportedAt().orElse(null);
                    result = exportedAt == null || exportedAt.before(taggedAt);
                }

                return result;
            }).collect(ListResult.collector()).build());

            LOG.debug("Exporting ({}) integrations for environment {} ...", integrations.getItems().size(), environment);
        }

        final List<String> ids = integrations.getItems().stream()
                .map(integration -> integration.getId().get())
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            throw new WebApplicationException("No integrations to export", Response.Status.NO_CONTENT);
        }

        final Date exportedAt = new Date();
        final StreamingOutput output = handler.export(ids);

        // update lastExportedAt
        updateCDEnvironments(integrations.getItems(), environment, b -> b.lastExportedAt(exportedAt));

        LOG.debug("Exported ({}) integrations for environment {}", ids.size(), environment);
        return output;
    }

    private void validateParam(String name, String param) {
        if (param == null || param.isEmpty()) {
            throw new ClientErrorException("Missing parameter " + name, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public ContinuousDeliveryImportResults importResources(SecurityContext sec, ImportFormDataInput formInput) throws IOException {

        if (formInput == null) {
            throw new ClientErrorException("Multipart request is empty", Response.Status.BAD_REQUEST);
        }

        final String environment = formInput.getEnvironment();
        validateParam("environment", environment);
        final boolean deploy = Boolean.TRUE.equals(formInput.getDeploy());

        try {
            // actual import data created using the exportResources endpoint above
            final InputStream importFile = formInput.getImportFile();
            if (importFile == null) {
                throw new ClientErrorException("Missing file 'data' in multipart request", Response.Status.BAD_REQUEST);
            }
            // connection properties configuration file
            final InputStream paramFile = formInput.getParamsFile();
            if (paramFile == null) {
                throw new ClientErrorException("Missing file 'connectionparams' in multipart request", Response.Status.BAD_REQUEST);
            }

            // importedAt date to be updated in all imported integrations
            final Date lastImportedAt = new Date();

            final Map<String, List<WithResourceId>> resources = handler.importIntegration(sec, importFile);
            final List<WithResourceId> results = new ArrayList<>();
            resources.values().forEach(results::addAll);

            // get imported integrations
            final List<Integration> integrations = getResourcesOfType(results, Integration.class);

            // update importedAt field for imported integrations
            updateCDEnvironments(integrations, environment, b -> b.lastImportedAt(lastImportedAt));

            // update connection fields using paramfile
            Map<String, Map<String, String>> params = getParams(paramFile);

            final List<Connection> connections = getResourcesOfType(results, Connection.class);
            connections.forEach(c -> {
                final Map<String, String> values = params.get(c.getName());
                if (values != null) {
                    updateConnection(c, values, lastImportedAt);
                }
            });

            if (deploy) {
                // deploy integrations
                integrations.forEach(i -> publishIntegration(sec, i));
            }

            // update cache
            if (!this.environments.contains(environment)) {
                this.environments.add(environment);
            }

            return new ContinuousDeliveryImportResults.Builder()
                    .lastImportedAt(lastImportedAt)
                    .results(results)
                    .build();

        } catch (IOException e) {
            throw new ClientErrorException(String.format("Error processing multipart request: %s", e.getMessage()),
                    Response.Status.BAD_REQUEST, e);
        }
    }

    @Override
    public ConnectionOverview configureConnection(SecurityContext sec, String connectionId, Map<String, String> properties) throws IOException {

        validateParam("connectionId", connectionId);

        final Connection connection = dataMgr.fetch(Connection.class, connectionId);
        if (connection == null) {
            throw new ClientErrorException("Missing connection with id " + connectionId, Response.Status.NOT_FOUND);
        }

        updateConnection(connection, properties);
        return connectionHandler.get(connectionId);
    }

    @Override
    public IntegrationDeploymentStateDetails getIntegrationState(SecurityContext sec, String integrationId) throws
            IOException {
        validateParam("integrationId", integrationId);
        return monitoringProvider.getIntegrationStateDetails(integrationId);
    }

    @Override
    public IntegrationDeployment publishIntegration(SecurityContext sec, String integrationId) {
        final Integration integration = getIntegration(integrationId);
        return publishIntegration(sec, integration);
    }

    @Override
    public void stopIntegration(SecurityContext sec, String integrationId) {

        validateParam("integrationId", integrationId);
        IntegrationDeploymentHandler.TargetStateRequest targetState = new IntegrationDeploymentHandler
                .TargetStateRequest();
        targetState.targetState = IntegrationDeploymentState.Unpublished;

        // find current deployed version
        final IntegrationDeployment[] deployment = {null};
        dataMgr.fetchAllByPropertyValue(IntegrationDeployment
                .class, "integrationId", integrationId)
                .forEach(d -> {
                    if (d.getCurrentState() == IntegrationDeploymentState.Published) {
                        deployment[0] = d;
                    }
                });

        if (deployment[0] != null) {
            deploymentHandler.updateTargetState(integrationId, deployment[0].getVersion(), targetState);
        } else {
            throw new ClientErrorException("Integration " + integrationId + " is not published", Response.Status.FORBIDDEN);
        }
    }

    private IntegrationDeployment publishIntegration(SecurityContext sec, Integration i) {
        return deploymentHandler.update(sec, i.getId().get());
    }

    private void updateConnection(Connection c, Map<String, String> values) {
        updateConnection(c, values, new Date());
    }

    private void updateConnection(Connection c, Map<String, String> values, Date lastImportedAt) {
        // encrypt properties
        final Connector connector = dataMgr.fetch(Connector.class, c.getConnectorId());
        final Map<String, String> encryptedValues = encryptionComponent.encryptPropertyValues(values,
                connector.getProperties());

        // update connection parameters
        final HashMap<String, String> map = new HashMap<>(c.getConfiguredProperties());
        map.putAll(encryptedValues);

        // TODO how can credential flow be handled without a user session??
        // is there any way to determine which connections require manual intervention??
        dataMgr.update(c.builder()
                .configuredProperties(map)
                .lastUpdated(lastImportedAt)
                .build());
    }

    private Integration getIntegration(String integrationId) {
        validateParam("integrationId", integrationId);
        final Integration integration = dataMgr.fetch(Integration.class, integrationId);
        if (integration == null) {
            throw new ClientErrorException("Missing integration with id " + integrationId, Response.Status.NOT_FOUND);
        }
        return integration;
    }

    private Map<String, Map<String, String>> getParams(InputStream paramFile) throws IOException {
        Properties properties = new Properties();
        properties.load(paramFile);

        Map<String, Map<String, String>> result = new HashMap<>();
        properties.forEach((key1, value) -> {

            final String key = key1.toString();
            final String val = value.toString();

            // key is of the form <connection-name>.<property-name>
            final int index = key.indexOf('.');
            if (index == -1) {
                LOG.warn(String.format("Ignoring invalid substitution key: %s", key));
            } else {
                final String conn = key.substring(0, index);
                final String prop = key.substring(index + 1);

                final Map<String, String> valueMap = result.computeIfAbsent(conn, k -> new HashMap<>());
                valueMap.put(prop, val);
            }
        });

        return result;
    }

    private <T> List<T> getResourcesOfType(List<WithResourceId> resources, Class<T> type) {
        return resources.stream()
                        .filter(type::isInstance)
                        .map(type::cast)
                        .collect(Collectors.toList());
    }

    private void updateCDEnvironments(List<Integration> integrations, String environment,
                                      Function<ContinuousDeliveryEnvironment.Builder, ContinuousDeliveryEnvironment.Builder> operator) {
        integrations.forEach(i -> updateCDEnvironment(i, environment, operator));
    }

    private void updateCDEnvironment(Integration integration, String environment,
                                     Function<ContinuousDeliveryEnvironment.Builder, ContinuousDeliveryEnvironment.Builder> operator) {
        final Map<String, ContinuousDeliveryEnvironment> map = new HashMap<>(integration.getContinuousDeliveryState());
        map.put(environment, operator.apply(map.get(environment).builder()).build());
        dataMgr.update(integration.builder().continuousDeliveryState(map).build());
    }
}
