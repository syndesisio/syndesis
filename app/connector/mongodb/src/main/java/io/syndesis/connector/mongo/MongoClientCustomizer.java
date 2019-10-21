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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public class MongoClientCustomizer implements ComponentProxyCustomizer, CamelContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientCustomizer.class);
    private static final String JSON_COUNT_RESULT = "{\"count\": %d}";

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
        // We ensure to convert input/ouput to json text
        component.setBeforeConsumer(this::convertToJsonConsumer);
        component.setAfterProducer(this::convertToJsonProducer);
        // Set connection parameter
        if (!options.containsKey("mongoConnection")) {
            if (options.containsKey("user") && options.containsKey("password") && options.containsKey("host")) {
                try {
                    MongoConfiguration mongoConf = new MongoConfiguration();
                    consumeOption(camelContext, options, "host", String.class, mongoConf::setHost);
                    consumeOption(camelContext, options, "user", String.class, mongoConf::setUser);
                    consumeOption(camelContext, options, "password", String.class, mongoConf::setPassword);
                    // Use default connection db if an admin db has not specified explicitly
                    String adminDB = ConnectorOptions.extractOption(options, "adminDB");
                    String defaultDB = ConnectorOptions.extractOption(options, "database");
                    if (adminDB == null) {
                        // database parameter is mandatory, we must have it
                        LOGGER.info("Using connection database {} as administration database", defaultDB);
                        adminDB = defaultDB;
                    }
                    mongoConf.setAdminDB(adminDB);
                    LOGGER.debug("Creating and registering a client connection to {}", mongoConf);
                    MongoClientURI mongoClientURI = new MongoClientURI(mongoConf.getMongoClientURI());
                    MongoClient mongoClient = new MongoClient(mongoClientURI);
                    options.put("mongoConnection", mongoClient);
                    if (!options.containsKey("connectionBean")) {
                        //We safely put a default name instead of leaving null
                        options.put("connectionBean", String.format("%s-%s", mongoConf.getHost(), mongoConf.getUser()));
                    }
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                LOGGER.warn(
                    "Not enough information provided to set-up the MongoDB client. Required at least host, user and password.");
            }
        }
    }

    public void convertToJsonProducer(Exchange exchange) {
        List<String> result = new ArrayList<>();
        Message in = exchange.getIn();
        if (in.getBody() instanceof Document) {
            String singleDoc = in.getBody(Document.class).toJson();
            result.add(singleDoc);
        } else if (in.getBody() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Document> list = in.getBody(List.class);
            List<String> convertedToJson = list.stream().map(Document::toJson).collect(toList());
            result.addAll(convertedToJson);
        } else if (in.getBody() instanceof DeleteResult) {
            String jsonResult = String.format(JSON_COUNT_RESULT, in.getBody(DeleteResult.class).getDeletedCount());
            result.add(jsonResult);
        } else if (in.getBody() instanceof UpdateResult) {
            String jsonResult = String.format(JSON_COUNT_RESULT, in.getBody(UpdateResult.class).getModifiedCount());
            result.add(jsonResult);
        } else if (in.getBody() instanceof Long) {
            String jsonResult = String.format(JSON_COUNT_RESULT, in.getBody(Long.class));
            result.add(jsonResult);
        } else {
            LOGGER.warn("Impossible to convert the body, type was {}", in.getBody() == null? null : in.getBody().getClass());
            // return without setting the converted result
            return;
        }
        in.setBody(result);
    }

    public void convertToJsonConsumer(Exchange exchange) {
        List<String> convertedToJson = new ArrayList<>();
        Message in = exchange.getIn();
        if (in.getBody() instanceof Document) {
            convertedToJson.add(in.getBody(Document.class).toJson());
        } else if (in.getBody() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Document> list = in.getBody(List.class);
            convertedToJson = list.stream().map(Document::toJson).collect(toList());
        } else {
            LOGGER.warn("Impossible to convert the body, type was {}", in.getBody().getClass());
            // return without setting the converted result
            return;
        }
        in.setBody(convertedToJson);
    }
}
