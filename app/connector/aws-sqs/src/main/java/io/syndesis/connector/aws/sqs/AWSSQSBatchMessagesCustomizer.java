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
package io.syndesis.connector.aws.sqs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.component.aws.sqs.SqsOperations;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class AWSSQSBatchMessagesCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {

        component.setBeforeProducer(this::beforeProducer);
    }

    @SuppressWarnings("unchecked")
    public void beforeProducer(final Exchange exchange) throws IOException {
        final Message in = exchange.getIn();
        final List<SQSMessage> messages = in.getBody(List.class);
        List<String> messageList = new ArrayList<String>();
        if (messages != null && !messages.isEmpty()) {
            for (Iterator<SQSMessage> iterator = messages.iterator(); iterator.hasNext();) {
                SQSMessage singleMessage = iterator.next();
                messageList.add(singleMessage.getMessage());
            }
        }
        in.setBody(messageList);
        in.setHeader(SqsConstants.SQS_OPERATION, SqsOperations.sendBatchMessage);
    }
}
