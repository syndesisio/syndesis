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
package io.syndesis.connector.gmail;

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.mail.stream.GoogleMailStreamConstants;
import org.apache.camel.util.ObjectHelper;

public class GmailReceiveEmailCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void beforeConsumer(Exchange exchange) {

        final Message in = exchange.getIn();
        final GmailMessageModel mail = new GmailMessageModel();
        if (ObjectHelper.isNotEmpty(in.getBody())) {
            mail.setText(in.getBody(String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(GoogleMailStreamConstants.MAIL_SUBJECT))) {
            mail.setSubject(in.getHeader(GoogleMailStreamConstants.MAIL_SUBJECT, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(GoogleMailStreamConstants.MAIL_TO))) {
            mail.setTo(in.getHeader(GoogleMailStreamConstants.MAIL_TO, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(GoogleMailStreamConstants.MAIL_CC))) {
            mail.setCc(in.getHeader(GoogleMailStreamConstants.MAIL_CC, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(GoogleMailStreamConstants.MAIL_BCC))) {
            mail.setBcc(in.getHeader(GoogleMailStreamConstants.MAIL_BCC, String.class));
        }
        exchange.getIn().setBody(mail);
    }
}
