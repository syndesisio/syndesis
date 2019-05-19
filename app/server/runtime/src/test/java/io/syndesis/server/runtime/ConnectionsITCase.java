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
package io.syndesis.server.runtime;

import java.util.List;
import java.util.UUID;

import io.syndesis.common.model.Violation;
import io.syndesis.common.model.connection.Connection;

import io.syndesis.common.model.connection.ConnectionOverview;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionsITCase extends BaseITCase {

    private final static ParameterizedTypeReference<List<Violation>> RESPONSE_TYPE = new ParameterizedTypeReference<List<Violation>>() {
        // defining type parameters
    };

    private final String id = UUID.randomUUID().toString();

    @Test
    public void emptyNamesShouldNotBeAllowed() {
        final Connection connection = new Connection.Builder().name(" ").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).containsExactly(
            new Violation.Builder().property("name").error("NotNull").message("Value is required").build());
    }

    @Test
    public void emptyTagsShouldBeIgnored() {
        final Connection connection = new Connection.Builder()
            .id("tags-connection-test")
            .connectorId("http")
            .name("tags-connection-test")
            .addTags("", " ", "taggy")
            .build();

        final ResponseEntity<Connection> got = post("/api/v1/connections", connection, Connection.class,
            tokenRule.validToken(), HttpStatus.OK);

        final Connection created = got.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getTags()).containsExactly("taggy");
    }

    @Test
    public void nullNamesShouldNotBeAllowed() {
        final Connection connection = new Connection.Builder().build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).containsExactly(
            new Violation.Builder().property("name").error("NotNull").message("Value is required").build());
    }

    @Before
    public void preexistingConnection() {
        final Connection connection = new Connection.Builder().name("Existing connection").id(id).build();

        dataManager.create(connection);
    }

    @Test
    public void shouldAllowConnectionUpdateWithExistingName() {
        final Connection connection = new Connection.Builder().name("Existing connection").connectorId("http").id(id).build();

        final ResponseEntity<Void> got = put("/api/v1/connections/" + id, connection, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        assertThat(got.getBody()).isNull();
    }

    @Test
    public void shouldDetermineValidityForInvalidConnections() {
        final Connection connection = new Connection.Builder().name("Existing connection").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).containsExactly(new Violation.Builder().property("name").error("UniqueProperty")
            .message("Value 'Existing connection' is not unique").build());
    }

    @Test
    public void shouldDetermineValidityForValidConnections() {
        final Connection connection = new Connection.Builder().name("Test connection").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        assertThat(got.getBody()).isNull();
    }

    @Test
    public void violationsShouldBeGivenForInvalidConnectionCreation() {
        final Connection connection = new Connection.Builder().name("Existing connection").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).containsExactly(new Violation.Builder().property("create.obj.name")
            .error("UniqueProperty").message("Value 'Existing connection' is not unique").build());
    }

    @Test
    public void checkKnativeConnectionAbsentByDefault() {
        get("/api/v1/connections/knative", ConnectionOverview.class, HttpStatus.NOT_FOUND);
    }
}
