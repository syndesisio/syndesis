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
package io.syndesis.dv.lsp;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.websocket.TeiidDdlWebSocketEndpoint;

public class TeiidDdlLanguageServerRunner {

    static class TeiidDdlWebSocketServerConfigProvider implements ServerApplicationConfig {

        private static final String WEBSOCKET_TEIID_DDL_SERVER_PATH = "/teiid-ddl-language-server";

        @Override
        public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
            ServerEndpointConfig conf = ServerEndpointConfig.Builder.create(TeiidDdlWebSocketEndpoint.class, WEBSOCKET_TEIID_DDL_SERVER_PATH).build();
            return Collections.singleton(conf);
        }

        @Override
        public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
            return scanned;
        }

    }

    static class TeiidDdlWebSocketRunner implements Closeable {

        private Server server;

        public TeiidDdlWebSocketRunner() throws DeploymentException {
            this(null, null, null);
        }

        public TeiidDdlWebSocketRunner(String hostname, Integer port, String contextPath) throws DeploymentException {
            server = new Server(hostname, port == null ? 0:port, contextPath, null, TeiidDdlWebSocketServerConfigProvider.class);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "teiid-ddl-lsp-websocket-server-shutdown-hook"));

            server.start();
            LOGGER.info("DDL LSP Websocket server started on port " + server.getPort());
        }

        @Override
        public void close() {
            server.stop();
        }

    }

    /**
     * For test only
     */
    static TeiidDdlLanguageServer server;

    private static final String WEBSOCKET_PARAMETER = "--websocket";
    private static final String PORT_PARAMETER = "--port=";
    private static final String HOSTNAME_PARAMETER = "--hostname=";
    private static final String CONTEXTPATH_PARAMETER = "--contextPath=";

    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlTextDocumentService.class);

    public static void main(String[] args) throws DeploymentException, InterruptedException {
        LOGGER.info("   --  >>>  TeiidDdlLanguageServerRunner.main()");
        List<String> arguments = Arrays.asList(args);
        if (arguments.contains(WEBSOCKET_PARAMETER)) {
            LOGGER.info("   --  >>>  Started Teiid LS as WEB SOCKET");
            int port = extractPort(arguments);
            String hostname = extractHostname(arguments);
            String contextPath = extractContextPath(arguments);
            try (TeiidDdlWebSocketRunner runner = new TeiidDdlWebSocketRunner(hostname, port, contextPath);) {
                Thread.currentThread().join();
            }
        } else {
            LOGGER.info("   --  >>>  Started Teiid LS as JAVA SERVER");
            server = new TeiidDdlLanguageServer();

            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);

            server.connect(launcher.getRemoteProxy());

            launcher.startListening();
            LOGGER.info("   --  >>>  Teiid LS Started. launch listening started");
        }
    }

    private static String extractContextPath(List<String> arguments) {
        return extractParameterValue(arguments, CONTEXTPATH_PARAMETER);
    }

    private static String extractHostname(List<String> arguments) {
        return extractParameterValue(arguments, HOSTNAME_PARAMETER);
    }

    private static String extractParameterValue(List<String> arguments, String parameterToExtract) {
        for (String argument : arguments) {
            if (argument.startsWith(parameterToExtract)) {
                return argument.substring(parameterToExtract.length());
            }
        }
        return null;
    }

    private static int extractPort(List<String> arguments) {
        for (String argument : arguments) {
            if (argument.startsWith(PORT_PARAMETER)) {
                String providedPort = argument.substring(PORT_PARAMETER.length());
                try {
                    return Integer.parseInt(providedPort);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("The provided port is invalid.", nfe);
                }
            }
        }
        return 0;
    }
}