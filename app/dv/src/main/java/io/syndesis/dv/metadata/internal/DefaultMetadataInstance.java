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
package io.syndesis.dv.metadata.internal;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.adminapi.Admin;
import org.teiid.adminapi.AdminException;
import org.teiid.adminapi.Model.MetadataStatus;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.VDB.Status;
import org.teiid.adminapi.VDBImport;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.ModelMetaData.Message;
import org.teiid.adminapi.impl.ModelMetaData.Message.Severity;
import org.teiid.adminapi.impl.SourceMappingMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.api.exception.query.FunctionExecutionException;
import org.teiid.api.exception.query.QueryMetadataException;
import org.teiid.core.TeiidComponentException;
import org.teiid.core.types.ArrayImpl;
import org.teiid.core.types.TransformationException;
import org.teiid.core.types.basic.ClobToStringTransform;
import org.teiid.core.util.AccessibleByteArrayOutputStream;
import org.teiid.core.util.ArgCheck;
import org.teiid.deployers.VDBLifeCycleListener;
import org.teiid.deployers.VirtualDatabaseException;
import org.teiid.dqp.internal.datamgr.ConnectorManagerRepository.ConnectorManagerException;
import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.MetadataException;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.Schema;
import org.teiid.query.function.GeometryUtils;
import org.teiid.query.metadata.BasicQueryMetadataWrapper;
import org.teiid.query.metadata.CompositeMetadataStore;
import org.teiid.query.metadata.MetadataValidator;
import org.teiid.query.metadata.SystemMetadata;
import org.teiid.query.metadata.TransformationMetadata;
import org.teiid.query.parser.QueryParser;
import org.teiid.query.validator.ValidatorReport;
import org.teiid.translator.TranslatorException;
import org.teiid.util.FullyQualifiedName;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

import io.syndesis.dv.KException;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.datasources.ExternalSource;
import io.syndesis.dv.metadata.MetadataInstance;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.query.QSColumn;
import io.syndesis.dv.metadata.query.QSResult;
import io.syndesis.dv.metadata.query.QSRow;
import io.syndesis.dv.utils.KLog;

@Component
public class DefaultMetadataInstance implements MetadataInstance {

    public class TeiidVdbImpl implements TeiidVdb {

        private VDBMetaData vdb;

        public TeiidVdbImpl(VDB vdb) {
            ArgCheck.isInstanceOf(VDBMetaData.class, vdb);

            this.vdb = (VDBMetaData)vdb;
        }

        @Override
        public String getName() {
            return vdb.getName();
        }

        @Override
        public String getVersion() {
            return vdb.getVersion();
        }

        @Override
        public boolean isActive() {
            return Status.ACTIVE.equals(vdb.getStatus());
        }

