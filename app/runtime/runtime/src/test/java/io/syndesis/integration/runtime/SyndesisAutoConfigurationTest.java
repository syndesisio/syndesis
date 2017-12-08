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
package io.syndesis.integration.runtime;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
        "spring.main.banner-mode=off",
        "syndesis.configuration=classpath:io/syndesis/integration/runtime/syndesis-spring.yml"
    },
    classes = {
        CamelAutoConfiguration.class,
        SyndesisAutoConfiguration.class
    }
)
public class SyndesisAutoConfigurationTest {
    @Autowired
    private CamelContext context;
    @Autowired
    private SyndesisRouteBuilder builder;

    @Test
    public void test() {
        Assertions.assertThat(builder).isNotNull();
        Assertions.assertThat(context.getRoutes().size()).isEqualTo(1);
        Assertions.assertThat(context.getEndpoint("direct:start")).isNotNull();
        Assertions.assertThat(context.getEndpoint("mock:result")).isNotNull();
    }
}
