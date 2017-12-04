/**
 * Copyright (C) 2016 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.runtime;


import io.syndesis.dao.extension.ExtensionDataAccessObject;
import io.syndesis.dao.extension.ExtensionDataManager;
import io.syndesis.dao.manager.DataManager;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and configures the extension bits.
 */
@Configuration
public class ExtensionConfiguration {

    @Bean
    @Conditional(ExtensionDataManagerCondition.class)
    public ExtensionDataManager extensionDataManager(
            DataManager dataManager,
            ExtensionDataAccessObject extensionDataAccess) {

        return  new ExtensionDataManager(dataManager, extensionDataAccess);
    }

    // Required as ConditionalOnBean uses as OR operation in Spring Boot 1.x
    // but it will use AND in Spring 2.x so this class can be replaced by a simple
    // ConditionalOnBean once migrated to 2.x
    public static class ExtensionDataManagerCondition extends AllNestedConditions {
        public ExtensionDataManagerCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(DataManager.class)
        static class OnDataManager {
        }

        @ConditionalOnBean(ExtensionDataAccessObject.class)
        static class OnExtensionDataAccessObject {
        }
    }
}
