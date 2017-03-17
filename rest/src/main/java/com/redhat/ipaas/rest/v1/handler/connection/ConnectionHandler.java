/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.rest.v1.handler.connection;

import javax.ws.rs.Path;

import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.connection.Connector;
import com.redhat.ipaas.rest.v1.handler.BaseHandler;
import com.redhat.ipaas.rest.v1.operations.*;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Component;

@Path("/connections")
@Api(value = "connections")
@Component
public class ConnectionHandler extends BaseHandler implements Lister<Connection>, Getter<Connection>, Creator<Connection>, Deleter<Connection>, Updater<Connection> {

    public ConnectionHandler(DataManager dataMgr) {
        super(dataMgr);
    }

    @Override
    public Class<Connection> resourceClass() {
        return Connection.class;
    }

    @Override
    public String resourceKind() {
        return Connection.KIND;
    }
    
    @Override
    public Connection get(String id) {
        Connection connection = Getter.super.get(id);
        if (connection.getConnector().isPresent()) {
            Connector connector = getDataManager().fetch(Connector.KIND, connection.getConnector().get().getId().get());
            connection = new Connection.Builder().createFrom(connection).connector(connector).build();
        }
        return connection;
    }

}
