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

public class MongoConfiguration {
    String host = "localhost";
    String user;
    String password;
    int port = 27017;
    private String adminDB = "admin";

    public MongoConfiguration() {
        super();
    }

    public MongoConfiguration(Map<String, String> parameters) {
        super();
        this.host = parameters.get("host");
        this.user = parameters.get("user");
        this.password = parameters.get("password");
        // Optional parameters
        String optionalPort = parameters.getOrDefault("port", "");
        String optionalAdminDB = parameters.getOrDefault("adminDB", "");
        if (!"".equals(optionalPort)) {
            this.port = Integer.parseInt(optionalPort);
        }
        if (!"".equals(optionalAdminDB)) {
            this.adminDB = optionalAdminDB;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdminDB() {
        return adminDB;
    }

    public void setAdminDB(String adminDB) {
        this.adminDB = adminDB;
    }

    public String getMongoClientURI() {
        return String.format("mongodb://%s:%s@%s:%d/%s", this.user, this.password, this.host,
            this.port, this.adminDB);
    }

    @Override
    public String toString() {
        return "MongoConfiguration [host=" + host + ", user=" + user + ", password=***, port=" + port + ", adminDB="
            + adminDB + "]";
    }

}
