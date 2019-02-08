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
package io.syndesis.server.runtime;

import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.endpoint.v1.state.ClientSideStateProperties;
import io.syndesis.server.endpoint.v1.state.StaticEdition;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.validation.Validator;
import java.io.IOException;
import java.util.List;

@SpringBootApplication(
    exclude = {
        TwitterAutoConfiguration.class,
        FacebookAutoConfiguration.class,
        LinkedInAutoConfiguration.class,
        SocialWebAutoConfiguration.class
    })
@EnableConfigurationProperties({ClientSideStateProperties.class, SpringMavenProperties.class})
public class Application extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Value("${encrypt.key}")
    private String encryptKey;

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
    public WebMvcConfigurer staticResourceConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                    .addResourceHandler("/mapper/**")
                    .addResourceLocations(
                        "classpath:/META-INF/syndesis/mapper/",
                        "classpath:/META-INF/resources/mapper/",
                        "classpath:/static/mapper/",
                        "classpath:/resources/mapper/"
                    );
            }
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public TextEncryptor getTextEncryptor() {
        return Encryptors.text(encryptKey, "deadbeef");
    }

    @Bean
    public ClientSideState clientSideState(final ClientSideStateProperties properties) {
        if (!properties.areSet()) {
            LOG.warn(new StringBuilder("\n*** Client side state persistence configuration is not defined, please set\n")
                .append("    CLIENT_STATE_AUTHENTICATION_ALGORITHM\n")//
                .append("    CLIENT_STATE_AUTHENTICATION_KEY\n")//
                .append("    CLIENT_STATE_ENCRYPTION_ALGORITHM\n")//
                .append("    CLIENT_STATE_ENCRYPTION_KEY\n")//
                .append("    CLIENT_STATE_TID\n")//
                .append(" environment variables.\n")//
                .append("*** Using randomized values for missing properties, this will not work across restarts or when scaled!")//
                .toString());
        }

        final StaticEdition edition = new StaticEdition(properties);

        return new ClientSideState(edition);
    }

    @Bean
    public Validator localValidatorFactoryBean(final ResteasyProviderFactory factory, final MessageSource messageSource, final ResourcePatternResolver resolver) throws IOException {
        final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(messageSource);

        final Resource[] mappings = resolver.getResources("classpath*:/META-INF/validation/*.xml");
        localValidatorFactoryBean.setMappingLocations(mappings);

        factory.register(new ValidatorContextResolver(localValidatorFactoryBean));

        return localValidatorFactoryBean;
    }

}
