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
package io.syndesis.connector.email;

import java.util.Locale;
import io.syndesis.common.util.StringConstants;

public interface EMailConstants extends StringConstants {

    String PROTOCOL = "protocol";

    String SECURE_TYPE = "secureType";

    String HOST = "host";

    String PORT = "port";

    String USER = "username";

    String PASSWORD = "password";

    String FOLDER = "folderName";

    String ADDITIONAL_MAIL_PROPERTIES = "AdditionalJavaMailProperties";

    String SSL_CONTEXT_PARAMETERS = "sslContextParameters";

    String SERVER_CERTIFICATE = "serverCertificate";

    String UNSEEN_ONLY = "unseenOnly";

    String TO_PLAIN_TEXT = "plainText";

    String MAX_MESSAGES = "maxMessagesPerPoll";

    String CONSUMER = "consumer";

    String DELAY = "delay";

    String MAIL_SUBJECT = "subject";

    String MAIL_FROM = "from";

    String MAIL_TO = "to";

    String MAIL_CC = "cc";

    String MAIL_BCC = "bcc";

    String MAIL_TEXT = "text";

    String PRIORITY = "priority";

    String CONNECTION_TIMEOUT = "mail.connection.timeout";

    /**
     * Content types of email
     */
    String TEXT_HTML = "text/html";
    String TEXT_PLAIN = "text/plain";

    enum Protocol {
        IMAP("imap"), IMAPS("imaps"),
        POP3("pop3"), POP3S("pop3s"),
        SMTP("smtp"), SMTPS("smtps");

        private final String id;

        Protocol(String id) {
            this.id = id;
        }

        public static Protocol getValueOf(String name) {
            if (name == null) {
                return null;
            }

            try {
                return valueOf(name.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        public static Protocol toSecureProtocol(String name) {
            if (name == null) {
                return null;
            }

            Protocol p = getValueOf(name);
            return p.toSecureProtocol();
        }

        public Protocol toSecureProtocol() {
            switch (this) {
                case IMAP:
                    return IMAPS;
                case POP3:
                    return POP3S;
                case SMTP:
                    return SMTPS;
                default:
                    return this;
            }
        }

        public Protocol toPlainProtocol() {
            switch (this) {
                case IMAPS:
                    return IMAP;
                case POP3S:
                    return POP3;
                case SMTPS:
                    return SMTP;
                default:
                    return this;
            }
        }

        public String id() {
            return id;
        }

        public boolean isSecure() {
            return name().endsWith("S");
        }

        public boolean isReceiver() {
            return ! isProducer();
        }

        public boolean isProducer() {
            return this.equals(SMTP) || this.equals(SMTPS);
        }

        public String componentSchema() {
            return isReceiver() ? "email-receive" : "email-send";
        }
    }

    enum SecureType {
        STARTTLS("StartTLS"),
        SSL_TLS("SSL/TLS");

        private final String id;

        SecureType(String id) {
            this.id = id;
        }

        public static SecureType secureTypeFromId(String id) {
            if (id == null) {
                return null;
            }

            for (SecureType secureType : SecureType.values()) {
                if (secureType.id.equalsIgnoreCase(id)) {
                    return secureType;
                }
            }

            return null;
        }

        public String id() {
            return this.id;
        }
    }

    enum Priority {
        INPUT_VALUES("inputValues"),
        CONSUMED_DATA("consumedData");

        private final String id;

        Priority(String id) {
            this.id = id;
        }

        public static Priority priorityFromId(String id) {
            if (id == null) {
                return CONSUMED_DATA;
            }

            for (Priority priority : Priority.values()) {
                if (priority.id.equalsIgnoreCase(id)) {
                    return priority;
                }
            }

            return Priority.CONSUMED_DATA;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
