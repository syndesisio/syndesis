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

package io.syndesis.connector.kudu;

import io.syndesis.connector.kudu.common.KuduSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.kudu.client.KuduClient;

import java.util.Map;

public class KuduConnectorVerifierExtension extends DefaultComponentVerifierExtension {
    public KuduConnectorVerifierExtension(CamelContext camelContext) {
        super("kudu-connector", camelContext);
    }

    @Override
    public Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
                .error(ResultErrorHelper.requiresOption("host", parameters))
                .error(ResultErrorHelper.requiresOption("port", parameters));

        return builder.build();
    }

    @Override
    public Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
                .error(parameters, this::verifyConnection)
                .build();
    }

    private void verifyConnection(ResultBuilder builder, Map<String, Object> parameters) {
        try {
            KuduClient c = KuduSupport.createConnection(parameters);
            c.getTablesList();
        } catch (Exception e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(
                    VerificationError.StandardCode.EXCEPTION, e.getMessage());
            builder.error(errorBuilder.build());
        }
    }
}
