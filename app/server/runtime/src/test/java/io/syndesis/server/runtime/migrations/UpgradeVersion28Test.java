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

package io.syndesis.server.runtime.migrations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Test db schema migration to version 28. This migration will auto add split steps to integrations with implicit split
 * configured.
 *
 * @author Christoph Deppisch
 */
public class UpgradeVersion28Test {

    private static final int SCHEMA_VERSION = 28;
    private static final String INTEGRATIONS_PATH = "/integrations";

    private Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());

    @Test
    public void testSchemaUpgrade() throws IOException {
        JsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
        jsondb.push(INTEGRATIONS_PATH, new ClassPathResource("migrations/sql-integration.json").getInputStream());
        jsondb.push(INTEGRATIONS_PATH, new ClassPathResource("migrations/simple-timer-integration.json").getInputStream());
        jsondb.push(INTEGRATIONS_PATH, new ClassPathResource("migrations/servicenow-integration.json").getInputStream());
        jsondb.push(INTEGRATIONS_PATH, new ClassPathResource("migrations/aws-s3-integration.json").getInputStream());

        migrator.migrate(jsondb, SCHEMA_VERSION);

        String integrations = jsondb.getAsString(INTEGRATIONS_PATH);
        List<String> integrationIds = new ArrayList<>();

        Json.reader().readTree(integrations).fieldNames().forEachRemaining(integrationIds::add);

        Assert.assertEquals(4, integrationIds.size());
        Integration integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(0)));
        Assert.assertEquals(6, integration.getFlows().get(0).getSteps().size());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(0).getStepKind());
        Assert.assertEquals(StepKind.split, integration.getFlows().get(0).getSteps().get(1).getStepKind());
        Assert.assertEquals(StepKind.log, integration.getFlows().get(0).getSteps().get(2).getStepKind());
        Assert.assertEquals(StepKind.mapper, integration.getFlows().get(0).getSteps().get(3).getStepKind());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(4).getStepKind());
        Assert.assertEquals(StepKind.aggregate, integration.getFlows().get(0).getSteps().get(5).getStepKind());

        integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(1)));
        Assert.assertEquals(2, integration.getFlows().get(0).getSteps().size());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(0).getStepKind());
        Assert.assertEquals("Simple Timer", integration.getFlows().get(0).getSteps().get(0).getAction().orElseGet(UpgradeVersion28Test::dummyAction).getName());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(1).getStepKind());
        Assert.assertEquals("Simple Logger", integration.getFlows().get(0).getSteps().get(1).getAction().orElseGet(UpgradeVersion28Test::dummyAction).getName());

        integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(2)));
        Assert.assertEquals(5, integration.getFlows().get(0).getSteps().size());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(0).getStepKind());
        Assert.assertEquals(StepKind.split, integration.getFlows().get(0).getSteps().get(1).getStepKind());
        Assert.assertEquals(StepKind.mapper, integration.getFlows().get(0).getSteps().get(2).getStepKind());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(3).getStepKind());
        Assert.assertEquals(StepKind.aggregate, integration.getFlows().get(0).getSteps().get(4).getStepKind());

        integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(3)));
        Assert.assertEquals(5, integration.getFlows().get(0).getSteps().size());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(0).getStepKind());
        Assert.assertEquals(StepKind.split, integration.getFlows().get(0).getSteps().get(1).getStepKind());
        Assert.assertEquals(StepKind.log, integration.getFlows().get(0).getSteps().get(2).getStepKind());
        Assert.assertEquals(StepKind.endpoint, integration.getFlows().get(0).getSteps().get(3).getStepKind());
        Assert.assertEquals(StepKind.aggregate, integration.getFlows().get(0).getSteps().get(4).getStepKind());
    }

    @Test
    public void testSchemaUpgradeMultipleTimes() throws IOException {
        JsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
        jsondb.push(INTEGRATIONS_PATH, new ClassPathResource("migrations/sql-integration.json").getInputStream());

        migrator.migrate(jsondb, SCHEMA_VERSION);
        migrator.migrate(jsondb, SCHEMA_VERSION);

        String integrations = jsondb.getAsString(INTEGRATIONS_PATH);
        List<String> integrationIds = new ArrayList<>();

        Json.reader().readTree(integrations).fieldNames().forEachRemaining(integrationIds::add);

        Assert.assertEquals(1, integrationIds.size());
        Integration integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(0)));
        Assert.assertEquals(6, integration.getFlows().get(0).getSteps().size());
    }

    private static Action dummyAction() {
        return new StepAction.Builder().build();
    }
}
