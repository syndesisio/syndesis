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
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.jsoup.Jsoup;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.email.model.EMailMessageModel;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class EMailReceiveCustomizer implements ComponentProxyCustomizer, EMailConstants {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void beforeConsumer(Exchange exchange) throws MessagingException, IOException {

        final Message in = exchange.getIn();
        final EMailMessageModel mail = new EMailMessageModel();

        if (ObjectHelper.isNotEmpty(in.getBody())) {
            textFromMessage(in, mail);
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(MAIL_SUBJECT))) {
            mail.setSubject(in.getHeader(MAIL_SUBJECT, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(MAIL_FROM))) {
            mail.setFrom(in.getHeader(MAIL_FROM, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(MAIL_TO))) {
            mail.setTo(in.getHeader(MAIL_TO, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(MAIL_CC))) {
            mail.setCc(in.getHeader(MAIL_CC, String.class));
        }
        if (ObjectHelper.isNotEmpty(in.getHeader(MAIL_BCC))) {
            mail.setBcc(in.getHeader(MAIL_BCC, String.class));
        }
        exchange.getIn().setBody(mail);
    }

    private String getPlainTextFromMultipart(Multipart multipart)  throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType(TEXT_PLAIN)) {
                result.append(NEW_LINE)
                            .append(bodyPart.getContent());
                break; // without break same text can appear twice
            } else if (bodyPart.isMimeType(TEXT_HTML)) {
                result.append(NEW_LINE)
                            .append(Jsoup.parse((String) bodyPart.getContent()).text());
                break; // without break same text can appear twice
            } else if (bodyPart.isMimeType("application/pgp-encrypted")) {
                //
                // Body is encrypted so flag as such to enable easy understanding for users
                //
                result.append(NEW_LINE)
                            .append("<pgp encrypted text>")
                            .append(NEW_LINE)
                            .append(bodyPart.getContent().toString());
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result.append(NEW_LINE)
                            .append(getPlainTextFromMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private void textFromMessage(Message camelMessage, EMailMessageModel model) throws MessagingException, IOException {
        Object content = camelMessage.getBody();

        if (content instanceof String) {
            content = content.toString().trim();
        } else if (content instanceof Multipart) {
            content = getPlainTextFromMultipart((Multipart) content);
        }

        model.setContent(content);
    }
}
