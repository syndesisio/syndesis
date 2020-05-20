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

import java.util.List;

import io.syndesis.dv.metadata.MetadataInstance.ValidationResult;
import org.teiid.adminapi.VDBImport;
import org.teiid.metadata.Schema;

/**
 *
 */
public interface TeiidVdb {

    /**
     * Extension of a vdb file
     */
    String VDB_EXTENSION = "vdb"; //$NON-NLS-1$

    /**
     * Extension of a vdb file with dot appended
     */
    String VDB_DOT_EXTENSION = ".vdb"; //$NON-NLS-1$

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the version
     */
    String getVersion();

    /**
     * @return <code>true</code> if this VDB is active
     */
    boolean isActive();

    /**
     * @return <code>true</code> if this VDB has loaded
     */
    boolean hasLoaded();

    /**
     * @return <code>true</code> if this VDB is loading
     */
    boolean isLoading();

    /**
     * @return <code>true</code> if this VDB failed
     */
    boolean hasFailed();

    /**
     * @return any validity errors
     */
    List<String> getValidityErrors();

    /**
     * @return value of property or null
     */
    String getPropertyValue(String key);

    List<? extends VDBImport> getImports();

    /**
     * Return the live metadata {@link Schema} instance
     * WARNING: do not modify
     */
    Schema getSchema(String name);

    List<Schema> getLocalSchema();

    ValidationResult validate(String ddl);

    boolean hasValidationError(String schemaName, String objectName,
            String childType);

}
