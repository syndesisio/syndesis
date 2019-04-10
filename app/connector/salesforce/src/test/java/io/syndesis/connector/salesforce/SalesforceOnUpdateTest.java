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
package io.syndesis.connector.salesforce;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SalesforceOnUpdateTest {
    @Test
    public void testTopicLength() throws Exception {

        Map<String, String> options = new HashMap<>();
        options.put(SalesforceEndpointConfig.SOBJECT_NAME, "superlongvaluevaluevaluevaluevalue");
        options.put("notifyForOperationUpdate", "true");

        Assertions.assertThat(SalesforceUtil.topicNameFor(options)).isEqualToIgnoringCase("syndesis_superlongvalu_up");
    }
}
