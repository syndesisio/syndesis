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

package io.syndesis.connector.kudu;

import io.syndesis.connector.support.verifier.api.MetadataRetrieval;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnProperty(prefix = "io.syndesis.connector.meta", name = "enabled")
public class KuduMetaDataAutoConfiguration {
    @Bean("kudu-adapter")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Lazy
    @ConditionalOnProperty(prefix = "io.syndesis.connector.kudu.meta", name = "enabled", matchIfMissing = true)
    public MetadataRetrieval kuduMetaDataAdapter() {
        return new KuduMetadataRetrieval();
    }
}
