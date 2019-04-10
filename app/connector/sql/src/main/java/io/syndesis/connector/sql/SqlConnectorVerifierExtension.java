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
package io.syndesis.connector.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.syndesis.connector.sql.common.DbEnum;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlConnectorVerifierExtension extends DefaultComponentVerifierExtension {
    private static final Logger LOG = LoggerFactory.getLogger(SqlConnectorVerifierExtension.class);

    public SqlConnectorVerifierExtension(CamelContext camelContext) {
        super("sql-connector", camelContext);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    public Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
            .error(ResultErrorHelper.requiresOption("url", parameters))
            .error(ResultErrorHelper.requiresOption("user", parameters))
            .error(ResultErrorHelper.requiresOption("password", parameters));

        if (builder.build().getErrors().isEmpty()) {
            try (Connection connection = SqlSupport.createConnection(parameters)) {
                // just try to get the connection
                if (!connection.isValid(0)) {
                    builder.error(ResultErrorBuilder.withCodeAndDescription(
                        VerificationError.StandardCode.GENERIC, "Unable to connect to database")
                        .build());
                }
            } catch (SQLException e) {
                final Map<String, Object> redacted = new HashMap<>(parameters);
                redacted.replace("password", "********");
                LOG.warn("Unable to connect to database with parameters {}, SQLSTATE: {}, error code: {}",
                    redacted, e.getSQLState(), e.getErrorCode(), e);

                final String sqlState = e.getSQLState();
                if (sqlState == null || sqlState.length() < 2) {
                    unsupportedDatabase(builder);
                } else {
                    switch (sqlState.substring(0, 2)) {
                    case "28":
                        builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                            .parameterKey("user")
                            .parameterKey("password")
                            .build());
                        break;
                    case "08":
                    case "3D":
                        builder.error(ResultErrorBuilder.withCodeAndDescription(
                            VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, e.getMessage())
                            .parameterKey("url")
                            .build());
                        break;
                    default:
                        builder.error(ResultErrorBuilder.withCodeAndDescription(
                            VerificationError.StandardCode.GENERIC, e.getMessage())
                            .build());
                        break;
                    }
                }
            }
        }

        return builder.build();
    }

    private static void unsupportedDatabase(ResultBuilder builder) {
        String supportedDatabases = Arrays.stream(DbEnum.values()).map(DbEnum::name).collect(Collectors.joining(","));
        String msg = "Supported Databases are [" + supportedDatabases + "]";

        builder.error(
            ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.UNSUPPORTED, msg).build()
        );
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    public Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
            .error(parameters, this::verifyCredentials)
            .build();
    }

    private void verifyCredentials(ResultBuilder builder, Map<String, Object> parameters) {
        try ( Connection connection = SqlSupport.createConnection(parameters)) {
            if (connection == null) {
                throw new SQLException("No Connection");
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(
                    VerificationError.StandardCode.AUTHENTICATION, e.getMessage());
            builder.error(errorBuilder.build());
        }
    }
}
