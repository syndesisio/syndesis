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
import io.syndesis.common.model.integration.Step;
import org.apache.camel.FailedToStartRouteException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class MongoDBConnectorMissingDatabaseOptionsTest extends MongoDBConnectorProducerTestSupport {

    private final static String COLLECTION = "missingDBCollection";

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    @Before
    public void setUp()  {
        Assertions.assertThatExceptionOfType(FailedToStartRouteException.class).isThrownBy(super::setUp);
    }

    @Override
    protected List<Step> createSteps() {
        return fromDirectToMongo("start", "io.syndesis.connector:connector-mongodb-find", "",
            COLLECTION);
    }

    @Test
    public void mongoTest() {
        // Just need to verify that the route is not created in the setup phase
        assertTrue(true);
    }

}
