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

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import io.syndesis.common.model.integration.Step;
import org.apache.camel.component.mock.MockEndpoint;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MongoDBConnectorChangeStreamConsumerTest extends MongoDBConnectorTestSupport {

    protected static MongodExecutable mongodExecutablePrimary;
    protected static MongodExecutable mongodExecutableSecondary;

    @Override
    protected List<Step> createSteps() {
        return fromMongoChangeStreamToMock("result", "io.syndesis.connector:connector-mongodb-consumer-changestream", DATABASE, COLLECTION);
    }

    /**
     * We need to create a cluster of at least 2 nodes to avoid exception on cluster shutdown
     * @throws Exception
     */
    @BeforeClass
    public static void startUpMongo() throws Exception {
        IMongodConfig mongodConfigPrimary = new MongodConfigBuilder()
            .version(Version.V3_6_5)
            .net(new Net(HOST, PORT, false))
            .replication(new Storage(null, "rs0", 5000))
            .build();
        mongodExecutablePrimary = MongodStarter.getDefaultInstance().prepare(mongodConfigPrimary);
        mongodExecutablePrimary.start();

        IMongodConfig mongodConfigSecondary = new MongodConfigBuilder()
            .version(Version.V3_6_5)
            .net(new Net(HOST, 27018, false))
            .replication(new Storage(null, "rs0", 5000))
            .build();
        mongodExecutableSecondary = MongodStarter.getDefaultInstance().prepare(mongodConfigSecondary);
        mongodExecutableSecondary.start();

        initCluster();
        initClient();
        //Create a collection needed by this test
        database.createCollection(COLLECTION);
    }

    private static void initCluster() {
        mongoClient = new MongoClient(HOST);
        // init replica set
        Document config = new Document("_id", "rs0");
        BasicDBList members = new BasicDBList();
        members.add(new Document("_id", 0)
            .append("host", HOST+":" + PORT));
        members.add(new Document("_id", 1)
            .append("host", HOST+":27018"));
        config.put("members", members);
        mongoClient.getDatabase("admin").runCommand(new Document("replSetInitiate", config));
        database = mongoClient.getDatabase(DATABASE);
        collection = database.getCollection(COLLECTION);
    }

    @AfterClass
    public static void tearDownMongo() {
        mongoClient.close();
        mongodExecutablePrimary.stop();
        mongodExecutableSecondary.stop();
    }

    @Test
    public void singleInsertTest() throws Exception {
        // When
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        // Given
        Document doc = new Document();
        doc.append("someKey", "someValue");
        doc.append("test", "junit");
        collection.insertOne(doc);
        Document doc2 = new Document();
        doc2.append("someKey", "someValue2");
        doc2.append("test", "junit2");
        collection.insertOne(doc2);
        // Then
        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, mock);
    }
}
