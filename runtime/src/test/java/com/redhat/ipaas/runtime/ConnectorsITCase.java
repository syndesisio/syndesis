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

import com.redhat.ipaas.model.ListResult;
import com.redhat.ipaas.model.connection.Connector;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorsITCase extends BaseITCase {

    @Test
    public void connectorsListWithoutToken() {
        ResponseEntity<ListResult> response = restTemplate().getForEntity("/api/v1/connectors", ListResult.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void connectorListWithExpiredToken() {
        get("/api/v1/connectors", ListResult.class, tokenRule.expiredToken(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void connectorListWithValidToken() {
        ResponseEntity<ListResult> response = get("/api/v1/connectors", ListResult.class);
        ListResult<Connector> result = response.getBody();
        assertThat(result.getTotalCount()).as("connectors total").isEqualTo(2);
        assertThat(result.getItems()).as("connector list").hasSize(2);
    }

    @Test
    public void connectorsGetTest() {
        ResponseEntity<Connector> result = get("/api/v1/connectors/twitter", Connector.class);
        Connector connector = result.getBody();
        assertThat(connector).isNotNull();
        assertThat(connector.getId()).contains("twitter");
    }

}
