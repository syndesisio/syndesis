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
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class MongoDBConnectorTestSupport extends ConnectorTestSupport {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected final static String HOST = "localhost";
    protected final static int PORT = 27017;
    protected final static String USER = "test-user";
    protected final static String PASSWORD = "test-pwd";
    protected final static String DATABASE = "test";
    protected final static String COLLECTION = "test";
    protected static final String ADMIN_DB = "admin";
    // Client connections
    protected static MongoClient mongoClient;
    protected static MongoDatabase database;
    protected static MongoCollection<Document> collection;
    private static MongodExecutable mongodExecutable;

    @AfterClass
    public static void tearDownMongo() {
        mongodExecutable.stop();
        mongoClient.close();
    }

    @BeforeClass
    public static void startUpMongo() throws Exception {
        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.V3_6_5)
            .net(new Net(PORT, false))
            .replication(new Storage(null, "replicationName", 5000))
            .build();

        mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig);
        mongodExecutable.start();

        initClient();
    }

    private static void initClient() {
        mongoClient = new MongoClient(HOST);
        // init replica set
        mongoClient.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));
        createAuthorizationUser();
        database = mongoClient.getDatabase(DATABASE);
        collection = database.getCollection(COLLECTION);
    }

    private static void createAuthorizationUser() {
        MongoDatabase admin = mongoClient.getDatabase("admin");
        MongoCollection<Document> usersCollection = admin.getCollection("system.users");
        usersCollection.insertOne(Document.parse("" + "{\n"
            + "    \"_id\": \"admin.test-user\",\n"
            // + " \"userId\": Binary(\"rT2IgiexSzisOOsmjGXZEQ==\", 4),\n"
            + "    \"user\": \"test-user\",\n"
            + "    \"db\": \"admin\",\n"
            + "    \"credentials\": {\n"
            + "        \"SCRAM-SHA-1\": {\n"
            + "            \"iterationCount\": 10000,\n"
            + "            \"salt\": \"gmmPAoNdvFSWCV6PGnNcAw==\",\n"
            + "            \"storedKey\": \"qE9u1Ax7Y40hisNHL2b8/LAvG7s=\",\n"
            + "            \"serverKey\": \"RefeJcxClt9JbOP/VnrQ7YeQh6w=\"\n"
            + "        }\n"
            + "    },\n"
            + "    \"roles\": [\n"
            + "        {\n"
            + "            \"role\": \"readWrite\",\n"
            + "            \"db\": \"test\"\n"
            + "        }\n"
            + "    ]\n"
            + "}"
            + ""));
    }

    protected List<Step> fromDirectToMongo(String directStart, String connector, String db, String collection) {
        return fromDirectToMongo(directStart, connector, db, collection, null);
    }

    protected List<Step> fromDirectToMongo(String directStart, String connector, String db, String collection, String operation) {
        return Arrays.asList(
            newSimpleEndpointStep("direct", builder -> builder.putConfiguredProperty("name", directStart)),
            newEndpointStep("mongodb3", connector, nop(Connection.Builder.class), builder -> {
                builder.putConfiguredProperty("host", String.format("%s:%s", HOST, PORT));
                builder.putConfiguredProperty("user", USER);
                builder.putConfiguredProperty("password", PASSWORD);
                builder.putConfiguredProperty("adminDB", ADMIN_DB);
                builder.putConfiguredProperty("database", db);
                builder.putConfiguredProperty("collection", collection);
                if (operation != null) {
                    builder.putConfiguredProperty("operation", operation);
                }
            }));
    }

    protected List<Step> fromMongoTailToMock(String mock, String connector, String db, String collection,
                                         String tailTrackIncreasingField) {
        return this.fromMongoTailToMock(mock, connector, db, collection, tailTrackIncreasingField, null, null, null, null, null);
    }

    protected List<Step> fromMongoTailToMock(String mock, String connector, String db, String collection,
                                         String tailTrackIncreasingField, Boolean persistentTailTracking, String persistentId,
                                         String tailTrackDb, String tailTrackCollection, String tailTrackField) {
        return Arrays.asList(newEndpointStep("mongodb3", connector, nop(Connection.Builder.class), builder -> {
            builder.putConfiguredProperty("host", String.format("%s:%s", HOST, PORT));
            builder.putConfiguredProperty("user", USER);
            builder.putConfiguredProperty("password", PASSWORD);
            builder.putConfiguredProperty("adminDB", ADMIN_DB);
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
            builder.putConfiguredProperty("host", String.format("%s:%s", HOST, PORT));
            builder.putConfiguredProperty("user", USER);
            builder.putConfiguredProperty("password", PASSWORD);
            builder.putConfiguredProperty("adminDB", ADMIN_DB);
            builder.putConfiguredProperty("database", db);
            builder.putConfiguredProperty("collection", collection);
        }), newSimpleEndpointStep("mock", builder -> builder.putConfiguredProperty("name", mock)));
    }
}
