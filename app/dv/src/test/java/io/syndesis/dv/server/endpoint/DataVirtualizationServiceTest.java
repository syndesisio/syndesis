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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.adminapi.AdminException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.datasources.H2SQLDefinition;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.repository.RepositoryConfiguration;
import io.syndesis.dv.repository.RepositoryManagerImpl;
import io.syndesis.dv.rest.JsonMarshaller;

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

    @Test public void testImport() throws AdminException {
        ImportPayload payload = new ImportPayload();
        payload.setTables(Arrays.asList("tbl", "tbl2", "tbl3"));

        assertThatThrownBy(() -> dataVirtualizationService.importViews("dv", "source", payload))
            .isInstanceOf(ResponseStatusException.class);

        DataVirtualization dv = workspaceManagerImpl.createDataVirtualization("dv");

        assertThatThrownBy(() -> dataVirtualizationService.importViews("dv", "source", payload))
            .isInstanceOf(ResponseStatusException.class);

        DefaultSyndesisDataSource sds = createH2DataSource("source");
        metadataInstance.registerDataSource(sds);

        assertThatThrownBy(() -> dataVirtualizationService.importViews("dv", "source", payload))
            .isInstanceOf(ResponseStatusException.class);

        //add the schema definition - so that we don't really need the datasource, and redeploy
        workspaceManagerImpl.createSchema("someid", "source",
                "create foreign table tbl (col string) options (\"teiid_rel:fqn\" 'schema=s/table=tbl');"
                + "create foreign table tbl2 (col string) options (\"teiid_rel:fqn\" 'schema=s/table=tbl2');"
                + "create foreign table tbl3 (col string) options (\"teiid_rel:fqn\" 'schema=s/table=tbl3');");
        metadataInstance.undeployDynamicVdb(MetadataService.getWorkspaceSourceVdbName("source"));

        StatusObject kso = dataVirtualizationService.importViews("dv", "source", payload);
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
                "  \"ddl\" : \"CREATE VIEW tbl (\\n  col\\n) AS \\n  SELECT \\n    t1.col\\n  FROM \\n    source.tbl AS t1\",\n" +
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

    @Test public void testValidateNameUsingGet() {
        assertThatThrownBy(() -> dataVirtualizationService.getDataVirtualization("foo"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        //must end with number/letter
        assertThatThrownBy(() ->  dataVirtualizationService.getDataVirtualization("foo-"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        //bad chars
        assertThatThrownBy(() -> dataVirtualizationService.getDataVirtualization("%foo&"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

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
