/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.jms;

/**
 * Camel activemq-subscribe connector
 */
public class ActiveMQSubscribeComponent extends AbstractActiveMQConnector {

    public ActiveMQSubscribeComponent() {
        this(null);
    }

    public ActiveMQSubscribeComponent(String componentSchema) {
        super("activemq-subscribe", componentSchema, ActiveMQSubscribeComponent.class.getName());

    }

}
