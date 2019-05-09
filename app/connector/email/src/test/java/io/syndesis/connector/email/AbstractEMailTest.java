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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.DefaultPropertiesParser;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesParser;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;
import io.syndesis.connector.email.server.EMailTestServer;
import io.syndesis.connector.email.server.EMailTestServer.Options;

public abstract class AbstractEMailTest implements EMailConstants {

    protected static final String TEST_HOST_NAME = "localhost";
    protected static final String TEST_USER_NAME = "bob" + AT + TEST_HOST_NAME;
    protected static final String TEST_PASSWORD = "MyReallySecurePassword";

    protected static final int MOCK_TIMEOUT_MILLISECONDS = 60000;

    private static Options[] serverTypes = {
            Options.IMAP,
            Options.IMAPS,
            Options.POP3,
            Options.POP3S,
            Options.SMTP,
            Options.SMTPS
            };

    private static Map<Options, EMailTestServer> mailServers = new HashMap<>();

    @Configuration
    public static class TestConfiguration {
        @Bean
        public PropertiesParser propertiesParser(PropertyResolver propertyResolver) {
            return new DefaultPropertiesParser() {
                @Override
                public String parseProperty(String key, String value, Properties properties) {
                    return propertyResolver.getProperty(key);
                }
            };
        }

        @Bean(destroyMethod = "")
        public PropertiesComponent properties(PropertiesParser parser) {
            PropertiesComponent pc = new PropertiesComponent();
            pc.setPropertiesParser(parser);
            return pc;
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    protected CamelContext context;


    @BeforeClass
    public static void scaffold() throws Exception {
        for (Options option : serverTypes) {
            if (! mailServers.containsKey(option)) {
                EMailTestServer server = new EMailTestServer(TEST_HOST_NAME, option);
                server.createUser(TEST_USER_NAME, TEST_PASSWORD);
                refresh(server);
                server.start();
                mailServers.put(option, server);
            }
        }
    }

    protected static void refresh(EMailTestServer server) throws Exception {
        if (server == null) {
            return;
        }

        server.clear();
        if (! server.isSmtp()) {
            server.generateMail(TEST_USER_NAME, TEST_PASSWORD);
        }
    }

    @Before
    public void setup() {
        context = createCamelContext();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    /**
     * Creates a camel context complete with a properties component that handles
     * lookups of secret values such as passwords. Fetches the values from external
     * properties file.
     *
     * @return CamelContext
     */
    protected CamelContext createCamelContext() {
            CamelContext ctx = new SpringCamelContext(applicationContext);
            ctx.disableJMX();
            PropertiesComponent pc = new PropertiesComponent("classpath:mail-test-options.properties");
            ctx.addComponent("properties", pc);
            return ctx;
        }

    protected static EMailTestServer server(Options option) {
        return mailServers.get(option);
    }

    protected static EMailTestServer imapServer() {
        return server(Options.IMAP);
    }

    protected static EMailTestServer imapsServer() {
        return server(Options.IMAPS);
    }

    protected static EMailTestServer pop3Server() {
        return server(Options.POP3);
    }

    protected static EMailTestServer pop3sServer() {
        return server(Options.POP3S);
    }

    protected static EMailTestServer smtpServer() {
        return server(Options.SMTP);
    }

    protected static EMailTestServer smtpsServer() {
        return server(Options.SMTPS);
    }

}
