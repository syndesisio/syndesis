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
package io.syndesis.connector.email.customizer;

import java.io.IOException;
import java.util.Map;
import javax.mail.MessagingException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.email.model.EMailMessageModel;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class EMailSendCustomizer implements ComponentProxyCustomizer, EMailConstants {

    private String from;
    private String to;
    private String subject;
    private String text;
    private String bcc;
    private String cc;
    private Priority priority;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        from = ConnectorOptions.extractOption(options, MAIL_FROM);
        to = ConnectorOptions.extractOption(options, MAIL_TO);
        subject = ConnectorOptions.extractOption(options, MAIL_SUBJECT);
        text = ConnectorOptions.extractOption(options, MAIL_TEXT);
        cc = ConnectorOptions.extractOption(options, MAIL_CC);
        bcc = ConnectorOptions.extractOption(options, MAIL_BCC);

        //
        // Will return injected data if not set
        //
        priority = ConnectorOptions.extractOptionAndMap(options, PRIORITY, Priority::priorityFromId);
    }

    private Object updateMail(String inputValue, Object dataValue) {
        if (ObjectHelper.isEmpty(inputValue)) {
            // Input value is empty so return data value
            return dataValue;
        }

        if (ObjectHelper.isEmpty(dataValue)) {
            // Data value is empty so return input value (which is not empty)
            return inputValue;
        }

        if (Priority.INPUT_VALUES.equals(priority)) {
            // Input values are given priority so don't update
            return inputValue;
        }

        // Otherwise return the data value
        return dataValue;
    }

    private void beforeProducer(Exchange exchange) throws MessagingException, IOException {
        final Message in = exchange.getIn();
        final EMailMessageModel mail = in.getBody(EMailMessageModel.class);
        if (mail == null) {
            return;
        }

        in.setHeader(MAIL_FROM, updateMail(from, mail.getFrom()));
        in.setHeader(MAIL_TO, updateMail(to, mail.getTo()));
        in.setHeader(MAIL_CC, updateMail(cc, mail.getCc()));
        in.setHeader(MAIL_BCC, updateMail(bcc, mail.getBcc()));
        in.setHeader(MAIL_SUBJECT, updateMail(subject, mail.getSubject()));

        in.setBody(updateMail(text, mail.getContent()));
    }
}
