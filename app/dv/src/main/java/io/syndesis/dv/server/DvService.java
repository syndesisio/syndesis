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
package io.syndesis.dv.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import io.syndesis.dv.RepositoryManager;
import io.syndesis.dv.utils.KLog;
/**
 * A DV service implementation.
 */
public abstract class DvService implements V1Constants {

    protected static final KLog LOGGER = KLog.getLogger();

    /**
     * Query parameter keys used by the service methods.
     */
    public interface QueryParamKeys {

        /**
         * The number of objects to return. If not present, all objects are returned.
         */
        String SIZE = "size"; //$NON-NLS-1$

        /**
         * The index of the first object to return. Defaults to zero.
         */
        String START = "start"; //$NON-NLS-1$

        String VIRTUALIZATION = "virtualization";
    }

    @Autowired
    protected RepositoryManager repositoryManager;

    protected RepositoryManager getWorkspaceManager() {
        return this.repositoryManager;
    }

    public static ResponseStatusException notFound(String resourceName) {
        String message = Messages.getString( Messages.Error.RESOURCE_NOT_FOUND,resourceName);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public static ResponseStatusException error(HttpStatus returnCode, Messages.Error errorType,
                                           Object... errorMsgInputs) {
        String message = Messages.getString(errorType, errorMsgInputs);
        throw new ResponseStatusException(returnCode, message);
    }

    public static ResponseStatusException forbidden(Messages.Error errorType,
                                           Object... errorMsgInputs) {
        return error(HttpStatus.FORBIDDEN, errorType, errorMsgInputs);
    }

}
