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

package io.syndesis.test.integration.source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.Json;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class IntegrationExportSourceTest {

    @Test
    public void shouldGetFromZip() throws IOException {
        Integration expected = Json.readFromStream(IntegrationExportSourceTest.class.getResourceAsStream("TimerToLog.json"), Integration.class);
        IntegrationExportSource source = new IntegrationExportSource(IntegrationExportSourceTest.class.getResourceAsStream("TimerToLog-export.zip"));
        Assert.assertEquals(expected, source.get());
    }

    @Test
    public void shouldGetFromDirectory() throws IOException, URISyntaxException {
        Integration expected = Json.readFromStream(IntegrationExportSourceTest.class.getResourceAsStream("TimerToLog.json"), Integration.class);
        IntegrationExportSource source = new IntegrationExportSource(Paths.get(IntegrationExportSourceTest.class.getResource("TimerToLog-export").toURI()));
        Assert.assertEquals(expected, source.get());
    }
}
