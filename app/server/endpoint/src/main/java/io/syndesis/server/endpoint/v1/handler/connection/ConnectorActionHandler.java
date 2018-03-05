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
package io.syndesis.server.endpoint.v1.handler.connection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.util.ReflectiveSorter;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;
import io.syndesis.server.endpoint.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.server.endpoint.v1.operations.SortOptionsFromQueryParams;
import io.syndesis.server.endpoint.v1.util.PredicateFilter;

@Api(value = "actions")
public class ConnectorActionHandler extends BaseHandler implements Lister<ConnectorAction>, Getter<ConnectorAction> {

    private final String connectorId;

    ConnectorActionHandler(DataManager dataMgr, String connectorId) {
        super(dataMgr);
        this.connectorId = connectorId;
    }

    @Override
    public Kind resourceKind() {
        return Kind.ConnectorAction;
    }

    @Override
    public ConnectorAction get(String id) {
        ConnectorAction result = Getter.super.get(id);
        if (result == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (result.getDescriptor().getConnectorId().equals(connectorId)){
            return result;
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Override
    public ListResult<ConnectorAction> list(UriInfo uriInfo) {
        return getDataManager().fetchAll(
            ConnectorAction.class,
            new PredicateFilter<>(o -> o.getDescriptor().getConnectorId().equals(connectorId)),
            new ReflectiveSorter<>(ConnectorAction.class, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        );
    }
}
