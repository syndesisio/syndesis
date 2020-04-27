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
package io.syndesis.dv.openshift;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teiid.spring.data.BaseConnection;
import org.teiid.spring.data.BaseConnectionFactory;

import io.syndesis.dv.KException;
import io.syndesis.dv.RepositoryManager;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.model.SourceSchema;
import io.syndesis.dv.openshift.SyndesisConnectionMonitor.EventMsg;
import io.syndesis.dv.server.endpoint.MetadataService;
import io.syndesis.dv.server.endpoint.MetadataService.SourceDeploymentMode;

/**
 * This class provides the communication and hooks
 */
@Component
public class SyndesisConnectionSynchronizer {
    private static final Log LOGGER = LogFactory.getLog(SyndesisConnectionSynchronizer.class);

    private TeiidOpenShiftClient openshiftClient;
    private MetadataService metadataService;
    private RepositoryManager repositoryManager;

    public SyndesisConnectionSynchronizer(@Autowired TeiidOpenShiftClient toc,
            @Autowired MetadataService metadataService, @Autowired RepositoryManager repositoryManager) {
        this.openshiftClient = toc;
        this.metadataService = metadataService;
        this.repositoryManager = repositoryManager;
    }

    /*
     * This method processes each connection event and delegates to appropriate
     * connection operation
     */
    public void handleConnectionEvent(final EventMsg event) throws KException {
        switch (event.getAction()) {
        case created:
            LOGGER.info("Handling CREATE connection with Event ID = " + event.getId());
            handleAddConnection(event, false);
            break;
        case deleted:
            LOGGER.info("Handling DELETE connection with Event ID = " + event.getId());
            handleDeleteConnection(event.getId());
            break;
        case updated:
            LOGGER.info("Handling UPDATE connection with Event ID = " + event.getId());
            handleAddConnection(event, true);
            break;
        }
    }

    /*
     * This method checks each applicable syndesis connection and updates all
     * associated syndesisSource vdbs and schema
     */
    public void synchronizeConnections(boolean update) throws KException {
        // Get syndesis sources
        Collection<DefaultSyndesisDataSource> dataSources = openshiftClient.getSyndesisSources();
        synchronizeConnections(update, dataSources);
    }

    public void synchronizeConnections(boolean update, Collection<DefaultSyndesisDataSource> dataSources)
            throws KException {

        List<String> existingSchemas = this.repositoryManager.findAllSourceIds();
        for (DefaultSyndesisDataSource sds : dataSources) {
            existingSchemas.remove(sds.getSyndesisConnectionId());
            addConnection(sds, update);
        }

        if (update) {
            // for these there are no syndesis connection
            for (String removed : existingSchemas) {
                SourceSchema schema = this.repositoryManager.findSchemaBySourceId(removed);
                if (schema != null) {
                    deleteConnectionSchema(removed, schema.getName());
                }
            }
        } else {
            for (String removed : existingSchemas) {
                handleDeleteConnection(removed);
            }
        }
    }

    private void handleAddConnection(EventMsg event, boolean update) throws KException {
        DefaultSyndesisDataSource sds = this.openshiftClient.getSyndesisDataSourceById(event.getId(), true);
        if (sds != null) {
            addConnection(sds, update);
        }
    }

    private void handleDeleteConnection(String id) throws KException {
        // note here that the datasource is already deleted from the syndesis
        // so we would need to search by local cached event id
        DefaultSyndesisDataSource sds = this.openshiftClient.getSyndesisDataSourceById(id, false);
        if (sds != null) {
            deleteConnection(sds);
        }
    }

    public void addConnection(DefaultSyndesisDataSource sds, boolean update) {
        // this is avoid circular creation of the virtualization connection that is
        // published through syndesis
        if (repositoryManager.findDataVirtualizationBySourceId(sds.getSyndesisConnectionId()) != null) {
            return;
        }

        boolean create = true;
        if (update) {
            try {
                //ensure that the properties have changed
                if (sds.getTeiidName() != null) {
                    TeiidDataSource tds = this.metadataService.findTeiidDatasource(sds.getTeiidName());
                    if (tds != null) {
                        DefaultSyndesisDataSource existing = tds.getSyndesisDataSource();
                        if (existing.getProperties().equals(sds.getProperties())
                                //TODO: we could preserve the metadata, but for now we'll just re-create
                                && sds.getSyndesisName().equals(tds.getSyndesisDataSource().getSyndesisName())) {
                            create = false;
                        }
                    }
                }
                if (create) {
                    this.openshiftClient.deleteDataSource(sds);
                }
            } catch (KException e) {
                LOGGER.warn("Error deleting data source for " + sds.getSyndesisName(), e);
            }
        }

        if (create) {
            try {
                this.openshiftClient.createDataSource(sds);
                validateConnection(sds);
            } catch (Exception e) {
                LOGGER.warn("Error creating data source for " + sds.getSyndesisName(), e);
                return;
            }
        }

        try {
            this.metadataService.deploySourceVdb(sds.getTeiidName(), update?SourceDeploymentMode.REFRESH:SourceDeploymentMode.MAKE_LIVE);
            LOGGER.info("submitted request to fetch metadata of connection " + sds.getSyndesisName());
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch metadata for connection " + sds.getSyndesisName(), e);
        }
    }

    private void validateConnection(DefaultSyndesisDataSource sds) throws Exception {
        for (TeiidDataSource tds : this.metadataService.getTeiidDatasources()) {
            LOGGER.warn("data sources" + tds.getSyndesisId() +"="+sds.getSyndesisConnectionId() + ", " + sds.getSyndesisName());
            if (tds.getSyndesisId().contentEquals(sds.getSyndesisConnectionId())){
                Object obj = tds.getConnectionFactory();
                if (obj instanceof BaseConnectionFactory) {
                    BaseConnection conn = ((BaseConnectionFactory<?>)obj).getConnection();
                    if (conn != null) {
                        conn.close();
                    }
                } else {
                    Connection conn = ((DataSource)obj).getConnection();
                    if (conn != null) {
                        conn.close();
                    }
                }
            }
        }
    }

    public void deleteConnection(DefaultSyndesisDataSource dsd) throws KException {
        try {
            if (this.metadataService.deleteSchema(dsd.getSyndesisConnectionId(), dsd.getTeiidName())) {
                LOGGER.info("Workspace schema " + dsd.getTeiidName() + " deleted.");
            } // else already deleted
        } catch (Exception e) {
            LOGGER.info("Failed to delete schema " + dsd.getTeiidName(), e);
        }

        this.openshiftClient.deleteDataSource(dsd);
        LOGGER.info("Connection deleted " + dsd.getSyndesisName());
    }

    public void deleteConnectionSchema(String sourceId, String teiidDatasourceName) throws KException {
        try {
            if (this.metadataService.deleteSchema(sourceId, teiidDatasourceName)) {
                LOGGER.info("Workspace schema " + teiidDatasourceName + " deleted.");
            } // else already deleted
        } catch (Exception e) {
            LOGGER.info("Failed to delete schema " + teiidDatasourceName, e);
        }
    }
}
