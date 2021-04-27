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
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Test db schema migration to version 28. This migration will auto add split step to integrations with implicit split
 * configured.
 */
public class UpgradeVersion28Test {

    private static final int SCHEMA_VERSION = 28;
    private static final String INTEGRATIONS_PATH = "/integrations";

    private final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());

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

        JsonUtils.reader().readTree(integrations).fieldNames().forEachRemaining(integrationIds::add);

        Assertions.assertEquals(4, integrationIds.size());
        Integration integration = JsonUtils.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(0)));
        Flow flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.split, StepKind.log, StepKind.mapper, StepKind.endpoint);
        Assertions.assertTrue(flow.getSteps().get(0).getId().isPresent());
        Assertions.assertNotEquals("step-sql-start", flow.getSteps().get(0).getId().get());
        Assertions.assertEquals("step-sql-start", flow.getSteps().get(1).getId().orElseThrow(AssertionError::new));

        DataShape splitInputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getInputDataShape().orElseThrow(AssertionError::new);
        Assertions.assertEquals(DataShapeKinds.NONE, splitInputShape.getKind());
        Assertions.assertEquals("SQL_PARAM_IN", splitInputShape.getType());
        Assertions.assertNull(splitInputShape.getSpecification());

        DataShape splitOutputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape().orElseThrow(AssertionError::new);
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, splitOutputShape.getKind());
        Assertions.assertEquals("SQL_PARAM_OUT", splitOutputShape.getType());
        Assertions.assertNotNull(splitOutputShape.getSpecification());

        integration = JsonUtils.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(1)));
        flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.endpoint);
        Assertions.assertEquals("Simple Timer", flow.getSteps().get(0).getAction().orElseGet(UpgradeVersion28Test::dummyAction).getName());
        Assertions.assertEquals("Simple Logger", flow.getSteps().get(1).getAction().orElseGet(UpgradeVersion28Test::dummyAction).getName());

        integration = JsonUtils.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(2)));
        flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.split, StepKind.mapper, StepKind.endpoint);
        Assertions.assertNotEquals("step-service-now-start", flow.getSteps().get(0).getId().get());
        Assertions.assertEquals("step-service-now-start", flow.getSteps().get(1).getId().orElseThrow(AssertionError::new));

        splitInputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getInputDataShape().orElseThrow(AssertionError::new);
        Assertions.assertEquals(DataShapeKinds.NONE, splitInputShape.getKind());
        Assertions.assertNull(splitInputShape.getSpecification());

        splitOutputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape().orElseThrow(AssertionError::new);
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, splitOutputShape.getKind());
        Assertions.assertEquals("{\"type\":\"object\",\"$schema\":\"http://json-schema.org/schema#\",\"properties\":{\"ID\":{\"type\":\"integer\",\"required\":true}}}", splitOutputShape.getSpecification());

        integration = JsonUtils.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(3)));
        flow = integration.getFlows().get(0);
        assertStepsOnFlow(flow, StepKind.endpoint, StepKind.split, StepKind.log, StepKind.endpoint);
        Assertions.assertNotEquals("step-aws-s3-start", flow.getSteps().get(0).getId().get());
        Assertions.assertEquals("step-aws-s3-start", flow.getSteps().get(1).getId().orElseThrow(AssertionError::new));

        splitInputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getInputDataShape().orElseThrow(AssertionError::new);
        Assertions.assertEquals(DataShapeKinds.NONE, splitInputShape.getKind());
        Assertions.assertNull(splitInputShape.getSpecification());

        splitOutputShape = flow.getSteps().get(1).getActionAs(StepAction.class).orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape().orElseThrow(AssertionError::new);
        Assertions.assertEquals(DataShapeKinds.JAVA, splitOutputShape.getKind());
        Assertions.assertEquals("S3Object", splitOutputShape.getName());
        Assertions.assertEquals("java.io.InputStream", splitOutputShape.getType());
    }

    @Test
    public void testSchemaUpgradeMultipleTimes() throws IOException {
        JsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
        jsondb.push(INTEGRATIONS_PATH, new ClassPathResource("migrations/sql-integration.json").getInputStream());

        migrator.migrate(jsondb, SCHEMA_VERSION);
        migrator.migrate(jsondb, SCHEMA_VERSION);

        String integrations = jsondb.getAsString(INTEGRATIONS_PATH);
        List<String> integrationIds = new ArrayList<>();

        JsonUtils.reader().readTree(integrations).fieldNames().forEachRemaining(integrationIds::add);

        Assertions.assertEquals(1, integrationIds.size());
        Integration integration = JsonUtils.reader().forType(Integration.class).readValue(jsondb.getAsString(INTEGRATIONS_PATH + "/" + integrationIds.get(0)));
        Assertions.assertEquals(5, integration.getFlows().get(0).getSteps().size());
        Assertions.assertEquals(StepKind.split, integration.getFlows().get(0).getSteps().get(1).getStepKind());
        Assertions.assertEquals("step-sql-start", integration.getFlows().get(0).getSteps().get(1).getId().orElseThrow(AssertionError::new));
    }

    private static Action dummyAction() {
        return new StepAction.Builder().build();
    }

    private static void assertStepsOnFlow(Flow flow, StepKind ... steps) {
        Assertions.assertEquals(steps.length, flow.getSteps().size());

        for (int i = 0; i < steps.length; i++) {
            Assertions.assertEquals(steps[i], flow.getSteps().get(i).getStepKind());
        }
    }
}
