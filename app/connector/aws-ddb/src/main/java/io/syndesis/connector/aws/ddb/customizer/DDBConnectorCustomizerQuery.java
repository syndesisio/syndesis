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
package io.syndesis.connector.aws.ddb.customizer;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.base.Splitter;
import io.syndesis.connector.aws.ddb.util.Util;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.Exchange;
import org.apache.camel.component.aws.ddb.DdbConstants;
import org.apache.camel.component.aws.ddb.DdbOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DDBConnectorCustomizerQuery extends DDBConnectorCustomizer {

    @Override
    protected void customize(Exchange exchange, Map<String, Object> options) {
        Map<String, AttributeValue> element =
        Util.getAttributeValueMap("element", options);
        exchange.getIn().setHeader(DdbConstants.KEY, element);
        exchange.getIn().setHeader(DdbConstants.OPERATION, DdbOperations.GetItem);

        List<String> attributes = new ArrayList<String>();
        String optionAttributes = ConnectorOptions.extractOption(options, "attributes", "");
        if (!optionAttributes.isEmpty()) {
            Splitter splitter = Splitter.on(',');
            splitter = splitter.trimResults();
            splitter = splitter.omitEmptyStrings();
            attributes = splitter.splitToList(optionAttributes);
        }


        //fallback to use the list of attributes on the filter
        if (attributes.isEmpty()) {
            attributes.addAll(element.keySet());
        }

        exchange.getIn().setHeader(DdbConstants.ATTRIBUTE_NAMES, attributes);

    }

}
