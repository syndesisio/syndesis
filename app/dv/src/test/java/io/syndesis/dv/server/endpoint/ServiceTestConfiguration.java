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

package io.syndesis.dv.server.endpoint;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.teiid.runtime.EmbeddedConfiguration;

import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.metadata.internal.TeiidServer;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;

@ComponentScan(basePackageClasses = {DataVirtualizationService.class, DefaultMetadataInstance.class})
@TestConfiguration
public class ServiceTestConfiguration {

    @MockBean
    @SuppressWarnings("UnusedVariable")
    private TeiidOpenShiftClient teiidOpenShiftClient;

    @Bean
    public TeiidServer teiidServer() {
        EmbeddedConfiguration ec = new EmbeddedConfiguration();
        TeiidServer server = new TeiidServer();
        server.start(ec);
        return server;
    }

    @MockBean(name="connectionExecutor")
    @SuppressWarnings("UnusedVariable")
    private ScheduledThreadPoolExecutor connectionExecutor;

}