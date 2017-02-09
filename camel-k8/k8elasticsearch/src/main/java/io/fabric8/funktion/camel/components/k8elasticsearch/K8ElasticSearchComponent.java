/*
 * Copyright 2017 Red Hat, Inc.
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

package io.fabric8.funktion.camel.components.k8elasticsearch;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
import org.apache.camel.component.elasticsearch.ElasticsearchEndpoint;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class K8ElasticSearchComponent extends ElasticsearchComponent {

    public K8ElasticSearchComponent() {
    }

    public K8ElasticSearchComponent(CamelContext context) {
        super(context);
    }


    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        K8ElasticSearchConfiguration config = new K8ElasticSearchConfiguration();
        setProperties(config, parameters);
        config.setLocal(false);

        String serviceName = config.getServiceName();
        config.setClusterName(serviceName);

        String operation = remaining;
        if (operation != null){
            config.setOperation(operation);
        }


        config.setTransportAddressesList(parseTransportAddresses(config.getTransportAddresses(), config));

        Endpoint endpoint = new ElasticsearchEndpoint(uri, this, config, getClient());
        return endpoint;
    }

    private List<InetSocketTransportAddress> parseTransportAddresses(String ipsString, ElasticsearchConfiguration config) throws UnknownHostException {
        if (ipsString == null || ipsString.isEmpty()) {
            return null;
        }
        List<String> addressesStr = Arrays.asList(ipsString.split(ElasticsearchConstants.TRANSPORT_ADDRESSES_SEPARATOR_REGEX));
        List<InetSocketTransportAddress> addressesTrAd = new ArrayList<InetSocketTransportAddress>(addressesStr.size());
        for (String address : addressesStr) {
            String[] split = address.split(ElasticsearchConstants.IP_PORT_SEPARATOR_REGEX);
            String hostname;
            if (split.length > 0) {
                hostname = split[0];
            } else {
                throw new IllegalArgumentException();
            }
            Integer port = split.length > 1 ? Integer.parseInt(split[1]) : ElasticsearchConstants.DEFAULT_PORT;
            addressesTrAd.add(new InetSocketTransportAddress(InetAddress.getByName(hostname), port));
        }
        return addressesTrAd;
    }
}
