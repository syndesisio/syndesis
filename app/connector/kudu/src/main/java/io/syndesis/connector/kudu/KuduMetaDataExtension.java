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

package io.syndesis.connector.kudu;

import io.syndesis.connector.kudu.meta.KuduMetaData;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;
import org.apache.camel.util.ObjectHelper;

import java.util.Map;
import java.util.Optional;

public class KuduMetaDataExtension extends AbstractMetaDataExtension {
    private static final MetaData EMPTY_METADATA = new DefaultMetaData(null, null, null);

    public KuduMetaDataExtension(CamelContext camelContext) {
        super(camelContext);
    }

    @Override
    public Optional<MetaData> meta(final Map<String, Object> properties) {
        final String tableName = Optional.ofNullable(properties.get("tableName"))
                .map(Object::toString)
                .orElse("");

        MetaData metaData = EMPTY_METADATA;

        if (ObjectHelper.isNotEmpty(tableName)) {
            KuduMetaData kuduInsertMetaData = new KuduMetaData();
            kuduInsertMetaData.setTableName(tableName);

            final String host = Optional.ofNullable(properties.get("host"))
                    .map(Object::toString)
                    .orElse("localhost");
            kuduInsertMetaData.setHost(host);

            final String port = Optional.ofNullable(properties.get("port"))
                    .map(Object::toString)
                    .orElse("7051");
            kuduInsertMetaData.setPort(port);

            metaData = new DefaultMetaData(null, null, kuduInsertMetaData);
        }

        return Optional.of(metaData);
    }
}
