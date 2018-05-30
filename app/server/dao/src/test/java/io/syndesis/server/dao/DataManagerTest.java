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
package io.syndesis.server.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.util.cache.CacheManager;
import io.syndesis.common.util.cache.LRUCacheManager;
import io.syndesis.server.dao.manager.DataAccessObject;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;

public class DataManagerTest {
    private CacheManager cacheManager;
    private DataManager dataManager = null;

    @Before
    public void setup() {
        cacheManager = new LRUCacheManager(100);

        EncryptionComponent encryptionComponent = new EncryptionComponent(null);
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        //Create Data Manager
        dataManager = new DataManager(cacheManager, Collections.emptyList(), null, encryptionComponent, resourceLoader);
        dataManager.init();
        dataManager.resetDeploymentData();
    }

    @Test
    public void getConnectors() {
        @SuppressWarnings("unchecked")
        ListResult<Connector> connectors = dataManager.fetchAll(Connector.class);
        assertThat(connectors.getItems().stream().map(Connector::getId).map(Optional::get))
            .contains("activemq", "amqp", "ftp","sftp", "sql", "salesforce", "twitter", "aws-s3", "mqtt", "http4", "https4", "dropbox", "slack");
        Assert.assertTrue(connectors.getTotalCount() > 1);
        Assert.assertTrue(connectors.getItems().size() > 1);
        Assert.assertTrue(connectors.getTotalCount() >= connectors.getItems().size());
    }

    @Test
    public void getConnections() {
        ListResult<Connection> connections = dataManager.fetchAll(Connection.class);
        assertThat(connections.getItems().stream().map(Connection::getId).map(Optional::get)).containsOnly("5", "webhook");
        Assert.assertEquals(2, connections.getTotalCount());
        Assert.assertEquals(2, connections.getItems().size());
        Assert.assertEquals(connections.getTotalCount(), connections.getItems().size());
    }

    @Test
    public void getConnectorsWithFilterFunction() {
        @SuppressWarnings("unchecked")
        ListResult<Connector> connectors = dataManager.fetchAll(
            Connector.class,
            resultList -> new ListResult.Builder<Connector>()
                .createFrom(resultList)
                .items(resultList.getItems().stream()
                    .filter(connector -> connector.getId().get().equals("twitter") || connector.getId().get().equals("activemq"))
                    .collect(Collectors.toList()))
                .build()
        );

        assertThat(connectors.getItems().stream().map(Connector::getId).map(Optional::get)).containsExactlyInAnyOrder("twitter", "activemq");
        Assert.assertEquals(14, connectors.getTotalCount());
        Assert.assertEquals(2, connectors.getItems().size());
    }

    @Test
    public void getTwitterConnector() {
        Connector connector = dataManager.fetch(Connector.class, "twitter");
        Assert.assertEquals("First Connector in the deployment.json is Twitter", "Twitter", connector.getName());
        Assert.assertEquals(2, connector.getActions().size());
    }

    @Test
    public void getSalesforceConnector() {
        Connector connector = dataManager.fetch(Connector.class, "salesforce");
        Assert.assertEquals("Second Connector in the deployment.json is Salesforce", "Salesforce", connector.getName());
        Assert.assertEquals(10, connector.getActions().size());
    }

    @Test
    public void createShouldCreateWithUnspecifiedIds() {
        final Connector given = new Connector.Builder().version(1).icon("my-icon").name("my-name").build();
        final Connector got = dataManager.create(given);

        assertThat(got).isEqualToIgnoringGivenFields(given, "id");
        assertThat(got.getId()).isPresent();
        assertThat(cacheManager.getCache(Kind.Connector.modelName).get(got.getId().get())).isSameAs(got);
    }

    @Test
    public void createShouldCreateWithSpecifiedId() {
        final Connector connector = new Connector.Builder().version(1).id("custom-id").name("my-name").build();
        final Connector got = dataManager.create(connector);

        assertThat(got).isSameAs(connector);
        assertThat(cacheManager.getCache(Kind.Connector.modelName).get("custom-id")).isEqualTo(connector);
    }

    @Test
    public void shouldFetchIdsByPropertyValuePairs() {
        @SuppressWarnings("unchecked")
        final DataAccessObject<Connector> connectorDao = mock(DataAccessObject.class);
        when(connectorDao.getType()).thenReturn(Connector.class);
        dataManager.registerDataAccessObject(connectorDao);

        when(connectorDao.fetchIdsByPropertyValue("prop", "exists")).thenReturn(Collections.singleton("id"));
        when(connectorDao.fetchIdsByPropertyValue("prop", "not")).thenReturn(Collections.emptySet());

        assertThat(dataManager.fetchIdsByPropertyValue(Connector.class, "prop", "exists")).containsOnly("id");
        assertThat(dataManager.fetchIdsByPropertyValue(Connector.class, "prop", "not")).isEmpty();
    }

    @Test
    public void shouldFetchIdsByMultiplePropertyValuePairs() {
        @SuppressWarnings("unchecked")
        final DataAccessObject<Extension> extensionDao = mock(DataAccessObject.class);
        when(extensionDao.getType()).thenReturn(Extension.class);
        dataManager.registerDataAccessObject(extensionDao);

        when(extensionDao.fetchIdsByPropertyValue("prop1", "value1"))
            .thenReturn(new HashSet<>(Arrays.asList("1", "2", "3")));
        when(extensionDao.fetchIdsByPropertyValue("prop2", "value2"))
            .thenReturn(new HashSet<>(Arrays.asList("2", "3")));
        when(extensionDao.fetchIdsByPropertyValue("prop3", "value3"))
            .thenReturn(new HashSet<>(Arrays.asList("3", "4")));

        assertThat(dataManager.fetchIdsByPropertyValue(Extension.class, "prop1", "value1",
            "prop2", "value2", "prop3", "value3")).containsExactly("3");
    }

    @Test
    public void shouldUseShortCircuitWhenFetchingIdsByMultiplePropertyValuePairs() {
        @SuppressWarnings("unchecked")
        final DataAccessObject<Extension> extensionDao = mock(DataAccessObject.class);
        when(extensionDao.getType()).thenReturn(Extension.class);
        dataManager.registerDataAccessObject(extensionDao);

        when(extensionDao.fetchIdsByPropertyValue("prop1", "value1"))
            .thenReturn(Collections.emptySet());
        when(extensionDao.fetchIdsByPropertyValue("prop2", "value2"))
            .thenThrow(new RuntimeException());

        assertThat(dataManager.fetchIdsByPropertyValue(Extension.class, "prop1", "value1",
            "prop2", "value2")).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void multiplePropertyValuePairsShouldCheckInput() {
        @SuppressWarnings("unchecked")
        final DataAccessObject<Extension> extensionDao = mock(DataAccessObject.class);
        when(extensionDao.getType()).thenReturn(Extension.class);
        dataManager.registerDataAccessObject(extensionDao);

        dataManager.fetchIdsByPropertyValue(Extension.class, "prop1", "value1","prop2");
        fail("Should fail before getting here");
    }

}
