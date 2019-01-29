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

import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.google.gson.Gson;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kudu.KuduDbOperations;
import org.apache.kudu.Type;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.RowResult;
import org.apache.kudu.client.RowResultIterator;

import java.util.HashMap;
import java.util.Map;

public class KuduScanCustomizer implements ComponentProxyCustomizer {
    Map<String, Object> row;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        row = new HashMap<>();
        setOptions(options);
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void setOptions(Map<String, Object> options) {
        options.put("operation", KuduDbOperations.SCAN);
        options.put("type", KuduDbOperations.SCAN);
    }

    private void beforeConsumer(Exchange exchange) throws KuduException {
        final Message in = exchange.getIn();
        final KuduScanner scanner = in.getBody(KuduScanner.class);

        RowResultIterator results = scanner.nextRows();

        RowResult result = results.next();
        for (int i = 0; i < result.getSchema().getColumnCount(); i++) {
            String key = result.getSchema().getColumnByIndex(i).getName();
            Type type = result.getColumnType(i);

            switch (type.getName()) {
                case "string":
                    row.put(key, result.getString(i));
                    break;
                case "bool":
                    row.put(key, result.getBoolean(i));
                    break;
                case "int8":
                case "int16":
                case "int32":
                    row.put(key, result.getInt(i));
                    break;
                case "int64":
                    row.put(key, result.getLong(i));
                    break;
                case "double":
                    row.put(key, result.getDouble(i));
                    break;
                case "float":
                    row.put(key, result.getFloat(i));
                    break;
                default:
                    throw new SyndesisServerException("The column schema type " + type.getName()
                            + " for column " + key
                            + " is not supported at the moment");
            }
        }

        Gson gson = new Gson();
        in.setBody(gson.toJson(row));
    }
}
