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
package io.syndesis.integration.runtime.sb;


import io.syndesis.integration.runtime.sb.jmx.IntegrationMetadataAutoConfiguration;
import io.syndesis.integration.runtime.sb.logging.IntegrationLoggingAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
public class ApplicationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testApplicationSetup() {
        assertThat(applicationContext.getBeansOfType(IntegrationRuntimeAutoConfiguration.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(IntegrationMetadataAutoConfiguration.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(IntegrationLoggingAutoConfiguration.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(SyndesisHttpConfiguration.class)).hasSize(1);
    }
}
