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
package io.syndesis.rest.v1.handler.tests;

import io.syndesis.dao.init.ModelData;
import io.syndesis.dao.manager.DataAccessObject;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.ListResult;
import io.syndesis.model.WithId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/test-support")
@org.springframework.stereotype.Component
@ConditionalOnProperty(value = "endpoints.test_support.enabled")
public class TestSupportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TestSupportHandler.class);

    private final DataManager dataMgr;
    private final List<DataAccessObject> daos;

    @Context
    private HttpServletRequest context;

    public TestSupportHandler(DataManager dataMgr, List<DataAccessObject> daos) {
        this.dataMgr = dataMgr;
        this.daos = daos.stream().filter(x -> !x.isReadOnly()).collect(Collectors.toList());
    }

    @GET
    @Path("/reset-db")
    public void resetDBToDefault() {
        LOG.warn("user {} is resetting DB", context.getRemoteUser());
        deleteAllDBEntities();
        dataMgr.resetDeploymentData();
    }

    @POST
    @Path("/restore-db")
    @Consumes(MediaType.APPLICATION_JSON)
    public void restoreDB(ModelData[] data) {
        LOG.warn("user {} is restoring db state", context.getRemoteUser());
        deleteAllDBEntities();
        for (ModelData modelData : data) {
            dataMgr.store(modelData);
        }
    }

    private void deleteAllDBEntities() {
        for (DataAccessObject dao : daos) {
            dataMgr.deleteAll(dao.getType());
        }
    }

    @GET
    @Path("/snapshot-db")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<ModelData> snapshotDB() {
        LOG.info("user {} is making snapshot", context.getRemoteUser());
        ArrayList<ModelData> result = new ArrayList<ModelData>();
        for (DataAccessObject dao : daos) {
            ListResult<? extends WithId> l = dao.fetchAll();
            for (WithId entity : l.getItems()) {
                result.add(new ModelData(entity.getKind(), entity));
            }
        }
        return result;
    }

}
