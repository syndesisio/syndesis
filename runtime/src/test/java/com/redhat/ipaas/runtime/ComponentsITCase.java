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

import com.redhat.ipaas.api.v1.model.Component;
import com.redhat.ipaas.api.v1.model.ListResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ITConfig.class)
public class ComponentsITCase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void componentsListTest() {
        ListResult<Component> results = this.restTemplate.getForObject("/api/v1/components", ListResult.class);
        assertThat(results.getItems()).hasSize(20);
        assertThat(results.getTotalCount()).isEqualTo(50);
    }

    @Test
    public void componentsGetTest() {
        Component result = this.restTemplate.getForObject("/api/v1/components/1", Component.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).contains("1");
    }

}
