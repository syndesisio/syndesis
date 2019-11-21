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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.model.SourceSchema;

@SuppressWarnings("nls")
@RunWith(SpringRunner.class)
@DataJpaTest
public class SourceSchemaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositoryManagerImpl workspaceManagerImpl;

    @Test
    public void testFindDeleteByName() {
        SourceSchema s = workspaceManagerImpl.createSchema("foo", "bar", "create ...");
        entityManager.flush();

        SourceSchema found = workspaceManagerImpl.findSchemaBySourceId(s.getSourceId());
        assertEquals(s.getDdl(), found.getDdl());

        try {
            workspaceManagerImpl.createSchema("foo", "bar", "create ...");
            workspaceManagerImpl.flush();
            fail();
        } catch (DataIntegrityViolationException e) {
        }

        entityManager.clear();

        try {
            workspaceManagerImpl.createSchema("foo", "bar1", "create ...");
            workspaceManagerImpl.flush();
            fail();
        } catch (DataIntegrityViolationException e) {
        }

        entityManager.clear();

        try {
            workspaceManagerImpl.createSchema("foo1", "baR", "create ...");
            workspaceManagerImpl.flush();
            fail();
        } catch (DataIntegrityViolationException e) {
        }

        entityManager.clear();

        assertTrue(workspaceManagerImpl.deleteSchemaBySourceId(s.getSourceId()));

        assertNull(workspaceManagerImpl.findSchemaBySourceId(s.getSourceId()));

        assertFalse(workspaceManagerImpl.deleteSchemaBySourceId(s.getSourceId()));

        entityManager.flush();
    }

    @Test
    public void testAllNames() {
        workspaceManagerImpl.createSchema("foo", "bar", "create ...");
        workspaceManagerImpl.createDataVirtualization("x");

        assertTrue(workspaceManagerImpl.isNameInUse("bar"));
        assertTrue(workspaceManagerImpl.isNameInUse("BAR"));
        assertTrue(workspaceManagerImpl.isNameInUse("x"));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testNameConflict() {
        workspaceManagerImpl.createSchema("foo", "x", "create ...");
        workspaceManagerImpl.createDataVirtualization("x");
        workspaceManagerImpl.flush();
    }

}
