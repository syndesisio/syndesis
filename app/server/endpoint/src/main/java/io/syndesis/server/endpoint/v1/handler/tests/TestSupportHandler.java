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
package io.syndesis.server.endpoint.v1.handler.tests;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.fabric8.openshift.api.model.DeploymentConfig;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.ModelData;
import io.syndesis.common.model.WithId;
import io.syndesis.common.util.backend.BackendController;
import io.syndesis.common.util.cache.CacheManager;
import io.syndesis.server.dao.manager.DataAccessObject;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Path("/test-support")
@org.springframework.stereotype.Component
@ConditionalOnProperty(value = "endpoints.test_support.enabled")
public class TestSupportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TestSupportHandler.class);

    private final DataManager dataMgr;
    private final List<DataAccessObject<?>> daos;
    private final OpenShiftService openShiftService;

    @Context
    private HttpServletRequest context;

    private final DBI dbi;

    private CacheManager cacheManager;

    private Collection<BackendController> controllers;

    public TestSupportHandler(DBI dbi, DataManager dataMgr, CacheManager cacheManager, List<DataAccessObject<?>> daos, OpenShiftService openShiftService, Collection<BackendController> controllers) {
        this.dbi = dbi;
        this.dataMgr = dataMgr;
        this.cacheManager = cacheManager;
        this.controllers = controllers;
        this.daos = daos.stream().filter(x -> !x.isReadOnly()).collect(Collectors.toList());
        this.openShiftService = openShiftService;
    }

    @GET
    @Path("/reset-db")
    public void resetDBToDefault() {
        final String user = context.getRemoteUser();
        LOG.warn("user {} is resetting DB", user);
        // Deployments must be also deleted because we it is not possible to reach them after deleting DB.
        deleteDeployments();
        stopControllers();
        deleteAllDBEntities();
        startControllers();
        dataMgr.resetDeploymentData();
        LOG.warn("user {} reset the DB", user);
    }

    @POST
    @Path("/restore-db")
    @Consumes(MediaType.APPLICATION_JSON)
    public void restoreDB(ModelData<?>... data) {
        final String user = context.getRemoteUser();
        LOG.warn("user {} is restoring db state", user);
        resetDBToDefault();
        for (ModelData<?> modelData : data) {
            dataMgr.store(modelData);
        }
        LOG.warn("user {} restored db state", user);
    }

    @GET
    @Path("/delete-deployments")
    public void deleteDeployments() {
        final String user = context.getRemoteUser();
        LOG.warn("user {} is deleting all integration deploymets", user);
        final List<DeploymentConfig> integrationDeployments = openShiftService.getDeploymentsByLabel(Collections.singletonMap(OpenShiftService.INTEGRATION_ID_LABEL, null));
        for (DeploymentConfig integrationDeployment : integrationDeployments) {
            final String integrationDeploymentName = integrationDeployment.getMetadata().getName().replaceFirst("^i-", "");
            LOG.debug("Deleting integration \"{}\"", integrationDeploymentName);
            openShiftService.delete(integrationDeploymentName);
        }
        LOG.warn("user {} deleted all integration deploymets", user);
    }

    private void deleteAllDBEntities() {
        dbi.withHandle(h -> {
            final DatabaseMetaData databaseMetaData = h.getConnection().getMetaData();
            if ("PostgreSQL".equalsIgnoreCase(databaseMetaData.getDatabaseProductName())) {
                h.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='syndesis' AND pid != pg_backend_pid()");
            }
            h.execute("TRUNCATE TABLE jsondb");
            h.execute("TRUNCATE TABLE filestore");
            h.execute("TRUNCATE TABLE config");

            return null;
        });
        cacheManager.evictAll();
    }

    private void startControllers() {
        controllers.forEach(BackendController::start);
    }

    private void stopControllers() {
        controllers.forEach(BackendController::stop);
    }

    @GET
    @Path("/snapshot-db")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ModelData<?>> snapshotDB() {
        LOG.info("user {} is making snapshot", context.getRemoteUser());
        ArrayList<ModelData<?>> result = new ArrayList<>();
        for (DataAccessObject<?> dao : daos) {
            ListResult<? extends WithId<?>> l = dao.fetchAll();
            for (WithId<?> entity : l.getItems()) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                ModelData<?> modelData = new ModelData(entity.getKind(), entity);
                result.add(modelData);
            }
        }
        return result;
    }

}
