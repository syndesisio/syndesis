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

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.MongoDBConnectorTestSupport;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoDBVerifierTest extends MongoDBConnectorTestSupport {

    private final static String CONNECTOR_ID = "io.syndesis.connector:connector-mongodb-producer";
    private final static String SCHEME = "mongodb3";
    private final static MongoDBVerifier VERIFIER = new MongoDBVerifier();

    @Override
    protected List<Step> createSteps() {
        // No need to define any route
        return Collections.emptyList();
    }

    @Test
    public void verifyConnectionOK() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("user", USER);
        params.put("password", PASSWORD);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.OK, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.OK, result.getStatus());
    }

    @Test
    public void verifyConnectionKO() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "notReachableHost");
        params.put("user", USER);
        params.put("password", PASSWORD);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.OK, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.ERROR, result.getStatus());
    }

    @Test
    public void verifyConnectionMissingParams() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("user", USER);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.ERROR, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.ERROR, result.getStatus());
    }

    @Test
    public void verifyConnectionNotAuthenticated() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("user", USER);
        params.put("password", "wrongPassword");
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.OK, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.ERROR, result.getStatus());
        log.info(result.getErrors().get(0).getDescription());
    }

    @Test
    public void verifyConnectionAdminDBKO() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("adminDB", "someAdminDB");
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.OK, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.ERROR, result.getStatus());
        log.info(result.getErrors().get(0).getDescription());
    }

    @Test
    public void verifyConnectionPortKO() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost:12343");
        params.put("user", USER);
        params.put("password", PASSWORD);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.OK, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.ERROR, result.getStatus());
        log.info(result.getErrors().get(0).getDescription());
    }

    @Test
    public void verifyConnectionDefaultDatabase() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("database", "admin");
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        ComponentVerifierExtension.Result result = VERIFIER
            .resolveComponentVerifierExtension(this.context, SCHEME)
            .verify(ComponentVerifierExtension.Scope.CONNECTIVITY, params);
        //Then
        assertEquals(Verifier.Status.OK, response.get(0).getStatus());
        assertEquals(ComponentVerifierExtension.Result.Status.OK, result.getStatus());
    }
}
