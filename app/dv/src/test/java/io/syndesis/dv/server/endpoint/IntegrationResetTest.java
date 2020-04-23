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

package io.syndesis.dv.server.endpoint;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.server.Application;
import io.syndesis.dv.server.endpoint.IntegrationTest.IntegrationTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, Application.class})
@SuppressWarnings("nls")
public class IntegrationResetTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testReset() throws Exception {
        RestDataVirtualization rdv = new RestDataVirtualization();
        String dvName = "testPublish";
        rdv.setName(dvName);
        rdv.setDescription("description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/virtualizations", rdv, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseEntity<List> virts = restTemplate.getForEntity("/v1/virtualizations", List.class);
        assertEquals(HttpStatus.OK, virts.getStatusCode());
        assertEquals(1, virts.getBody().size());

        ResponseEntity<String> resetResponse = restTemplate.getForEntity("/v1/test-support/reset-db", String.class);
        assertEquals(HttpStatus.OK, resetResponse.getStatusCode());

        virts = restTemplate.getForEntity("/v1/virtualizations", List.class);
        assertEquals(HttpStatus.OK, virts.getStatusCode());
        assertEquals(0, virts.getBody().size());
    }

}
