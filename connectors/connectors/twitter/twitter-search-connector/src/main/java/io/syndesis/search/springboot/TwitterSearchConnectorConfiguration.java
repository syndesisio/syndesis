/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.search.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "twitter-search-connector")
public class TwitterSearchConnectorConfiguration
        extends
            TwitterSearchConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, TwitterSearchConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, TwitterSearchConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}