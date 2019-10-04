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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.MongoDBConnectorTestSupport;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MongoDBVerifierTest extends MongoDBConnectorTestSupport {

    private final static String CONNECTOR_ID = "io.syndesis.connector:connector-mongodb-find";
    private final static MongoDBVerifier VERIFIER = new MongoDBVerifier();

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", CONNECTOR_ID, DATABASE, COLLECTION);
    }

    @Test
    public void verifyConnectionOK() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", HOST + ":" + PORT);
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("database", ADMIN_DB);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        Assertions.assertThat(params.get("adminDB")).isEqualTo(params.get("database"));
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.OK));
    }

    @Test
    public void verifyConnectionFallbackAdmin() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", HOST + ":" + PORT);
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("database", DATABASE);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        Assertions.assertThat(params.get("adminDB")).isEqualTo(params.get("database"));
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.ERROR));
    }

    @Test
    public void verifyConnectionFullParamsOK() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", HOST + ":" + PORT);
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("database", DATABASE);
        params.put("adminDB", ADMIN_DB);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        Assertions.assertThat(params.get("adminDB")).isNotEqualTo(params.get("database"));
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.OK));
    }

    @Test
    public void verifyConnectionKO() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "notReachableHost");
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("database", DATABASE);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.ERROR));
    }

    @Test
    public void verifyConnectionMissingParams() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", HOST + ":" + PORT);
        params.put("user", USER);
        params.put("database", DATABASE);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.PARAMETERS).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.ERROR));
    }

    @Test
    public void verifyConnectionNotAuthenticated() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", HOST + ":" + PORT);
        params.put("user", USER);
        params.put("password", "wrongPassword");
        params.put("database", DATABASE);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.ERROR));
    }

    @Test
    public void verifyConnectionAdminDBKO() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", HOST + ":" + PORT);
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("adminDB", "someAdminDB");
        params.put("database", DATABASE);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.ERROR));
    }

    @Test
    public void verifyConnectionPortKO() {
        //When
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost:12343");
        params.put("user", USER);
        params.put("password", PASSWORD);
        params.put("database", DATABASE);
        //Given
        List<VerifierResponse> response = VERIFIER.verify(this.context,
            CONNECTOR_ID, params);
        //Then
        response.stream().filter(verifierResponse -> verifierResponse.getScope() == Verifier.Scope.CONNECTIVITY).
            forEach(verifierResponse -> Assertions.assertThat(verifierResponse.getStatus()).isEqualTo(Verifier.Status.ERROR));
    }
}
