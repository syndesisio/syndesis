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

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.syndesis.core.Json;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.jsondb.impl.SqlJsonDB;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
		Application.class,
        InfinispanCacheConfiguration.class,
        DataStoreConfiguration.class,
        SyndesisCorsConfiguration.class
})
public abstract class BaseITCase {

    public static WireMockRule wireMock;

    @Autowired
    protected SqlJsonDB jsondb;

    @Autowired
    protected DataManager dataManager;

    @BeforeClass
    public static void envSetup() {
        // On some systems (like running a build in a k8s pod), you don't get home dir.
        if( System.getProperty("user.home")==null ) {
            // But ivy/grape fails if it does not know where the user home dir is located..
            String target = Paths.get("target").toAbsolutePath().toString();
            System.setProperty("user.home", target);
        }
    }

    @PostConstruct()
    public void resetDB() {
        get("/api/v1/test-support/reset-db", Void.class, tokenRule.validToken(), HttpStatus.NO_CONTENT);
    }

    protected void clearDB() {
        try {
            this.dataManager.clearCache();
            this.jsondb.dropTables();
        } catch (Exception e) {
        }
        this.jsondb.createTables();
    }

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private TestRestTemplate restTemplate;

    @Rule
    public final APITokenRule tokenRule = new APITokenRule();

    public TestRestTemplate restTemplate() {
        return restTemplate;
    }

    public static class TestConfigurationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(final ConfigurableApplicationContext applicationContext) {
            final ConfigurableEnvironment environment = applicationContext.getEnvironment();
            environment.getPropertySources().addFirst(new MapPropertySource("test-source",
                    Collections.singletonMap("verifier.service", "localhost:" + wireMock.port())));
        }
    }

    @Autowired
    public void setRestTemplate(TestRestTemplate testRestTemplate) {
        List<HttpMessageConverter<?>> messageConverters = testRestTemplate.getRestTemplate().getMessageConverters();
        messageConverters.add(0, new YamlJackson2HttpMessageConverter());
        messageConverters.add(0, new JsonJackson2HttpMessageConverter());
        this.restTemplate = testRestTemplate;
    }


    protected <T> ResponseEntity<T> delete(String url) {
        return delete(url, null, tokenRule.validToken(), HttpStatus.NO_CONTENT);
    }

    protected <T> ResponseEntity<T> delete(String url, Class<T> responseClass) {
        return delete(url, responseClass, tokenRule.validToken(), HttpStatus.NO_CONTENT);
    }

    protected <T> ResponseEntity<T> delete(String url, Class<T> responseClass, String token) {
        return delete(url, responseClass, token, HttpStatus.NO_CONTENT);
    }

    protected <T> ResponseEntity<T> delete(String url, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.DELETE, url, null, responseClass, token, expectedStatus);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass) {
        return get(url, responseClass, HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, HttpStatus expectedStatus) {
        return get(url, responseClass, tokenRule.validToken(), expectedStatus);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, String token) {
        return get(url, responseClass, token, HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.GET, url, null, responseClass, token, expectedStatus);
    }

    protected <T> ResponseEntity<T> get(String url, ParameterizedTypeReference<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.GET, url, null, responseClass, token, new HttpHeaders(), expectedStatus);
    }

    protected <T> ResponseEntity<T> post(String url, Object body,  Class<T> responseClass) {
        return post(url, body, responseClass, tokenRule.validToken(), HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> post(String url, Object body, Class<T> responseClass, String token) {
        return post(url, body, responseClass, token, HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> post(String url, Object body, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.POST, url, body, responseClass, token, expectedStatus);
    }

    protected <T> ResponseEntity<T> post(String url, Object body, ParameterizedTypeReference<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.POST, url, body, responseClass, token, new HttpHeaders(), expectedStatus);
    }

    protected <T> ResponseEntity<T> post(String url, Object body, Class<T> responseClass, String token, HttpStatus expectedStatus, HttpHeaders headers) {
        return http(HttpMethod.POST, url, body, responseClass, token, headers, expectedStatus);
    }

    protected <T> ResponseEntity<T> put(String url, Object body, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.PUT, url, body, responseClass, token, new HttpHeaders(), expectedStatus);
    }

    protected <T> ResponseEntity<T> http(HttpMethod method, String url, Object body, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(method, url, body, responseClass, token, new HttpHeaders(), expectedStatus);
    }

    protected <T> ResponseEntity<T> http(HttpMethod method, String url, Object body, ParameterizedTypeReference<T> responseClass, String token, HttpHeaders headers, HttpStatus expectedStatus) {
        prepareHeaders(body, headers, token);

        ResponseEntity<T> response = restTemplate().exchange(url, method, new HttpEntity<>(body, headers), responseClass);

        return processResponse(expectedStatus, response);
    }

    protected <T> ResponseEntity<T> http(HttpMethod method, String url, Object body, Class<T> responseClass, String token, HttpHeaders headers, HttpStatus expectedStatus) {
        prepareHeaders(body, headers, token);

        ResponseEntity<T> response = restTemplate().exchange(url, method, new HttpEntity<>(body, headers), responseClass);

        return processResponse(expectedStatus, response);
    }

    private <T> ResponseEntity<T> processResponse(HttpStatus expectedStatus, ResponseEntity<T> response) {
        if( expectedStatus!=null ) {
            assertThat(response.getStatusCode()).as("status code").isEqualTo(expectedStatus);
        }
        return response;
    }

    private void prepareHeaders(Object body, HttpHeaders headers, String token) {
        if( body!=null && !headers.containsKey(HttpHeaders.CONTENT_TYPE) ) {
            headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        }
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        headers.set("X-Forwarded-User", "someone_important");
        headers.set("X-Forwarded-Access-Token", token);
    }

    final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        YamlJackson2HttpMessageConverter() {
            super(new YAMLMapper(), MediaType.parseMediaType("application/yaml"));
        }
    }
    final class JsonJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        JsonJackson2HttpMessageConverter() {
            super(Json.mapper(), MediaType.parseMediaType("application/json"));
        }
    }

}
