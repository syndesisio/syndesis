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

package io.fabric8.funktion.camel.components.k8amq;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.component.jms.JmsConfiguration;

/**
 * The <a href="http://activemq.apache.org/camel/activemq.html">ActiveMQ Component</a>
 */
public class K8ActiveMQComponent extends ActiveMQComponent {

    @Override
    public void setConfiguration(JmsConfiguration configuration) {
        if (configuration instanceof K8ActiveMQConfiguration) {
            ((K8ActiveMQConfiguration) configuration).setActiveMQComponent(this);
        }
        super.setConfiguration(configuration);
    }

    @Override
    protected JmsConfiguration createConfiguration() {
        K8ActiveMQConfiguration answer = new K8ActiveMQConfiguration();
        answer.setBrokerURL("tcp://"+answer.getServiceName() + ":" + answer.getPort());
        answer.setActiveMQComponent(this);
        return answer;
    }

}
