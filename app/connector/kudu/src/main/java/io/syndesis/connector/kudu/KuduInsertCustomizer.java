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

import io.syndesis.connector.kudu.model.KuduInsert;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kudu.KuduDbOperations;
import org.apache.camel.util.ObjectHelper;

import java.util.Map;

public class KuduInsertCustomizer implements ComponentProxyCustomizer {
    private KuduInsert row;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setOptions(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setOptions(Map<String, Object> options) {
        String[] ro = options.get("row").toString().split(";", -1);
        Object[] optionsRow = new Object[ro.length];

        for (int i = 0; i < ro.length; i++) {
            String[] current = ro[i].split(",", 2);
            switch (current[0]) {
                case "String":
                    optionsRow[i] = current[1];
                    break;
                case "Integer":
                    optionsRow[i] = Integer.parseInt(current[1]);
                    break;
                default:
                    throw new IllegalArgumentException("The type " + current[0] + " is not supported");
            }
        }

        row = new KuduInsert();
        row.setRow(optionsRow, true);

        options.put("operation", KuduDbOperations.INSERT);
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final KuduInsert model = exchange.getIn().getBody(KuduInsert.class);

        if (model != null && ObjectHelper.isNotEmpty(model.getRow())) {
                row = model;
        }

        in.setBody(row.getRow());
    }
}
