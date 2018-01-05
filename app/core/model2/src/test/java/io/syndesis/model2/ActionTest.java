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
package io.syndesis.model2;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ActionTest {

	private static EntityManager em;

	@BeforeClass
	public static void setUp() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("io.syndesis.runtime.db");
        em = factory.createEntityManager();
	}

	@AfterClass
	public static void tearDown() {
		em.close();
	}

	@Test
    public void testCreateFetchAndUpdateConnector() {
        assertNotNull(em);

        em.getTransaction().begin();
        Action action = new Action();
        action.setName("myAction");
        action.setCamelConnectorGav("org.foo_twitter-mention_1.0");
        action.setDescription("TwitterMention");

        ActionProperty actionProperty = new ActionProperty();
        actionProperty.setName("myActionProperty");
        actionProperty.setSecret(false);
        Set<ActionProperty> actionProperties = new HashSet<ActionProperty>();
        actionProperties.add(actionProperty);
        action.setActionProperties(actionProperties);
        em.persist(action);
        em.getTransaction().commit();
	}
}
