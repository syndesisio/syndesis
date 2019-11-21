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
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.sql.DataSource;

import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.internal.TeiidDataSourceImpl;
import org.springframework.boot.jdbc.DataSourceBuilder;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Service catalog based Data Services that are available
 */
public abstract class DataSourceDefinition {
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    /**
     * Returns the type of the database. Matches with the translator name
     * @return name of the source type
     */
    public abstract String getType();

    /**
     * @return return the text to include in pom.xml as dependencies for this source
     */
    public abstract String getPomDendencies();

    /**
     * Returns the matching translator type
     * @return translator name
     */
    public abstract String getTranslatorName();

    /**
     * Check to see if the properties given match to the Data Source definition, using which
     * connection can be made.
     * @param properties
     * @return true if the properties match
     */
    public boolean isTypeOf(Map<String, String> properties, String type) {
        return false;
    }

    /**
     * create data source for the given Syndesis Data source
     * @param deploymentName
     * @param scd
     * @return {@link TeiidDataSource}
     */
    public TeiidDataSourceImpl createDatasource(String deploymentName, DefaultSyndesisDataSource scd) {
        DataSource ds = DataSourceBuilder.create().url(scd.getProperty("url"))
                .username(scd.getProperty("username") != null ? scd.getProperty("username")
                        : scd.getProperty("user"))
                .password(scd.getProperty("password")).build();

        if (ds instanceof HikariDataSource) {
            ((HikariDataSource)ds).setMaximumPoolSize(10);
            ((HikariDataSource)ds).setMinimumIdle(0);
            ((HikariDataSource)ds).setIdleTimeout(60000);
            ((HikariDataSource)ds).setScheduledExecutor(executor);
        }
        Map<String, String> importProperties = new HashMap<String, String>();
        Map<String, String> translatorProperties = new HashMap<String, String>();

        if (scd.getProperty("schema") != null) {
            importProperties.put("importer.schemaName", scd.getProperty("schema"));
        }
        importProperties.put("importer.TableTypes", "TABLE,VIEW");
        importProperties.put("importer.UseQualifiedName", "true");
        importProperties.put("importer.UseCatalogName", "false");
        importProperties.put("importer.UseFullSchemaName", "false");

        TeiidDataSourceImpl teiidDS = new TeiidDataSourceImpl(scd.getSyndesisConnectionId(), deploymentName, getTranslatorName(), ds);
        teiidDS.setImportProperties(importProperties);
        teiidDS.setTranslatorProperties(translatorProperties);
        return teiidDS;
    }

    /**
     * Given the connection properties from the Syndesis secrets generate Spring Boot
     * configuration file to configure the data source
     * @return properties properties required to create a connection in target environment
     */
    public Map<String, String> getPublishedImageDataSourceProperties(DefaultSyndesisDataSource scd) {
        Map<String, String> props = new HashMap<>();
        ds(props, scd, "jdbc-url", scd.getProperty("url"));
        ds(props, scd, "username", scd.getProperty("user"));
        ds(props, scd, "password", scd.getProperty("password"));

        if (scd.getProperty("schema") != null) {
            ds(props, scd, "importer.schemaName", scd.getProperty("schema"));
        }

        // pool properties
        ds(props, scd, "maximumPoolSize", "5");
        ds(props, scd, "minimumIdle", "0");

        return props;
    }

    protected void ds(Map<String, String> props, DefaultSyndesisDataSource scd, String key, String value) {
        props.put("spring.datasource." + scd.getTeiidName() + "." + key, value);
    }
}
