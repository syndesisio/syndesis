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
package io.syndesis.connector.mongo.verifier;

import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import io.syndesis.connector.mongo.MongoConfiguration;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.util.CastUtils.cast;

public class MongoConnectorVerifierExtension extends DefaultComponentVerifierExtension {
    private static final Logger LOG = LoggerFactory.getLogger(MongoConnectorVerifierExtension.class);

    private static final int CONNECTION_TIMEOUT = 2000;

    public MongoConnectorVerifierExtension(CamelContext camelContext) {
        super("mongodb3", camelContext);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    public Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
            .error(ResultErrorHelper.requiresOption("host", parameters))
            .error(ResultErrorHelper.requiresOption("user", parameters))
            .error(ResultErrorHelper.requiresOption("password", parameters));
        return builder.build();
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
        MongoConfiguration mongoConf = new MongoConfiguration(cast(parameters));
        String adminDB = ConnectorOptions.extractOption(parameters, "adminDB");
        String defaultDB = ConnectorOptions.extractOption(parameters, "database");
        if (adminDB == null && defaultDB != null) {
            mongoConf.setAdminDB(defaultDB);
        }
        MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
        // Avoid retry and long timeout
        optionsBuilder.connectTimeout(CONNECTION_TIMEOUT);
        optionsBuilder.serverSelectionTimeout(CONNECTION_TIMEOUT);
        optionsBuilder.maxWaitTime(CONNECTION_TIMEOUT);
        MongoClientURI connectionURI = new MongoClientURI(mongoConf.getMongoClientURI(), optionsBuilder);

        LOG.info("Testing connection against {}", connectionURI);
        try (MongoClient mongoClient = new MongoClient(connectionURI)) {
            // Just ping the server
            mongoClient.getConnectPoint();
        } catch (MongoSecurityException e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(
                VerificationError.StandardCode.AUTHENTICATION,
                String.format("Unable to authenticate %s against %s authentication database!", mongoConf.getUser(), mongoConf.getAdminDB()))
                .parameterKey("");
            builder.error(errorBuilder.build());
        } catch (MongoSocketException e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(
                VerificationError.StandardCode.GENERIC,
                String.format("Unable to connect to %s!", mongoConf.getHost()));
            builder.error(errorBuilder.build());
        } catch (Exception e) {
            // TODO We will always hit a MongoTimerException, did not find a way to catch the above yet...
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(
                VerificationError.StandardCode.GENERIC,
                e.getMessage());
            builder.error(errorBuilder.build());
        }
    }
}
