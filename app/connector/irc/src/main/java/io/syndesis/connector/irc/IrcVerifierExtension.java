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
package io.syndesis.connector.irc;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import io.syndesis.connector.support.util.ConnectorOptions;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

public class IrcVerifierExtension extends DefaultComponentVerifierExtension {
    protected IrcVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    @Override
    public Result verify(Scope scope, Map<String, Object> parameters) {
        final String hostname = ConnectorOptions.extractOption(parameters, "hostname");
        final int port = ConnectorOptions.extractOptionAndMap(parameters, "port", Integer::parseInt, 7000);
        final Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(hostname,port), 5000);
            s.setSoTimeout(5000);
            s.close();
            return ResultBuilder.withStatusAndScope(Result.Status.OK, scope).build();
        } catch (UnknownHostException e) {
            return ResultBuilder.withStatusAndScope(Result.Status.ERROR, scope)
                    .error(ResultErrorBuilder.withIllegalOption("Hostname", hostname).build()).build();
        } catch (IOException ex) {
                return ResultBuilder.withStatusAndScope(Result.Status.ERROR, scope).error(new ResultErrorBuilder().code(VerificationError.StandardCode.GENERIC).description("Unable to connect to specified IRC server").build()).build();
        }
    }
}
