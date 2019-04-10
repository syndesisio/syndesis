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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.connector.kudu.common.KuduSupport;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KuduScanCustomizer implements ComponentProxyCustomizer {
    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setOptions(options);
        component.setBeforeConsumer(this::processBody);
        component.setAfterProducer(this::processBody);
    }

    private void setOptions(Map<String, Object> options) {
        options.put("operation", KuduDbOperations.SCAN);
        options.put("type", KuduDbOperations.SCAN);
    }

    private void processBody(Exchange exchange) throws KuduException, JsonProcessingException {
        final Message in = exchange.getIn();
        final KuduScanner scanner = in.getBody(KuduScanner.class);

        final List<String> answer = new ArrayList<>();
        while(scanner.hasMoreRows()) {
            RowResultIterator results = scanner.nextRows();

            while (results.hasNext()) {
                Map<String, Object> row = new HashMap<String, Object>();
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
                answer.add(KuduSupport.toJSONBean(row));
            }
        }

        in.setBody(answer);
    }
}
