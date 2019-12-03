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
package io.syndesis.connector.mongo.embedded;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bson.Document;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.Slf4jStreamProcessor;
import de.flapdoodle.embed.process.io.directories.FixedPath;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;
import static de.flapdoodle.embed.mongo.distribution.Version.Main.V3_6;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;

public class EmbedMongoConfiguration {

    public final static int PORT = findAvailableTcpPort();

    public final static String HOST = "localhost";
    public final static String USER = "test-user";
    public final static String PASSWORD = "test-pwd";
    public final static String ADMIN_DB = "admin";
    // Client connections
    public final static MongoClient CLIENT;
    public final static MongoDatabase DATABASE;

    private EmbedMongoConfiguration(){}

    static {
        try {
            final Path storagePath = Files.createTempDirectory("embeddeddmongo");
            storagePath.toFile().deleteOnExit();
            IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(V3_6)
                    .net(new Net(PORT, localhostIsIPv6()))
                    .replication(new Storage(storagePath.toString(), "replicationName", 5000))
                    .build();

            final IStreamProcessor logDestination = new Slf4jStreamProcessor(LoggerFactory.getLogger("embeddeddmongo"), Slf4jLevel.INFO);
            final IStreamProcessor daemon = Processors.named("mongod", logDestination);
            final IStreamProcessor error = Processors.named("mongod-error", logDestination);
            final IStreamProcessor command = Processors.named("mongod-command", logDestination);
            final ProcessOutput processOutput = new ProcessOutput(daemon, error, command);
            final IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                    .defaults(Command.MongoD)
                    .extractDir(new FixedPath(".extracted"))
                    .download(new DownloadConfigBuilder()
                        .defaultsForCommand(Command.MongoD)
                        .artifactStorePath(new FixedPath(".cache"))
                        .build())
                    .build())
                .processOutput(processOutput)
                .build();
            MongodExecutable mongodExecutable = MongodStarter.getInstance(runtimeConfig)
                    .prepare(mongodConfig);
            mongodExecutable.start();
            // init replica set
            CLIENT = new MongoClient(HOST, PORT);
            CLIENT.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));
            createAuthorizationUser();
            DATABASE = CLIENT.getDatabase("test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createAuthorizationUser() {
        MongoDatabase admin = CLIENT.getDatabase("admin");
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
}
