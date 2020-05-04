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

package io.syndesis.dv.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import io.syndesis.dv.model.TablePrivileges;
import io.syndesis.dv.model.TablePrivileges.Privilege;
import io.syndesis.dv.model.ViewDefinition;

@SuppressWarnings("nls")
@RunWith(SpringRunner.class)
@DataJpaTest
public class TablePrivilegesTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositoryManagerImpl repositoryManagerImpl;

    @Test
    public void testCreateTablePrivileges() {
        repositoryManagerImpl.createDataVirtualization("x");

        assertFalse(repositoryManagerImpl.hasRoles("x"));

        assertTrue(repositoryManagerImpl.findRoleNames().isEmpty());

        ViewDefinition vd = repositoryManagerImpl.createViewDefiniton("x", "y");

        TablePrivileges tp = repositoryManagerImpl.createTablePrivileges(vd.getId(), "my-role");
        tp.getGrantPrivileges().add(Privilege.D);

        entityManager.flush();

        TablePrivileges tpDup = repositoryManagerImpl.createTablePrivileges(vd.getId(), "my-role");
        tpDup.getGrantPrivileges().add(Privilege.D);

        entityManager.flush();

        TablePrivileges tp2 = repositoryManagerImpl.createTablePrivileges(vd.getId(), "my-role1");
        tp.getGrantPrivileges().add(Privilege.D);

        assertTrue(repositoryManagerImpl.hasRoles("x"));

        assertEquals(2, repositoryManagerImpl.findRoleNames().size());

        assertNull(repositoryManagerImpl.findTablePrivileges(vd.getId(), "my-other-role"));

        List<TablePrivileges> tablePrivileges = repositoryManagerImpl.findAllTablePrivileges("x");
        assertEquals(tp.getGrantPrivileges(), tablePrivileges.get(0).getGrantPrivileges());

        repositoryManagerImpl.deleteDataVirtualization("x");

        assertTrue(repositoryManagerImpl.findRoleNames().isEmpty());
    }

}
