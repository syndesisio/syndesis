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
package io.syndesis.connector.mongo;

import java.util.Map;

import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.component.mongodb3.conf.ConnectionParamsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.camel.util.CastUtils.cast;

public class MongoClientCustomizer implements ComponentProxyCustomizer, CamelContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientCustomizer.class);

    private CamelContext camelContext;

    @Override
    public CamelContext getCamelContext() {
        return this.camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        MongoCustomizersUtil.replaceAdminDBIfMissing(options);
        // Set connection parameter
        if (!options.containsKey("mongoConnection")) {
            if (options.containsKey("user") && options.containsKey("password") && options.containsKey("host")) {
                ConnectionParamsConfiguration mongoConf = new ConnectionParamsConfiguration(cast(options));
                // We need to force consumption in order to perform property placeholder done by Camel
                consumeOption(camelContext, options, "password", String.class, mongoConf::setPassword);
                LOGGER.debug("Creating and registering a client connection to {}", mongoConf);
                MongoClientURI mongoClientURI = new MongoClientURI(mongoConf.getMongoClientURI());
                MongoClient mongoClient = MongoClients.create(mongoClientURI.toString());
                options.put("mongoConnection", mongoClient);
                if (!options.containsKey("connectionBean")) {
                    //We safely put a default name instead of leaving null
                    options.put("connectionBean", String.format("%s-%s", mongoConf.getHost(), mongoConf.getUser()));
                }
            } else {
                LOGGER.warn(
                    "Not enough information provided to set-up the MongoDB client. Required at least host, user and password.");
            }
        }
    }
}
