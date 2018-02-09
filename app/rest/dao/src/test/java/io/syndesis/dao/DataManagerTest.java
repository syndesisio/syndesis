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
package io.syndesis.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import io.syndesis.core.cache.CacheManager;
import io.syndesis.core.cache.LRUCacheManager;
import io.syndesis.dao.manager.DataAccessObject;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

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
            .containsExactlyInAnyOrder("gmail", "activemq", "amqp", "github", "ftp", "facebook", "linkedin", "sql", "salesforce", "jms", "timer", "twitter", "day-trade", "servicenow", "aws-s3", "http", "trade-insight");
        Assert.assertTrue(connectors.getTotalCount() > 1);
        Assert.assertTrue(connectors.getItems().size() > 1);
        Assert.assertEquals(connectors.getTotalCount(), connectors.getItems().size());
    }

    @Test
    public void getConnections() {
        @SuppressWarnings("unchecked")
        ListResult<Connection> connections = dataManager.fetchAll(Connection.class);
        assertThat(connections.getItems().stream().map(Connection::getId).map(Optional::get)).containsExactlyInAnyOrder("1", "2", "3", "4", "5");
        Assert.assertEquals(5, connections.getTotalCount());
        Assert.assertEquals(5, connections.getItems().size());
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
                    .filter(connector -> connector.getId().get().equals("gmail") || connector.getId().get().equals("activemq"))
                    .collect(Collectors.toList()))
                .build()
        );

        assertThat(connectors.getItems().stream().map(Connector::getId).map(Optional::get)).containsExactlyInAnyOrder("gmail", "activemq");
        Assert.assertEquals(17, connectors.getTotalCount());
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
    public void getIntegration() throws IOException {
        Integration integration = dataManager.fetch(Integration.class, "1");
        Assert.assertEquals("Example Integration", "Twitter to Salesforce Example", integration.getName());
        Assert.assertEquals(4, integration.getSteps().size());
        Assert.assertTrue(integration.getTags().contains("example"));
    }

    @Test
    public void createShouldCreateWithUnspecifiedIds() {
        final Connector given = new Connector.Builder().icon("my-icon").build();
        final Connector got = dataManager.create(given);

        assertThat(got).isEqualToIgnoringGivenFields(given, "id");
        assertThat(got.getId()).isPresent();
        assertThat(cacheManager.getCache(Kind.Connector.modelName).get(got.getId().get())).isSameAs(got);
    }

    @Test
    public void createShouldCreateWithSpecifiedId() {
        final Connector connector = new Connector.Builder().id("custom-id").build();
        final Connector got = dataManager.create(connector);

        assertThat(got).isSameAs(connector);
        assertThat(cacheManager.getCache(Kind.Connector.modelName).get("custom-id")).isSameAs(connector);
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

    @Test
    public void metricsTest() {
        ListResult<IntegrationMetricsSummary> list = dataManager.fetchAll(IntegrationMetricsSummary.class);
        assertThat(list.getTotalCount()).isEqualTo(2);
    }

    @Test
    public void getMetricsIdsTest() {
        Set<String> metricsIds = dataManager.fetchIds(IntegrationMetricsSummary.class);
        assertThat(metricsIds.size()).isEqualTo(2);
        assertThat(metricsIds).contains("1");
    }

}
