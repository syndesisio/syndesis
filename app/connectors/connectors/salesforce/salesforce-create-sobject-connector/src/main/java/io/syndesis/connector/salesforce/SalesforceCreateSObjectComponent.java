/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.connector.salesforce;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Message;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.api.dto.CreateSObjectResult;
import org.apache.camel.component.salesforce.api.utils.JsonUtils;

/**
 * Camel salesforce-create-sobject connector
 */
public class SalesforceCreateSObjectComponent extends DefaultConnectorComponent {


    public SalesforceCreateSObjectComponent() {
        this(null);
    }

    public SalesforceCreateSObjectComponent(String componentSchema) {
        super("salesforce-create-sobject", componentSchema, SalesforceCreateSObjectComponent.class.getName());

        setAfterProducer( exchange -> {

            // map json response back to CreateSObjectResult POJO
            ObjectMapper mapper = JsonUtils.createObjectMapper();
            if (!exchange.isFailed()) {
                Message out = exchange.getOut();
                CreateSObjectResult result = mapper.readValue(out.getBody(String.class),
                        CreateSObjectResult.class);
                out.setBody(result);
            }
        });
    }

}
