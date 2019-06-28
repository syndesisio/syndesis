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
package io.syndesis.connector.odata.customizer;

import java.io.IOException;
import java.util.Map;
import org.apache.camel.Exchange;
import io.syndesis.connector.odata.component.ODataComponent;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

public class ODataReadFromCustomizer extends AbstractODataCustomizer {

    /*
     * The component subsumes the split & key predicate properties from the
     * options map & makes them available via a getter, hence the need to look
     * at the component rather than the options
     */
    private boolean shouldSplitResult(ComponentProxyComponent component) {
        if (! (component instanceof ODataComponent)) {
            return false;
        }

        ODataComponent oc = (ODataComponent) component;
        return oc.getKeyPredicate() != null || oc.isSplitResult();
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setSplit(shouldSplitResult(component));
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void beforeConsumer(Exchange exchange) throws IOException {
        convertMessageToJson(exchange.getIn());
    }
}
