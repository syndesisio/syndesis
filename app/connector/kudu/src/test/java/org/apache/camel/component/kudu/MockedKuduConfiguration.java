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

package org.apache.camel.component.kudu;

import org.apache.kudu.client.KuduClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;

public class MockedKuduConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MockedKuduConfiguration.class);

    @Bean
    public KuduClient kuduClientBean() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new instance of a mocked influx db connection");
        }

        KuduClient mockedDbConnection = mock(KuduClient.class);

        assertNotNull(mockedDbConnection);
        return mockedDbConnection;
    }
}