        @Override
        public boolean hasLoaded() {
            //the notion of loaded needs to be
            //consistent whether the vdb has been
            //removed or not, so we can't just check the vdb status
            if (vdb.getStatus() == Status.LOADING
                    || vdb.getStatus() == Status.FAILED) {
                return false;
            }
            for (org.teiid.adminapi.Model m : vdb.getModels()) {
                if (m.getMetadataStatus() != MetadataStatus.LOADED) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isLoading() {
            return Status.LOADING.equals(vdb.getStatus());
        }

        @Override
        public boolean hasFailed() {
            return Status.FAILED.equals(vdb.getStatus());
        }

        @Override
        public List<String> getValidityErrors() {
            return vdb.getValidityErrors();
        }

        @Override
        public String getPropertyValue(String key) {
            return vdb.getPropertyValue(key);
        }

        @Override
        public List<? extends VDBImport> getImports() {
            return this.vdb.getVDBImports();
        }

        public VDBMetaData getVDBMetaData() {
            return this.vdb;
        }

        @Override
        public Schema getSchema(String name) {
            if (!hasLoaded()) {
                return null;
            }
            TransformationMetadata qmi = vdb.getAttachment(TransformationMetadata.class);
            return qmi.getMetadataStore().getSchema(name);
        }

        @Override
        public ValidationResult validate(String ddl) throws KException {
            return DefaultMetadataInstance.this.validate(this, ddl, false);
        }

        @Override
        public List<Schema> getLocalSchema() {
            if (!hasLoaded()) {
                return Collections.emptyList();
            }
            TransformationMetadata qmi = vdb.getAttachment(TransformationMetadata.class);

            return vdb.getModels().stream()
                    .map(m -> qmi.getMetadataStore().getSchema(m.getName()))
                    .collect(Collectors.toList());
        }

        private Set<String> haveErrors;

        @Override
        public boolean hasValidationError(String schemaName, String objectName, String childType) {
            ModelMetaData m = this.vdb.getModel(schemaName);
            if (m == null) {
                return false;
            }
            if (haveErrors == null) {
                haveErrors = new HashSet<>();
                for (Message message : m.getMessages()) {
                    if (message.getPath() != null && message.getSeverity() == Severity.ERROR) {
                        haveErrors.add(message.getPath());
                    }
                }
            }
            FullyQualifiedName fqn = new FullyQualifiedName(childType, objectName);
            String path = fqn.toString();
            return haveErrors.contains(path);
        }

    }

    public static final String DEFAULT_VDB_VERSION = "1"; //$NON-NLS-1$

    @Autowired
    private TeiidServer server;

    private Admin admin;

    public DefaultMetadataInstance() {

    }

    public DefaultMetadataInstance(TeiidServer server) {
        this.server = server;
    }

    public Admin getAdmin() {
        //no need to synchronize, as delegate holds no state
        if (admin == null) {
            admin = server.getAdmin();
        }
        return admin;
    }

    public Connection getConnection(String vdb, String version) {
        Properties props = new Properties();
        //TODO: when security working the user name needs to be passed in we need to work delegation model for security
        try {
            return server.getDriver().connect("jdbc:teiid:"+vdb+"."+version, props);
        } catch (SQLException e) {
            KLog.getLogger().warn("Could not get a connection to " + vdb, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }
    }

    /**
     * Wraps error in a {@link KException} if necessary.
     *
     * @param e
     *        the error being handled (cannot be <code>null</code>)
     * @return the error (never <code>null</code>)
     */
    protected static KException handleError(Throwable e) {
        assert (e != null);

        if (e instanceof KException) {
            return (KException)e;
        }

        return new KException(e);
    }

    @Override
    public QSResult query(String vdb, String query, int offset, int limit) throws KException {
        ObjectMapper mapper = new ObjectMapper();

        QSResult result = new QSResult();

        KLog.getLogger().debug("Commencing query execution: %s", query);

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        KLog.getLogger().debug("Initialising SQL connection for vdb %s", vdb);

        //
        // Ensure any runtime exceptions are always caught and thrown as KExceptions
        //
        try {
            connection = getConnection(vdb, DEFAULT_VDB_VERSION);

            statement = connection.createStatement();

            KLog.getLogger().debug("Executing SQL Statement for query %s with offset of %d and limit of %d",
                                   query,
                                   offset,
                                   limit);

            if (offset != NO_OFFSET || limit != NO_LIMIT) {
                //if we want more effective pagination, then
                //we need to enable result set caching and parameterize the limit/offset
                query = "SELECT * FROM (" + query + ") x LIMIT " + Math.max(0, offset) + ", " + (limit < 0?Integer.MAX_VALUE:limit);
            }

            try {
                rs = statement.executeQuery(query);
            } catch (SQLException e) {
                KLog.getLogger().warn("Could not execute query: " + query, e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            ResultSetMetaData rsmd = rs.getMetaData();
            int columns = rsmd.getColumnCount();

            //
            // Populate the columns
            //
            for (int i = 1; i <= columns; ++i) {
                String columnName = rsmd.getColumnName(i);
                String columnLabel = rsmd.getColumnLabel(i);
                String colTypeName = rsmd.getColumnTypeName(i);
                QSColumn column = new QSColumn(colTypeName, columnName, columnLabel);
                result.addColumn(column);
            }

            while (rs.next()) {
                QSRow row = new QSRow();
                for (int i = 1; i <= columns; ++i) {
                    Object value = rs.getObject(i);
                    if (value instanceof ArrayImpl) {
                        row.add(mapper.writeValueAsString(((ArrayImpl)value).getArray()));
                    } else if (value instanceof java.sql.Blob) {
                        row.add("blob");
                    }  else if (value instanceof java.sql.Clob) {
                        row.add("clob");
                    }  else if (value instanceof org.teiid.core.types.AbstractGeospatialType) {
                        Clob clob = GeometryUtils.geometryToClob((org.teiid.core.types.AbstractGeospatialType)value, true);
                        ClobToStringTransform transform = new ClobToStringTransform();
                        row.add(transform.transform(clob, String.class));
                    } else {
                       row.add(value);
                    }
                }
                result.addRow(row);
            }

            KLog.getLogger().debug("Query executed and returning %d results", result.getRows().size());

            return result;
        } catch (SQLException | JsonProcessingException | FunctionExecutionException | TransformationException e) {
            throw new KException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (statement != null) {
                    statement.close();
                }

                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e1) {
                // ignore
            }
        }
    }

    @Override
    public TeiidDataSourceImpl getDataSource(String name) throws KException {
        return this.server.getDatasources().get(name);
    }

    @Override
    public void deleteDataSource(String dsName) throws KException {
        try {
            TeiidDataSource ds = this.server.getDatasources().remove(dsName);
            if (ds != null) {
                // close the underlying datasource and any connections
                Object cf = ds.getConnectionFactory();
                if (cf instanceof HikariDataSource) {
                    ((HikariDataSource)cf).close();
                }
                if (cf instanceof Closeable) {
                    ((Closeable)cf).close();
                }
            }
        } catch (Exception ex) {
            throw handleError(ex);
        }
    }

    @Override
    public Collection<? extends TeiidDataSource> getDataSources() throws KException {
        return this.server.getDatasources().values();
    }

    @Override
    public Collection<TeiidVdb> getVdbs() throws KException {
        try {
            Collection<? extends VDB> vdbs = getAdmin().getVDBs();
            if (vdbs.isEmpty()) {
                return Collections.emptyList();
            }

            List<TeiidVdb> teiidVdbs = new ArrayList<>();
            for (VDB vdb : vdbs) {
                teiidVdbs.add(new TeiidVdbImpl(vdb));
            }

            return teiidVdbs;
        } catch (AdminException ex) {
            throw handleError(ex);
        }
    }

    @Override
    public TeiidVdbImpl getVdb(String name) throws KException {
        try {
            VDB vdb = getAdmin().getVDB(name, DEFAULT_VDB_VERSION);
            if (vdb == null) {
                return null;
            }

            return new TeiidVdbImpl(vdb);
        } catch (AdminException ex) {
            throw handleError(ex);
        }
    }

    @Override
    public void deploy(VDBMetaData vdb) throws KException {
        String vdbName = vdb.getName();

        try {
            // Deploy the VDB
            Admin admin = getAdmin();

            VDB existing = admin.getVDB(vdbName, vdb.getVersion());
            if (existing != null) {
                admin.undeploy(existing.getName());
            }

            for (ModelMetaData model : vdb.getModelMetaDatas().values()) {
                for (SourceMappingMetadata smm : model.getSourceMappings()) {
                    addTranslator(smm.getTranslatorName());
                    if (smm.getConnectionJndiName() == null) {
                        continue;
                    }
                    TeiidDataSourceImpl teiidDataSourceImpl = this.server.getDatasources().get(smm.getConnectionJndiName());
                    server.addConnectionFactory(smm.getName(), teiidDataSourceImpl.getConnectionFactory());
                }
            }

            server.deployVDB(vdb);
        } catch (AdminException | VirtualDatabaseException
                | ConnectorManagerException | TranslatorException ex) {
            throw handleError(ex);
        }
    }

    @Override
    public void undeployDynamicVdb(String vdbName) throws KException {
        try {
            TeiidVdb vdb = getVdb(vdbName);
            if (vdb != null) {
                getAdmin().undeploy(vdbName);
            }
        } catch (AdminException ex) {
            throw handleError(ex);
        }
    }

    @Override
    public String getSchema(String vdbName, String modelName) throws KException {
        try {
            return getAdmin().getSchema(vdbName, DEFAULT_VDB_VERSION, modelName, null, null);
        } catch (AdminException ex) {
            throw handleError(ex);
        }
    }

    public static AccessibleByteArrayOutputStream toBytes(VDBMetaData vdb) throws KException {
        AccessibleByteArrayOutputStream baos = new AccessibleByteArrayOutputStream();
        try {
            VDBMetadataParser.marshell(vdb, baos);
        } catch (XMLStreamException | IOException e) {
            throw new KException(e);
        }

        return baos;
    }

    @Override
    public void registerDataSource(DefaultSyndesisDataSource teiidDS) throws AdminException {
        this.server.getDatasources().computeIfAbsent(teiidDS.getTeiidName(),
                (s) -> {
                    return teiidDS.createDataSource();
                });
    }

    @Override
    public Collection<String> getDataSourceNames() throws AdminException {
        return this.server.getDatasources().keySet();
    }

    void addTranslator(String translatorname) {
        try {
            if (server.getExecutionFactory(translatorname) == null) {
                server.addTranslator(ExternalSource.translatorClass(translatorname, "io.syndesis.dv.rest"));
            }
        } catch (ConnectorManagerException | TranslatorException e) {
            throw new IllegalStateException("Failed to load translator " + translatorname, e);
        }
    }

    @Override
    public ValidationResult parse(String ddl) throws KException {
        return validate(null, ddl, true); //$NON-NON-NLS-1$
    }

    public ValidationResult validate(TeiidVdbImpl preview, String ddl, boolean parseOnly) throws KException {
        QueryParser parser = QueryParser.getQueryParser();

        ModelMetaData m = new ModelMetaData();
        m.setName("preview"); //$NON-NLS-1$ //TODO: could use the actual name where possible
        MetadataFactory mf = new MetadataFactory(preview == null?"vdb":preview.getName(), DefaultMetadataInstance.DEFAULT_VDB_VERSION, SystemMetadata.getInstance().getRuntimeTypeMap(),m);
        ValidatorReport report = new ValidatorReport();
        MetadataException metadataException = null;
        try {
            parser.parseDDL(mf, ddl);
        } catch (MetadataException e) {
            metadataException = e;
        }

        if (!parseOnly) {
            if (preview == null || !preview.hasLoaded()) {
                throw new KException("Preview VDB is not available");
            }
            VDBMetaData vdb = preview.getVDBMetaData();
            TransformationMetadata qmi = preview.getVDBMetaData().getAttachment(TransformationMetadata.class);

            //create an metadata facade so we can find stuff that was parsed
            CompositeMetadataStore compositeMetadataStore = new CompositeMetadataStore(mf.asMetadataStore());
            BasicQueryMetadataWrapper wrapper = new BasicQueryMetadataWrapper(qmi) {
                @Override
                public Object getGroupID(String groupName) throws TeiidComponentException, QueryMetadataException {
                    try {
                        return super.getGroupID(groupName);
                    } catch (QueryMetadataException e) {
                        return compositeMetadataStore.findGroup(groupName);
                    }
                }
            };

            MetadataValidator validator = new MetadataValidator();
            for (AbstractMetadataRecord record : mf.getSchema().getResolvingOrder()) {
                validator.validate(vdb, m, record, report, wrapper, mf, parser);
            }
        }

        return new ValidationResult(report, mf.getSchema(), metadataException);
    }

    @Override
    public void addVDBLifeCycleListener(VDBLifeCycleListener listener) {
        this.server.addVDBLifeCycleListener(listener);
    }

}
