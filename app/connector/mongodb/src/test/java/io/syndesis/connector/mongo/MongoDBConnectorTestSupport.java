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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.support.test.ConnectorTestSupport;

import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class MongoDBConnectorTestSupport extends ConnectorTestSupport {

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, String> configuration;

    public MongoDBConnectorTestSupport(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    protected List<Step> fromDirectToMongo(String directStart, String connector, String db, String collection) {
        return fromDirectToMongo(directStart, connector, db, collection, null, null);
    }

    protected List<Step> fromDirectToMongo(String directStart, String connector, String db, String collection, String operation, String filter) {
        return this.fromDirectToMongo(directStart, connector, db, collection, operation, filter, null);
    }

    protected List<Step> fromDirectToMongo(String directStart, String connector, String db, String collection, String operation,
                                           String filter, String updateExession) {
        return Arrays.asList(
            newSimpleEndpointStep("direct", builder -> builder.putConfiguredProperty("name", directStart)),
            newEndpointStep("mongodb3", connector, nop(Connection.Builder.class), builder -> {
                builder.putAllConfiguredProperties(configuration);
                builder.putConfiguredProperty("database", db);
                builder.putConfiguredProperty("collection", collection);
                if (filter != null) {
                    builder.putConfiguredProperty("filter", filter);
                }
                if (updateExession != null) {
                    builder.putConfiguredProperty("updateExpression", updateExession);
                }
                if (operation != null) {
                    builder.putConfiguredProperty("operation", operation);
                }
            }));
    }

    protected List<Step> fromMongoTailToMock(String mock, String connector, String db, String collection,
                                             String tailTrackIncreasingField) {
        return this.fromMongoTailToMock(mock, connector, db, collection, tailTrackIncreasingField, true, null, null, null, null);
    }

    protected List<Step> fromMongoTailToMock(String mock, String connector, String db, String collection,
                                             String tailTrackIncreasingField, Boolean persistentTailTracking, String persistentId,
                                             String tailTrackDb, String tailTrackCollection, String tailTrackField) {
        return Arrays.asList(newEndpointStep("mongodb3", connector, nop(Connection.Builder.class), builder -> {
            builder.putAllConfiguredProperties(configuration);
            builder.putConfiguredProperty("database", db);
            builder.putConfiguredProperty("collection", collection);
            builder.putConfiguredProperty("tailTrackIncreasingField", tailTrackIncreasingField);
            if (persistentTailTracking != null) {
                builder.putConfiguredProperty("persistentTailTracking", persistentTailTracking.toString());
            }
            if (persistentId != null) {
                builder.putConfiguredProperty("persistentId", persistentId);
            }
            if (tailTrackDb != null) {
                builder.putConfiguredProperty("tailTrackDb", tailTrackDb);
            }
            if (tailTrackCollection != null) {
                builder.putConfiguredProperty("tailTrackCollection", tailTrackCollection);
            }
            if (tailTrackField != null) {
                builder.putConfiguredProperty("tailTrackField", tailTrackField);
            }
        }), newSimpleEndpointStep("mock", builder -> builder.putConfiguredProperty("name", mock)));
    }

    protected List<Step> fromMongoChangeStreamToMock(String mock, String connector, String db, String collection) {
        return Arrays.asList(newEndpointStep("mongodb3", connector, nop(Connection.Builder.class), builder -> {
            builder.putAllConfiguredProperties(configuration);
            builder.putConfiguredProperty("database", db);
            builder.putConfiguredProperty("collection", collection);
        }), newSimpleEndpointStep("mock", builder -> builder.putConfiguredProperty("name", mock)));
    }

    protected static ClosableMongoCollection<Document> createCollection(final MongoClient client, final String collectionName) {
        final MongoDatabase database = client.getDatabase("test");
        database.createCollection(collectionName);

        final MongoCollection<Document> collection = database.getCollection(collectionName);

        @SuppressWarnings({"unchecked", "rawtypes"})
        final ClosableMongoCollection<Document> thing = (ClosableMongoCollection<Document>) Proxy.newProxyInstance(ClosableMongoCollection.class.getClassLoader(), new Class[] { ClosableMongoCollection.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    collection.drop();
                    return null;
                }

                return method.invoke(collection, args);
            }
        });

        return thing;
    }
}
