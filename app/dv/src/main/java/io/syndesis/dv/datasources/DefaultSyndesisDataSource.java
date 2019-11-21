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

import java.util.Map;

import io.syndesis.dv.metadata.internal.TeiidDataSourceImpl;

public class DefaultSyndesisDataSource {
    private String syndesisConnectionId;
    private String syndesisName;
    private volatile String teiidName;
    private String translator;
    private Map<String, String> properties;
    private DataSourceDefinition definition;

    public String getSyndesisConnectionId() {
        return syndesisConnectionId;
    }

    public String getSyndesisName() {
        return syndesisName;
    }

    public String getType() {
        return definition.getType();
    }

    public void setId(String id) {
        this.syndesisConnectionId = id;
    }

    public void setSyndesisName(String syndesisName) {
        this.syndesisName = syndesisName;
    }

    public String getTranslatorName() {
        return translator;
    }

    public void setTranslatorName(String translator) {
        this.translator = translator;
    }

    public DataSourceDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(DataSourceDefinition definition) {
        this.definition = definition;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public TeiidDataSourceImpl createDataSource() {
        return this.definition.createDatasource(this.teiidName, this);
    }

    /**
     * If bound returns the unique Teiid datasource name, which is also a valid
     * schema name.  It will already be cleansed of problematic characters.
     * @return
     */
    public String getTeiidName() {
        return teiidName;
    }

    public void setTeiidName(String teiidName) {
        this.teiidName = teiidName;
    }
}
