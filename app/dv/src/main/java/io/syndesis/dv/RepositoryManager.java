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

package io.syndesis.dv;

import java.util.List;
import java.util.concurrent.Callable;

import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.Edition;
import io.syndesis.dv.model.SourceSchema;
import io.syndesis.dv.model.ViewDefinition;

public interface RepositoryManager {

    public static class EntityNotFoundException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = -3995911719208421687L;

    }

    public class TimeoutException extends Exception {

        private static final long serialVersionUID = -3492466153109760780L;

        public TimeoutException(Exception cause) {
            super(cause);
        }

    }

    /**
     * Run the callable in the given transaction
     * @param rollbackOnly
     * @param callable
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T runInTransaction(boolean rollbackOnly, Callable<T> callable) throws Exception;

    SourceSchema findSchemaBySourceId(String id);

    boolean deleteSchemaBySourceId(String id);

    SourceSchema createSchema(String id, String name, String contents);

    List<String> findAllSchemaNames();


    DataVirtualization createDataVirtualization(String virtualizationName);

    DataVirtualization findDataVirtualization(String virtualizationName);

    DataVirtualization findDataVirtualizationByNameIgnoreCase(String virtualizationName);

    DataVirtualization findDataVirtualizationBySourceId(String sourceId);

    public Iterable<? extends DataVirtualization> findDataVirtualizations();

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
     * @param virtualization
     * @return
     */
    Edition createEdition(String virtualization);

    List<Edition> findEditions(String virtualization);

    Edition findEdition(String virtualization, long revision);

    void saveEditionExport(Edition edition, byte[] byteArray);

    byte[] findEditionExport(Edition edition);
}
