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

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.Flow;
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
 * Test db schema migration to version 28. This migration will auto add split step to integrations with implicit split
 * configured.
 *
 * @author Christoph Deppisch
 */
public class UpgradeVersion28Test {

    private static final int SCHEMA_VERSION = 28;
    private static final String INTEGRATIONS_PATH = "/integrations";

    private Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
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
        Flow flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.split, StepKind.log, StepKind.mapper, StepKind.endpoint);
        Assert.assertTrue(flow.getSteps().get(0).getId().isPresent());
        Assert.assertNotEquals("step-sql-start", flow.getSteps().get(0).getId().get());
        Assert.assertEquals("step-sql-start", flow.getSteps().get(1).getId().orElseThrow(AssertionError::new));

        DataShape splitInputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getInputDataShape().orElseThrow(AssertionError::new);
        Assert.assertEquals(DataShapeKinds.NONE, splitInputShape.getKind());
        Assert.assertEquals("SQL_PARAM_IN", splitInputShape.getType());
        Assert.assertNull(splitInputShape.getSpecification());

        DataShape splitOutputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape().orElseThrow(AssertionError::new);
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, splitOutputShape.getKind());
        Assert.assertEquals("SQL_PARAM_OUT", splitOutputShape.getType());
        Assert.assertNotNull(splitOutputShape.getSpecification());

        integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(1)));
        flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.endpoint);
        Assert.assertEquals("Simple Timer", flow.getSteps().get(0).getAction().orElseGet(UpgradeVersion28Test::dummyAction).getName());
        Assert.assertEquals("Simple Logger", flow.getSteps().get(1).getAction().orElseGet(UpgradeVersion28Test::dummyAction).getName());

        integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(2)));
        flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.split, StepKind.mapper, StepKind.endpoint);
        Assert.assertNotEquals("step-service-now-start", flow.getSteps().get(0).getId().get());
        Assert.assertEquals("step-service-now-start", flow.getSteps().get(1).getId().orElseThrow(AssertionError::new));

        splitInputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getInputDataShape().orElseThrow(AssertionError::new);
        Assert.assertEquals(DataShapeKinds.NONE, splitInputShape.getKind());
        Assert.assertNull(splitInputShape.getSpecification());

        splitOutputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape().orElseThrow(AssertionError::new);
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, splitOutputShape.getKind());
        Assert.assertEquals("{\"type\":\"object\",\"$schema\":\"http://json-schema.org/schema#\",\"properties\":{\"ID\":{\"type\":\"integer\",\"required\":true}}}", splitOutputShape.getSpecification());

        integration = Json.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(3)));
        flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.split, StepKind.log, StepKind.endpoint);
        Assert.assertNotEquals("step-aws-s3-start", flow.getSteps().get(0).getId().get());
        Assert.assertEquals("step-aws-s3-start", flow.getSteps().get(1).getId().orElseThrow(AssertionError::new));

        splitInputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getInputDataShape().orElseThrow(AssertionError::new);
        Assert.assertEquals(DataShapeKinds.NONE, splitInputShape.getKind());
        Assert.assertNull(splitInputShape.getSpecification());

        splitOutputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape().orElseThrow(AssertionError::new);
        Assert.assertEquals(DataShapeKinds.JAVA, splitOutputShape.getKind());
        Assert.assertEquals("S3Object", splitOutputShape.getName());
        Assert.assertEquals("java.io.InputStream", splitOutputShape.getType());
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
        Assert.assertEquals(5, integration.getFlows().get(0).getSteps().size());
        Assert.assertEquals(StepKind.split, integration.getFlows().get(0).getSteps().get(1).getStepKind());
        Assert.assertEquals("step-sql-start", integration.getFlows().get(0).getSteps().get(1).getId().orElseThrow(AssertionError::new));
    }

    private static Action dummyAction() {
        return new StepAction.Builder().build();
    }

    private void assertStepsOnFlow(Flow flow, StepKind ... steps) {
        Assert.assertEquals(steps.length, flow.getSteps().size());

        for (int i = 0; i < steps.length; i++) {
            Assert.assertEquals(steps[i], flow.getSteps().get(i).getStepKind());
        }
    }
}
