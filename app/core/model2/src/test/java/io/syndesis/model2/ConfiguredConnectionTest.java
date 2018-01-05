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

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfiguredConnectionTest {

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
    public void testCreateFetchAndDeleteConfiguredConnector() {
        assertNotNull(em);

        //create connector
        Connector connector = new Connector();
        connector.setName("name");
        connector.setTags("tag1,tag2");
        Set<ConnectorProperty> connectorProperties = new HashSet<ConnectorProperty>();
        ConnectorProperty connectorProperty = new ConnectorProperty();
        connectorProperty.setName("myProperty");
        connectorProperties.add(connectorProperty);
        connector.setConnectorProperties(connectorProperties);
        em.persist(connector);

        // create ConfiguredConnector
        ConfiguredConnector configuredConnector = new ConfiguredConnector();
        configuredConnector.setConnector(connector);
        em.persist(configuredConnector);

        //fetch ConfiguredConnector
        ConfiguredConnector c = em.find(ConfiguredConnector.class, configuredConnector.getId());
        assertNotNull(c);

        //delete ConfiguredConnector
        em.remove(c);

        //make sure ConfiguredConnector is gone
        ConfiguredConnector deletedConfiguredConnector = em.find(ConfiguredConnector.class, configuredConnector.getId());
        assertNull(deletedConfiguredConnector);
        //make sure the connector is still there
        Connector c2 = em.find(Connector.class, connector.getId());
        assertNotNull(c2);
	}
}
