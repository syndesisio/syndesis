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
package io.syndesis.server.endpoint.v1.handler;

import javax.ws.rs.core.UriInfo;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithId;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.WithDataManager;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.util.ReflectiveFilterer;
import io.syndesis.server.endpoint.util.ReflectiveSorter;
import io.syndesis.server.endpoint.v1.operations.FilterOptionsFromQueryParams;
import io.syndesis.server.endpoint.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.server.endpoint.v1.operations.SortOptionsFromQueryParams;


public abstract class BaseHandler implements WithDataManager {

    private final DataManager dataMgr;

    protected BaseHandler(DataManager dataMgr) {
        this.dataMgr = dataMgr;
    }

    @Override
    public DataManager getDataManager() {
        return dataMgr;
    }

    protected <T extends WithId<T>> ListResult<T> fetchAll(Class<T> type, UriInfo uriInfo) {
        return getDataManager().fetchAll(
            type,
            new ReflectiveFilterer<>(type, new FilterOptionsFromQueryParams(uriInfo).getFilters()),
            new ReflectiveSorter<>(type, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        );
    }
}
