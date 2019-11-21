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
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.model.DataVirtualization;

@SuppressWarnings("nls")
@RunWith(SpringRunner.class)
@DataJpaTest
public class DataVirtualizationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataVirtualizationRepository dataVirtualizationRepository;

    @Autowired
    private RepositoryManagerImpl workspaceManagerImpl;

    @Test
    public void testFindDeleteByName() {
        DataVirtualization dv = workspaceManagerImpl.createDataVirtualization("foo");

        entityManager.flush();

        dv.setModifiedAt(null);

        entityManager.flush();

        assertNotNull(dv.getModifiedAt());
        assertEquals(Long.valueOf(1), dv.getVersion());

        DataVirtualization found = dataVirtualizationRepository.findByName(dv.getName());

        assertTrue(workspaceManagerImpl.isNameInUse(dv.getName()));

        assertNotNull(found.getId());

        assertEquals(dv.getName(), found.getName());

        assertTrue(workspaceManagerImpl.deleteDataVirtualization(dv.getName()));

        entityManager.flush();
    }

    @Test public void testGetAllNames() {
        workspaceManagerImpl.createDataVirtualization("foo");
        workspaceManagerImpl.createDataVirtualization("bar");

        assertEquals(2, workspaceManagerImpl.findDataVirtualizationNames().size());
    }

    @Test
    public void testGetBySourceId() {
        DataVirtualization dv = workspaceManagerImpl.createDataVirtualization("foo");
        dv.setSourceId("bar");
        entityManager.flush();

        assertEquals("foo", workspaceManagerImpl.findDataVirtualizationBySourceId("bar").getName());
    }
}
