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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.repository.RepositoryConfiguration;
import io.syndesis.dv.repository.RepositoryManagerImpl;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.adminapi.Model;
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

    @Test public void testStash() {

        repositoryManager.createDataVirtualization("x");

        ViewDefinition vd = new ViewDefinition("x", "y");

        ViewDefinition saved = utilService.upsertViewEditorState(vd);

        entityManager.flush();

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getModifiedAt());
        assertEquals(Long.valueOf(0), saved.getVersion());
        assertNotNull(saved.getId());

        assertThatThrownBy(() -> {
            ViewDefinition incorrect = new ViewDefinition("x", "y");
            incorrect.setId("not correct");
            utilService.upsertViewEditorState(incorrect);
        }).isInstanceOf(ResponseStatusException.class);

        //add a dummy preview vdb
        VDBMetaData vdb = dummyPreviewVdb(true);
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

        utilService.upsertViewEditorState(vd);
        //v is hidden, can't be unqualified
        ViewListing listing = dvService.getViewListing(vd.getDataVirtualizationName(), vd.getName());
        assertFalse(listing.isValid());

        vd.setDdl("create view y as select * from dummy.v");
        saved = utilService.upsertViewEditorState(vd);

        //the save does not determine the source paths
        assertEquals(Arrays.asList(), saved.getSourcePaths());

        for (ViewListing vl : dvService.getViewList("x")) {
            assertTrue(vl.isValid());
        }

        //invalid
        vd.setDdl("create view y as * from dummy.v");
        vd.setComplete(false);
        saved = utilService.upsertViewEditorState(vd);
        assertFalse(saved.isParsable());

        //still invalid
        vd.setComplete(true);
        saved = utilService.upsertViewEditorState(vd);
        assertFalse(saved.isParsable());

        //still invalid - parse error after main ddl
        vd.setDdl("create view y as select * from dummy.v abc 123");
        saved = utilService.upsertViewEditorState(vd);
        assertFalse(saved.isParsable());
    }

    static VDBMetaData dummyPreviewVdb(boolean hidden) {
        VDBMetaData vdb = new VDBMetaData();
        vdb.setName(EditorService.PREVIEW_VDB);
        ModelMetaData m = new ModelMetaData();
        m.setName("dummy");
        vdb.addModel(m);
        m.setModelType(Model.Type.VIRTUAL);
        m.addSourceMetadata("DDL", "create view v as select 1");
        if (hidden) {
            m.setVisible(false);
        }
        return vdb;
    }

}
