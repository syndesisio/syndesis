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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectorTest {

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
        //create Connector
        Connector connector = new Connector();
        connector.setName("name");
        connector.setTags("tag1,tag2");
        Set<ConnectorProperty> connectorProperties = new HashSet<ConnectorProperty>();
        ConnectorProperty connectorProperty = new ConnectorProperty();
        connectorProperty.setName("myConnectorProperty");
        connectorProperties.add(connectorProperty);
        connector.setConnectorProperties(connectorProperties);
        em.persist(connector);

        //fetch Connector
        Connector c = em.find(Connector.class, connector.getId());
        assertNotNull(c);
        assertEquals(connector.getId(), c.getId());
        assertNotNull(c.getConnectorProperties());

        //update Connector by changing a property, and the name and adding an action
        connectorProperty.setRequired(true);
        connectorProperties.clear();
        connectorProperties.add(connectorProperty);
        connector.setName("updatedConnector");

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
        //action.setConnector(connector);
        em.persist(action);
        Set<Action> actions = new HashSet<Action>();
        actions.add(action);

        connector.setActions(actions);

        em.persist(connector);
        em.flush();

        //fetch the Connector
        Connector updatedConnector = em.find(Connector.class, connector.getId());
        assertEquals("updatedConnector", updatedConnector.getName());
        ConnectorProperty connectorProperty2 = updatedConnector.getConnectorProperties().iterator().next();
        assertEquals(true,connectorProperty2.isRequired());

        ConnectorProperty cP = em.find(ConnectorProperty.class, connectorProperty2.getId());
        assertNotNull(cP);

        //fetch all connectors
        List<Connector> resultList = em.createQuery("SELECT p FROM Connector p", Connector.class).getResultList();
		assertEquals(1, resultList.size());

        //fetch all properties
        List<ConnectorProperty> resultList1 = em.createQuery("SELECT p FROM ConnectorProperty p", ConnectorProperty.class).getResultList();
		assertEquals(1, resultList1.size());

        //delete the Connector
        em.remove(updatedConnector);
        em.getTransaction().commit();

        //make sure the Connector is gone
        Connector deletedConnector = em.find(Connector.class, connector.getId());
        assertNull(deletedConnector);

        //also make sure the properties got deleted (cascade delete on properties)
        CriteriaQuery<ConnectorProperty> criteria2 = em.getCriteriaBuilder().createQuery(ConnectorProperty.class);
		criteria2.select(criteria2.from(ConnectorProperty.class));
		List<ConnectorProperty> resultList2 = em.createQuery(criteria2).getResultList();
		assertEquals(0, resultList2.size());
	}
}
