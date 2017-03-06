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
package com.redhat.ipaas.runtime;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.redhat.ipaas.jsondb.impl.SqlJsonDB;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseITCase {

    @Autowired
    protected SqlJsonDB jsondb;

    @BeforeClass
    public static void envSetup() {
        // If the keycloak.http.port is not configured.. configure it now so that
        // our test cases work in an IDE without having to do additional config.
        if( System.getProperty("keycloak.http.port")==null ) {
            System.setProperty("keycloak.http.port", "8282");
        }
    }

    protected void databaseReset() {
        try {
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

    @Autowired
    public void setRestTemplate(TestRestTemplate testRestTemplate) {
        testRestTemplate.getRestTemplate().getMessageConverters().add(new YamlJackson2HttpMessageConverter());
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
        return get(url, responseClass, tokenRule.validToken(), HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, String token) {
        return get(url, responseClass, token, HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        return http(HttpMethod.GET, url, null, responseClass, token, expectedStatus);
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

    protected <T> ResponseEntity<T> http(HttpMethod method, String url, Object body, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        ResponseEntity<T> response = restTemplate().exchange(url, method, new HttpEntity<>(body, headers), responseClass);
        if( expectedStatus!=null ) {
            assertThat(response.getStatusCode()).as("status code").isEqualTo(expectedStatus);
        }
        return response;
    }

    final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        YamlJackson2HttpMessageConverter() {
            super(new YAMLMapper(), MediaType.parseMediaType("application/yaml"));
        }
    }

}
