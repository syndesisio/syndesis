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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.metadata.internal.TeiidDataSourceImpl;
import io.syndesis.dv.metadata.internal.TeiidServer;
import io.syndesis.dv.metadata.query.QSResult;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.model.export.v1.DataVirtualizationV1Adapter;
import io.syndesis.dv.openshift.SyndesisConnectionMonitor;
import io.syndesis.dv.openshift.SyndesisConnectionSynchronizer;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.Application;
import io.syndesis.dv.server.DvConfigurationProperties;
import io.syndesis.dv.server.SSOConfigurationProperties;
import io.syndesis.dv.server.endpoint.IntegrationTest.IntegrationTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, Application.class})
@SuppressWarnings("nls")
public class IntegrationTest {

    //inject simple auth bypass
    @TestConfiguration
    static class IntegrationTestConfiguration {
        @MockBean
        private SSOConfigurationProperties ssoConfigurationProperties;
        @MockBean
        private DvConfigurationProperties dvConfigurationProperties;
        /* Stub out the connectivity to syndesis / openshift */
        @MockBean
        private SyndesisConnectionMonitor syndesisConnectionMonitor;
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SyndesisConnectionSynchronizer syndesisConnectionSynchronizer;
    @Autowired
    private TeiidOpenShiftClient teiidOpenShiftClient;
    @Autowired
    private TeiidServer teiidServer;

    @Autowired DataSource datasource;

    //for some reason dirtiescontext does not seem to work, so clear manually
    @After public void after() throws Exception {
        try (Connection c = datasource.getConnection();) {
            c.createStatement().execute("delete from data_virtualization");
        }
    }

    @Test
    public void testError() throws Exception {
        QueryAttribute kqa = new QueryAttribute();
        ResponseEntity<String> response = restTemplate.postForEntity("/v1/metadata/query", kqa, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().endsWith("\"status\":403,\"error\":\"Forbidden\","
                + "\"message\":\"No query has been specified\",\"path\":\"/v1/metadata/query\"}"));

    }

    /**
     * Tests a simple view layering with no sources
     * @throws Exception
     */
    @Test
    public void testViewLayers() throws Exception {
        RestDataVirtualization rdv = new RestDataVirtualization();
        String dvName = "dv";
        rdv.setName(dvName);
        rdv.setDescription("description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/virtualizations", rdv, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseEntity<ViewDefinition> validateViewName = restTemplate.getForEntity(
                "/v1/virtualizations/{virtualization}/views/{viewName}",
                ViewDefinition.class, dvName, "myView");
        assertEquals(HttpStatus.NOT_FOUND, validateViewName.getStatusCode());

        ViewDefinition vd = new ViewDefinition(dvName, "myview");
        vd.setComplete(true);
        vd.setDdl("create view myview as select 1 as col");
        vd.setUserDefined(true);

        //using a string response as spring does not seem to handle the
        //unwrap correctly
        ResponseEntity<String> stashStatus = restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        assertEquals(HttpStatus.OK, stashStatus.getStatusCode());
        RestViewDefinitionStatus saved = JsonMarshaller.unmarshall(stashStatus.getBody(), RestViewDefinitionStatus.class);
        String id = saved.getViewDefinition().getId();

        assertEquals("SUCCESS", saved.getStatus());
        assertNotNull(saved.getViewDefinition().getCreatedAt());

        ViewDefinition vd2 = new ViewDefinition(dvName, "myview2");
        vd2.setComplete(true);
        vd2.setDdl("create view myview as select * from myview");
        vd2.setUserDefined(true);

        ResponseEntity<String> stashStatus2 = restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd2), String.class);

        assertEquals(HttpStatus.OK, stashStatus2.getStatusCode());
        String id2 = JsonMarshaller.unmarshall(stashStatus2.getBody(), RestViewDefinitionStatus.class)
                .getViewDefinition().getId();
        assertNotNull(id2);

        ResponseEntity<ViewDefinition> view = restTemplate.getForEntity(
                "/v1/editors/{id}",
                ViewDefinition.class, id);
        assertEquals(HttpStatus.OK, view.getStatusCode());

        assertNotNull(view.getBody().getModifiedAt());
        assertNotNull(view.getBody().getCreatedAt());
        assertNotNull(view.getBody().getVersion());

        validateViewName = restTemplate.getForEntity(
                "/v1/virtualizations/{virtualization}/views/{viewName}",
                ViewDefinition.class, dvName, "myView");
        assertEquals(HttpStatus.OK, validateViewName.getStatusCode());
        //means that it already exists, therefore not valid
        assertNotNull(validateViewName.getBody());

        query("select * from dv.myview", dvName, true);

