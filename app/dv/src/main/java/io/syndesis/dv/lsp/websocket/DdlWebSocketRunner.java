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
package io.syndesis.dv.lsp.websocket;

import java.io.Closeable;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DdlWebSocketRunner implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DdlWebSocketRunner.class);

    private Server server;

    public DdlWebSocketRunner() throws DeploymentException {
        this(null, null, null);
    }

    public DdlWebSocketRunner(String hostname, Integer port, String contextPath) throws DeploymentException {
        server = new Server(hostname, port == null ? 0:port, contextPath, null, TeiidDdlWebSocketServerConfigProvider.class);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "teiid-ddl-lsp-websocket-server-shutdown-hook"));

        server.start();
        LOGGER.info("DDL Language Server server started on port " + server.getPort());
    }

    @Override
    public void close() {
        server.stop();
    }

}
