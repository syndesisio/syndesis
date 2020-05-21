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
package io.syndesis.dv.metadata;

import java.util.Collection;

import org.teiid.adminapi.AdminException;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.deployers.VDBLifeCycleListener;
import org.teiid.metadata.MetadataException;
import org.teiid.metadata.Schema;
import org.teiid.query.validator.ValidatorReport;

import io.syndesis.dv.KException;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.query.QSResult;

public interface MetadataInstance {

    /**
     * The host of the metadata instance
     */
    String HOST = "localhost"; //$NON-NLS-1$

    /**
     * The default admin user for the metadata instance
     */
    String DEFAULT_ADMIN_USER = "admin"; //$NON-NLS-1$

    /**
     * The default admin password for the metadata instance
     */
    String DEFAULT_ADMIN_PASSWORD = "admin"; //$NON-NLS-1$

    /**
     * The default admin port for the metadata instance
     */
    int DEFAULT_ADMIN_PORT = 9990; // $NON-NLS-1$

    /**
     * The default jdbc user for the metadata instance
     */
    String DEFAULT_JDBC_USER = "user"; //$NON-NLS-1$

    /**
     * The default jdbc password for the metadata instance
     */
    String DEFAULT_JDBC_PASSWORD = "user"; //$NON-NLS-1$

    /**
     * The default jdbc port for the metadata instance
     */
    int DEFAULT_JDBC_PORT = 31000; // $NON-NLS-1$

    /**
     * The default protocol for the metadata instance
     */
    String DEFAULT_INSTANCE_PROTOCOL = "mms"; //$NON-NLS-1$

    /**
     * Value representing no limit to results returned by a query
     */
    int NO_LIMIT = -1;

    /**
     * Value representing no offset to a set of results returned by a query
     */
    int NO_OFFSET = 0;

    /**
     * Type of connectivity
     */
    enum ConnectivityType {
        /**
         * Admin connection of the metadata instance
         */
        ADMIN,

        /**
         * JDBC connection of the metadata instance
         */
        JDBC;

        /**
         * @return the {@link ConnectivityType} for the given type
         */
        public static ConnectivityType findType(String type) {
            if (type == null) {
                return null;
            }

            for (ConnectivityType cType : ConnectivityType.values()) {
                if (type.equalsIgnoreCase(cType.name())) {
                    return cType;
                }
            }

            return null;
        }
    }

    class ValidationResult {
        private final ValidatorReport report;
        private final Schema schema;
        private final MetadataException metadataException;

        public ValidationResult(ValidatorReport report, Schema schema, MetadataException metadataException) {
            this.report = report;
            this.schema = schema;
            this.metadataException = metadataException;
        }

        public ValidatorReport getReport() {
            return report;
        }

        public Schema getSchema() {
            return schema;
        }

        public MetadataException getMetadataException() {
            return metadataException;
        }

    }

    /**
     * Query the vdb with given name
     *
     * @param vdbName the name of the vdb to query
     * @param query   the SQL query
     * @param offset  an offset of the results to return
     * @param limit   a limit on the number of results to return
     * @return the set of results
     */
    QSResult query(String vdbName, String query, int offset, int limit);

    /**
     * @return the collection of deployed vdbs
     */
    Collection<TeiidVdb> getVdbs();

    /**
     * @return the deployed vdb
     */
    TeiidVdb getVdb(String vdbDeploymentName);

    /**
     * @return the schema from the given model in the vdb with the given name
     */
    String getSchema(String vdbName, String modelName);

    /**
     * Undeploy the dynamic vdb with the given name
     */
    void undeployDynamicVdb(String name);

    /**
     * @return the collection of data sources
     */
    Collection<? extends TeiidDataSource> getDataSources();

    /**
     * @return the data source with the given name
     */
    TeiidDataSource getDataSource(String sourceName);

    /**
     * Removes the data source from the metadata instance (if exists)
     *
     * @param dsName the data source name
     * @throws KException if failure in deleting data source on metadata instance
     */
    void deleteDataSource(String dsName);

    void deploy(VDBMetaData vdb);

    Collection<String> getDataSourceNames() throws AdminException;

    void registerDataSource(DefaultSyndesisDataSource dataSource) throws AdminException;

    ValidationResult parse(String ddl);

    void addVDBLifeCycleListener(VDBLifeCycleListener listener);
}
