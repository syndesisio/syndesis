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
package io.syndesis.credential;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.core.EventBus;
import io.syndesis.credential.CredentialsIntegrationTest.TestConfiguration;
import io.syndesis.dao.ConnectionDao;
import io.syndesis.dao.manager.DataManager;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.social.FacebookAutoConfiguration;
import org.springframework.boot.autoconfigure.social.LinkedInAutoConfiguration;
import org.springframework.boot.autoconfigure.social.SocialWebAutoConfiguration;
import org.springframework.boot.autoconfigure.social.TwitterAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = {CredentialConfiguration.class, TestConfiguration.class})
@SpringBootTest
@EnableAutoConfiguration(exclude = {TwitterAutoConfiguration.class, FacebookAutoConfiguration.class,
    LinkedInAutoConfiguration.class, SocialWebAutoConfiguration.class})
@PropertySource(value = "dynamic", name = "dynamic", factory = CredentialsIntegrationTest.DynamicPropertySource.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
public class CredentialsIntegrationTest {

    @Parameter(0)
    public static String PROVIDER;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CredentialProviderLocator credentialProviderLocator;

    @Configuration
    public static class TestConfiguration {
        @Bean
        public ConnectionDao connectionDao() {
            return mock(ConnectionDao.class);
        }

        @Bean
        public DataManager dataManager() {
            return new DataManager(null, null, (String) null);
        }

        @Bean
        public EventBus eventBus() {
            return mock(EventBus.class);
        }
    }

    static class DynamicPropertySource implements PropertySourceFactory {
        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(final String name,
            final EncodedResource resource) throws IOException {
            final Map<String, Object> properties = new HashMap<>();

            // needed by DataManager
            properties.put("deployment.file", "");

            properties.put("spring.social." + PROVIDER + ".appId", "testClientId");
            properties.put("spring.social." + PROVIDER + ".appSecret", "testClientSecret");

            return new MapPropertySource(name, properties);
        }
    }

    @Test
    public void shouldSupportResourceProviders() {
        assertThat(credentialProviderLocator.providerWithId(PROVIDER)).isNotNull();
    }

    @Parameters(name = "provider={0}")
    public static Iterable<String> resourceProviders() {
        return Arrays.asList("salesforce", "twitter");
    }
}
