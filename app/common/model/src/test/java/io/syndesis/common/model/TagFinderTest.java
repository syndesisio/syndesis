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
package io.syndesis.common.model;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Integration;
import org.junit.Assert;
import org.junit.Test;

public class TagFinderTest {

    @Test
    public void findTags() {
        Integration integration = new Integration.Builder()
            .addTag("tag1")
            .addTag("tag2")
            .build();
        Connection connection = new Connection.Builder()
            .addTag("tag2")
            .addTag("tag3")
            .build();
        ListResult<String> allTags = new TagFinder()
            .add(ListResult.of(integration))
            .add(ListResult.of(connection))
            .getResult();

        Assert.assertEquals( 3, allTags.getTotalCount());
        Assert.assertTrue(allTags.getItems().contains("tag1") );
        Assert.assertTrue(allTags.getItems().contains("tag2") );
        Assert.assertTrue(allTags.getItems().contains("tag3") );

    }

}
