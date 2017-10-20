/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.syndesis.integration.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.SyndesisModelBuilder;
import io.syndesis.integration.model.steps.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableConfigurationProperties(SyndesisConfiguration.class)
public class SyndesisAutoConfiguration {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private SyndesisConfiguration configuration;
    @Autowired(required = false)
    private List<Step> steps = Collections.emptyList();
    @Autowired(required = false)
    private List<StepHandler<? extends Step>> stepsHandlers = Collections.emptyList();

    /**
     * To automatic add SyndesisRouteBuilder which loads the syndesis.yml file
     */
    @Bean
    @ConditionalOnMissingBean(SyndesisRouteBuilder.class)
    public SyndesisRouteBuilder syndesisRouteBuilder() throws IOException {
        final Resource resource = context.getResource(configuration.getConfiguration());

        try (InputStream is = resource.getInputStream()) {
            final SyndesisModel model = new SyndesisModelBuilder().addSteps(steps).build(is);

            return new SyndesisRouteBuilder(model, stepsHandlers);
        }
    }
}
