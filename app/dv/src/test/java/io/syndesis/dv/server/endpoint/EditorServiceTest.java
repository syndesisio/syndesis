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

import org.junit.Test;
import org.junit.runner.RunWith;

import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.repository.RepositoryConfiguration;
import io.syndesis.dv.repository.RepositoryManagerImpl;
import io.syndesis.dv.server.endpoint.DataVirtualizationService;
import io.syndesis.dv.server.endpoint.EditorService;
import io.syndesis.dv.server.endpoint.ViewListing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.adminapi.Model.Type;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBMetaData;

@SuppressWarnings("nls")
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfiguration.class, ServiceTestConfiguration.class})
public class EditorServiceTest {

    @Autowired
    private DefaultMetadataInstance metadataInstance;

    @Autowired
    private EditorService utilService;

    @Autowired
    private DataVirtualizationService dvService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositoryManagerImpl repositoryManager;

    @Test public void testStash() throws Exception {

        repositoryManager.createDataVirtualization("x");

        ViewDefinition vd = new ViewDefinition("x", "y");

        ViewDefinition saved = utilService.upsertViewEditorState(vd);

        entityManager.flush();

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getModifiedAt());
        assertEquals(Long.valueOf(0), saved.getVersion());
        assertNotNull(saved.getId());

        vd = new ViewDefinition("x", "y");
        vd.setId("not correct");

        try {
            saved = utilService.upsertViewEditorState(vd);
            fail();
        } catch (ResponseStatusException e) {
            //trying to change the id
        }

        //add a dummy preview vdb
        VDBMetaData vdb = dummyPreviewVdb();
        metadataInstance.deploy(vdb);

        entityManager.clear();

        //update with invalid ddl
        vd.setId(null);
        vd.setDdl("create something");
        vd.setUserDefined(true);
        vd.setComplete(true);

        saved = utilService.upsertViewEditorState(vd);

        entityManager.flush();

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getModifiedAt());
        assertEquals(Long.valueOf(1), saved.getVersion());
        assertNotNull(saved.getId());

        ViewDefinition found = repositoryManager.findViewDefinition(saved.getId());
        assertEquals("create something", found.getDdl());

        //saving with valid ddl
        vd.setDdl("create view y as select * from v");

        saved = utilService.upsertViewEditorState(vd);

        //the save does not determine the source paths
        assertEquals(Arrays.asList(), saved.getSourcePaths());

        for (ViewListing vl : dvService.getViewList("x")) {
            assertTrue(vl.isValid());
        }
    }

    static VDBMetaData dummyPreviewVdb() {
        VDBMetaData vdb = new VDBMetaData();
        vdb.setName(EditorService.PREVIEW_VDB);
        ModelMetaData m = new ModelMetaData();
        m.setName("dummy");
        vdb.addModel(m);
        m.setModelType(Type.VIRTUAL);
        m.addSourceMetadata("DDL", "create view v as select 1");
        return vdb;
    }

}
