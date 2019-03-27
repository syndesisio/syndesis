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
package io.syndesis.connector.sql;

import org.testcontainers.containers.JdbcDatabaseContainer;

public class DerbyContainer extends JdbcDatabaseContainer<DerbyContainer> {

    public DerbyContainer() {
        super("datagrip/derby-server:10.12");
    }

    @Override
    public String getDriverClassName() {
        return "org.apache.derby.jdbc.ClientDriver";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:derby://" + getContainerIpAddress() + ":" + getMappedPort(1527) + "/MyDbTest";
    }

    @Override
    public String getPassword() {
        return "derbyuser";
    }

    @Override
    public String getUsername() {
        return "derbyuser";
    }

    @Override
    protected String getTestQueryString() {
        return "VALUES (1)";
    }

}