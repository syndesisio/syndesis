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
package io.syndesis.connector.mongo;

import io.syndesis.common.model.integration.Step;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.hamcrest.CoreMatchers.isA;

public class MongoDBConnectorNotSupportedSyndesisOperationTest extends MongoDBConnectorTestSupport {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    @Before
    public void setUp() throws Exception {
        expectedException.expectCause(isA(IllegalArgumentException.class));
        expectedException.expectMessage("Operation aggregate is not supported");
        super.setUp();
        fail("Setup should have thrown an exception!");
    }

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-producer", DATABASE,
            COLLECTION, "aggregate");
    }

    @Test
    public void mongoTest() {
        assertTrue(true);
    }

}
