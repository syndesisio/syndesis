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
package io.syndesis.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.syndesis.model.ListResult;
import io.syndesis.model.connection.Connector;
import io.syndesis.verifier.AlwaysOkVerifier;
import io.syndesis.verifier.Verifier;

public class ConnectorsITCase extends BaseITCase {

    @Autowired
    private Verifier verifier;

    @Test
    public void connectorsListForbidden() {
        ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1/connectors", JsonNode.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void connectorListWithValidToken() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<ListResult<Connector>> type = (Class) ListResult.class;
        ResponseEntity<ListResult<Connector>> response = get("/api/v1/connectors", type);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        ListResult<Connector> result = response.getBody();
        assertThat(result.getTotalCount()).as("connectors total").isGreaterThan(2);
        assertThat(result.getItems().size()).as("connector list").isGreaterThan(2);
    }

    @Test
    public void connectorsGetTest() {
        ResponseEntity<Connector> response = get("/api/v1/connectors/twitter", Connector.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        Connector connector = response.getBody();
        assertThat(connector).isNotNull();
        assertThat(connector.getId()).contains("twitter");
    }

    // Disabled as it works only for the LocalProcessVerifier which would needs some update
    @Test
    @Ignore
    public void verifyGoodTwitterConnectionSettings() throws IOException {
        Properties credentials = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/valid-twitter-keys.properties")) {
            credentials.load(is);
        }

        ResponseEntity<Verifier.Result> response = post("/api/v1/connectors/twitter/verifier/connectivity", credentials, Verifier.Result.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        Verifier.Result result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Verifier.Result.Status.OK);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @Ignore
    public void verifyBadTwitterConnectionSettings() throws IOException {

        // AlwaysOkVerifier never fails.. do don't try this test case, if that's whats being used.
        Assume.assumeFalse(verifier instanceof AlwaysOkVerifier);

        Properties credentials = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/valid-twitter-keys.properties")) {
            credentials.load(is);
        }
        credentials.put("accessTokenSecret", "badtoken");

        ResponseEntity<Verifier.Result> response = post("/api/v1/connectors/twitter/verifier/connectivity", credentials, Verifier.Result.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        Verifier.Result result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Verifier.Result.Status.ERROR);
        assertThat(result.getErrors()).isNotEmpty();
    }

}
