/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dv.metadata.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.deployers.VDBLifeCycleListener;
import org.teiid.deployers.VirtualDatabaseException;
import org.teiid.dqp.internal.datamgr.ConnectorManager;
import org.teiid.dqp.internal.datamgr.ConnectorManagerRepository;
import org.teiid.dqp.internal.datamgr.ConnectorManagerRepository.ConnectorManagerException;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.TranslatorException;

public class TeiidServer extends EmbeddedServer {
    private Map<String, TeiidDataSourceImpl> datasources = new ConcurrentHashMap<>();

    public TeiidServer() {
        this.cmr = new SBConnectorManagerRepository();
    }

    public Map<String, TeiidDataSourceImpl> getDatasources() {
        return datasources;
    }

    @SuppressWarnings("serial")
    protected class SBConnectorManagerRepository extends ConnectorManagerRepository {
        public SBConnectorManagerRepository() {
            super(true);
        }

        @Override
        protected ConnectorManager createConnectorManager(String translatorName, String connectionName,
                ExecutionFactory<Object, Object> ef) throws ConnectorManagerException {
            return new ConnectorManager(translatorName, connectionName, ef) {
                @Override
                public Object getConnectionFactory() throws TranslatorException {
                    if (getConnectionName() == null) {
                        return null;
                    }
                    ConnectionFactoryProvider<?> connectionFactoryProvider = datasources
                            .get(getConnectionName());
                    if (connectionFactoryProvider != null) {
                        return connectionFactoryProvider.getConnectionFactory();
                    }
                    return super.getConnectionFactory();
                }
            };
        }
    }

    public void addVDBLifeCycleListener(VDBLifeCycleListener listener) {
        super.getVDBRepository().addListener(listener);
    }

    public void deployVDB(VDBMetaData vdb)
            throws ConnectorManagerException, VirtualDatabaseException,
            TranslatorException {
        vdb.setXmlDeployment(true);
        super.deployVDB(vdb, null);
    }
}
