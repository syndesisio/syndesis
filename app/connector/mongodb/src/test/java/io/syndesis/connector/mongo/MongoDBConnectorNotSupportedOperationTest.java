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

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.syndesis.common.model.integration.Step;

@SuppressWarnings({ "PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert" })
public class MongoDBConnectorNotSupportedOperationTest extends MongoDBConnectorTestSupport {

    // **************************
    // Set up
    // **************************

    @Override
    @Before
    public void setUp() {
        try {
            super.setUp();
            fail("Setup should have thrown an exception!");
        } catch (Exception e) {
            // We do expect a failure cause the operation provided is not
            // supported
        }
    }

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-producer", DATABASE,
                COLLECTION, "somethingNotSupported");
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void mongoTest() {
        assertTrue(true);
    }

}
