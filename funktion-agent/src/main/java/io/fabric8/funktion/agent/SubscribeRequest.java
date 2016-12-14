/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.agent;

import io.fabric8.funktion.model.DtoSupport;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.funktion.model.Flow;
import io.fabric8.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.fabric8.utils.Lists.notNullList;

/**
 */
public class SubscribeRequest extends DtoSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(SubscribeRequest.class);

    private Funktion funktion;
    private Map<String, String> applicationProperties = new HashMap<>();
    private String connectorName;
    private String namespace;

    public SubscribeRequest() {
    }

    public SubscribeRequest(String namespace, Funktion funktion, Map<String, String> applicationProperties) {
        this.funktion = funktion;
        this.applicationProperties = applicationProperties;
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "CreateFlowRequest{" +
                "funktion=" + funktion +
                ", applicationProperties=" + applicationProperties +
                ", namespace='" + namespace + '\'' +
                '}';
    }

    /**
     * Returns the connector name either from an explicit parameter or by taking the first trigger
     */
    public String findConnectorName() {
        String answer = getConnectorName();
        if (Strings.isNullOrBlank(answer)) {
            if (funktion != null) {
                List<Flow> rules = notNullList(funktion.getFlows());
                for (Flow rule : rules) {
                    String trigger = rule.getTrigger();
                    try {
                        return getURIScheme(trigger);
                    } catch (URISyntaxException e) {
                        LOG.info("Ignoring parse issue with trigger " + trigger + ". " + e, e);
                    }
                }
            }
        }
        return answer;
    }

    public static String getURIScheme(String text) throws URISyntaxException {
        URI uri = new URI(text);
        return uri.getScheme();
    }


    public Funktion getFunktion() {
        return funktion;
    }

    public void setFunktion(Funktion funktion) {
        this.funktion = funktion;
    }

    public Map<String, String> getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(Map<String, String> applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
