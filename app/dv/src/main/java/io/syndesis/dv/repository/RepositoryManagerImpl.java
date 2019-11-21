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

package io.syndesis.dv.repository;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import io.syndesis.dv.RepositoryManager;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.Edition;
import io.syndesis.dv.model.SourceSchema;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.utils.KLog;

@Component
public class RepositoryManagerImpl implements RepositoryManager {

    private static DefaultTransactionDefinition NEW_TRANSACTION_DEFINITION = new DefaultTransactionDefinition();
    static {
        NEW_TRANSACTION_DEFINITION.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    protected static final KLog LOGGER = KLog.getLogger();

    @Autowired
    private DataVirtualizationRepository dataVirtualizationRepository;
    @Autowired
    private SourceSchemaRepository schemaRepository;
    @Autowired
    private ViewDefinitionRepository viewDefinitionRepository;
    @Autowired
    private EditionRepository editionRepository;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Override
    public <T> T runInTransaction(boolean rollbackOnly, Callable<T> callable) throws Exception {
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(NEW_TRANSACTION_DEFINITION);
        if (transactionStatus.isNewTransaction()) {
            if (rollbackOnly) {
                transactionStatus.setRollbackOnly();
            }
        } else {
            //there is a surrounding txn, so we can't set the rollback only flag
            rollbackOnly = false;
        }
        String txnName = null;
        if (LOGGER.isDebugEnabled()) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            txnName = stackTraceElements[2].getMethodName();
            LOGGER.debug( "createTransaction:created '%s', rollbackOnly = '%b'", txnName, rollbackOnly ); //$NON-NLS-1$
        }
        try {
            return callable.call();
        } finally {
            try {
                platformTransactionManager.commit(transactionStatus);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug( "transaction ended '%s'", //$NON-NLS-1$
                            txnName);
                }
            } catch (TransactionTimedOutException e) {
                throw new TimeoutException(e);
            }
        }
    }

    @Override
    public io.syndesis.dv.model.SourceSchema findSchemaBySourceId(String id) {
        return this.schemaRepository.findBySourceId(id);
    }

    @Override
    public boolean deleteSchemaBySourceId(String sourceid) {
        try {
            if (this.schemaRepository.deleteBySourceId(sourceid) == 0) {
                return false;
            }
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public SourceSchema createSchema(String sourceId, String name, String contents) {
        SourceSchema schema = new SourceSchema();
        schema.setSourceId(sourceId);
        schema.setName(name);
        schema.setDdl(contents);
        return this.schemaRepository.save(schema);
    }

    @Override
    public List<String> findAllSchemaNames() {
        return dataVirtualizationRepository.findNamesByTypeLike("s"); //$NON-NLS-1$
    }

    @Override
    public boolean isNameInUse(String name) {
        return dataVirtualizationRepository.countByUpperName(name.toUpperCase()) > 0;
    }

    @Override
    public DataVirtualization createDataVirtualization(String virtualizationName) {
        DataVirtualization dataservice = new DataVirtualization(virtualizationName);
        return this.dataVirtualizationRepository.save(dataservice);
    }

    @Override
    public DataVirtualization findDataVirtualization(String virtualizationName) {
        return this.dataVirtualizationRepository.findByName(virtualizationName);
    }

    @Override
    public DataVirtualization findDataVirtualizationBySourceId(String sourceId) {
        return this.dataVirtualizationRepository.findBySourceId(sourceId);
    }

    @Override
    public DataVirtualization findDataVirtualizationByNameIgnoreCase(
            String virtualizationName) {
        return this.dataVirtualizationRepository.findByNameIgnoreCase(virtualizationName);
    }

    @Override
    public Iterable<? extends DataVirtualization> findDataVirtualizations() {
        return this.dataVirtualizationRepository.findAll();
    }

    @Override
    public List<String> findDataVirtualizationNames() {
        return dataVirtualizationRepository.findNamesByTypeLike("v"); //$NON-NLS-1$
    }

    @Override
    public boolean deleteDataVirtualization(String serviceName) {
        io.syndesis.dv.model.DataVirtualization dv = this.dataVirtualizationRepository.findByName(serviceName);
        if (dv == null) {
            return false;
        }
        this.dataVirtualizationRepository.delete(dv);
        this.dataVirtualizationRepository.flush();
        return true;
    }

    @Override
    public List<ViewDefinition> saveAllViewDefinitions(Iterable<ViewDefinition> entities) {
        return this.viewDefinitionRepository.saveAll(entities);
    }

    @Override
    public io.syndesis.dv.model.ViewDefinition createViewDefiniton(String dvName, String viewName) {
        io.syndesis.dv.model.ViewDefinition viewEditorState = new io.syndesis.dv.model.ViewDefinition(dvName, viewName);
        return this.viewDefinitionRepository.save(viewEditorState);
    }

    @Override
    public List<String> findViewDefinitionsNames(String dvName) {
        return this.viewDefinitionRepository.findAllNamesByDataVirtualizationName(dvName);
    }

    @Override
    public List<io.syndesis.dv.model.ViewDefinition> findViewDefinitions(String dvName) {
        return this.viewDefinitionRepository.findAllByDataVirtualizationName(dvName);
    }

    @Override
    public boolean deleteViewDefinition(String id) {
        try {
            this.viewDefinitionRepository.deleteById(id);
            this.viewDefinitionRepository.flush();
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public io.syndesis.dv.model.ViewDefinition findViewDefinition(String id) {
        return this.viewDefinitionRepository.findById(id).orElse(null);
    }

    @Override
    public ViewDefinition findViewDefinitionByNameIgnoreCase(String dvName, String viewDefinitionName) {
        return this.viewDefinitionRepository.findByNameIgnoreCase(dvName, viewDefinitionName);
    }

    public void flush() {
        this.viewDefinitionRepository.flush();
    }

    @Override
    public Edition createEdition(String virtualization) {
        Edition edition = new Edition();
        Long val = this.editionRepository.findMaxRevision(virtualization);
        edition.setRevision(val == null?1:(val+1));
        edition.setDataVirtualizationName(virtualization);
        return this.editionRepository.save(edition);
    }

    @Override
    public Edition findEdition(String virtualization, long revision) {
        return this.editionRepository.findByDataVirtualizationNameAndRevision(virtualization, revision);
    }

    @Override
    public List<Edition> findEditions(String virtualization) {
        return this.editionRepository.findAllByDataVirtualizationName(virtualization);
    }

    @Override
    public void saveEditionExport(Edition edition, byte[] byteArray) {
        this.editionRepository.saveExport(edition.getId(), byteArray);
    }

    @Override
    public byte[] findEditionExport(Edition edition) {
        return this.editionRepository.findExport(edition.getId());
    }

    @Override
    public Long deleteViewDefinitions(String virtualization) {
        return this.viewDefinitionRepository.deleteByDataVirtualizationName(virtualization);
    }
}
