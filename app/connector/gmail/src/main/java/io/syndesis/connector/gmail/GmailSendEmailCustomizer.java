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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.google.mail.internal.GmailUsersMessagesApiMethod;
import org.apache.camel.component.google.mail.internal.GoogleMailApiCollection;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.util.Base64;
import com.google.common.base.Splitter;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class GmailSendEmailCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(GmailSendEmailCustomizer.class);

    private String to;
    private String subject;
    private String text;
    private String userId;
    private String bcc;
    private String cc;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        to = ConnectorOptions.extractOption(options, "to");
        subject = ConnectorOptions.extractOption(options, "subject");
        text = ConnectorOptions.extractOption(options, "text");
        userId = "me";
        cc = ConnectorOptions.extractOption(options, "cc");
        bcc = ConnectorOptions.extractOption(options, "bcc");
        options.put("apiName",
                GoogleMailApiCollection.getCollection().getApiName(GmailUsersMessagesApiMethod.class).getName());
        options.put("methodName", "send");
    }

    private void beforeProducer(Exchange exchange) throws MessagingException, IOException {

        final Message in = exchange.getIn();
        final GmailMessageModel mail = exchange.getIn().getBody(GmailMessageModel.class);
        if (mail != null) {
            if (ObjectHelper.isNotEmpty(mail.getText())) {
                text = mail.getText();
            }
            if (ObjectHelper.isNotEmpty(mail.getSubject())) {
                subject = mail.getSubject();
            }
            if (ObjectHelper.isNotEmpty(mail.getTo())) {
                to = mail.getTo();
            }
            if (ObjectHelper.isNotEmpty(mail.getCc())) {
                cc = mail.getCc();
            }
            if (ObjectHelper.isNotEmpty(mail.getBcc())) {
                bcc = mail.getBcc();
            }
        }
        com.google.api.services.gmail.model.Message message = createMessage(to, userId, subject, text, cc, bcc);

        in.setHeader("CamelGoogleMail.content", message);
        in.setHeader("CamelGoogleMail.userId", userId);
    }

    private com.google.api.services.gmail.model.Message createMessage(String to, String from, String subject,
            String bodyText, String cc, String bcc) throws MessagingException, IOException {

        if (ObjectHelper.isEmpty(to)) {
            throw new RuntimeCamelException("Cannot create gmail message as no 'to' address is available");
        }

        if (ObjectHelper.isEmpty(from)) {
            throw new RuntimeCamelException("Cannot create gmail message as no 'from' address is available");
        }

        if (ObjectHelper.isEmpty(subject)) {
            LOG.warn("New gmail message wil have no 'subject'. This may not be want you wanted?");
        }

        if (ObjectHelper.isEmpty(bodyText)) {
            LOG.warn("New gmail message wil have no 'body text'. This may not be want you wanted?");
        }

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipients(javax.mail.Message.RecipientType.TO, getAddressesList(to));
        email.setSubject(subject);
        email.setText(bodyText);
        if (ObjectHelper.isNotEmpty(cc)) {
            email.addRecipients(javax.mail.Message.RecipientType.CC, getAddressesList(cc));
        }
        if (ObjectHelper.isNotEmpty(bcc)) {
            email.addRecipients(javax.mail.Message.RecipientType.BCC, getAddressesList(bcc));
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private Address[] getAddressesList(String addressString) throws AddressException {
        List<String> recipientList = Splitter.on(',').splitToList(addressString);
        Address[] recipientAddress = new InternetAddress[recipientList.size()];
        int counter = 0;
        for (String recipient : recipientList) {
            recipientAddress[counter] = new InternetAddress(recipient.trim());
            counter++;
        }
        return recipientAddress;
    }
}
