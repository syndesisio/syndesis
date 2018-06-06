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

import java.util.Map;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionITCase extends BaseITCase {

    private static final ParameterizedTypeReference<Map<String, String>> MAP_OF_STRINGS = new ParameterizedTypeReference<Map<String, String>>() {
    };

    @Test
    public void shouldFetchDetailedVersion() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/json");

        final ResponseEntity<Map<String, String>> detailed = http(HttpMethod.GET, "/api/v1/version", null, MAP_OF_STRINGS,
            tokenRule.validToken(), headers, HttpStatus.OK);

        assertThat(detailed.getBody()).isNotEmpty();
    }

    @Test
    public void shouldFetchPlainVersion() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "text/plain");

        final ResponseEntity<String> plainVersion = http(HttpMethod.GET, "/api/v1/version", null, String.class, tokenRule.validToken(),
            headers, HttpStatus.OK);

        assertThat(plainVersion.getBody()).isNotEmpty();
    }
}
