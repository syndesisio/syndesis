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
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseITCase {

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

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass) {
        return get(url, responseClass, tokenRule.validToken(), HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, String token) {
        return get(url, responseClass, token, HttpStatus.OK);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseClass, String token, HttpStatus expectedStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        ResponseEntity<T> response = restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseClass);
        assertThat(response.getStatusCode()).as("status code").isEqualTo(expectedStatus);
        return response;
    }

    final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        YamlJackson2HttpMessageConverter() {
            super(new YAMLMapper(), MediaType.parseMediaType("application/yaml"));
        }
    }

}
