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

import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.mongo.embedded.EmbedMongoConfiguration;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EmbedMongoConfiguration.class},
    loader = CamelSpringDelegatingTestContextLoader.class)
public abstract class MongoDBConnectorTestSupport extends ConnectorTestSupport {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected final static String DATABASE = "test";
    protected final static String COLLECTION = "test";

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
            newEndpointStep("mongodb", connector, nop(Connection.Builder.class), builder -> {
                builder.putConfiguredProperty("host", String.format("%s:%s", EmbedMongoConfiguration.HOST, EmbedMongoConfiguration.PORT));
                builder.putConfiguredProperty("user", EmbedMongoConfiguration.USER);
                builder.putConfiguredProperty("password", EmbedMongoConfiguration.PASSWORD);
                builder.putConfiguredProperty("adminDB", EmbedMongoConfiguration.ADMIN_DB);
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
        return Arrays.asList(newEndpointStep("mongodb", connector, nop(Connection.Builder.class), builder -> {
                builder.putConfiguredProperty("host", String.format("%s:%s", EmbedMongoConfiguration.HOST, EmbedMongoConfiguration.PORT));
                builder.putConfiguredProperty("user", EmbedMongoConfiguration.USER);
                builder.putConfiguredProperty("password", EmbedMongoConfiguration.PASSWORD);
                builder.putConfiguredProperty("adminDB", EmbedMongoConfiguration.ADMIN_DB);
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
        return Arrays.asList(newEndpointStep("mongodb", connector, nop(Connection.Builder.class), builder -> {
            builder.putConfiguredProperty("host", String.format("%s:%s", EmbedMongoConfiguration.HOST, EmbedMongoConfiguration.PORT));
            builder.putConfiguredProperty("user", EmbedMongoConfiguration.USER);
            builder.putConfiguredProperty("password", EmbedMongoConfiguration.PASSWORD);
            builder.putConfiguredProperty("adminDB", EmbedMongoConfiguration.ADMIN_DB);
            builder.putConfiguredProperty("database", db);
            builder.putConfiguredProperty("collection", collection);
        }), newSimpleEndpointStep("mock", builder -> builder.putConfiguredProperty("name", mock)));
    }
}
