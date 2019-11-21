/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.datasources;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.dv.metadata.internal.TeiidDataSourceImpl;
import org.teiid.spring.data.mongodb.MongoDBConfiguration;
import org.teiid.spring.data.mongodb.MongoDBConnectionFactory;
import org.teiid.spring.data.mongodb.MongoDBTemplate;

public class MongoDBDefinition extends DataSourceDefinition {

    @Override
    public String getType() {
        return "mongodb";
    }

    @Override
    public String getPomDendencies() {
        return
                "<dependency>" +
                "  <groupId>org.teiid</groupId>" +
                "  <artifactId>spring-data-mongodb</artifactId>" +
                "  <version>${version.springboot.teiid}</version>" +
                "</dependency>\n";
    }

    @Override
    public String getTranslatorName() {
        return "mongodb";
    }

    @Override
    public boolean isTypeOf(Map<String, String> properties, String type) {
        if (type.equalsIgnoreCase("mongodb3")) {
            return true;
        }
        return false;
    }

    @Override
    public TeiidDataSourceImpl createDatasource(String deploymentName, DefaultSyndesisDataSource scd) {
        MongoDBConfiguration config = new MongoDBConfiguration();
        config.setUser(scd.getProperty("user"));
        config.setPassword(scd.getProperty("password"));
        config.setAuthDatabase(scd.getProperty("adminDB"));
        config.setDatabase(scd.getProperty("database"));
        config.setRemoteServerList(scd.getProperty("host"));

        MongoDBConnectionFactory mcf = new MongoDBConnectionFactory(new MongoDBTemplate(config));

        Map<String, String> importProperties = new HashMap<String, String>();
        Map<String, String> translatorProperties = new HashMap<String, String>();
        TeiidDataSourceImpl teiidDS = new TeiidDataSourceImpl(scd.getSyndesisConnectionId(), deploymentName, getTranslatorName(), mcf);
        teiidDS.setImportProperties(importProperties);
        teiidDS.setTranslatorProperties(translatorProperties);
        return teiidDS;
    }

    @Override
    public Map<String, String> getPublishedImageDataSourceProperties(DefaultSyndesisDataSource scd) {
        Map<String, String> props = new HashMap<>();
        ds(props, scd, "user", scd.getProperty("user"));
        ds(props, scd, "password", scd.getProperty("password"));
        ds(props, scd, "database", scd.getProperty("database"));
        ds(props, scd, "authDatabase", scd.getProperty("adminDB"));
        ds(props, scd, "remoteServerList", scd.getProperty("host"));
        return props;
    }

    @Override
    protected void ds(Map<String, String> props, DefaultSyndesisDataSource scd, String key, String value) {
        props.put("spring.teiid.data.mongodb." + scd.getTeiidName() + "." + key, value);
    }
}
