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

package io.syndesis.test.container.db;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @author Christoph Deppisch
 */
public class SyndesisDbContainer extends PostgreSQLContainer<SyndesisDbContainer> {

    public static final int DB_PORT = 5432;

    public SyndesisDbContainer() {
        withDatabaseName("sampledb");
        withUsername("sampledb");
        withPassword("secret");

        withCreateContainerCmdModifier(cmd -> cmd.withName("syndesis-db"));
        withCreateContainerCmdModifier(cmd -> cmd.withPortBindings(new PortBinding(Ports.Binding.bindPort(DB_PORT), new ExposedPort(DB_PORT))));
        withInitScript("syndesis-db-init.sql");

        withNetwork(Network.newNetwork());
        withNetworkAliases("syndesis-db");
    }
}
