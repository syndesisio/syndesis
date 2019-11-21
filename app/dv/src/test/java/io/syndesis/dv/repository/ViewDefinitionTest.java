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

package io.syndesis.dv.repository;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.ViewDefinition;

@RunWith(SpringRunner.class)
@DataJpaTest
@SuppressWarnings("nls")
public class ViewDefinitionTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositoryManagerImpl repositoryManager;

    @Test
    public void testFindDeleteByName() throws Exception {
        DataVirtualization dv = repositoryManager.createDataVirtualization("name");

        ViewDefinition v = repositoryManager.createViewDefiniton(dv.getName(), "x");
        v.setDdl("create ...");

        entityManager.flush();

        ViewDefinition found = repositoryManager.findViewDefinition(v.getId());

        assertEquals(v.getDdl(), found.getDdl());

        repositoryManager.createViewDefiniton(dv.getName(), "y");

        repositoryManager.createViewDefiniton(dv.getName(), "x1").setComplete(true);

        assertNotNull(found.getCreatedAt());

        entityManager.flush();

        assertNotNull(found.getCreatedAt());

        assertEquals(3, repositoryManager.findViewDefinitions(dv.getName()).size());

        assertEquals(Arrays.asList("x", "y", "x1"), repositoryManager.findViewDefinitionsNames(dv.getName()));

        //x matching ignore case
        assertNotNull(repositoryManager.findViewDefinitionByNameIgnoreCase(dv.getName(), "X"));

        assertTrue(repositoryManager.deleteViewDefinition(v.getId()));

        assertFalse(repositoryManager.deleteViewDefinition(v.getId()));

        repositoryManager.createViewDefiniton(dv.getName(), v.getName());

        entityManager.flush();
    }

    @Test
    public void testState() {
        DataVirtualization dv = repositoryManager.createDataVirtualization("name");

        ViewDefinition v = repositoryManager.createViewDefiniton(dv.getName(), "existing");

        v.setDdl("create ...");
        v.addSourcePath("x");

        entityManager.flush();
        entityManager.detach(v);

        ViewDefinition found = repositoryManager.findViewDefinition(v.getId());
        assertEquals("create ...", found.getDdl());
        assertEquals(Arrays.asList("x"), found.getSourcePaths());
    }

    @Test
    public void testSameName() throws Exception {
        repositoryManager.createDataVirtualization("name");

        repositoryManager.createDataVirtualization("name1");

        repositoryManager.createViewDefiniton("name", "x");

        entityManager.flush();

        repositoryManager.createViewDefiniton("name1", "x");

        entityManager.flush();
    }

    @Test
    public void testDeleteByVirtualization() throws Exception {
        repositoryManager.createDataVirtualization("dv");
        repositoryManager.createViewDefiniton("dv", "x");
        repositoryManager.createViewDefiniton("dv", "y");

        repositoryManager.createDataVirtualization("dv1");
        repositoryManager.createViewDefiniton("dv1", "x");
        entityManager.flush();

        assertEquals(2, repositoryManager.deleteViewDefinitions("dv").intValue());
    }

}
