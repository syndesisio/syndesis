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

    import io.syndesis.integration.runtime.api.SyndesisExtensionRoute;
    import org.apache.camel.CamelContext;
    import org.apache.camel.model.RouteDefinition;
    import org.apache.camel.spring.boot.CamelAutoConfiguration;
    import org.assertj.core.api.Assertions;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.test.annotation.DirtiesContext;
    import org.springframework.test.context.junit4.SpringRunner;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
        "spring.main.banner-mode=off"
    },
    classes = {
        CamelAutoConfiguration.class,
        SyndesisAutoConfiguration.class,
        SyndesisExtensionCollectorAutoConfiguration.class,
        SyndesisExtensionCollectorAutoConfigurationTest.TestConfiguration.class
    }
)
public class SyndesisExtensionCollectorAutoConfigurationTest {
    @Autowired
    private CamelContext context;

    @Test
    public void test() {
        Assertions.assertThat(context.getRoute("extension-one")).isNotNull();
        Assertions.assertThat(context.getRoute("extension-two")).isNotNull();
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public RouteDefinition one() {
            return SyndesisExtensionRoute.from("direct:one")
                .routeId("extension-one")
                .log("one");
        }

        @Bean
        public RouteDefinition two() {
            return SyndesisExtensionRoute.from("direct:two")
                .routeId("extension-two")
                .log("two");
        }
    }
}
