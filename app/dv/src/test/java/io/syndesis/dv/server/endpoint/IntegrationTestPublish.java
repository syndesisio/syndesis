/*
 * Copyright (C) 2013 Red Hat, Inc.
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

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.openshift.SyndesisConnectionSynchronizer;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.Application;
import io.syndesis.dv.server.V1Constants;
import io.syndesis.dv.server.endpoint.IntegrationTest.IntegrationTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, Application.class})
@SuppressWarnings("nls")
public class IntegrationTestPublish {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SyndesisConnectionSynchronizer syndesisConnectionSynchronizer;
    @Autowired
    private TeiidOpenShiftClient teiidOpenShiftClient;

    @Autowired DataSource datasource;

    //for some reason dirtiescontext does not seem to work, so clear manually
    @After public void after() throws Exception {
        try (Connection c = datasource.getConnection();) {
            c.createStatement().execute("delete from data_virtualization");
        }
    }

    @Autowired
    DefaultMetadataInstance metadata;

    @Test
    public void testPublishRevert() throws Exception {
        RestDataVirtualization rdv = new RestDataVirtualization();
        String dvName = "testPublish";
        rdv.setName(dvName);
        rdv.setDescription("description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/virtualizations", rdv, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ViewDefinition vd = new ViewDefinition(dvName, "myview");
        vd.setComplete(true);
        vd.setDdl("create view myview as select 1 as col");
        vd.setUserDefined(true);

        ResponseEntity<String> stashStatus = restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        assertEquals(HttpStatus.OK, stashStatus.getStatusCode());
        RestViewDefinitionStatus saved = JsonMarshaller.unmarshall(stashStatus.getBody(), RestViewDefinitionStatus.class);
        String id = saved.getViewDefinition().getId();

        ResponseEntity<List> listResponse = restTemplate.getForEntity("/v1/virtualizations/publish/{name}", List.class, dvName);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertTrue(listResponse.getBody().isEmpty());

        PublishRequestPayload publishPayload = new PublishRequestPayload();
        publishPayload.setName(dvName);

        ResponseEntity<StatusObject> statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish", HttpMethod.POST,
                new HttpEntity<PublishRequestPayload>(publishPayload), StatusObject.class);

        //check that it published
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        StatusObject status = statusResponse.getBody();
        assertEquals("1", status.getAttributes().get(V1Constants.REVISION));
        assertEquals("dv-testpublish", status.getAttributes().get("OpenShift Name"));

        //there should be a new edition
        listResponse = restTemplate.getForEntity("/v1/virtualizations/publish/{name}", List.class, dvName);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().size());

        Map<?, ?> map = (Map<?, ?>) listResponse.getBody().get(0);
        assertEquals(1, map.get(V1Constants.REVISION));

        //modify a view
        vd.setDdl("create view myview as select 1 as colX");
        restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        //publish again
        statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish", HttpMethod.POST,
                new HttpEntity<PublishRequestPayload>(publishPayload), StatusObject.class);

        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        status = statusResponse.getBody();
        assertEquals("2", status.getAttributes().get(V1Constants.REVISION));
        assertEquals("dv-testpublish", status.getAttributes().get("OpenShift Name"));

        //there should be a new edition
        listResponse = restTemplate.getForEntity("/v1/virtualizations/publish/{name}", List.class, dvName);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(2, listResponse.getBody().size());

        ResponseEntity<ViewDefinition> view = restTemplate.getForEntity(
                "/v1/editors/{id}",
                ViewDefinition.class, id);
        assertEquals(HttpStatus.OK, view.getStatusCode());
        assertEquals("create view myview as select 1 as colX", view.getBody().getDdl());

        //go back to 1
        statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish/{name}/{edition}/revert", HttpMethod.POST,
                new HttpEntity<PublishRequestPayload>(publishPayload), StatusObject.class, dvName, 1);
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        view = restTemplate.getForEntity(
                "/v1/editors/{id}",
                ViewDefinition.class, id);
        //for now ids are reassigned
        assertEquals(HttpStatus.NOT_FOUND, view.getStatusCode());

        ResponseEntity<List> views = restTemplate.getForEntity(
                "/v1/virtualizations/{name}/views", List.class, dvName);

        assertEquals(HttpStatus.OK, views.getStatusCode());
        assertEquals(1, views.getBody().size());
        Map<?, ?> viewMap = (Map<?, ?>) views.getBody().get(0);
        id = (String) viewMap.get("id");

        //back to the old
        view = restTemplate.getForEntity(
                "/v1/editors/{id}",
                ViewDefinition.class, id);
        assertEquals(HttpStatus.OK, view.getStatusCode());
        assertEquals("create view myview as select 1 as col", view.getBody().getDdl());

        //start 1
        statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish/{name}/{edition}/start", HttpMethod.POST,
                null, StatusObject.class, dvName, 1);
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
    }
}
