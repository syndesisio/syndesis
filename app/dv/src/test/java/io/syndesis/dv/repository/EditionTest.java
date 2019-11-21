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

import java.nio.charset.Charset;

import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.model.Edition;

@SuppressWarnings("nls")
@RunWith(SpringRunner.class)
@DataJpaTest
public class EditionTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositoryManagerImpl repositoryManagerImpl;

    @Test
    public void testCreateEdition() {
        repositoryManagerImpl.createDataVirtualization("x");

        Edition e1 = repositoryManagerImpl.createEdition("x");
        assertEquals(1, e1.getRevision());

        Edition e2 = repositoryManagerImpl.createEdition("x");
        assertEquals(2, e2.getRevision());

        assertEquals(2, repositoryManagerImpl.findEditions("x").size());

        entityManager.flush();

        entityManager.detach(e1);

        e1 = repositoryManagerImpl.findEdition("x", 1);
        assertNotNull(e1);
    }

    //dv does not exist
    @Test(expected = PersistenceException.class)
    public void testCreateFails() {
        repositoryManagerImpl.createEdition("does not exist");
        entityManager.flush();
    }

    @Test
    public void testExportBytes() {
        repositoryManagerImpl.createDataVirtualization("x");

        Edition e1 = repositoryManagerImpl.createEdition("x");

        repositoryManagerImpl.saveEditionExport(e1, "hello world".getBytes(Charset.forName("UTF-8")));

        entityManager.flush();

        byte[] bytes = repositoryManagerImpl.findEditionExport(e1);

        assertEquals("hello world", new String(bytes, Charset.forName("UTF-8")));
    }
}
