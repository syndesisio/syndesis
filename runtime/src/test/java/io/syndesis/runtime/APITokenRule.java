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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.rules.ExternalResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class APITokenRule extends ExternalResource {

    private static final String EXPIRED_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGSjg2R2NGM2pUYk5MT2NvNE52WmtVQ0lVbWZZQ3FvcXRPUWVNZmJoTmxFIn0.eyJqdGkiOiIzNTg2M2I4Ny1mMjQ2LTQ3NzItYTMyNy00Yzc3NzY5NjVjNDkiLCJleHAiOjE0ODYzMzQ1MDAsIm5iZiI6MCwiaWF0IjoxNDg2MzM0MjAwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvaXBhYXMtdGVzdCIsImF1ZCI6ImFkbWluLWNsaSIsInN1YiI6IjliZWRhNjUyLWY0NDYtNGFhZS1iODQ1LWQ1M2VjNDc1OGQ3OCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFkbWluLWNsaSIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjU3MzcyMmQ5LTc2NzAtNDhhZi1iMDY4LWUxNGNmNjRjM2U2NCIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjAyY2M5ZTVkLTU3NWUtNDEzNi05NDk5LWI5OGY4ZjhjYmFhYiIsImFsbG93ZWQtb3JpZ2lucyI6W10sInJlc291cmNlX2FjY2VzcyI6e30sIm5hbWUiOiJTYW1wbGUgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6InVzZXIiLCJnaXZlbl9uYW1lIjoiU2FtcGxlIiwiZmFtaWx5X25hbWUiOiJVc2VyIiwiZW1haWwiOiJzYW1wbGUtdXNlckBleGFtcGxlIn0.H2edv1-kUIYd7_nStjR-70hmdy7H6QG3sgjPhJGHhqMM6SMkjBjCHO0BHSkPFiG05fD6ah6kQAsxHIV-Bfd7k0rCoWrF3WH2mwtJDje36WLpGtFXbPNBUv0YFO5F61tkdCUL-gBJS-3VPWD68nskpAZcgabFGhM9TBxbC0geJzA";

    private RestTemplate restTemplate = new RestTemplate();

    private String keycloakScheme = System.getProperty("keycloak.http.scheme", "http");

    private String keycloakHost = System.getProperty("keycloak.http.host", "localhost");

    private int keycloakPort = Integer.parseUnsignedInt(System.getProperty("keycloak.http.port", "8080"));

    private String keycloakRealm = System.getProperty("keycloak.realm", "ipaas-test");

    private String keycloakProtocol = System.getProperty("keycloak.protocol", "openid-connect");

    private String accessToken;

    private String refreshToken;

    public APITokenRule() {
    }

    public APITokenRule(String keycloakScheme, String keycloakHost, int keycloakPort, String keycloakRealm, String keycloakProtocol) {
        this.keycloakScheme = keycloakScheme;
        this.keycloakHost = keycloakHost;
        this.keycloakPort = keycloakPort;
        this.keycloakRealm = keycloakRealm;
        this.keycloakProtocol = keycloakProtocol;
    }

    public String validToken() {
        return accessToken;
    }

    public String expiredToken() {
        return EXPIRED_TOKEN;
    }

    @Override
    protected void before() throws Throwable {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.set("username", "user");
        map.set("password", "password");
        map.set("grant_type", "password");
        map.set("client_id", "admin-cli");
        ResponseEntity<JsonNode> json = restTemplate.postForEntity(
            keycloakScheme + "://" + keycloakHost + ":" + keycloakPort + "/auth/realms/" + keycloakRealm + "/protocol/" + keycloakProtocol + "/token",
            map,
            JsonNode.class
        );
        assertThat(json.getStatusCode()).as("get token status code").isEqualTo(HttpStatus.OK);
        String token = json.getBody().get("access_token").textValue();
        assertThat(token).as("access token").isNotEmpty();
        accessToken = token;
        String refreshToken = json.getBody().get("refresh_token").textValue();
        assertThat(token).as("refresh token").isNotEmpty();
        this.refreshToken = refreshToken;
    }

    @Override
    protected void after() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.set("refresh_token", refreshToken);
        map.set("client_id", "admin-cli");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        ResponseEntity<JsonNode> json = restTemplate.postForEntity(
            "http://localhost:" + keycloakPort + "/auth/realms/" + keycloakRealm + "/protocol/" + keycloakProtocol + "/logout",
            map,
            JsonNode.class
        );
        assertThat(json.getStatusCode()).as("logout status code").isEqualTo(HttpStatus.NO_CONTENT);
    }
}
