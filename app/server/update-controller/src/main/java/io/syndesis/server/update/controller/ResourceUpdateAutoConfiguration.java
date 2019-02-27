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
package io.syndesis.server.update.controller;

import java.util.Arrays;

import javax.validation.Validator;

import io.syndesis.common.util.EventBus;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.update.controller.bulletin.ConnectionUpdateHandler;
import io.syndesis.server.update.controller.bulletin.IntegrationUpdateHandler;
import io.syndesis.server.update.controller.usage.UsageUpdateHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnProperty(prefix = ResourceUpdateConstants.CONFIGURATION_PREFIX, value = "enabled")
@EnableConfigurationProperties(ResourceUpdateConfiguration.class)
public class ResourceUpdateAutoConfiguration {
    @Autowired
    private ResourceUpdateConfiguration configuration;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EncryptionComponent encryptionComponent;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private Validator validator;

    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(initMethod = "start", destroyMethod = "stop")
    public ResourceUpdateController resourceUpdateController() {
        return new ResourceUpdateController(
            configuration,
            eventBus,
            Arrays.<ResourceUpdateHandler>asList(
                new ConnectionUpdateHandler(dataManager, encryptionComponent, validator),
                new IntegrationUpdateHandler(dataManager, encryptionComponent, validator),
                new UsageUpdateHandler(dataManager)));
    }
}
