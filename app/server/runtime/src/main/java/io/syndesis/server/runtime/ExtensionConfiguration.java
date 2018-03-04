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
package io.syndesis.server.runtime;


import io.syndesis.server.dao.file.FileDAO;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.dao.manager.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and configures the extension bits.
 */
@Configuration
public class ExtensionConfiguration {
    @Bean
    @Autowired
    public FileDataManager extensionDataManager(
            DataManager dataManager,
            FileDAO extensionDataAccess) {

        return new FileDataManager(dataManager, extensionDataAccess);
    }
}
