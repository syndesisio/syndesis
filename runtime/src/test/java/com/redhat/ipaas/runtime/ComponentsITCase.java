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

import com.redhat.ipaas.api.v1.model.Component;
import com.redhat.ipaas.api.v1.model.ListResult;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentsITCase extends BaseITCase {

    @Test
    public void componentsListWithoutToken() {
        ResponseEntity<ListResult> response = restTemplate().getForEntity("/api/v1/components", ListResult.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void componentsListWithExpiredToken() {
        get("/api/v1/components", ListResult.class, tokenRule.expiredToken(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void componentsListWithValidToken() {
        ResponseEntity<ListResult> response = get("/api/v1/components", ListResult.class);
        ListResult<Component> result = response.getBody();
        assertThat(result.getTotalCount()).as("components total").isEqualTo(50);
        assertThat(result.getItems()).as("components list").hasSize(20);
    }

    @Test
    public void componentsGetTest() {
        ResponseEntity<Component> response = get("/api/v1/components/1", Component.class);
        Component result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getId()).contains("1");
    }

}
