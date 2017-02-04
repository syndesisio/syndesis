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
package com.redhat.ipaas.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.ipaas.api.v1.model.Component;
import com.redhat.ipaas.api.v1.model.ListResult;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentsITCase extends BaseITCase {

    private int keycloakPort = Integer.parseUnsignedInt(System.getProperty("keycloak.http.port", "8080"));

    private String token() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", "user");
        map.add("password", "password");
        map.add("grant_type", "password");
        map.add("client_id", "admin-cli");
        ResponseEntity<JsonNode> json = restTemplate().postForEntity("http://localhost:" + keycloakPort + "/auth/realms/ipaas-test/protocol/openid-connect/token", map, JsonNode.class);
        assertThat(json.getStatusCode()).as("get token status code").isEqualTo(HttpStatus.OK);
        String token = json.getBody().get("access_token").textValue();
        assertThat(token).as("access token").isNotEmpty();
        return token;
    }

    @Test
    public void componentsListWithoutToken() {
        ResponseEntity<ListResult> response = restTemplate().getForEntity("/api/v1/components", ListResult.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void componentsListWithValidToken() {
        String token = token();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<ListResult> response = restTemplate().exchange("/api/v1/components", HttpMethod.GET, new HttpEntity<>(headers), ListResult.class);
        assertThat(response.getStatusCode()).as("components list status code").isEqualTo(HttpStatus.OK);
        ListResult<Component> result = response.getBody();
        assertThat(result.getTotalCount()).as("components total").isEqualTo(50);
        assertThat(result.getItems()).as("components list").hasSize(20);
    }

    @Test
    public void componentsGetTest() {
        Component result = restTemplate().getForObject("/api/v1/components/1", Component.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).contains("1");
    }

}
