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

import java.util.Map;
import io.syndesis.connector.aws.ddb.util.Util;
import org.apache.camel.Exchange;
import org.apache.camel.component.aws.ddb.DdbConstants;
import org.apache.camel.component.aws.ddb.DdbOperations;

public class DDBConnectorCustomizerRemoveItem extends DDBConnectorCustomizer {

    @Override
    protected void customize(Exchange exchange, Map<String, Object> options) {
        exchange.getIn().setHeader(DdbConstants.KEY, Util.getAttributeValueMap("element", options));
        exchange.getIn().setHeader(DdbConstants.OPERATION, DdbOperations.DeleteItem);
    }
}
