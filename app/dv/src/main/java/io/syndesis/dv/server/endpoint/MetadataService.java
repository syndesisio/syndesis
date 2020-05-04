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
package io.syndesis.dv.server.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.adminapi.Model.Type;
import org.teiid.adminapi.VDB.Status;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBImportMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.deployers.CompositeVDB;
import org.teiid.deployers.VDBLifeCycleListener;
import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.Schema;

import com.google.common.util.concurrent.Striped;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.dv.KException;
import io.syndesis.dv.StringConstants;
import io.syndesis.dv.metadata.MetadataInstance;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.internal.DDLDBMetadataRepository;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.metadata.query.QSResult;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.SourceSchema;
import io.syndesis.dv.openshift.SyndesisConnectionMonitor;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;
import io.syndesis.dv.server.DvService;
import io.syndesis.dv.server.Messages;
import io.syndesis.dv.server.V1Constants;
import io.syndesis.dv.utils.PathUtils;
import io.syndesis.dv.utils.StringUtils;
/**
 * A REST service for obtaining information from a metadata instance.
 */
@RestController
@RequestMapping( V1Constants.APP_PATH+V1Constants.FS+V1Constants.METADATA_SEGMENT )
@Api( tags = {V1Constants.METADATA_SEGMENT} )
public class MetadataService extends DvService implements ServiceVdbGenerator.SchemaFinder {

    private static final String FAILED_DDL = "--failed: "; //$NON-NLS-1$

    private static final String VERSION_PROPERTY = "version"; //$NON-NLS-1$

    private static final String CONNECTION_VDB_SUFFIX = "conn"; //$NON-NLS-1$

    private static final String LOAD_SUFFIX = "-load"; //$NON-NLS-1$

    /**
     * fqn table option key
     */
    public static final String TABLE_OPTION_FQN = AbstractMetadataRecord.RELATIONAL_PREFIX+"fqn"; //$NON-NLS-1$

    @Autowired
    private MetadataInstance metadataInstance;

    /**
     * Anything that updates the main preview vdb or can
     * undeploy an active connection vdb will be delegated
     * to the connectionExecutor to remove the need for further locking
     */
    @Autowired
    private ScheduledThreadPoolExecutor connectionExecutor;

    /**
     * locks to make sure request threads don't step on each other's
     * vdb deployments/undeployments. The keys are either dv names, or
     * source vdb names
     */
    private Striped<Lock> previewVdbLocks = Striped.lazyWeakLock(32);
    /**
     * lock for operations that depend on / affect the master preview vdb
     */
    private Object masterLock = new Object();

    private MetadataInstance getMetadataInstance() {
        return metadataInstance;
    }

    /**
     * Does not need to be transactional as it only affects the runtime instance
     */
    public void removeVdb(final String vdbName) throws KException {
        getMetadataInstance().undeployDynamicVdb(vdbName);
    }

    public void refreshPreviewVdb() throws KException {
        VDBMetaData workingCopy = new VDBMetaData();
        workingCopy.setName(EditorService.PREVIEW_VDB);
        workingCopy.addProperty("preview", "true");  //$NON-NLS-1$ //$NON-NLS-2$

        Collection<TeiidVdb> vdbs = getMetadataInstance().getVdbs();
        for( TeiidVdb vdb: vdbs) {
            if (vdb.getName().endsWith(CONNECTION_VDB_SUFFIX)) {
                if (!vdb.isActive()) {
                    continue;
                }
                VDBImportMetadata vdbImport = new VDBImportMetadata();
                vdbImport.setVersion(DefaultMetadataInstance.DEFAULT_VDB_VERSION);
                vdbImport.setName(vdb.getName());
                workingCopy.getVDBImports().add(vdbImport);
            } else if (vdb.getName().endsWith(StringConstants.SERVICE_VDB_SUFFIX)) {
                //no longer valid
                synchronized (masterLock) {
                    //TODO: could make this the more granular lock
                    getMetadataInstance().undeployDynamicVdb(vdb.getName());
                }
            }
        }
        synchronized (masterLock) {
            getMetadataInstance().deploy(workingCopy);
        }
        LOGGER.debug("preview vdb updated"); //$NON-NLS-1$
    }

