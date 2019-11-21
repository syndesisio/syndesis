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
package io.syndesis.dv.metadata.internal;

import java.util.Map;

import org.teiid.core.util.ArgCheck;
import org.teiid.runtime.EmbeddedServer.ConnectionFactoryProvider;

import io.syndesis.dv.metadata.TeiidDataSource;

public class TeiidDataSourceImpl implements Comparable<TeiidDataSourceImpl>, TeiidDataSource, ConnectionFactoryProvider<Object> {
    private final String name;
    private final String translatorName;
    private Object dataSource;
    private String id;
    private Map<String, String> importProperties;
    private Map<String, String> translatorProperties;

    public TeiidDataSourceImpl(String id, String name, String translatorName, Object dataSource) {
        ArgCheck.isNotEmpty(name, "name"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(translatorName, "translatorName"); //$NON-NLS-1$

        this.id = id;
        this.name = name;
        this.translatorName = translatorName;
        this.dataSource = dataSource;
    }

    @Override
    public int compareTo(TeiidDataSourceImpl other) {
        return getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof TeiidDataSource) {
            TeiidDataSource tds = (TeiidDataSource) obj;
            if (getName().equals(tds.getName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTranslatorName() {
        return this.translatorName;
    }

    @Override
    public int hashCode() {
        int result = 0;
        final int prime = 31;
        result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Data Source:\t" + getName()); //$NON-NLS-1$
        if (!getTranslatorName().equalsIgnoreCase("<unknown>")) { //$NON-NLS-1$
            sb.append("\nType: \t\t" + getTranslatorName()); //$NON-NLS-1$
        }

        return sb.toString();
    }

    @Override
    public String getSyndesisId() {
        return this.id;
    }

    @Override
    public Object getConnectionFactory() {
        return this.dataSource;
    }

    @Override
    public Map<String, String> getImportProperties() {
        return importProperties;
    }

    public void setImportProperties(Map<String, String> importProperties) {
        this.importProperties = importProperties;
    }

    @Override
    public Map<String, String> getTranslatorProperties() {
        return translatorProperties;
    }

    public void setTranslatorProperties(Map<String, String> translatorProperties) {
        this.translatorProperties = translatorProperties;
    }
}
