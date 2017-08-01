/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.runtime;

import java.util.List;

import io.syndesis.rest.v1.state.ClientSideState;
import io.syndesis.rest.v1.state.ClientSideStateProperties;
import io.syndesis.rest.v1.state.StaticEdition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.social.FacebookAutoConfiguration;
import org.springframework.boot.autoconfigure.social.LinkedInAutoConfiguration;
import org.springframework.boot.autoconfigure.social.SocialWebAutoConfiguration;
import org.springframework.boot.autoconfigure.social.TwitterAutoConfiguration;
import org.springframework.boot.context.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {TwitterAutoConfiguration.class, FacebookAutoConfiguration.class,
    LinkedInAutoConfiguration.class, SocialWebAutoConfiguration.class})
@EnableConfigurationProperties(ClientSideStateProperties.class)
public class Application extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Autowired
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory(List<UndertowDeploymentInfoCustomizer> customizers) {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        for (UndertowDeploymentInfoCustomizer customizer : customizers) {
            factory.addDeploymentInfoCustomizers(customizer);
        }
        return factory;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ClientSideState clientSideState(final ClientSideStateProperties properties) {
        if (!properties.areSet()) {
            LOG.warn("\n*** Client side state persistence configuration is not defined, please set\n"
                + "    CLIENT_STATE_AUTHENTICATION_ALGORITHM\n"//
                + "    CLIENT_STATE_AUTHENTICATION_KEY\n"//
                + "    CLIENT_STATE_ENCRYPTION_ALGORITHM\n"//
                + "    CLIENT_STATE_ENCRYPTION_KEY\n"//
                + "    CLIENT_STATE_TID\n"//
                + " environment variables.\n"//
                + "*** Using randomized values for missing properties, this will not work across restarts or when scaled!");
        }

        final StaticEdition edition = new StaticEdition(properties);

        return new ClientSideState(edition);
    }

}
