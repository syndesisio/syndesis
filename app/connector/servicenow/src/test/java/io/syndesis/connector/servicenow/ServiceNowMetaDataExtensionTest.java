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
package io.syndesis.connector.servicenow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.assertj.core.api.Condition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceNowMetaDataExtensionTest {

    @Test
    public void retrieveTableListTes() {
        CamelContext context = new DefaultCamelContext();
        ServiceNowMetaDataExtension extension = new ServiceNowMetaDataExtension(context);

        Map<String, Object> properties = new HashMap<>();
        properties.put("instanceName", System.getProperty("servicenow.instance"));
        properties.put("userName", System.getProperty("servicenow.username"));
        properties.put("password", System.getProperty("servicenow.password"));
        properties.put("objectType", ServiceNowConstants.RESOURCE_TABLE);
        properties.put("metaType", "list");

        Optional<MetaDataExtension.MetaData> meta = extension.meta(properties);

        assertThat(meta).isPresent();
        assertThat(meta.get().getPayload()).isInstanceOf(JsonNode.class);
        assertThat(meta.get().getAttributes()).hasEntrySatisfying(MetaDataExtension.MetaData.JAVA_TYPE, new Condition<Object>() {
            @Override
            public boolean matches(Object val) {
                return Objects.equals(JsonNode.class, val);
            }
        });
        assertThat(meta.get().getAttributes()).hasEntrySatisfying(MetaDataExtension.MetaData.CONTENT_TYPE, new Condition<Object>() {
            @Override
            public boolean matches(Object val) {
                return Objects.equals("application/json", val);
            }
        });
        assertThat(meta.get().getAttributes()).hasEntrySatisfying("Meta-Context", new Condition<Object>() {
            @Override
            public boolean matches(Object val) {
                return Objects.equals("import", val);
            }
        });
    }
}
