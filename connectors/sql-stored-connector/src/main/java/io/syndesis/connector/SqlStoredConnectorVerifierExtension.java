/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;

public class SqlStoredConnectorVerifierExtension extends DefaultComponentVerifierExtension {

    public SqlStoredConnectorVerifierExtension() {
        super("sql-stored-connector");
    }

    public SqlStoredConnectorVerifierExtension(String scheme) {
        super(scheme);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
            .error(ResultErrorHelper.requiresOption("url", parameters));

        
        if (builder.build().getErrors().isEmpty()) {
            try (Connection connection = 
                DriverManager.getConnection(
                        parameters.get("url").toString(), 
                        String.valueOf(parameters.get("user")), 
                        String.valueOf(parameters.get("password")))) {
            } catch (SQLException e) {
                String supportedDatabases = String.join(",", 
                        Arrays.stream(DatabaseProduct.values())
                        .map(Enum::name)
                        .toArray(String[]::new));
                String msg = "Supported Databases are [" + supportedDatabases + "]";
                builder.error(ResultErrorBuilder.withCodeAndDescription(
                        VerificationError.StandardCode.UNSUPPORTED, 
                            msg).build()).build();
            }
        }
        return builder.build();
       
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
            .error(parameters, this::verifyCredentials)
            .build();
    }

    private void verifyCredentials(ResultBuilder builder, Map<String, Object> parameters) {
        try ( Connection connection = DriverManager.getConnection(
                    parameters.get("url").toString(), 
                    String.valueOf(parameters.get("user")), 
                    String.valueOf(parameters.get("password")))) {
            if (connection == null) {
                throw new SQLException("No Connection");
            }
        } catch (Exception e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(
                    VerificationError.StandardCode.AUTHENTICATION, e.getMessage());
            builder.error(errorBuilder.build());
        }
    }
}