    /**
     * Query the teiid server
     * @param kqa the query attribute (never <code>null</code>)
     * @return a JSON representation of the Query results (never <code>null</code>)
     * @throws Exception
     */
    @SuppressWarnings( "nls" )
    @RequestMapping(value = V1Constants.QUERY_SEGMENT, method = RequestMethod.POST,
        produces= { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Pass a query to the teiid server")
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 400, message = "An error has occurred.")
    })
    public QSResult query(@ApiParam( value = "" +
             "JSON of the properties of the query:<br>" +
             OPEN_PRE_TAG +
             OPEN_BRACE + BR +
             NBSP + "query: \"SQL formatted query to interrogate the target\"" + COMMA + BR +
             NBSP + "target: \"The name of the target data virtualization to be queried\"" + BR +
             NBSP + OPEN_PRE_CMT + "(The target can be a vdb or data service. If the latter " +
             NBSP + "then the name of the service vdb is extracted and " +
             NBSP + "replaces the data service)" + CLOSE_PRE_CMT + COMMA + BR +
             NBSP + "limit: Add a limit on number of results to be returned" + COMMA + BR +
             NBSP + "offset: The index of the result to begin the results with" + BR +
             CLOSE_BRACE +
             CLOSE_PRE_TAG,required = true)
           @RequestBody final QueryAttribute kqa) throws Exception {
        //
        // Error if there is no query attribute defined
        //
        if (kqa.getQuery() == null) {
            throw forbidden(Messages.Error.METADATA_SERVICE_QUERY_MISSING_QUERY);
        }

        if (kqa.getTarget() == null) {
            throw forbidden(Messages.Error.METADATA_SERVICE_QUERY_MISSING_TARGET);
        }

        String target = kqa.getTarget();
        String query = kqa.getQuery();

        TeiidVdb vdb = updatePreviewVdb(target);

        LOGGER.debug("Establishing query service for query %s on vdb %s", query, target);
        QSResult result = getMetadataInstance().query(vdb.getName(), query, kqa.getOffset(), kqa.getLimit());
        return result;
    }

    protected TeiidVdb updatePreviewVdb(String dvName) throws Exception {
        SyndesisConnectionMonitor.setUpdate(true);
        return repositoryManager.runInTransaction(true, ()->{
            DataVirtualization dv = repositoryManager.findDataVirtualization(dvName);
            if (dv == null) {
                throw notFound(dvName);
            }

            String serviceVdbName = DataVirtualization.getPreviewVdbName(dvName);
            TeiidVdb vdb = getMetadataInstance().getVdb(serviceVdbName);

            if (vdb != null
                    && dv.getVersion().compareTo(Long.valueOf(vdb.getPropertyValue(VERSION_PROPERTY))) == 0) {
                return vdb;
            }
            Lock lock = previewVdbLocks.get(dvName);
            lock.lock();
            try {
                vdb = getMetadataInstance().getVdb(serviceVdbName);
                if (vdb != null
                        && dv.getVersion().compareTo(Long.valueOf(vdb.getPropertyValue(VERSION_PROPERTY))) == 0) {
                    return vdb;
                }
                VDBMetaData theVdb = new ServiceVdbGenerator(this)
                        .createPreviewVdb(dvName, serviceVdbName, repositoryManager.findViewDefinitions(dvName));
                theVdb.addProperty(VERSION_PROPERTY, dv.getVersion().toString());

                synchronized (masterLock) {
                    metadataInstance.deploy(theVdb);
                    vdb = metadataInstance.getVdb(serviceVdbName);
                }
            } finally {
                lock.unlock();
            }
            return vdb;
        });
    }

    /**
     * Initiate schema refresh for a syndesis source.
     * @param teiidSourceName the syndesis source name (cannot be empty)
     * @throws Exception
     */
    @RequestMapping(value = StringConstants.FS + V1Constants.REFRESH_SCHEMA_SEGMENT
            + StringConstants.FS
            + V1Constants.TEIID_SOURCE_PLACEHOLDER, method = RequestMethod.POST,
            produces= { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Initiate schema refresh for a syndesis source")
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public StatusObject refreshSchema( @ApiParam( value = "Name of the teiid source", required = true )
                                   final @PathVariable(V1Constants.TEIID_SOURCE) String teiidSourceName) throws Exception {
        // Error if the syndesisSource is missing
        if (StringUtils.isBlank( teiidSourceName )) {
            throw forbidden(Messages.Error.CONNECTION_SERVICE_MISSING_CONNECTION_NAME);
        }
        deploySourceVdb(teiidSourceName, SourceDeploymentMode.REFRESH);
        return new StatusObject( "Refresh schema submitted" ); //$NON-NLS-1$
    }

    public void deploySourceVdb(String teiidSourceName,
            SourceDeploymentMode sourceDeploymentMode ) throws Exception {
        TeiidDataSource teiidSource = findTeiidDatasource(teiidSourceName);

        if (teiidSource == null) {
            throw notFound(teiidSourceName);
        }

        repositoryManager.runInTransaction(true, () -> {
            doDeploySourceVdb(teiidSource, sourceDeploymentMode);
            return null;
        });
    }

    public boolean deleteSchema(String sourceId, String teiidDataSourceName) throws Exception {
        //TODO: this can invalidate a lot of stuff
        boolean result = repositoryManager.runInTransaction(false, () -> {
            return repositoryManager.deleteSchemaBySourceId(sourceId);
        });

        if (result) {
            connectionExecutor.execute(()->{
                try {
                    removeVdb(getWorkspaceSourceVdbName(teiidDataSourceName));
                    refreshPreviewVdb();
                } catch (KException e) {
                    LOGGER.warn("Error removing the source vdb", e); //$NON-NLS-1$
                }
            });
        }

        return result;
    }

    @PostConstruct
    void init() {
        //create an initial dummy preview vdb
        try {
            refreshPreviewVdb();
        } catch (KException e1) {
            LOGGER.warn("Could not create initial preview vdb", e1); //$NON-NLS-1$
        }
        this.metadataInstance.addVDBLifeCycleListener(new VDBLifeCycleListener() {
            @Override
            public void finishedDeployment(String name, CompositeVDB vdb) {
                if (!name.endsWith(LOAD_SUFFIX)
                        //we remove inline, but will receive another event for this
                        //if we don't filter
                        || vdb.getVDB().getPropertyValue("pending-removal") != null) { //$NON-NLS-1$
                    return;
                }
                try {
                    String teiidSourceName = vdb.getVDB().getModels().get(0).getName();
                    String modelDdl;
                    Lock lock = previewVdbLocks.get(name);
                    lock.lock();
                    try {
                        if (vdb.getVDB().getStatus() == Status.ACTIVE) {
                            modelDdl = getMetadataInstance().getSchema(name, teiidSourceName);
                        } else {
                            //failed, effectively remove the source
                            List<String> errors = vdb.getVDB().getValidityErrors();
                            modelDdl = FAILED_DDL + (!errors.isEmpty()?errors.get(0):""); //$NON-NLS-1$
                        }
                        vdb.getVDB().addProperty("pending-removal", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                        getMetadataInstance().undeployDynamicVdb(name);
                    } finally {
                        lock.unlock();
                    }
                    boolean updateSource = false;
                    try {
                        updateSource = repositoryManager.runInTransaction(false, () -> {
                            SourceSchema schema = repositoryManager.findSchemaBySourceId(vdb.getVDB().getPropertyValue(TeiidOpenShiftClient.ID));
                            if (schema != null) {
                                String ddl = schema.getDdl();
                                if (!Objects.equals(ddl, modelDdl)) {
                                    schema.setDdl(modelDdl);
                                    return true;
                                }
                            }
                            return false;
                        });
                    } catch (ConcurrencyFailureException e) {
                        updateSource = true;
                        //this can only occur when running multiple pods
                        //since we've made a best effort to update the schema from the
                        //runtime state, we'll just ignore
                        //TODO: if we allow for multiple pods the notion of connection
                        //synchronization needs to be expanded to refresh reloading
                    }
                    if (updateSource) {
                        connectionExecutor.execute(()->{
                            try {
                                deploySourceVdb(teiidSourceName, SourceDeploymentMode.REPLACE_DDL);
                            } catch (Exception e) {
                                LOGGER.warn("Could not replace schema or update the preview vdb", e); //$NON-NLS-1$
                            }});
                    }
                } catch (Exception e) {
                    LOGGER.warn("Could not save schema or update the preview vdb", e); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * @return the JSON representation of the schema collection (never <code>null</code>)
     * @throws Exception
     */
    @RequestMapping(value = {"sourceSchema", "sourceSchema" + FS + TEIID_SOURCE_PLACEHOLDER}, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation( value = "Get the native schema for a teiid source.  In no teiidSource supplied, all schema are returned",
                   response = RestSchemaNode.class,
                   responseContainer = "List")
    @ApiResponses( value = {
        @ApiResponse( code = 403, message = "An error has occurred." ),
        @ApiResponse( code = 404, message = "No results found" ),
        @ApiResponse( code = 406, message = "Only JSON is returned by this operation" )
    } )
    public List<RestSchemaNode> getSourceSchema(
                                @ApiParam(value = "Name of the teiid source", required = false)
                                final @PathVariable(required=false, name=V1Constants.TEIID_SOURCE) String teiidSourceName) throws Exception {
        SyndesisConnectionMonitor.setUpdate(true);
        return repositoryManager.runInTransaction(true, () -> {

            List<RestSchemaNode> rootNodes = new ArrayList<RestSchemaNode>();
            Collection<TeiidDataSource> resultTeiidSources = new ArrayList();
            if (teiidSourceName != null) {
                // Find the bound teiid source corresponding to the syndesis source
                TeiidDataSource teiidSource = findTeiidDatasource(teiidSourceName);

                if (teiidSource == null) {
                    LOGGER.debug("teiid source '%s' was not found", teiidSourceName); //$NON-NLS-1$
                    throw notFound(teiidSourceName);
                }
                resultTeiidSources.add(teiidSource);
            } else {
                resultTeiidSources.addAll(getMetadataInstance().getDataSources());
            }

            for (TeiidDataSource teiidSource : resultTeiidSources) {
                final Schema schemaModel = findSchemaModel(teiidSource);

                if (schemaModel == null) {
                    continue;
                }

                List<RestSchemaNode> schemaNodes = this.generateSourceSchema(schemaModel.getName(),
                        schemaModel.getTables().values());
                if (schemaNodes != null && !schemaNodes.isEmpty()) {
                    RestSchemaNode rootNode = new RestSchemaNode();
                    rootNode.setName(schemaModel.getName());
                    rootNode.setType("teiidSource");
                    for (RestSchemaNode sNode : schemaNodes) {
                        rootNode.addChild(sNode);
                    }
                    rootNodes.add(rootNode);
                }
            }

            return rootNodes;
        });
    }

    /**
     * Get status for the available syndesis sources.
     * @return a JSON document representing the statuses of the sources (never <code>null</code>)
     * @throws Exception
     */
    @RequestMapping(value = V1Constants.SOURCE_STATUSES, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Return the source statuses",
        response = RestSyndesisSourceStatus.class,
        responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "An error has occurred.")
    })
    public List<RestSyndesisSourceStatus> getSyndesisSourceStatuses() throws Exception {

        final List< RestSyndesisSourceStatus > statuses = new ArrayList<>();

        return repositoryManager.runInTransaction(true, ()->{
            for (TeiidDataSource tds : getTeiidDatasources()) {
                RestSyndesisSourceStatus status = new RestSyndesisSourceStatus(
                        tds.getSyndesisDataSource().getSyndesisName());
                setSchemaStatus(tds.getSyndesisId(), status);
                status.setTeiidName(tds.getName());
                // Name of vdb based on source name
                String vdbName = getWorkspaceSourceVdbName(tds.getName());
                TeiidVdb teiidVdb = getMetadataInstance().getVdb(vdbName+LOAD_SUFFIX);
                if (teiidVdb != null) {
                    status.setLoading(teiidVdb.isLoading());
                }
                if (tds.getLastMetadataLoadTime() != null) {
                    status.setLastLoad(tds.getLastMetadataLoadTime());
                }
                statuses.add(status);
            }
            LOGGER.debug( "getSyndesisSourceStatuses '{0}' statuses", statuses.size() ); //$NON-NLS-1$
            return statuses;
        });
    }

    /**
     * Find and return all runtime metadata
     * @return source schema object array
     * @throws Exception
     */
    @RequestMapping(value = V1Constants.RUNTIME_METADATA + StringConstants.FS
            + V1Constants.VIRTUALIZATION_PLACEHOLDER, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Get Source Schema for a Virtualization", response = RestViewSourceInfo.class)
    @ApiResponses(value = { @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public RestViewSourceInfo getRuntimeMetadata(
            @ApiParam( value = "Name of the data virtualization", required = true )
            final @PathVariable( VIRTUALIZATION ) String virtualization) throws Exception {
        LOGGER.debug("getRuntimeMetadata()");

        if (virtualization == null) {
            throw forbidden(Messages.Error.DATASERVICE_SERVICE_MISSING_NAME);
        }

        List<RestSourceSchema> srcSchemas = new ArrayList<>();

        TeiidVdb vdb = updatePreviewVdb(virtualization);
        if (vdb == null || !vdb.hasLoaded()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        for (Schema s : vdb.getLocalSchema()) {
            srcSchemas.add(new RestSourceSchema(s));
        }

        return new RestViewSourceInfo(srcSchemas.toArray(new RestSourceSchema[srcSchemas.size()]));
    }

    public enum SourceDeploymentMode {
        REUSE_DDL,
        REPLACE_DDL, //a special case of reuse
        REFRESH,
        MAKE_LIVE
    }

    /**
     * Deploy / re-deploy a VDB to the metadata instance for the provided teiid data source.
     * @param teiidSource the teiidSource
     * @throws KException
     */
    private void doDeploySourceVdb( TeiidDataSource teiidSource, SourceDeploymentMode sourceDeploymentMode) throws KException {
        assert( teiidSource != null );

        boolean replace = false;
        if (sourceDeploymentMode == SourceDeploymentMode.REPLACE_DDL) {
            replace = true;
            sourceDeploymentMode = SourceDeploymentMode.REUSE_DDL;
        }

        // VDB is created in the repository.  If it already exists, delete it
        SourceSchema schema = repositoryManager.findSchemaBySourceId(teiidSource.getSyndesisId());
        if (schema == null) {
            //something is wrong, the logic that creates TeiidDataSources will always ensure
            //a sourceschema is created
            LOGGER.info("schema entry was not found for source vdb"); //$NON-NLS-1$
            return;
        }

        // Name of VDB to be created is based on the source name
        String vdbName = getWorkspaceSourceVdbName( teiidSource.getName() );

        if (sourceDeploymentMode == SourceDeploymentMode.REUSE_DDL
                && schema.getDdl() == null) {
            //if the ddl doesn't already exist, don't do a deployment/save
            //the calling operation should be fail-fast
            return;
        }

        String ddl = sourceDeploymentMode == SourceDeploymentMode.REFRESH ? null : schema.getDdl();

        if (ddl == null) {
            vdbName += LOAD_SUFFIX;
        }

        TeiidVdb existing = getMetadataInstance().getVdb(vdbName);
        if (existing != null && ((ddl == null && existing.isLoading()) || (existing.isActive() && !replace))) {
            return;
        }

        Lock lock = previewVdbLocks.get(vdbName);
        lock.lock();
        try {
            //under the lock, check again
            existing = getMetadataInstance().getVdb(vdbName);
            if (existing != null && ((ddl == null && existing.isLoading()) || (existing.isActive() && !replace))) {
                return;
            }

            if (ddl != null && ddl.startsWith(FAILED_DDL)) {
                getMetadataInstance().undeployDynamicVdb(vdbName);
            } else {
                try {
                    VDBMetaData vdb = generateSourceVdb(teiidSource, vdbName, ddl);
                    teiidSource.loadingMetadata();
                    getMetadataInstance().deploy(vdb);
                } catch (KException e) {
                    LOGGER.error("could not deploy source vdb", e); //$NON-NLS-1$
                }
            }
        } finally {
            lock.unlock();
        }

        if (ddl != null) {
            //this is the actual connection vdb, trigger a load of the preview vdbs
            connectionExecutor.execute(()->{
                try {
                    refreshPreviewVdb();
                } catch (Exception e) {
                    LOGGER.error("could not refresh preview vdb", e); //$NON-NLS-1$
                }
            });
        }
    }

    /**
     * If schema is null, an async-load vdb will be generated
     * @param teiidSource
     * @param vdbName
     * @param schema
     * @return
     */
    static VDBMetaData generateSourceVdb(TeiidDataSource teiidSource, String vdbName, String schema) {
        // Get necessary info from the source
        String sourceName = teiidSource.getName();
        String translatorName = teiidSource.getTranslatorName();

        VDBMetaData vdb = new VDBMetaData();
        vdb.setName(vdbName);
        vdb.setDescription("Vdb for source "+teiidSource); //$NON-NLS-1$
        ModelMetaData mmd = new ModelMetaData();
        mmd.setName(sourceName);
        vdb.addModel(mmd);
        vdb.addProperty(TeiidOpenShiftClient.ID, teiidSource.getSyndesisId());
        mmd.setModelType(Type.PHYSICAL);

        for (Map.Entry<String,String> entry : teiidSource.getImportProperties().entrySet()) {
            mmd.addProperty(entry.getKey(), entry.getValue());
        }

        if (schema != null) {
            //use this instead
            mmd.addSourceMetadata(DDLDBMetadataRepository.TYPE_NAME, teiidSource.getSyndesisId());
            mmd.setVisible(false);
        } else {
            vdb.addProperty("async-load", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Add model source to the model
        final String modelSourceName = teiidSource.getName();
        mmd.addSourceMapping(modelSourceName, translatorName, sourceName);
        return vdb;
    }

    /**
     * Find the schema VDB model in the workspace for the specified teiid source
     * @param dataSource the teiid datasource
     * @return the Model
     * @throws KException
     */
    private Schema findSchemaModel(final TeiidDataSource dataSource ) throws KException {
        final String dataSourceName = dataSource.getName( );

        //find from deployed state
        String vdbName = getWorkspaceSourceVdbName( dataSourceName );
        TeiidVdb vdb = getMetadataInstance().getVdb(vdbName);
        if (vdb == null) {
            doDeploySourceVdb(dataSource, SourceDeploymentMode.REUSE_DDL);
            vdb = getMetadataInstance().getVdb(vdbName);
        }

        if (vdb == null) {
            return null;
        }

        return vdb.getSchema(dataSourceName);
    }

    /**
     * Generate a workspace source vdb name, given the name of the source
     * @param sourceName the source name
     * @return the source vdb name
     */
    static String getWorkspaceSourceVdbName( final String sourceName ) {
        return sourceName + CONNECTION_VDB_SUFFIX;
    }

    /**
     * Generate the syndesis source schema structure using the supplied table fqn information.
     * @param sourceName the name of the source
     * @param tables the supplied array of tables
     * @return the list of schema nodes
     * @throws KException exception if problem occurs
     */
    private static List<RestSchemaNode> generateSourceSchema(final String sourceName, final Collection<org.teiid.metadata.Table> tables) throws KException {
        List<RestSchemaNode> schemaNodes = new ArrayList<RestSchemaNode>();

        for(final org.teiid.metadata.Table table : tables) {
            // Use the fqn table option do determine native structure
            String option = table.getProperty(TABLE_OPTION_FQN);
            if( option != null ) {
                // Break fqn into segments (segment starts at root, eg "schema=public/table=customer")
                List<Pair<String, String>> segments = PathUtils.getOptions(option);
                // Get the parent node of the final segment in the 'path'.  New nodes are created if needed.
                RestSchemaNode parentNode = getLeafNodeParent(sourceName, schemaNodes, segments);

                Pair<String, String> segment = segments.get(segments.size() - 1);
                String type = segment.getFirst();
                String name = segment.getSecond();

                // Use last segment to create the leaf node child in the parent.  If parent is null, was root (and leaf already created).
                if( parentNode != null ) {
                    RestSchemaNode node = new RestSchemaNode(sourceName, name, type);
                    node.setTeiidName(table.getName());
                    node.setQueryable(true);
                    parentNode.addChild(node);
                } else {
                    RestSchemaNode node = getMatchingNode(sourceName, name, type, schemaNodes);
                    node.setTeiidName(table.getName());
                    node.setQueryable(true);
                }
            }
        }

        return schemaNodes;
    }

    /**
     * Get the RestSchemaNode immediately above the last path segment (leaf parent).  If the parent nodes do not already exist,
     * they are created and added to the currentNodes.  The returned List is a list of the root nodes.  The root node children,
     * children's children, etc, are built out according to the path segments.
     * @param sourceName the name of the source
     * @param currentNodes the current node list
     * @param segments the full path of segments, starting at the root
     * @return the final segments parent node.  (null if final segment is at the root)
     */
    private static RestSchemaNode getLeafNodeParent(String sourceName, List<RestSchemaNode> currentNodes, List<Pair<String, String>> segments) {
        RestSchemaNode parentNode = null;
        // Determine number of levels to process.
        // - process one level if one segment
        // - if more than one level, process nSegment - 1 levels
        int nLevels = (segments.size() > 1) ? segments.size()-1 : 1;

        // Start at beginning of segment path, creating nodes if necessary
        for( int i=0; i < nLevels; i++ ) {
            Pair<String, String> segment = segments.get(i);
            String type = segment.getFirst();
            String name = segment.getSecond();
            // Root Level - look for matching root node in the list
            if( i == 0 ) {
                RestSchemaNode matchNode = getMatchingNode(sourceName, name, type, currentNodes);
                // No match - create a new node
                if(matchNode == null) {
                    matchNode = new RestSchemaNode(sourceName, name, type);
                    currentNodes.add(matchNode);
                }
                // Set parent for next iteration
                if( segments.size() == 1 ) {       // Only one segment - parent is null (root)
                    matchNode.setQueryable(true);
                    parentNode = null;
                } else {
                    // Set next parent if not last level
                    if( i != segments.size()-1 ) {
                        parentNode = matchNode;
                    }
                }
            // Not at root - look for matching node in parents children
            } else {
                RestSchemaNode matchNode = getMatchingNode(sourceName, name, type, parentNode.getChildren());
                // No match - create a new node
                if(matchNode == null) {
                    matchNode = new RestSchemaNode(sourceName, name, type);
                    parentNode.addChild(matchNode);
                }
                // Set next parent if not last level
                if( i != segments.size()-1 ) {
                    parentNode = matchNode;
                }
            }
        }
        return parentNode;
    }

    /**
     * Searches the supplied list for node with matching name and type.  Does NOT search children or parents of supplied nodes.
     * @param sourceName the source name
     * @param name the node name
     * @param type the node type
     * @param nodes the list of nodes to search
     * @return the matching node, if found
     */
    private static RestSchemaNode getMatchingNode(String sourceName, String name, String type, Collection<RestSchemaNode> nodes) {
        RestSchemaNode matchedNode = null;
        for(RestSchemaNode node : nodes) {
            if( node.getConnectionName().equals(sourceName) && node.getName().equals(name) && node.getType().equals(type) ) {
                matchedNode = node;
                break;
            }
        }
        return matchedNode;
    }

    /**
     * Set the schema availability for the provided RestSyndesisSourceStatus
     * @param status the RestSyndesisSourceStatus
     * @throws Exception if error occurs
     */
    private void setSchemaStatus(String schemaId, final RestSyndesisSourceStatus status ) throws Exception {
        // Get the workspace schema VDB
        SourceSchema schema = repositoryManager.findSchemaBySourceId(schemaId);
        status.setId(schemaId);

        if ( schema != null && schema.getDdl() != null) {
            if (schema.getDdl().startsWith(FAILED_DDL)) {
                status.setSchemaState( RestSyndesisSourceStatus.EntityState.FAILED );
                String error = schema.getDdl().substring(FAILED_DDL.length());
                if (error != null) {
                    status.setErrors(Arrays.asList(error));
                }
            } else {
                status.setSchemaState( RestSyndesisSourceStatus.EntityState.ACTIVE );
            }
            status.setLastLoad(schema.getModifiedAt());
        } else {
            status.setSchemaState( RestSyndesisSourceStatus.EntityState.MISSING );
        }
    }

    @Override
    public Schema findSchema(String connectionName) throws KException {
        TeiidDataSource tds = findTeiidDatasource(connectionName);
        if (tds == null) {
            return null;
        }
        return findSchemaModel(tds);
    }

    @Override
    public TeiidDataSource findTeiidDatasource(String connectionName) throws KException {
        return getMetadataInstance().getDataSource(connectionName);
    }

    public Collection<? extends TeiidDataSource> getTeiidDatasources() throws KException {
        return getMetadataInstance().getDataSources();
    }
}
