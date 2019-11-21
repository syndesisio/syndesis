/*
 * Copyright (C) 2013 Red Hat, Inc.
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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.syndesis.dv.KException;
import io.syndesis.dv.RepositoryManager;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.openshift.SyndesisConnectionMonitor.EventMsg;
import io.syndesis.dv.server.endpoint.MetadataService;
import io.syndesis.dv.server.endpoint.MetadataService.SourceDeploymentMode;

/**
 * This class provides the communication and hooks
 *
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

    public void synchronizeConnections(boolean update,
            Collection<DefaultSyndesisDataSource> dataSources)
            throws KException {
        Map<String, ? extends TeiidDataSource> existing = openshiftClient
                .getDataSources().stream().collect(Collectors.toMap(TeiidDataSource::getSyndesisId, ds->{return ds;}));

        for (DefaultSyndesisDataSource sds : dataSources) {
            existing.remove(sds.getSyndesisConnectionId());
            addConnection(sds, update);
        }

        for (TeiidDataSource removed : existing.values()) {
            handleDeleteConnection(removed.getSyndesisId());
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

        if (update) {
            try {
                this.openshiftClient.deleteDataSource(sds);
            } catch (KException e) {
                LOGGER.warn("Error deleting data source for " + sds.getSyndesisName(), e);
            }
        }
        try {
            this.openshiftClient.createDataSource(sds);
        } catch (Exception e) {
            LOGGER.warn("Error creating data source for " + sds.getSyndesisName(), e);
            return;
        }

        try {
            this.metadataService.deploySourceVdb(sds.getTeiidName(), update?SourceDeploymentMode.REFRESH:SourceDeploymentMode.MAKE_LIVE);
            LOGGER.info("submitted request to fetch metadata of connection " + sds.getSyndesisName());
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch metadata for connection " + sds.getSyndesisName(), e);
        }
    }

    public void deleteConnection(DefaultSyndesisDataSource dsd) throws KException {
        try {
            if (this.metadataService.deleteSchema(dsd)) {
                LOGGER.info("Workspace schema " + dsd.getTeiidName() + " deleted.");
            } // else already deleted
        } catch (Exception e) {
            LOGGER.info("Failed to delete schema " + dsd.getTeiidName(), e);
        }

        this.openshiftClient.deleteDataSource(dsd);
        LOGGER.info("Connection deleted " + dsd.getSyndesisName());
    }

}
