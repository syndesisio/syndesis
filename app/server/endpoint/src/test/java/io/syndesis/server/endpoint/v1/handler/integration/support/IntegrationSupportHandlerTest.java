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
package io.syndesis.server.endpoint.v1.handler.integration.support;

import io.syndesis.common.model.ModelExport;
import io.syndesis.common.util.json.JsonUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IntegrationSupportHandlerTest {

    @SuppressWarnings({ "PMD.UnusedLocalVariable"})
    @Test
    public void verifyJacksonBehaviorWithSourceStreams() throws IOException {
        // disabling feature inline, skipt closing source stream
        try (InputStream in = IntegrationSupportHandlerTest.class.getResourceAsStream("/model.json");
            InputStream fis = spy(in)) {
            ModelExport models = JsonUtils.reader().forType(ModelExport.class).readValue(fis);
            assertThat(models).isNotNull();
            verify(fis, times(0)).close();
        }
    }
}
