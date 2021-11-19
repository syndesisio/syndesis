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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoNotPrimaryException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.DirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.TempNaming;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.Slf4jStreamProcessor;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;

public class EmbeddedMongoExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

    private static final Namespace NAMESPACE = Namespace.create(EmbeddedMongoExtension.class);

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(EmbeddedMongoExtension.class)
    public @interface Mongo {
        // marker
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MongoConfiguration {
        // marker
    }

    static class State {
        final ServerAddress address;

        final File dataDirectory;

        final MongodExecutable node;

        public State(final MongodExecutable node, final File dataDirectory, final ServerAddress address) {
            this.node = node;
            this.dataDirectory = dataDirectory;
            this.address = address;
        }
    }

    static class CustomTempNaming implements TempNaming {
        @Override
        public synchronized String nameFor(final String prefix, final String suffix) {
            try {
                final Path tempFile = Files.createTempFile(prefix, suffix);
                tempFile.toFile().delete();
                return tempFile.toString();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) throws IOException {
        final Store store = context.getStore(NAMESPACE);

        final State state = store.get("state", State.class);
        if (state != null) {
            destroy(state.node, state.dataDirectory);
        }
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws IOException {
        if (!context.getRequiredTestClass().isAnnotationPresent(Mongo.class)) {
            return;
        }

        final Store store = context.getStore(NAMESPACE);

        synchronized(NAMESPACE) {
            store.getOrComputeIfAbsent("state", key -> startMongo(), State.class);
        }
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Class<?> type = parameterContext.getParameter().getType();
        final State state = extensionContext.getStore(NAMESPACE).get("state", State.class);
        final ServerAddress server = state.address;

        if (MongoClient.class.equals(type)) {
            return new MongoClient(server);
        }

        if (Map.class.equals(type)) {
            final Map<String, String> configuration = new HashMap<>();
            configuration.put("host", server.getHost() + ":" + server.getPort());
            configuration.put("user", "test-user");
            configuration.put("password", "test-pwd");
            configuration.put("adminDB", "admin");

            return configuration;
        }

        throw new ParameterResolutionException("Don't know how to resolve this parameter");
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Class<?> type = parameterContext.getParameter().getType();

        return MongoClient.class.equals(type) || Map.class.isAssignableFrom(type);
    }

    static MongodConfig createEmbeddedMongoConfiguration() {
        try {
            final Path storagePath = Files.createTempDirectory("embeddeddmongo");
            return MongodConfig.builder()
                .version(Version.Main.V3_6)
                .replication(new Storage(storagePath.toString(), "rs0", 0))
                .build();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void destroy(final MongodExecutable node, final File dataDirectory) throws IOException {
        node.stop();
        node.getFile().baseDir().delete();

        FileUtils.deleteDirectory(dataDirectory);
    }

    private static void createAuthorizationUser(final MongoClient client) {
        final MongoDatabase admin = client.getDatabase("admin");
        final MongoCollection<Document> usersCollection = admin.getCollection("system.users");
        usersCollection.insertOne(Document.parse("{\n"
            + "    \"_id\": \"admin.test-user\",\n"
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
            + "}"));
    }

    private static State startMongo() {
        final StreamProcessor logDestination = new Slf4jStreamProcessor(LoggerFactory.getLogger("embeddeddmongo"), Slf4jLevel.INFO);
        final StreamProcessor daemon = Processors.named("mongod", logDestination);
        final StreamProcessor error = Processors.named("mongod-error", logDestination);
        final StreamProcessor command = Processors.named("mongod-command", logDestination);
        final ProcessOutput processOutput = ProcessOutput.builder()
            .commands(command)
            .error(error)
            .output(daemon)
            .build();
        final RuntimeConfig runtimeConfig = RuntimeConfig.builder()
            .artifactStore(Defaults.extractedArtifactStoreFor(Command.MongoD)
                .withTemp(DirectoryAndExecutableNaming.builder()
                    .directory(new PropertyOrPlatformTempDir())
                    .executableNaming(new CustomTempNaming())
                    .build()))
            .processOutput(processOutput)
            .build();

        final MongodConfig node1Config = createEmbeddedMongoConfiguration();

        final MongodExecutable node = MongodStarter.getInstance(runtimeConfig)
            .prepare(node1Config);
        try {
            node.start();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        final Net net = node1Config.net();
        InetAddress host;
        try {
            host = net.getServerAddress();
        } catch (final UnknownHostException e) {
            throw new UncheckedIOException(e);
        }

        final int node1Port = net.getPort();
        final ServerAddress server = new ServerAddress(host, node1Port);

        try (MongoClient client = new MongoClient(server)) {
            final MongoDatabase admin = client.getDatabase("admin");
            admin.runCommand(new Document("replSetInitiate", new Document()));
            final Map<String, Object> getStatus = new HashMap<>();
            getStatus.put("replSetGetStatus", 1);
            getStatus.put("initialSync", 1);

            Awaitility.await().atMost(Duration.of(3, ChronoUnit.SECONDS)).until(() -> {
                final Document status = admin.runCommand(new Document(getStatus));
                @SuppressWarnings("unchecked")
                final List<Document> members = status.get("members", List.class);
                final String state = members.get(0).get("stateStr", String.class);

                if ("PRIMARY".equals(state)) {
                    // we wait until the server is primary repolica
                    return true;
                }

                return false;
            });
        }

        try (MongoClient client = new MongoClient(server)) {
            // sometimes it's not enough for the replication state to claim the
            // server is a primary replica, and adding a document to a
            // collection fails with `10107 (NotMaster)` so we retry until we
            // succeed
            Awaitility.await().atMost(Duration.of(3, ChronoUnit.SECONDS)).until(() -> {
                try {
                    createAuthorizationUser(client);
                    return true;
                } catch (final MongoNotPrimaryException ignored) {
                    return false;
                }
            });
        }

        final File dataDirectory = new File(node1Config.replication().getDatabaseDir());

        return new State(node, dataDirectory, server);
    }

}
