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
package io.syndesis.server.metrics.jsondb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.type.TypeReference;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.cache.CacheManager;
import io.syndesis.common.util.cache.LRUCacheManager;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.impl.Index;
import io.syndesis.server.jsondb.impl.SqlJsonDB;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;

/**
 * Unit Tests for Metrics Collector.
 */
public class MetricsCollectorTest {

    private SqlJsonDB jsondb;
    private DataManager dataManager;
    private JsonDBRawMetrics jsondbRM;
    private IntegrationMetricsHandler intMH;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    @Before
    public void before() throws IOException, ParseException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        DBI dbi = new DBI(ds);

        this.jsondb = new SqlJsonDB(dbi, null,
            Arrays.asList(new Index("/pair", "key"))
        );

        try {
            this.jsondb.dropTables();
        } catch (Exception e) {
        }
        this.jsondb.createTables();

        jsondbRM = new JsonDBRawMetrics(jsondb);

        load();

        CacheManager cacheManager = new LRUCacheManager(100);
        EncryptionComponent encryptionComponent = new EncryptionComponent(null);
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        //Create Data Manager
        dataManager = new DataManager(cacheManager, Collections.emptyList(), null, encryptionComponent, resourceLoader, null);
        intMH = new IntegrationMetricsHandler(dataManager);
    }

    private void load() throws IOException, ParseException {
        jsondbRM.persist(raw("intId1","1","pod1",3L, "31-01-2018 10:20:56"));
        jsondbRM.persist(raw("intId1","1","pod2",3L, "31-01-2018 10:22:56"));
        jsondbRM.persist(raw("intId1","1","HISTORY1",3L, "22-01-2015 10:20:56"));
        jsondbRM.persist(raw("intId2","1","pod3",3L, "31-01-2018 10:20:56"));
        jsondbRM.persist(raw("intId3","1","pod4",3L, "31-01-2018 10:20:56"));
        jsondbRM.persist(raw("intId3","1","pod5",3L, "31-01-2018 10:20:56"));
    }

    private RawMetrics raw(String integrationId, String version, String podName, Long messages, String startDateString) throws ParseException { //NOPMD
        Date startDate = sdf.parse(startDateString);
        return new RawMetrics.Builder()
                .integrationId(integrationId)
                .version(version)
                .pod(podName)
                .messages(messages)
                .errors(1L)
                .startDate(startDate)
                .resetDate(Optional.empty())
                .lastProcessed(new Date())
                .build();
    }

    @Test
    public void testGetMetricsForIntegration1() throws IOException {
        String json = jsondb.getAsString(JsonDBRawMetrics.path("intId1"), new GetOptions().prettyPrint(true));
        Map<String,RawMetrics> metrics = Json.reader().forType(new TypeReference<Map<String,RawMetrics>>() {}).readValue(json);
        assertThat(metrics.size()).isEqualTo(3);
        assertThat(metrics.keySet()).contains("HISTORY1");
    }

    @Test
    public void testGetRawMetrics() throws IOException {
        MetricsCollector collector = new MetricsCollector(null, jsondb, null);
        Map<String,RawMetrics> metrics = jsondbRM.getRawMetrics("intId1");
        assertThat(metrics.size()).isEqualTo(3);
        assertThat(metrics.keySet()).contains("HISTORY1");

        //let's kill pod2, this so add pod2's metrics to the history
        Set<String> livePodIds = new HashSet<>(Arrays.asList("pod1"));
        jsondbRM.curate("intId1", metrics, livePodIds);
        Map<String,RawMetrics> metrics2 = jsondbRM.getRawMetrics("intId1");
        assertThat(metrics2.size()).isEqualTo(2);
        assertThat(metrics2.keySet()).contains("HISTORY1");

        collector.close();
    }

    @Test
    public void testGetIntegrationSummary() throws IOException, ParseException {
        String integrationId = "intId1";
        Set<String> livePodIds = new HashSet<String>(
                Arrays.asList("pod1", "pod2", "pod3", "pod4", "pod5"));

        MetricsCollector collector = new MetricsCollector(null, jsondb, null);
        Map<String,RawMetrics> metrics = jsondbRM.getRawMetrics(integrationId);
        IntegrationMetricsSummary summary = intMH
                .compute(integrationId, metrics, livePodIds);

        assertThat(summary.getMessages()).isEqualTo(9);
        assertThat(summary.getErrors()).isEqualTo(3);
        //Oldest living pod
        assertThat(summary.getStart().get()).isEqualTo(sdf.parse("31-01-2018 10:20:56"));

        //Update pod2, add 6 messages
        jsondb.update(JsonDBRawMetrics.path("intId1","pod2"), Json.writer().writeValueAsString(raw("intId1","2","pod2",9L,"31-01-2018 10:22:56")));
        Map<String,RawMetrics> updatedMetrics = jsondbRM.getRawMetrics(integrationId);
        IntegrationMetricsSummary updatedSummary = intMH
                .compute(integrationId, updatedMetrics, livePodIds);
        assertThat(updatedSummary.getMessages()).isEqualTo(15);
        assertThat(updatedSummary.getErrors()).isEqualTo(3);

        collector.close();
    }

    @Test
    public void testDeadPodCurator() throws IOException, ParseException {
        String integrationId = "intId1";
        MetricsCollector collector = new MetricsCollector(null, jsondb, null);
        //Update pod1 metrics and kill pod1
        Set<String> livePodIds = new HashSet<String>(
            Arrays.asList("pod2", "pod3", "pod4", "pod5"));
        jsondb.update(JsonDBRawMetrics.path("intId1","pod1"), Json.writer().writeValueAsString(raw("intId1","1","pod1",12L,"31-01-2018 10:22:56")));
        Map<String,RawMetrics> metrics = jsondbRM.getRawMetrics(integrationId);
        IntegrationMetricsSummary summary = intMH
                .compute(integrationId, metrics, livePodIds);
        assertThat(summary.getMessages()).isEqualTo(18);
        assertThat(summary.getErrors()).isEqualTo(3);
        //Oldest living pod is now pod2
        assertThat(summary.getStart().get()).isEqualTo(sdf.parse("31-01-2018 10:22:56"));

        collector.close();
    }

    @Test
    public void testDeletedIntegrationsCurator() throws IOException, ParseException {

        String integrationId = "intId1";
        Set<String> livePodIds = new HashSet<String>(
                Arrays.asList("pod1", "pod2"));
        Map<String,RawMetrics> metrics = jsondbRM.getRawMetrics(integrationId);
        IntegrationMetricsSummary summary = intMH.compute(integrationId, metrics, livePodIds);
        dataManager.create(summary);

        assertThat(metrics.size()).isEqualTo(3);
        assertThat(dataManager.fetchAll(IntegrationMetricsSummary.class).getTotalCount()).isEqualTo(1);

        //Now pretend to delete the integration itself and
        //run the curator with no active integrations
        jsondbRM.curate(new HashSet<String>());
        intMH.curate(new HashSet<String>());

        //expect all metrics to be deleted
        Map<String,RawMetrics> metricsAfter = jsondbRM.getRawMetrics(integrationId);
        assertThat(metricsAfter.size()).isEqualTo(0);
        assertThat(dataManager.fetchAll(IntegrationMetricsSummary.class).getTotalCount()).isEqualTo(0);
    }
}
