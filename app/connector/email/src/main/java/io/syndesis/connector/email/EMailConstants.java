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

    String SECURE = "secure";

    String HOST = "host";

    String PORT = "port";

    String USER = "username";

    String PASSWORD = "password";

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

    /**
     * Content types of email
     */
    String TEXT_HTML = "text/html";
    String TEXT_PLAIN = "text/plain";

    enum Protocols {
        IMAP, IMAPS,
        POP3, POP3S,
        SMTP, SMTPS;

        public static Protocols getValueOf(String name) {
            for (Protocols method : Protocols.values()) {
                if (method.name().equalsIgnoreCase(name)) {
                    return method;
                }
            }

            return null;
        }

        public String id() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public boolean isSSL() {
            return name().endsWith("S");
        }

        public boolean isReceiver() {
            return ! isProducer();
        }

        public boolean isProducer() {
            return this.equals(SMTP) || this.equals(SMTPS);
        }

        public String componentSchema() {
            return "email" + HYPHEN + (isReceiver() ? "receive" : "send");
        }
    }

    enum EMailFunction {
        READ,
        SEND;

        public String id() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public String connectorId() {
            return "email" + HYPHEN + id() + HYPHEN + "connector";
        }
    }

    enum Priorities {
        INPUT_VALUES("inputValues"),
        CONSUMED_DATA("consumedData");

        private final String id;

        Priorities(String id) {
            this.id = id;
        }

        public static Priorities getValueOf(String name) {
            if (name == null) {
                return CONSUMED_DATA;
            }

            for (Priorities priority : Priorities.values()) {
                if (priority.id.equalsIgnoreCase(name)) {
                    return priority;
                }
            }

            return CONSUMED_DATA;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
