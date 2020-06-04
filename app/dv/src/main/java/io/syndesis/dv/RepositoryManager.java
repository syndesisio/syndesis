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

package io.syndesis.dv;

import java.util.List;
import java.util.concurrent.Callable;

import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.Edition;
import io.syndesis.dv.model.SourceSchema;
import io.syndesis.dv.model.TablePrivileges;
import io.syndesis.dv.model.ViewDefinition;

public interface RepositoryManager {

    class TimeoutException extends RuntimeException {

        private static final long serialVersionUID = -3492466153109760780L;

        public TimeoutException(Exception cause) {
            super(cause);
        }

    }

    @FunctionalInterface
    interface Task<T> extends Callable<T> {
        @Override
        T call();
    }

    /**
     * Run the callable in the given transaction
     */
    <T> T runInTransaction(boolean rollbackOnly, Task<T> callable);

    SourceSchema findSchemaBySourceId(String id);

    boolean deleteSchemaBySourceId(String id);

    SourceSchema createSchema(String id, String name, String contents);

    List<String> findAllSourceIds();


    DataVirtualization createDataVirtualization(String virtualizationName);

    DataVirtualization findDataVirtualization(String virtualizationName);

    DataVirtualization findDataVirtualizationByNameIgnoreCase(String virtualizationName);

    DataVirtualization findDataVirtualizationBySourceId(String sourceId);

    Iterable<? extends DataVirtualization> findDataVirtualizations();

    boolean deleteDataVirtualization(String virtualizationName);

    List<String> findDataVirtualizationNames();


    ViewDefinition findViewDefinitionByNameIgnoreCase(String dvName, String viewName);

    ViewDefinition findViewDefinition(String id);

    boolean deleteViewDefinition(String id);

    ViewDefinition createViewDefiniton(String dvName, String viewName);

    List<String> findViewDefinitionsNames(String dvName);

    List<? extends ViewDefinition> findViewDefinitions(String dvName);

    List<ViewDefinition> saveAllViewDefinitions(
            Iterable<ViewDefinition> entities);

    boolean isNameInUse(String name);

    Long deleteViewDefinitions(String virtualization);

    /**
     * Create a new published edition, with an automatically assigned revision number
     */
    Edition createEdition(String virtualization);

    List<Edition> findEditions(String virtualization);

    Edition findEdition(String virtualization, long revision);

    void saveEditionExport(Edition edition, byte[] byteArray);

    byte[] findEditionExport(Edition edition);

    long getEditionCount(String virtualization);

    /*
     * Role related
     */
    List<String> findRoleNames();

    boolean hasRoles(String name);

    List<TablePrivileges> findAllTablePrivileges(String virtualization);

    TablePrivileges createTablePrivileges(String viewId, String roleName);

    List<TablePrivileges> findTablePrivileges(String viewId);

    TablePrivileges findTablePrivileges(String viewId, String roleName);

    void deleteTablePrivileges(List<String> viewIds);

    void deleteTablePrivileges(TablePrivileges existing);
}
