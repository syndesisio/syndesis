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

import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.apache.camel.component.elasticsearch.ElasticsearchEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.elasticsearch.client.Client;


@UriEndpoint(scheme = "k8elasticsearch", title = "ElasticSearch", syntax = "k8elasticsearch:Operation", producerOnly = true, label = "monitoring,search")

public class K8ElasticSearchEndpoint extends ElasticsearchEndpoint {


    @UriParam
    private K8ElasticSearchConfiguration elasticSearchConfiguration; //only used by APT generator

    public K8ElasticSearchEndpoint(String uri, ElasticsearchComponent component, K8ElasticSearchConfiguration config, Client client) throws Exception {
        super(uri, component,config,client);
        this.elasticSearchConfiguration = config;

    }


}
