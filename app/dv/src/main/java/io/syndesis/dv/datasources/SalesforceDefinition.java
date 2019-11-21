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
import org.teiid.spring.data.salesforce.SalesforceConfiguration;
import org.teiid.spring.data.salesforce.SalesforceConnectionFactory;

public class SalesforceDefinition extends DataSourceDefinition {

    @Override
    public String getType() {
        return "salesforce";
    }

    @Override
    public String getPomDendencies() {
        return
            "<dependency>" +
            "  <groupId>org.teiid</groupId>" +
            "  <artifactId>spring-data-salesforce</artifactId>" +
            "  <version>${version.springboot.teiid}</version>" +
            "</dependency>\n";
    }

    @Override
    public String getTranslatorName() {
        return "salesforce";
    }

    @Override
    public boolean isTypeOf(Map<String, String> properties, String type) {
        if (type.equals("salesforce")) {
            return true;
        }
        return false;
    }

    @Override
    public TeiidDataSourceImpl createDatasource(String deploymentName, DefaultSyndesisDataSource scd) {
        SalesforceConfiguration config = new SalesforceConfiguration();
        config.setClientId(scd.getProperty("clientId"));
        config.setClientSecret(scd.getProperty("clientSecret"));
        config.setRefreshToken(scd.getProperty("refreshToken"));

        SalesforceConnectionFactory scf = new SalesforceConnectionFactory(config);

        Map<String, String> importProperties = new HashMap<String, String>();
        Map<String, String> translatorProperties = new HashMap<String, String>();
        importProperties.put("includeExtensionMetadata", "true");

        TeiidDataSourceImpl teiidDS = new TeiidDataSourceImpl(scd.getSyndesisConnectionId(), deploymentName, getTranslatorName(), scf);
        teiidDS.setImportProperties(importProperties);
        teiidDS.setTranslatorProperties(translatorProperties);
        return teiidDS;
    }

    /**
     * Given the connection properties from the Syndesis secrets generate Spring Boot
     * configuration file to configure the data source
     * @return properties properties required to create a connection in target environment
     */
    @Override
    public Map<String, String> getPublishedImageDataSourceProperties(DefaultSyndesisDataSource scd) {
        Map<String, String> props = new HashMap<>();
        ds(props, scd, "client-id", scd.getProperty("clientId"));
        ds(props, scd, "client-secret", scd.getProperty("clientSecret"));
        ds(props, scd, "refresh-token", scd.getProperty("refreshToken"));
        return props;
    }

    @Override
    protected void ds(Map<String, String> props, DefaultSyndesisDataSource scd, String key, String value) {
        props.put("spring.teiid.data.salesforce." + scd.getTeiidName() + "." + key, value);
    }
}
