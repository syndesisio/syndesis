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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.datasources.H2SQLDefinition;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.repository.RepositoryConfiguration;
import io.syndesis.dv.repository.RepositoryManagerImpl;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.endpoint.DataVirtualizationService;
import io.syndesis.dv.server.endpoint.ImportPayload;
import io.syndesis.dv.server.endpoint.MetadataService;
import io.syndesis.dv.server.endpoint.RestDataVirtualization;
import io.syndesis.dv.server.endpoint.StatusObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfiguration.class, ServiceTestConfiguration.class})
@DirtiesContext
@SuppressWarnings("nls")
public class DataVirtualizationServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositoryManagerImpl workspaceManagerImpl;

    @Autowired
    private DataVirtualizationService dataVirtualizationService;

    @Autowired
    private DefaultMetadataInstance metadataInstance;

    @Test public void testImport() throws Exception {
        ImportPayload payload = new ImportPayload();
        payload.setTables(Arrays.asList("tbl", "tbl2", "tbl3"));

        StatusObject kso = null;
        try {
            kso = dataVirtualizationService.importViews("dv", "source", payload);
            fail();
        } catch (ResponseStatusException e) {
            //dv not found
        }

        DataVirtualization dv = workspaceManagerImpl.createDataVirtualization("dv");

        try {
            kso = dataVirtualizationService.importViews("dv", "source", payload);
            fail();
        } catch (ResponseStatusException e) {
            //source not found
        }

        DefaultSyndesisDataSource sds = createH2DataSource("source");
        metadataInstance.registerDataSource(sds);

        try {
            kso = dataVirtualizationService.importViews("dv", "source", payload);
            fail();
        } catch (ResponseStatusException e) {
            //source not found - as the properties are not valid
        }

        //add the schema definition - so that we don't really need the datasource, and redeploy
        workspaceManagerImpl.createSchema("someid", "source",
                "create foreign table tbl (col string) options (\"teiid_rel:fqn\" 'schema=s/table=tbl');"
                + "create foreign table tbl2 (col string) options (\"teiid_rel:fqn\" 'schema=s/table=tbl2');"
                + "create foreign table tbl3 (col string) options (\"teiid_rel:fqn\" 'schema=s/table=tbl3');");
        metadataInstance.undeployDynamicVdb(MetadataService.getWorkspaceSourceVdbName("source"));

        kso = dataVirtualizationService.importViews("dv", "source", payload);
        assertEquals(3, kso.getAttributes().size());

        for (String id : kso.getAttributes().values()) {
            ViewDefinition vd = workspaceManagerImpl.findViewDefinition(id);
            assertEquals(Long.valueOf(0), vd.getVersion());
        }

        String id = kso.getAttributes().values().iterator().next();

        ViewDefinition vd = workspaceManagerImpl.findViewDefinition(id);

        assertTrue(vd.isParsable());

        vd.setId("consistent");
        assertEquals("{\n" +
                "  \"complete\" : true,\n" +
                "  \"dataVirtualizationName\" : \"dv\",\n" +
                "  \"ddl\" : \"CREATE VIEW tbl (col) AS \\nSELECT col\\nFROM source.tbl\",\n" +
                "  \"id\" : \"consistent\",\n" +
                "  \"name\" : \"tbl\",\n" +
                "  \"sourcePaths\" : [ \"schema=source/table=tbl\" ],\n" +
                "  \"userDefined\" : false,\n" +
                "  \"version\" : 0\n" +
                "}", JsonMarshaller.marshall(vd));

        vd.setId(id);

        entityManager.flush();

        assertEquals(Long.valueOf(1), dv.getVersion());
    }

    @Test public void testValidateNameUsingGet() throws Exception {
        try {
            dataVirtualizationService.getDataVirtualization("foo");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }

        //must end with number/letter
        try {
            dataVirtualizationService.getDataVirtualization("foo-");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        }

        //bad chars
        try {
            dataVirtualizationService.getDataVirtualization("%foo&");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        }

        workspaceManagerImpl.createDataVirtualization("foo");

        //conflicts
        RestDataVirtualization response = dataVirtualizationService.getDataVirtualization("FOO");
        assertNotNull(response);
    }

    static DefaultSyndesisDataSource createH2DataSource(String name) {
        DefaultSyndesisDataSource sds = new DefaultSyndesisDataSource();
        sds.setDefinition(new H2SQLDefinition());
        sds.setId("someid");
        sds.setTeiidName(name);
        sds.setTranslatorName("h2");
        sds.setSyndesisName(name);
        Map<String, String> properties = new HashMap<>();
        properties.put("url", "jdbc:h2:mem:");
        properties.put("user", "sa");
        properties.put("password", "sa");
        sds.setProperties(properties);
        return sds;
    }
}
