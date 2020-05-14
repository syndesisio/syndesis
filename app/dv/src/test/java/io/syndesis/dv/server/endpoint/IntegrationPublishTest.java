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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.model.export.v1.DataVirtualizationV1Adapter;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.Application;
import io.syndesis.dv.server.V1Constants;
import io.syndesis.dv.server.endpoint.IntegrationTest.IntegrationTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, Application.class})
@TestPropertySource(properties = "spring.config.name=application-test")
@SuppressWarnings("nls")
public class IntegrationPublishTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired DataSource datasource;

    //for some reason dirtiescontext does not seem to work, so clear manually
    @After public void after() throws SQLException {
        try (Connection c = datasource.getConnection();) {
            c.createStatement().execute("delete from data_virtualization");
        }
    }

    @Autowired
    DefaultMetadataInstance metadata;

    @Test
    public void testPublishRevert() throws IOException {
        RestDataVirtualization rdv = new RestDataVirtualization();
        String dvName = "testPublish";
        rdv.setName(dvName);
        rdv.setDescription("description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/virtualizations", rdv, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ViewDefinition vd = new ViewDefinition(dvName, "myview");
        vd.setComplete(true);
        vd.setDdl("create view myview as bad");
        vd.setUserDefined(true);

        ResponseEntity<String> stashStatus = restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        assertEquals(HttpStatus.OK, stashStatus.getStatusCode());
        RestViewDefinitionStatus saved = JsonMarshaller.unmarshall(stashStatus.getBody(), RestViewDefinitionStatus.class);
        String id = saved.getViewDefinition().getId();

        @SuppressWarnings({"rawtypes", "unchecked"})
        final Class<List<?>> listOfAnything = (Class) List.class;

        ResponseEntity<List<?>> listResponse = restTemplate.getForEntity("/v1/virtualizations/publish/{name}", listOfAnything, dvName);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertTrue(listResponse.getBody().isEmpty());

        PublishRequestPayload publishPayload = new PublishRequestPayload();
        publishPayload.setName(dvName);

        //try to publish
        ResponseEntity<StatusObject> statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish", HttpMethod.POST,
                new HttpEntity<PublishRequestPayload>(publishPayload), StatusObject.class);

        //check that failed to published
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        StatusObject status = statusResponse.getBody();
        assertNull(status.getAttributes().get(V1Constants.REVISION));
        assertEquals("myview is not parsable", status.getAttributes().get("error"));

        //correct the view
        vd.setDdl("create view myview as select 1 as col");
        restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        //actually publish
        statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish", HttpMethod.POST,
                new HttpEntity<PublishRequestPayload>(publishPayload), StatusObject.class);

        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        status = statusResponse.getBody();
        assertEquals("1", status.getAttributes().get(V1Constants.REVISION));
        assertEquals("dv-testpublish", status.getAttributes().get("OpenShift Name"));

        //there should be a new edition
        listResponse = restTemplate.getForEntity("/v1/virtualizations/publish/{name}", listOfAnything, dvName);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().size());

        ResponseEntity<RestDataVirtualization> virtResponse = restTemplate.getForEntity("/v1/virtualizations/{name}", RestDataVirtualization.class, dvName);
        assertFalse(virtResponse.getBody().isModified());

        Map<?, ?> map = (Map<?, ?>) listResponse.getBody().get(0);
        assertEquals(1, map.get(V1Constants.REVISION));

        //modify a view
        vd.setDdl("create view myview as select 1 as colX");
        restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        virtResponse = restTemplate.getForEntity("/v1/virtualizations/{name}", RestDataVirtualization.class, dvName);
        assertTrue(virtResponse.getBody().isModified());
        assertEquals(1, virtResponse.getBody().getEditionCount());

        //publish again
        statusResponse = restTemplate.exchange(
                "/v1/virtualizations/publish", HttpMethod.POST,
                new HttpEntity<PublishRequestPayload>(publishPayload), StatusObject.class);

        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        status = statusResponse.getBody();
        assertEquals("2", status.getAttributes().get(V1Constants.REVISION));
        assertEquals("dv-testpublish", status.getAttributes().get("OpenShift Name"));

        //there should be a new edition
        listResponse = restTemplate.getForEntity("/v1/virtualizations/publish/{name}", listOfAnything, dvName);
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

        virtResponse = restTemplate.getForEntity("/v1/virtualizations/{name}", RestDataVirtualization.class, dvName);
        assertFalse(virtResponse.getBody().isModified());
        assertEquals(2, virtResponse.getBody().getEditionCount());

        view = restTemplate.getForEntity(
                "/v1/editors/{id}",
                ViewDefinition.class, id);
        //for now ids are reassigned
        assertEquals(HttpStatus.NOT_FOUND, view.getStatusCode());

        ResponseEntity<List<?>> views = restTemplate.getForEntity(
                "/v1/virtualizations/{name}/views", listOfAnything, dvName);

        assertEquals(HttpStatus.OK, views.getStatusCode());
        assertEquals(1, views.getBody().size());
        Map<?, ?> viewMap = (Map<?, ?>) views.getBody().get(0);
        id = (String) viewMap.get("id");

        //export 1
        ResponseEntity<byte[]> export = restTemplate.getForEntity("/v1/virtualizations/{name}/export/1", byte[].class, dvName);
        assertEquals(HttpStatus.OK, export.getStatusCode());
        byte[] result = export.getBody();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(result));
        ZipEntry ze = zis.getNextEntry();
        assertEquals("dv.json", ze.getName());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //prevent the autoclose
        DataVirtualizationV1Adapter dv = mapper.readValue(new InputStream() {

            @Override
            public int read() throws IOException {
                return zis.read();
            }

        }, DataVirtualizationV1Adapter.class);
        assertEquals("testPublish", dv.getName());
        assertEquals("create view myview as select 1 as col", dv.getViews().get(0).getDdl());

        ze = zis.getNextEntry();
        assertEquals("dv-info.json", ze.getName());
        JsonNode info = mapper.readTree(zis);
        assertEquals(1, info.get("exportVersion").asInt());
        assertEquals(3, info.get("entityVersion").asInt());
        assertEquals(1, info.get("revision").asInt());

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