        //myview2 is not yet valid
        query("select * from dv.myview2", dvName, false);

        //correct it
        vd2.setDdl("create view myview2 as select * from myview");
        restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd2), StatusObject.class);

        query("select * from dv.myview2", dvName, true);
    }

    private void query(String queryString, String dvName, boolean ok) {
        QueryAttribute queryAttribute = new QueryAttribute();
        queryAttribute.setQuery(queryString);
        queryAttribute.setTarget(dvName);
        ResponseEntity<QSResult> query = restTemplate.postForEntity(
                "/v1/metadata/query", queryAttribute, QSResult.class);
        if (ok) {
            assertEquals(HttpStatus.OK, query.getStatusCode());
            QSResult result = query.getBody();
            assertEquals(1, result.getColumns().size());
            assertEquals(1, result.getRows().size());
        } else {
            //resolving error is a bad request
            assertEquals(HttpStatus.BAD_REQUEST, query.getStatusCode());
            //TODO: need to make sure of the other codes
            //can't connect = service unavailable
            //unexpected exception = 500
            //need to ensure that a redeploy while querying is accounted for
        }
    }

    @Autowired
    DefaultMetadataInstance metadata;

    /**
     * Tests an update to source metadata
     * @throws Exception
     */
    @Test
    public void testSourceRefresh() throws Exception {
        RestDataVirtualization rdv = new RestDataVirtualization();
        String dvName = "testSourceRefresh";
        rdv.setName(dvName);
        rdv.setDescription("description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/virtualizations", rdv, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        //the syndesis and os logic has been stubbed, but we can interact
        //directly with the synchronizer

        DefaultSyndesisDataSource dsd = new DefaultSyndesisDataSource();
        dsd.setId("test");
        //invalid, but should silently fail
        syndesisConnectionSynchronizer.addConnection(dsd, false);

        dsd.setId("1");
        dsd.setSyndesisName("super integration source");
        dsd.setTranslatorName("h2");
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("url", "jdbc:h2:mem:"+dvName);
        properties.put("schema", "DV");
        dsd.setProperties(properties);
        dsd.setDefinition(teiidOpenShiftClient.getSourceDefinitionThatMatches(properties, "sql"));

        syndesisConnectionSynchronizer.addConnection(dsd, false);

        Thread.sleep(1000); //TODO: wait for this to fail

        //source is still invalid - no tables, but we should still have gotten a local name assigned
        String teiidName = "superintegrationsource";
        assertEquals(teiidName, dsd.getTeiidName());

        //preview vdb should still be up regardless
        query("select 1", dvName, true);

        //add a source table
        TeiidDataSourceImpl tds = metadata.getDataSource(teiidName);
        Connection c = ((DataSource)tds.getConnectionFactory()).getConnection();
        c.createStatement().execute("create schema DV");
        c.createStatement().execute("create table DV.t (col integer)");

        //issue a refresh over rest, first not valid
        ResponseEntity<StatusObject> statusResponse = restTemplate.postForEntity(
                "/v1/metadata/refreshSchema/xyz", rdv, StatusObject.class);
        assertEquals(HttpStatus.NOT_FOUND, statusResponse.getStatusCode());
        //now valid
        statusResponse = restTemplate.postForEntity(
                "/v1/metadata/refreshSchema/{teiidName}", rdv, StatusObject.class, teiidName);
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000); //TODO: a better wait for this to succeed
            try {
                query("select col from superintegrationsource.t union select 1 as col", dvName, true);
                break;
            } catch (AssertionError e) {
                if (i == 9) {
                    throw e;
                }
            }
        }

        //test that unqualified does not work
        query("select col from t union select 1 as col", dvName, false);

        ResponseEntity<List> sourceStatusResponse = restTemplate.getForEntity("/v1/metadata/sourceStatuses", List.class);
        assertEquals(HttpStatus.OK, sourceStatusResponse.getStatusCode());
        Map status = (Map)sourceStatusResponse.getBody().get(0);
        assertEquals(0, ((List)status.get("errors")).size());
        assertEquals("ACTIVE", status.get("schemaState"));
        assertEquals(Boolean.FALSE, status.get("loading"));
        Long last = (Long)status.get("lastLoad");
        assertNotNull(last);

        //add another source table
        c.createStatement().execute("create table DV.t2 (col integer)");
        //update through the synchronizer
        syndesisConnectionSynchronizer.addConnection(dsd, true);

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000); //TODO: a better wait for this to succeed
            try {
                query("select col from superintegrationsource.t2 union select 1 as col", dvName, true);
                break;
            } catch (AssertionError e) {
                if (i == 9) {
                    throw e;
                }
            }
        }

        c.createStatement().execute("create table DV.t3 (col integer)");

        //manually call the timed refresh - there's more refactoring to do to isolate syndesis calls
        syndesisConnectionSynchronizer.synchronizeConnections(true, Arrays.asList(dsd));

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000); //TODO: a better wait for this to succeed
            try {
                query("select col from superintegrationsource.t3 union select 1 as col", dvName, true);
                break;
            } catch (AssertionError e) {
                if (i == 9) {
                    throw e;
                }
            }
        }

        c.createStatement().execute("drop schema DV CASCADE");

        syndesisConnectionSynchronizer.addConnection(dsd, true);

        //the same query should now fail that the schema was dropped
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000); //TODO: a better wait for this to succeed
            try {
                query("select col from superintegrationsource.t2 union select 1 as col", dvName, false);
                break;
            } catch (AssertionError e) {
                if (i == 9) {
                    throw e;
                }
            }
        }

        sourceStatusResponse = restTemplate.getForEntity("/v1/metadata/sourceStatuses", List.class);
        assertEquals(HttpStatus.OK, sourceStatusResponse.getStatusCode());
        status = (Map)sourceStatusResponse.getBody().get(0);
        assertEquals(1, ((List)status.get("errors")).size());
        assertEquals("FAILED", status.get("schemaState"));
        assertEquals(Boolean.FALSE, status.get("loading"));
        Long errorLast = (Long)status.get("lastLoad");
        assertNotNull(errorLast);
        assertTrue(errorLast.longValue() > last);

        ResponseEntity<List> virts = restTemplate.getForEntity("/v1/virtualizations", List.class);
        assertEquals(HttpStatus.OK, virts.getStatusCode());
        assertEquals(1, virts.getBody().size());
        Map virt = (Map)virts.getBody().get(0);
        assertEquals("testSourceRefresh", virt.get("name"));

        //should stay the same instance if nothing has changed
        TeiidDataSourceImpl impl = this.teiidServer.getDatasources().get(dsd.getTeiidName());
        syndesisConnectionSynchronizer.addConnection(dsd, true);
        TeiidDataSourceImpl impl1 = this.teiidServer.getDatasources().get(dsd.getTeiidName());
        assertSame(impl, impl1);

        //should change as the name is different
        DefaultSyndesisDataSource nameChange = dsd.clone();
        nameChange.setSyndesisName("new-name");
        syndesisConnectionSynchronizer.addConnection(nameChange, true);
        impl1 = this.teiidServer.getDatasources().get(nameChange.getTeiidName());
        assertNotEquals(impl.getSyndesisDataSource().getSyndesisName(),  impl1.getSyndesisDataSource().getSyndesisName());
    }

    @Test
    public void testImportExport() throws IOException {
        RestDataVirtualization rdv = new RestDataVirtualization();
        String dvName = "testExport";
        rdv.setName(dvName);
        rdv.setDescription("description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/virtualizations", rdv, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ViewDefinition vd = new ViewDefinition(dvName, "myview");
        vd.setComplete(true);
        vd.setDdl("create view myview as select 1 as col");
        vd.setUserDefined(true);

        restTemplate.exchange(
                "/v1/editors", HttpMethod.PUT,
                new HttpEntity<ViewDefinition>(vd), String.class);

        ResponseEntity<byte[]> export = restTemplate.getForEntity("/v1/virtualizations/testExport/export", byte[].class);
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
        assertEquals("testExport", dv.getName());

        ze = zis.getNextEntry();
        assertEquals("dv-info.json", ze.getName());
        JsonNode info = mapper.readTree(zis);
        assertEquals(1, info.get("exportVersion").asInt());
        assertEquals(2, info.get("entityVersion").asInt());
        assertEquals("draft", info.get("revision").asText());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(result) {
            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public String getFilename() {
                return "import.zip";
            }
        });

        ResponseEntity<StatusObject> importResponse = restTemplate.postForEntity(
                "/v1/virtualizations", body, StatusObject.class);

        //trying to re-import with the existing name
        assertEquals(HttpStatus.CONFLICT, importResponse.getStatusCode());

        importResponse = restTemplate.postForEntity(
                "/v1/virtualizations?virtualization={name}", body, StatusObject.class, "newName");

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());

        ResponseEntity<List> views = restTemplate.getForEntity(
                "/v1/virtualizations/{name}/views", List.class, "newName");

        assertEquals(HttpStatus.OK, views.getStatusCode());
        assertEquals(1, views.getBody().size());
        Map view = (Map)views.getBody().get(0);
        assertEquals(Boolean.TRUE, view.get("valid"));
    }

    @Test
    public void testSwagger() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v1/swagger.json", String.class);
        assertTrue(response.getBody().contains("Editor Service"));
    }
}
