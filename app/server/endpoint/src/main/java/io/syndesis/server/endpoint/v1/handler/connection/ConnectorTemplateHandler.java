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

import javax.ws.rs.Path;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;
import org.springframework.stereotype.Component;

@Path("/connector-templates")
@Tag(name = "connector-template")
@Component
public final class ConnectorTemplateHandler extends BaseHandler
    implements Lister<ConnectorTemplate>, Getter<ConnectorTemplate> {

    protected ConnectorTemplateHandler(final DataManager dataMgr) {
        super(dataMgr);
    }

    @Override
    public ConnectorTemplate get(final String id) {
        return Getter.super.get(id);
    }

    @Override
    public ListResult<ConnectorTemplate> list(int page, int perPage) {
        return Lister.super.list(page, perPage);
    }

    @Override
    public Kind resourceKind() {
        return Kind.ConnectorTemplate;
    }
}
